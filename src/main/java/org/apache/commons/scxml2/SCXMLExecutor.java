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
package org.apache.commons.scxml2;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.Observable;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.model.TransitionalState;
import org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl;
import org.apache.commons.scxml2.system.EventVariable;

/**
 * <p>The SCXML &quot;engine&quot; that executes SCXML documents. The
 * particular semantics used by this engine for executing the SCXML are
 * encapsulated in the SCXMLSemantics implementation that it uses.</p>
 *
 * <p>The default implementation is
 * <code>org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl</code></p>
 *
 * @see SCXMLSemantics
 */
@SuppressWarnings("unused") // TODO: remove again after refactoring is done
public class SCXMLExecutor {

    /**
     * SCXMLExecutor put into motion without setting a model (state machine).
     */
    private static final String ERR_NO_STATE_MACHINE = "SCXMLExecutor: State machine not set";

    /**
     * The Logger for the SCXMLExecutor.
     */
    private Log log = LogFactory.getLog(SCXMLExecutor.class);

    /**
     * The evaluator for expressions.
     */
    private Evaluator evaluator;

    /**
     * The event dispatcher to interface with external documents etc.
     */
    private EventDispatcher eventdispatcher;

    /**
     * The environment specific error reporter.
     */
    private ErrorReporter errorReporter = null;

    /**
     * The notification registry.
     */
    private NotificationRegistry notificationRegistry;

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
     * The external event queue
     */
    private final Queue<TriggerEvent> externalEventQueue = new ConcurrentLinkedQueue<TriggerEvent>();

    /**
     * The <code>Invoker</code> classes <code>Map</code>, keyed by
     * &lt;invoke&gt; target types (specified using "type" attribute).
     */
    private final Map<String, Class<? extends Invoker>> invokerClasses;

    /**
     * The state machine execution context
     */
    private SCXMLExecutionContext exctx;

    /**
     * Get the state chart instance for this executor.
     *
     * @return The SCInstance for this executor.
     */
    SCInstance getSCInstance() {
        return scInstance;
    }

    /**
     * Detach the current SCInstance to allow external serialization.
     * <p>
     * {@link #attachInstance(SCInstance)} can be used to re-attach a previously detached instance
     * </p>
     * <p>
     * Note: until an instance is re-attached, no operations are allowed (and probably throw exceptions) except
     * for {@link #addEvent(TriggerEvent)} which might still be used (concurrently) by running Invokers, or
     * {@link #hasPendingEvents()} to check for possible pending events.
     * </p>
     * @return the detached instance
     */
    public SCInstance detachInstance() {
        SCInstance instance = scInstance;
        scInstance.setExecutor(null);
        scInstance = null;
        return instance;
    }

    /**
     * Re-attach a previously detached SCInstance.
     * <p>
     * Note: an already attached instance will get overwritten (and thus lost).
     * </p>
     * @param instance An previously detached SCInstance
     */
    public void attachInstance(SCInstance instance) {
        if (scInstance != null ) {
            scInstance.setExecutor(null);
        }
        scInstance = instance;
        if (scInstance != null) {
            scInstance.setExecutor(this);
        }
    }

