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
package org.apache.commons.scxml.io;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Assign;
import org.apache.commons.scxml.model.Cancel;
import org.apache.commons.scxml.model.Data;
import org.apache.commons.scxml.model.Datamodel;
import org.apache.commons.scxml.model.Else;
import org.apache.commons.scxml.model.ElseIf;
import org.apache.commons.scxml.model.Exit;
import org.apache.commons.scxml.model.ExternalContent;
import org.apache.commons.scxml.model.Finalize;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.If;
import org.apache.commons.scxml.model.Initial;
import org.apache.commons.scxml.model.Invoke;
import org.apache.commons.scxml.model.Log;
import org.apache.commons.scxml.model.NamespacePrefixesHolder;
import org.apache.commons.scxml.model.OnEntry;
import org.apache.commons.scxml.model.OnExit;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.Param;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.model.Var;
import org.w3c.dom.Node;

/**
 * <p>Utility class for serializing the Commons SCXML Java object
 * model. Class uses the visitor pattern to trace through the
 * object heirarchy. Used primarily for testing, debugging and
 * visual verification.</p>
 *
 * <b>NOTE:</b> This serializer makes the following assumptions about the
 * original SCXML document(s) parsed to create the object model:
 * <ul>
 *  <li>The default document namespace is the SCXML namespace:
 *      <i>http://www.w3.org/2005/07/scxml</i></li>
 *  <li>The Commons SCXML namespace
 *      ( <i>http://commons.apache.org/scxml</i> ), if needed, uses the
 *      &quot;<i>cs</i>&quot; prefix</li>
 *  <li>All namespace prefixes needed throughout the document are
 *      declared on the document root element (&lt;scxml&gt;)</li>
 * </ul>
 */
public class SCXMLSerializer {

    /** The indent to be used while serializing an SCXML object. */
    private static final String INDENT = " ";
    /** The JAXP transformer. */
    private static final Transformer XFORMER = getTransformer();
    /** The SCXML namespace. */
    private static final String NAMESPACE_SCXML =
        "http://www.w3.org/2005/07/scxml";
    /** The Commons SCXML namespace. */
    private static final String NAMESPACE_COMMONS_SCXML =
        "http://commons.apache.org/scxml";

    /**
     * Serialize this SCXML object (primarily for debugging).
     *
     * @param scxml
     *            The SCXML to be serialized
     * @return String The serialized SCXML
     */
    public static String serialize(final SCXML scxml) {
        StringBuffer b =
            new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").
                append("<scxml xmlns=\"").append(NAMESPACE_SCXML).
                append("\"").append(serializeNamespaceDeclarations(scxml)).
                append(" version=\"").append(scxml.getVersion()).
                append("\" initial=\"").append(scxml.getInitial()).
                append("\">\n");
        if (XFORMER == null) {
            org.apache.commons.logging.Log log = LogFactory.
                getLog(SCXMLSerializer.class);
            log.warn("SCXMLSerializer: DOM serialization pertinent to"
                + " the document will be skipped since a suitable"
                + " JAXP Transformer could not be instantiated.");
        }
        b.append(INDENT).append("<!-- http://commons.apache.org/scxml -->\n");
        Datamodel dm = scxml.getDatamodel();
        if (dm != null) {
            serializeDatamodel(b, dm, INDENT);
        }
        Map c = scxml.getChildren();
        Iterator i = c.keySet().iterator();
        while (i.hasNext()) {
            TransitionTarget tt = (TransitionTarget) c.get(i.next());
            if (tt instanceof State) {
                serializeState(b, (State) tt, INDENT);
            } else {
                serializeParallel(b, (Parallel) tt, INDENT);
            }
        }
        b.append("</scxml>\n");
        return b.toString();
    }

