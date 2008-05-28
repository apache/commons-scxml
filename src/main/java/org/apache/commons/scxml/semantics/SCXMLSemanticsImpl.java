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
package org.apache.commons.scxml.semantics;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.NotificationRegistry;
import org.apache.commons.scxml.PathResolver;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.SCXMLSemantics;
import org.apache.commons.scxml.Step;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.invoke.InvokerException;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Finalize;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.Invoke;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.OnEntry;
import org.apache.commons.scxml.model.OnExit;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.Param;
import org.apache.commons.scxml.model.Path;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * <p>This class encapsulates a particular SCXML semantics, that is, a
 * particular semantic interpretation of Harel Statecharts, which aligns
 * mostly with W3C SCXML July 5 public draft (that is, UML 1.5). However,
 * certain aspects are taken from STATEMATE.</p>
 *
 * <p>Specific semantics can be created by subclassing this class.</p>
 */
public class SCXMLSemanticsImpl implements SCXMLSemantics, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * SCXML Logger for the application.
     */
    private Log appLog = LogFactory.getLog("scxml.app.log");

    /**
     * The TransitionTarget comparator.
     */
    private TransitionTargetComparator<TransitionTarget> targetComparator =
        new TransitionTargetComparator<TransitionTarget>();

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";

    /**
     * Zero-length array of {@link TransitionTarget}s.
     */
    private static final TransitionTarget[] TT_ARR0 = new TransitionTarget[0];

    /**
     * Zero-length array of {@link Transition}s.
     */
    private static final Transition[] TR_ARR0 = new Transition[0];

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
     * @param targets
     *            a set of initial targets to populate [out]
     * @param entryList
     *            a list of States and Parallels to enter [out]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @param scInstance
     *            The state chart instance [in]
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void determineInitialStates(final SCXML input, final Set<TransitionTarget> targets,
            final List<TransitionTarget> entryList, final ErrorReporter errRep,
            final SCInstance scInstance)
            throws ModelException {
        TransitionTarget tmp = input.getInitialTarget();
        if (tmp == null) {
            errRep.onError(ErrorConstants.NO_INITIAL,
                    "SCXML initialstate is missing!", input);
        } else {
            targets.add(tmp);
            determineTargetStates(targets, errRep, scInstance);
            //set of ALL entered states (even if initialState is a jump-over)
            Set<TransitionTarget> onEntry = SCXMLHelper.getAncestorClosure(targets, null);
            // sort onEntry according state hierarchy
            TransitionTarget[] oen = (TransitionTarget[]) onEntry.toArray(TT_ARR0);
            onEntry.clear();
            Arrays.sort(oen, getTTComparator());
            // we need to impose reverse order for the onEntry list
            List<TransitionTarget> entering = Arrays.asList(oen);
            Collections.reverse(entering);
            entryList.addAll(entering);

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
     * @param evtDispatcher
     *            the event dispatcher - EventDispatcher instance
     * @param errRep
     *            error reporter
     * @param scInstance
     *            The state chart instance
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void executeActions(final Step step, final SCXML stateMachine,
            final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance)
    throws ModelException {
        NotificationRegistry nr = scInstance.getNotificationRegistry();
        Collection<TriggerEvent> internalEvents = step.getAfterStatus().getEvents();
        Map<TransitionTarget, Invoker> invokers = scInstance.getInvokers();
        // ExecutePhaseActions / OnExit
        for (TransitionTarget tt : step.getExitList()) {
            OnExit oe = tt.getOnExit();
            try {
                for (Action onExitAct : oe.getActions()) {
                    onExitAct.execute(evtDispatcher,
                        errRep, scInstance, appLog, internalEvents);
                }
            } catch (SCXMLExpressionException e) {
                errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(),
                        oe);
            }
            // check if invoke is active in this state
            if (invokers.containsKey(tt)) {
                Invoker toCancel = invokers.get(tt);
                try {
                    toCancel.cancel();
                } catch (InvokerException ie) {
                    TriggerEvent te = new TriggerEvent(tt.getId()
                        + ".invoke.cancel.failed", TriggerEvent.ERROR_EVENT);
                    internalEvents.add(te);
                }
                // done here, don't wait for cancel response
                invokers.remove(tt);
            }
            nr.fireOnExit(tt, tt);
            nr.fireOnExit(stateMachine, tt);
            TriggerEvent te = new TriggerEvent(tt.getId() + ".exit",
                    TriggerEvent.CHANGE_EVENT);
            internalEvents.add(te);
        }
        // ExecutePhaseActions / Transitions
        for (Transition t : step.getTransitList()) {
            try {
                for (Action transitAct : t.getActions()) {
                    transitAct.execute(evtDispatcher,
                        errRep, scInstance, appLog, internalEvents);
                }
            } catch (SCXMLExpressionException e) {
                errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                    e.getMessage(), t);
            }
            List<TransitionTarget> rtargets = t.getRuntimeTargets();
            for (TransitionTarget tt : rtargets) {
                nr.fireOnTransition(t, t.getParent(), tt, t);
                nr.fireOnTransition(stateMachine, t.getParent(), tt, t);
            }
        }
        // ExecutePhaseActions / OnEntry
        for (TransitionTarget tt : step.getEntryList()) {
            OnEntry oe = tt.getOnEntry();
            try {
                for (Action onEntryAct : oe.getActions()) {
                    onEntryAct.execute(evtDispatcher,
                        errRep, scInstance, appLog, internalEvents);
                }
            } catch (SCXMLExpressionException e) {
                errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(),
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
                if (ts.isFinal()) {
                    State parent = (State) ts.getParent();
                    String prefix = "";
                    if (parent != null) {
                        prefix = parent.getId();
                    }
                    te = new TriggerEvent(prefix + ".done",
                            TriggerEvent.CHANGE_EVENT);
                    internalEvents.add(te);
                    if (parent != null) {
                        scInstance.setDone(parent, true);
                    }
                    if (parent != null && parent.isRegion()) {
                        //3.4 we got a region, which is finalized
                        //let's check its siblings too
                        Parallel p = (Parallel) parent.getParent();
                        int finCount = 0;
                        int pCount = p.getChildren().size();
                        for (TransitionTarget ttreg : p.getChildren()) {
                            State reg = (State) ttreg;
                            if (scInstance.isDone(reg)) {
                                finCount++;
                            }
                        }
                        if (finCount == pCount) {
                            te = new TriggerEvent(p.getId() + ".done",
                                        TriggerEvent.CHANGE_EVENT);
                            internalEvents.add(te);
                            //this is not in the specs, but is makes sense
                            scInstance.setDone(p, true);
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
        Set<Transition> transSet = new HashSet<Transition>();
        // prevents visiting the same state multiple times
        Set<TransitionTarget> stateSet = new HashSet<TransitionTarget>(step.getBeforeStatus().getStates());
        // breath-first search to-do list
        LinkedList<TransitionTarget> todoList = new LinkedList<TransitionTarget>(stateSet);
        while (!todoList.isEmpty()) {
            TransitionTarget tt = todoList.removeFirst();
            for (Transition t : tt.getTransitionsList()) {
                if (!transSet.contains(t)) {
                    transSet.add(t);
                    step.getTransitList().add(t);
                }
            }
            TransitionTarget parent = tt.getParent();
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
     * @param evtDispatcher
     *            The {@link EventDispatcher} [in]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @param scInstance
     *            The state chart instance [in]
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void filterTransitionsSet(final Step step,
            final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance)
    throws ModelException {
        /*
         * - filter transition set by applying events
         * (step/beforeStatus/events + step/externalEvents) (local check)
         * - evaluating guard conditions for
         * each transition (local check) - transition precedence (bottom-up)
         * as defined by SCXML specs
         */
        Set<TriggerEvent> allEvents = new HashSet<TriggerEvent>(step.getBeforeStatus().getEvents().size()
            + step.getExternalEvents().size());
        allEvents.addAll(step.getBeforeStatus().getEvents());
        allEvents.addAll(step.getExternalEvents());
        // Finalize invokes, if applicable
        for (TransitionTarget tt : scInstance.getInvokers().keySet()) {
            State s = (State) tt;
            if (finalizeMatch(s.getId(), allEvents)) {
                Finalize fn = s.getInvoke().getFinalize();
                if (fn != null) {
                    try {
                        for (Action fnAct : fn.getActions()) {
                            fnAct.execute(evtDispatcher, errRep, scInstance, appLog,
                                step.getAfterStatus().getEvents());
                        }
                    } catch (SCXMLExpressionException e) {
                        errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                            e.getMessage(), fn);
                    }
                }
            }
        }
        //remove list (filtered-out list)
        List<Transition> removeList = new LinkedList<Transition>();
        //iterate over non-filtered transition set
        for (Transition t : step.getTransitList()) {
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
                    Context ctx = scInstance.getContext(t.getParent());
                    ctx.setLocal(NAMESPACES_KEY, t.getNamespaces());
                    rslt = scInstance.getEvaluator().evalCond(ctx,
                        t.getCond());
                    ctx.setLocal(NAMESPACES_KEY, null);
                } catch (SCXMLExpressionException e) {
                    rslt = Boolean.FALSE;
                    errRep.onError(ErrorConstants.EXPRESSION_ERROR, e
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
            Transition[] trans = (Transition[]) step.getTransitList().toArray(TR_ARR0);
            // non-determinism candidates
            Set<Transition> nonDeterm = new LinkedHashSet<Transition>();
            for (int i = 0; i < trans.length; i++) {
                Transition t = trans[i];
                TransitionTarget tsrc = t.getParent();
                for (int j = i + 1; j < trans.length; j++) {
                    Transition t2 = trans[j];
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
            // check if all non-deterministic situations have been resolved
            nonDeterm.removeAll(removeList);
            if (nonDeterm.size() > 0) {
                // if not, first one in each state / region (which is also
                // first in document order) wins
                Set<TransitionTarget> regions = new HashSet<TransitionTarget>();
                for (Transition t : nonDeterm) {
                    TransitionTarget parent = t.getParent();
                    if (regions.contains(parent)) {
                        removeList.add(t);
                    } else {
                        regions.add(parent);
                    }
                }
            }
            // apply global and document order transition filter
            step.getTransitList().removeAll(removeList);
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
    public Set<TransitionTarget> seedTargetSet(final Set<TransitionTarget> residual,
            final List<Transition> transitList, final ErrorReporter errRep) {
        Set<TransitionTarget> seedSet = new HashSet<TransitionTarget>();
        Set<TransitionTarget> regions = new HashSet<TransitionTarget>();
        for (Transition t : transitList) {
            //iterate over transitions and add target states
            if (t.getTargets().size() > 0) {
                seedSet.addAll(t.getTargets());
            }
            //build a set of all entered regions
            for (Path p : t.getPaths()) {
                if (p.isCrossRegion()) {
                    List<State> regs = p.getRegionsEntered();
                    for (State region : regs) {
                        regions.addAll(((Parallel) region.getParent()).
                            getChildren());
                    }
                }
            }
        }
        //check whether all active regions have their siblings active too
        Set<TransitionTarget> allStates = new HashSet<TransitionTarget>(residual);
        allStates.addAll(seedSet);
        allStates = SCXMLHelper.getAncestorClosure(allStates, null);
        regions.removeAll(allStates);
        //iterate over inactive regions and visit them implicitly using initial
        for (TransitionTarget tt : regions) {
            State reg = (State) tt;
            seedSet.add(reg);
        }
        return seedSet;
    }

    /**
     * @param states
     *            a set seeded in previous step [inout]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @param scInstance
     *            The state chart instance [in]
     * @throws ModelException On illegal configuration
     * @see #seedTargetSet(Set, List, ErrorReporter)
     */
    public void determineTargetStates(final Set<TransitionTarget> states,
            final ErrorReporter errRep, final SCInstance scInstance)
    throws ModelException {
        LinkedList<TransitionTarget> wrkSet = new LinkedList<TransitionTarget>(states);
        // clear the seed-set - will be populated by leaf states
        states.clear();
        while (!wrkSet.isEmpty()) {
            TransitionTarget tt = wrkSet.removeFirst();
            if (tt instanceof State) {
                State st = (State) tt;
                //state can either have parallel or substates w. initial
                //or it is a leaf state
                // NOTE: Digester has to verify this precondition!
                if (st.isSimple()) {
                    states.add(st); //leaf
                } else if (st.isOrthogonal()) { //TODO: Remove else if in v1.0
                    wrkSet.addLast(st.getParallel()); //parallel
                } else {
                    // composite state
                    List<TransitionTarget> initialStates = st.getInitial().getTransition().
                        getTargets();
                    wrkSet.addAll(initialStates);
                }
            } else if (tt instanceof Parallel) {
                Parallel prl = (Parallel) tt;
                for (TransitionTarget kid : prl.getChildren()) {
                    //fork
                    wrkSet.addLast(kid);
                }
            } else if (tt instanceof History) {
                History h = (History) tt;
                if (scInstance.isEmpty(h)) {
                    wrkSet.addAll(h.getTransition().getRuntimeTargets());
                } else {
                    wrkSet.addAll(scInstance.getLastConfiguration(h));
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
     * @param scInstance
     *            The state chart instance [inout]
     */
    public void updateHistoryStates(final Step step,
            final ErrorReporter errRep, final SCInstance scInstance) {
        Set<TransitionTarget> oldStates = step.getBeforeStatus().getStates();
        for (TransitionTarget tt : step.getExitList()) {
            if (tt instanceof State) {
                State s = (State) tt;
                if (s.hasHistory()) {
                    Set<TransitionTarget> shallow = null;
                    Set<TransitionTarget> deep = null;
                    for (History h : s.getHistory()) {
                        if (h.isDeep()) {
                            if (deep == null) {
                                //calculate deep history for a given state once
                                deep = new HashSet<TransitionTarget>();
                                for (TransitionTarget ott : oldStates) {
                                    State os = (State) ott;
                                    if (SCXMLHelper.isDescendant(os, s)) {
                                        deep.add(os);
                                    }
                                }
                            }
                            scInstance.setLastConfiguration(h, deep);
                        } else {
                            if (shallow == null) {
                                //calculate shallow history for a given state
                                // once
                                shallow = new HashSet<TransitionTarget>();
                                shallow.addAll(s.getChildren().values());
                                shallow.retainAll(SCXMLHelper
                                        .getAncestorClosure(oldStates, null));
                            }
                            scInstance.setLastConfiguration(h, shallow);
                        }
                    }
                    shallow = null;
                    deep = null;
                }
            }
        }
    }

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param step The current Step
     * @param errorReporter The ErrorReporter for the current environment
     * @param scInstance The state chart instance
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void followTransitions(final Step step,
            final ErrorReporter errorReporter, final SCInstance scInstance)
    throws ModelException {
        Set<TransitionTarget> currentStates = step.getBeforeStatus().getStates();
        List<Transition> transitions = step.getTransitList();
        // DetermineExitedStates (currentStates, transitList) -> exitedStates
        Set<TransitionTarget> exitedStates = new HashSet<TransitionTarget>();
        for (Transition t : transitions) {
            Set<TransitionTarget> ext = SCXMLHelper.getStatesExited(t, currentStates);
            exitedStates.addAll(ext);
        }
        // compute residual states - these are preserved from the previous step
        Set<TransitionTarget> residual = new HashSet<TransitionTarget>(currentStates);
        residual.removeAll(exitedStates);
        // SeedTargetSet (residual, transitList) -> seedSet
        Set<TransitionTarget> seedSet = seedTargetSet(residual, transitions, errorReporter);
        // DetermineTargetStates (initialTargetSet) -> targetSet
        Set<TransitionTarget> targetSet = step.getAfterStatus().getStates();
        targetSet.addAll(seedSet); //copy to preserve seedSet
        determineTargetStates(targetSet, errorReporter, scInstance);
        // BuildOnEntryList (targetSet, seedSet) -> entryList
        Set<TransitionTarget> entered = SCXMLHelper.getAncestorClosure(targetSet, seedSet);
        seedSet.clear();
        for (Transition t : transitions) {
            List<Path> paths = t.getPaths();
            for (Path p : paths) {
                entered.addAll(p.getDownwardSegment());
            }
            // If target is a History pseudo state, remove from entered list
            List<TransitionTarget> rtargets = t.getRuntimeTargets();
            for (TransitionTarget tt : rtargets) {
                if (tt instanceof History) {
                    entered.remove(tt);
                }
            }
        }
        // Check whether the computed state config is legal
        targetSet.addAll(residual);
        residual.clear();
        if (!SCXMLHelper.isLegalConfig(targetSet, errorReporter)) {
            throw new ModelException("Illegal state machine configuration!");
        }
        // sort onEntry and onExit according state hierarchy
        TransitionTarget[] oex = (TransitionTarget[]) exitedStates.toArray(TT_ARR0);
        exitedStates.clear();
        TransitionTarget[] oen = (TransitionTarget[]) entered.toArray(TT_ARR0);
        entered.clear();
        Arrays.sort(oex, getTTComparator());
        Arrays.sort(oen, getTTComparator());
        step.getExitList().addAll(Arrays.asList(oex));
        // we need to impose reverse order for the onEntry list
        List<TransitionTarget> entering = Arrays.asList(oen);
        Collections.reverse(entering);
        step.getEntryList().addAll(entering);
        // reset 'done' flag
        for (TransitionTarget tt : entering) {
            if (tt instanceof State) {
                scInstance.setDone(tt, false);
            }
        }
    }
    /**
     * Process any existing invokes, includes forwarding external events,
     * and executing any finalize handlers.
     *
     * @param events
     *            The events to be forwarded
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    public void processInvokes(final TriggerEvent[] events,
            final ErrorReporter errRep, final SCInstance scInstance)
    throws ModelException {
        Set<TriggerEvent> allEvents = new HashSet<TriggerEvent>();
        allEvents.addAll(Arrays.asList(events));
        for (Map.Entry<TransitionTarget, Invoker> iEntry : scInstance.getInvokers().entrySet()) {
            String parentId = iEntry.getKey().getId();
            if (!finalizeMatch(parentId, allEvents)) { // prevent cycles
                Invoker inv = iEntry.getValue();
                try {
                    inv.parentEvents(events);
                } catch (InvokerException ie) {
                    appLog.error(ie.getMessage(), ie);
                    throw new ModelException(ie.getMessage(), ie.getCause());
                }
            }
        }
    }

    /**
     * Initiate any new invokes.
     *
     * @param step
     *            The current Step
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     */
    public void initiateInvokes(final Step step, final ErrorReporter errRep,
            final SCInstance scInstance) {
        Evaluator eval = scInstance.getEvaluator();
        Collection<TriggerEvent> internalEvents = step.getAfterStatus().getEvents();
        for (TransitionTarget tt : step.getAfterStatus().getStates()) {
            State s = (State) tt;
            Context ctx = scInstance.getContext(s);
            Invoke i = s.getInvoke();
            if (i != null && scInstance.getInvoker(s) == null) {
                String src = i.getSrc();
                if (src == null) {
                    String srcexpr = i.getSrcexpr();
                    Object srcObj = null;
                    try {
                        ctx.setLocal(NAMESPACES_KEY, i.getNamespaces());
                        srcObj = eval.eval(ctx, srcexpr);
                        ctx.setLocal(NAMESPACES_KEY, null);
                        src = String.valueOf(srcObj);
                    } catch (SCXMLExpressionException see) {
                        errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                            see.getMessage(), i);
                    }
                }
                String source = src;
                PathResolver pr = i.getPathResolver();
                if (pr != null) {
                    source = i.getPathResolver().resolvePath(src);
                }
                String ttype = i.getTargettype();
                Invoker inv = null;
                try {
                    inv = scInstance.newInvoker(ttype);
                } catch (InvokerException ie) {
                    TriggerEvent te = new TriggerEvent(s.getId()
                        + ".invoke.failed", TriggerEvent.ERROR_EVENT);
                    internalEvents.add(te);
                    continue;
                }
                inv.setParentStateId(s.getId());
                inv.setSCInstance(scInstance);
                List<Param> params = i.params();
                Map<String, Object> args = new HashMap<String, Object>();
                for (Param p : params) {
                    String argExpr = p.getExpr();
                    Object argValue = null;
                    if (argExpr != null && argExpr.trim().length() > 0) {
                        try {
                            ctx.setLocal(NAMESPACES_KEY, p.getNamespaces());
                            argValue = eval.eval(ctx, argExpr);
                            ctx.setLocal(NAMESPACES_KEY, null);
                        } catch (SCXMLExpressionException see) {
                            errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                                see.getMessage(), i);
                        }
                    }
                    args.put(p.getName(), argValue);
                }
                try {
                    inv.invoke(source, args);
                } catch (InvokerException ie) {
                    TriggerEvent te = new TriggerEvent(s.getId()
                        + ".invoke.failed", TriggerEvent.ERROR_EVENT);
                    internalEvents.add(te);
                    continue;
                }
                scInstance.setInvoker(s, inv);
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
    protected boolean eventMatch(final String transEvent,
            final Set<TriggerEvent> eventOccurrences) {
        if (SCXMLHelper.isStringEmpty(transEvent)) { // Eventless transition
            return true;
        } else {
            String trimTransEvent = transEvent.trim();
            for (TriggerEvent te : eventOccurrences) {
                String event = te.getName();
                if (event == null) {
                    continue; // Unnamed events
                }
                String trimEvent = event.trim();
                if (trimEvent.equals(trimTransEvent)) {
                    return true; // Match
                } else if (te.getType() != TriggerEvent.CHANGE_EVENT
                        && trimTransEvent.equals("*")) {
                    return true; // Wildcard, skip gen'ed ones like .done etc.
                } else if (trimTransEvent.endsWith(".*")
                        && trimEvent.startsWith(trimTransEvent.substring(0,
                                trimTransEvent.length() - 1))) {
                    return true; // Prefixed wildcard
                }
            }
            return false;
        }
    }

    /**
     * Implements event prefix match to ascertain &lt;finalize&gt; execution.
     *
     * @param parentStateId
     *            the ID of the parent state of the &lt;invoke&gt; holding
     *            the &lt;finalize&gt;
     * @param eventOccurrences
     *            current events
     * @return true/false
     */
    protected boolean finalizeMatch(final String parentStateId,
            final Set<TriggerEvent> eventOccurrences) {
        String prefix = parentStateId + ".invoke."; // invoke prefix
        for (TriggerEvent te : eventOccurrences) {
            String evt = te.getName();
            if (evt == null) {
                continue; // Unnamed events
            } else if (evt.trim().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TransitionTargetComparator factory method.
     * @return Comparator The TransitionTarget comparator
     */
    protected Comparator<TransitionTarget> getTTComparator() {
        return targetComparator;
    }

    /**
     * Set the log used by this <code>SCXMLSemantics</code> instance.
     *
     * @param log The new log.
     */
    protected void setLog(final Log log) {
        this.appLog = log;
    }

    /**
     * Get the log used by this <code>SCXMLSemantics</code> instance.
     *
     * @return Log The log being used.
     */
    protected Log getLog() {
        return appLog;
    }

}

