/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.scxml2.model.NodeValue;
import org.apache.commons.scxml2.model.ParsedValue;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

class ContentParserTest {

    @Test
    void testParseJson() throws Exception {
        final ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        // not by default configured, but much easier for unit-testing Java embedded JSON Strings
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        final ContentParser contentParser = new ContentParser(jsonObjectMapper);

        final String jsonObjectString = "{ /*comment*/ 'string' : 'foobar', 'int' : 1, 'boolean' : false, 'null' : null }";
        final LinkedHashMap<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("string", "foobar");
        jsonObject.put("int", 1);
        jsonObject.put("boolean", Boolean.FALSE);
        jsonObject.put("null", null);
        assertEquals(jsonObject, contentParser.parseJson(jsonObjectString));

        final String jsonArrayString = "[" + jsonObjectString + ",# yaml comment\n" + jsonObjectString+"]";
        final ArrayList<Object> jsonArray = new ArrayList<>(2);
        jsonArray.add(jsonObject);
        jsonArray.add(jsonObject);
        assertEquals(jsonArray, contentParser.parseJson(jsonArrayString));
    }

    /**
     * The XML string must be parsed as content.
     *
     * <p>{@link DocumentBuilder#parse(String)}, previously used, interpreted it as a URI.</p>
     */
    @Test
    void testParseXml() throws Exception {
        final ContentParser contentParser = new ContentParser();

        final Node node = contentParser.parseXml("<?xml version=\"1.0\"?><root attr=\"value\">text</root>");
        assertInstanceOf(Element.class, node);
        assertEquals("root", node.getNodeName());
        assertEquals("value", ((Element) node).getAttribute("attr"));
        assertEquals("text", node.getTextContent());

        final ParsedValue parsedValue = contentParser.parseContent("<?xml version=\"1.0\"?><root attr=\"value\">text</root>");
        assertInstanceOf(NodeValue.class, parsedValue);
        assertEquals("root", ((Node) parsedValue.getValue()).getNodeName());

        // Round trip: the serialized node parses back into an equivalent node
        final String xml = contentParser.toXml(node);
        assertTrue(xml.contains("<root attr=\"value\">text</root>"), xml);
        assertEquals("text", contentParser.parseXml(xml).getTextContent());
    }

    @Test
    void testSpaceNormalizeContent() {
        assertNull(ContentParser.spaceNormalizeContent(null));
        assertEquals("", ContentParser.spaceNormalizeContent(""));
        assertEquals("a", ContentParser.spaceNormalizeContent("a"));
        assertEquals("a", ContentParser.spaceNormalizeContent(" a"));
        assertEquals("a", ContentParser.spaceNormalizeContent("a "));
        assertEquals("a", ContentParser.spaceNormalizeContent(" a "));
        assertEquals("a b c", ContentParser.spaceNormalizeContent("  a\tb \n \r c  "));
    }

    @Test
    void testTrimContent() {
        assertNull(ContentParser.trimContent(null));
        assertEquals("", ContentParser.trimContent(""));
        assertEquals("", ContentParser.trimContent(" "));
        assertEquals("", ContentParser.trimContent("  "));
        assertEquals("", ContentParser.trimContent("   "));
        assertEquals("", ContentParser.trimContent("\t\n\r"));
        assertEquals("a", ContentParser.trimContent("a"));
        assertEquals("a", ContentParser.trimContent(" a"));
        assertEquals("a", ContentParser.trimContent("a "));
        assertEquals("a", ContentParser.trimContent(" a "));
    }
}
