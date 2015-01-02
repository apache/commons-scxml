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
package org.apache.commons.scxml2.semantics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExecutionContext;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSemantics;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.StateConfiguration;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.DocumentOrder;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Executable;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.OnEntry;
import org.apache.commons.scxml2.model.OnExit;
import org.apache.commons.scxml2.model.Script;
import org.apache.commons.scxml2.model.SimpleTransition;
import org.apache.commons.scxml2.model.TransitionalState;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.system.EventVariable;

/**
 * This class encapsulate and implements the
 * <a href="http://www.w3.org/TR/2014/CR-scxml-20140313/#AlgorithmforSCXMLInterpretation">
 *     W3C SCXML Algorithm for SCXML Interpretation</a>
 *
 * <p>Custom semantics can be created by sub-classing this implementation.</p>
 * <p>This implementation is full stateless and all methods are public accessible to make
 * it easier to extend, reuse and test its behavior.</p>
 */
public class SCXMLSemanticsImpl implements SCXMLSemantics {

    /**
     * Suffix for error event that are triggered in reaction to invalid data
     * model locations.
     */
    public static final String ERR_ILLEGAL_ALLOC = ".error.illegalalloc";

    /**
     * Optional post processing immediately following SCXMLReader. May be used
     * for removing pseudo-states etc.
     *
     * @param input  SCXML state machine
     * @param errRep ErrorReporter callback
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     */
    public SCXML normalizeStateMachine(final SCXML input, final ErrorReporter errRep) {
        //it is a no-op for now
        return input;
    }

    /**
     * First step in the execution of an SCXML state machine.
     * <p>
     * This will first (re)initialize the state machine instance, destroying any existing state!
     * </p>
     * <p>
     * The first step is corresponding to the Algorithm for SCXML processing from the interpret() procedure to the
     * mainLoop() procedure up to the blocking wait for an external event.
     * </p>
     * <p>
     * This step will thus complete the SCXML initial execution and a subsequent macroStep to stabilize the state
     * machine again before returning.
     * </p>
     * <p>
     * If the state machine no longer is running after all this, first the {@link #finalStep(SCXMLExecutionContext)}
     * will be called for cleanup before returning.
     * </p>
     * @param exctx The execution context for this step
     * @throws ModelException if the state machine instance failed to initialize or a SCXML model error occurred during
     * the execution.
     */
    public void firstStep(final SCXMLExecutionContext exctx) throws ModelException {
        // (re)initialize the execution context and state machine instance
        exctx.initialize();
        // execute global script if defined
        executeGlobalScript(exctx);
        // enter initial states
        HashSet<TransitionalState> statesToInvoke = new HashSet<TransitionalState>();
        Step step = new Step(null);
        step.getTransitList().add(exctx.getStateMachine().getInitialTransition());
        microStep(exctx, step, statesToInvoke);
        // Execute Immediate Transitions

        if (exctx.isRunning()) {
            macroStep(exctx, statesToInvoke);
        }

        if (!exctx.isRunning()) {
            finalStep(exctx);
        }
    }

