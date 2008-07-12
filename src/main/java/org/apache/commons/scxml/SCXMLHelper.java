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
package org.apache.commons.scxml;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.model.Data;
import org.apache.commons.scxml.model.Datamodel;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.Path;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.semantics.ErrorConstants;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Helper class, all methods static final.
 *
 */
public final class SCXMLHelper {

    /**
     * Return true if the string is empty.
     *
     * @param attr The String to test
     * @return Is string empty
     */
    public static boolean isStringEmpty(final String attr) {
        if (attr == null || attr.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether a transition target tt (State or Parallel) is a
     * descendant of the transition target context.
     *
     * @param tt
     *            TransitionTarget to check - a potential descendant
     * @param ctx
     *            TransitionTarget context - a potential ancestor
     * @return true iff tt is a descendant of ctx, false otherwise
     */
    public static boolean isDescendant(final TransitionTarget tt,
            final TransitionTarget ctx) {
        TransitionTarget parent = tt.getParent();
        while (parent != null) {
            if (parent == ctx) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Creates a set which contains given states and all their ancestors
     * recursively up to the upper bound. Null upperBound means root
     * of the state machine.
     *
     * @param states The Set of States
     * @param upperBounds The Set of upper bound States
     * @return transitive closure of a given state set
     */
    public static Set<TransitionTarget> getAncestorClosure(final Set<TransitionTarget> states,
            final Set<TransitionTarget> upperBounds) {
        Set<TransitionTarget> closure = new HashSet<TransitionTarget>(states.size() * 2);
        for (TransitionTarget tt : states) {
            while (tt != null) {
                if (!closure.add(tt)) {
                    //tt is already a part of the closure
                    break;
                }
                if (upperBounds != null && upperBounds.contains(tt)) {
                    break;
                }
                tt = tt.getParent();
            }
        }
        return closure;
    }

    /**
     * Checks whether a given set of states is a legal Harel State Table
     * configuration (with the respect to the definition of the OR and AND
     * states).
     *
     * @param states
     *            a set of states
     * @param errRep
     *            ErrorReporter to report detailed error info if needed
     * @return true if a given state configuration is legal, false otherwise
     */
    public static boolean isLegalConfig(final Set<TransitionTarget> states,
            final ErrorReporter errRep) {
        /*
         * For every active state we add 1 to the count of its parent. Each
         * Parallel should reach count equal to the number of its children and
         * contribute by 1 to its parent. Each State should reach count exactly
         * 1. SCXML elemnt (top) should reach count exactly 1. We essentially
         * summarize up the hierarchy tree starting with a given set of
         * states = active configuration.
         */
        boolean legalConfig = true; // let's be optimists
        Map<TransitionTarget, Set<TransitionTarget>> counts =
            new IdentityHashMap<TransitionTarget, Set<TransitionTarget>>();
        Set<TransitionTarget> scxmlCount = new HashSet<TransitionTarget>();
        for (TransitionTarget tt : states) {
            TransitionTarget parent = null;
            while ((parent = tt.getParent()) != null) {
                Set<TransitionTarget> cnt = counts.get(parent);
                if (cnt == null) {
                    cnt = new HashSet<TransitionTarget>();
                    counts.put(parent, cnt);
                }
                cnt.add(tt);
                tt = parent;
            }
            //top-level contribution
            scxmlCount.add(tt);
        }
        //Validate counts:
        for (Map.Entry<TransitionTarget, Set<TransitionTarget>> entry : counts.entrySet()) {
            TransitionTarget tt = entry.getKey();
            Set<TransitionTarget> count = entry.getValue();
            if (tt instanceof Parallel) {
                Parallel p = (Parallel) tt;
                if (count.size() < p.getChildren().size()) {
                    errRep.onError(ErrorConstants.ILLEGAL_CONFIG,
                        "Not all AND states active for parallel "
                        + p.getId(), entry);
                    legalConfig = false;
                }
            } else {
                if (count.size() > 1) {
                    errRep.onError(ErrorConstants.ILLEGAL_CONFIG,
                        "Multiple OR states active for state "
                        + tt.getId(), entry);
                    legalConfig = false;
                }
            }
            count.clear(); //cleanup
        }
        if (scxmlCount.size() > 1) {
            errRep.onError(ErrorConstants.ILLEGAL_CONFIG,
                    "Multiple top-level OR states active!", scxmlCount);
        }
        //cleanup
        scxmlCount.clear();
        counts.clear();
        return legalConfig;
    }

    /**
     * Finds the least common ancestor of transition targets tt1 and tt2 if
     * one exists.
     *
     * @param tt1 First TransitionTarget
     * @param tt2 Second TransitionTarget
     * @return closest common ancestor of tt1 and tt2 or null
     */
    public static TransitionTarget getLCA(final TransitionTarget tt1,
            final TransitionTarget tt2) {
        if (tt1 == tt2) {
            return tt1; //self-transition
        } else if (isDescendant(tt1, tt2)) {
            return tt2;
        } else if (isDescendant(tt2, tt1)) {
            return tt1;
        }
        Set<TransitionTarget> parents = new HashSet<TransitionTarget>();
        TransitionTarget tmp = tt1;
        while ((tmp = tmp.getParent()) != null) {
            parents.add(tmp);
        }
        tmp = tt2;
        while ((tmp = tmp.getParent()) != null) {
            //test redundant add = common ancestor
            if (!parents.add(tmp)) {
                parents.clear();
                return tmp;
            }
        }
        return null;
    }

    /**
     * Returns the set of all states (and parallels) which are exited if a
     * given transition t is going to be taken.
     * Current states are necessary to be taken into account
     * due to orthogonal states and cross-region transitions -
     * see UML specs for more details.
     *
     * @param t
     *            transition to be taken
     * @param currentStates
     *            the set of current states (simple states only)
     * @return a set of all states (including composite) which are exited if a
     *         given transition is taken
     */
    public static Set<TransitionTarget> getStatesExited(final Transition t,
            final Set<TransitionTarget> currentStates) {
        Set<TransitionTarget> allStates = new HashSet<TransitionTarget>();
        if (t.getTargets().size() == 0) {
            return allStates;
        }
        Path p = (Path) t.getPaths().get(0); // all paths have same upseg
        //the easy part
        allStates.addAll(p.getUpwardSegment());
        TransitionTarget source = t.getParent();
        for (TransitionTarget a : currentStates) {
            if (isDescendant(a, source)) {
                boolean added = false;
                added = allStates.add(a);
                while (added && a != source) {
                    a = a.getParent();
                    added = allStates.add(a);
                }
            }
        }
        if (p.isCrossRegion()) {
            for (State region : p.getRegionsExited()) {
                Parallel par = (Parallel) region.getParent();
                //let's find affected states in sibling regions
                for (TransitionTarget tt : par.getChildren()) {
                    State s = (State) tt;
                    for (TransitionTarget a : currentStates) {
                        if (isDescendant(a, s)) {
                            //a is affected
                            boolean added = false;
                            added = allStates.add(a);
                            while (added && a != s) {
                                a = a.getParent();
                                added = allStates.add(a);
                            }
                        }
                    }
                }
            }
        }
        return allStates;
    }

    /**
     * According to the UML definition, two transitions
     * are conflicting if the sets of states they exit overlap.
     *
     * @param t1 a transition to check against t2
     * @param t2 a transition to check against t1
     * @param currentStates the set of current states (simple states only)
     * @return true if the t1 and t2 are conflicting transitions
     * @see #getStatesExited(Transition, Set)
     */
    public static boolean inConflict(final Transition t1,
            final Transition t2, final Set<TransitionTarget> currentStates) {
        Set<TransitionTarget> ts1 = getStatesExited(t1, currentStates);
        Set<TransitionTarget> ts2 = getStatesExited(t2, currentStates);
        ts1.retainAll(ts2);
        if (ts1.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Whether the first argument is a subtype of the second.
     *
     * @param child The candidate subtype
     * @param parent The supertype
     * @return true if child is subtype of parent, otherwise false
     */
    public static boolean subtypeOf(final Class<?> child, final Class<?> parent) {
        if (child == null || parent == null) {
            return false;
        }
        for (Class<?> current = child; current != Object.class;
                current = current.getSuperclass()) {
            if (current == parent) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the class implements the interface.
     *
     * @param clas The candidate class
     * @param interfayce The interface
     * @return true if clas implements interfayce, otherwise false
     */
    public static boolean implementationOf(final Class<?> clas,
            final Class<?> interfayce) {
        if (clas == null || interfayce == null || !interfayce.isInterface()) {
            return false;
        }
        for (Class<?> current = clas; current != Object.class;
                current = current.getSuperclass()) {
            Class<?>[] implementedInterfaces = current.getInterfaces();
            for (int i = 0; i < implementedInterfaces.length; i++) {
                if (implementedInterfaces[i] == interfayce) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set node value, depending on its type, from a String.
     *
     * @param node A Node whose value is to be set
     * @param value The new value
     */
    public static void setNodeValue(final Node node, final String value) {
        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                node.setNodeValue(value);
                break;
            case Node.ELEMENT_NODE:
                //remove all text children
                if (node.hasChildNodes()) {
                    Node child = node.getFirstChild();
                    while (child != null) {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            node.removeChild(child);
                        }
                        child = child.getNextSibling();
                    }
                }
                //create a new text node and append
                Text txt = node.getOwnerDocument().createTextNode(value);
                node.appendChild(txt);
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                ((CharacterData) node).setData(value);
                break;
            default:
                String err = "Trying to set value of a strange Node type: "
                    + node.getNodeType();
                //Logger.logln(Logger.E, err);
                throw new IllegalArgumentException(err);
        }
    }

    /**
     * Retrieve a DOM node value as a string depending on its type.
     *
     * @param node A node to be retreived
     * @return The value as a string
     */
    public static String getNodeValue(final Node node) {
        String result = "";
        if (node == null) {
            return result;
        }
        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                result = node.getNodeValue();
                break;
            case Node.ELEMENT_NODE:
                if (node.hasChildNodes()) {
                    Node child = node.getFirstChild();
                    StringBuffer buf = new StringBuffer();
                    while (child != null) {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            buf.append(((CharacterData) child).getData());
                        }
                        child = child.getNextSibling();
                    }
                    result = buf.toString();
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                result = ((CharacterData) node).getData();
                break;
            default:
                String err = "Trying to get value of a strange Node type: "
                    + node.getNodeType();
                //Logger.logln(Logger.W, err );
                throw new IllegalArgumentException(err);
        }
        return result.trim();
    }

    /**
     * Clone data model.
     *
     * @param ctx The context to clone to.
     * @param datamodel The datamodel to clone.
     * @param evaluator The expression evaluator.
     * @param log The error log.
     */
    public static void cloneDatamodel(final Datamodel datamodel,
            final Context ctx, final Evaluator evaluator,
            final Log log) {
        if (datamodel == null) {
            return;
        }
        List<Data> data = datamodel.getData();
        if (data == null) {
            return;
        }
        for (Data datum : data) {
            Node datumNode = datum.getNode();
            Node valueNode = null;
            if (datumNode != null) {
                valueNode = datumNode.cloneNode(true);
            }
            // prefer "src" over "expr" over "inline"
            if (!SCXMLHelper.isStringEmpty(datum.getSrc())) {
                ctx.setLocal(datum.getId(), valueNode);
            } else if (!SCXMLHelper.isStringEmpty(datum.
                    getExpr())) {
                Object value = null;
                try {
                    ctx.setLocal(NAMESPACES_KEY, datum.getNamespaces());
                    value = evaluator.eval(ctx, datum.getExpr());
                    ctx.setLocal(NAMESPACES_KEY, null);
                } catch (SCXMLExpressionException see) {
                    if (log != null) {
                        log.error(see.getMessage(), see);
                    } else {
                        Log defaultLog = LogFactory.getLog(SCXMLHelper.class);
                        defaultLog.error(see.getMessage(), see);
                    }
                }
                ctx.setLocal(datum.getId(), value);
            } else {
                ctx.setLocal(datum.getId(), valueNode);
            }
        }
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private SCXMLHelper() {
        super();
    }

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";

}

