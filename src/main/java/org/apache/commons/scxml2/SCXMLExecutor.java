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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.Observable;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl;

/**
 * <p>The SCXML &quot;engine&quot; that executes SCXML documents. The
 * particular semantics used by this engine for executing the SCXML are
 * encapsulated in the SCXMLSemantics implementation that it uses.</p>
 *
 * <p>The default implementation is
 * <code>org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl</code></p>
 *
 * <p>The executor uses SCXMLExecutionContext to manage the state and
 * provide all the services to the SCXMLSemantics implementation.</p>
 *
 * @see SCXMLSemantics
 */
public class SCXMLExecutor implements SCXMLIOProcessor {

    /**
     * SCXMLExecutor put into motion without setting a model (state machine).
     */
    private static final String ERR_NO_STATE_MACHINE = "SCXMLExecutor: State machine not set";

    /**
     * The Logger for the SCXMLExecutor.
     */
    private Log log = LogFactory.getLog(SCXMLExecutor.class);

    /**
     *  Interpretation semantics.
     */
    private SCXMLSemantics semantics;

    /**
     * The state machine execution context
     */
    private SCXMLExecutionContext exctx;

    /**
     * The external event queue
     */
    private final Queue<TriggerEvent> externalEventQueue = new ConcurrentLinkedQueue<TriggerEvent>();

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
        this.semantics = semantics != null ? semantics : new SCXMLSemanticsImpl();
        this.exctx = new SCXMLExecutionContext(this, expEvaluator, evtDisp, errRep);
    }

    /**
     * Get the current state machine instance status.
     *
     * @return The current Status
     */
    public synchronized Status getCurrentStatus() {
        return exctx.getScInstance().getCurrentStatus();
    }

    /**
     * Set or replace the expression evaluator
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * <p>
     * Also the external event queue will be cleared.
     * </p>
     * @param evaluator The evaluator to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    public void setEvaluator(final Evaluator evaluator) throws ModelException {
        exctx.setEvaluator(evaluator);
    }

    /**
     * Get the expression evaluator in use.
     *
     * @return Evaluator The evaluator in use.
     */
    public Evaluator getEvaluator() {
        return exctx.getEvaluator();
    }

    /**
     * Get the root context for the state machine execution.
     *
     * @return Context The root context.
     */
    public Context getRootContext() {
        return exctx.getScInstance().getRootContext();
    }

    /**
     * Set the root context for the state machine execution.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param rootContext The Context that ties to the host environment.
     */
    public void setRootContext(final Context rootContext) {
        exctx.getScInstance().setRootContext(rootContext);
    }

    /**
     * Get the state machine that is being executed.
     * <b>NOTE:</b> This is the state machine definition or model used by this
     * executor instance. It may be shared across multiple executor instances
     * and should not be altered once in use. Also note that
     * manipulation of instance data for the executor should happen through
     * its root context or state contexts only, never through the direct
     * manipulation of any {@link org.apache.commons.scxml2.model.Datamodel}s associated with this state
     * machine definition.
     *
     * @return Returns the stateMachine.
     */
    public SCXML getStateMachine() {
        return exctx.getStateMachine();
    }

    /**
     * Set or replace the state machine to be executed
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * <p>
     * Also the external event queue will be cleared.
     * </p>
     * @param stateMachine The state machine to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    public void setStateMachine(final SCXML stateMachine) throws ModelException {
        exctx.setStateMachine(semantics.normalizeStateMachine(stateMachine, exctx.getErrorReporter()));
        externalEventQueue.clear();
    }

    /**
     * Get the environment specific error reporter.
     *
     * @return Returns the errorReporter.
     */
    public ErrorReporter getErrorReporter() {
        return exctx.getErrorReporter();
    }

    /**
     * Set or replace the error reporter
     *
     * @param errorReporter The error reporter to set, if null a SimpleErrorReporter instance will be used instead
     */
    public void setErrorReporter(final ErrorReporter errorReporter) {
        exctx.setErrorReporter(errorReporter);
    }

    /**
     * Get the event dispatcher.
     *
     * @return Returns the eventdispatcher.
     */
    public EventDispatcher getEventdispatcher() {
        return exctx.getEventDispatcher();
    }

    /**
     * Set or replace the event dispatch
     *
     * @param eventdispatcher The event dispatcher to set, if null a SimpleDispatcher instance will be used instead
     */
    public void setEventdispatcher(final EventDispatcher eventdispatcher) {
        exctx.setEventdispatcher(eventdispatcher);
    }

    /**
     * Get the notification registry.
     *
     * @return The notification registry.
     */
    public NotificationRegistry getNotificationRegistry() {
        return exctx.getNotificationRegistry();
    }

    /**
     * Add a listener to the {@link Observable}.
     *
     * @param observable The {@link Observable} to attach the listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final Observable observable, final SCXMLListener listener) {
        exctx.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener from the {@link Observable}.
     *
     * @param observable The {@link Observable}.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final Observable observable,
                               final SCXMLListener listener) {
        exctx.getNotificationRegistry().removeListener(observable, listener);
    }

    /**
     * Register an Invoker for this target type.
     *
     * @param type The target type (specified by "type" attribute of the invoke element).
     * @param invokerClass The Invoker class.
     */
    public void registerInvokerClass(final String type, final Class<? extends Invoker> invokerClass) {
        exctx.registerInvokerClass(type, invokerClass);
    }

    /**
     * Remove the Invoker registered for this target type (if there is one registered).
     *
     * @param type The target type (specified by "type" attribute of the invoke element).
     */
    public void unregisterInvokerClass(final String type) {
        exctx.unregisterInvokerClass(type);
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
        return exctx.detachInstance();
    }

    /**
     * Re-attach a previously detached SCInstance.
     * <p>
     * Note: an already attached instance will get overwritten (and thus lost).
     * </p>
     * @param instance An previously detached SCInstance
     */
    public void attachInstance(SCInstance instance) {
        exctx.attachInstance(instance);
    }

    /**
     * @return Returns true if the state machine is running
     */
    public boolean isRunning() {
        return exctx.isRunning();
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
     * Clear all state and begin executing the state machine
     *
     * @throws ModelException if the state machine instance failed to initialize
     */
    public void reset() throws ModelException {
        // clear any pending external events
        externalEventQueue.clear();

        // go
        semantics.firstStep(exctx);

        if (!exctx.isRunning()) {
            semantics.finalStep(exctx);
        }

        logState();
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
     * @return Returns the current number of pending external events to be processed.
     */
    public int getPendingEvents() {
        return externalEventQueue.size();
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
     * Trigger all pending and incoming events, until there are no more pending events
     * @throws ModelException in case there is a fatal SCXML object model problem.
     */
    public void triggerEvents() throws ModelException {
        TriggerEvent evt;
        while (exctx.isRunning() && (evt = externalEventQueue.poll()) != null) {
            eventStep(evt);
        }
    }

    protected void eventStep(TriggerEvent event) throws ModelException {
        semantics.nextStep(exctx, event);

        if (!exctx.isRunning()) {
            semantics.finalStep(exctx);
        }

        logState();
    }

    /**
     * Get the state chart instance for this executor.
     *
     * @return The SCInstance for this executor.
     */
    protected SCInstance getSCInstance() {
        return exctx.getScInstance();
    }

    /**
     * Log the current set of active states.
     */
    protected void logState() {
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
}

