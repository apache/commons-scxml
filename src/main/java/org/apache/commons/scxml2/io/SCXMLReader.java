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
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.PathResolver;
import org.apache.commons.scxml2.env.SimpleErrorHandler;
import org.apache.commons.scxml2.env.URLResolver;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.ActionsContainer;
import org.apache.commons.scxml2.model.Assign;
import org.apache.commons.scxml2.model.Cancel;
import org.apache.commons.scxml2.model.Content;
import org.apache.commons.scxml2.model.ContentContainer;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.Data;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.Else;
import org.apache.commons.scxml2.model.ElseIf;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Executable;
import org.apache.commons.scxml2.model.ExternalContent;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.Finalize;
import org.apache.commons.scxml2.model.Foreach;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.If;
import org.apache.commons.scxml2.model.Initial;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.Log;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.NamespacePrefixesHolder;
import org.apache.commons.scxml2.model.OnEntry;
import org.apache.commons.scxml2.model.OnExit;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.Param;
import org.apache.commons.scxml2.model.ParamsContainer;
import org.apache.commons.scxml2.model.Raise;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Script;
import org.apache.commons.scxml2.model.Send;
import org.apache.commons.scxml2.model.SimpleTransition;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionType;
import org.apache.commons.scxml2.model.TransitionalState;
import org.apache.commons.scxml2.model.Var;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>The SCXMLReader provides the ability to read a SCXML document into
 * the Java object model provided in the model package.</p>
 *
 * <p>See latest version of the SCXML Working Draft for more details.</p>
 *
 * <p><b>NOTE:</b> The SCXMLReader assumes that the SCXML document to be
 * parsed is well-formed and correct. If that assumption does not hold,
 * any subsequent behavior is undefined.</p>
 *
 * @since 1.0
 */
public final class SCXMLReader {

    //---------------------- PRIVATE CONSTANTS ----------------------//
    //---- NAMESPACES ----//
    /**
     * The SCXML namespace that this Reader is built for. Any document
     * that is intended to be parsed by this reader <b>must</b>
     * bind the SCXML elements to this namespace.
     */
    private static final String XMLNS_SCXML =
            "http://www.w3.org/2005/07/scxml";

    /**
     * The namespace that defines any custom actions defined by the Commons
     * SCXML implementation. Any document that intends to use these custom
     * actions needs to ensure that they are in the correct namespace. Use
     * of actions in this namespace makes the document non-portable across
     * implementations.
     */
    private static final String XMLNS_COMMONS_SCXML =
            "http://commons.apache.org/scxml";

    /**
     * The version attribute value the SCXML element <em>must</em> have as stated by the spec: 3.2.1
     */
    private static final String SCXML_REQUIRED_VERSION = "1.0";
    /**
     * The default namespace for attributes.
     */
    private static final String XMLNS_DEFAULT = null;

    //---- ERROR MESSAGES ----//
    /**
     * Null URL passed as argument.
     */
    private static final String ERR_NULL_URL = "Cannot parse null URL";

    /**
     * Null path passed as argument.
     */
    private static final String ERR_NULL_PATH = "Cannot parse null path";

    /**
     * Null InputStream passed as argument.
     */
    private static final String ERR_NULL_ISTR = "Cannot parse null InputStream";

    /**
     * Null Reader passed as argument.
     */
    private static final String ERR_NULL_READ = "Cannot parse null Reader";

    /**
     * Null Source passed as argument.
     */
    private static final String ERR_NULL_SRC = "Cannot parse null Source";

    /**
     * Error message while attempting to define a custom action which does
     * not extend the Commons SCXML Action base class.
     */
    private static final String ERR_CUSTOM_ACTION_TYPE = "Custom actions list"
            + " contained unknown object, class not a Commons SCXML Action class subtype: ";

    /**
     * Parser configuration error while trying to parse stream to DOM node(s).
     */
    private static final String ERR_PARSER_CFG = "ParserConfigurationException while trying"
            + " to parse stream into DOM node(s).";

    /**
     * Error message when the URI in a &lt;state&gt;'s &quot;src&quot;
     * attribute does not point to a valid SCXML document, and thus cannot be
     * parsed.
     */
    private static final String ERR_STATE_SRC =
            "Source attribute in <state src=\"{0}\"> cannot be parsed";

    /**
     * Error message when the target of the URI fragment in a &lt;state&gt;'s
     * &quot;src&quot; attribute is not defined in the referenced document.
     */
    private static final String ERR_STATE_SRC_FRAGMENT = "URI Fragment in "
            + "<state src=\"{0}\"> is an unknown state in referenced document";

    /**
     * Error message when the target of the URI fragment in a &lt;state&gt;'s
     * &quot;src&quot; attribute is not a &lt;state&gt; or &lt;final&gt; in
     * the referenced document.
     */
    private static final String ERR_STATE_SRC_FRAGMENT_TARGET = "URI Fragment"
            + " in <state src=\"{0}\"> does not point to a <state> or <final>";

    /**
     * Error message when the target of the URI fragment in a &lt;state&gt;'s
     * &quot;src&quot; attribute is not a &lt;state&gt; or &lt;final&gt; in
     * the referenced document.
     */
    private static final String ERR_REQUIRED_ATTRIBUTE_MISSING = "<{0}> is missing"
            +" required attribute \"{1}\" value at {2}";

    /**
     * Error message when the target of the URI fragment in a &lt;state&gt;'s
     * &quot;src&quot; attribute is not a &lt;state&gt; or &lt;final&gt; in
     * the referenced document.
     */
    private static final String ERR_ATTRIBUTE_NOT_BOOLEAN = "Illegal value \"{0}\""
            + "for attribute \"{1}\" in element <{2}> at {3}."
            +" Only the value \"true\" or \"false\" is allowed.";

    /**
     * Error message when the element (state|parallel|final|history) uses an id value
     * with the reserved prefix {@link SCXML#GENERATED_TT_ID_PREFIX}.
     */
    private static final String ERR_RESERVED_ID_PREFIX = "Reserved id prefix \""
            +SCXML.GENERATED_TT_ID_PREFIX+"\" used for <{0} id=\"{1}\"> at {2}";

    /**
     * Error message when the target of the URI fragment in a &lt;state&gt;'s
     * &quot;src&quot; attribute is not defined in the referenced document.
     */
    private static final String ERR_UNSUPPORTED_TRANSITION_TYPE = "Unsupported transition type "
            + "for <transition type=\"{0}\"> at {1}.";

    /**
     * Error message when the target of the URI fragment in a &lt;state&gt;'s
     * &quot;src&quot; attribute is not a &lt;state&gt; or &lt;final&gt; in
     * the referenced document.
     */
    private static final String ERR_INVALID_VERSION = "The <scxml> element defines"
            +" an unsupported version \"{0}\", only version \"1.0\" is supported.";

    //--------------------------- XML VOCABULARY ---------------------------//
    //---- ELEMENT NAMES ----//
    private static final String ELEM_ASSIGN = "assign";
    private static final String ELEM_CANCEL = "cancel";
    private static final String ELEM_CONTENT = "content";
    private static final String ELEM_DATA = "data";
    private static final String ELEM_DATAMODEL = "datamodel";
    private static final String ELEM_ELSE = "else";
    private static final String ELEM_ELSEIF = "elseif";
    private static final String ELEM_RAISE = "raise";
    private static final String ELEM_FINAL = "final";
    private static final String ELEM_FINALIZE = "finalize";
    private static final String ELEM_HISTORY = "history";
    private static final String ELEM_IF = "if";
    private static final String ELEM_INITIAL = "initial";
    private static final String ELEM_INVOKE = "invoke";
    private static final String ELEM_FOREACH = "foreach";
    private static final String ELEM_LOG = "log";
    private static final String ELEM_ONENTRY = "onentry";
    private static final String ELEM_ONEXIT = "onexit";
    private static final String ELEM_PARALLEL = "parallel";
    private static final String ELEM_PARAM = "param";
    private static final String ELEM_SCRIPT = "script";
    private static final String ELEM_SCXML = "scxml";
    private static final String ELEM_SEND = "send";
    private static final String ELEM_STATE = "state";
    private static final String ELEM_TRANSITION = "transition";
    private static final String ELEM_VAR = "var";

    //---- ATTRIBUTE NAMES ----//
    private static final String ATTR_ARRAY = "array";
    private static final String ATTR_ATTR = "attr";
    private static final String ATTR_AUTOFORWARD = "autoforward";
    private static final String ATTR_COND = "cond";
    private static final String ATTR_DATAMODEL = "datamodel";
    private static final String ATTR_DELAY = "delay";
    private static final String ATTR_DELAYEXPR = "delayexpr";
    private static final String ATTR_EVENT = "event";
    private static final String ATTR_EVENTEXPR = "eventexpr";
    private static final String ATTR_EXMODE = "exmode";
    private static final String ATTR_EXPR = "expr";
    private static final String ATTR_HINTS = "hints";
    private static final String ATTR_ID = "id";
    private static final String ATTR_IDLOCATION = "idlocation";
    private static final String ATTR_INDEX = "index";
    private static final String ATTR_INITIAL = "initial";
    private static final String ATTR_ITEM = "item";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_LOCATION = "location";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_NAMELIST = "namelist";
    private static final String ATTR_PROFILE = "profile";
    private static final String ATTR_SENDID = "sendid";
    private static final String ATTR_SENDIDEXPR = "sendidexpr";
    private static final String ATTR_SRC = "src";
    private static final String ATTR_SRCEXPR = "srcexpr";
    private static final String ATTR_TARGET = "target";
    private static final String ATTR_TARGETEXPR = "targetexpr";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_TYPEEXPR = "typeexpr";
    private static final String ATTR_VERSION = "version";

