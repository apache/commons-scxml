/*
 *
 *   Copyright 2005 The Apache Software Foundation.
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
package org.apache.commons.scxml.semantics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.NotificationRegistry;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.SCXMLSemantics;
import org.apache.commons.scxml.Step;
import org.apache.commons.scxml.TriggerEvent;
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
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.OnEntry;
import org.apache.commons.scxml.model.OnExit;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.Path;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.model.Var;

/**
 * <p>This class encapsulates a particular SCXML semantics, that is, a
 * particular semantic interpretation of Harel Statecharts, which aligns
 * mostly with W3C SCXML July 5 public draft (that is, UML 1.5). However,
 * certain aspects are taken from STATEMATE.</p>
 *
 * <p>Specific semantics can be created by subclassing this class.</p>
 */
public class SCXMLSemanticsImpl implements SCXMLSemantics {

    /**
     * SCXML Logger for the application.
     */
    protected static final org.apache.commons.logging.Log APP_LOG = LogFactory
            .getLog("scxml.app.log");

    /**
     * The TransitionTarget comparator.
     */
    private TransitionTargetComparator targetComparator =
        new TransitionTargetComparator();

    /**
     * @param input
     *            SCXML state machine
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     * @param errRep
     *            ErrorReporter callback
     */
    public SCXML normalizeStateMachine(final SCXML input,
            final ErrorReporter errRep) {
        //it is a no-op for now
        return input;
    }

