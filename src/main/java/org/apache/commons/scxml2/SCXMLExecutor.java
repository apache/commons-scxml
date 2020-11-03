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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.Observable;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionTarget;
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
     * The Logger for the SCXMLExecutor.
     */
    private static final Log log = LogFactory.getLog(SCXMLExecutor.class);

    /**
     * Parent SCXMLIOProcessor
     */
    private ParentSCXMLIOProcessor parentSCXMLIOProcessor;

    /**
     *  Interpretation semantics.
     */
    private final SCXMLSemantics semantics;

    /**
     * The state machine execution context
     */
    private final SCXMLExecutionContext exctx;

    /**
     * The external event queue
     */
    private final Queue<TriggerEvent> externalEventQueue = new ConcurrentLinkedQueue<>();

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
     * Constructor using a parent SCXMLExecutor
     *
     * @param parentSCXMLExecutor the parent SCXMLExecutor
     * @param invokeId SCXML invoke id
     * @param scxml {@link SCXML} instance
     * @throws ModelException if the internal {@link SCInstance} is already initialized
     */
    public SCXMLExecutor(final SCXMLExecutor parentSCXMLExecutor, final String invokeId, final SCXML scxml) throws ModelException {
        this.parentSCXMLIOProcessor = new ParentSCXMLIOProcessor(parentSCXMLExecutor, invokeId);
        this.semantics = parentSCXMLExecutor.semantics;
        this.exctx = new SCXMLExecutionContext(this, parentSCXMLExecutor.getEvaluator(),
                parentSCXMLExecutor.getEventdispatcher().newInstance(), parentSCXMLExecutor.getErrorReporter());
        getSCInstance().setSingleContext(parentSCXMLExecutor.isSingleContext());
        getSCInstance().setStateMachine(scxml);
    }

    /**
     * @return the parent SCXMLIOProcessor (if any)
     */
    public ParentSCXMLIOProcessor getParentSCXMLIOProcessor() {
        return parentSCXMLIOProcessor;
    }

    /**
     * Get the current state machine instance status.
     *
     * @return The current Status
     */
    public synchronized Status getStatus() {
        return exctx.getScInstance().getCurrentStatus();
    }

    /**
     * @return the (optionally) &lt;final&gt;&lt;donedata/&gt;&lt;/final&gt; produced data after the current statemachine
     *         completed its execution.
     */
    public Object getFinalDoneData() {
        return getGlobalContext().getSystemContext().getPlatformVariables().get(SCXMLSystemContext.FINAL_DONE_DATA_KEY);
    }

    /**
     * starts the state machine with a specific active configuration, as the result of a (first) step
     * <p>
     * This will first (re)initialize the current state machine: clearing all variable contexts, histories and current
     * status, and clones the SCXML root datamodel into the root context.
     * </p>
     * @param atomicStateIds The set of atomic state ids for the state machine
     * @throws ModelException when the state machine hasn't been properly configured yet, when an unknown or illegal
     * stateId is specified, or when the specified active configuration does not represent a legal configuration.
     * @see SCInstance#initialize()
     * @see SCXMLSemantics#isLegalConfiguration(java.util.Set, ErrorReporter)
     */
    public synchronized void setConfiguration(Set<String> atomicStateIds) throws ModelException {
        semantics.initialize(exctx, Collections.emptyMap());
        Set<EnterableState> states = new HashSet<>();
        for (String stateId : atomicStateIds) {
            TransitionTarget tt = getStateMachine().getTargets().get(stateId);
            if (tt instanceof EnterableState && ((EnterableState)tt).isAtomicState()) {
                EnterableState es = (EnterableState)tt;
                while (es != null && !states.add(es)) {
                    es = es.getParent();
                }
            }
            else {
                throw new ModelException("Illegal atomic stateId "+stateId+": state unknown or not an atomic state");
            }
        }
        if (semantics.isLegalConfiguration(states, getErrorReporter())) {
            for (EnterableState es : states) {
                exctx.getScInstance().getStateConfiguration().enterState(es);
            }
            logState();
        }
        else {
            throw new ModelException("Illegal state machine configuration!");
        }
        exctx.start();
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
     * <p>
     * The root context can be used for providing external data to the state machine
     * </p>
     *
     * @return Context The root context.
     */
    public Context getRootContext() {
        return exctx.getScInstance().getRootContext();
    }

    /**
     * Get the global context for the state machine execution.
     * <p>
     * The global context is the top level context within the state machine itself and should be regarded and treated
     * "read-only" from external usage.
     * </p>
     * @return Context The global context.
     */
    public Context getGlobalContext() {
        return exctx.getScInstance().getGlobalContext();
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

    public void setSingleContext(boolean singleContext) throws ModelException {
        getSCInstance().setSingleContext(singleContext);
    }

    public boolean isSingleContext() {
        return getSCInstance().isSingleContext();
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
     * Set if the SCXML configuration should be checked before execution (default = true)
     * @param checkLegalConfiguration flag to set
     */
    public void setCheckLegalConfiguration(boolean checkLegalConfiguration) {
        this.exctx.setCheckLegalConfiguration(checkLegalConfiguration);
    }

    /**
     * @return if the SCXML configuration will be checked before execution
     */
    public boolean isCheckLegalConfiguration() {
        return exctx.isCheckLegalConfiguration();
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

    public void go() throws ModelException {
        go(Collections.emptyMap());
    }

    /**
     * Clear all state, optionally initialize/override global context data, and begin executing the state machine
     * @param data optional data to initialize/override data defined (only) in the global context of the state machine
     * @throws ModelException if the state machine instance failed to initialize
     */
    public void go(final Map<String, Object> data) throws ModelException {
        // first stop the state machine (flag only, otherwise start may fail hereafter)
        exctx.stop();
        // clear any pending external events
        externalEventQueue.clear();

        // (re)initialize
        semantics.initialize(exctx, data);

        // begin
        semantics.firstStep(exctx);
        logState();
    }

    /**
     * Same as {@link #go}
     * @throws ModelException if the state machine instance failed to initialize
     */
    public void reset() throws ModelException {
        go();
    }

    public Thread run() throws ModelException {
        return run(Collections.emptyMap());
    }

    public Thread run(final Map<String, Object> data) throws ModelException {
        go(data);
        final Thread t = new Thread(() -> {
            try {
                while (exctx.isRunning()) {
                    triggerEvents();
                }
            } catch (final Exception ignored) {
            }
        }, getStateMachine().getName() + '-' + getClass().getSimpleName());
        t.start();
        return t;
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
        addEvent(evt);
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
                addEvent(evt);
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
        Thread.yield();
    }

    protected void eventStep(TriggerEvent event) throws ModelException {
        semantics.nextStep(exctx, event);
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
            for (EnterableState es : getStatus().getStates()) {
                sb.append(es.getId()).append(", ");
            }
            int length = sb.length();
            sb.delete(length - 2, length).append(" ]");
            log.debug(sb.toString());
        }
    }
}