    //------------------------- PUBLIC API METHODS -------------------------//
    /*
     * Public methods
     */
    /**
     * Parse the SCXML document at the supplied path.
     *
     * @param scxmlPath The real path to the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final String scxmlPath)
            throws IOException, ModelException, XMLStreamException {

        return read(scxmlPath, new Configuration());
    }

    /**
     * Parse the SCXML document at the supplied path with the given {@link Configuration}.
     *
     * @param scxmlPath The real path to the SCXML document.
     * @param configuration The {@link Configuration} to use when parsing the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final String scxmlPath, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (scxmlPath == null) {
            throw new IllegalArgumentException(ERR_NULL_PATH);
        }
        SCXML scxml = readInternal(configuration, null, scxmlPath, null, null, null);
        if (scxml != null) {
            ModelUpdater.updateSCXML(scxml);
        }
        return scxml;
    }

    /**
     * Parse the SCXML document at the supplied {@link URL}.
     *
     * @param scxmlURL The SCXML document {@link URL} to parse.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final URL scxmlURL)
            throws IOException, ModelException, XMLStreamException {

        return read(scxmlURL, new Configuration());
    }

    /**
     * Parse the SCXML document at the supplied {@link URL} with the given {@link Configuration}.
     *
     * @param scxmlURL The SCXML document {@link URL} to parse.
     * @param configuration The {@link Configuration} to use when parsing the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final URL scxmlURL, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (scxmlURL == null) {
            throw new IllegalArgumentException(ERR_NULL_URL);
        }
        SCXML scxml = readInternal(configuration, scxmlURL, null, null, null, null);
        if (scxml != null) {
            ModelUpdater.updateSCXML(scxml);
        }
        return scxml;
    }

    /**
     * Parse the SCXML document supplied by the given {@link InputStream}.
     *
     * @param scxmlStream The {@link InputStream} supplying the SCXML document to parse.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final InputStream scxmlStream)
            throws IOException, ModelException, XMLStreamException {

        return read(scxmlStream, new Configuration());
    }

    /**
     * Parse the SCXML document supplied by the given {@link InputStream} with the given {@link Configuration}.
     *
     * @param scxmlStream The {@link InputStream} supplying the SCXML document to parse.
     * @param configuration The {@link Configuration} to use when parsing the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final InputStream scxmlStream, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (scxmlStream == null) {
            throw new IllegalArgumentException(ERR_NULL_ISTR);
        }
        SCXML scxml = readInternal(configuration, null, null, scxmlStream, null, null);
        if (scxml != null) {
            ModelUpdater.updateSCXML(scxml);
        }
        return scxml;
    }

    /**
     * Parse the SCXML document supplied by the given {@link Reader}.
     *
     * @param scxmlReader The {@link Reader} supplying the SCXML document to parse.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final Reader scxmlReader)
            throws IOException, ModelException, XMLStreamException {

        return read(scxmlReader, new Configuration());
    }

    /**
     * Parse the SCXML document supplied by the given {@link Reader} with the given {@link Configuration}.
     *
     * @param scxmlReader The {@link Reader} supplying the SCXML document to parse.
     * @param configuration The {@link Configuration} to use when parsing the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final Reader scxmlReader, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (scxmlReader == null) {
            throw new IllegalArgumentException(ERR_NULL_READ);
        }
        SCXML scxml = readInternal(configuration, null, null, null, scxmlReader, null);
        if (scxml != null) {
            ModelUpdater.updateSCXML(scxml);
        }
        return scxml;
    }

    /**
     * Parse the SCXML document supplied by the given {@link Source}.
     *
     * @param scxmlSource The {@link Source} supplying the SCXML document to parse.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final Source scxmlSource)
            throws IOException, ModelException, XMLStreamException {

        return read(scxmlSource, new Configuration());
    }

    /**
     * Parse the SCXML document supplied by the given {@link Source} with the given {@link Configuration}.
     *
     * @param scxmlSource The {@link Source} supplying the SCXML document to parse.
     * @param configuration The {@link Configuration} to use when parsing the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    public static SCXML read(final Source scxmlSource, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        if (scxmlSource == null) {
            throw new IllegalArgumentException(ERR_NULL_SRC);
        }
        SCXML scxml = readInternal(configuration, null, null, null, null, scxmlSource);
        if (scxml != null) {
            ModelUpdater.updateSCXML(scxml);
        }
        return scxml;
    }

    //---------------------- PRIVATE UTILITY METHODS ----------------------//
    /**
     * Parse the SCXML document at the supplied {@link URL} using the supplied {@link Configuration}, but do not
     * wire up the object model to be usable just yet. Exactly one of the url, path, stream, reader or source
     * parameters must be provided.
     *
     * @param configuration The {@link Configuration} to use when parsing the SCXML document.
     * @param scxmlURL The optional SCXML document {@link URL} to parse.
     * @param scxmlPath The optional real path to the SCXML document as a string.
     * @param scxmlStream The optional {@link InputStream} providing the SCXML document.
     * @param scxmlReader The optional {@link Reader} providing the SCXML document.
     * @param scxmlSource The optional {@link Source} providing the SCXML document.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document
     *         (not wired up to be immediately usable).
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static SCXML readInternal(final Configuration configuration, final URL scxmlURL, final String scxmlPath,
                                      final InputStream scxmlStream, final Reader scxmlReader, final Source scxmlSource)
            throws IOException, ModelException, XMLStreamException {

        if (configuration.pathResolver == null) {
            if (scxmlURL != null) {
                configuration.pathResolver = new URLResolver(scxmlURL);
            } else if (scxmlPath != null) {
                configuration.pathResolver = new URLResolver(new URL(scxmlPath));
            }
        }

        XMLStreamReader reader = getReader(configuration, scxmlURL, scxmlPath, scxmlStream, scxmlReader, scxmlSource);

        return readDocument(reader, configuration);
    }

    /*
     * Private utility functions for reading the SCXML document.
     */
    /**
     * Read the SCXML document through the {@link XMLStreamReader}.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     *
     * @return The parsed output, the Commons SCXML object model corresponding to the SCXML document
     *         (not wired up to be immediately usable).
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static SCXML readDocument(final XMLStreamReader reader, final Configuration configuration)
            throws IOException, ModelException, XMLStreamException {

        SCXML scxml = new SCXML();
        while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_SCXML.equals(name)) {
                            readSCXML(reader, configuration, scxml);
                        } else {
                            reportIgnoredElement(reader, configuration, "DOCUMENT_ROOT", nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, "DOCUMENT_ROOT", nsURI, name);
                    }
                    break;
                case XMLStreamConstants.NAMESPACE:
                    System.err.println(reader.getNamespaceCount());
                    break;
                default:
            }
        }
        return scxml;
    }

    /**
     * Read the contents of this &lt;scxml&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readSCXML(final XMLStreamReader reader, final Configuration configuration, final SCXML scxml)
            throws IOException, ModelException, XMLStreamException {

        scxml.setDatamodelName(readAV(reader, ATTR_DATAMODEL));
        scxml.setExmode(readAV(reader, ATTR_EXMODE));
        scxml.setInitial(readAV(reader, ATTR_INITIAL));
        scxml.setName(readAV(reader, ATTR_NAME));
        scxml.setProfile(readAV(reader, ATTR_PROFILE));
        scxml.setVersion(readRequiredAV(reader, ELEM_SCXML, ATTR_VERSION));
        if (!SCXML_REQUIRED_VERSION.equals(scxml.getVersion())) {
            throw new ModelException(new MessageFormat(ERR_INVALID_VERSION).format(new Object[] {scxml.getVersion()}));
        }
        readNamespaces(configuration, scxml);

        boolean hasGlobalScript = false;

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_STATE.equals(name)) {
                            readState(reader, configuration, scxml, null);
                        } else if (ELEM_PARALLEL.equals(name)) {
                            readParallel(reader, configuration, scxml, null);
                        } else if (ELEM_FINAL.equals(name)) {
                            readFinal(reader, configuration, scxml, null);
                        } else if (ELEM_DATAMODEL.equals(name)) {
                            readDatamodel(reader, configuration, scxml, null);
                        } else if (ELEM_SCRIPT.equals(name) && !hasGlobalScript) {
                            readGlobalScript(reader, configuration, scxml);
                            hasGlobalScript = true;
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_SCXML, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_SCXML, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }
    }

    /**
     * Read the contents of this &lt;state&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     * @param parent The parent {@link TransitionalState} for this state (null for top level state).
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readState(final XMLStreamReader reader, final Configuration configuration, final SCXML scxml,
                                  final TransitionalState parent)
            throws IOException, ModelException, XMLStreamException {

        State state = new State();
        state.setId(readOrGeneratedTransitionTargetId(reader, scxml, ELEM_STATE));
        String initial = readAV(reader, ATTR_INITIAL);
        if (initial != null) {
            state.setFirst(initial);
        }
        String src = readAV(reader, ATTR_SRC);
        if (src != null) {
            String source = src;
            Configuration copy = new Configuration(configuration);
            if (copy.parent == null) {
                copy.parent = scxml;
            }
            if (configuration.pathResolver != null) {
                source = configuration.pathResolver.resolvePath(src);
                copy.pathResolver = configuration.pathResolver.getResolver(src);
            }
            readTransitionalStateSrc(copy, source, state);
        }

        if (parent == null) {
            scxml.addChild(state);
        } else if (parent instanceof State) {
            ((State)parent).addChild(state);
        }
        else {
            ((Parallel)parent).addChild(state);
        }
        scxml.addTarget(state);
        if (configuration.parent != null) {
            configuration.parent.addTarget(state);
        }

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_TRANSITION.equals(name)) {
                            state.addTransition(readTransition(reader, configuration));
                        } else if (ELEM_STATE.equals(name)) {
                            readState(reader, configuration, scxml, state);
                        } else if (ELEM_INITIAL.equals(name)) {
                            readInitial(reader, configuration, state);
                        } else if (ELEM_FINAL.equals(name)) {
                            readFinal(reader, configuration, scxml, state);
                        } else if (ELEM_ONENTRY.equals(name)) {
                            readOnEntry(reader, configuration, state);
                        } else if (ELEM_ONEXIT.equals(name)) {
                            readOnExit(reader, configuration, state);
                        } else if (ELEM_PARALLEL.equals(name)) {
                            readParallel(reader, configuration, scxml, state);
                        } else if (ELEM_DATAMODEL.equals(name)) {
                            readDatamodel(reader, configuration, null, state);
                        } else if (ELEM_INVOKE.equals(name)) {
                            readInvoke(reader, configuration, state);
                        } else if (ELEM_HISTORY.equals(name)) {
                            readHistory(reader, configuration, scxml, state);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_STATE, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_STATE, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }
    }

    /**
     * Read the contents of this &lt;parallel&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     * @param parent The parent {@link TransitionalState} for this parallel (null for top level state).
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readParallel(final XMLStreamReader reader, final Configuration configuration, final SCXML scxml,
                                     final TransitionalState parent)
            throws IOException, ModelException, XMLStreamException {

        Parallel parallel = new Parallel();
        parallel.setId(readOrGeneratedTransitionTargetId(reader, scxml, ELEM_PARALLEL));
        String src = readAV(reader, ATTR_SRC);
        if (src != null) {
            String source = src;
            Configuration copy = new Configuration(configuration);
            if (copy.parent == null) {
                copy.parent = scxml;
            }
            if (configuration.pathResolver != null) {
                source = configuration.pathResolver.resolvePath(src);
                copy.pathResolver = configuration.pathResolver.getResolver(src);
            }
            readTransitionalStateSrc(copy, source, parallel);
        }

        if (parent == null) {
            scxml.addChild(parallel);
        } else if (parent instanceof State) {
            ((State)parent).addChild(parallel);
        }
        else {
            ((Parallel)parent).addChild(parallel);
        }
        scxml.addTarget(parallel);
        if (configuration.parent != null) {
            configuration.parent.addTarget(parallel);
        }

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_TRANSITION.equals(name)) {
                            parallel.addTransition(readTransition(reader, configuration));
                        } else if (ELEM_STATE.equals(name)) {
                            readState(reader, configuration, scxml, parallel);
                        } else if (ELEM_PARALLEL.equals(name)) {
                            readParallel(reader, configuration, scxml, parallel);
                        } else if (ELEM_ONENTRY.equals(name)) {
                            readOnEntry(reader, configuration, parallel);
                        } else if (ELEM_ONEXIT.equals(name)) {
                            readOnExit(reader, configuration, parallel);
                        } else if (ELEM_DATAMODEL.equals(name)) {
                            readDatamodel(reader, configuration, null, parallel);
                        } else if (ELEM_INVOKE.equals(name)) {
                            readInvoke(reader, configuration, parallel);
                        } else if (ELEM_HISTORY.equals(name)) {
                            readHistory(reader, configuration, scxml, parallel);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_PARALLEL, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_PARALLEL, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }
    }

    /**
     * Read the contents of this &lt;final&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     * @param parent The parent {@link State} for this final (null for top level state).
     *
     * @throws IOException An IO error during parsing.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readFinal(final XMLStreamReader reader, final Configuration configuration, final SCXML scxml,
                                  final State parent)
            throws XMLStreamException, ModelException, IOException {

        Final end = new Final();
        end.setId(readOrGeneratedTransitionTargetId(reader, scxml, ELEM_FINAL));

        if (parent == null) {
            scxml.addChild(end);
        } else {
            parent.addChild(end);
        }

        scxml.addTarget(end);
        if (configuration.parent != null) {
            configuration.parent.addTarget(end);
        }

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_ONENTRY.equals(name)) {
                            readOnEntry(reader, configuration, end);
                        } else if (ELEM_ONEXIT.equals(name)) {
                            readOnExit(reader, configuration, end);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_FINAL, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_FINAL, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }
    }

    /**
     * Parse the contents of the SCXML document that this "src" attribute value of a &lt;state&gt; or &lt;parallel&gt;
     * element points to. Without a URL fragment, the entire state machine is imported as contents of the
     * &lt;state&gt; or &lt;parallel&gt;. If a URL fragment is present, the fragment must specify the id of the
     * corresponding &lt;state&gt; or &lt;parallel&gt; to import.
     *
     * @param configuration The {@link Configuration} to use while parsing.
     * @param src The "src" attribute value.
     * @param ts The parent {@link TransitionalState} that specifies this "src" attribute.
     *
     * @throws IOException An IO error during parsing.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readTransitionalStateSrc(final Configuration configuration, final String src,
                                                 final TransitionalState ts)
            throws IOException, ModelException, XMLStreamException {

        // Check for URI fragment
        String[] fragments = src.split("#", 2);
        String location = fragments[0];
        String fragment = null;
        if (fragments.length > 1) {
            fragment = fragments[1];
        }

        // Parse external document
        SCXML externalSCXML;
        try {
            externalSCXML = SCXMLReader.readInternal(configuration, new URL(location), null, null, null, null);
        } catch (Exception e) {
            MessageFormat msgFormat = new MessageFormat(ERR_STATE_SRC);
            String errMsg = msgFormat.format(new Object[] {src});
            throw new ModelException(errMsg + " : " + e.getMessage(), e);
        }

        // Pull in the parts of the external document as needed
        if (fragment == null) {
            // All targets pulled in since its not a src fragment
            if (ts instanceof State) {
                State s = (State) ts;
                Initial ini = new Initial();
                SimpleTransition t = new SimpleTransition();
                t.setNext(externalSCXML.getInitial());
                ini.setTransition(t);
                s.setInitial(ini);
                for (EnterableState child : externalSCXML.getChildren()) {
                    s.addChild(child);
                }
                s.setDatamodel(externalSCXML.getDatamodel());
            } else if (ts instanceof Parallel) {
                // TODO src attribute for <parallel>
            }
        } else {
            // Need to pull in only descendent targets
            Object source = externalSCXML.getTargets().get(fragment);
            if (source == null) {
                MessageFormat msgFormat = new MessageFormat(ERR_STATE_SRC_FRAGMENT);
                String errMsg = msgFormat.format(new Object[] {src});
                throw new ModelException(errMsg);
            }
            if (source instanceof State && ts instanceof State) {
                State s = (State) ts;
                State include = (State) source;
                for (OnEntry onentry : include.getOnEntries()) {
                    s.addOnEntry(onentry);
                }
                for (OnExit onexit : include.getOnExits()) {
                    s.addOnExit(onexit);
                }
                s.setDatamodel(include.getDatamodel());
                List<History> histories = include.getHistory();
                for (History h : histories) {
                    s.addHistory(h);
                    configuration.parent.addTarget(h);
                }
                for (EnterableState child : include.getChildren()) {
                    s.addChild(child);
                    configuration.parent.addTarget(child);
                    readInExternalTargets(configuration.parent, child);
                }
                for (Invoke invoke : include.getInvokes()) {
                    s.addInvoke(invoke);
                }
                if (include.getInitial() != null) {
                    s.setInitial(include.getInitial());
                }
                List<Transition> transitions = include.getTransitionsList();
                for (Transition t : transitions) {
                    s.addTransition(t);
                }
            } else if (ts instanceof Parallel && source instanceof Parallel) {
                // TODO src attribute for <parallel>
            } else {
                MessageFormat msgFormat =
                        new MessageFormat(ERR_STATE_SRC_FRAGMENT_TARGET);
                String errMsg = msgFormat.format(new Object[] {src});
                throw new ModelException(errMsg);
            }
        }
    }

    /**
     * Add all the nested targets from given target to given parent state machine.
     *
     * @param parent The state machine
     * @param es The target to import
     */
    private static void readInExternalTargets(final SCXML parent, final EnterableState es) {
        if (es instanceof TransitionalState) {
            for (History h : ((TransitionalState)es).getHistory()) {
                parent.addTarget(h);
            }
            for (EnterableState child : ((TransitionalState) es).getChildren()) {
                parent.addTarget(child);
                readInExternalTargets(parent, child);
            }
        }
    }