    /**
     * @param input
     *            SCXML state machine [in]
     * @param states
     *            a set of States to populate [out]
     * @param entryList
     *            a list of States and Parallels to enter [out]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void determineInitialStates(final SCXML input, final Set states,
            final List entryList, final ErrorReporter errRep)
            throws ModelException {
        State tmp = input.getInitialState();
        if (tmp == null) {
            errRep.onError(ErrorReporter.NO_INITIAL,
                    "SCXML initialstate is missing!", input);
        } else {
            states.add(tmp);
            determineTargetStates(states, errRep);
            //set of ALL entered states (even if initialState is a jump-over)
            Set onEntry = SCXMLHelper.getAncestorClosure(states, null);
            // sort onEntry according state hierarchy
            Object[] oen = onEntry.toArray();
            onEntry.clear();
            Arrays.sort(oen, getTTComparator());
            // we need to impose reverse order for the onEntry list
            List entering = Arrays.asList(oen);
            Collections.reverse(entering);
            entryList.addAll(entering);

        }
    }

    /**
     * @param actions
     *            a list of actions to execute [in]
     * @param derivedEvents
     *            collection of internal events generated by the actions [out]
     * @param eval
     *            the expression evaluator - Evaluator instance
     * @param evtDispatcher
     *            the event dispatcher - EventDispatcher instance
     * @param errRep
     *            ErrorReporter callback [inout]
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem
     * @throws SCXMLExpressionException
     *             in case there is an problem evaluating an expression
     *             within the SCXML document
     * @see ErrorReporter
     * @see NotificationRegistry
     * @see EventDispatcher
     * @see Context
     * @see Evaluator
     */
    public void executeActionList(final List actions,
            final Collection derivedEvents, final Evaluator eval,
            final EventDispatcher evtDispatcher, final ErrorReporter errRep)
            throws ModelException, SCXMLExpressionException {
        // NOTE: "if" statement is a container - we may need to call this method
        // recursively and pass a sub-list of actions embedded in a particular
        // "if"
        for (Iterator i = actions.iterator(); i.hasNext();) {
            Action a = (Action) i.next();
            State parentState = a.getParentState();
            Context ctx = parentState.getContext();
            // NOTE: "elseif" and "else" do not appear here, since they are
            // always handled as a part of "if" as a container
            if (a instanceof Assign) {
                Assign asgn = (Assign) a;
                String varName = asgn.getName();
                if (!ctx.has(varName)) {
                    errRep.onError(ErrorReporter.UNDEFINED_VARIABLE, varName
                            + " = null", parentState);
                } else {
                    Object varObj = eval.eval(ctx, asgn.getExpr());
                    ctx.set(varName, varObj);
                    TriggerEvent ev = new TriggerEvent(varName + ".change",
                            TriggerEvent.CHANGE_EVENT);
                    derivedEvents.add(ev);
                }
            } else if (a instanceof Cancel) {
                Cancel cncl = (Cancel) a;
                evtDispatcher.cancel(cncl.getSendid());
            } else if (a instanceof Exit) {
                // Ignore; Exit instance holds other information that might
                // be needed, and is not transformed at parse time.
                //throw new ModelException("The <exit/> tag must be "
                //    + "transformed to an anonymous final state at "
                //    + "the parse time!");
                continue; //for checkstyle
            } else if (a instanceof If) {
                //determine elseif/else separators evaluate conditions
                //extract a sub-list of If's actions and invoke
                // executeActionList()
                List todoList = new LinkedList();
                If ifo = (If) a;
                List subAct = ifo.getActions();
                boolean cnd = eval.evalCond(ctx, ifo.getCond()).booleanValue();
                for (Iterator ifiter = subAct.iterator(); ifiter.hasNext();) {
                    Action aa = (Action) ifiter.next();
                    if (cnd && !(aa instanceof ElseIf)
                            && !(aa instanceof Else)) {
                        todoList.add(aa);
                    } else if (cnd
                            && (aa instanceof ElseIf || aa instanceof Else)) {
                        break;
                    } else if (aa instanceof ElseIf) {
                        cnd = eval.evalCond(ctx, ((ElseIf) aa).getCond())
                                .booleanValue();
                    } else if (aa instanceof Else) {
                        cnd = true;
                    } else {
                        //skip
                        continue; //for checkstyle
                    }
                }
                if (!todoList.isEmpty()) {
                    executeActionList(todoList, derivedEvents, eval,
                        evtDispatcher, errRep);
                }
                todoList.clear();
            } else if (a instanceof Log) {
                Log lg = (Log) a;
                Object exprRslt = eval.eval(ctx, lg.getExpr());
                APP_LOG.info(lg.getLabel() + ": " + String.valueOf(exprRslt));
            } else if (a instanceof Send) {
                Send snd = (Send) a;
                Object hints = null;
                if (!SCXMLHelper.isStringEmpty(snd.getHints())) {
                    hints = eval.eval(ctx, snd.getHints());
                }
                Map params = null;
                if (!SCXMLHelper.isStringEmpty(snd.getNamelist())) {
                    StringTokenizer tkn = new StringTokenizer(snd.
                        getNamelist());
                    params = new HashMap(tkn.countTokens());
                    while (tkn.hasMoreTokens()) {
                        String varName = tkn.nextToken();
                        Object varObj = ctx.get(varName);
                        if (varObj == null) {
                            //considered as a warning here
                            errRep.onError(ErrorReporter.UNDEFINED_VARIABLE,
                                    varName + " = null", parentState);
                        }
                        params.put(varName, varObj);
                    }
                }
                evtDispatcher.send(snd.getSendid(),
                        snd.getTarget(), snd.getTargettype(), snd.getEvent(),
                        params, hints, Long.parseLong(snd.getDelay()));
            } else if (a instanceof Var) {
                Var vr = (Var) a;
                String varName = vr.getName();
                Object varObj = eval.eval(ctx, vr.getExpr());
                ctx.setLocal(varName, varObj);
                TriggerEvent ev = new TriggerEvent(varName + ".change",
                        TriggerEvent.CHANGE_EVENT);
                derivedEvents.add(ev);
            } else {
                errRep.onError(ErrorReporter.UNKNOWN_ACTION,
                        "unsupported executable statement", a);
            }
        }
    }

