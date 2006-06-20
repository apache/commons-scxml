/*
 *
 *   Copyright 2005-2006 The Apache Software Foundation.
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
package org.apache.commons.scxml.io;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.Initial;
import org.apache.commons.scxml.model.Invoke;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * The ModelUpdater provides the utility methods to check the Commons
 * SCXML model for inconsistencies, detect errors, and wire the Commons
 * SCXML model appropriately post document parsing by the digester to make
 * it executor ready.
 */
final class ModelUpdater {

    /*
     * Post-processing methods to make the SCXML object SCXMLExecutor ready.
     */
    /**
     * <p>Update the SCXML object model and make it SCXMLExecutor ready.
     * This is part of post-digester processing, and sets up the necessary
     * object references throughtout the SCXML object model for the parsed
     * document.</p>
     *
     * @param scxml The SCXML object (output from Digester)
     * @throws ModelException If the object model is flawed
     */
   static void updateSCXML(final SCXML scxml) throws ModelException {
       // Watch case, slightly unfortunate naming ;-)
       String initialstate = scxml.getInitialstate();
       //we have to use getTargets() here since the initialState can be
       //an indirect descendant
       // Concern marked by one of the code reviewers: better type check,
       //            now ClassCastException happens for Parallel
       // Response: initial should be a State, for Parallel, it is implicit
       State initialState = (State) scxml.getTargets().get(initialstate);
       if (initialState == null) {
           // Where do we, where do we go?
           logAndThrowModelError(ERR_SCXML_NO_INIT, new Object[] {
               initialstate });
       }
       scxml.setInitialState(initialState);
       Map targets = scxml.getTargets();
       Map states = scxml.getStates();
       Iterator i = states.keySet().iterator();
       while (i.hasNext()) {
           updateState((State) states.get(i.next()), targets);
       }
   }