    /**
     * Read the contents of this &lt;datamodel&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     * @param parent The parent {@link TransitionalState} for this datamodel (null for top level).
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readDatamodel(final XMLStreamReader reader, final Configuration configuration,
                                      final SCXML scxml, final TransitionalState parent)
            throws XMLStreamException, ModelException {

        Datamodel dm = new Datamodel();

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_DATA.equals(name)) {
                            readData(reader, configuration, dm);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_DATAMODEL, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_DATAMODEL, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }

        if (parent == null) {
            scxml.setDatamodel(dm);
        } else {
            parent.setDatamodel(dm);
        }
    }

    /**
     * Read the contents of this &lt;data&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param dm The parent {@link Datamodel} for this data.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readData(final XMLStreamReader reader, final Configuration configuration, final Datamodel dm)
            throws XMLStreamException, ModelException {

        Data datum = new Data();
        datum.setId(readRequiredAV(reader, ELEM_DATA, ATTR_ID));
        datum.setExpr(readAV(reader, ATTR_EXPR));
        readNamespaces(configuration, datum);
        datum.setNode(readNode(reader, configuration, XMLNS_SCXML, ELEM_DATA, new String[]{"id"}));
        dm.addData(datum);
    }

    /**
     * Read the contents of this &lt;invoke&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param parent The parent {@link TransitionalState} for this invoke.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readInvoke(final XMLStreamReader reader, final Configuration configuration,
                                   final TransitionalState parent)
            throws XMLStreamException, ModelException {

        Invoke invoke = new Invoke();
        invoke.setId(readAV(reader, ATTR_ID));
        invoke.setSrc(readAV(reader, ATTR_SRC));
        invoke.setSrcexpr(readAV(reader, ATTR_SRCEXPR));
        invoke.setType(readAV(reader, ATTR_TYPE));
        invoke.setAutoForward(readBooleanAV(reader, ELEM_INVOKE, ATTR_AUTOFORWARD));
        invoke.setPathResolver(configuration.pathResolver);
        readNamespaces(configuration, invoke);

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_PARAM.equals(name)) {
                            readParam(reader, configuration, invoke);
                        } else if (ELEM_FINALIZE.equals(name)) {
                            readFinalize(reader, configuration, parent, invoke);
                        } else if (ELEM_CONTENT.equals(name)) {
                            readContent(reader, configuration, invoke);
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_INVOKE, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_INVOKE, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }

        parent.addInvoke(invoke);
    }

    /**
     * Read the contents of this &lt;param&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param parent The parent {@link org.apache.commons.scxml2.model.ParamsContainer} for this param.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readParam(final XMLStreamReader reader, final Configuration configuration,
                                  final ParamsContainer parent)
            throws XMLStreamException, ModelException {

        Param param = new Param();
        param.setName(readRequiredAV(reader, ELEM_PARAM, ATTR_NAME));
        String location = readAV(reader, ATTR_LOCATION);
        String expr = readAV(reader, ATTR_EXPR);
        if (expr != null) {
            if (location != null) {
                reportConflictingAttribute(reader, configuration, ELEM_PARAM, ATTR_LOCATION, ATTR_EXPR);
            }
            else {
                param.setExpr(expr);
            }
        }
        else if (location == null) {
            // force error missing required location or expr: use location attr for this
            param.setLocation(readRequiredAV(reader, ELEM_PARAM, ATTR_LOCATION));
        }
        else {
            param.setLocation(location);
        }
        readNamespaces(configuration, param);
        parent.getParams().add(param);
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this &lt;finalize&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param state The {@link TransitionalState} which contains the parent {@link Invoke}.
     * @param invoke The parent {@link Invoke} for this finalize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readFinalize(final XMLStreamReader reader, final Configuration configuration,
                                     final TransitionalState state, final Invoke invoke)
            throws XMLStreamException, ModelException {

        Finalize finalize = new Finalize();
        readExecutableContext(reader, configuration, finalize, null);
        invoke.setFinalize(finalize);
        finalize.setParent(state);
    }

    /**
     * Read the contents of this &lt;content&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param contentContainer The {@link ContentContainer} for this content.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readContent(final XMLStreamReader reader, final Configuration configuration,
                                    final ContentContainer contentContainer)
            throws XMLStreamException {

        Content content = new Content();
        content.setExpr(readAV(reader, ATTR_EXPR));
        if (content.getExpr() != null) {
            skipToEndElement(reader);
        }
        else {
            Node body = readNode(reader, configuration, XMLNS_SCXML, ELEM_CONTENT, new String[]{});
            if (body.hasChildNodes()) {
                NodeList children = body.getChildNodes();
                if (children.getLength() == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
                    content.setBody(children.item(0).getNodeValue());
                }
                else {
                    content.setBody(body);
                }
            }
        }
        contentContainer.setContent(content);
    }

    /**
     * Read the contents of this &lt;initial&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param state The parent composite {@link State} for this initial.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readInitial(final XMLStreamReader reader, final Configuration configuration,
                                    final State state)
            throws XMLStreamException, ModelException {

        Initial initial = new Initial();

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_TRANSITION.equals(name)) {
                            initial.setTransition(readSimpleTransition(reader, configuration));
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_INITIAL, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_INITIAL, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }

        state.setInitial(initial);
    }

    /**
     * Read the contents of this &lt;history&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     * @param ts The parent {@link org.apache.commons.scxml2.model.TransitionalState} for this history.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readHistory(final XMLStreamReader reader, final Configuration configuration,
                                    final SCXML scxml, final TransitionalState ts)
            throws XMLStreamException, ModelException {

        History history = new History();
        history.setId(readOrGeneratedTransitionTargetId(reader, scxml, ELEM_HISTORY));
        history.setType(readAV(reader, ATTR_TYPE));

        ts.addHistory(history);
        scxml.addTarget(history);

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_TRANSITION.equals(name)) {
                            history.setTransition(readTransition(reader, configuration));
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_HISTORY, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_HISTORY, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }
    }

    /**
     * Read the contents of this &lt;onentry&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param es The parent {@link EnterableState} for this onentry.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readOnEntry(final XMLStreamReader reader, final Configuration configuration,
                                    final EnterableState es)
            throws XMLStreamException, ModelException {

        OnEntry onentry = new OnEntry();
        onentry.setRaiseEvent(readBooleanAV(reader, ELEM_ONENTRY, ATTR_EVENT));
        readExecutableContext(reader, configuration, onentry, null);
        es.addOnEntry(onentry);
    }

    /**
     * Read the contents of this &lt;onexit&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param es The parent {@link EnterableState} for this onexit.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readOnExit(final XMLStreamReader reader, final Configuration configuration,
                                   final EnterableState es)
            throws XMLStreamException, ModelException {

        OnExit onexit = new OnExit();
        onexit.setRaiseEvent(readBooleanAV(reader, ELEM_ONEXIT, ATTR_EVENT));
        readExecutableContext(reader, configuration, onexit, null);
        es.addOnExit(onexit);
    }

    /**
     * Read the contents of this simple &lt;transition&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static SimpleTransition readSimpleTransition(final XMLStreamReader reader, final Configuration configuration)
            throws XMLStreamException, ModelException {

        SimpleTransition transition = new SimpleTransition();
        transition.setNext(readAV(reader, ATTR_TARGET));
        String type = readAV(reader, ATTR_TYPE);
        if (type != null) {
            try {
                transition.setType(TransitionType.valueOf(type));
            }
            catch (IllegalArgumentException e) {
                MessageFormat msgFormat = new MessageFormat(ERR_UNSUPPORTED_TRANSITION_TYPE);
                String errMsg = msgFormat.format(new Object[] {type, reader.getLocation()});
                throw new ModelException(errMsg);
            }
        }

        readNamespaces(configuration, transition);
        readExecutableContext(reader, configuration, transition, null);

        return transition;
    }

    /**
     * Read the contents of this &lt;transition&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static Transition readTransition(final XMLStreamReader reader, final Configuration configuration)
            throws XMLStreamException, ModelException {

        Transition transition = new Transition();
        transition.setCond(readAV(reader, ATTR_COND));
        transition.setEvent(readAV(reader, ATTR_EVENT));
        transition.setNext(readAV(reader, ATTR_TARGET));
        String type = readAV(reader, ATTR_TYPE);
        if (type != null) {
            try {
                transition.setType(TransitionType.valueOf(type));
            }
            catch (IllegalArgumentException e) {
                MessageFormat msgFormat = new MessageFormat(ERR_UNSUPPORTED_TRANSITION_TYPE);
                String errMsg = msgFormat.format(new Object[] {type, reader.getLocation()});
                throw new ModelException(errMsg);
            }
        }

        readNamespaces(configuration, transition);
        readExecutableContext(reader, configuration, transition, null);

        return transition;
    }

    /**
     * Read this set of executable content elements.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} to which this content belongs.
     * @param parent The optional parent {@link ActionsContainer} if this is child content of an ActionsContainer action.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readExecutableContext(final XMLStreamReader reader, final Configuration configuration,
                                              final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        String end = "";
        if (parent != null) {
            end = parent.getContainerElementName();
        } else if (executable instanceof SimpleTransition) {
            end = ELEM_TRANSITION;
        } else if (executable instanceof OnEntry) {
            end = ELEM_ONENTRY;
        } else if (executable instanceof OnExit) {
            end = ELEM_ONEXIT;
        } else if (executable instanceof Finalize) {
            end = ELEM_FINALIZE;
        }

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_RAISE.equals(name)) {
                            readRaise(reader, configuration, executable, parent);
                        } else if (ELEM_FOREACH.equals(name)) {
                            readForeach(reader, configuration, executable, parent);
                        } else if (ELEM_IF.equals(name)) {
                            readIf(reader, configuration, executable, parent);
                        } else if (ELEM_LOG.equals(name)) {
                            readLog(reader, configuration, executable, parent);
                        } else if (ELEM_ASSIGN.equals(name)) {
                            readAssign(reader, configuration, executable, parent);
                        } else if (ELEM_SEND.equals(name)) {
                            readSend(reader, configuration, executable, parent);
                        } else if (ELEM_CANCEL.equals(name)) {
                            readCancel(reader, configuration, executable, parent);
                        } else if (ELEM_SCRIPT.equals(name)) {
                            readScript(reader, configuration, executable, parent);
                        } else if (ELEM_IF.equals(end) && ELEM_ELSEIF.equals(name)) {
                            readElseIf(reader, configuration, executable, (If) parent);
                        } else if (ELEM_IF.equals(end) && ELEM_ELSE.equals(name)) {
                            readElse(reader, configuration, executable, (If)parent);
                        } else {
                            reportIgnoredElement(reader, configuration, end, nsURI, name);
                        }
                    } else if (XMLNS_COMMONS_SCXML.equals(nsURI)) {
                        if (ELEM_VAR.equals(name)) {
                            readVar(reader, configuration, executable, parent);
                        } else {
                            reportIgnoredElement(reader, configuration, end, nsURI, name);
                        }
                    } else { // custom action
                        CustomAction customAction = null;
                        if (!configuration.customActions.isEmpty()) {
                            for (CustomAction ca : configuration.customActions) {
                                if (ca.getNamespaceURI().equals(nsURI) && ca.getLocalName().equals(name)) {
                                    customAction = ca;
                                }
                            }
                        }
                        if (customAction != null) {
                            readCustomAction(reader, configuration, customAction, executable, parent);
                        } else {
                            reportIgnoredElement(reader, configuration, end, nsURI, name);
                        }
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }
    }

    /**
     * Read the contents of this &lt;raise&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readRaise(final XMLStreamReader reader, final Configuration configuration,
                                  final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        if (executable instanceof Finalize) {
            // http://www.w3.org/TR/2013/WD-scxml-20130801/#finalize
            // [...] the executable content inside <finalize> MUST NOT raise events or invoke external actions.
            // In particular, the <send> and <raise> elements MUST NOT occur.
            reportIgnoredElement(reader, configuration, ELEM_FINALIZE, XMLNS_SCXML, ELEM_RAISE);
        }
        else {
            Raise raise = new Raise();
            raise.setEvent(readAV(reader, ATTR_EVENT));
            readNamespaces(configuration, raise);
            raise.setParent(executable);
            if (parent != null) {
                parent.addAction(raise);
            } else {
                executable.addAction(raise);
            }
            skipToEndElement(reader);
        }
    }

    /**
     * Read the contents of this &lt;if&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this &lt;if&gt; is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readIf(final XMLStreamReader reader, final Configuration configuration,
                               final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        If iff = new If();
        iff.setCond(readRequiredAV(reader, ELEM_IF, ATTR_COND));
        readNamespaces(configuration, iff);
        iff.setParent(executable);
        if (parent != null) {
            parent.addAction(iff);
        } else {
            executable.addAction(iff);
        }
        readExecutableContext(reader, configuration, executable, iff);
    }

    /**
     * Read the contents of this &lt;elseif&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param iff The parent {@link If} for this &lt;elseif&gt;.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readElseIf(final XMLStreamReader reader, final Configuration configuration,
                                   final Executable executable, final If iff)
            throws XMLStreamException, ModelException {

        ElseIf elseif = new ElseIf();
        elseif.setCond(readRequiredAV(reader, ELEM_ELSEIF, ATTR_COND));
        readNamespaces(configuration, elseif);
        elseif.setParent(executable);
        iff.addAction(elseif);
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this &lt;else&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param iff The parent {@link If} for this &lt;else&gt;.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readElse(final XMLStreamReader reader, final Configuration configuration,
                                 final Executable executable, final If iff)
            throws XMLStreamException {

        Else els = new Else();
        readNamespaces(configuration, els);
        els.setParent(executable);
        iff.addAction(els);
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this &lt;foreach&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this &lt;foreach&gt; is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readForeach(final XMLStreamReader reader, final Configuration configuration,
                                    final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        Foreach fe = new Foreach();
        fe.setArray(readRequiredAV(reader, ELEM_FOREACH, ATTR_ARRAY));
        fe.setItem(readRequiredAV(reader, ELEM_FOREACH, ATTR_ITEM));
        fe.setIndex(readAV(reader, ATTR_INDEX));
        readNamespaces(configuration, fe);
        fe.setParent(executable);
        if (parent != null) {
            parent.addAction(fe);
        } else {
            executable.addAction(fe);
        }
        readExecutableContext(reader, configuration, executable, fe);
    }

    /**
     * Read the contents of this &lt;log&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readLog(final XMLStreamReader reader, final Configuration configuration,
                                final Executable executable, final ActionsContainer parent)
            throws XMLStreamException {

        Log log = new Log();
        log.setExpr(readAV(reader, ATTR_EXPR));
        log.setLabel(readAV(reader, ATTR_LABEL));
        readNamespaces(configuration, log);
        log.setParent(executable);
        if (parent != null) {
            parent.addAction(log);
        } else {
            executable.addAction(log);
        }
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this &lt;assign&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readAssign(final XMLStreamReader reader, final Configuration configuration,
                                   final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        Assign assign = new Assign();
        assign.setExpr(readAV(reader, ATTR_EXPR));
        assign.setLocation(readRequiredAV(reader, ELEM_ASSIGN, ATTR_LOCATION));
        String attrValue = readAV(reader, ATTR_TYPE);
        if (attrValue != null) {
            assign.setType(Evaluator.AssignType.fromValue(attrValue));
            if (assign.getType() == null) {
                reportIgnoredAttribute(reader, configuration, ELEM_ASSIGN, ATTR_TYPE, attrValue);
            }
        }
        attrValue = readAV(reader, ATTR_ATTR);
        if (attrValue != null) {
            if (Evaluator.AssignType.ADD_ATTRIBUTE.equals(assign.getType())) {
                assign.setAttr(attrValue);
            }
            else {
                reportIgnoredAttribute(reader, configuration, ELEM_ASSIGN, ATTR_ATTR, attrValue);
            }
        }
        assign.setSrc(readAV(reader, ATTR_SRC));
        assign.setPathResolver(configuration.pathResolver);
        readNamespaces(configuration, assign);
        assign.setParent(executable);
        if (parent != null) {
            parent.addAction(assign);
        } else {
            executable.addAction(assign);
        }
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this &lt;send&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void readSend(final XMLStreamReader reader, final Configuration configuration,
                                 final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        if (executable instanceof Finalize) {
            // http://www.w3.org/TR/2013/WD-scxml-20130801/#finalize
            // [...] the executable content inside <finalize> MUST NOT raise events or invoke external actions.
            // In particular, the <send> and <raise> elements MUST NOT occur.
            reportIgnoredElement(reader, configuration, ELEM_FINALIZE, XMLNS_SCXML, ELEM_SEND);
            return;
        }

        Send send = new Send();
        send.setId(readAV(reader, ATTR_ID));
        String attrValue = readAV(reader, ATTR_IDLOCATION);
        if (attrValue != null) {
            if (send.getId() != null) {
                reportConflictingAttribute(reader, configuration, ELEM_SEND, ATTR_ID, ATTR_IDLOCATION);
            }
            else {
                send.setIdlocation(attrValue);
            }
        }
        send.setDelay(readAV(reader, ATTR_DELAY));
        attrValue = readAV(reader, ATTR_DELAYEXPR);
        if (attrValue != null) {
            if (send.getDelay() != null) {
                reportConflictingAttribute(reader, configuration, ELEM_SEND, ATTR_DELAY, ATTR_DELAYEXPR);
            }
            else {
                send.setDelayexpr(attrValue);
            }
        }
        send.setEvent(readAV(reader, ATTR_EVENT));
        attrValue = readAV(reader, ATTR_EVENTEXPR);
        if (attrValue != null) {
            if (send.getEvent() != null) {
                reportConflictingAttribute(reader, configuration, ELEM_SEND, ATTR_EVENT, ATTR_EVENTEXPR);
            }
            else {
                send.setEventexpr(attrValue);
            }
        }
        send.setHints(readAV(reader, ATTR_HINTS));
        send.setNamelist(readAV(reader, ATTR_NAMELIST));
        send.setTarget(readAV(reader, ATTR_TARGET));
        attrValue = readAV(reader, ATTR_TARGETEXPR);
        if (attrValue != null) {
            if (send.getTarget() != null) {
                reportConflictingAttribute(reader, configuration, ELEM_SEND, ATTR_TARGET, ATTR_TARGETEXPR);
            }
            else {
                send.setTargetexpr(attrValue);
            }
        }
        send.setType(readAV(reader, ATTR_TYPE));
        attrValue = readAV(reader, ATTR_TYPEEXPR);
        if (attrValue != null) {
            if (send.getType() != null) {
                reportConflictingAttribute(reader, configuration, ELEM_SEND, ATTR_TYPE, ATTR_TYPEEXPR);
            }
            else {
                send.setTypeexpr(attrValue);
            }
        }
        readNamespaces(configuration, send);

        loop : while (reader.hasNext()) {
            String name, nsURI;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    if (XMLNS_SCXML.equals(nsURI)) {
                        if (ELEM_PARAM.equals(name)) {
                            if (send.getContent() == null) {
                                readParam(reader, configuration, send);
                            }
                            else {
                                reportIgnoredElement(reader, configuration, ELEM_SEND, nsURI, name);
                            }
                        } else if (ELEM_CONTENT.equals(name)) {
                            if (send.getNamelist() == null && send.getParams().isEmpty()) {
                                readContent(reader, configuration, send);
                            }
                            else {
                                reportIgnoredElement(reader, configuration, ELEM_SEND, nsURI, name);
                            }
                        } else {
                            reportIgnoredElement(reader, configuration, ELEM_SEND, nsURI, name);
                        }
                    } else {
                        reportIgnoredElement(reader, configuration, ELEM_SEND, nsURI, name);
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    break loop;
                default:
            }
        }

        send.setParent(executable);
        if (parent != null) {
            parent.addAction(send);
        } else {
            executable.addAction(send);
        }
    }

    /**
     * Read the contents of this &lt;cancel&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readCancel(final XMLStreamReader reader, final Configuration configuration,
                                   final Executable executable, final ActionsContainer parent)
            throws XMLStreamException, ModelException {

        Cancel cancel = new Cancel();
        cancel.setSendid(readAV(reader, ATTR_SENDID));
        String attrValue = readAV(reader, ATTR_SENDIDEXPR);
        if (attrValue != null) {
            if (cancel.getSendid() != null) {
                reportConflictingAttribute(reader, configuration, ELEM_CANCEL, ATTR_SENDID, ATTR_SENDIDEXPR);
            }
            else {
                cancel.setSendidexpr(attrValue);
            }
        }
        readNamespaces(configuration, cancel);
        cancel.setParent(executable);
        if (parent != null) {
            parent.addAction(cancel);
        } else {
            executable.addAction(cancel);
        }
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this &lt;script&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readScript(final XMLStreamReader reader, final Configuration configuration,
                                   final Executable executable, final ActionsContainer parent)
            throws XMLStreamException {

        Script script = new Script();
        readNamespaces(configuration, script);
        script.setBody(readBody(reader));
        script.setParent(executable);
        if (parent != null) {
            parent.addAction(script);
        } else {
            executable.addAction(script);
        }
    }

    /**
     * Read the contents of the initial &lt;script&gt; element.
     * @see <a href="http://www.w3.org/TR/2013/WD-scxml-20130801/#scxml">
     *     http://www.w3.org/TR/2013/WD-scxml-20130801/#scxml<a> section 3.2.2
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param scxml The root of the object model being parsed.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readGlobalScript(final XMLStreamReader reader, final Configuration configuration,
                                         final SCXML scxml)
            throws XMLStreamException {

        Script globalScript = new Script();
        globalScript.setGlobalScript(true);
        readNamespaces(configuration, globalScript);
        globalScript.setBody(readBody(reader));
        scxml.setGlobalScript(globalScript);
    }

    /**
     * Read the contents of this &lt;var&gt; element.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param executable The parent {@link Executable} for this action.
     * @param parent The optional parent {@link ActionsContainer} if this action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readVar(final XMLStreamReader reader, final Configuration configuration,
                                final Executable executable, final ActionsContainer parent)
            throws XMLStreamException {

        Var var = new Var();
        var.setName(readAV(reader, ATTR_NAME));
        var.setExpr(readAV(reader, ATTR_EXPR));
        readNamespaces(configuration, var);
        var.setParent(executable);
        if (parent != null) {
            parent.addAction(var);
        } else {
            executable.addAction(var);
        }
        skipToEndElement(reader);
    }

    /**
     * Read the contents of this custom action.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param customAction The {@link CustomAction} to read.
     * @param executable The parent {@link Executable} for this custom action.
     * @param parent The optional parent {@link ActionsContainer} if this custom action is a child of one.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void readCustomAction(final XMLStreamReader reader, final Configuration configuration,
                                         final CustomAction customAction, final Executable executable,
                                         final ActionsContainer parent)
            throws XMLStreamException {

        // Instantiate custom action
        Object actionObject;
        String className = customAction.getActionClass().getName();
        ClassLoader cl = configuration.customActionClassLoader;
        if (configuration.useContextClassLoaderForCustomActions) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        if (cl == null) {
            cl = SCXMLReader.class.getClassLoader();
        }
        Class<?> clazz;
        try {
            clazz = cl.loadClass(className);
            actionObject = clazz.newInstance();
        } catch (ClassNotFoundException cnfe) {
            throw new XMLStreamException("Cannot find custom action class:" + className, cnfe);
        } catch (IllegalAccessException iae) {
            throw new XMLStreamException("Cannot access custom action class:" + className, iae);
        } catch (InstantiationException ie) {
            throw new XMLStreamException("Cannot instantiate custom action class:" + className, ie);
        }
        if (!(actionObject instanceof Action)) {
            throw new IllegalArgumentException(ERR_CUSTOM_ACTION_TYPE + className);
        }

        // Set the attribute values as properties
        Action action = (Action) actionObject;
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            String setter = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
            Method method;
            try {
                method = clazz.getMethod(setter, String.class);
                method.invoke(action, value);
            } catch (NoSuchMethodException nsme) {
                throw new XMLStreamException("No setter in class:" + className + ", for string property:" + name,
                        nsme);
            } catch (InvocationTargetException ite) {
                throw new XMLStreamException("Exception calling setter for string property:" + name + " in class:"
                        + className, ite);
            } catch (IllegalAccessException iae) {
                throw new XMLStreamException("Cannot access setter for string property:" + name + " in class:"
                        + className, iae);
            }
        }

        // Add any body content if necessary
        if (action instanceof ExternalContent) {
            Node body = readNode(reader, configuration, customAction.getNamespaceURI(),
                    customAction.getLocalName(), new String [] {});
            NodeList childNodes = body.getChildNodes();
            List<Node> externalNodes = ((ExternalContent) action).getExternalNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                externalNodes.add(childNodes.item(i));
            }
        }
        else {
            skipToEndElement(reader);
        }

        // Wire in the action and add to parent
        readNamespaces(configuration, action);
        action.setParent(executable);
        if (parent != null) {
            parent.addAction(action);
        } else {
            executable.addAction(action);
        }
    }

    /**
     * Read the following contents into a DOM {@link Node}.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param namespaceURI The namespace URI of the parent element
     * @param localName The local name of the parent element
     * @param attrs The attributes that will be read into the root DOM node.
     *
     * @return The parsed content as a DOM {@link Node}.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static Node readNode(final XMLStreamReader reader, final Configuration configuration,
                                 final String namespaceURI, final String localName, final String[] attrs)
            throws XMLStreamException {

        // Create a document in which to build the DOM node
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException pce) {
            throw new XMLStreamException(ERR_PARSER_CFG);
        }

        // This root element will be returned, add any attributes as specified
        Element root = document.createElementNS(namespaceURI, localName);
        for (final String attr1 : attrs) {
            Attr attr = document.createAttributeNS(XMLNS_DEFAULT, attr1);
            attr.setValue(readAV(reader, attr1));
            root.setAttributeNodeNS(attr);
        }
        document.appendChild(root);

        boolean children = false;
        Node parent = root;

        // Convert stream to DOM node(s) while maintaining parent child relationships
        loop : while (reader.hasNext()) {
            String name, nsURI;
            Node child = null;
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (!children && root.hasChildNodes()) {
                        // remove any children
                        root.setTextContent(null);
                    }
                    children = true;
                    pushNamespaces(reader, configuration);
                    nsURI = reader.getNamespaceURI();
                    name = reader.getLocalName();
                    Element elem = document.createElementNS(nsURI, name);
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        nsURI = reader.getAttributeNamespace(i);
                        name = reader.getAttributeLocalName(i);
                        String prefix = reader.getAttributePrefix(i);
                        if (prefix != null && prefix.length() > 0) {
                            name = prefix + ":" + name;
                        }
                        Attr attr = document.createAttributeNS(nsURI, name);
                        attr.setValue(reader.getAttributeValue(i));
                        elem.setAttributeNodeNS(attr);
                    }
                    parent.appendChild(elem);
                    parent = elem;
                    break;
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.ENTITY_REFERENCE:
                    if (!children || parent != root) {
                        child = document.createTextNode(reader.getText());
                    }
                    break;
                case XMLStreamConstants.CDATA:
                    children = true;
                    child = document.createCDATASection(reader.getText());
                    break;
                case XMLStreamConstants.COMMENT:
                    children = true;
                    child = document.createComment(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    popNamespaces(reader, configuration);
                    parent = parent.getParentNode();
                    if (parent == document) {
                        break loop;
                    }
                    break;
                default: // rest is ignored
            }
            if (child != null) {
                parent.appendChild(child);
            }
        }
        if (!children && root.hasChildNodes()) {
            root.setTextContent(root.getTextContent().trim());
        }
        return root;
    }

    /**
     * Read the following body contents into a String.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     *
     * @return The body content read into a String.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static String readBody(final XMLStreamReader reader)
            throws XMLStreamException {

        StringBuilder body = new StringBuilder();
        org.apache.commons.logging.Log log;

        // Add all body content to StringBuilder
        loop : while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    log = LogFactory.getLog(SCXMLReader.class);
                    log.warn("Ignoring XML content in <script> element, encountered element with local name: "
                            + reader.getLocalName());
                    skipToEndElement(reader);
                    break;
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.CHARACTERS:
                case XMLStreamConstants.ENTITY_REFERENCE:
                case XMLStreamConstants.CDATA:
                case XMLStreamConstants.COMMENT:
                    body.append(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    break loop;
                default: // rest is ignored
            }
        }
        return body.toString();
    }

    /**
     * @param input input string to check if null or empty after trim
     * @return null if input is null or empty after trim()
     */
    private static String nullIfEmpty(String input) {
        return input == null || input.trim().length()==0 ? null : input.trim();
    }

