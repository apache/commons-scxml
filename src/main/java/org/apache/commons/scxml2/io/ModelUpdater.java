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

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.SCXMLHelper;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.Initial;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;

/**
 * The ModelUpdater provides the utility methods to check the Commons
 * SCXML model for inconsistencies, detect errors, and wire the Commons
 * SCXML model appropriately post document parsing by the SCXMLReader to make
 * it executor ready.
 */
final class ModelUpdater {

    //// Error messages
    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_SCXML_NO_INIT = "No SCXML child state "
            + "with ID \"{0}\" found; illegal initial state for SCXML document";

    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_UNSUPPORTED_INIT = "Initial attribute or element not supported for "
            + "atomic {0}";

    /**
     * Error message when a state element specifies an initial state which
     * is not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT = "Initial state "
            + "null or not a descendant of {0}";

    /**
     * Error message when a referenced history state cannot be found.
     */
    private static final String ERR_STATE_NO_HIST = "Referenced history state"
            + " null for {0}";

    /**
     * Error message when a shallow history state is not a child state.
     */
    private static final String ERR_STATE_BAD_SHALLOW_HIST = "History state"
            + " for shallow history is not child for {0}";

    /**
     * Error message when a deep history state is not a descendent state.
     */
    private static final String ERR_STATE_BAD_DEEP_HIST = "History state"
            + " for deep history is not descendant for {0}";

    /**
     * Transition target is not a legal IDREF (not found).
     */
    private static final String ERR_TARGET_NOT_FOUND =
            "Transition target with ID \"{0}\" not found";

    /**
     * Transition targets do not form a legal configuration.
     */
    private static final String ERR_ILLEGAL_TARGETS =
            "Transition targets \"{0}\" do not satisfy the requirements for"
                    + " target regions belonging to a <parallel>";

    /**
     * Simple states should not contain a history.
     */
    private static final String ERR_HISTORY_SIMPLE_STATE =
            "Simple {0} contains history elements";

    /**
     * History does not specify a default transition target.
     */
    private static final String ERR_HISTORY_NO_DEFAULT =
            "No default target specified for history with ID \"{0}\""
                    + " belonging to {1}";

    /**
     * Error message when an &lt;invoke&gt; does not specify a "type"
     * attribute.
     */
    private static final String ERR_INVOKE_NO_TYPE = "{0} contains "
            + "<invoke> with no \"type\" attribute specified.";

    /**
     * Error message when an &lt;invoke&gt; does not specify a "src"
     * or a "srcexpr" attribute.
     */
    private static final String ERR_INVOKE_NO_SRC = "{0} contains "
            + "<invoke> without a \"src\" or \"srcexpr\" attribute specified.";

    /**
     * Error message when an &lt;invoke&gt; specifies both "src" and "srcexpr"
     * attributes.
     */
    private static final String ERR_INVOKE_AMBIGUOUS_SRC = "{0} contains "
            + "<invoke> with both \"src\" and \"srcexpr\" attributes specified,"
            + " must specify either one, but not both.";

    /**
     * Discourage instantiation since this is a utility class.
     */
    private ModelUpdater() {
        super();
    }

    /*
     * Post-processing methods to make the SCXML object SCXMLExecutor ready.
     */
    /**
     * <p>Update the SCXML object model and make it SCXMLExecutor ready.
     * This is part of post-read processing, and sets up the necessary
     * object references throughtout the SCXML object model for the parsed
     * document.</p>
     *
     * @param scxml The SCXML object (output from SCXMLReader)
     * @throws ModelException If the object model is flawed
     */
    static void updateSCXML(final SCXML scxml) throws ModelException {
        String initial = scxml.getInitial();
        Transition initialTransition = new Transition();

        if (initial != null) {

            initialTransition.setNext(scxml.getInitial());
            updateTransition(initialTransition, scxml.getTargets());

            if (initialTransition.getTargets().size() == 0) {
                logAndThrowModelError(ERR_SCXML_NO_INIT, new Object[] {
                        initial });
            }
        } else {
            // If 'initial' is not specified, the default initial state is
            // the first child state in document order.
            initialTransition.getTargets().add(scxml.getFirstChild());
            initialTransition.getPaths(); // init paths
        }

        scxml.setInitialTransition(initialTransition);
        Map<String, TransitionTarget> targets = scxml.getTargets();
        Map<String, TransitionTarget> children = scxml.getChildren();
        for (TransitionTarget tt : children.values()) {
            if (tt instanceof State) {
                updateState((State) tt, targets);
            } else {
                updateParallel((Parallel) tt, targets);
            }
        }
    }