    /**
     * Serialize this State object.
     *
     * @param b The buffer to append the serialization to
     * @param s The State to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeState(final StringBuffer b,
            final State s, final String indent) {
        b.append(indent).append("<state");
        serializeTransitionTargetAttributes(b, s);
        boolean f = s.isFinal();
        if (f) {
            b.append(" final=\"true\"");
        }
        b.append(">\n");
        Initial ini = s.getInitial();
        if (ini != null) {
            serializeInitial(b, ini, indent + INDENT);
        }
        List h = s.getHistory();
        if (h != null) {
            serializeHistory(b, h, indent + INDENT);
        }
        Datamodel dm = s.getDatamodel();
        if (dm != null) {
            serializeDatamodel(b, dm, indent + INDENT);
        }
        serializeOnEntry(b, s, indent + INDENT);
        List t = s.getTransitionsList();
        for (int i = 0; i < t.size(); i++) {
            serializeTransition(b, (Transition) t.get(i), indent + INDENT);
        }
        Parallel p = s.getParallel(); //TODO: Remove in v1.0
        Invoke inv = s.getInvoke();
        if (p != null) {
            serializeParallel(b, p, indent + INDENT);
        } else if (inv != null) {
            serializeInvoke(b , inv, indent + INDENT);
        } else {
            Map c = s.getChildren();
            Iterator j = c.keySet().iterator();
            while (j.hasNext()) {
                TransitionTarget tt = (TransitionTarget) c.get(j.next());
                if (tt instanceof State) {
                    serializeState(b, (State) tt, indent + INDENT);
                } else if (tt instanceof Parallel) {
                    serializeParallel(b, (Parallel) tt, indent + INDENT);
                }
            }
        }
        serializeOnExit(b, s, indent + INDENT);
        b.append(indent).append("</state>\n");
    }

    /**
     * Serialize this Parallel object.
     *
     * @param b The buffer to append the serialization to
     * @param p The Parallel to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeParallel(final StringBuffer b,
            final Parallel p, final String indent) {
        b.append(indent).append("<parallel");
        serializeTransitionTargetAttributes(b, p);
        b.append(">\n");
        serializeOnEntry(b, p, indent + INDENT);
        Set s = p.getChildren();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            serializeState(b, (State) i.next(), indent + INDENT);
        }
        serializeOnExit(b, p, indent + INDENT);
        b.append(indent).append("</parallel>\n");
    }

    /**
     * Serialize this Invoke object.
     *
     * @param b The buffer to append the serialization to
     * @param i The Invoke to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeInvoke(final StringBuffer b,
            final Invoke i, final String indent) {
        b.append(indent).append("<invoke");
        String ttype = i.getTargettype();
        String src = i.getSrc();
        String srcexpr = i.getSrcexpr();
        if (ttype != null) {
            b.append(" targettype=\"").append(ttype).append("\"");
        }
        // Prefer src
        if (src != null) {
            b.append(" src=\"").append(src).append("\"");
        } else if (srcexpr != null) {
            b.append(" srcexpr=\"").append(srcexpr).append("\"");
        }
        b.append(">\n");
        List params = i.params();
        for (Iterator iter = params.iterator(); iter.hasNext();) {
            Param p = (Param) iter.next();
            b.append(indent).append(INDENT).append("<param name=\"").
                append(p.getName()).append("\" expr=\"").
                append(SCXMLHelper.escapeXML(p.getExpr())).append("\"/>\n");
        }
        Finalize f = i.getFinalize();
        if (f != null) {
            b.append(indent).append(INDENT).append("<finalize>\n");
            serializeActions(b, f.getActions(), indent + INDENT + INDENT);
            b.append(indent).append(INDENT).append("</finalize>\n");
        }
        b.append(indent).append("</invoke>\n");
    }

    /**
     * Serialize this Initial object.
     *
     * @param b The buffer to append the serialization to
     * @param i The Initial to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeInitial(final StringBuffer b, final Initial i,
            final String indent) {
        b.append(indent).append("<initial");
        serializeTransitionTargetAttributes(b, i);
        b.append(">\n");
        serializeTransition(b, i.getTransition(), indent + INDENT);
        b.append(indent).append("</initial>\n");
    }

    /**
     * Serialize the History.
     *
     * @param b The buffer to append the serialization to
     * @param l The List of History objects to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeHistory(final StringBuffer b, final List l,
            final String indent) {
        if (l.size() > 0) {
            for (int i = 0; i < l.size(); i++) {
                History h = (History) l.get(i);
                b.append(indent).append("<history");
                serializeTransitionTargetAttributes(b, h);
                 if (h.isDeep()) {
                     b.append(" type=\"deep\"");
                 } else {
                     b.append(" type=\"shallow\"");
                 }
                b.append(">\n");
                serializeTransition(b, h.getTransition(), indent + INDENT);
                b.append(indent).append("</history>\n");
            }
        }
    }

    /**
     * Serialize this Transition object.
     *
     * @param b The buffer to append the serialization to
     * @param t The Transition to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeTransition(final StringBuffer b,
            final Transition t, final String indent) {
        b.append(indent).append("<transition");
        if (!SCXMLHelper.isStringEmpty(t.getEvent())) {
            b.append(" event=\"").append(t.getEvent()).append("\"");
        }
        if (!SCXMLHelper.isStringEmpty(t.getCond())) {
            b.append(" cond=\"").append(SCXMLHelper.escapeXML(t.getCond())).
                append("\"");
        }
        boolean next = !SCXMLHelper.isStringEmpty(t.getNext());
        if (next) {
            b.append(" target=\"" + t.getNext() + "\"");
        }
        b.append(">\n");
        boolean exit = serializeActions(b, t.getActions(), indent + INDENT);
        if (!next && !exit) {
            serializeTarget(b, t, indent + INDENT);
        }
        b.append(indent).append("</transition>\n");
    }

    /**
     * Serialize this Transition's Target.
     *
     *
     * @param b The buffer to append the serialization to
     * @param t The Transition whose Target needs to be serialized
     * @param indent The indent for this XML element
     *
     * @deprecated Inline &lt;target&gt; element has been deprecated
     *             in the SCXML WD
     */
    public static void serializeTarget(final StringBuffer b,
            final Transition t, final String indent) {
        if (t.getTarget() != null) {
            b.append(indent).append("<target>");
            // The inline transition target can only be a state
            serializeState(b, (State) t.getTarget(), indent + INDENT);
            b.append(indent).append("</target>");
        }
    }