    /**
     * Get the attribute value at the current reader location.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param attrLocalName The attribute name whose value is needed.
     *
     * @return The value of the attribute.
     */
    private static String readAV(final XMLStreamReader reader, final String attrLocalName) {
        return nullIfEmpty(reader.getAttributeValue(XMLNS_DEFAULT, attrLocalName));
    }

    /**
     * Get the Boolean attribute value at the current reader location.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param elementName The name of the element for which the attribute value is needed.
     * @param attrLocalName The attribute name whose value is needed.
     *
     * @return The Boolean value of the attribute.
     * @throws ModelException When the attribute value is not empty but neither "true" or "false".
     */
    private static Boolean readBooleanAV(final XMLStreamReader reader, final String elementName,
                                         final String attrLocalName)
            throws ModelException {
        String value = nullIfEmpty(reader.getAttributeValue(XMLNS_DEFAULT, attrLocalName));
        Boolean result = "true".equals(value) ? Boolean.TRUE : "false".equals(value) ? Boolean.FALSE : null;
        if (result == null && value != null) {
            MessageFormat msgFormat = new MessageFormat(ERR_ATTRIBUTE_NOT_BOOLEAN);
            String errMsg = msgFormat.format(new Object[] {value, attrLocalName, elementName, reader.getLocation()});
            throw new ModelException(errMsg);
        }
        return result;
    }

