/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.scxml2.model.JsonValue;
import org.apache.commons.scxml2.model.NodeValue;
import org.apache.commons.scxml2.model.ParsedValue;
import org.apache.commons.scxml2.model.TextValue;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The ContentParser provides utility methods for cleaning content strings and parsing them into "raw" content model Objects
 */
public class ContentParser {

    public static final ContentParser DEFAULT_PARSER = new ContentParser();

    /**
     * Check if content starts with JSON object '{' or array '[' marker
     * @param content text to check
     * @return true if content start with '{' or '[' character
     */
    public static boolean hasJsonSignature(final String content) {
        final char c = !content.isEmpty() ? content.charAt(0) : 0;
        return c == '{' || c == '[';
    }

    /**
     * Check if content indicates its an XML document
     * @param content content to check
     * @return true if content indicates its an XML document
     */
    public static boolean hasXmlSignature(final String content) {
        return content != null && content.startsWith("<?xml ");
    }

    /**
     * Check if a character is whitespace (space, tab, newline, cr) or not
     * @param c character to check
     * @return true if character is whitespace
     */
    public static boolean isWhiteSpace(final char c) {
        return c==' ' || c=='\n' || c=='\t' || c=='\r';
    }

    /**
     * Space normalize content string, trimming pre/post-fixed whitespace and collapsing embedded whitespaces to
     * single space.
     * @param content content to space-normalize
     * @return space-normalized content
     */
    public static String spaceNormalizeContent(final String content) {
        if (content != null) {
            int index = 0;
            final int length = content.length();
            final StringBuilder buffer = new StringBuilder(length);
            boolean whiteSpace = false;
            while (index < length) {
                if (isWhiteSpace(content.charAt(index))) {
                    if (!whiteSpace && buffer.length() > 0) {
                        buffer.append(' ');
                        whiteSpace = true;
                    }
                }
                else {
                    buffer.append(content.charAt(index));
                    whiteSpace = false;
                }
                index++;
            }
            if (whiteSpace) {
                buffer.setLength(buffer.length()-1);
            }
            return buffer.toString();
        }
        return null;
    }

    /**
     * Trim pre/post-fixed whitespace from content string
     * @param content content to trim
     * @return trimmed content
     */
    public static String trimContent(final String content) {
        if (content != null) {
            int start = 0;
            int length = content.length();
            while (start < length && isWhiteSpace(content.charAt(start))) {
                start++;
            }
            while (length > start && isWhiteSpace(content.charAt(length - 1))) {
                length--;
            }
            if (start == length) {
                return "";
            }
            return content.substring(start, length);
        }
        return null;
    }

    /**
     * Jackson JSON ObjectMapper
     */
    private final ObjectMapper jsonObjectMapper;

    /**
     * Default constructor initializing a Jackson ObjectMapper allowing embedded comments, including YAML style
     */
    public ContentParser() {
        this.jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
    }

    /**
     * Constructor with a custom configured Jackson ObjectMapper
     * @param jsonObjectMapper custom configured Jackson ObjectMapper
     */
    public ContentParser(final ObjectMapper jsonObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
    }

    /**
     * Parse a string into a ParsedValue content object, following the SCXML rules as specified for the ECMAscript (section B.2.1) Data Model
     * <ul>
     *   <li>if the content can be interpreted as JSON, it will be parsed as JSON into an 'raw' object model</li>
     *   <li>if the content can be interpreted as XML, it will be parsed into a XML DOM element</li>
     *   <li>otherwise the content will be treated (cleaned) as a space-normalized string literal</li>
     * </ul>
     * @param content the content to parse
     * @return the parsed content object
     * @throws IOException In case of parsing exceptions
     */
    public ParsedValue parseContent(final String content) throws IOException {
        if (content != null) {
            final String src = trimContent(content);
            if (hasJsonSignature(src)) {
                return new JsonValue(parseJson(src), false);
            }
            if (hasXmlSignature(src)) {
                return new NodeValue(parseXml(src));
            }
            return new TextValue(spaceNormalizeContent(src), false);
        }
        return null;
    }

    /**
     * Parse and map JSON string to 'raw' Java Objects: object -&gt; LinkedHashMap, array -&gt; ArrayList
     * @param jsonString JSON string to parse
     * @return 'raw' mapped Java Object for JSON string
     * @throws IOException In case of parsing exceptions
     */
    public Object parseJson(final String jsonString) throws IOException {
        return jsonObjectMapper.readValue(jsonString, Object.class);
    }

    /**
     * Load a resource (URL) as an UTF-8 encoded content string to be parsed into a ParsedValue content object through {@link #parseContent(String)}
     * @param resourceURL Resource URL to load content from
     * @return the parsed content object
     * @throws IOException In case of loading or parsing exceptions
     */
    public ParsedValue parseResource(final String resourceURL) throws IOException {
        try (InputStream in = new URL(resourceURL).openStream()) {
            final String content = IOUtils.toString(in, StandardCharsets.UTF_8);
            return parseContent(content);
        }
    }

    /**
     * Parse an XML String and return the document element
     * @param xmlString XML String to parse
     * @return document element
     * @throws IOException if IO error occurs while parsing XML
     */
    public Node parseXml(final String xmlString) throws IOException {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlString);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
        return doc != null ? doc.getDocumentElement() : null;
    }

    /**
     * Transforms a jsonObject to a json String
     * @param jsonObject object to transform
     * @return json string
     * @throws IOException if IO error occurs while serializing it to JSON
     */
    public String toJson(final Object jsonObject) throws IOException {
        return jsonObjectMapper.writeValueAsString(jsonObject);
    }

    /**
     * Transforms a XML Node to XML
     * @param node node to transform
     * @return XML string
     * @throws IOException if IO error occurs while serializing node to XML
     */
    public String toXml(final Node node) throws IOException {
        try {
            final StringWriter writer = new StringWriter();
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final Properties outputProps = new Properties();
            outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "no");
            outputProps.put(OutputKeys.STANDALONE, "no");
            outputProps.put(OutputKeys.INDENT, "yes");
            transformer.setOutputProperties(outputProps);
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (final TransformerException e) {
            throw new IOException(e);
        }
    }
}