    /**
     * Executes all OnExit/Transition/OnEntry transitional actions.
     *
     * @param step
     *            provides EntryList, TransitList, ExitList gets
     *            updated its AfterStatus/Events
     * @param stateMachine
     *            state machine - SCXML instance
     * @param eval
     *            the expression evaluator - Evaluator instance
     * @param evtDispatcher
     *            the event dispatcher - EventDispatcher instance
     * @param errRep
     *            error reporter
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void executeActions(final Step step, final SCXML stateMachine,
            final Evaluator eval, final EventDispatcher evtDispatcher,
            final ErrorReporter errRep) throws ModelException {
        NotificationRegistry nr = stateMachine.getNotificationRegistry();
        Collection internalEvents = step.getAfterStatus().getEvents();
        // ExecutePhaseActions / OnExit
        for (Iterator i = step.getExitList().iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            OnExit oe = tt.getOnExit();
            try {
                executeActionList(oe.getActions(), internalEvents, eval,
                    evtDispatcher, errRep);
            } catch (SCXMLExpressionException e) {
                errRep.onError(ErrorReporter.EXPRESSION_ERROR, e.getMessage(),
                        oe);
            }
            nr.fireOnExit(tt, tt);
            nr.fireOnExit(stateMachine, tt);
            TriggerEvent te = new TriggerEvent(tt.getId() + ".exit",
                    TriggerEvent.CHANGE_EVENT);
            internalEvents.add(te);
        }
        // ExecutePhaseActions / Transitions
        for (Iterator i = step.getTransitList().iterator(); i.hasNext();) {
            Transition t = (Transition) i.next();
            try {
                executeActionList(t.getActions(), internalEvents, eval,
                    evtDispatcher, errRep);
            } catch (SCXMLExpressionException e) {
                errRep.onError(ErrorReporter.EXPRESSION_ERROR,
                    e.getMessage(), t);
            }
            nr.fireOnTransition(t, t.getParent(), t.getRuntimeTarget(), t);
            nr.fireOnTransition(stateMachine, t.getParent(),
                t.getRuntimeTarget(), t);
        }
        // ExecutePhaseActions / OnEntry
        for (Iterator i = step.getEntryList().iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            OnEntry oe = tt.getOnEntry();
            try {
                executeActionList(oe.getActions(), internalEvents, eval,
                    evtDispatcher, errRep);
            } catch (SCXMLExpressionException e) {
                errRep.onError(ErrorReporter.EXPRESSION_ERROR, e.getMessage(),
                        oe);
            }
            nr.fireOnEntry(tt, tt);
            nr.fireOnEntry(stateMachine, tt);
            TriggerEvent te = new TriggerEvent(tt.getId() + ".entry",
                    TriggerEvent.CHANGE_EVENT);
            internalEvents.add(te);
            //3.2.1 and 3.4 (.done events)
            if (tt instanceof State) {
                State ts = (State) tt;
                if (ts.getIsFinal()) {
                    State parent = (State) ts.getParent();
                    String prefix = "";
                    if (parent != null) {
                        prefix = parent.getId();
                    }
                    te = new TriggerEvent(prefix + ".done",
                            TriggerEvent.CHANGE_EVENT);
                    internalEvents.add(te);
                    if (parent != null) {
                        parent.setDone(true);
                    }
                    if (parent != null && parent.isRegion()) {
                        //3.4 we got a region, which is finalized
                        //let's check its siblings too
                        Parallel p = (Parallel) parent.getParent();
                        int finCount = 0;
                        int pCount = p.getStates().size();
                        for (Iterator regions = p.getStates().iterator();
                                regions.hasNext();) {
                            State reg = (State) regions.next();
                            if (reg.isDone()) {
                                finCount++;
                            }
                        }
                        if (finCount == pCount) {
                            te = new TriggerEvent(p.getId() + ".done",
                                        TriggerEvent.CHANGE_EVENT);
                            internalEvents.add(te);
                            te = new TriggerEvent(p.getParent().getId()
                                + ".done", TriggerEvent.CHANGE_EVENT);
                            internalEvents.add(te);
                            //this is not in the specs, but is makes sense
                            p.getParentState().setDone(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param stateMachine
     *            a SM to traverse [in]
     * @param step
     *            with current status and list of transitions to populate
     *            [inout]
     * @param errRep
     *            ErrorReporter callback [inout]
     */
    public void enumerateReachableTransitions(final SCXML stateMachine,
            final Step step, final ErrorReporter errRep) {
        // prevents adding the same transition multiple times
        Set transSet = new HashSet();
        // prevents visiting the same state multiple times
        Set stateSet = new HashSet(step.getBeforeStatus().getStates());
        // breath-first search to-do list
        LinkedList todoList = new LinkedList(stateSet);
        while (!todoList.isEmpty()) {
            State st = (State) todoList.removeFirst();
            for (Iterator i = st.getTransitionsList().iterator();
                    i.hasNext();) {
                Transition t = (Transition) i.next();
                if (!transSet.contains(t)) {
                    transSet.add(t);
                    step.getTransitList().add(t);
                }
            }
            State parent = st.getParentState();
            if (parent != null && !stateSet.contains(parent)) {
                stateSet.add(parent);
                todoList.addLast(parent);
            }
        }
        transSet.clear();
        stateSet.clear();
        todoList.clear();
    }