    /**
     * Get a required attribute value at the current reader location,
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param elementName The name of the element for which the attribute value is needed.
     * @param attrLocalName The attribute name whose value is needed.
     *
     * @return The value of the attribute.
     * @throws ModelException When the required attribute is missing or empty.
     */
    private static String readRequiredAV(final XMLStreamReader reader, final String elementName, final String attrLocalName)
            throws ModelException {
        String value = nullIfEmpty(reader.getAttributeValue(XMLNS_DEFAULT, attrLocalName));
        if (value == null) {
            MessageFormat msgFormat = new MessageFormat(ERR_REQUIRED_ATTRIBUTE_MISSING);
            String errMsg = msgFormat.format(new Object[] {elementName, attrLocalName, reader.getLocation()});
            throw new ModelException(errMsg);
        }
        return value;
    }

    private static String readOrGeneratedTransitionTargetId(final XMLStreamReader reader, final SCXML scxml,
                                                            final String elementName)
            throws ModelException {
        String id = readAV(reader, ATTR_ID);
        if (id == null) {
            id = scxml.generateTransitionTargetId();
        }
        else if (id.startsWith(SCXML.GENERATED_TT_ID_PREFIX)) {
            MessageFormat msgFormat = new MessageFormat(ERR_RESERVED_ID_PREFIX);
            String errMsg = msgFormat.format(new Object[] {elementName, id, reader.getLocation()});
            throw new ModelException(errMsg);
        }
        return id;
    }

