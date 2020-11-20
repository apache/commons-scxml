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
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.SCXMLConstants;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.Assign;
import org.apache.commons.scxml2.model.Cancel;
import org.apache.commons.scxml2.model.Content;
import org.apache.commons.scxml2.model.CustomActionWrapper;
import org.apache.commons.scxml2.model.Data;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.Else;
import org.apache.commons.scxml2.model.ElseIf;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.JsonValue;
import org.apache.commons.scxml2.model.NodeListValue;
import org.apache.commons.scxml2.model.NodeValue;
import org.apache.commons.scxml2.model.ParsedValue;
import org.apache.commons.scxml2.model.Raise;
import org.apache.commons.scxml2.model.ParsedValueContainer;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.Finalize;
import org.apache.commons.scxml2.model.Foreach;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.If;
import org.apache.commons.scxml2.model.Initial;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.Log;
import org.apache.commons.scxml2.model.OnEntry;
import org.apache.commons.scxml2.model.OnExit;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.Param;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Script;
import org.apache.commons.scxml2.model.Send;
import org.apache.commons.scxml2.model.SimpleTransition;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TextValue;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.model.Var;
import org.w3c.dom.Node;

/**
 * <p>Utility class for serializing the Commons SCXML Java object
 * model. Class uses the visitor pattern to trace through the
 * object heirarchy. Used primarily for testing, debugging and
 * visual verification.</p>
 *
 * <b>NOTE:</b> This writer makes the following assumptions about the
 * original SCXML document(s) parsed to create the object model:
 * <ul>
 *  <li>The default document namespace is the SCXML namespace:
 *      <i>http://www.w3.org/2005/07/scxml</i></li>
 *  <li>The Commons SCXML namespace
 *      ( <i>https://commons.apache.org/scxml</i> ), if needed, uses the
 *      &quot;<i>cs</i>&quot; prefix</li>
 *  <li>All namespace prefixes needed throughout the document are
 *      declared on the document root element (&lt;scxml&gt;)</li>
 * </ul>
 *
 * @since 1.0
 */
public class SCXMLWriter {

    //---------------------- PRIVATE CONSTANTS ----------------------//

    //---- ERROR MESSAGES ----//
    /**
     * Null OutputStream passed as argument.
     */
    private static final String ERR_NULL_OSTR = "Cannot write to null OutputStream";

    /**
     * Null Writer passed as argument.
     */
    private static final String ERR_NULL_WRIT = "Cannot write to null Writer";

    /**
     * Null Result passed as argument.
     */
    private static final String ERR_NULL_RES = "Cannot parse null Result";

    //------------------------- STATIC MEMBERS -------------------------//
    /**
     * The JAXP transformer.
     */
    private static final Transformer XFORMER = getTransformer();

    //------------------------- PUBLIC API METHODS -------------------------//
    /**
     * Write out the Commons SCXML object model as an SCXML document (used
     * primarily for testing, debugging and visual verification), returned as
     * a string.
     *
     * @param scxml The object model to serialize.
     *
     * @return The corresponding SCXML document as a string.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static String write(final SCXML scxml)
            throws IOException, XMLStreamException {

        return write(scxml, new Configuration(true, true));
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document (used
     * primarily for testing, debugging and visual verification) using the
     * supplied {@link Configuration}, and return as a string.
     *
     * @param scxml The object model to serialize.
     * @param configuration The {@link Configuration} to use while serializing.
     *
     * @return The corresponding SCXML document as a string.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static String write(final SCXML scxml, final Configuration configuration)
            throws IOException, XMLStreamException {

        // Must be true since we want to return a string
        configuration.writeToString = true;
        writeInternal(scxml, configuration, null, null, null);
        if (configuration.usePrettyPrint) {
            return configuration.prettyPrintOutput;
        } else {
            configuration.internalWriter.flush();
            return configuration.internalWriter.toString();
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link OutputStream}.
     *
     * @param scxml The object model to write out.
     * @param scxmlStream The {@link OutputStream} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static void write(final SCXML scxml, final OutputStream scxmlStream)
            throws IOException, XMLStreamException {

        write(scxml, scxmlStream, new Configuration());
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link OutputStream} using the given {@link Configuration}.
     *
     * @param scxml The object model to write out.
     * @param scxmlStream The {@link OutputStream} to write to.
     * @param configuration The {@link Configuration} to use.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static void write(final SCXML scxml, final OutputStream scxmlStream, final Configuration configuration)
            throws IOException, XMLStreamException {

        if (scxmlStream == null) {
            throw new IllegalArgumentException(ERR_NULL_OSTR);
        }
        writeInternal(scxml, configuration, scxmlStream, null, null);
        if (configuration.closeUnderlyingWhenDone) {
            scxmlStream.flush();
            scxmlStream.close();
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Writer}.
     *
     * @param scxml The object model to write out.
     * @param scxmlWriter The {@link Writer} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static void write(final SCXML scxml, final Writer scxmlWriter)
            throws IOException, XMLStreamException {

        write(scxml, scxmlWriter, new Configuration());
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Writer} using the given {@link Configuration}.
     *
     * @param scxml The object model to write out.
     * @param scxmlWriter The {@link Writer} to write to.
     * @param configuration The {@link Configuration} to use.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static void write(final SCXML scxml, final Writer scxmlWriter, final Configuration configuration)
            throws IOException, XMLStreamException {

        if (scxmlWriter == null) {
            throw new IllegalArgumentException(ERR_NULL_WRIT);
        }
        writeInternal(scxml, configuration, null, scxmlWriter, null);
        if (configuration.closeUnderlyingWhenDone) {
            scxmlWriter.flush();
            scxmlWriter.close();
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Result}.
     *
     * @param scxml The object model to write out.
     * @param scxmlResult The {@link Result} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static void write(final SCXML scxml, final Result scxmlResult)
            throws IOException, XMLStreamException {

        write(scxml, scxmlResult, new Configuration());
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document to the
     * supplied {@link Result} using the given {@link Configuration}.
     *
     * @param scxml The object model to write out.
     * @param scxmlResult The {@link Result} to write to.
     * @param configuration The {@link Configuration} to use.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    public static void write(final SCXML scxml, final Result scxmlResult, final Configuration configuration)
            throws IOException, XMLStreamException {

        if (scxmlResult == null) {
            throw new IllegalArgumentException(ERR_NULL_RES);
        }
        writeInternal(scxml, configuration, null, null, scxmlResult);
    }

    //---------------------- PRIVATE UTILITY METHODS ----------------------//

    /**
     * Escape XML strings for serialization.
     * The basic algorithm is taken from Commons Lang (see oacl.Entities.java)
     *
     * @param str A string to be escaped
     * @return The escaped string
     */
    private static String escapeXML(final String str) {
        if (str == null) {
            return null;
        }

        // Make the writer an arbitrary bit larger than the source string
        int len = str.length();
        StringWriter stringWriter = new StringWriter(len + 8);

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            String entityName = null; // Look for XML 1.0 predefined entities
            switch (c) {
                case '"':
                    entityName = "quot";
                    break;
                case '&':
                    entityName = "amp";
                    break;
                case '<':
                    entityName = "lt";
                    break;
                case '>':
                    entityName = "gt";
                    break;
                default:
            }
            if (entityName == null) {
                if (c > 0x7F) {
                    stringWriter.write("&#");
                    stringWriter.write(Integer.toString(c));
                    stringWriter.write(';');
                } else {
                    stringWriter.write(c);
                }
            } else {
                stringWriter.write('&');
                stringWriter.write(entityName);
                stringWriter.write(';');
            }
        }