    /**
     * Serialize this Datamodel object.
     *
     * @param b The buffer to append the serialization to
     * @param dm The Datamodel to be serialized
     * @param indent The indent for this XML element
     */
    public static void serializeDatamodel(final StringBuffer b,
            final Datamodel dm, final String indent) {
        List data = dm.getData();
        if (data != null && data.size() > 0) {
            b.append(indent).append("<datamodel>\n");
            if (XFORMER == null) {
                b.append(indent).append(INDENT).
                    append("<!-- Body content was not serialized -->\n");
                b.append(indent).append("</datamodel>\n");
                return;
            }
            for (Iterator iter = data.iterator(); iter.hasNext();) {
                Data datum = (Data) iter.next();
                Node dataNode = datum.getNode();
                if (dataNode != null) {
                    StringWriter out = new StringWriter();
                    try {
                        Source input = new DOMSource(dataNode);
                        Result output = new StreamResult(out);
                        XFORMER.transform(input, output);
                    } catch (TransformerException te) {
                        org.apache.commons.logging.Log log = LogFactory.
                            getLog(SCXMLSerializer.class);
                        log.error(te.getMessage(), te);
                        b.append(indent).append(INDENT).
                            append("<!-- Data content not serialized -->\n");
                    }
                    b.append(indent).append(INDENT).append(out.toString());
                } else {
                    b.append(indent).append(INDENT).append("<data id=\"").
                        append(datum.getId()).append("\" expr=\"").
                        append(SCXMLHelper.escapeXML(datum.getExpr())).
                        append("\" />\n");
                }
            }
            b.append(indent).append("</datamodel>\n");
        }
    }

    /**
     * Serialize this OnEntry object.
     *
     * @param b The buffer to append the serialization to
     * @param t The TransitionTarget whose OnEntry is to be serialized
     * @param indent The indent for this XML element
     */
    public static void serializeOnEntry(final StringBuffer b,
            final TransitionTarget t, final String indent) {
        OnEntry e = t.getOnEntry();
        if (e != null && e.getActions().size() > 0) {
            b.append(indent).append("<onentry>\n");
            serializeActions(b, e.getActions(), indent + INDENT);
            b.append(indent).append("</onentry>\n");
        }
    }

    /**
     * Serialize this OnExit object.
     *
     * @param b The buffer to append the serialization to
     * @param t The TransitionTarget whose OnExit is to be serialized
     * @param indent The indent for this XML element
     */
    public static void serializeOnExit(final StringBuffer b,
            final TransitionTarget t, final String indent) {
        OnExit x = t.getOnExit();
        if (x != null && x.getActions().size() > 0) {
            b.append(indent).append("<onexit>\n");
            serializeActions(b, x.getActions(), indent + INDENT);
            b.append(indent).append("</onexit>\n");
        }
    }

