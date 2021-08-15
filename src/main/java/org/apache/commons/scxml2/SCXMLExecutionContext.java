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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.env.SimpleErrorReporter;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.invoke.SimpleSCXMLInvoker;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

/**
 * SCXMLExecutionContext provides all the services and internal data used during the interpretation of an SCXML
 * statemachine across micro and macro steps
 */
public class SCXMLExecutionContext implements SCXMLIOProcessor {

    /**
     * Default and required supported SCXML Processor Invoker service URI
     */
    public static final String SCXML_INVOKER_TYPE_URI = "http://www.w3.org/TR/scxml/";
    /**
     * Alias for {@link #SCXML_INVOKER_TYPE_URI}
     */
    public static final String SCXML_INVOKER_TYPE = "scxml";

    /**
     * SCXML Execution Logger for the application.
     */
    private static final Log appLog = LogFactory.getLog(SCXMLExecutionContext.class);

    /**
     * The action execution context instance, providing restricted access to this execution context
     */
    private final ActionExecutionContext actionExecutionContext;

    /**
     * The SCXMLExecutor of this SCXMLExecutionContext
     */
    private final SCXMLExecutor scxmlExecutor;

    /**
     * The SCInstance.
     */
    private SCInstance scInstance;

    /**
     * The evaluator for expressions.
     */
    private Evaluator evaluator;

    /**
     * The external IOProcessor for Invokers to communicate back on
     */
    private final SCXMLIOProcessor externalIOProcessor;

    /**
     * The event dispatcher to interface with external documents etc.
     */
    private EventDispatcher eventdispatcher;

    /**
     * The environment specific error reporter.
     */
    private ErrorReporter errorReporter;

    /**
     * The notification registry.
     */
    private final NotificationRegistry notificationRegistry;

    /**
     * The internal event queue
     */
    private final Queue<TriggerEvent> internalEventQueue = new LinkedList<>();

    /**
     * The Invoker classes map, keyed by invoke target types (specified using "type" attribute).
     */
    private final Map<String, Class<? extends Invoker>> invokerClasses = new HashMap<>();

    /**
     * The map storing the unique invokeId for an Invoke with an active Invoker
     */
    private final Map<Invoke, String> invokeIds = new HashMap<>();

    /**
     * The Map of active Invoker, keyed by their unique invokeId.
     */
    private final Map<String, Invoker> invokers = new HashMap<>();

    /**
     * The Map of the current ioProcessors
     */
    private final Map<String, SCXMLIOProcessor> ioProcessors = new HashMap<>();

    /**
     * Flag indicating if the SCXML configuration should be checked before execution (default = true)
     */
    private boolean checkLegalConfiguration = true;

    /**
     * Local cache of the SCInstance sessionId, to be able to check against clear/reinitialization
     */
    private String sessionId;

