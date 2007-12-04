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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.model.Datamodel;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.semantics.SCXMLSemanticsImpl;

/**
 * <p>The SCXML &quot;engine&quot; that executes SCXML documents. The
 * particular semantics used by this engine for executing the SCXML are
 * encapsulated in the SCXMLSemantics implementation that it uses.</p>
 *
 * <p>The default implementation is
 * <code>org.apache.commons.scxml.semantics.SCXMLSemanticsImpl</code></p>
 *
 * @see SCXMLSemantics
 */
public class SCXMLExecutor implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Logger for the SCXMLExecutor.
     */
    private Log log = LogFactory.getLog(SCXMLExecutor.class);

    /**
     * The stateMachine being executed.
     */
    private SCXML stateMachine;

    /**
     * The current status of the stateMachine.
     */
    private Status currentStatus;

    /**
     * The event dispatcher to interface with external documents etc.
     */
    private EventDispatcher eventdispatcher;

    /**
     * The environment specific error reporter.
     */
    private ErrorReporter errorReporter = null;

    /**
     * Run-to-completion.
     */
    private boolean superStep = true;

    /**
     *  Interpretation semantics.
     */
    private SCXMLSemantics semantics;

    /**
     * The SCInstance.
     */
    private SCInstance scInstance;

    /**
     * The worker method.
     * Re-evaluates current status whenever any events are triggered.
     *
     * @param evts
     *            an array of external events which triggered during the last
     *            time quantum
     * @throws ModelException in case there is a fatal SCXML object
     *            model problem.
     */
    public synchronized void triggerEvents(final TriggerEvent[] evts)
            throws ModelException {
        // Set event data, saving old values
        Object[] oldData = setEventData(evts);

        // Forward events (external only) to any existing invokes,
        // and finalize processing
        semantics.processInvokes(evts, errorReporter, scInstance);

        List evs = new ArrayList(Arrays.asList(evts));
        Step step = null;

        do {
            // CreateStep
            step = new Step(evs, currentStatus);
            // EnumerateReachableTransitions
            semantics.enumerateReachableTransitions(stateMachine, step,
                errorReporter);
            // FilterTransitionSet
            semantics.filterTransitionsSet(step, eventdispatcher,
                errorReporter, scInstance);
            // FollowTransitions
            semantics.followTransitions(step, errorReporter, scInstance);
            // UpdateHistoryStates
            semantics.updateHistoryStates(step, errorReporter, scInstance);
            // ExecuteActions
            semantics.executeActions(step, stateMachine, eventdispatcher,
                errorReporter, scInstance);
            // AssignCurrentStatus
            updateStatus(step);
            // ***Cleanup external events if superStep
            if (superStep) {
                evs.clear();
            }
        } while (superStep && currentStatus.getEvents().size() > 0);

        // InitiateInvokes only after state machine has stabilized
        semantics.initiateInvokes(step, errorReporter, scInstance);

        // Restore event data
        restoreEventData(oldData);
        logState();
    }

    /**
     * Convenience method when only one event needs to be triggered.
     *
     * @param evt
     *            the external events which triggered during the last
     *            time quantum
     * @throws ModelException in case there is a fatal SCXML object
     *            model problem.
     */
    public void triggerEvent(final TriggerEvent evt)
            throws ModelException {
        triggerEvents(new TriggerEvent[] {evt});
    }

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp The event dispatcher
     * @param errRep The error reporter
     */
    public SCXMLExecutor(final Evaluator expEvaluator,
            final EventDispatcher evtDisp, final ErrorReporter errRep) {
        this(expEvaluator, evtDisp, errRep, null);
    }

    /**
     * Convenience constructor.
     */
    public SCXMLExecutor() {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp The event dispatcher
     * @param errRep The error reporter
     * @param semantics The SCXML semantics
     */
    public SCXMLExecutor(final Evaluator expEvaluator,
            final EventDispatcher evtDisp, final ErrorReporter errRep,
            final SCXMLSemantics semantics) {
        this.eventdispatcher = evtDisp;
        this.errorReporter = errRep;
        this.currentStatus = new Status();
        this.stateMachine = null;
        if (semantics == null) {
            // Use default semantics, if none provided
            this.semantics = new SCXMLSemanticsImpl();
        } else {
            this.semantics = semantics;
        }
        this.scInstance = new SCInstance(this);
        this.scInstance.setEvaluator(expEvaluator);
    }

    /**
     * Clear all state and begin from &quot;initialstate&quot; indicated
     * on root SCXML element.
     *
     * @throws ModelException in case there is a fatal SCXML object
     *         model problem.
     */
    public synchronized void reset() throws ModelException {
        // Reset all variable contexts
        Context rootCtx = scInstance.getRootContext();
        // Clone root datamodel
        if (stateMachine == null) {
            log.error(ERR_NO_STATE_MACHINE);
            throw new ModelException(ERR_NO_STATE_MACHINE);
        } else {
            Datamodel rootdm = stateMachine.getDatamodel();
            SCXMLHelper.cloneDatamodel(rootdm, rootCtx,
                scInstance.getEvaluator(), log);
        }
        // all states and parallels, only states have variable contexts
        for (Iterator i = stateMachine.getTargets().values().iterator();
                i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            if (tt instanceof State) {
                Context context = scInstance.lookupContext(tt);
                if (context != null) {
                    context.reset();
                    Datamodel dm = tt.getDatamodel();
                    if (dm != null) {
                        SCXMLHelper.cloneDatamodel(dm, context,
                            scInstance.getEvaluator(), log);
                    }
                }
            } else if (tt instanceof History) {
                scInstance.reset((History) tt);
            }
        }
        // CreateEmptyStatus
        currentStatus = new Status();
        Step step = new Step(null, currentStatus);
        // DetermineInitialStates
        semantics.determineInitialStates(stateMachine,
                step.getAfterStatus().getStates(),
                step.getEntryList(), errorReporter, scInstance);
        // ExecuteActions
        semantics.executeActions(step, stateMachine, eventdispatcher,
                errorReporter, scInstance);
        // AssignCurrentStatus
        updateStatus(step);
        // Execute Immediate Transitions
        if (superStep && currentStatus.getEvents().size() > 0) {
            this.triggerEvents(new TriggerEvent[0]);
        } else {
            // InitiateInvokes only after state machine has stabilized
            semantics.initiateInvokes(step, errorReporter, scInstance);
            logState();
        }
    }

    /**
     * Get the current status.
     *
     * @return The current Status
     */
    public synchronized Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Set the expression evaluator.
     *
     * @param evaluator The evaluator to set.
     */
    public void setEvaluator(final Evaluator evaluator) {
        this.scInstance.setEvaluator(evaluator);
    }

    /**
     * Get the expression evaluator in use.
     *
     * @return Evaluator The evaluator in use.
     */
    public Evaluator getEvaluator() {
        return scInstance.getEvaluator();
    }

    /**
     * Set the root context for this execution.
     *
     * @param rootContext The Context that ties to the host environment.
     */
    public void setRootContext(final Context rootContext) {
        this.scInstance.setRootContext(rootContext);
    }

    /**
     * Get the root context for this execution.
     *
     * @return Context The root context.
     */
    public Context getRootContext() {
        return scInstance.getRootContext();
    }

    /**
     * Get the state machine that is being executed.
     *
     * @return Returns the stateMachine.
     */
    public SCXML getStateMachine() {
        return stateMachine;
    }

    /**
     * Set the state machine to be executed.
     *
     * @param stateMachine The stateMachine to set.
     */
    public void setStateMachine(final SCXML stateMachine) {
        // NormalizeStateMachine
        SCXML sm = semantics.normalizeStateMachine(stateMachine,
                errorReporter);
        // StoreStateMachine
        this.stateMachine = sm;
    }

    /**
     * Initiate state machine execution.
     *
     * @throws ModelException in case there is a fatal SCXML object
     *  model problem.
     */
    public void go() throws ModelException {
        // same as reset
        this.reset();
    }

    /**
     * Get the environment specific error reporter.
     *
     * @return Returns the errorReporter.
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Set the environment specific error reporter.
     *
     * @param errorReporter The errorReporter to set.
     */
    public void setErrorReporter(final ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    /**
     * Get the event dispatcher.
     *
     * @return Returns the eventdispatcher.
     */
    public EventDispatcher getEventdispatcher() {
        return eventdispatcher;
    }

    /**
     * Set the event dispatcher.
     *
     * @param eventdispatcher The eventdispatcher to set.
     */
    public void setEventdispatcher(final EventDispatcher eventdispatcher) {
        this.eventdispatcher = eventdispatcher;
    }

    /**
     * Use &quot;super-step&quot;, default is <code>true</code>
     * (that is, run-to-completion is default).
     *
     * @return Returns the superStep property.
     * @see #setSuperStep(boolean)
     */
    public boolean isSuperStep() {
        return superStep;
    }

    /**
     * Set the super step.
     *
     * @param superStep
     * if true, the internal derived events are also processed
     *    (run-to-completion);
     * if false, the internal derived events are stored in the
     * CurrentStatus property and processed within the next
     * triggerEvents() invocation, also the immediate (empty event) transitions
     * are deferred until the next step
      */
    public void setSuperStep(final boolean superStep) {
        this.superStep = superStep;
    }

    /**
     * Add a listener to the document root.
     *
     * @param scxml The document root to attach listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final SCXML scxml, final SCXMLListener listener) {
        Object observable = scxml;
        scInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener from the document root.
     *
     * @param scxml The document root.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final SCXML scxml,
            final SCXMLListener listener) {
        Object observable = scxml;
        scInstance.getNotificationRegistry().removeListener(observable,
            listener);
    }

    /**
     * Add a listener to this transition target.
     *
     * @param transitionTarget The <code>TransitionTarget</code> to
     *                         attach listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final TransitionTarget transitionTarget,
            final SCXMLListener listener) {
        Object observable = transitionTarget;
        scInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener for this transition target.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final TransitionTarget transitionTarget,
            final SCXMLListener listener) {
        Object observable = transitionTarget;
        scInstance.getNotificationRegistry().removeListener(observable,
            listener);
    }

    /**
     * Add a listener to this transition.
     *
     * @param transition The <code>Transition</code> to attach listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final Transition transition,
            final SCXMLListener listener) {
        Object observable = transition;
        scInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener for this transition.
     *
     * @param transition The <code>Transition</code>.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final Transition transition,
            final SCXMLListener listener) {
        Object observable = transition;
        scInstance.getNotificationRegistry().removeListener(observable,
            listener);
    }

    /**
     * Register an <code>Invoker</code> for this target type.
     *
     * @param targettype The target type (specified by "targettype"
     *                   attribute of &lt;invoke&gt; tag).
     * @param invokerClass The <code>Invoker</code> <code>Class</code>.
     */
    public void registerInvokerClass(final String targettype,
            final Class invokerClass) {
        scInstance.registerInvokerClass(targettype, invokerClass);
    }

    /**
     * Remove the <code>Invoker</code> registered for this target
     * type (if there is one registered).
     *
     * @param targettype The target type (specified by "targettype"
     *                   attribute of &lt;invoke&gt; tag).
     */
    public void unregisterInvokerClass(final String targettype) {
        scInstance.unregisterInvokerClass(targettype);
    }

    /**
     * Get the state chart instance for this executor.
     *
     * @return The SCInstance for this executor.
     */
    SCInstance getSCInstance() {
        return scInstance;
    }

    /**
     * Log the current set of active states.
     */
    private void logState() {
        if (log.isDebugEnabled()) {
            Iterator si = currentStatus.getStates().iterator();
            StringBuffer sb = new StringBuffer("Current States: [");
            while (si.hasNext()) {
                State s = (State) si.next();
                sb.append(s.getId());
                if (si.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(']');
            log.debug(sb.toString());
        }
    }

    /**
     * @param step The most recent Step
     */
    private void updateStatus(final Step step) {
        currentStatus = step.getAfterStatus();
        scInstance.getRootContext().setLocal("_ALL_STATES",
            SCXMLHelper.getAncestorClosure(currentStatus.getStates(), null));
        setEventData((TriggerEvent[]) currentStatus.getEvents().
            toArray(new TriggerEvent[0]));
    }

    /**
     * @param evts The events being triggered.
     * @return Object[] Previous values.
     */
    private Object[] setEventData(final TriggerEvent[] evts) {
        Context rootCtx = scInstance.getRootContext();
        Object[] oldData = {rootCtx.get(EVENT_DATA),
            rootCtx.get(EVENT_DATA_MAP)};
        int len = evts.length;
        if (len > 0) { // 0 has retry semantics (eg: see usage in reset())
            Object eventData = null;
            Map payloadMap = new HashMap();
            for (int i = 0; i < len; i++) {
                TriggerEvent te = evts[i];
                payloadMap.put(te.getName(), te.getPayload());
            }
            if (len == 1) {
                // we have only one event
                eventData = evts[0].getPayload();
            }
            rootCtx.setLocal(EVENT_DATA, eventData);
            rootCtx.setLocal(EVENT_DATA_MAP, payloadMap);
        }
        return oldData;
    }

    /**
     * @param oldData The old values to restore to.
     */
    private void restoreEventData(final Object[] oldData) {
        scInstance.getRootContext().setLocal(EVENT_DATA, oldData[0]);
        scInstance.getRootContext().setLocal(EVENT_DATA_MAP, oldData[1]);
    }

    /**
     * The special variable for storing single event data / payload.
     */
    private static final String EVENT_DATA = "_eventdata";

    /**
     * The special variable for storing event data / payload,
     * when multiple events are triggered, keyed by event name.
     */
    private static final String EVENT_DATA_MAP = "_eventdatamap";

    /**
     * SCXMLExecutor put into motion without setting a model (state machine).
     */
    private static final String ERR_NO_STATE_MACHINE =
        "SCXMLExecutor: State machine not set";

}

