/*
 *
 *   Copyright 2006 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.scxml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Assign;
import org.apache.commons.scxml.model.Cancel;
import org.apache.commons.scxml.model.Else;
import org.apache.commons.scxml.model.ElseIf;
import org.apache.commons.scxml.model.Exit;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.If;
import org.apache.commons.scxml.model.Initial;
import org.apache.commons.scxml.model.Log;
import org.apache.commons.scxml.model.OnEntry;
import org.apache.commons.scxml.model.OnExit;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.model.Var;

/**
 * Utility class for serializing the Commons SCXML Java object
 * model. Class uses the visitor pattern to trace through the
 * object heirarchy. Used primarily for testing, debugging and
 * visual verification.
 *
 */
public class SCXMLSerializer {

    /** The indent to be used while serializing an SCXML object. */
    private static final String INDENT = " ";

    /**
     * Serialize this SCXML object (primarily for debugging).
     *
     * @param scxml
     *            The SCXML to be serialized
     * @return String The serialized SCXML
     */
    public static String serialize(final SCXML scxml) {
        StringBuffer b = new StringBuffer("<scxml xmlns=\"").append(
                scxml.getXmlns()).append("\" version=\"").append(
                scxml.getVersion()).append("\" initialstate=\"").append(
                scxml.getInitialstate()).append("\">\n");
        Map s = scxml.getStates();
        Iterator i = s.keySet().iterator();
        while (i.hasNext()) {
            serializeState(b, (State) s.get(i.next()), INDENT);
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
        boolean f = s.getIsFinal();
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
        serializeOnEntry(b, s, indent + INDENT);
        Map t = s.getTransitions();
        Iterator i = t.keySet().iterator();
        while (i.hasNext()) {
            List et = (List) t.get(i.next());
            for (int len = 0; len < et.size(); len++) {
                serializeTransition(b, (Transition) et.get(len), indent
                        + INDENT);
            }
        }
        Parallel p = s.getParallel();
        if (p != null) {
            serializeParallel(b, p, indent + INDENT);
        } else {
            Map c = s.getChildren();
            Iterator j = c.keySet().iterator();
            while (j.hasNext()) {
                State cs = (State) c.get(j.next());
                serializeState(b, cs, indent + INDENT);
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
        Set s = p.getStates();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            serializeState(b, (State) i.next(), indent + INDENT);
        }
        serializeOnExit(b, p, indent + INDENT);
        b.append(indent).append("</parallel>\n");
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
        b.append(indent).append("<transition event=\"").append(t.getEvent())
                .append("\" cond=\"").append(t.getCond()).append("\">\n");
        boolean exit = serializeActions(b, t.getActions(), indent + INDENT);
        if (!exit) {
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
     */
    public static void serializeTarget(final StringBuffer b,
            final Transition t, final String indent) {
        b.append(indent).append("<target");
        String n = t.getNext();
        if (n != null) {
            b.append(" next=\"" + n + "\">\n");
        } else {
            b.append(">\n");
            if (t.getTarget() != null) {
                // The inline transition target can only be a state
                serializeState(b, (State) t.getTarget(), indent + INDENT);
            }
        }
        b.append(indent).append("</target>\n");
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
                b.append(indent).append("<var name=\"").append(v.getName())
                        .append("\" expr=\"").append(v.getExpr()).append(
                                "\"/>\n");
            } else if (a instanceof Assign) {
                Assign asn = (Assign) a;
                b.append(indent).append("<assign name=\"")
                        .append(asn.getName()).append("\" expr=\"")
                        .append(asn.getExpr()).append("\"/>\n");
            } else if (a instanceof Send) {
                serializeSend(b, (Send) a, indent);
            } else if (a instanceof Cancel) {
                Cancel c = (Cancel) a;
                b.append(indent).append("<cancel sendid=\"")
                    .append(c.getSendid()).append("\"/>\n");
            } else if (a instanceof Log) {
                Log lg = (Log) a;
                b.append(indent).append("<log expr=\"").append(lg.getExpr())
                        .append("\"/>\n");
            } else if (a instanceof Exit) {
                Exit e = (Exit) a;
                b.append(indent).append("<exit");
                String expr = e.getExpr();
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
                        .append(eif.getCond()).append("\" />\n");
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
        b.append(indent).append("<send sendid=\"")
            .append(send.getSendid()).append("\" target=\"")
            .append(send.getTarget()).append("\" targetType=\"")
            .append(send.getTargettype()).append("\" namelist=\"")
            .append(send.getNamelist()).append("\" delay=\"")
            .append(send.getDelay()).append("\" events=\"")
            .append(send.getEvent()).append("\" hints=\"")
            .append(send.getHints()).append("\">\n");
        /* TODO - Serialize body content
        try {
            b.append(send.getBodyContent());
        } catch (IOException ioe) {
            log.error("Failed to serialize external nodes for <send>", ioe);
        }
        */
        b.append(indent).append("</send>\n");
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
        b.append(indent).append("<if cond=\"").append(iff.getCond()).append(
                "\">\n");
        serializeActions(b, iff.getActions(), indent + INDENT);
        b.append(indent).append("</if>\n");
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
            b.append(" id=\"" + id + "\"");
        }
        TransitionTarget pt = t.getParent();
        if (pt != null) {
            String pid = pt.getId();
            if (pid != null) {
                b.append(" parentid=\"").append(pid).append("\"");
            }
        }
    }

}