    /**
     * Read the current active namespace declarations into the namespace prefixes holder.
     *
     * @param configuration The {@link Configuration} to use while parsing.
     * @param holder The {@link NamespacePrefixesHolder} to populate.
     */
    private static void readNamespaces(final Configuration configuration, final NamespacePrefixesHolder holder) {

        holder.setNamespaces(configuration.getCurrentNamespaces());
    }

    /**
     * Report an ignored element via the {@link XMLReporter} if available and the class
     * {@link org.apache.commons.logging.Log}.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param parent The parent element local name in the SCXML namespace.
     * @param nsURI The namespace URI of the ignored element.
     * @param name The local name of the ignored element.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void reportIgnoredElement(final XMLStreamReader reader, final Configuration configuration,
                                             final String parent, final String nsURI, final String name)
            throws XMLStreamException, ModelException {

        org.apache.commons.logging.Log log = LogFactory.getLog(SCXMLReader.class);
        StringBuilder sb = new StringBuilder();
        sb.append("Ignoring unknown or invalid element <").append(name)
                .append("> in namespace \"").append(nsURI)
                .append("\" as child of <").append(parent)
                .append("> at ").append(reader.getLocation());
        if (!configuration.isSilent() && log.isWarnEnabled()) {
            log.warn(sb.toString());
        }
        if (configuration.isStrict()) {
            throw new ModelException(sb.toString());
        }
        XMLReporter reporter = configuration.reporter;
        if (reporter != null) {
            reporter.report(sb.toString(), "COMMONS_SCXML", null, reader.getLocation());
        }
        skipToEndElement(reader);
    }

    /**
     * Advances the XMLStreamReader until after the end of the current element: all children will be skipped as well
     * @param reader the reader
     * @throws XMLStreamException
     */
    private static void skipToEndElement(final XMLStreamReader reader) throws XMLStreamException {
        int elementsToSkip = 1;
        while (elementsToSkip > 0 && reader.hasNext()) {
            int next = reader.next();
            if (next == XMLStreamConstants.START_ELEMENT) {
                elementsToSkip++;
            }
            else if (next == XMLStreamConstants.END_ELEMENT) {
                elementsToSkip--;
            }
        }
    }