    /**
     * Update this State object (part of post-read processing).
     * Also checks for any errors in the document.
     *
     * @param s The State object
     * @param targets The global Map of all transition targets
     * @throws ModelException If the object model is flawed
     */
    private static void updateState(final State s, final Map<String, TransitionTarget> targets)
            throws ModelException {
        Map<String, TransitionTarget> c = s.getChildren();
        if (s.isComposite()) {
            //initialize next / initial
            Initial ini = s.getInitial();
            if (ini == null) {
                s.setFirst(c.keySet().iterator().next());
                ini = s.getInitial();
            }
            Transition initialTransition = ini.getTransition();
            updateTransition(initialTransition, targets);
            List<TransitionTarget> initialStates = initialTransition.getTargets();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialStates.size() == 0) {
                logAndThrowModelError(ERR_STATE_BAD_INIT,
                        new Object[] {getName(s)});
            } else {
                for (TransitionTarget initialState : initialStates) {
                    if (!SCXMLHelper.isDescendant(initialState, s)) {
                        logAndThrowModelError(ERR_STATE_BAD_INIT,
                                new Object[] {getName(s)});
                    }
                }
            }
        }
        else if (s.getInitial() != null) {
            logAndThrowModelError(ERR_UNSUPPORTED_INIT, new Object[] {getName(s)});
        }

        List<History> histories = s.getHistory();
        if (histories.size() > 0 && s.isSimple()) {
            logAndThrowModelError(ERR_HISTORY_SIMPLE_STATE,
                    new Object[] {getName(s)});
        }
        for (History h : histories) {
            updateHistory(h, targets, s);
        }
        for (Transition trn : s.getTransitionsList()) {
            updateTransition(trn, targets);
        }

        // TODO: state must may have multiple invokes
        Invoke inv = s.getInvoke();
        if (inv != null) {
            String type = inv.getType();
            if (type == null || type.trim().length() == 0) {
                logAndThrowModelError(ERR_INVOKE_NO_TYPE,
                        new Object[] {getName(s)});
            }
            String src = inv.getSrc();
            boolean noSrc = (src == null || src.trim().length() == 0);
            String srcexpr = inv.getSrcexpr();
            boolean noSrcexpr = (srcexpr == null
                    || srcexpr.trim().length() == 0);
            if (noSrc && noSrcexpr) {
                logAndThrowModelError(ERR_INVOKE_NO_SRC,
                        new Object[] {getName(s)});
            }
            if (!noSrc && !noSrcexpr) {
                logAndThrowModelError(ERR_INVOKE_AMBIGUOUS_SRC,
                        new Object[] {getName(s)});
            }
        }