    /**
     * Log the current set of active states.
     */
    private void logState() {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Current States: [ ");
            for (EnterableState es : getCurrentStatus().getStates()) {
                sb.append(es.getId()).append(", ");
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
        getCurrentStatus().getStates().clear();
        getCurrentStatus().getStates().addAll(step.getAfterStatus().getStates());
        scInstance.getSystemContext().setLocal(SCXMLSystemContext.VARIABLE_ALL_STATES, getCurrentStatus().getAllStates());
    }

    /**
     * @param evt The event being triggered.
     */
    private void setSystemEventVariable(final TriggerEvent evt, boolean internalQueue) {
        Context systemContext = scInstance.getSystemContext();
        EventVariable eventVar = null;
        if (evt != null) {
            String eventType = internalQueue ? EventVariable.TYPE_INTERNAL : EventVariable.TYPE_EXTERNAL;

            final int triggerEventType = evt.getType();
            if (triggerEventType == TriggerEvent.ERROR_EVENT || triggerEventType == TriggerEvent.CHANGE_EVENT) {
                eventType = EventVariable.TYPE_PLATFORM;
            }

            // TODO: determine sendid, origin, originType and invokeid based on context later.
            eventVar = new EventVariable(evt.getName(), eventType, null, null, null, null, evt.getPayload());
        }
        systemContext.setLocal(SCXMLSystemContext.VARIABLE_EVENT, eventVar);
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
     */
    public SCXMLExecutor(final Evaluator expEvaluator,
                         final EventDispatcher evtDisp, final ErrorReporter errRep) {
        this(expEvaluator, evtDisp, errRep, null);
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
        this.evaluator = expEvaluator;
        this.eventdispatcher = evtDisp;
        this.errorReporter = errRep;
        if (semantics == null) {
            // Use default semantics, if none provided
            this.semantics = new SCXMLSemanticsImpl();
        } else {
            this.semantics = semantics;
        }
        this.scInstance = new SCInstance(this);
        this.notificationRegistry = new NotificationRegistry();
        this.invokerClasses = new HashMap<String, Class<? extends Invoker>>();
        this.exctx = new SCXMLExecutionContext(this);
    }

    /**
     * Get the current status.
     *
     * @return The current Status
     */
    public synchronized Status getCurrentStatus() {
        return scInstance.getCurrentStatus();
    }

    /**
     * Set the expression evaluator.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param evaluator The evaluator to set.
     */
    public void setEvaluator(final Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Get the expression evaluator in use.
     *
     * @return Evaluator The evaluator in use.
     */
    public Evaluator getEvaluator() {
        return evaluator;
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
     * manipulation of any {@link Datamodel}s associated with this state
     * machine definition.
     *
     * @return Returns the stateMachine.
     */
    public SCXML getStateMachine() {
        return scInstance.getStateMachine();
    }

    /**
     * Set the state machine to be executed.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param stateMachine The stateMachine to set.
     */
    public void setStateMachine(final SCXML stateMachine) {
        exctx.reset();
        scInstance.setStateMachine(semantics.normalizeStateMachine(stateMachine, errorReporter));
        externalEventQueue.clear();
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
     * Get the notification registry.
     *
     * @return The notification registry.
     */
    public NotificationRegistry getNotificationRegistry() {
        return notificationRegistry;
    }

    /**
     * Set the notification registry.
     *
     * @param notifRegistry The notification registry.
     */
    @SuppressWarnings("unused")
    void setNotificationRegistry(final NotificationRegistry notifRegistry) {
        this.notificationRegistry = notifRegistry;
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
     * Clear all state and begin from &quot;initialstate&quot; indicated
     * on root SCXML element.
     *
     * @throws ModelException in case there is a fatal SCXML object
     *         model problem.
     */
    public synchronized void reset() throws ModelException {
        if (getStateMachine() == null) {
            log.error(ERR_NO_STATE_MACHINE);
            throw new ModelException(ERR_NO_STATE_MACHINE);
        }
        // Reset all variable contexts
        exctx.reset();
        Context rootContext = scInstance.getRootContext();
        // Clone root datamodel
        SCXML stateMachine = getStateMachine();
        Datamodel rootdm = stateMachine.getDatamodel();
        SCXMLHelper.cloneDatamodel(rootdm, rootContext, getEvaluator(), log);
        if (scInstance.getGlobalContext() != null) {
            scInstance.getGlobalContext().reset();
        }
        // all states and parallels, only states have variable contexts
        for (TransitionTarget tt : stateMachine.getTargets().values()) {
            if (tt instanceof EnterableState) {
                Context context = scInstance.lookupContext((EnterableState)tt);
                if (context != null) {
                    context.reset();
                    if (tt instanceof TransitionalState) {
                        Datamodel dm = ((TransitionalState)tt).getDatamodel();
                        SCXMLHelper.cloneDatamodel(dm, context, getEvaluator(), log);
                    }
                }
            } else if (tt instanceof History) {
                scInstance.reset((History) tt);
            }
        }
        // Clear currentStatus
        getCurrentStatus().getStates().clear();
        Step step = new Step(null, getCurrentStatus());
        // execute global script if defined
        semantics.executeGlobalScript(exctx, step);
        // DetermineInitialStates
        semantics.determineInitialStates(exctx, step);
        // enter initial states
        semantics.enterStates(exctx, step);
        // AssignCurrentStatus
        updateStatus(step);
        // Execute Immediate Transitions

        TriggerEvent event = exctx.nextInternalEvent();

        if (event != null) {
            handleEvent(event);
        } else {
            // InitiateInvokes only after state machine has stabilized
            semantics.initiateInvokes(this, exctx, step);
            logState();
        }
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
        if (evt != null) {
            externalEventQueue.add(evt);
        }
        triggerEvents();
    }

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
    public void triggerEvents(final TriggerEvent[] evts)
            throws ModelException {
        if (evts != null) {
            for (TriggerEvent evt : evts) {
                if (evt != null) {
                    externalEventQueue.add(evt);
                }
            }
        }
        triggerEvents();
    }

    /**
     * Add a new external event, which may be done concurrently, and even when the current SCInstance is detached.
     * <p>
     * No processing of the vent will be done, until the next triggerEvent methods is invoked.
     * </p>
     * @param evt an external event
     */
    public void addEvent(final TriggerEvent evt) {
        if (evt != null) {
            externalEventQueue.add(evt);
        }
    }

    /**
     * @return Returns true if there are pending external events to be processed.
     */
    public boolean hasPendingEvents() {
        return !externalEventQueue.isEmpty();
    }

    /**
     * Trigger all pending and incoming events, until there are no more pending events
     * @throws ModelException in case there is a fatal SCXML object model problem.
     */
    public void triggerEvents() throws ModelException {
        TriggerEvent evt;
        while ((evt = externalEventQueue.poll()) != null) {
            // Forward events (external only) to any existing invokes,
            // and finalize processing
            semantics.processInvokes(exctx, evt);
            handleEvent(evt);
        }
    }

    /**
     * The internal worker method for handling the next external event
     * @param evt an external event
     * @throws ModelException in case there is a fatal SCXML object model problem.
     */
    protected void handleEvent(final TriggerEvent evt)
            throws ModelException {
        TriggerEvent event = evt;

        Step step;

        boolean internalQueue = false;

        do {
            setSystemEventVariable(event, internalQueue);

            // CreateStep
            step = new Step(event, getCurrentStatus());
            // EnumerateReachableTransitions
            semantics.enumerateReachableTransitions(exctx, step);
            // FilterTransitionSet
            semantics.filterTransitionsSet(exctx, step);
            // FollowTransitions
            semantics.followTransitions(exctx, step);
            // UpdateHistoryStates
            semantics.updateHistoryStates(exctx, step);
            // ExecuteActions
            semantics.executeActions(exctx, step);
            // AssignCurrentStatus
            updateStatus(step);

            internalQueue = true;
            event = exctx.nextInternalEvent();

        } while (event != null);

        // InitiateInvokes only after state machine has stabilized
        semantics.initiateInvokes(this, exctx, step);

        logState();
    }

    /**
     * Add a listener to the {@link Observable}.
     *
     * @param observable The {@link Observable} to attach the listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final Observable observable, final SCXMLListener listener) {
        notificationRegistry.addListener(observable, listener);
    }

    /**
     * Remove this listener from the {@link Observable}.
     *
     * @param observable The {@link Observable}.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final Observable observable,
            final SCXMLListener listener) {
        notificationRegistry.removeListener(observable, listener);
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
        invokerClasses.put(type, invokerClass);
    }

    /**
     * Remove the <code>Invoker</code> registered for this target
     * type (if there is one registered).
     *
     * @param type The target type (specified by "type"
     *             attribute of &lt;invoke&gt; tag).
     */
    public void unregisterInvokerClass(final String type) {
        invokerClasses.remove(type);
    }

    /**
     * Create a new {@link Invoker}
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     *         invoker class is registered against that type,
     *         <code>null</code> otherwise.
     * @throws org.apache.commons.scxml2.invoke.InvokerException When a suitable {@link Invoker} cannot
     *                          be instantiated.
     */
    Invoker newInvoker(final String type)
            throws InvokerException {
        Class<? extends Invoker> invokerClass = invokerClasses.get(type);
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for type \""
                    + type + "\"");
        }
        try {
            return invokerClass.newInstance();
        } catch (InstantiationException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iae) {
            throw new InvokerException(iae.getMessage(), iae.getCause());
        }
    }
}