    /**
     * Report an ignored attribute via the {@link XMLReporter} if available and the class
     * {@link org.apache.commons.logging.Log}.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param element The element name.
     * @param attr The attribute which is ignored.
     * @param value The value of the attribute which is ignored.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void reportIgnoredAttribute(final XMLStreamReader reader, final Configuration configuration,
                                               final String element, final String attr, final String value)
            throws XMLStreamException, ModelException {

        org.apache.commons.logging.Log log = LogFactory.getLog(SCXMLReader.class);
        StringBuilder sb = new StringBuilder();
        sb.append("Ignoring unknown or invalid <").append(element).append("> attribute ").append(attr)
                .append("=\"").append(value).append("\" at ").append(reader.getLocation());
        if (!configuration.isSilent() && log.isWarnEnabled()) {
            log.warn(sb.toString());
        }
        if (configuration.isStrict()) {
            throw new ModelException(sb.toString());
        }
        XMLReporter reporter = configuration.reporter;
        if (reporter != null) {
            reporter.report(sb.toString(), "COMMONS_SCXML", null, reader.getLocation());
        }
    }

    /**
     * Report a conflicting attribute via the {@link XMLReporter} if available and the class
     * {@link org.apache.commons.logging.Log}.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     * @param element The element name.
     * @param attr The attribute with which a conflict is detected.
     * @param conflictingAttr The conflicting attribute
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     * @throws ModelException The Commons SCXML object model is incomplete or inconsistent (includes
     *                        errors in the SCXML document that may not be identified by the schema).
     */
    private static void reportConflictingAttribute(final XMLStreamReader reader, final Configuration configuration,
                                             final String element, final String attr, final String conflictingAttr)
            throws XMLStreamException, ModelException {

        org.apache.commons.logging.Log log = LogFactory.getLog(SCXMLReader.class);
        StringBuilder sb = new StringBuilder();
        sb.append("Ignoring <").append(element).append("> attribute \"").append(conflictingAttr)
                .append("\" which conflicts with already defined attribute \"").append(attr)
                .append("\" at ").append(reader.getLocation());
        if (!configuration.isSilent() && log.isWarnEnabled()) {
            log.warn(sb.toString());
        }
        if (configuration.isStrict()) {
            throw new ModelException(sb.toString());
        }
        XMLReporter reporter = configuration.reporter;
        if (reporter != null) {
            reporter.report(sb.toString(), "COMMONS_SCXML", null, reader.getLocation());
        }
    }