    /**
     * @param step
     *            [inout]
     * @param evaluator
     *            guard condition evaluator
     * @param errRep
     *            ErrorReporter callback [inout]
     */
    public void filterTransitionsSet(final Step step,
            final Evaluator evaluator, final ErrorReporter errRep) {
        /*
         * - filter transition set by applying events
         * (step/beforeStatus/events + step/externalEvents) (local check)
         * - evaluating guard conditions for
         * each transition (local check) - transition precedence (bottom-up)
         * as defined by SCXML specs
         */
        Set allEvents = new HashSet(step.getBeforeStatus().getEvents()
                .size()
                + step.getExternalEvents().size());
        //for now, we only match against event names
        for (Iterator ei = step.getBeforeStatus().getEvents().iterator();
                ei.hasNext();) {
            TriggerEvent te = (TriggerEvent) ei.next();
            allEvents.add(te.getName());
        }
        for (Iterator ei = step.getExternalEvents().iterator();
                ei.hasNext();) {
            TriggerEvent te = (TriggerEvent) ei.next();
            allEvents.add(te.getName());
        }
        //remove list (filtered-out list)
        List removeList = new LinkedList();
        //iterate over non-filtered transition set
        for (Iterator iter = step.getTransitList().iterator();
                iter.hasNext();) {
            Transition t = (Transition) iter.next();
            // event check
            String event = t.getEvent();
            if (!eventMatch(event, allEvents)) {
                // t has a non-empty event which is not triggered
                removeList.add(t);
                continue; //makes no sense to eval guard cond.
            }
            // guard condition check
            Boolean rslt;
            String expr = t.getCond();
            if (SCXMLHelper.isStringEmpty(expr)) {
                rslt = Boolean.TRUE;
            } else {
                try {
                    rslt = evaluator.evalCond(((State) t.getParent())
                            .getContext(), t.getCond());
                } catch (SCXMLExpressionException e) {
                    rslt = Boolean.FALSE;
                    errRep.onError(ErrorReporter.EXPRESSION_ERROR, e
                            .getMessage(), t);
                }
            }
            if (!rslt.booleanValue()) {
                // guard condition has not passed
                removeList.add(t);
            }
        }
        // apply event + guard condition filter
        step.getTransitList().removeAll(removeList);
        // cleanup temporary structures
        allEvents.clear();
        removeList.clear();
        // optimization - global precedence potentially applies
        // only if there are multiple enabled transitions
        if (step.getTransitList().size() > 1) {
            // global transition precedence check
            Object[] trans = step.getTransitList().toArray();
            Set currentStates = step.getBeforeStatus().getStates();
            // non-determinism candidates
            Set nonDeterm = new HashSet();
            for (int i = 0; i < trans.length; i++) {
                Transition t = (Transition) trans[i];
                TransitionTarget tsrc = t.getParent();
                for (int j = i + 1; j < trans.length; j++) {
                    Transition t2 = (Transition) trans[j];
                    boolean conflict = SCXMLHelper.inConflict(t, t2,
                            currentStates);
                    if (conflict) {
                        //potentially conflicting transitions
                        TransitionTarget t2src = t2.getParent();
                        if (SCXMLHelper.isDescendant(t2src, tsrc)) {
                            //t2 takes precedence over t
                            removeList.add(t);
                            break; //it makes no sense to waste cycles with t
                        } else if (SCXMLHelper.isDescendant(tsrc, t2src)) {
                            //t takes precendence over t2
                            removeList.add(t2);
                        } else {
                            //add both to the non-determinism candidates
                            nonDeterm.add(t);
                            nonDeterm.add(t2);
                        }
                    }
                }
            }
            // check if all non-deterministic situations have been resolved
            nonDeterm.removeAll(removeList);
            if (nonDeterm.size() > 0) {
                errRep.onError(ErrorReporter.NON_DETERMINISTIC,
                    "Multiple conflicting transitions enabled.", nonDeterm);
            }
            // apply global transition filter
            step.getTransitList().removeAll(removeList);
            removeList.clear();
            nonDeterm.clear();
        }
    }