        for (TransitionTarget tt : c.values()) {
            if (tt instanceof State) {
                updateState((State) tt, targets);
            } else if (tt instanceof Parallel) {
                updateParallel((Parallel) tt, targets);
            }
        }
    }

    /**
     * Update this Parallel object (part of post-read processing).
     *
     * @param p The Parallel object
     * @param targets The global Map of all transition targets
     * @throws ModelException If the object model is flawed
     */
    private static void updateParallel(final Parallel p, final Map<String, TransitionTarget> targets)
            throws ModelException {
        for (TransitionTarget tt : p.getChildren()) {
            updateState((State) tt, targets);
        }
        for (Transition trn : p.getTransitionsList()) {
            updateTransition(trn, targets);
        }
        List<History> histories = p.getHistory();
        for (History h : histories) {
            updateHistory(h, targets, p);
        }
        // TODO: parallel must may have invokes too
    }

    /**
     * Update this History object (part of post-read processing).
     *
     * @param h The History object
     * @param targets The global Map of all transition targets
     * @param parent The parent TransitionTarget for this History
     * @throws ModelException If the object model is flawed
     */
    private static void updateHistory(final History h,
                                      final Map<String, TransitionTarget> targets,
                                      final TransitionTarget parent)
            throws ModelException {
        Transition historyTransition = h.getTransition();
        if (historyTransition == null || historyTransition.getNext() == null) {
            logAndThrowModelError(ERR_HISTORY_NO_DEFAULT,
                    new Object[] {h.getId(), getName(parent)});
        }
        else {
            updateTransition(historyTransition, targets);
            List<TransitionTarget> historyStates = historyTransition.getTargets();
            if (historyStates.size() == 0) {
                logAndThrowModelError(ERR_STATE_NO_HIST,
                        new Object[] {getName(parent)});
            }
            for (TransitionTarget historyState : historyStates) {
                if (!h.isDeep()) {
                    // Shallow history
                    boolean shallow = false;
                    if (parent instanceof State) {
                        shallow = ((State) parent).getChildren().
                                containsValue(historyState);
                    } else if (parent instanceof Parallel) {
                        shallow = ((Parallel) parent).getChildren().
                                contains(historyState);
                    }
                    if (!shallow) {
                        logAndThrowModelError(ERR_STATE_BAD_SHALLOW_HIST,
                                new Object[] {getName(parent)});
                    }
                } else {
                    // Deep history
                    if (!SCXMLHelper.isDescendant(historyState, parent)) {
                        logAndThrowModelError(ERR_STATE_BAD_DEEP_HIST,
                                new Object[] {getName(parent)});
                    }
                }
            }
        }
    }

    /**
     * Update this Transition object (part of post-read processing).
     *
     * @param t The Transition object
     * @param targets The global Map of all transition targets
     * @throws ModelException If the object model is flawed
     */
    private static void updateTransition(final Transition t,
                                         final Map<String, TransitionTarget> targets) throws ModelException {
        String next = t.getNext();
        if (next == null) { // stay transition
            return;
        }
        List<TransitionTarget> tts = t.getTargets();
        if (tts.size() == 0) {
            // 'next' is a space separated list of transition target IDs
            StringTokenizer ids = new StringTokenizer(next);
            while (ids.hasMoreTokens()) {
                String id = ids.nextToken();
                TransitionTarget tt = targets.get(id);
                if (tt == null) {
                    logAndThrowModelError(ERR_TARGET_NOT_FOUND, new Object[] {
                            id });
                }
                tts.add(tt);
            }
            if (tts.size() > 1) {
                boolean legal = verifyTransitionTargets(tts);
                if (!legal) {
                    logAndThrowModelError(ERR_ILLEGAL_TARGETS, new Object[] {
                            next });
                }
            }
        }
        t.getPaths(); // init paths
    }

    /**
     * Log an error discovered in post-read processing.
     *
     * @param errType The type of error
     * @param msgArgs The arguments for formatting the error message
     * @throws ModelException The model error, always thrown.
     */
    private static void logAndThrowModelError(final String errType,
                                              final Object[] msgArgs) throws ModelException {
        MessageFormat msgFormat = new MessageFormat(errType);
        String errMsg = msgFormat.format(msgArgs);
        org.apache.commons.logging.Log log = LogFactory.
                getLog(ModelUpdater.class);
        log.error(errMsg);
        throw new ModelException(errMsg);
    }

    /**
     * Get a transition target identifier for error messages. This method is
     * only called to produce an appropriate log message in some error
     * conditions.
     *
     * @param tt The <code>TransitionTarget</code> object
     * @return The transition target identifier for the error message
     */
    private static String getName(final TransitionTarget tt) {
        String name = "anonymous transition target";
        if (tt instanceof State) {
            name = "anonymous state";
            if (!SCXMLHelper.isStringEmpty(tt.getId())) {
                name = "state with ID \"" + tt.getId() + "\"";
            }
        } else if (tt instanceof Parallel) {
            name = "anonymous parallel";
            if (!SCXMLHelper.isStringEmpty(tt.getId())) {
                name = "parallel with ID \"" + tt.getId() + "\"";
            }
        } else {
            if (!SCXMLHelper.isStringEmpty(tt.getId())) {
                name = "transition target with ID \"" + tt.getId() + "\"";
            }
        }
        return name;
    }

    /**
     * If a transition has multiple targets, then they satisfy the following
     * criteria.
     * <ul>
     *  <li>They must belong to the regions of the same parallel</li>
     *  <li>All regions must be represented with exactly one target</li>
     * </ul>
     *
     * @param tts The transition targets
     * @return Whether this is a legal configuration
     */
    private static boolean verifyTransitionTargets(final List<TransitionTarget> tts) {
        if (tts.size() <= 1) { // No contention
            return true;
        }
        TransitionTarget lca = SCXMLHelper.getLCA(tts.get(0), tts.get(1));
        if (lca == null || !(lca instanceof Parallel)) {
            return false; // Must have a Parallel LCA
        }
        Parallel p = (Parallel) lca;
        Set<TransitionTarget> regions = new HashSet<TransitionTarget>();
        for (TransitionTarget tt : tts) {
            while (tt.getParent() != p) {
                tt = tt.getParent();
            }
            if (!regions.add(tt)) {
                return false; // One per region
            }
        }
        return regions.size() == p.getChildren().size();
    }
}