    /**
     * Push any new namespace declarations on the configuration namespaces map.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     */
    private static void pushNamespaces(final XMLStreamReader reader, final Configuration configuration) {

        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            Stack<String> stack = configuration.namespaces.get(reader.getNamespacePrefix(i));
            if (stack == null) {
                stack = new Stack<String>();
                configuration.namespaces.put(reader.getNamespacePrefix(i), stack);
            }
            stack.push(reader.getNamespaceURI(i));
        }
    }

    /**
     * Pop any expiring namespace declarations from the configuration namespaces map.
     *
     * @param reader The {@link XMLStreamReader} providing the SCXML document to parse.
     * @param configuration The {@link Configuration} to use while parsing.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamReader}.
     */
    private static void popNamespaces(final XMLStreamReader reader, final Configuration configuration)
            throws XMLStreamException {

        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            Stack<String> stack = configuration.namespaces.get(reader.getNamespacePrefix(i));
            if (stack == null) {
                throw new XMLStreamException("Configuration namespaces stack null");
            }
            try {
                stack.pop();
                if (stack.empty()) {
                    configuration.namespaces.remove(reader.getNamespacePrefix(i));
                }
            } catch (EmptyStackException e) {
                throw new XMLStreamException("Configuration namespaces stack popped too many times");
            }
        }
    }

    /**
     * Use the supplied {@link Configuration} to create an appropriate {@link XMLStreamReader} for this
     * {@link SCXMLReader}. Exactly one of the url, path, stream, reader or source parameters must be provided.
     *
     * @param configuration The {@link Configuration} to be used.
     * @param url The {@link URL} to the SCXML document to read.
     * @param path The optional real path to the SCXML document as a string.
     * @param stream The optional {@link InputStream} providing the SCXML document.
     * @param reader The optional {@link Reader} providing the SCXML document.
     * @param source The optional {@link Source} providing the SCXML document.
     *
     * @return The appropriately configured {@link XMLStreamReader}.
     *
     * @throws IOException Exception with the URL IO.
     * @throws XMLStreamException A problem with the XML stream creation or an wrapped {@link SAXException}
     *                            thrown in trying to validate the document against the XML Schema for SCXML.
     */
    private static XMLStreamReader getReader(final Configuration configuration, final URL url, final String path,
                                             final InputStream stream, final Reader reader, final Source source)
            throws IOException, XMLStreamException {

        // Instantiate the XMLInputFactory
        XMLInputFactory factory = XMLInputFactory.newInstance();
        if (configuration.factoryId != null && configuration.factoryClassLoader != null) {
            factory = XMLInputFactory.newFactory(configuration.factoryId, configuration.factoryClassLoader);
        }
        factory.setEventAllocator(configuration.allocator);
        for (Map.Entry<String, Object> property : configuration.properties.entrySet()) {
            factory.setProperty(property.getKey(), property.getValue());
        }
        factory.setXMLReporter(configuration.reporter);
        factory.setXMLResolver(configuration.resolver);

        // Consolidate InputStream options
        InputStream urlStream = null;
        if (url != null || path != null) {
            URL scxml = (url != null ? url : new URL(path));
            URLConnection conn = scxml.openConnection();
            conn.setUseCaches(false);
            urlStream = conn.getInputStream();
        } else if (stream != null) {
            urlStream = stream;
        }

        // Create the XMLStreamReader
        XMLStreamReader xsr = null;

        if (configuration.validate) {
            // Validation requires us to use a Source

            URL scxmlSchema = new URL("TODO"); // TODO, point to appropriate location
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema;
            try {
                schema = schemaFactory.newSchema(scxmlSchema);
            } catch (SAXException se) {
                throw new XMLStreamException("Failed to create SCXML Schema for validation", se);
            }

            Validator validator = schema.newValidator();
            validator.setErrorHandler(new SimpleErrorHandler());

            Source src = null;
            if (urlStream != null) {
                // configuration.encoding is ignored
                if (configuration.systemId != null) {
                    src = new StreamSource(urlStream, configuration.systemId);
                } else {
                    src = new StreamSource(urlStream);
                }
            } else if (reader != null) {
                if (configuration.systemId != null) {
                    src = new StreamSource(reader, configuration.systemId);
                } else {
                    src = new StreamSource(reader);
                }
            } else if (source != null) {
                src = source;
            }
            xsr = factory.createXMLStreamReader(src);
            try {
                validator.validate(src);
            } catch (SAXException se) {
                throw new XMLStreamException("Failed to create apply SCXML Validator", se);
            }

        } else {
            // We can use the more direct XMLInputFactory API if validation isn't needed

            if (urlStream != null) {
                // systemId gets preference, then encoding if either are present
                if (configuration.systemId != null) {
                    xsr = factory.createXMLStreamReader(configuration.systemId, urlStream);
                } else if (configuration.encoding != null) {
                    xsr = factory.createXMLStreamReader(urlStream, configuration.encoding);
                } else {
                    xsr = factory.createXMLStreamReader(urlStream);
                }
            } else if (reader != null) {
                if (configuration.systemId != null) {
                    xsr = factory.createXMLStreamReader(configuration.systemId, reader);
                } else {
                    xsr = factory.createXMLStreamReader(reader);
                }
            } else if (source != null) {
                xsr = factory.createXMLStreamReader(source);
            }

        }

        return xsr;
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private SCXMLReader() {
        super();
    }

    //------------------------- CONFIGURATION CLASS -------------------------//
    /**
     * <p>
     * Configuration for the {@link SCXMLReader}. The configuration properties necessary for the following are
     * covered:
     * </p>
     *
     * <ul>
     *   <li>{@link XMLInputFactory} configuration properties such as {@link XMLReporter}, {@link XMLResolver} and
     *   {@link XMLEventAllocator}</li>
     *   <li>{@link XMLStreamReader} configuration properties such as <code>systemId</code> and <code>encoding</code>
     *   </li>
     *   <li>Commons SCXML object model configuration properties such as the list of custom actions and the
     *   {@link PathResolver} to use.</li>
     * </ul>
     */
    public static class Configuration {

        /*
         * Configuration properties for this {@link SCXMLReader}.
         */
        // XMLInputFactory configuration properties.
        /**
         * The <code>factoryId</code> to use for the {@link XMLInputFactory}.
         */
        final String factoryId;

        /**
         * The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to create.
         */
        final ClassLoader factoryClassLoader;

        /**
         * The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         */
        final XMLEventAllocator allocator;

        /**
         * The map of properties (keys are property name strings, values are object property values) for the
         * {@link XMLInputFactory}.
         */
        final Map<String, Object> properties;

        /**
         * The {@link XMLResolver} for the {@link XMLInputFactory}.
         */
        final XMLResolver resolver;

        /**
         * The {@link XMLReporter} for the {@link XMLInputFactory}.
         */
        final XMLReporter reporter;

        // XMLStreamReader configuration properties.
        /**
         * The <code>encoding</code> to use for the {@link XMLStreamReader}.
         */
        final String encoding;

        /**
         * The <code>systemId</code> to use for the {@link XMLStreamReader}.
         */
        final String systemId;

        /**
         * Whether to validate the input with the XML Schema for SCXML.
         */
        final boolean validate;

        // Commons SCXML object model configuration properties.
        /**
         * The list of Commons SCXML custom actions that will be available for this document.
         */
        final List<CustomAction> customActions;

        /**
         * The {@link ClassLoader} to use for loading the {@link CustomAction} instances to create.
         */
        final ClassLoader customActionClassLoader;

        /**
         * Whether to use the thread context {@link ClassLoader} for loading any {@link CustomAction} classes.
         */
        final boolean useContextClassLoaderForCustomActions;

        /**
         * The map for bookkeeping the current active namespace declarations. The keys are prefixes and the values are
         * {@link Stack}s containing the corresponding namespaceURIs, with the active one on top.
         */
        final Map<String, Stack<String>> namespaces;

        // Mutable Commons SCXML object model configuration properties.
        /**
         * The parent SCXML document if this document is src'ed in via the &lt;state&gt; or &lt;parallel&gt; element's
         * "src" attribute.
         */
        SCXML parent;

        /**
         * The Commons SCXML {@link PathResolver} to use for this document.
         */
        PathResolver pathResolver;

        /**
         * Whether to silently ignore any unknown or invalid elements
         * or to leave warning logs for those.
         */
        boolean silent;

        /**
         * Whether to strictly throw a model exception when there are any unknown or invalid elements
         * or to leniently allow to read the model even with those.
         */
        boolean strict;

        /*
         * Public constructors
         */
        /**
         * Default constructor.
         */
        public Configuration() {
            this(null, null);
        }

        /**
         * Minimal convenience constructor.
         *
         * @param reporter The {@link XMLReporter} to use for this reading.
         * @param pathResolver The Commons SCXML {@link PathResolver} to use for this reading.
         */
        public Configuration(final XMLReporter reporter, final PathResolver pathResolver) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, null, null, null, false);
        }

        /**
         * Convenience constructor.
         *
         * @param reporter The {@link XMLReporter} to use for this reading.
         * @param pathResolver The Commons SCXML {@link PathResolver} to use for this reading.
         * @param customActions The list of Commons SCXML custom actions that will be available for this document.
         */
        public Configuration(final XMLReporter reporter, final PathResolver pathResolver,
                             final List<CustomAction> customActions) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, null, customActions, null,
                    false);
        }

        /**
         * All purpose constructor. Any of the parameters passed in can be <code>null</code> (booleans should default
         * to <code>false</code>).
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param classLoader The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to create.
         * @param allocator The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XMLInputFactory}.
         * @param resolver The {@link XMLResolver} for the {@link XMLInputFactory}.
         * @param reporter The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XMLStreamReader}
         * @param systemId The <code>systemId</code> to use for the {@link XMLStreamReader}
         * @param validate Whether to validate the input with the XML Schema for SCXML.
         * @param pathResolver The Commons SCXML {@link PathResolver} to use for this document.
         * @param customActions The list of Commons SCXML custom actions that will be available for this document.
         * @param customActionClassLoader The {@link ClassLoader} to use for the {@link CustomAction} instances to
         *                                create.
         * @param useContextClassLoaderForCustomActions Whether to use the thread context {@link ClassLoader} for the
         *                                             {@link CustomAction} instances to create.
         */
        public Configuration(final String factoryId, final ClassLoader classLoader, final XMLEventAllocator allocator,
                             final Map<String, Object> properties, final XMLResolver resolver, final XMLReporter reporter,
                             final String encoding, final String systemId, final boolean validate, final PathResolver pathResolver,
                             final List<CustomAction> customActions, final ClassLoader customActionClassLoader,
                             final boolean useContextClassLoaderForCustomActions) {
            this(factoryId, classLoader, allocator, properties, resolver, reporter, encoding, systemId, validate,
                    pathResolver, null, customActions, customActionClassLoader,
                    useContextClassLoaderForCustomActions);
        }

        /*
         * Package access constructors
         */
        /**
         * Convenience package access constructor.
         *
         * @param reporter The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param pathResolver The Commons SCXML {@link PathResolver} to use for this document.
         * @param parent The parent SCXML document if this document is src'ed in via the &lt;state&gt; or
         *               &lt;parallel&gt; element's "src" attribute.
         */
        Configuration(final XMLReporter reporter, final PathResolver pathResolver, final SCXML parent) {
            this(null, null, null, null, null, reporter, null, null, false, pathResolver, parent, null, null, false);
        }

        /**
         * Package access copy constructor.
         *
         * @param source The source {@link Configuration} to replicate.
         */
        Configuration(final Configuration source) {
            this(source.factoryId, source.factoryClassLoader, source.allocator, source.properties, source.resolver,
                    source.reporter, source.encoding, source.systemId, source.validate, source.pathResolver,
                    source.parent, source.customActions, source.customActionClassLoader,
                    source.useContextClassLoaderForCustomActions, source.silent, source.strict);
        }

        /**
         * All-purpose package access constructor.
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param factoryClassLoader The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to
         *                           create.
         * @param allocator The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XMLInputFactory}.
         * @param resolver The {@link XMLResolver} for the {@link XMLInputFactory}.
         * @param reporter The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XMLStreamReader}
         * @param systemId The <code>systemId</code> to use for the {@link XMLStreamReader}
         * @param validate Whether to validate the input with the XML Schema for SCXML.
         * @param pathResolver The Commons SCXML {@link PathResolver} to use for this document.
         * @param parent The parent SCXML document if this document is src'ed in via the &lt;state&gt; or
         *               &lt;parallel&gt; element's "src" attribute.
         * @param customActions The list of Commons SCXML custom actions that will be available for this document.
         * @param customActionClassLoader The {@link ClassLoader} to use for the {@link CustomAction} instances to
         *                                create.
         * @param useContextClassLoaderForCustomActions Whether to use the thread context {@link ClassLoader} for the
         *                                             {@link CustomAction} instances to create.
         */
        Configuration(final String factoryId, final ClassLoader factoryClassLoader, final XMLEventAllocator allocator,
                      final Map<String, Object> properties, final XMLResolver resolver, final XMLReporter reporter,
                      final String encoding, final String systemId, final boolean validate, final PathResolver pathResolver,
                      final SCXML parent, final List<CustomAction> customActions, final ClassLoader customActionClassLoader,
                      final boolean useContextClassLoaderForCustomActions) {
            this(factoryId, factoryClassLoader, allocator, properties, resolver, reporter, encoding, systemId,
                    validate, pathResolver, parent, customActions, customActionClassLoader,
                    useContextClassLoaderForCustomActions, false, false);
        }

        /**
         * All-purpose package access constructor.
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param factoryClassLoader The {@link ClassLoader} to use for the {@link XMLInputFactory} instance to
         *                           create.
         * @param allocator The {@link XMLEventAllocator} for the {@link XMLInputFactory}.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XMLInputFactory}.
         * @param resolver The {@link XMLResolver} for the {@link XMLInputFactory}.
         * @param reporter The {@link XMLReporter} for the {@link XMLInputFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XMLStreamReader}
         * @param systemId The <code>systemId</code> to use for the {@link XMLStreamReader}
         * @param validate Whether to validate the input with the XML Schema for SCXML.
         * @param pathResolver The Commons SCXML {@link PathResolver} to use for this document.
         * @param parent The parent SCXML document if this document is src'ed in via the &lt;state&gt; or
         *               &lt;parallel&gt; element's "src" attribute.
         * @param customActions The list of Commons SCXML custom actions that will be available for this document.
         * @param customActionClassLoader The {@link ClassLoader} to use for the {@link CustomAction} instances to
         *                                create.
         * @param useContextClassLoaderForCustomActions Whether to use the thread context {@link ClassLoader} for the
         *                                             {@link CustomAction} instances to create.
         * @param silent Whether to silently ignore any unknown or invalid elements or to leave warning logs for those.
         * @param strict Whether to strictly throw a model exception when there are any unknown or invalid elements
         *               or to leniently allow to read the model even with those.
         */
        Configuration(final String factoryId, final ClassLoader factoryClassLoader, final XMLEventAllocator allocator,
                      final Map<String, Object> properties, final XMLResolver resolver, final XMLReporter reporter,
                      final String encoding, final String systemId, final boolean validate, final PathResolver pathResolver,
                      final SCXML parent, final List<CustomAction> customActions, final ClassLoader customActionClassLoader,
                      final boolean useContextClassLoaderForCustomActions, final boolean silent, final boolean strict) {
            this.factoryId = factoryId;
            this.factoryClassLoader = factoryClassLoader;
            this.allocator = allocator;
            this.properties = (properties == null ? new HashMap<String, Object>() : properties);
            this.resolver = resolver;
            this.reporter = reporter;
            this.encoding = encoding;
            this.systemId = systemId;
            this.validate = validate;
            this.pathResolver = pathResolver;
            this.parent = parent;
            this.customActions = (customActions == null ? new ArrayList<CustomAction>() : customActions);
            this.customActionClassLoader = customActionClassLoader;
            this.useContextClassLoaderForCustomActions = useContextClassLoaderForCustomActions;
            this.namespaces = new HashMap<String, Stack<String>>();
            this.silent = silent;
            this.strict = strict;
        }

        /*
         * Package access convenience methods
         */
        /**
         * Get the current namespaces at this point in the StAX reading.
         *
         * @return Map<String,String> The namespace map (keys are prefixes and values are the corresponding current
         *                            namespace URIs).
         */
        Map<String, String> getCurrentNamespaces() {
            Map<String, String> currentNamespaces = new HashMap<String, String>();
            for (Map.Entry<String, Stack<String>> nsEntry : namespaces.entrySet()) {
                currentNamespaces.put(nsEntry.getKey(), nsEntry.getValue().peek());
            }
            return currentNamespaces;
        }

        /**
         * Returns true if it is set to read models silently without any model error warning logs.
         * @return
         * @see {@link #silent}
         */
        public boolean isSilent() {
            return silent;
        }

        /**
         * Turn on/off silent mode (whether to read models silently without any model error warning logs)
         * @param silent
         * @see {@link #silent}
         */
        public void setSilent(boolean silent) {
            this.silent = silent;
        }

        /**
         * Returns true if it is set to check model strictly with throwing exceptions on any model error.
         * @return
         * @see {@link #strict}
         */
        public boolean isStrict() {
            return strict;
        }

        /**
         * Turn on/off strict model (whether to check model strictly with throwing exception on any model error)
         * @param strict
         * @see {@link #strict}
         */
        public void setStrict(boolean strict) {
            this.strict = strict;
        }
    }
}