    /**
     * Populate the target set.
     * <ul>
     * <li>take targets of selected transitions</li>
     * <li>take exited regions into account and make sure every active
     * parallel region has all siblings active
     * [that is, explicitly visit or sibling regions in case of newly visited
     * (revisited) orthogonal states]</li>
     * </ul>
     * @param residual [in]
     * @param transitList [in]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @return Set The target set
     */
    public Set seedTargetSet(final Set residual, final List transitList,
            final ErrorReporter errRep) {
        Set seedSet = new HashSet();
        Set regions = new HashSet();
        for (Iterator i = transitList.iterator(); i.hasNext();) {
            Transition t = (Transition) i.next();
            //iterate over transitions and add target states
            if (t.getTarget() != null) {
                seedSet.add(t.getTarget());
            }
            //build a set of all entered regions
            Path p = t.getPath();
            if (p.isCrossRegion()) {
                List regs = p.getRegionsEntered();
                for (Iterator j = regs.iterator(); j.hasNext();) {
                    State region = (State) j.next();
                    regions.addAll(((Parallel) region.getParent()).
                        getStates());
                }
            }
        }
        //check whether all active regions have their siblings active too
        Set allStates = new HashSet(residual);
        allStates.addAll(seedSet);
        allStates = SCXMLHelper.getAncestorClosure(allStates, null);
        regions.removeAll(allStates);
        //iterate over inactive regions and visit them implicitly using initial
        for (Iterator i = regions.iterator(); i.hasNext();) {
            State reg = (State) i.next();
            seedSet.add(reg);
        }
        return seedSet;
    }

    /**
     * @param states
     *            a set seeded in previous step [inout]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @throws ModelException On illegal configuration
     * @see #seedTargetSet(Set, List, ErrorReporter)
     */
    public void determineTargetStates(final Set states,
            final ErrorReporter errRep) throws ModelException {
        LinkedList wrkSet = new LinkedList(states);
        // clear the seed-set - will be populated by leaf states
        states.clear();
        while (!wrkSet.isEmpty()) {
            TransitionTarget tt = (TransitionTarget) wrkSet.removeFirst();
            if (tt instanceof State) {
                State st = (State) tt;
                //state can either have parallel or substates w. initial
                //or it is a leaf state
                // NOTE: Digester has to verify this precondition!
                if (st.isSimple()) {
                    states.add(st); //leaf
                } else if (st.isOrthogonal()) {
                    wrkSet.addLast(st.getParallel()); //parallel
                } else {
                    // composite state
                    Initial ini = st.getInitial();
                    if (ini == null) {
                        errRep.onError(ErrorReporter.NO_INITIAL,
                            "Initial pseudostate is missing!", st);
                    } else {
                        // If we are here, transition target must be a State
                        // or History
                        Transition initialTransition = ini.getTransition();
                        if (initialTransition == null) {
                            errRep.onError(ErrorReporter.ILLEGAL_INITIAL,
                                "Initial transition is null!", st);
                        } else {
                            TransitionTarget init = initialTransition.
                                getTarget();
                            if (init == null
                                ||
                                !(init instanceof State
                                  || init instanceof History)) {
                                errRep.onError(ErrorReporter.ILLEGAL_INITIAL,
                                "Initial not pointing to a State or History!",
                                st);
                            } else {
                                wrkSet.addLast(init);
                            }
                        }
                    }
                }
            } else if (tt instanceof Parallel) {
                Parallel prl = (Parallel) tt;
                for (Iterator i = prl.getStates().iterator(); i.hasNext();) {
                    //fork
                    wrkSet.addLast(i.next());
                }
            } else if (tt instanceof History) {
                History h = (History) tt;
                if (h.isEmpty()) {
                    wrkSet.addLast(h.getTransition().getRuntimeTarget());
                } else {
                    wrkSet.addAll(h.getLastConfiguration());
                }
            } else {
                throw new ModelException("Unknown TransitionTarget subclass:"
                        + tt.getClass().getName());
            }
        }
    }