        return stringWriter.toString();
    }

    /**
     * Write out the Commons SCXML object model using the supplied {@link Configuration}.
     * Exactly one of the stream, writer or result parameters must be provided.
     *
     * @param scxml The object model to write out.
     * @param configuration The {@link Configuration} to use.
     * @param scxmlStream The optional {@link OutputStream} to write to.
     * @param scxmlWriter The optional {@link Writer} to write to.
     * @param scxmlResult The optional {@link Result} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeInternal(final SCXML scxml, final Configuration configuration,
                                      final OutputStream scxmlStream, final Writer scxmlWriter, final Result scxmlResult)
            throws IOException, XMLStreamException {

        XMLStreamWriter writer = getWriter(configuration, scxmlStream, scxmlWriter, scxmlResult);
        writeDocument(writer, configuration, scxml);
        writer.flush();
        writer.close();
        if (configuration.internalWriter != null) {
            configuration.internalWriter.flush();
        }
        if (configuration.usePrettyPrint) {
            Writer prettyPrintWriter = (scxmlWriter != null ? scxmlWriter : new StringWriter());
            writePretty(configuration, scxmlStream, prettyPrintWriter, scxmlResult);
            if (configuration.writeToString) {
                prettyPrintWriter.flush();
                configuration.prettyPrintOutput = prettyPrintWriter.toString();
            }
        }
    }

    /**
     * Write out the Commons SCXML object model as an SCXML document using the supplied {@link Configuration}.
     * This method tackles the XML document level concerns.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param configuration The {@link Configuration} in use.
     * @param scxml The root of the object model to write out.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeDocument(final XMLStreamWriter writer, final Configuration configuration,
                                      final SCXML scxml)
            throws XMLStreamException {

        String encoding = "UTF-8";
        if (configuration.encoding != null) {
            encoding = configuration.encoding;
        }
        writer.writeStartDocument(encoding, "1.0");
        writeSCXML(writer, scxml);
        writer.writeEndDocument();
    }

    /**
     * Write out this {@link SCXML} object into its serialization as the corresponding &lt;scxml&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param scxml The root of the object model to write out.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeSCXML(final XMLStreamWriter writer, final SCXML scxml)
            throws XMLStreamException {

        // Start
        writer.writeStartElement(SCXMLConstants.ELEM_SCXML);

        // Namespaces
        writer.writeNamespace(null, SCXMLConstants.XMLNS_SCXML);
//        writer.writeNamespace("cs", XMLNS_COMMONS_SCXML);
        if (scxml.getNamespaces() != null) {
            for (Map.Entry<String, String> entry : scxml.getNamespaces().entrySet()) {
                String key = entry.getKey();
                if (key != null && key.trim().length() > 0) {
                    writer.writeNamespace(key, entry.getValue());
                }
            }
        }

        // Attributes
        writeAV(writer, SCXMLConstants.ATTR_VERSION, scxml.getVersion());
        writeAV(writer, SCXMLConstants.ATTR_INITIAL, scxml.getInitial());
        writeAV(writer, SCXMLConstants.ATTR_DATAMODEL, scxml.getDatamodelName());
        if (scxml.isLateBinding() != null) {
            writeAV(writer, SCXMLConstants.ATTR_BINDING, scxml.isLateBinding() ? SCXMLConstants.ATTR_BINDING_LATE : SCXMLConstants.ATTR_BINDING_EARLY);
        }
        writeAV(writer, SCXMLConstants.ATTR_NAME, scxml.getName());
        writeAV(writer, SCXMLConstants.ATTR_PROFILE, scxml.getProfile());
        writeAV(writer, SCXMLConstants.ATTR_EXMODE, scxml.getExmode());

        // Marker to indicate generated document
        writer.writeComment(SCXMLConstants.XMLNS_COMMONS_SCXML);

        // Write global script if defined
        if (scxml.getGlobalScript() != null) {
            Script s = scxml.getGlobalScript();
            writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_SCRIPT);
            if (s.getSrc() != null) {
                writeAV(writer, SCXMLConstants.ATTR_SRC, s.getSrc());
            } else {
                writer.writeCData(s.getScript());
            }
            writer.writeEndElement();
        }

        // Children
        writeDatamodel(writer, scxml.getDatamodel());
        writeEnterableStates(writer, scxml.getChildren());

        // End
        writer.writeEndElement();
    }

    /**
     * Write out this {@link Datamodel} object into its serialization as the corresponding &lt;datamodel&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param datamodel The {@link Datamodel} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeDatamodel(final XMLStreamWriter writer, final Datamodel datamodel)
            throws XMLStreamException {

        if (datamodel == null) {
            return;
        }

        writer.writeStartElement(SCXMLConstants.ELEM_DATAMODEL);
        for (Data d : datamodel.getData()) {
            writer.writeStartElement(SCXMLConstants.ELEM_DATA);
            writeAV(writer, SCXMLConstants.ATTR_ID, d.getId());
            writeAV(writer, SCXMLConstants.ATTR_SRC, escapeXML(d.getSrc()));
            writeAV(writer, SCXMLConstants.ATTR_EXPR, escapeXML(d.getExpr()));
            writeParsedValue(writer, d.getParsedValue());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Write out the TransitionTarget id attribute unless it was auto-generated
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param tt The {@link TransitionTarget} for which to write the id attribute.
     * @throws XMLStreamException
     */
    private static void writeTransitionTargetId(final XMLStreamWriter writer, final TransitionTarget tt)
            throws XMLStreamException {
        if (!tt.getId().startsWith(SCXML.GENERATED_TT_ID_PREFIX)) {
            writeAV(writer, SCXMLConstants.ATTR_ID, tt.getId());
        }
    }

    /**
     * Write out this {@link State} object into its serialization as the corresponding &lt;state&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param state The {@link State} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeState(final XMLStreamWriter writer, final State state)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_STATE);
        writeTransitionTargetId(writer, state);
        writeAV(writer, SCXMLConstants.ATTR_INITIAL, state.getFirst());
        writeInitial(writer, state.getInitial());
        writeDatamodel(writer, state.getDatamodel());
        writeHistory(writer, state.getHistory());
        for (OnEntry onentry : state.getOnEntries()) {
            writeOnEntry(writer, onentry);
        }

        for (Transition t : state.getTransitionsList()) {
            writeTransition(writer, t);
        }

        for (Invoke inv : state.getInvokes()) {
            writeInvoke(writer, inv);
        }

        writeEnterableStates(writer, state.getChildren());

        for (OnExit onexit : state.getOnExits()) {
            writeOnExit(writer, onexit);
        }
        writer.writeEndElement();
    }

    /**
     * Write out this {@link Parallel} object into its serialization as the corresponding &lt;parallel&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param parallel The {@link Parallel} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeParallel(final XMLStreamWriter writer, final Parallel parallel)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_PARALLEL);
        writeTransitionTargetId(writer, parallel);

        writeDatamodel(writer, parallel.getDatamodel());
        writeHistory(writer, parallel.getHistory());
        for (OnEntry onentry : parallel.getOnEntries()) {
            writeOnEntry(writer, onentry);
        }

        for (Transition t : parallel.getTransitionsList()) {
            writeTransition(writer, t);
        }

        for (Invoke inv : parallel.getInvokes()) {
            writeInvoke(writer, inv);
        }

        writeEnterableStates(writer, parallel.getChildren());

        for (OnExit onexit : parallel.getOnExits()) {
            writeOnExit(writer, onexit);
        }
        writer.writeEndElement();
    }

    /**
     * Write out this {@link Final} object into its serialization as the corresponding &lt;final&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param end The {@link Final} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeFinal(final XMLStreamWriter writer, final Final end)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_FINAL);
        writeTransitionTargetId(writer, end);
        for (OnEntry onentry : end.getOnEntries()) {
            writeOnEntry(writer, onentry);
        }
        for (OnExit onexit : end.getOnExits()) {
            writeOnExit(writer, onexit);
        }
        if (end.getDoneData() != null) {
            writer.writeStartElement(SCXMLConstants.ELEM_DONEDATA);
            writeParams(writer, end.getDoneData().getParams());
            writeContent(writer, end.getDoneData().getContent());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Write out the {@link EnterableState} objects into its serialization.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param states The {@link EnterableState}s to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeEnterableStates(final XMLStreamWriter writer, final List<EnterableState> states)
            throws XMLStreamException {
        for (EnterableState es : states) {
            if (es instanceof Final) {
                writeFinal(writer, (Final) es);
            } else if (es instanceof State) {
                writeState(writer, (State) es);
            } else if (es instanceof Parallel) {
                writeParallel(writer, (Parallel) es);
            }
        }
    }

    /**
     * Write out this {@link Initial} object into its serialization as the corresponding &lt;initial&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param initial The {@link Initial} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeInitial(final XMLStreamWriter writer, final Initial initial)
            throws XMLStreamException {

        if (initial == null || initial.isGenerated()) {
            return;
        }

        writer.writeStartElement(SCXMLConstants.ELEM_INITIAL);
        writeTransition(writer, initial.getTransition());
        writer.writeEndElement();
    }

    /**
     * Write out this {@link History} list into its serialization as the corresponding set of &lt;history&gt;
     * elements.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param history The {@link History} list to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeHistory(final XMLStreamWriter writer, final List<History> history)
            throws XMLStreamException {

        if (history == null) {
            return;
        }

        for (History h : history) {
            writer.writeStartElement(SCXMLConstants.ELEM_HISTORY);
            writeTransitionTargetId(writer, h);
            if (h.isDeep()) {
                writeAV(writer, SCXMLConstants.ATTR_TYPE, "deep");
            } else {
                writeAV(writer, SCXMLConstants.ATTR_TYPE, "shallow");
            }
            writeTransition(writer, h.getTransition());
            writer.writeEndElement();
        }
    }

    /**
     * Write out this {@link OnEntry} object into its serialization as the corresponding &lt;onentry&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param onentry The {@link OnEntry} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeOnEntry(final XMLStreamWriter writer, final OnEntry onentry)
            throws XMLStreamException {

        if (onentry != null && (onentry.isRaiseEvent() || onentry.getActions().size() > 0 )) {
            writer.writeStartElement(SCXMLConstants.ELEM_ONENTRY);
            writeAV(writer, SCXMLConstants.ATTR_EVENT, onentry.getRaiseEvent());
            writeExecutableContent(writer, onentry.getActions());
            writer.writeEndElement();
        }
    }

    /**
     * Write out this {@link OnExit} object into its serialization as the corresponding &lt;onexit&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param onexit The {@link OnExit} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeOnExit(final XMLStreamWriter writer, final OnExit onexit)
            throws XMLStreamException {

        if (onexit != null && (onexit.isRaiseEvent() || onexit.getActions().size() > 0)) {
            writer.writeStartElement(SCXMLConstants.ELEM_ONEXIT);
            writeAV(writer, SCXMLConstants.ATTR_EVENT, onexit.getRaiseEvent());
            writeExecutableContent(writer, onexit.getActions());
            writer.writeEndElement();
        }
    }

    /**
     * Write out this {@link Transition} object into its serialization as the corresponding &lt;transition&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param transition The {@link Transition} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeTransition(final XMLStreamWriter writer, final SimpleTransition transition)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_TRANSITION);
        if (transition instanceof Transition) {
            writeAV(writer, SCXMLConstants.ATTR_EVENT, ((Transition)transition).getEvent());
            writeAV(writer, SCXMLConstants.ATTR_COND, escapeXML(((Transition)transition).getCond()));
        }

        writeAV(writer, SCXMLConstants.ATTR_TARGET, transition.getNext());
        if (transition.getType() != null) {
            writeAV(writer, SCXMLConstants.ATTR_TYPE, transition.getType().name());
        }
        writeExecutableContent(writer, transition.getActions());
        writer.writeEndElement();
    }

    /**
     * Write out this {@link Invoke} object into its serialization as the corresponding &lt;invoke&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param invoke The {@link Invoke} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeInvoke(final XMLStreamWriter writer, final Invoke invoke)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_INVOKE);
        writeAV(writer, SCXMLConstants.ATTR_ID, invoke.getId());
        writeAV(writer, SCXMLConstants.ATTR_IDLOCATION, invoke.getIdlocation());
        writeAV(writer, SCXMLConstants.ATTR_SRC, invoke.getSrc());
        writeAV(writer, SCXMLConstants.ATTR_SRCEXPR, invoke.getSrcexpr());
        writeAV(writer, SCXMLConstants.ATTR_TYPE, invoke.getType());
        writeAV(writer, SCXMLConstants.ATTR_AUTOFORWARD, invoke.getAutoForward());
        writeAV(writer, SCXMLConstants.ATTR_NAMELIST, invoke.getNamelist());

        writeParams(writer, invoke.getParams());
        writeFinalize(writer, invoke.getFinalize());
        writeContent(writer, invoke.getContent());

        writer.writeEndElement();
    }

    /**
     * Write out this {@link Finalize} object into its serialization as the corresponding &lt;finalize&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param finalize The {@link Finalize} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeFinalize(final XMLStreamWriter writer, final Finalize finalize)
            throws XMLStreamException {

        if (finalize != null && finalize.getActions().size() > 0) {
            writer.writeStartElement(SCXMLConstants.ELEM_FINALIZE);
            writeExecutableContent(writer, finalize.getActions());
            writer.writeEndElement();
        }
    }

    /**
     * Write out this executable content (list of actions) into its serialization as the corresponding set of action
     * elements.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param actions The list of actions to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeExecutableContent(final XMLStreamWriter writer, final List<Action> actions)
            throws XMLStreamException {

        if (actions == null) {
            return;
        }
        for (Action a : actions) {
            if (a instanceof Assign) {
                Assign asn = (Assign) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_ASSIGN);
                writeAV(writer, SCXMLConstants.ATTR_LOCATION, asn.getLocation());
                writeAV(writer, SCXMLConstants.ATTR_SRC, asn.getSrc());
                writeAV(writer, SCXMLConstants.ATTR_EXPR, escapeXML(asn.getExpr()));
                writeParsedValue(writer, ((Assign) a).getParsedValue());
                writer.writeEndElement();
            } else if (a instanceof Send) {
                writeSend(writer, (Send) a);
            } else if (a instanceof Cancel) {
                Cancel c = (Cancel) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_CANCEL);
                writeAV(writer, SCXMLConstants.ATTR_SENDID, c.getSendid());
                writer.writeEndElement();
            } else if (a instanceof Foreach) {
                writeForeach(writer, (Foreach) a);
            } else if (a instanceof Log) {
                Log lg = (Log) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_LOG);
                writeAV(writer, SCXMLConstants.ATTR_LABEL, lg.getLabel());
                writeAV(writer, SCXMLConstants.ATTR_EXPR, escapeXML(lg.getExpr()));
                writer.writeEndElement();
            } else if (a instanceof Raise) {
                Raise e = (Raise) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_RAISE);
                writeAV(writer, SCXMLConstants.ATTR_EVENT, e.getEvent());
                writer.writeEndElement();
            } else if (a instanceof Script) {
                Script s = (Script) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_SCRIPT);
                if (s.getSrc() != null) {
                    writeAV(writer, SCXMLConstants.ATTR_SRC, s.getSrc());
                } else {
                    writer.writeCData(s.getScript());
                }
                writer.writeEndElement();
            } else if (a instanceof If) {
                writeIf(writer, (If) a);
            } else if (a instanceof Else) {
                writer.writeEmptyElement(SCXMLConstants.ELEM_ELSE);
            } else if (a instanceof ElseIf) {
                ElseIf eif = (ElseIf) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_ELSEIF);
                writeAV(writer, SCXMLConstants.ATTR_COND, escapeXML(eif.getCond()));
                writer.writeEndElement();
            } else if (a instanceof Var) {
                // 'naked' Var custom action, not wrapped in a CustomActionWrapper
                Var v = (Var) a;
                writer.writeStartElement(SCXMLConstants.XMLNS_COMMONS_SCXML, SCXMLConstants.ELEM_VAR);
                writeAV(writer, SCXMLConstants.ATTR_NAME, v.getName());
                writeAV(writer, SCXMLConstants.ATTR_EXPR, escapeXML(v.getExpr()));
                writer.writeEndElement();
            } else if (a instanceof CustomActionWrapper) {
                CustomActionWrapper actionWrapper = (CustomActionWrapper)a;
                writer.writeStartElement(createQualifiedName(actionWrapper.getPrefix(), actionWrapper.getLocalName()));
                if (actionWrapper.getAttributes() != null) {
                    for (final String attr : actionWrapper.getAttributes().keySet()) {
                        writer.writeAttribute(attr, escapeXML(actionWrapper.getAttributes().get(attr)));
                    }
                }
                for (final String prefix : actionWrapper.getNamespaces().keySet()) {
                    writer.writeNamespace(prefix, actionWrapper.getNamespaces().get(prefix));
                }
                if (actionWrapper.getAction() instanceof ParsedValueContainer) {
                    writeParsedValue(writer, ((ParsedValueContainer)actionWrapper.getAction()).getParsedValue());
                }
                writer.writeEndElement();
            } else {
                writer.writeComment("Unknown action with class name '" + a.getClass().getName() + "' not serialized");
            }
        }
    }

    /**
     * Write out this {@link Send} object into its serialization as the corresponding &lt;send&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param send The {@link Send} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeSend(final XMLStreamWriter writer, final Send send)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.XMLNS_SCXML, SCXMLConstants.ELEM_SEND);
        writeAV(writer, SCXMLConstants.ATTR_ID, send.getId());
        writeAV(writer, SCXMLConstants.ATTR_IDLOCATION, send.getIdlocation());
        writeAV(writer, SCXMLConstants.ATTR_EVENT, send.getEvent());
        writeAV(writer, SCXMLConstants.ATTR_EVENTEXPR, send.getEventexpr());
        writeAV(writer, SCXMLConstants.ATTR_TARGET, send.getTarget());
        writeAV(writer, SCXMLConstants.ATTR_TARGETEXPR, send.getTargetexpr());
        writeAV(writer, SCXMLConstants.ATTR_TYPE, send.getType());
        writeAV(writer, SCXMLConstants.ATTR_TYPEEXPR, send.getTypeexpr());
        writeAV(writer, SCXMLConstants.ATTR_DELAY, send.getDelay());
        writeAV(writer, SCXMLConstants.ATTR_DELAYEXPR, send.getDelayexpr());
        writeAV(writer, SCXMLConstants.ATTR_NAMELIST, send.getNamelist());
        writeAV(writer, SCXMLConstants.ATTR_HINTS, send.getHints());

        writeParams(writer, send.getParams());
        writeContent(writer, send.getContent());

        writer.writeEndElement();
    }

    /**
     * Write out the {@link Param} objects into its serialization.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param params The {@link Param}s to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeParams(final XMLStreamWriter writer, final List<Param> params)
            throws XMLStreamException {
        for (Param p : params) {
            writer.writeStartElement(SCXMLConstants.ELEM_PARAM);
            writeAV(writer, SCXMLConstants.ATTR_NAME, p.getName());
            writeAV(writer, SCXMLConstants.ATTR_LOCATION, p.getLocation());
            writeAV(writer, SCXMLConstants.ATTR_EXPR, escapeXML(p.getExpr()));
            writer.writeEndElement();
        }
    }

    /**
     * Write out this {@link If} object into its serialization as the corresponding &lt;if&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param iff The {@link If} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeIf(final XMLStreamWriter writer, final If iff)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_IF);
        writeAV(writer, SCXMLConstants.ATTR_COND, escapeXML(iff.getCond()));
        writeExecutableContent(writer, iff.getActions());
        writer.writeEndElement();
    }

    /**
     * Write out this {@link Foreach} object into its serialization as the corresponding &lt;foreach&gt; element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param foreach The {@link If} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeForeach(final XMLStreamWriter writer, final Foreach foreach)
            throws XMLStreamException {

        writer.writeStartElement(SCXMLConstants.ELEM_FOREACH);
        writeAV(writer, SCXMLConstants.ATTR_ITEM, foreach.getItem());
        writeAV(writer, SCXMLConstants.ATTR_INDEX, foreach.getIndex());
        writeAV(writer, SCXMLConstants.ATTR_ARRAY, escapeXML(foreach.getArray()));
        writeExecutableContent(writer, foreach.getActions());
        writer.writeEndElement();
    }

    /**
     * Write the {@link Content} element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param content The content element to write.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeContent(final XMLStreamWriter writer, final Content content)
            throws XMLStreamException {

        if (content != null) {
            writer.writeStartElement(SCXMLConstants.ELEM_CONTENT);
            writeAV(writer, SCXMLConstants.ATTR_EXPR, content.getExpr());
            writeParsedValue(writer, content.getParsedValue());
            writer.writeEndElement();
        }
    }

    /**
     * Write out this {@link ParsedValue} object as body of its containing {@link ParsedValueContainer} element.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param parsedValue The {@link ParsedValue} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeParsedValue(final XMLStreamWriter writer, final ParsedValue parsedValue)
            throws XMLStreamException {
        try {
            if (parsedValue != null) {
                switch (parsedValue.getType()) {
                    case TEXT:
                        final TextValue textValue = (TextValue)parsedValue;
                        if (textValue.isCDATA()) {
                            writer.writeCData(textValue.getValue());
                        } else {
                            writer.writeCharacters(textValue.getValue());
                        }
                        break;
                    case JSON:
                        final String value = ContentParser.DEFAULT_PARSER.toJson(parsedValue.getValue());
                        if (((JsonValue) parsedValue).isCDATA()) {
                            writer.writeCData(value);
                        } else {
                            writer.writeCharacters(value);
                        }
                        break;
                    case NODE:
                        if (XFORMER == null) {
                            writer.writeComment("element body was not serialized");
                        } else {
                            writeNode(writer, ((NodeValue)parsedValue).getValue());
                        }
                        break;
                    case NODE_LIST:
                        List<Node> nodeList = ((NodeListValue)parsedValue).getValue();
                        if (!nodeList.isEmpty() && XFORMER == null) {
                            writer.writeComment("element body was not serialized");
                        } else {
                            for (final Node node : nodeList) {
                                writeNode(writer, node);
                            }
                        }
                        break;
                    case NODE_TEXT:
                        writeNode(writer, ContentParser.DEFAULT_PARSER.parseXml((String)parsedValue.getValue()));
                        break;
                }
            }
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Write out this {@link Node} object into its serialization.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param node The {@link Node} to serialize.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeNode(final XMLStreamWriter writer, final Node node)
            throws XMLStreamException {

        Source input = new DOMSource(node);
        StringWriter out = new StringWriter();
        Result output = new StreamResult(out);
        try {
            XFORMER.transform(input, output);
        } catch (TransformerException te) {
            org.apache.commons.logging.Log log = LogFactory.getLog(SCXMLWriter.class);
            log.error(te.getMessage(), te);
            writer.writeComment("TransformerException: Node was not serialized");
        }
        writer.writeCharacters(out.toString());
    }

    /**
     * Write out this attribute, if the value is not <code>null</code>.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param localName The local name of the attribute.
     * @param value The attribute value.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeAV(final XMLStreamWriter writer, final String localName, final String value)
            throws XMLStreamException {
        if (value != null) {
            writer.writeAttribute(localName, value);
        }
    }

    /**
     * Write out this attribute, if the value is not <code>null</code>.
     *
     * @param writer The {@link XMLStreamWriter} in use for the serialization.
     * @param localName The local name of the attribute.
     * @param value The attribute value.
     *
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writeAV(final XMLStreamWriter writer, final String localName, final Boolean value)
            throws XMLStreamException {
        if (value != null) {
            writer.writeAttribute(localName, value.toString());
        }
    }

    /**
     * @param prefix prefix
     * @param localName localName
     * @return a qualified name from a prefix and localName
     */
    private static String createQualifiedName(final String prefix, final String localName) {
        return (prefix != null && prefix.length() > 0 ? prefix + ":" : "") +localName;
    }

    /**
     * Write the serialized SCXML document while making attempts to make the serialization human readable. This
     * includes using new-lines and indentation as appropriate, where possible. Exactly one of the stream, writer
     * or result parameters must be provided.
     *
     * @param configuration The {@link Configuration} to use.
     * @param scxmlStream The optional {@link OutputStream} to write to.
     * @param scxmlWriter The optional {@link Writer} to write to.
     * @param scxmlResult The optional {@link Result} to write to.
     *
     * @throws IOException An IO error during serialization.
     * @throws XMLStreamException An exception processing the underlying {@link XMLStreamWriter}.
     */
    private static void writePretty(final Configuration configuration, final OutputStream scxmlStream,
                                    final Writer scxmlWriter, final Result scxmlResult)
            throws IOException, XMLStreamException {

        // There isn't any portable way to write pretty using the JDK 1.6 StAX API
        configuration.internalWriter.flush();
        Source prettyPrintSource = new StreamSource(new StringReader(configuration.internalWriter.toString()));
        Result prettyPrintResult = null;
        if (scxmlStream != null) {
            prettyPrintResult = new StreamResult(scxmlStream);
        } else if (scxmlWriter != null) {
            prettyPrintResult = new StreamResult(scxmlWriter);
        } else if (scxmlResult != null) {
            prettyPrintResult = scxmlResult;
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            Transformer transformer = factory.newTransformer();
            if (configuration.encoding != null) {
                transformer.setOutputProperty(OutputKeys.ENCODING, configuration.encoding);
            }
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(prettyPrintSource, prettyPrintResult);
        } catch (TransformerException te) {
            throw new XMLStreamException("TransformerException while pretty printing SCXML", te);
        }
    }

    /**
     * Use the supplied {@link Configuration} to create an appropriate {@link XMLStreamWriter} for this
     * {@link SCXMLWriter}. Exactly one of the stream, writer or result parameters must be provided.
     *
     * @param configuration The {@link Configuration} to use.
     * @param stream The optional {@link OutputStream} to write to.
     * @param writer The optional {@link Writer} to write to.
     * @param result The optional {@link Result} to write to.
     *
     * @return The appropriately configured {@link XMLStreamWriter}.
     *
     * @throws XMLStreamException A problem with the XML stream creation.
     */
    private static XMLStreamWriter getWriter(final Configuration configuration, final OutputStream stream,
                                             final Writer writer, final Result result)
            throws XMLStreamException {

        // Instantiate the XMLOutputFactory
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        /*
        if (configuration.factoryId != null && configuration.factoryClassLoader != null) {
            // TODO StAX API bug means we can't use custom factories yet
            //factory = XMLOutputFactory.newInstance(configuration.factoryId, configuration.factoryClassLoader);
        }
        */
        for (Map.Entry<String, Object> property : configuration.properties.entrySet()) {
            factory.setProperty(property.getKey(), property.getValue());
        }

        XMLStreamWriter xsw = null;
        if (configuration.usePrettyPrint || configuration.writeToString) {
            xsw = factory.createXMLStreamWriter(configuration.internalWriter);
        } else if (stream != null) {
            if (configuration.encoding != null) {
                xsw = factory.createXMLStreamWriter(stream, configuration.encoding);
            } else {
                xsw = factory.createXMLStreamWriter(stream);
            }
        } else if (writer != null) {
            xsw = factory.createXMLStreamWriter(writer);
        } else if (result != null) {
            xsw = factory.createXMLStreamWriter(result);
        }
        return xsw;
    }

    /**
     * Get a {@link Transformer} instance that pretty prints the output.
     *
     * @return Transformer The indenting {@link Transformer} instance.
     */
    private static Transformer getTransformer() {
        Transformer transformer;
        Properties outputProps = new Properties();
        outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        outputProps.put(OutputKeys.STANDALONE, "no");
        outputProps.put(OutputKeys.INDENT, "yes");
        try {
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            transformer = tfFactory.newTransformer();
            transformer.setOutputProperties(outputProps);
        } catch (TransformerFactoryConfigurationError | TransformerConfigurationException t) {
            org.apache.commons.logging.Log log = LogFactory.getLog(SCXMLWriter.class);
            log.error(t.getMessage(), t);
            return null;
        }
        return transformer;
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private SCXMLWriter() {
    }

    //------------------------- CONFIGURATION CLASS -------------------------//
    /**
     * <p>
     * Configuration for the {@link SCXMLWriter}. The configuration properties necessary for the following are
     * covered:
     * </p>
     *
     * <ul>
     *   <li>{@link XMLOutputFactory} configuration properties such as <code>factoryId</code> or any properties</li>
     *   <li>{@link XMLStreamWriter} configuration properties such as target {@link Writer} or {@link OutputStream}
     *   and the <code>encoding</code></li>
     * </ul>
     */
    public static class Configuration {

        /*
         * Configuration properties for this {@link SCXMLWriter}.
         */
        // XMLOutputFactory configuration properties.
        /**
         * The <code>factoryId</code> to use for the {@link XMLOutputFactory}.
         */
        final String factoryId;

        /**
         * The {@link ClassLoader} to use for the {@link XMLOutputFactory} instance to create.
         */
        final ClassLoader factoryClassLoader;

        /**
         * The map of properties (keys are property name strings, values are object property values) for the
         * {@link XMLOutputFactory}.
         */
        final Map<String, Object> properties;

        // XMLStreamWriter configuration properties.
        /**
         * The <code>encoding</code> to use for the {@link XMLStreamWriter}.
         */
        final String encoding;

        /**
         * Whether to use a pretty print style that makes the output much more human readable.
         */
        final boolean usePrettyPrint;

        /**
         * The intermediate writer that will hold the output to be pretty printed, given the lack of a standard
         * StAX property for the {@link XMLOutputFactory} in this regard. The contents will get transformed using
         * the transformation API.
         */
        final Writer internalWriter;

        // Underlying stream or writer close
        /**
         * Whether to close the underlying stream or writer passed by the caller.
         */
        final boolean closeUnderlyingWhenDone;

        /**
         * Whether to maintain an internal writer to return the serialization as a string.
         */
        boolean writeToString;

        /**
         * The pretty print output as a string.
         */
        String prettyPrintOutput;

        /*
         * Public constructors
         */
        /**
         * Default constructor.
         */
        public Configuration() {

            this(null, null, null, null, false, false, false);
        }

        /**
         * All-purpose constructor. Any of the parameters passed in can be <code>null</code> (booleans should default
         * to <code>false</code>). At the moment, the <code>factoryId</code> and <code>factoryClassLoader</code>
         * arguments are effectively ignored due to a bug in the underlying StAX {@link XMLOutputFactory} API.
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param factoryClassLoader The {@link ClassLoader} to use for the {@link XMLOutputFactory} instance to
         *                           create.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XMLOutputFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XMLStreamWriter}
         * @param usePrettyPrint Whether to make the output human readable as far as possible. Since StAX does not
         *                       provide a portable way to do this in JDK 1.6, choosing the pretty print option
         *                       is currently not very efficient.
         * @param closeUnderlyingWhenDone Whether to close the underlying stream or writer passed by the caller.
         */
        public Configuration(final String factoryId, final ClassLoader factoryClassLoader,
                             final Map<String, Object> properties, final String encoding, final boolean usePrettyPrint,
                             final boolean closeUnderlyingWhenDone) {

            this(factoryId, factoryClassLoader, properties, encoding, usePrettyPrint, closeUnderlyingWhenDone, false);
        }

        /*
         * Package access constructors
         */
        /**
         * Convenience package access constructor.
         *
         * @param writeToString Whether we will be returning the serialization as a string.
         * @param usePrettyPrint Whether we will attempt to make the output human readable as far as possible.
         */
        Configuration(final boolean writeToString, final boolean usePrettyPrint) {

            this(null, null, null, null, usePrettyPrint, false, writeToString);
        }

        /**
         * All-purpose package access constructor.
         *
         * @param factoryId The <code>factoryId</code> to use.
         * @param factoryClassLoader The {@link ClassLoader} to use for the {@link XMLOutputFactory} instance to
         *                           create.
         * @param properties The map of properties (keys are property name strings, values are object property values)
         *                   for the {@link XMLOutputFactory}.
         * @param encoding The <code>encoding</code> to use for the {@link XMLStreamWriter}
         * @param usePrettyPrint Whether to make the output human readable as far as possible. Since StAX does not
         *                       provide a portable way to do this in JDK 1.6, choosing the pretty print option
         *                       is currently not very efficient.
         * @param closeUnderlyingWhenDone Whether to close the underlying stream or writer passed by the caller.
         * @param writeToString Whether to maintain an internal writer to return the serialization as a string.
         */
        Configuration(final String factoryId, final ClassLoader factoryClassLoader,
                      final Map<String, Object> properties, final String encoding, final boolean usePrettyPrint,
                      final boolean closeUnderlyingWhenDone, final boolean writeToString) {

            this.factoryId = factoryId;
            this.factoryClassLoader = factoryClassLoader;
            this.properties = (properties == null ? new HashMap<>() : properties);
            this.encoding = encoding;
            this.usePrettyPrint = usePrettyPrint;
            this.closeUnderlyingWhenDone = closeUnderlyingWhenDone;
            this.writeToString = writeToString;
            if (this.usePrettyPrint || this.writeToString) {
                this.internalWriter = new StringWriter();
            } else {
                this.internalWriter = null;
            }
        }
    }
}