    /**
     * Next step in the execution of an SCXML state machine.
     * <p>
     * The next step is corresponding to the Algorithm for SCXML processing mainEventLoop() procedure after receiving an
     * external event, up to the blocking wait for another external event.
     * </p>
     * <p>
     * If the state machine isn't {@link SCXMLExecutionContext#isRunning()} (any more), invoking this method will simply
     * do nothing.
     * </p>
     * <p>
     * If the provided event is a {@link TriggerEvent#CANCEL_EVENT}, the state machine will stop running.
     * </p>
     * <p>
     * Otherwise, the event is set in the {@link SCXMLSystemContext} and processing of the event then is started, and
     * if the event leads to any transitions a microStep for this event will be performed, followed up by a macroStep
     * to stabilize the state machine again before returning.
     * </p>
     * <p>
     * If the state machine no longer is running after all this, first the {@link #finalStep(SCXMLExecutionContext)}
     * will be called for cleanup before returning.
     * </p>
     * @param exctx The execution context for this step
     * @param event The event to process
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void nextStep(final SCXMLExecutionContext exctx, final TriggerEvent event) throws ModelException {
        if (!exctx.isRunning()) {
            return;
        }
        if (isCancelEvent(event)) {
            exctx.stopRunning();
        }
        else {
            setSystemEventVariable(exctx.getScInstance(), event, false);
            processInvokes(exctx, event);
            Step step = new Step(event);
            selectTransitions(exctx, step);
            if (!step.getTransitList().isEmpty()) {
                HashSet<TransitionalState> statesToInvoke = new HashSet<TransitionalState>();
                microStep(exctx, step, statesToInvoke);
                if (exctx.isRunning()) {
                    macroStep(exctx, statesToInvoke);
                }
            }
        }
        if (!exctx.isRunning()) {
            finalStep(exctx);
        }
    }

    /**
     * The final step in the execution of an SCXML state machine.
     * <p>
     * This final step is corresponding to the Algorithm for SCXML processing exitInterpreter() procedure, after the
     * state machine stopped running.
     * </p>
     * <p>
     * If the state machine still is {@link SCXMLExecutionContext#isRunning()} invoking this method will simply
     * do nothing.
     * </p>
     * <p>
     * This final step will exit all remaining active states and cancel any active invokers.
     * </p>
     * <p>
     *  <em>TODO: the current implementation does not yet provide final donedata handling.</em>
     * </p>
     * @param exctx The execution context for this step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void finalStep(SCXMLExecutionContext exctx) throws ModelException {
        if (exctx.isRunning()) {
            return;
        }
        ArrayList<EnterableState> configuration = new ArrayList<EnterableState>(exctx.getScInstance().getStateConfiguration().getActiveStates());
        Collections.sort(configuration, DocumentOrder.reverseDocumentOrderComparator);
        for (EnterableState es : configuration) {
            for (OnExit onexit : es.getOnExits()) {
                executeContent(exctx, onexit);
            }
            if (es instanceof TransitionalState) {
                // check if invokers are active in this state
                for (Invoke inv : ((TransitionalState)es).getInvokes()) {
                    exctx.cancelInvoker(inv);
                }
            }
            exctx.getNotificationRegistry().fireOnExit(es, es);
            exctx.getNotificationRegistry().fireOnExit(exctx.getStateMachine(), es);
            if (!(es instanceof Final && es.getParent() == null)) {
                exctx.getScInstance().getStateConfiguration().exitState(es);
            }
            // else: keep final Final
            // TODO: returnDoneEvent(s.donedata)?
        }
    }

    /**
     * Perform a micro step in the execution of a state machine.
     * <p>
     * This micro step is corresponding to the Algorithm for SCXML processing microstep() procedure.
     * <p>
     * @param exctx The execution context for this step
     * @param step The current micro step
     * @param statesToInvoke the set of activated states which invokes need to be invoked at the end of the current
     *                       macro step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void microStep(final SCXMLExecutionContext exctx, final Step step,
                          final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        buildStep(exctx, step);
        exitStates(exctx, step, statesToInvoke);
        executeTransitionContent(exctx, step);
        enterStates(exctx, step, statesToInvoke);
        step.clearIntermediateState();
    }

    /**
     * buildStep builds the exitSet and entrySet for the current configuration given the transitionList on the step.
     *
     * @param exctx The SCXML execution context
     * @param step The step containing the list of transitions to be taken
     * @throws ModelException if the result of taking the transitions would lead to an illegal configuration
     */
    public void buildStep(final SCXMLExecutionContext exctx, final Step step) throws ModelException {
        step.clearIntermediateState();

        // compute exitSet, if there is something to exit and record their History configurations if applicable
        if (!exctx.getScInstance().getStateConfiguration().getActiveStates().isEmpty()) {
            computeExitSet(step, exctx.getScInstance().getStateConfiguration());
        }
        // compute entrySet
        computeEntrySet(exctx, step);

        // default result states to entrySet
        Set<EnterableState> states = step.getEntrySet();
        if (!step.getExitSet().isEmpty()) {
            // calculate result states by taking current states, subtracting exitSet and adding entrySet
            states = new HashSet<EnterableState>(exctx.getScInstance().getStateConfiguration().getStates());
            states.removeAll(step.getExitSet());
            states.addAll(step.getEntrySet());
        }
        // validate the result states represent a legal configuration
        if (exctx.isCheckLegalConfiguration() && !isLegalConfiguration(states, exctx.getErrorReporter())) {
            throw new ModelException("Illegal state machine configuration!");
        }
    }