    /**
     * Go over the exit list and update history information for
     * relevant states.
     *
     * @param step
     *            [inout]
     * @param errRep
     *            ErrorReporter callback [inout]
     */
    public void updateHistoryStates(final Step step,
            final ErrorReporter errRep) {
        Set oldState = step.getBeforeStatus().getStates();
        for (Iterator i = step.getExitList().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof State) {
                State s = (State) o;
                if (s.hasHistory()) {
                    Set shallow = null;
                    Set deep = null;
                    for (Iterator j = s.getHistory().iterator();
                            j.hasNext();) {
                        History h = (History) j.next();
                        if (h.isDeep()) {
                            if (deep == null) {
                                //calculate deep history for a given state once
                                deep = new HashSet();
                                Iterator k = oldState.iterator();
                                while (k.hasNext()) {
                                    State os = (State) k.next();
                                    if (SCXMLHelper.isDescendant(os, s)) {
                                        deep.add(os);
                                    }
                                }
                            }
                            h.setLastConfiguration(deep);
                        } else {
                            if (shallow == null) {
                                //calculate shallow history for a given state
                                // once
                                shallow = new HashSet();
                                shallow.addAll(s.getChildren().values());
                                shallow.retainAll(SCXMLHelper
                                        .getAncestorClosure(oldState, null));
                            }
                            h.setLastConfiguration(shallow);
                        }
                    }
                    shallow = null;
                    deep = null;
                }
            }
        }
    }

    /**
     * Implements prefix match, that is, if, for example,
     * &quot;mouse.click&quot; is a member of eventOccurrences and a
     * transition is triggered by &quot;mouse&quot;, the method returns true.
     *
     * @param transEvent
     *            a trigger event of a transition
     * @param eventOccurrences
     *            current events
     * @return true/false
     */
    public boolean eventMatch(final String transEvent,
            final Set eventOccurrences) {
        if (SCXMLHelper.isStringEmpty(transEvent)) {
            return true;
        } else {
            String transEventDot = transEvent + "."; //wildcard (prefix) event
            // support
            Iterator i = eventOccurrences.iterator();
            while (i.hasNext()) {
                String evt = (String) i.next();
                if (evt == null || evt.equals(transEvent) //null for Standalone
                    || evt.startsWith(transEventDot)) {
                        return true;
                }
            }
            return false;
        }
    }

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param step The current Step
     * @param errorReporter The ErrorReporter for the current environment
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void followTransitions(final Step step,
            final ErrorReporter errorReporter) throws ModelException {
        Set currentStates = step.getBeforeStatus().getStates();
        List transitions = step.getTransitList();
        // DetermineExitedStates (currentStates, transitList) -> exitedStates
        Set exitedStates = new HashSet();
        for (Iterator i = transitions.iterator(); i.hasNext();) {
            Transition t = (Transition) i.next();
            Set ext = SCXMLHelper.getStatesExited(t, currentStates);
            exitedStates.addAll(ext);
        }
        // compute residual states - these are preserved from the previous step
        Set residual = new HashSet(currentStates);
        residual.removeAll(exitedStates);
        // SeedTargetSet (residual, transitList) -> seedSet
        Set seedSet = seedTargetSet(residual, transitions, errorReporter);
        // DetermineTargetStates (initialTargetSet) -> targetSet
        Set targetSet = step.getAfterStatus().getStates();
        targetSet.addAll(seedSet); //copy to preserve seedSet
        determineTargetStates(targetSet, errorReporter);
        // BuildOnEntryList (targetSet, seedSet) -> entryList
        Set entered = SCXMLHelper.getAncestorClosure(targetSet, seedSet);
        seedSet.clear();
        for (Iterator i = transitions.iterator(); i.hasNext();) {
            Transition t = (Transition) i.next();
            entered.addAll(t.getPath().getDownwardSegment());
        }
        // Chech whether the computed state config is legal
        targetSet.addAll(residual);
        residual.clear();
        if (!SCXMLHelper.isLegalConfig(targetSet, errorReporter)) {
            throw new ModelException("Illegal state machine configuration!");
        }
        // sort onEntry and onExit according state hierarchy
        Object[] oex = exitedStates.toArray();
        exitedStates.clear();
        Object[] oen = entered.toArray();
        entered.clear();
        Arrays.sort(oex, getTTComparator());
        Arrays.sort(oen, getTTComparator());
        step.getExitList().addAll(Arrays.asList(oex));
        // we need to impose reverse order for the onEntry list
        List entering = Arrays.asList(oen);
        Collections.reverse(entering);
        step.getEntryList().addAll(entering);
        // reset 'done' flag
        for (Iterator reset = entering.iterator(); reset.hasNext();) {
            Object o = reset.next();
            if (o instanceof State) {
                ((State) o).setDone(false);
            }
        }
    }

    /**
     * TransitionTargetComparator factory method.
     * @return Comparator The TransitionTarget comparator
     */
    protected Comparator getTTComparator() {
        return targetComparator;
    }

}