    /**
     * Constructor
     *
     * @param scxmlExecutor The SCXMLExecutor of this SCXMLExecutionContext
     * @param evaluator The evaluator
     * @param eventDispatcher The event dispatcher, if null a SimpleDispatcher instance will be used
     * @param errorReporter The error reporter, if null a SimpleErrorReporter instance will be used
     */
    protected SCXMLExecutionContext(final SCXMLExecutor scxmlExecutor, final Evaluator evaluator,
                                    final EventDispatcher eventDispatcher, final ErrorReporter errorReporter) {
        this.scxmlExecutor = scxmlExecutor;
        this.externalIOProcessor = scxmlExecutor;
        this.evaluator = evaluator;
        this.eventdispatcher = eventDispatcher != null ? eventDispatcher : new SimpleDispatcher();
        this.errorReporter = errorReporter != null ? errorReporter : new SimpleErrorReporter();
        this.notificationRegistry = new NotificationRegistry();

        this.scInstance = new SCInstance(this, this.evaluator, this.errorReporter);
        this.actionExecutionContext = new ActionExecutionContext(this);

        ioProcessors.put(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR, new ExternalSCXMLIOProcessor(getExternalIOProcessor()));
        ioProcessors.put(SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR, ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR));
        ioProcessors.put(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR, getInternalIOProcessor());
        if (scxmlExecutor.getParentSCXMLIOProcessor() != null) {
            ioProcessors.put(SCXMLIOProcessor.PARENT_EVENT_PROCESSOR, scxmlExecutor.getParentSCXMLIOProcessor());
        }
        initializeIOProcessors();
        registerInvokerClass(SCXML_INVOKER_TYPE_URI, SimpleSCXMLInvoker.class);
        registerInvokerClass(SCXML_INVOKER_TYPE, SimpleSCXMLInvoker.class);
    }

    public SCXMLExecutor getSCXMLExecutor() {
        return scxmlExecutor;
    }

    public SCXMLIOProcessor getExternalIOProcessor() {
        return externalIOProcessor;
    }

    public SCXMLIOProcessor getInternalIOProcessor() {
        return this;
    }

    /**
     * @return Returns the restricted execution context for actions
     */
    public ActionExecutionContext getActionExecutionContext() {
        return actionExecutionContext;
    }

    /**
     * Set if the SCXML configuration should be checked before execution (default = true)
     * @param checkLegalConfiguration flag to set
     */
    public void setCheckLegalConfiguration(final boolean checkLegalConfiguration) {
        this.checkLegalConfiguration = checkLegalConfiguration;
    }

    /**
     * @return if the SCXML configuration will be checked before execution
     */
    public boolean isCheckLegalConfiguration() {
        return checkLegalConfiguration;
    }

    /**
     * Initialize will cancel all current active Invokers, clear the internal event queue and (re)initialize the state machine
     * @param data input model map to initialize the data model with
     * @throws ModelException if the state machine hasn't been setup for the internal SCXML instance
     */
    public void initialize(final Map<String, Object> data) throws ModelException {
        if (!invokeIds.isEmpty()) {
            for (final Invoke invoke : new ArrayList<>(invokeIds.keySet())) {
                cancelInvoker(invoke);
            }
        }
        internalEventQueue.clear();
        scInstance.initialize();
        initializeIOProcessors();
        scInstance.initializeDatamodel(data);
    }
    /**
     * (re)start the state machine.
     */
    public void start() {
        if (scInstance.isRunning()) {
            throw new IllegalStateException("The state machine has already started.");
        } else if (scInstance.getGlobalContext() == null) {
            throw new IllegalStateException("The state machine has not been initialized yet.");
        }
        scInstance.start();
    }

    /**
     * @return Returns true if this state machine is running
     */
    public boolean isRunning() {
        return scInstance.isRunning();
    }

    /**
     * Stop the state machine
     */
    public void stop() {
        scInstance.stop();
    }

    /**
     * @return Returns the SCXML Execution Logger for the application
     */
    public Log getAppLog() {
        return appLog;
    }

    /**
     * @return Returns the state machine
     */
    public SCXML getStateMachine() {
        return scInstance.getStateMachine();
    }

    /**
     * Set or replace the state machine to be executed
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * @param stateMachine The state machine to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    protected void setStateMachine(final SCXML stateMachine) throws ModelException {
        scInstance.setStateMachine(stateMachine);
        // synchronize possible derived evaluator
        this.evaluator = scInstance.getEvaluator();
        initializeIOProcessors();
    }

    /**
     * @return Returns the SCInstance
     */
    public SCInstance getScInstance() {
        return scInstance;
    }

    /**
     * @return Returns The evaluator.
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Set or replace the evaluator
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * @param evaluator The evaluator to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    protected void setEvaluator(final Evaluator evaluator) throws ModelException {
        scInstance.setEvaluator(evaluator, false);
        // synchronize possible derived evaluator
        this.evaluator = scInstance.getEvaluator();
        initializeIOProcessors();
    }

    /**
     * @return Returns the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Set or replace the error reporter
     *
     * @param errorReporter The error reporter to set, if null a SimpleErrorReporter instance will be used instead
     */
    protected void setErrorReporter(final ErrorReporter errorReporter) {
        this.errorReporter = errorReporter != null ? errorReporter : new SimpleErrorReporter();
        try {
            scInstance.setErrorReporter(errorReporter);
        }
        catch (final ModelException me) {
            // won't happen
        }
    }

    /**
     * @return Returns the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return eventdispatcher;
    }

    /**
     * Set or replace the event dispatch
     *
     * @param eventdispatcher The event dispatcher to set, if null a SimpleDispatcher instance will be used instead
     */
    protected void setEventdispatcher(final EventDispatcher eventdispatcher) {
        this.eventdispatcher = eventdispatcher != null ? eventdispatcher : new SimpleDispatcher();
    }

    /**
     * @return Returns the notification registry
     */
    public NotificationRegistry getNotificationRegistry() {
        return notificationRegistry;
    }

    /**
     * Initialize the _ioprocessors environment variable, which only can be done when the evaluator is available
     */
    protected void initializeIOProcessors() {
        if (scInstance.getEvaluator() != null) {
            // lazy register/reset #_scxml_sessionId event target
            final String currentSessionId = (String)getScInstance().getSystemContext().get(SCXMLSystemContext.SESSIONID_KEY);
            if (sessionId != null && !sessionId.equals(currentSessionId)) {
                // remove possible old/stale #_scxml_sessionId target
                ioProcessors.remove(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX+sessionId);
            }
            sessionId = currentSessionId;
            if (!ioProcessors.containsKey(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX+sessionId)) {
                ioProcessors.put(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX+sessionId, getExternalIOProcessor());
            }
            getScInstance().getSystemContext().setLocal(SCXMLSystemContext.IOPROCESSORS_KEY, Collections.unmodifiableMap(ioProcessors));
        }
    }

    /**
     * Detach the current SCInstance to allow external serialization.
     * <p>
     * {@link #attachInstance(SCInstance)} can be used to re-attach a previously detached instance
     * </p>
     * @return the detached instance
     */
    protected SCInstance detachInstance() {
        final SCInstance instance = scInstance;
        scInstance.detach();
        final Map<String, Object> systemVars = scInstance.getSystemContext().getVars();
        systemVars.remove(SCXMLSystemContext.IOPROCESSORS_KEY);
        systemVars.remove(SCXMLSystemContext.EVENT_KEY);
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
    protected void attachInstance(final SCInstance instance) {
        if (scInstance != null ) {
            scInstance.detach();
        }
        scInstance = instance;
        if (scInstance != null) {
            scInstance.detach();
            try {
                scInstance.setInternalIOProcessor(this);
                scInstance.setEvaluator(evaluator, true);
                scInstance.setErrorReporter(errorReporter);
                initializeIOProcessors();
            }
            catch (final ModelException me) {
                // should not happen
            }
        }
    }

    /**
     * Trivial utility method needed for SCXML IRP test 216 which (IMO incorrectly uses http://www.w3.org/TR/scxml
     * (no trailing /) while the SCXML spec explicitly states http://www.w3.org/TR/scxml/ should be used (supported)
     * @param uri
     * @return
     */
    private String stripTrailingSlash(final String uri) {
        return uri.endsWith("/") ? uri.substring(0, uri.length()-1) : uri;
    }

    /**
     * Register an Invoker for this target type.
     *
     * @param type The target type (specified by "type" attribute of the invoke element).
     * @param invokerClass The Invoker class.
     */
    protected void registerInvokerClass(final String type, final Class<? extends Invoker> invokerClass) {
        invokerClasses.put(stripTrailingSlash(type), invokerClass);
    }

    /**
     * Remove the Invoker registered for this target type (if there is one registered).
     *
     * @param type The target type (specified by "type" attribute of the invoke element).
     */
    protected void unregisterInvokerClass(final String type) {
        invokerClasses.remove(stripTrailingSlash(type));
    }

    /**
     * Create a new {@link Invoker}
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     *         invoker class is registered against that type,
     *         <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot be instantiated.
     */
    public Invoker newInvoker(final String type) throws InvokerException {
        final Class<? extends Invoker> invokerClass = invokerClasses.get(stripTrailingSlash(type));
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for type \"" + stripTrailingSlash(type) + "\"");
        }
        try {
            return invokerClass.newInstance();
        } catch (InstantiationException | IllegalAccessException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        }
    }

    /**
     * Get the {@link Invoker} for this {@link Invoke}.
     * May return <code>null</code>. A non-null {@link Invoker} will be
     * returned if and only if the {@link Invoke} parent TransitionalState is
     * currently active and contains the &lt;invoke&gt; child.
     *
     * @param invoke The <code>Invoke</code>.
     * @return The Invoker.
     */
    public Invoker getInvoker(final Invoke invoke) {
        return invokers.get(invokeIds.get(invoke));
    }

    /**
     * Register the active {@link Invoker} for a {@link Invoke}
     *
     * @param invoke The Invoke.
     * @param invoker The Invoker.
     * @throws InvokerException when the Invoker doesn't have an invokerId
     */
    public void registerInvoker(final Invoke invoke, final Invoker invoker) throws InvokerException {
        final String invokeId = invoker.getInvokeId();
        if (invokeId == null) {
            throw new InvokerException("Registering an Invoker without invokerId");
        }
        invokeIds.put(invoke, invokeId);
        invokers.put(invokeId, invoker);
        ioProcessors.put(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX+invoke.getId(), invoker.getChildIOProcessor());
        initializeIOProcessors();
    }

    /**
     * Remove a previously active Invoker, which must already have been canceled
     * @param invoke The Invoke for the Invoker to remove
     */
    public void removeInvoker(final Invoke invoke) {
        invokers.remove(invokeIds.remove(invoke));
        ioProcessors.remove(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX+invoke.getId());
        initializeIOProcessors();
    }

    /**
     * @return Returns the map of current active Invokes and their invokeId
     */
    public Map<Invoke, String> getInvokeIds() {
        return invokeIds;
    }


    /**
     * Cancel and remove an active Invoker
     *
     * @param invoke The Invoke for the Invoker to cancel
     */
    public void cancelInvoker(final Invoke invoke) {
        final String invokeId = invokeIds.get(invoke);
        if (invokeId != null) {
            try {
                invokers.get(invokeId).cancel();
            } catch (final InvokerException ie) {
                final TriggerEvent te = new EventBuilder("failed.invoke.cancel."+invokeId, TriggerEvent.ERROR_EVENT).build();
                addEvent(te);
            }
            removeInvoker(invoke);
        }
    }

    /**
     * Add an event to the internal event queue
     * @param event The event
     */
    @Override
    public void addEvent(final TriggerEvent event) {
        internalEventQueue.add(event);
    }

    /**
     * @return Returns the next event from the internal event queue, if available
     */
    public TriggerEvent nextInternalEvent() {
        return internalEventQueue.poll();
    }

    /**
     * @return Returns true if the internal event queue isn't empty
     */
    public boolean hasPendingInternalEvent() {
        return !internalEventQueue.isEmpty();
    }
}