    /**
     * Perform a macro step in the execution of a state machine.
     * <p>
     * This macro step is corresponding to the Algorithm for SCXML processing mainEventLoop() procedure macro step
     * sub-flow, which are the first <em>3</em> steps of the described <em>4</em>, so everything up to the blocking
     * wait for an external event.
     * <p>
     * @param exctx The execution context for this step
     * @param statesToInvoke the set of activated states which invokes need to be invoked at the end of the current
     *                       macro step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void macroStep(final SCXMLExecutionContext exctx, final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        do {
            boolean macroStepDone = false;
            do {
                Step step = new Step(null);
                selectTransitions(exctx, step);
                if (step.getTransitList().isEmpty()) {
                    TriggerEvent event = exctx.nextInternalEvent();
                    if (event != null) {
                        if (isCancelEvent(event)) {
                            exctx.stopRunning();
                        }
                        else {
                            setSystemEventVariable(exctx.getScInstance(), event, true);
                            step = new Step(event);
                            selectTransitions(exctx, step);
                        }
                    }
                }
                if (step.getTransitList().isEmpty()) {
                    macroStepDone = true;
                }
                else {
                    microStep(exctx, step, statesToInvoke);
                }

            } while (exctx.isRunning() && !macroStepDone);

            if (exctx.isRunning() && !statesToInvoke.isEmpty()) {
                initiateInvokes(exctx, statesToInvoke);
                statesToInvoke.clear();
            }
        } while (exctx.isRunning() && exctx.hasPendingInternalEvent());
    }

    /**
     * Compute and store the set of states to exit for the current list of transitions in the provided step.
     * <p>
     * This method corresponds to the Algorithm for SCXML processing computeExitSet() procedure.
     * <p>
     * @param step The step containing the list of transitions to be taken
     * @param stateConfiguration The current configuration of the state machine ({@link SCInstance#getStateConfiguration()}).
     */
    public void computeExitSet(final Step step, final StateConfiguration stateConfiguration) {
        if (!stateConfiguration.getActiveStates().isEmpty()) {
            for (SimpleTransition st : step.getTransitList()) {
                computeExitSet(st, step.getExitSet(), stateConfiguration.getActiveStates());
            }
            recordHistory(step, stateConfiguration.getStates(), stateConfiguration.getActiveStates());
        }
    }

    /**
     * Compute and store the set of states to exit for one specific transition in the provided step.
     * <p>
     * This method corresponds to the Algorithm for SCXML processing computeExitSet() procedure.
     * <p>
     * @param transition The transition to compute the states to exit from
     * @param exitSet The set for adding the states to exit to
     * @param activeStates The current active states of the state machine ({@link StateConfiguration#getActiveStates()}).
     */
    public void computeExitSet(SimpleTransition transition, Set<EnterableState> exitSet, Set<EnterableState> activeStates) {
        if (!transition.getTargets().isEmpty()) {
            TransitionalState transitionDomain = transition.getTransitionDomain();
            if (transitionDomain == null) {
                // root transition: every active state will be exited
                exitSet.addAll(activeStates);
            }
            else {
                for (EnterableState state : activeStates) {
                    if (state.isDescendantOf(transitionDomain)) {
                        exitSet.add(state);
                    }
                }
            }
        }
    }

    /**
     * Record the history configurations for states to exit if applicable and temporarily store this in the step.
     * <p>
     * These history configurations must be pre-recorded as they might impact (re)entrance calculation during
     * {@link #computeEntrySet(SCXMLExecutionContext, Step)}.
     * </p>
     * <p>
     * Only after the new configuration has been validated (see: {@link #isLegalConfiguration(Set, ErrorReporter)}), the
     * history configurations will be persisted during the actual {@link #exitStates(SCXMLExecutionContext, Step, Set)}
     * processing.
     * </p>
     * @param step The step containing the list of states to exit, and the map to record the new history configurations
     * @param atomicStates The current set of active atomic states in the state machine
     * @param activeStates The current set of all active states in the state machine
     */
    public void recordHistory(final Step step, final Set<EnterableState> atomicStates, final Set<EnterableState> activeStates) {
        for (EnterableState es : step.getExitSet()) {
            if (es instanceof TransitionalState && ((TransitionalState)es).hasHistory()) {
                TransitionalState ts = (TransitionalState)es;
                Set<EnterableState> shallow = null;
                Set<EnterableState> deep = null;
                for (History h : ts.getHistory()) {
                    if (h.isDeep()) {
                        if (deep == null) {
                            //calculate deep history for a given state once
                            deep = new HashSet<EnterableState>();
                            for (EnterableState ott : atomicStates) {
                                if (ott.isDescendantOf(es)) {
                                    deep.add(ott);
                                }
                            }
                        }
                        step.getNewHistoryConfigurations().put(h, deep);
                    } else {
                        if (shallow == null) {
                            //calculate shallow history for a given state once
                            shallow = new HashSet<EnterableState>(ts.getChildren());
                            shallow.retainAll(activeStates);
                        }
                        step.getNewHistoryConfigurations().put(h, shallow);
                    }
                }
            }
        }
    }

