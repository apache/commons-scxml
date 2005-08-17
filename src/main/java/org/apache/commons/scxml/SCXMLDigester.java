/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
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

import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.ObjectCreateRule;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.SetNextRule;
import org.apache.commons.digester.SetPropertiesRule;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.scxml.env.URLResolver;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Assign;
import org.apache.commons.scxml.model.Cancel;
import org.apache.commons.scxml.model.Else;
import org.apache.commons.scxml.model.ElseIf;
import org.apache.commons.scxml.model.Executable;
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

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;

/**
 * The SCXMLDigester can be used to: <br>
 * a) Digest a SCXML file placed in a web application context <br>
 * b) Obtain a Digester instance configured with rules for SCXML digestion <br>
 * c) Serialize an SCXML object (primarily for debugging) <br>
 */
public class SCXMLDigester {

    private static final String ERR_PARSE_FAIL = "<!-- Error parsing " +
        "SCXML document for group: \"{0}\", with message: \"{1}\" -->\n";

    // Logging
    private static org.apache.commons.logging.Log log = LogFactory
            .getLog(SCXMLDigester.class);

    //-- PUBLIC METHODS --//
    /**
     * API for standalone usage where the SCXML document is a URL.
     * 
     * @param scxmlURL
     *            a canonical absolute URL to parse (relative URLs within the
     *            top level document are to be resovled against this URL).
     * @param errHandler
     *            The SAX ErrorHandler
     * @param evalCtx
     *            the document-level variable context for guard condition
     *            evaluation
     * @param evalEngine
     *            the scripting/expression language engine for creating local
     *            state-level variable contexts (if supported by a given
     *            scripting engine)
     *
     * @return SCXML The SCXML object corresponding to the file argument
     * 
     * @see Context
     * @see ErrorHandler
     * @see Evaluator
     * @see PathResolver
     */
    public static SCXML digest(URL scxmlURL, ErrorHandler errHandler,
            Context evalCtx, Evaluator evalEngine) {

        SCXML scxml = null;
        Digester scxmlDigester = SCXMLDigester
                .newInstance(null, new URLResolver(scxmlURL));
        scxmlDigester.setErrorHandler(errHandler);

        try {
            scxml = (SCXML) scxmlDigester.parse(scxmlURL.toString());
        } catch (Exception e) {
            MessageFormat msgFormat = new MessageFormat(ERR_PARSE_FAIL);
            String errMsg = msgFormat.format(new Object[] {scxmlURL.toString(),
                    e.getMessage()});
            log.error(errMsg, e);
        }

        if (scxml != null) {
            updateSCXML(scxml, evalCtx, evalEngine);
        }

        return scxml;

    }

    /**
     * API for standalone usage where the SCXML document is a URI.
     * A PathResolver must be provided.
     * 
     * @param pathResolver
     *            The PathResolver for this context
     * @param documentRealPath 
     *            The String pointing to the absolute (real) path of the
     *               SCXML config 
     * @param errHandler
     *            The SAX ErrorHandler
     * @param evalCtx
     *            the document-level variable context for guard condition
     *            evaluation
     * @param evalEngine
     *            the scripting/expression language engine for creating local
     *            state-level variable contexts (if supported by a given
     *            scripting engine)
     * 
     * @return SCXML The SCXML object corresponding to the file argument
     * 
     * @see Context
     * @see ErrorHandler
     * @see Evaluator
     * @see PathResolver
     */
    public static SCXML digest(String documentRealPath, 
            ErrorHandler errHandler, Context evalCtx, Evaluator evalEngine,
            PathResolver pr) {

        SCXML scxml = null;
        Digester scxmlDigester = SCXMLDigester.newInstance(null, pr);
        scxmlDigester.setErrorHandler(errHandler);

        try {
            scxml = (SCXML) scxmlDigester.parse(documentRealPath);
        } catch (Exception e) {
            MessageFormat msgFormat = new MessageFormat(ERR_PARSE_FAIL);
            String errMsg = msgFormat.format(new Object[] { documentRealPath,
                e.getMessage()});
            log.error(errMsg, e);
        }

        if (scxml != null) {
            updateSCXML(scxml, evalCtx, evalEngine);
        }

        return scxml;

    }