    /**
      * Update this State object (part of post-digestion processing).
      * Also checks for any errors in the document.
      *
      * @param s The State object
      * @param targets The global Map of all transition targets
      * @throws ModelException If the object model is flawed
      */
    private static void updateState(final State s, final Map targets)
    throws ModelException {
        //ensure both onEntry and onExit have parent
        //could add next two lines as a Digester rule for OnEntry/OnExit
        s.getOnEntry().setParent(s);
        s.getOnExit().setParent(s);
        //initialize next / inital
        Initial ini = s.getInitial();
        Map c = s.getChildren();
        TransitionTarget initialState = null;
        if (!c.isEmpty()) {
            if (ini == null) {
                logAndThrowModelError(ERR_STATE_NO_INIT,
                    new Object[] {getStateName(s)});
            }
            Transition initialTransition = ini.getTransition();
            updateTransition(initialTransition, targets);
            initialState = initialTransition.getTarget();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialState == null
                    || !SCXMLHelper.isDescendant(initialState, s)) {
                logAndThrowModelError(ERR_STATE_BAD_INIT,
                    new Object[] {getStateName(s)});
            }
        }
        List histories = s.getHistory();
        Iterator histIter = histories.iterator();
        while (histIter.hasNext()) {
            if (s.isSimple()) {
                logAndThrowModelError(ERR_HISTORY_SIMPLE_STATE,
                    new Object[] {getStateName(s)});
            }
            History h = (History) histIter.next();
            Transition historyTransition = h.getTransition();
            if (historyTransition == null) {
                // try to assign initial as default
                if (initialState != null
                        && !(initialState instanceof History)) {
                    historyTransition = new Transition();
                    historyTransition.setNext(initialState.getId());
                    historyTransition.setParent(h);
                    h.setTransition(historyTransition);
                } else {
                    logAndThrowModelError(ERR_HISTORY_NO_DEFAULT,
                        new Object[] {h.getId(), getStateName(s)});
                }
            }
            updateTransition(historyTransition, targets);
            State historyState = (State) historyTransition.getTarget();
            if (historyState == null) {
                logAndThrowModelError(ERR_STATE_NO_HIST,
                    new Object[] {getStateName(s)});
            }
            if (!h.isDeep()) {
                if (!c.containsValue(historyState)) {
                    logAndThrowModelError(ERR_STATE_BAD_SHALLOW_HIST,
                        new Object[] {getStateName(s)});
                }
            } else {
                if (!SCXMLHelper.isDescendant(historyState, s)) {
                    logAndThrowModelError(ERR_STATE_BAD_DEEP_HIST,
                        new Object[] {getStateName(s)});
                }
            }
        }
        Map t = s.getTransitions();
        Iterator i = t.keySet().iterator();
        while (i.hasNext()) {
            Iterator j = ((List) t.get(i.next())).iterator();
            while (j.hasNext()) {
                Transition trn = (Transition) j.next();
                //could add next two lines as a Digester rule for Transition
                trn.setParent(s);
                updateTransition(trn, targets);
            }
        }
        Parallel p = s.getParallel();
        Invoke inv = s.getInvoke();
        if ((inv != null && p != null)
                || (inv != null && !c.isEmpty())
                || (p != null && !c.isEmpty())) {
            logAndThrowModelError(ERR_STATE_BAD_CONTENTS,
                new Object[] {getStateName(s)});
        }
        if (p != null) {
            updateParallel(p, targets);
        } else if (inv != null) {
            String ttype = inv.getTargettype();
            if (ttype == null || ttype.trim().length() == 0) {
                logAndThrowModelError(ERR_INVOKE_NO_TARGETTYPE,
                    new Object[] {getStateName(s)});
            }
            String src = inv.getSrc();
            boolean noSrc = (src == null || src.trim().length() == 0);
            String srcexpr = inv.getSrcexpr();
            boolean noSrcexpr = (srcexpr == null
                                 || srcexpr.trim().length() == 0);
            if (noSrc && noSrcexpr) {
                logAndThrowModelError(ERR_INVOKE_NO_SRC,
                    new Object[] {getStateName(s)});
            }
            if (!noSrc && !noSrcexpr) {
                logAndThrowModelError(ERR_INVOKE_AMBIGUOUS_SRC,
                    new Object[] {getStateName(s)});
            }
        } else {
            Iterator j = c.keySet().iterator();
            while (j.hasNext()) {
                updateState((State) c.get(j.next()), targets);
            }
        }
    }

    /**
      * Update this Parallel object (part of post-digestion processing).
      *
      * @param p The Parallel object
      * @param targets The global Map of all transition targets
      * @throws ModelException If the object model is flawed
      */
    private static void updateParallel(final Parallel p, final Map targets)
    throws ModelException {
        Iterator i = p.getStates().iterator();
        while (i.hasNext()) {
            updateState((State) i.next(), targets);
        }
    }

    /**
      * Update this Transition object (part of post-digestion processing).
      *
      * @param t The Transition object
      * @param targets The global Map of all transition targets
      * @throws ModelException If the object model is flawed
      */
    private static void updateTransition(final Transition t,
            final Map targets) throws ModelException {
        String next = t.getNext();
        TransitionTarget tt = t.getTarget();
        if (tt == null) {
            tt = (TransitionTarget) targets.get(next);
            if (tt == null) {
                logAndThrowModelError(ERR_TARGET_NOT_FOUND, new Object[] {
                    next });
            }
            t.setTarget(tt);
        }
    }

    /**
      * Log an error discovered in post-digestion processing.
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
     * Get state identifier for error message. This method is only
     * called to produce an appropriate log message in some error
     * conditions.
     *
     * @param state The <code>State</code> object
     * @return The state identifier for the error message
     */
    private static String getStateName(final State state) {
        String badState = "anonymous state";
        if (!SCXMLHelper.isStringEmpty(state.getId())) {
            badState = "state with ID \"" + state.getId() + "\"";
        }
        return badState;
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private ModelUpdater() {
        super();
    }

    //// Error messages
    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_SCXML_NO_INIT = "No SCXML child state "
        + "with ID \"{0}\" found; illegal initialstate for SCXML document";

    /**
     * Error message when a state element specifies an initial state which
     * cannot be found.
     */
    private static final String ERR_STATE_NO_INIT = "No initial element "
        + "available for {0}";

    /**
     * Error message when a state element specifies an initial state which
     * is not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT = "Initial state "
        + "null or not a descendant of {0}";

    /**
     * Error message when a state element contains anything other than
     * one &lt;parallel&gt;, one &lt;invoke&gt; or any number of
     * &lt;state&gt; children.
     */
    private static final String ERR_STATE_BAD_CONTENTS = "{0} should "
        + "contain either one <parallel>, one <invoke> or any number of "
        + "<state> children.";

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
     * Error message when an &lt;invoke&gt; does not specify a "targettype"
     * attribute.
     */
    private static final String ERR_INVOKE_NO_TARGETTYPE = "{0} contains "
        + "<invoke> with no \"targettype\" attribute specified.";

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

}