    /**
     * Compute and store the set of states to enter for the current list of transitions in the provided step.
     * <p>
     * This method corresponds to the Algorithm for SCXML processing computeEntrySet() procedure.
     * <p>
     * @param exctx The execution context for this step
     * @param step The step containing the list of transitions to be taken
     */
    public void computeEntrySet(final SCXMLExecutionContext exctx, final Step step) {
        Set<History> historyTargets = new HashSet<History>();
        Set<EnterableState> entrySet = new HashSet<EnterableState>();
        for (SimpleTransition st : step.getTransitList()) {
            for (TransitionTarget tt : st.getTargets()) {
                if (tt instanceof EnterableState) {
                    entrySet.add((EnterableState) tt);
                }
                else {
                    // History
                    historyTargets.add((History)tt);
                }
            }
        }
        for (EnterableState es : entrySet) {
            addDescendantStatesToEnter(exctx, step, es);
        }
        for (History h : historyTargets) {
            addDescendantStatesToEnter(exctx, step, h);
        }
        for (SimpleTransition st : step.getTransitList()) {
            TransitionalState ancestor = st.getTransitionDomain();
            for (TransitionTarget tt : st.getTargets()) {
                addAncestorStatesToEnter(exctx, step, tt, ancestor);
            }
        }
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing addDescendantStatesToEnter() procedure.
     *
     * @param exctx The execution context for this step
     * @param step The step
     * @param tt The TransitionTarget
     */
    public void addDescendantStatesToEnter(final SCXMLExecutionContext exctx, final Step step,
                                              final TransitionTarget tt) {
        if (tt instanceof History) {
            History h = (History) tt;
            Set<EnterableState> lastConfiguration = step.getNewHistoryConfigurations().get(h);
            if (lastConfiguration == null) {
                lastConfiguration = exctx.getScInstance().getLastConfiguration(h);
            }
            if (lastConfiguration.isEmpty()) {
                step.getDefaultHistoryTransitions().put(h.getParent(), h.getTransition());
                for (TransitionTarget dtt : h.getTransition().getTargets()) {
                    addDescendantStatesToEnter(exctx, step, dtt);
                }
                for (TransitionTarget dtt : h.getTransition().getTargets()) {
                    addAncestorStatesToEnter(exctx, step, dtt, tt.getParent());
                }
            } else {
                for (TransitionTarget dtt : lastConfiguration) {
                    addDescendantStatesToEnter(exctx, step, dtt);
                }
                for (TransitionTarget dtt : lastConfiguration) {
                    addAncestorStatesToEnter(exctx, step, dtt, tt.getParent());
                }
            }
        }
        else { // tt instanceof EnterableState
            EnterableState es = (EnterableState)tt;
            step.getEntrySet().add(es);
            if (es instanceof Parallel) {
                for (EnterableState child : ((Parallel)es).getChildren()) {
                    if (!containsDescendant(step.getEntrySet(), child)) {
                        addDescendantStatesToEnter(exctx, step, child);
                    }
                }
            }
            else if (es instanceof State && ((State) es).isComposite()) {
                step.getDefaultEntrySet().add(es);
                for (TransitionTarget dtt : ((State)es).getInitial().getTransition().getTargets()) {
                    addDescendantStatesToEnter(exctx, step, dtt);
                }
                for (TransitionTarget dtt : ((State)es).getInitial().getTransition().getTargets()) {
                    addAncestorStatesToEnter(exctx, step, dtt, tt);
                }
            }
        }
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing addAncestorStatesToEnter() procedure.
     *
     * @param exctx The execution context for this step
     * @param step The step
     * @param tt The TransitionTarget
     * @param ancestor The ancestor TransitionTarget
     */
    public void addAncestorStatesToEnter(final SCXMLExecutionContext exctx, final Step step,
                                            final TransitionTarget tt, TransitionTarget ancestor) {
        // for for anc in getProperAncestors(tt,ancestor)
        for (int i = tt.getNumberOfAncestors()-1; i > -1; i--) {
            EnterableState anc = tt.getAncestor(i);
            if (anc == ancestor) {
                break;
            }
            step.getEntrySet().add(anc);
            if (anc instanceof Parallel) {
                for (EnterableState child : ((Parallel)anc).getChildren()) {
                    if (!containsDescendant(step.getEntrySet(), child)) {
                        addDescendantStatesToEnter(exctx, step, child);
                    }
                }

            }
        }
    }

    /**
     * @return Returns true if a member of the provided states set is a descendant of the provided state.
     * @param states the set of states to check for descendants
     * @param state the state to check with
     */
    public boolean containsDescendant(Set<EnterableState> states, EnterableState state) {
        for (EnterableState es : states) {
            if (es.isDescendantOf(state)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing selectTransitions() as well as the
     * selectEventlessTransitions() procedure, depending on the event (or null) in the provided step
     * <p>
     * @param exctx The execution context for this step
     * @param step The step
     */
    public void selectTransitions(final SCXMLExecutionContext exctx, final Step step) throws ModelException {
        step.getTransitList().clear();
        ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();

        ArrayList<EnterableState> configuration = new ArrayList<EnterableState>(exctx.getScInstance().getStateConfiguration().getActiveStates());
        Collections.sort(configuration,DocumentOrder.documentOrderComparator);

        HashSet<EnterableState> visited = new HashSet<EnterableState>();

        String eventName = step.getEvent() != null ? step.getEvent().getName() : null;
        for (EnterableState es : configuration) {
            if (es.isAtomicState()) {
                if (es instanceof Final) {
                    // Final states don't have transitions, skip to parent
                    if (es.getParent() == null) {
                        // should not happen: a top level active Final state should have stopped the state machine
                        throw new ModelException("Illegal state machine configuration: encountered top level <final> "
                                + "state while processing an event");
                    }
                    else {
                        es = es.getParent();
                    }
                }
                TransitionalState state = (TransitionalState)es;
                TransitionalState current = state;
                int ancestorIndex = state.getNumberOfAncestors()-1;
                boolean transitionMatched = false;
                do {
                    for (Transition transition : current.getTransitionsList()) {
                        if (transitionMatched = matchTransition(exctx, transition, eventName)) {
                            enabledTransitions.add(transition);
                            break;
                        }
                    }
                    current = (!transitionMatched && ancestorIndex > -1) ? state.getAncestor(ancestorIndex--) : null;
                } while (!transitionMatched && current != null && visited.add(current));
            }
        }
        removeConflictingTransitions(exctx, step, enabledTransitions);
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing removeConflictingTransitions() procedure.
     *
     * @param exctx The execution context for this step
     * @param step The step
     * @param enabledTransitions The list of enabled transitions
     */
    public void removeConflictingTransitions(final SCXMLExecutionContext exctx, final Step step,
                                             final List<Transition> enabledTransitions) {
        LinkedHashSet<Transition> filteredTransitions = new LinkedHashSet<Transition>();
        LinkedHashSet<Transition> preemptedTransitions = new LinkedHashSet<Transition>();
        Map<Transition, Set<EnterableState>> exitSets = new HashMap<Transition, Set<EnterableState>>();

        Set<EnterableState> configuration = exctx.getScInstance().getStateConfiguration().getActiveStates();
        Collections.sort(enabledTransitions, DocumentOrder.documentOrderComparator);

        for (Transition t1 : enabledTransitions) {
            boolean t1Preempted = false;
            Set<EnterableState> t1ExitSet = exitSets.get(t1);
            for (Transition t2 : filteredTransitions) {
                if (t1ExitSet == null) {
                    t1ExitSet = new HashSet<EnterableState>();
                    computeExitSet(t1, t1ExitSet, configuration);
                    exitSets.put(t1, t1ExitSet);
                }
                Set<EnterableState> t2ExitSet = exitSets.get(t2);
                if (t2ExitSet == null) {
                    t2ExitSet = new HashSet<EnterableState>();
                    computeExitSet(t2, t2ExitSet, configuration);
                    exitSets.put(t2, t2ExitSet);
                }
                Set<EnterableState> smaller = t1ExitSet.size() < t2ExitSet.size() ? t1ExitSet : t2ExitSet;
                Set<EnterableState> larger = smaller == t1ExitSet ? t2ExitSet : t1ExitSet;
                boolean hasIntersection = false;
                for (EnterableState s1 : smaller) {
                    hasIntersection = larger.contains(s1);
                    if (hasIntersection) {
                        break;
                    }
                }
                if (hasIntersection) {
                    if (t1.getParent().isDescendantOf(t2.getParent())) {
                        preemptedTransitions.add(t2);
                    }
                    else {
                        t1Preempted = true;
                        break;
                    }
                }
            }
            if (t1Preempted) {
                exitSets.remove(t1);
            }
            else {
                for (Transition preempted : preemptedTransitions) {
                    filteredTransitions.remove(preempted);
                    exitSets.remove(preempted);
                }
                filteredTransitions.add(t1);
            }
        }
        step.getTransitList().addAll(filteredTransitions);
    }

    /**
     * @param exctx The execution context for this step
     * @param transition The transition
     * @param eventName The (optional) event name to match against
     * @return Returns true if the transition matches against the provided eventName, or is event-less when no eventName
     *         is provided, <em>AND</em> its (optional) condition guard evaluates to true.
     */
    public boolean matchTransition(final SCXMLExecutionContext exctx, final Transition transition, final String eventName) {
        if (eventName != null) {
            if (!(transition.isNoEventsTransition() || transition.isAllEventsTransition())) {
                boolean eventMatch = false;
                for (String event : transition.getEvents()) {
                    if (eventName.startsWith(event)) {
                        if (eventName.length() == event.length() || eventName.charAt(event.length())=='.')
                            eventMatch = true;
                        break;
                    }
                }
                if (!eventMatch) {
                    return false;
                }
            }
            else if (!transition.isAllEventsTransition()) {
                return false;
            }
        }
        else if (!transition.isNoEventsTransition()) {
            return false;
        }
        if (transition.getCond() != null) {
            Boolean result = Boolean.FALSE;
            Context context = exctx.getScInstance().getContext(transition.getParent());
            context.setLocal(Context.NAMESPACES_KEY, transition.getNamespaces());
            try {
                if ((result = exctx.getEvaluator().evalCond(context, transition.getCond())) == null) {
                    result = Boolean.FALSE;
                    if (exctx.getAppLog().isDebugEnabled()) {
                        exctx.getAppLog().debug("Treating as false because the cond expression was evaluated as null: '"
                                + transition.getCond() + "'");
                    }
                }
            }
            catch (SCXMLExpressionException e) {
                exctx.getInternalIOProcessor().addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
                exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR, "Treating as false due to error: "
                        + e.getMessage(), transition);
            }
            finally {
                context.setLocal(Context.NAMESPACES_KEY, null);
            }
            return result;
        }
        return true;
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing isFinalState() function.
     *
     * @param es the enterable state to check
     * @param configuration the current state machine configuration
     * @return Return true if s is a compound state and one of its children is an active final state (i.e. is a member
     *         of the current configuration), or if s is a parallel state and isInFinalState is true of all its children.
     */
    public boolean isInFinalState(final EnterableState es, final Set<EnterableState> configuration) {
        if (es instanceof State) {
            for (EnterableState child : ((State)es).getChildren()) {
                if (child instanceof Final && configuration.contains(child)) {
                    return true;
                }
            }
        }
        else if (es instanceof Parallel) {
            for (EnterableState child : ((Parallel)es).getChildren()) {
                if (!isInFinalState(child, configuration)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if an external event was send (back) by an specific Invoker
     *
     * @param invokerId the invokerId
     *
     * @param event received external event
     * @return true if this event was send by the specific Invoker
     */
    public boolean isInvokerEvent(final String invokerId, final TriggerEvent event) {
        return event.getName().equals("done.invoke."+invokerId) ||
                event.getName().startsWith("done.invoke."+invokerId+".");
    }

    /**
     * Check if an external event indicates the state machine execution must be cancelled.
     *
     * @param event received external event
     * @return true if this event is of type {@link TriggerEvent#CANCEL_EVENT}.
     */
    public boolean isCancelEvent(TriggerEvent event) {
        return (event.getType() == TriggerEvent.CANCEL_EVENT);
    }

    /**
     * Checks whether a given set of states is a legal Harel State Table
     * configuration (with the respect to the definition of the OR and AND
     * states).
     *
     * @param states a set of states
     * @param errRep ErrorReporter to report detailed error info if needed
     * @return true if a given state configuration is legal, false otherwise
     */
    public boolean isLegalConfiguration(final Set<EnterableState> states, final ErrorReporter errRep) {
        /*
         * For every active state we add 1 to the count of its parent. Each
         * Parallel should reach count equal to the number of its children and
         * contribute by 1 to its parent. Each State should reach count exactly
         * 1. SCXML elemnt (top) should reach count exactly 1. We essentially
         * summarize up the hierarchy tree starting with a given set of
         * states = active configuration.
         */
        boolean legalConfig = true; // let's be optimists
        Map<EnterableState, Set<EnterableState>> counts = new HashMap<EnterableState, Set<EnterableState>>();
        Set<EnterableState> scxmlCount = new HashSet<EnterableState>();
        for (EnterableState es : states) {
            EnterableState parent;
            while ((parent = es.getParent()) != null) {
                Set<EnterableState> cnt = counts.get(parent);
                if (cnt == null) {
                    cnt = new HashSet<EnterableState>();
                    counts.put(parent, cnt);
                }
                cnt.add(es);
                es = parent;
            }
            //top-level contribution
            scxmlCount.add(es);
        }
        if (scxmlCount.size() > 1) {
            errRep.onError(ErrorConstants.ILLEGAL_CONFIG, "Multiple top-level OR states active!", scxmlCount);
            legalConfig = false;
        }
        else {
            //Validate child counts:
            for (Map.Entry<EnterableState, Set<EnterableState>> entry : counts.entrySet()) {
                EnterableState es = entry.getKey();
                Set<EnterableState> count = entry.getValue();
                if (es instanceof Parallel) {
                    Parallel p = (Parallel) es;
                    if (count.size() < p.getChildren().size()) {
                        errRep.onError(ErrorConstants.ILLEGAL_CONFIG, "Not all AND states active for parallel " + p.getId(), entry);
                        legalConfig = false;
                    }
                } else {
                    if (count.size() > 1) {
                        errRep.onError(ErrorConstants.ILLEGAL_CONFIG, "Multiple OR states active for state " + es.getId(), entry);
                        legalConfig = false;
                    }
                }
                count.clear(); //cleanup
            }
        }
        //cleanup
        scxmlCount.clear();
        counts.clear();
        return legalConfig;
    }

    /**
     * Stores the provided event in the system context
     * <p>
     * For the event a EventVariable is instantiated and the provided event its type is mapped to the one of the
     * SCXML specification predefined types.
     * </p>
     * @param scInstance the state machine instance holding the system context
     * @param event The event being stored
     * @param internal Flag indicating the event was received internally or externally
     */
    public void setSystemEventVariable(final SCInstance scInstance, final TriggerEvent event, boolean internal) {
        Context systemContext = scInstance.getSystemContext();
        EventVariable eventVar = null;
        if (event != null) {
            String eventType = internal ? EventVariable.TYPE_INTERNAL : EventVariable.TYPE_EXTERNAL;

            final int triggerEventType = event.getType();
            if (triggerEventType == TriggerEvent.ERROR_EVENT || triggerEventType == TriggerEvent.CHANGE_EVENT) {
                eventType = EventVariable.TYPE_PLATFORM;
            }

            // TODO: determine sendid, origin, originType and invokeid based on context later.
            eventVar = new EventVariable(event.getName(), eventType, null, null, null, null, event.getPayload());
        }
        systemContext.setLocal(SCXMLSystemContext.EVENT_KEY, eventVar);
    }

    /**
     * Executes the global SCXML script element
     *
     * @param exctx The execution context
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void executeGlobalScript(final SCXMLExecutionContext exctx) throws ModelException {
        Script globalScript = exctx.getStateMachine().getGlobalScript();
        if ( globalScript != null) {
            try {
                globalScript.execute(exctx.getActionExecutionContext());
            } catch (SCXMLExpressionException e) {
                exctx.getInternalIOProcessor().addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
                exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), exctx.getStateMachine());
            }
        }
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing exitStates() procedure, where the states to exit
     * already have been pre-computed in {@link #microStep(SCXMLExecutionContext, Step, java.util.Set)}.
     *
     * @param exctx The execution context for this micro step
     * @param step the step
     * @param statesToInvoke the set of activated states which invokes need to be invoked at the end of the current
     *                       macro step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void exitStates(final SCXMLExecutionContext exctx, final Step step,
                           final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        if (step.getExitSet().isEmpty()) {
            return;
        }
        ArrayList<EnterableState> exitList = new ArrayList<EnterableState>(step.getExitSet());
        Collections.sort(exitList, DocumentOrder.reverseDocumentOrderComparator);

        for (EnterableState es : exitList) {

            if (es instanceof TransitionalState && ((TransitionalState)es).hasHistory()) {
                // persist the new history configurations for this state to exit
                for (History h : ((TransitionalState)es).getHistory()) {
                    exctx.getScInstance().setLastConfiguration(h, step.getNewHistoryConfigurations().get(h));
                }
            }

            boolean onexitEventRaised = false;
            for (OnExit onexit : es.getOnExits()) {
                executeContent(exctx, onexit);
                if (!onexitEventRaised && onexit.isRaiseEvent()) {
                    onexitEventRaised = true;
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("exit.state."+es.getId(), TriggerEvent.CHANGE_EVENT));
                }
            }
            exctx.getNotificationRegistry().fireOnExit(es, es);
            exctx.getNotificationRegistry().fireOnExit(exctx.getStateMachine(), es);

            if (es instanceof TransitionalState && !statesToInvoke.remove(es)) {
                // check if invokers are active in this state
                for (Invoke inv : ((TransitionalState)es).getInvokes()) {
                    exctx.cancelInvoker(inv);
                }
            }
            exctx.getScInstance().getStateConfiguration().exitState(es);
        }
    }

    /**
     * Executes the executable content for all transitions in the micro step
     *
     * @param exctx The execution context for this micro step
     * @param step the step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void executeTransitionContent(final SCXMLExecutionContext exctx, final Step step) throws ModelException {
        for (SimpleTransition transition : step.getTransitList()) {
            executeContent(exctx, transition);
        }
    }

    /**
     * Executes the executable content for a specific executable in the micro step
     *
     * @param exctx The execution context for this micro step
     * @param exec the executable providing the execution content
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void executeContent(SCXMLExecutionContext exctx, Executable exec) throws ModelException {
        try {
            for (Action action : exec.getActions()) {
                action.execute(exctx.getActionExecutionContext());
            }
        } catch (SCXMLExpressionException e) {
            exctx.getInternalIOProcessor().addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
            exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), exec);
        }
        if (exec instanceof Transition) {
            Transition t = (Transition)exec;
            if (t.getTargets().isEmpty()) {
                notifyOnTransition(exctx, t, t.getParent());
            }
            else {
                for (TransitionTarget tt : t.getTargets()) {
                    notifyOnTransition(exctx, t, tt);
                }
            }
        }
    }

    /**
     * Notifies SCXMLListeners on the transition taken
     *
     * @param exctx The execution context for this micro step
     * @param t The Transition taken
     * @param target The target of the Transition
     */
    public void notifyOnTransition(final SCXMLExecutionContext exctx, final Transition t,
                                      final TransitionTarget target) {
        EventVariable event = (EventVariable)exctx.getScInstance().getSystemContext().getVars().get(SCXMLSystemContext.EVENT_KEY);
        String eventName = event != null ? event.getName() : null;
        exctx.getNotificationRegistry().fireOnTransition(t, t.getParent(), target, t, eventName);
        exctx.getNotificationRegistry().fireOnTransition(exctx.getStateMachine(), t.getParent(), target, t, eventName);
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing enterStates() procedure, where the states to enter
     * already have been pre-computed in {@link #microStep(SCXMLExecutionContext, Step, java.util.Set)}.
     *
     * @param exctx The execution context for this micro step
     * @param step the step
     * @param statesToInvoke the set of activated states which invokes need to be invoked at the end of the current
     *                       macro step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void enterStates(final SCXMLExecutionContext exctx, final Step step,
                            final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        if (step.getEntrySet().isEmpty()) {
            return;
        }
        ArrayList<EnterableState> entryList = new ArrayList<EnterableState>(step.getEntrySet());
        Collections.sort(entryList, DocumentOrder.documentOrderComparator);
        for (EnterableState es : entryList) {
            exctx.getScInstance().getStateConfiguration().enterState(es);
            if (es instanceof TransitionalState && !((TransitionalState)es).getInvokes().isEmpty()) {
                statesToInvoke.add((TransitionalState) es);
            }

            boolean onentryEventRaised = false;
            for (OnEntry onentry : es.getOnEntries()) {
                executeContent(exctx, onentry);
                if (!onentryEventRaised && onentry.isRaiseEvent()) {
                    onentryEventRaised = true;
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("entry.state."+es.getId(), TriggerEvent.CHANGE_EVENT));
                }
            }
            exctx.getNotificationRegistry().fireOnEntry(es, es);
            exctx.getNotificationRegistry().fireOnEntry(exctx.getStateMachine(), es);

            if (es instanceof State && step.getDefaultEntrySet().contains(es) && ((State)es).getInitial() != null) {
                executeContent(exctx, ((State)es).getInitial().getTransition());
            }
            if (es instanceof TransitionalState) {
                SimpleTransition hTransition = step.getDefaultHistoryTransitions().get(es);
                if (hTransition != null) {
                    executeContent(exctx, hTransition);
                }
            }

            if (es instanceof Final) {
                State parent = (State)es.getParent();
                if (parent == null) {
                    exctx.stopRunning();
                }
                else {
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("done.state."+parent.getId(),TriggerEvent.CHANGE_EVENT));
                    if (parent.isRegion()) {
                        if (isInFinalState(parent.getParent(), exctx.getScInstance().getStateConfiguration().getActiveStates())) {
                            exctx.getInternalIOProcessor().addEvent(new TriggerEvent("done.state."+parent.getParent().getId()
                                    , TriggerEvent.CHANGE_EVENT));
                        }
                    }
                }
            }
        }
    }

    /**
     * Initiate any new invoked activities.
     *
     * @param exctx provides the execution context
     * @param statesToInvoke the set of activated states which invokes need to be invoked
     */
    public void initiateInvokes(final SCXMLExecutionContext exctx,
                                final Set<TransitionalState> statesToInvoke) throws ModelException {
        ActionExecutionContext aexctx = exctx.getActionExecutionContext();
        for (TransitionalState ts : statesToInvoke) {
            for (Invoke invoke : ts.getInvokes()) {
                Context ctx = aexctx.getContext(invoke.getParentEnterableState());
                String invokerManagerKey = invoke.getInvokerManagerKey();
                ctx.setLocal(invokerManagerKey, exctx);
                invoke.execute(aexctx);
                ctx.setLocal(invokerManagerKey, null);
            }
        }
    }

    /**
     * Forward events to invoked activities, execute finalize handlers.
     *
     * @param exctx provides the execution context
     * @param event The events to be forwarded
     *
     * @throws ModelException in case there is a fatal SCXML object model problem.
     */
    public void processInvokes(final SCXMLExecutionContext exctx, final TriggerEvent event) throws ModelException {
        for (Map.Entry<Invoke, String> entry : exctx.getInvokeIds().entrySet()) {
            if (!isInvokerEvent(entry.getValue(), event)) {
                if (entry.getKey().isAutoForward()) {
                    Invoker inv = exctx.getInvoker(entry.getKey());
                    try {
                        inv.parentEvent(event);
                    } catch (InvokerException ie) {
                        exctx.getAppLog().error(ie.getMessage(), ie);
                        throw new ModelException(ie.getMessage(), ie.getCause());
                    }
                }
            }
            /*
            else {
                // TODO: applyFinalize
            }
            */
        }
    }
}