    /**
     * Serialize this SCXML object (primarily for debugging)
     * 
     * @param scxml
     *            The SCXML to be serialized
     * @return String The serialized SCXML
     */
    public static String serializeSCXML(SCXML scxml) {
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

    //-- PRIVATE CONSTANTS --//
    //// Patterns to get the digestion going
    private static final String XP_SM = "scxml";

    private static final String XP_SM_ST = "scxml/state";

    //// Universal matches
    // State
    private static final String XP_ST_ST = "!*/state/state";

    private static final String XP_PAR_ST = "!*/parallel/state";

    private static final String XP_TR_TAR_ST = "!*/transition/target/state";

    //private static final String XP_ST_TAR_ST = "!*/state/target/state";

    // Parallel
    private static final String XP_ST_PAR = "!*/state/parallel";

    // If
    private static final String XP_IF = "!*/if";

    //// Path Fragments
    // Onentries and Onexits
    private static final String XP_ONEN = "/onentry";

    private static final String XP_ONEX = "/onexit";

    // Initial
    private static final String XP_INI = "/initial";
    
    // History
    private static final String XP_HIST = "/history";

    // Transition, target and exit
    private static final String XP_TR = "/transition";

    private static final String XP_TAR = "/target";

    private static final String XP_ST = "/state";

    private static final String XP_EXT = "/exit";

    // Actions
    private static final String XP_VAR = "/var";

    private static final String XP_ASN = "/assign";

    private static final String XP_LOG = "/log";

    private static final String XP_SND = "/send";

    private static final String XP_CAN = "/cancel";

    private static final String XP_EIF = "/elseif";

    private static final String XP_ELS = "/else";

    //// Other constants
    private static final String INDENT = " ";

    //-- PRIVATE UTILITY METHODS --//
    /*
     * Get a SCXML digester instance
     * 
     * @return Digester A newly configured SCXML digester instance
     */
    private static Digester newInstance(SCXML scxml, PathResolver sc) {

        Digester digester = new Digester();
        //Uncomment next line after SCXML DTD is available
        //digester.setValidating(true);
        digester.setRules(initRules(scxml, sc));
        return digester;
    }

    /*
     * Private utility functions for configuring digester rule base for SCXML
     */
    private static ExtendedBaseRules initRules(SCXML scxml, PathResolver sc) {

        ExtendedBaseRules scxmlRules = new ExtendedBaseRules();

        //// SCXML
        scxmlRules.add(XP_SM, new ObjectCreateRule(SCXML.class));
        scxmlRules.add(XP_SM, new SetPropertiesRule());

        //// States
        // Level one states
        addStateRules(XP_SM_ST, scxmlRules, scxml, sc, 0);
        scxmlRules.add(XP_SM_ST, new SetNextRule("addState"));
        // Nested states
        addStateRules(XP_ST_ST, scxmlRules, scxml, sc, 1);
        scxmlRules.add(XP_ST_ST, new SetNextRule("addChild"));
        // Initial states (no longer needed due to addition of Initial)
        //addStateRules(XP_ST_TAR_ST, scxmlRules, scxml, sc, 1);
        //scxmlRules.add(XP_ST_TAR_ST, new SetNextRule("addChild"));
        //scxmlRules.add(XP_ST_TAR_ST, new SetNextRule("setInitial"));
        // Parallel states
        addStateRules(XP_PAR_ST, scxmlRules, scxml, sc, 1);
        scxmlRules.add(XP_PAR_ST, new SetNextRule("addState"));
        // Target states
        addStateRules(XP_TR_TAR_ST, scxmlRules, scxml, sc, 2);
        scxmlRules.add(XP_TR_TAR_ST, new SetNextRule("setTarget"));

        //// Parallels
        addParallelRules(XP_ST_PAR, scxmlRules, scxml);

        //// Ifs
        addIfRules(XP_IF, scxmlRules);

        return scxmlRules;

    }

    private static void addStateRules(String xp, ExtendedBaseRules scxmlRules,
            SCXML scxml, PathResolver sc, int parent) {
        scxmlRules.add(xp, new ObjectCreateRule(State.class));
        addStatePropertiesRules(xp, scxmlRules, sc);
        //scxmlRules.add(xp + XP_TAR, new SetPropertiesRule());
        //scxmlRules.add(xp + XP_INI_TR_TAR, new SetPropertiesRule());
        addInitialRule(xp + XP_INI, scxmlRules, sc, scxml);
        addHistoryRules(xp + XP_HIST, scxmlRules, sc, scxml);
        addParentRule(xp, scxmlRules, parent);
        addTransitionRules(xp + XP_TR, scxmlRules, "addTransition");
        addHandlerRules(xp, scxmlRules);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
    }

    private static void addParallelRules(String xp,
            ExtendedBaseRules scxmlRules, SCXML scxml) {
        addSimpleRulesTuple(xp, scxmlRules, Parallel.class, null, null,
                "setParallel");
        addHandlerRules(xp, scxmlRules);
        addParentRule(xp, scxmlRules, 1);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
    }

    private static void addStatePropertiesRules(String xp,
            ExtendedBaseRules scxmlRules, PathResolver sc) {
        scxmlRules.add(xp, new SetPropertiesRule(
                new String[] { "id", "final" },
                new String[] { "id", "isFinal" }));
        scxmlRules.add(xp, new DigestSrcAttributeRule(sc));
    }

    private static void addInitialRule(String xp,
            ExtendedBaseRules scxmlRules, PathResolver sc, SCXML scxml) {
        scxmlRules.add(xp, new ObjectCreateRule(Initial.class));
        addPseudoStatePropertiesRules(xp, scxmlRules, sc);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
        addTransitionRules(xp + XP_TR, scxmlRules, "setTransition");
        scxmlRules.add(xp, new SetNextRule("setInitial"));
    }
    
    private static void addHistoryRules(String xp,
            ExtendedBaseRules scxmlRules, PathResolver sc, SCXML scxml) {
        scxmlRules.add(xp, new ObjectCreateRule(History.class));
        addPseudoStatePropertiesRules(xp, scxmlRules, sc);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
        scxmlRules.add(xp, new SetPropertiesRule(
                new String[] { "type" }, new String[] { "type" }));
        addTransitionRules(xp + XP_TR, scxmlRules, "setTransition");
        scxmlRules.add(xp, new SetNextRule("addHistory"));
    }
    
    private static void addPseudoStatePropertiesRules(String xp,
            ExtendedBaseRules scxmlRules, PathResolver sc) {
        scxmlRules.add(xp, new SetPropertiesRule(
            new String[] { "id" }, new String[] { "id" }));
        scxmlRules.add(xp, new DigestSrcAttributeRule(sc));
        addParentRule(xp, scxmlRules, 1);
    }
    
    private static void addParentRule(String xp, ExtendedBaseRules scxmlRules,
            final int parent) {
        if (parent < 1) {
            return;
        }
        scxmlRules.add(xp, new Rule() {
            // A generic version of setTopRule
            public void body(String namespace, String name, String text)
                    throws Exception {
                TransitionTarget t = (TransitionTarget) getDigester().peek();
                TransitionTarget p = (TransitionTarget) getDigester().peek(
                        parent);
                // CHANGE - Moved parent property to TransitionTarget
                t.setParent(p);
            }
        });
    }

    private static void addTransitionRules(String xp,
            ExtendedBaseRules scxmlRules, String setNextMethod) {
        scxmlRules.add(xp, new ObjectCreateRule(Transition.class));
        scxmlRules.add(xp, new SetPropertiesRule());
        scxmlRules.add(xp + XP_TAR, new SetPropertiesRule());
        addActionRules(xp, scxmlRules);
        scxmlRules.add(xp + XP_EXT, new Rule() {
            public void end(String namespace, String name) {
                Transition t = (Transition) getDigester().peek(1);
                State exitState = new State();
                exitState.setIsFinal(true);
                t.setTarget(exitState);
            }
        });
        scxmlRules.add(xp, new SetNextRule(setNextMethod));
    }

    private static void addHandlerRules(String xp, ExtendedBaseRules scxmlRules) {
        scxmlRules.add(xp + XP_ONEN, new ObjectCreateRule(OnEntry.class));
        addActionRules(xp + XP_ONEN, scxmlRules);
        scxmlRules.add(xp + XP_ONEN, new SetNextRule("setOnEntry"));
        scxmlRules.add(xp + XP_ONEX, new ObjectCreateRule(OnExit.class));
        addActionRules(xp + XP_ONEX, scxmlRules);
        scxmlRules.add(xp + XP_ONEX, new SetNextRule("setOnExit"));
    }

    private static void addActionRules(String xp, ExtendedBaseRules scxmlRules) {
        addActionRulesTuple(xp + XP_ASN, scxmlRules, Assign.class);
        addActionRulesTuple(xp + XP_VAR, scxmlRules, Var.class);
        addActionRulesTuple(xp + XP_LOG, scxmlRules, Log.class);
        addActionRulesTuple(xp + XP_SND, scxmlRules, Send.class);
        addActionRulesTuple(xp + XP_CAN, scxmlRules, Cancel.class);
        addActionRulesTuple(xp + XP_EXT, scxmlRules, Exit.class);
    }

    private static void addIfRules(String xp, ExtendedBaseRules scxmlRules) {
        addActionRulesTuple(xp, scxmlRules, If.class);
        addActionRules(xp, scxmlRules);
        addActionRulesTuple(xp + XP_EIF, scxmlRules, ElseIf.class);
        addActionRulesTuple(xp + XP_ELS, scxmlRules, Else.class);
    }

    private static void addActionRulesTuple(String xp,
            ExtendedBaseRules scxmlRules, Class klass) {
        addSimpleRulesTuple(xp, scxmlRules, klass, null, null, "addAction");
        scxmlRules.add(xp, new SetExecutableParentRule());
    }

    private static void addSimpleRulesTuple(String xp,
            ExtendedBaseRules scxmlRules, Class klass, String[] args,
            String[] props, String addMethod) {
        scxmlRules.add(xp, new ObjectCreateRule(klass));
        if (args == null) {
            scxmlRules.add(xp, new SetPropertiesRule());
        } else {
            scxmlRules.add(xp, new SetPropertiesRule(args, props));
        }
        scxmlRules.add(xp, new SetNextRule(addMethod));
    }

    /*
     * Post-processing methods to make the SCXML object Executor-ready.
     */
    private static void updateSCXML(SCXML scxml, Context evalCtx,
            Evaluator evalEngine) {
        // Watch case, slightly unfortunate naming ;-)
        String initialstate = scxml.getInitialstate();
        //we have to use getTargets() here since the initialState can be
        //an indirect descendant
        //TODO: better type check, now ClassCastException happens for Parallel
        State initialState = (State) scxml.getTargets().get(initialstate);
        if (initialState == null) {
            // Where do we, where do we go?
            System.err.println("ERROR: SCXMLDigester - No SCXML child state "
                    + "with ID \"" + initialstate
                    + "\" found i.e. no initialstate" + " for SCXML ;-)");
        }
        scxml.setInitialState(initialState);
        scxml.setRootContext(evalCtx);
        Map targets = scxml.getTargets();
        Map states = scxml.getStates();
        Iterator i = states.keySet().iterator();
        while (i.hasNext()) {
            updateState((State) states.get(i.next()), targets, evalCtx,
                    evalEngine);
        }
    }

    private static void updateState(State s, Map targets, Context evalCtx,
            Evaluator evalEngine) {
        //setup local variable context
        Context localCtx = null;
        if (s.getParent() == null) {
            localCtx = evalEngine.newContext(evalCtx);
        } else {
            State parentState = null;
            if (s.getParent() instanceof Parallel) {
                parentState = (State) (s.getParent().getParent());
            } else {
                parentState = (State) (s.getParent());
            }
            localCtx = evalEngine.newContext(parentState.getContext());
        }
        s.setContext(localCtx);
        //ensure both onEntry and onExit have parent
        //TODO: add this rather as a Digester rule for OnEntry/OnExit
        s.getOnEntry().setParent(s);
        s.getOnExit().setParent(s);
        //ENDTODO
        //initialize next / inital
        Initial ini = s.getInitial();
        Map c = s.getChildren();
        if (!c.isEmpty()) {
            if (ini == null) {
                System.err.println("WARNING: SCXMLDigester - Initial "
                    + "null for " + (SCXMLHelper.isStringEmpty(s.getId()) ? 
                    "anonymous state" : s.getId()));
            }
            Transition initialTransition = ini.getTransition();
            updateTransition(initialTransition, targets);
            TransitionTarget initialState = initialTransition.getTarget();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialState == null || !SCXMLHelper.isDescendant(initialState, s)) {
                System.err.println("WARNING: SCXMLDigester - Initial state "
                    + "null or not descendant for " + (SCXMLHelper.
                    isStringEmpty(s.getId()) ? "anonymous state" : s.getId()));
            }
        }
        List histories = s.getHistory();
        Iterator histIter = histories.iterator();
        while (histIter.hasNext()) {
            History h = (History) histIter.next();
            Transition historyTransition = h.getTransition();
            updateTransition(historyTransition, targets);
            State historyState = (State)historyTransition.getTarget();
            if (historyState == null) {
                System.err.println("WARNING: SCXMLDigester - History state "
                    + "null " + (SCXMLHelper.isStringEmpty(s.getId()) ? 
                    "anonymous state" : s.getId()));
            }
            if (!h.isDeep()) {
                if (!c.containsValue(historyState)) {
                    System.err.println("WARNING: SCXMLDigester - History state "
                        + "for shallow history is not child for " + (SCXMLHelper.
                        isStringEmpty(s.getId()) ? "anonymous state" : s.getId()));                    
                }
            } else {
                if (!SCXMLHelper.isDescendant(historyState, s)) {
                    System.err.println("WARNING: SCXMLDigester - History state "
                        + "for deep history is not descendant for " + (SCXMLHelper.
                        isStringEmpty(s.getId()) ? "anonymous state" : s.getId()));
                }
            }
        }
        Map t = s.getTransitions();
        Iterator i = t.keySet().iterator();
        while (i.hasNext()) {
            Iterator j = ((List) t.get(i.next())).iterator();
            while (j.hasNext()) {
                Transition trn = (Transition) j.next();
                // TODO: add this rather as a Digester rule for Transition
                trn.setNotificationRegistry(s.getNotificationRegistry());
                trn.setParent(s);
                // ENDTODO
                updateTransition(trn, targets);
            }
        }
        Parallel p = s.getParallel();
        if (p != null) {
            updateParallel(p, targets, evalCtx, evalEngine);
        } else {
            Iterator j = c.keySet().iterator();
            while (j.hasNext()) {
                updateState((State) c.get(j.next()), targets, evalCtx,
                        evalEngine);
            }
        }
    }