    /**
     * Serialize this List of actions.
     *
     * @param b The buffer to append the serialization to
     * @param l The List of actions to serialize
     * @param indent The indent for this XML element
     * @return boolean true if the list of actions contains an &lt;exit/&gt;
     */
    public static boolean serializeActions(final StringBuffer b, final List l,
            final String indent) {
        if (l == null) {
            return false;
        }
        boolean exit = false;
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Action a = (Action) i.next();
            if (a instanceof Var) {
                Var v = (Var) a;
                b.append(indent).append("<cs:var name=\"").append(v.getName())
                    .append("\" expr=\"")
                    .append(SCXMLHelper.escapeXML(v.getExpr()))
                    .append("\"/>\n");
            } else if (a instanceof Assign) {
                Assign asn = (Assign) a;
                b.append(indent).append("<assign");
                if (!SCXMLHelper.isStringEmpty(asn.getLocation())) {
                    b.append(" location=\"").append(asn.getLocation());
                    if (!SCXMLHelper.isStringEmpty(asn.getSrc())) {
                        b.append("\" src=\"").append(asn.getSrc());
                    } else {
                        b.append("\" expr=\"").
                            append(SCXMLHelper.escapeXML(asn.getExpr()));
                    }
                } else {
                    b.append(" name=\"").append(asn.getName()).
                        append("\" expr=\"").
                        append(SCXMLHelper.escapeXML(asn.getExpr()));
                }
                b.append("\"/>\n");
            } else if (a instanceof Send) {
                serializeSend(b, (Send) a, indent);
            } else if (a instanceof Cancel) {
                Cancel c = (Cancel) a;
                b.append(indent).append("<cancel sendid=\"")
                    .append(c.getSendid()).append("\"/>\n");
            } else if (a instanceof Log) {
                Log lg = (Log) a;
                b.append(indent).append("<log expr=\"").
                    append(SCXMLHelper.escapeXML(lg.getExpr())).
                    append("\"/>\n");
            } else if (a instanceof Exit) {
                Exit e = (Exit) a;
                b.append(indent).append("<cs:exit");
                String expr = SCXMLHelper.escapeXML(e.getExpr());
                String nl = e.getNamelist();
                if (expr != null) {
                    b.append(" expr=\"" + expr + "\"");
                }
                if (nl != null) {
                    b.append(" namelist=\"" + nl + "\"");
                }
                b.append("/>\n");
                exit = true;
            } else if (a instanceof If) {
                If iff = (If) a;
                serializeIf(b, iff, indent);
            } else if (a instanceof Else) {
                b.append(indent).append("<else/>\n");
            } else if (a instanceof ElseIf) {
                ElseIf eif = (ElseIf) a;
                b.append(indent).append("<elseif cond=\"")
                    .append(SCXMLHelper.escapeXML(eif.getCond()))
                    .append("\" />\n");
            }
        }
        return exit;
    }

    /**
     * Serialize this Send object.
     *
     * @param b The buffer to append the serialization to
     * @param send The Send object to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeSend(final StringBuffer b,
            final Send send, final String indent) {
        b.append(indent).append("<send");
        if (send.getSendid() != null) {
            b.append(" sendid=\"").append(send.getSendid()).append("\"");
        }
        if (send.getTarget() != null) {
            b.append(" target=\"").append(send.getTarget()).append("\"");
        }
        if (send.getTargettype() != null) {
            b.append(" targetType=\"").append(send.getTargettype()).append("\"");
        }
        if (send.getNamelist() != null) {
            b.append(" namelist=\"").append(send.getNamelist()).append("\"");
        }
        if (send.getDelay() != null) {
            b.append(" delay=\"").append(send.getDelay()).append("\"");
        }
        if (send.getEvent() != null) {
            b.append(" event=\"").append(send.getEvent()).append("\"");
        }
        if (send.getHints() != null) {
            b.append(" hints=\"").append(send.getHints()).append("\"");
        }
        b.append(">\n");
        b.append(getBodyContent(send));
        b.append(indent).append("</send>\n");
    }

    /**
     * Return serialized body of <code>ExternalContent</code>.
     *
     * @param externalContent The model element containing the body content
     * @return String The serialized body content
     */
    public static final String getBodyContent(
            final ExternalContent externalContent) {
        StringBuffer buf = new StringBuffer();
        List externalNodes = externalContent.getExternalNodes();
        if (externalNodes.size() > 0 && XFORMER == null) {
            buf.append("<!-- Body content was not serialized -->\n");
            return buf.toString();
        }
        for (int i = 0; i < externalNodes.size(); i++) {
            Source input = new DOMSource((Node) externalNodes.get(i));
            StringWriter out = new StringWriter();
            Result output = new StreamResult(out);
            try {
                XFORMER.transform(input, output);
            } catch (TransformerException te) {
                org.apache.commons.logging.Log log = LogFactory.
                    getLog(SCXMLSerializer.class);
                log.error(te.getMessage(), te);
                buf.append("<!-- Not all body content was serialized -->");
            }
            buf.append(out.toString()).append("\n");
        }
        return buf.toString();
    }

    /**
     * Serialize this If object.
     *
     * @param b The buffer to append the serialization to
     * @param iff The If object to serialize
     * @param indent The indent for this XML element
     */
    public static void serializeIf(final StringBuffer b,
            final If iff, final String indent) {
        b.append(indent).append("<if cond=\"").append(SCXMLHelper.
            escapeXML(iff.getCond())).append("\">\n");
        serializeActions(b, iff.getActions(), indent + INDENT);
        b.append(indent).append("</if>\n");
    }

    /**
     * Serialize properties of TransitionTarget which are element attributes.
     *
     * @param b The buffer to append the serialization to
     * @param t The TransitionTarget
     */
    private static void serializeTransitionTargetAttributes(
            final StringBuffer b, final TransitionTarget t) {
        String id = t.getId();
        if (id != null) {
            b.append(" id=\"").append(id).append("\"");
        }
    }

    /**
     * Serialize namespace declarations for the root SCXML element.
     *
     * @param holder The {@link NamespacePrefixesHolder} object
     * @return The serialized namespace declarations
     */
    private static String serializeNamespaceDeclarations(
            final NamespacePrefixesHolder holder) {
        Map ns = holder.getNamespaces();
        StringBuffer b = new StringBuffer();
        if (ns != null) {
            Iterator iter = ns.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String prefix = (String) entry.getKey();
                String nsURI = (String) entry.getValue();
                if (prefix.length() == 0 && !nsURI.equals(NAMESPACE_SCXML)) {
                    org.apache.commons.logging.Log log = LogFactory.
                        getLog(SCXMLSerializer.class);
                    log.warn("When using the SCXMLSerializer, the default "
                        + "namespace must be the SCXML namespace:"
                        + NAMESPACE_SCXML);
                } if (prefix.equals("cs")
                        && !nsURI.equals(NAMESPACE_COMMONS_SCXML)) {
                    org.apache.commons.logging.Log log = LogFactory.
                        getLog(SCXMLSerializer.class);
                    log.warn("When using the SCXMLSerializer, the namespace"
                        + "prefix \"cs\" must bind to the Commons SCXML "
                        + "namespace:" + NAMESPACE_COMMONS_SCXML);
                } else if (prefix.length() > 0) {
                    b.append(" xmlns:").append(prefix).append("=\"").
                        append(nsURI).append("\"");
                }
            }
        }
        return b.toString();
    }

    /**
     * Get a <code>Transformer</code> instance.
     *
     * @return Transformer The <code>Transformer</code> instance.
     */
    private static Transformer getTransformer() {
        Transformer transformer = null;
        Properties outputProps = new Properties();
        outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
        outputProps.put(OutputKeys.STANDALONE, "no");
        outputProps.put(OutputKeys.INDENT, "yes");
        try {
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            transformer = tfFactory.newTransformer();
            transformer.setOutputProperties(outputProps);
        } catch (TransformerFactoryConfigurationError t) {
            return null;
        } catch (TransformerConfigurationException e) {
            return null;
        }
        return transformer;
    }

    /*
     * Private methods.
     */
    /**
     * Discourage instantiation since this is a utility class.
     */
    private SCXMLSerializer() {
        super();
    }

}

