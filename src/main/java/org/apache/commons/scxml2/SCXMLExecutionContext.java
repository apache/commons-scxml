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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.SCXML;

/**
 * SCXMLExecutionContext provides all the services and internal data used during the interpretation of an SCXML
 * statemachine across micro and macro steps
 */
public class SCXMLExecutionContext {

    /**
     * SCXML Execution Logger for the application.
     */
    private Log appLog = LogFactory.getLog(SCXMLExecutionContext.class);

    /**
     * The executor this execution context belongs to.
     */
    private final SCXMLExecutor executor;

    /**
     * The action execution context instance, providing restricted access to this execution context
     */
    private final ActionExecutionContext actionExecutionContext;

    /**
     * The internal event queue
     */
    private final Queue<TriggerEvent> internalEventQueue = new LinkedList<TriggerEvent>();

    /**
     * The map storing the unique invokeId for an Invoke with an active Invoker
     */
    private final Map<Invoke, String> invokeIds = new HashMap<Invoke, String>();

    /**
     * The Map of active Invoker, keyed by their unique invokeId.
     */
    private final Map<String, Invoker> invokers = new HashMap<String, Invoker>();

    /**
     * Running status for this state machine
     */
    private boolean running;

    /**
     * Constructor
     * @param executor the executor this execution context belongs to
     */
    SCXMLExecutionContext(SCXMLExecutor executor) {
        this.executor = executor;
        this.actionExecutionContext = new ActionExecutionContext(this);
        running = true;
    }

    /**
     * @return Returns the restricted execution context for actions
     */
    public ActionExecutionContext getActionExecutionContext() {
        return actionExecutionContext;
    }

    /**
     * @return Returns true if this state machine is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stop a running state machine
     */
    public void stopRunning() {
        this.running = false;
    }

    /**
     * Internal reset which will cancel all current active Invokers, clear the internal event queue and mark the
     * state machine process as running (again).
     */
    void reset() {
        if (!invokeIds.isEmpty()) {
            for (Invoke invoke : new ArrayList<Invoke>(invokeIds.keySet())) {
                cancelInvoker(invoke);
            }
        }
        internalEventQueue.clear();
        running = true;
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
    public SCXML getStatemachine() {
        return executor.getStateMachine();
    }

    /**
     * @return Returns the SCInstance
     */
    public SCInstance getScInstance() {
        return executor.getSCInstance();
    }

    /**
     * @return Returns The evaluator.
     */
    public Evaluator getEvaluator() {
        return executor.getEvaluator();
    }

    /**
     * @return Returns the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return executor.getErrorReporter();
    }

    /**
     * @return Returns the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return executor.getEventdispatcher();
    }

    /**
     * @return Returns the notification registry
     */
    public NotificationRegistry getNotificationRegistry() {
        return executor.getNotificationRegistry();
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
     * Create a new {@link Invoker}
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     *         invoker class is registered against that type,
     *         <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot
     *                          be instantiated.
     */
    public Invoker newInvoker(String type) throws InvokerException {
        return executor.newInvoker(type);
    }

    /**
     * Set the {@link Invoker} for a {@link Invoke} and returns the unique invokerId for the Invoker
     *
     * @param invoke The Invoke.
     * @param invoker The Invoker.
     * @return The invokeId
     */
    public String setInvoker(final Invoke invoke, final Invoker invoker) {
        String invokeId = invoke.getId();
        if (SCXMLHelper.isStringEmpty(invokeId)) {
            invokeId = UUID.randomUUID().toString();
        }
        invokeIds.put(invoke, invokeId);
        invokers.put(invokeId, invoker);
        return invokeId;
    }

    /**
     * Remove a previously active Invoker, which must already have been canceled
     * @param invoke The Invoke for the Invoker to remove
     */
    public void removeInvoker(final Invoke invoke) {
        invokers.remove(invokeIds.remove(invoke));
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
    public void cancelInvoker(Invoke invoke) {
        String invokeId = invokeIds.get(invoke);
        if (invokeId != null) {
            try {
                invokers.get(invokeId).cancel();
            } catch (InvokerException ie) {
                TriggerEvent te = new TriggerEvent(invokeId
                        + ".invoke.cancel.failed", TriggerEvent.ERROR_EVENT);
                addInternalEvent(te);
            }
            removeInvoker(invoke);
        }
    }

    /**
     * Add an event to the internal queue
     * @param event The event
     */
    public void addInternalEvent(TriggerEvent event) {
        internalEventQueue.add(event);
    }

    /**
     * @return Returns the next event from the internal queue, if available
     */
    TriggerEvent nextInternalEvent() {
        return internalEventQueue.poll();
    }
}