    private static void updateParallel(Parallel p, Map targets,
            Context evalCtx, Evaluator evalEngine) {
        Iterator i = p.getStates().iterator();
        while (i.hasNext()) {
            updateState((State) i.next(), targets, evalCtx, evalEngine);
        }
    }

    private static void updateTransition(Transition t, Map targets) {
        String next = t.getNext();
        TransitionTarget tt = t.getTarget();
        if (tt == null) {
            tt = (TransitionTarget) targets.get(next);
            if (tt == null) {
                // TODO: Move Digester warnings to errors
                System.err.println("WARNING: SCXMLDigester - Transition "
                        + "target \"" + next + "\" not found");
            }
            t.setTarget(tt);
        }
    }

    /*
     * Private SCXML object serialization utility functions
     */
    private static void serializeState(StringBuffer b, State s, String indent) {
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
        serializeOnEntry(b, (TransitionTarget) s, indent + INDENT);
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
        serializeOnExit(b, (TransitionTarget) s, indent + INDENT);
        b.append(indent).append("</state>\n");
    }

    private static void serializeParallel(StringBuffer b, Parallel p,
            String indent) {
        b.append(indent).append("<parallel");
        serializeTransitionTargetAttributes(b, p);
        b.append(">\n");
        serializeOnEntry(b, (TransitionTarget) p, indent + INDENT);
        Set s = p.getStates();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            serializeState(b, (State) i.next(), indent + INDENT);
        }
        serializeOnExit(b, (TransitionTarget) p, indent + INDENT);
        b.append(indent).append("</parallel>\n");
    }
    
    private static void serializeInitial(StringBuffer b, Initial i,
            String indent) {
        b.append(indent).append("<initial");
        serializeTransitionTargetAttributes(b, i);
        b.append(">\n");
        serializeTransition(b, i.getTransition(), indent + INDENT);
        b.append(indent).append("</initial>\n");
    }
    
    private static void serializeHistory(StringBuffer b, List l,
            String indent) {
        if (l.size() > 0) {
            for (int i = 0; i < l.size(); i++) {
                History h = (History) l.get(i);
                b.append(indent).append("<history");
                serializeTransitionTargetAttributes(b, h);
                 if(h.isDeep()) {
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

    private static void serializeTransitionTargetAttributes(StringBuffer b,
            TransitionTarget t) {
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

    private static void serializeTransition(StringBuffer b, Transition t,
            String indent) {
        b.append(indent).append("<transition event=\"").append(t.getEvent())
                .append("\" cond=\"").append(t.getCond()).append("\">\n");
        boolean exit = serializeActions(b, t.getActions(), indent + INDENT);
        if (!exit) {
            serializeTarget(b, t, indent + INDENT);
        }
        b.append(indent).append("</transition>\n");
    }

    private static void serializeTarget(StringBuffer b, Transition t,
            String indent) {
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

    private static void serializeOnEntry(StringBuffer b, TransitionTarget t,
            String indent) {
        OnEntry e = t.getOnEntry();
        if (e != null && e.getActions().size() > 0) {
            b.append(indent).append("<onentry>\n");
            serializeActions(b, e.getActions(), indent + INDENT);
            b.append(indent).append("</onentry>\n");
        }
    }

    private static void serializeOnExit(StringBuffer b, TransitionTarget t,
            String indent) {
        OnExit x = t.getOnExit();
        if (x != null && x.getActions().size() > 0) {
            b.append(indent).append("<onexit>\n");
            serializeActions(b, x.getActions(), indent + INDENT);
            b.append(indent).append("</onexit>\n");
        }
    }

    private static boolean serializeActions(StringBuffer b, List l,
            String indent) {
        if (l == null) {
            return false;
        }
        boolean exit = false;
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Action a = (Action) i.next();
            // TODO - Serialize action attrs, bodies; Priority: Very low ;-)
            if (a instanceof Var) {
                Var v = (Var) a;
                b.append(indent).append("<var name=\"").append(v.getName())
                        .append("\" expr=\"").append(v.getExpr()).append(
                                "\"/>\n");
            } else if (a instanceof Assign) {
                Assign asn = (Assign) a;
                b.append(indent).append("<assign name=\"")
                        .append(asn.getName()).append("\" expr=\"").append(
                                asn.getExpr()).append("\"/>\n");
            } else if (a instanceof Send) {
                Send s = (Send) a;
                b.append(indent).append("<send/>\n");
            } else if (a instanceof Cancel) {
                Cancel c = (Cancel) a;
                b.append(indent).append("<cancel/>\n");
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
                If IF = (If) a;
                serializeIf(b, IF, indent);
            } else if (a instanceof Else) {
                Else el = (Else) a;
                b.append(indent).append("<else/>\n");
            } else if (a instanceof ElseIf) {
                ElseIf eif = (ElseIf) a;
                b.append(indent).append("<elseif cond=\"")
                        .append(eif.getCond()).append("\" />\n");
            }
        }
        return exit;
    }

    private static void serializeIf(StringBuffer b, If IF, String indent) {
        b.append(indent).append("<if cond=\"").append(IF.getCond()).append(
                "\">\n");
        serializeActions(b, IF.getActions(), indent + INDENT);
        b.append(indent).append("</if>\n");
    }

    /**
     * Custom digestion rule for establishing necessary associations within the
     * SCXML object, which include: <br>
     * 1) Updation of the SCXML object's global targets Map <br>
     * 2) Obtaining a handle to the SCXML object's NotificationRegistry <br>
     * 
     */
    public static class UpdateModelRule extends Rule {

        private SCXML scxml;

        public UpdateModelRule(SCXML scxml) {
            super();
            this.scxml = scxml;
        }

        public void end(String namespace, String name) {
            if (scxml == null) {
                scxml = (SCXML) getDigester()
                        .peek(getDigester().getCount() - 1);
            }
            NotificationRegistry notifReg = scxml.getNotificationRegistry();
            TransitionTarget tt = (TransitionTarget) getDigester().peek();
            scxml.addTarget(tt);
            tt.setNotificationRegistry(notifReg);
        }
    }

    /**
     * Custom digestion rule for setting Executable parent of Action elements
     * 
     */
    public static class SetExecutableParentRule extends Rule {

        public SetExecutableParentRule() {
            super();
        }

        public void end(String namespace, String name) {
            Action child = (Action) getDigester().peek();
            for (int i = 1; i < getDigester().getCount() - 1; i++) {
                Object ancestor = (Object) getDigester().peek(i);
                if (ancestor instanceof Executable) {
                    child.setParent((Executable) ancestor);
                    return;
                }
            }
        }
    }

    /**
     * Custom digestion rule for external sources, that is, the src attribute of
     * the &lt;state&gt; element
     * 
     */
    public static class DigestSrcAttributeRule extends Rule {

        private PathResolver ctx;

        public DigestSrcAttributeRule(PathResolver sc) {
            super();
            this.ctx = sc;
        }

        public void begin(String namespace, String name, Attributes attributes) {
            String src = attributes.getValue("src");
            if (SCXMLHelper.isStringEmpty(src)) {
                return;
            }
            Digester digester = getDigester();
            SCXML scxml = (SCXML) digester.peek(digester.getCount() - 1);
            // 1) Digest the external SCXML file
            Digester externalSrcDigester = newInstance(scxml, ctx.getResolver(src));
            SCXML externalSCXML = null;
            String path = ctx == null ? src : ctx.resolvePath(src);

            try {
                externalSCXML = (SCXML) externalSrcDigester.parse(path);
            } catch (Exception e) {
                log.error(null, e);
            }
            // 2) Adopt the children
            // TODO - Clarify spec; Priority: High
            if (externalSCXML == null) {
                return;
            }
            State s = (State) digester.peek();
            Transition t = new Transition();
            t.setNext(externalSCXML.getInitialstate());
            Initial ini = new Initial();
            ini.setTransition(t);
            s.setInitial(ini);
            Map children = externalSCXML.getStates();
            Object[] ids = children.keySet().toArray();
            for (int i = 0; i < ids.length; i++) {
                s.addChild((State) children.get(ids[i]));
            }
        }
    }
}
