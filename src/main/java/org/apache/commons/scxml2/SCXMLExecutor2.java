/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.Observable;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl2;
import org.apache.commons.scxml2.system.EventVariable;

/**
 * <p>The SCXML &quot;engine&quot; that executes SCXML documents. The
 * particular semantics used by this engine for executing the SCXML are
 * encapsulated in the SCXMLSemantics implementation that it uses.</p>
 *
 * <p>The default implementation is
 * <code>org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl</code></p>
 *
 * @see org.apache.commons.scxml2.SCXMLSemantics
 */
public class SCXMLExecutor2 extends SCXMLExecutor {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Logger for the SCXMLExecutor.
     */
    private Log log = LogFactory.getLog(SCXMLExecutor2.class);

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
    private SCXMLSemanticsImpl2 semantics;

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
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object
     *            model problem.
     */
    public synchronized void triggerEvents(final TriggerEvent[] evts)
            throws ModelException {
        // Set event data, saving old values
        Object[] oldData = setEventData(evts);

        // Forward events (external only) to any existing invokes,
        // and finalize processing
        semantics.processInvokes(evts, errorReporter, scInstance);

        List<TriggerEvent> evs = new ArrayList<TriggerEvent>(Arrays.asList(evts));
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
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object
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
    public SCXMLExecutor2(final Evaluator expEvaluator,
                          final EventDispatcher evtDisp, final ErrorReporter errRep) {
        this(expEvaluator, evtDisp, errRep, null);
    }

    /**
     * Convenience constructor.
     */
    public SCXMLExecutor2() {
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
    public SCXMLExecutor2(final Evaluator expEvaluator,
                          final EventDispatcher evtDisp, final ErrorReporter errRep,
                          final SCXMLSemanticsImpl2 semantics) {
        this.eventdispatcher = evtDisp;
        this.errorReporter = errRep;
        this.currentStatus = new Status();
        this.stateMachine = null;
        if (semantics == null) {
            // Use default semantics, if none provided
            this.semantics = new SCXMLSemanticsImpl2();
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
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object
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
        if (stateMachine.getInitialScript() != null) {
            Context initialScriptCtx = scInstance.getContext(stateMachine.getInitialScript().getParentTransitionTarget());
            if (initialScriptCtx != null) {
                initialScriptCtx.reset();
            }
        }
        // all states and parallels, only states have variable contexts
        for (TransitionTarget tt : stateMachine.getTargets().values()) {
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
        // execute initial script if defined configured as transition so as to not trigger events
        if (stateMachine.getInitialScript() != null) {
            step.getTransitList().add((Transition)stateMachine.getInitialScript().getParent());
        }
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
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
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
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
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
     * <b>NOTE:</b> This is the state machine definition or model used by this
     * executor instance. It may be shared across multiple executor instances
     * and as a best practice, should not be altered. Also note that
     * manipulation of instance data for the executor should happen through
     * its root context or state contexts only, never through the direct
     * manipulation of any {@link org.apache.commons.scxml2.model.Datamodel}s associated with this state
     * machine definition.
     *
     * @return Returns the stateMachine.
     */
    public SCXML getStateMachine() {
        return stateMachine;
    }

    /**
     * Set the state machine to be executed.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
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
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object
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
     * Add a listener to the {@link org.apache.commons.scxml2.model.Observable}.
     *
     * @param observable The {@link org.apache.commons.scxml2.model.Observable} to attach the listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final Observable observable, final SCXMLListener listener) {
        scInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener from the {@link org.apache.commons.scxml2.model.Observable}.
     *
     * @param observable The {@link org.apache.commons.scxml2.model.Observable}.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final Observable observable,
            final SCXMLListener listener) {
        scInstance.getNotificationRegistry().removeListener(observable,
            listener);
    }

    /**
     * Register an <code>Invoker</code> for this target type.
     *
     * @param type The target type (specified by "type"
     *             attribute of &lt;invoke&gt; tag).
     * @param invokerClass The <code>Invoker</code> <code>Class</code>.
     */
    public void registerInvokerClass(final String type,
            final Class<? extends Invoker> invokerClass) {
        scInstance.registerInvokerClass(type, invokerClass);
    }

    /**
     * Remove the <code>Invoker</code> registered for this target
     * type (if there is one registered).
     *
     * @param type The target type (specified by "type"
     *             attribute of &lt;invoke&gt; tag).
     */
    public void unregisterInvokerClass(final String type) {
        scInstance.unregisterInvokerClass(type);
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
            StringBuffer sb = new StringBuffer("Current States: [ ");
            for (TransitionTarget tt : currentStatus.getStates()) {
                sb.append(tt.getId()).append(", ");
            }
            int length = sb.length();
            sb.delete(length - 2, length).append(" ]");
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
        setEventData(currentStatus.getEvents().toArray(new TriggerEvent[0]));
    }

    /**
     * @param evts The events being triggered.
     * @return Object[] Previous values.
     */
    private Object[] setEventData(final TriggerEvent[] evts) {
        Context rootCtx = scInstance.getRootContext();
        Object[] oldData = { rootCtx.get(EVENT_DATA), rootCtx.get(EVENT_DATA_MAP), rootCtx.get(EVENT_VARIABLE) };
        int len = evts.length;
        if (len > 0) { // 0 has retry semantics (eg: see usage in reset())
            Object eventData = null;
            EventVariable eventVar = null;
            Map<String, Object> payloadMap = new HashMap<String, Object>();
            for (TriggerEvent te : evts) {
                payloadMap.put(te.getName(), te.getPayload());
            }
            if (len == 1) {
                // we have only one event
                eventData = evts[0].getPayload();

                // NOTE: According to spec 5.10.1, _event.type must be 'platform', 'internal' or 'external'.
                //       So, error or variable change trigger events can be translated into 'platform' type event variables.
                //       However, the Send model for <send> element doesn't support any target yet, and so
                //       'internal' type can't supported either.
                //       All the others must be 'external'.

                String eventType = EventVariable.TYPE_EXTERNAL;
                final int triggerEventType = evts[0].getType();

                if (triggerEventType == TriggerEvent.ERROR_EVENT || triggerEventType == TriggerEvent.CHANGE_EVENT) {
                    eventType = EventVariable.TYPE_PLATFORM;
                }

                // TODO: determine sendid, origin, originType and invokeid based on context later.
                eventVar = new EventVariable(evts[0].getName(), eventType, null, null, null, null, eventData);
            }
            rootCtx.setLocal(EVENT_DATA, eventData);
            rootCtx.setLocal(EVENT_DATA_MAP, payloadMap);
            rootCtx.setLocal(EVENT_VARIABLE, eventVar);
        }
        return oldData;
    }

    /**
     * @param oldData The old values to restore to.
     */
    private void restoreEventData(final Object[] oldData) {
        scInstance.getRootContext().setLocal(EVENT_DATA, oldData[0]);
        scInstance.getRootContext().setLocal(EVENT_DATA_MAP, oldData[1]);
        scInstance.getRootContext().setLocal(EVENT_VARIABLE, oldData[2]);
    }

    /**
     * The special variable for storing single event data / payload.
     * @deprecated
     */
    private static final String EVENT_DATA = "_eventdata";

    /**
     * The special variable for storing event data / payload,
     * when multiple events are triggered, keyed by event name.
     */
    // TODO: is _eventdatamap really being used somewhere?
    private static final String EVENT_DATA_MAP = "_eventdatamap";

    /**
     * The special variable for storing single event data / payload.
     */
    private static final String EVENT_VARIABLE = "_event";

    /**
     * SCXMLExecutor put into motion without setting a model (state machine).
     */
    private static final String ERR_NO_STATE_MACHINE =
        "SCXMLExecutor: State machine not set";

}

