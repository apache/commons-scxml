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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.Invoke;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.model.TransitionalState;

/**
 * The <code>SCInstance</code> performs book-keeping functions for
 * a particular execution of a state chart represented by a
 * <code>SCXML</code> object.
 */
public class SCInstance implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The notification registry.
     */
    private NotificationRegistry notificationRegistry;

    /**
     * The <code>Map</code> of <code>Context</code>s per
     * <code>EnterableState</code>.
     */
    private final Map<EnterableState, Context> contexts;

    /**
     * The <code>Map</code> of last known configurations per
     * <code>History</code>.
     */
    private final Map<History, Set<EnterableState>> histories;

    /**
     * <code>Map</code> for recording the run to completion status of
     * composite states.
     */
    private final Map<EnterableState, Boolean> completions;

    /**
     * The <code>Invoker</code> classes <code>Map</code>, keyed by
     * &lt;invoke&gt; target types (specified using "type" attribute).
     */
    private final Map<String, Class<? extends Invoker>> invokerClasses;

    private final Map<Invoke, String> invokeIds;
    /**
     * The <code>Map</code> of active <code>Invoker</code>s, keyed by
     * their <code>invokeId</code>.
     */
    private final Map<String, Invoker> invokers;

    /**
     * The evaluator for expressions.
     */
    private Evaluator evaluator;

    /**
     * The root context.
     */
    private Context rootContext;

    /**
     * The initial script context
     */
    private Context globalScriptContext;

    /**
     * The owning state machine executor.
     */
    private SCXMLExecutor executor;

    /**
     * Constructor.
     *
     * @param executor The executor that this instance is attached to.
     */
    SCInstance(final SCXMLExecutor executor) {
        this.notificationRegistry = new NotificationRegistry();
        this.contexts = Collections.synchronizedMap(new HashMap<EnterableState, Context>());
        this.histories = Collections.synchronizedMap(new HashMap<History, Set<EnterableState>>());
        this.invokerClasses = Collections.synchronizedMap(new HashMap<String, Class<? extends Invoker>>());
        this.invokeIds = Collections.synchronizedMap(new HashMap<Invoke, String>());
        this.invokers = Collections.synchronizedMap(new HashMap<String, Invoker>());
        this.completions = Collections.synchronizedMap(new HashMap<EnterableState, Boolean>());
        this.evaluator = null;
        this.rootContext = null;
        this.executor = executor;
    }

    /**
     * Get the <code>Evaluator</code>.
     *
     * @return The evaluator.
     */
    public Evaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Set the <code>Evaluator</code>.
     *
     * @param evaluator The evaluator.
     */
    void setEvaluator(final Evaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Get the root context.
     *
     * @return The root context.
     */
    public Context getRootContext() {
        if (rootContext == null && evaluator != null) {
            rootContext = evaluator.newContext(null);
        }
        return rootContext;
    }

    /**
     * Set the root context.
     *
     * @param context The root context.
     */
    void setRootContext(final Context context) {
        this.rootContext = context;
    }

    public Context getGlobalScriptContext() {
        if (globalScriptContext == null) {
            Context rootContext = getRootContext();
            if (rootContext != null) {
                globalScriptContext = evaluator.newContext(getRootContext());
            }
        }
        return globalScriptContext;
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
     * Get the <code>Context</code> for this <code>EnterableState</code>.
     * If one is not available it is created.
     *
     * @param state The EnterableState.
     * @return The Context.
     */
    public Context getContext(final EnterableState state) {
        Context context = contexts.get(state);
        if (context == null) {
            EnterableState parent = state.getParent();
            if (parent == null) {
                // docroot
                context = evaluator.newContext(getGlobalScriptContext());
            } else {
                context = evaluator.newContext(getContext(parent));
            }
            if (state instanceof TransitionalState) {
                Datamodel datamodel = ((TransitionalState)state).getDatamodel();
                SCXMLHelper.cloneDatamodel(datamodel, context, evaluator, null);
            }
            contexts.put(state, context);
        }
        return context;
    }

    /**
     * Get the <code>Context</code> for this <code>EnterableState</code>.
     * May return <code>null</code>.
     *
     * @param state The <code>EnterableState</code>.
     * @return The Context.
     */
    Context lookupContext(final EnterableState state) {
        return contexts.get(state);
    }

    /**
     * Set the <code>Context</code> for this <code>EnterableState</code>.
     *
     * @param state The EnterableState.
     * @param context The Context.
     */
    void setContext(final EnterableState state,
            final Context context) {
        contexts.put(state, context);
    }

    /**
     * Get the last configuration for this history.
     *
     * @param history The history.
     * @return Returns the lastConfiguration.
     */
    public Set<EnterableState> getLastConfiguration(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        if (lastConfiguration == null) {
            lastConfiguration = new HashSet<EnterableState>();
            histories.put(history, lastConfiguration);
        }
        return lastConfiguration;
    }

    /**
     * Set the last configuration for this history.
     *
     * @param history The history.
     * @param lc The lastConfiguration to set.
     */
    public void setLastConfiguration(final History history,
            final Set<EnterableState> lc) {
        Set<EnterableState> lastConfiguration = getLastConfiguration(history);
        lastConfiguration.clear();
        lastConfiguration.addAll(lc);
    }

    /**
     * Check whether we have prior history.
     *
     * @param history The history.
     * @return Whether we have a non-empty last configuration
     */
    public boolean isEmpty(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        return lastConfiguration == null || lastConfiguration.isEmpty();
    }

    /**
     * Resets the history state.
     *
     * @param history The history.
     * @see org.apache.commons.scxml2.SCXMLExecutor#reset()
     */
    public void reset(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        if (lastConfiguration != null) {
            lastConfiguration.clear();
        }
    }

    /**
     * Get the {@link SCXMLExecutor} this instance is attached to.
     *
     * @return The SCXMLExecutor this instance is attached to.
     * @see org.apache.commons.scxml2.SCXMLExecutor
     */
    public SCXMLExecutor getExecutor() {
        return executor;
    }

    /**
     * Register an {@link Invoker} class for this target type.
     *
     * @param type The target type (specified by "type" attribute of
     *             &lt;invoke&gt; tag).
     * @param invokerClass The <code>Invoker</code> <code>Class</code>.
     */
    void registerInvokerClass(final String type,
            final Class<? extends Invoker> invokerClass) {
        invokerClasses.put(type, invokerClass);
    }

    /**
     * Remove the {@link Invoker} class registered for this target
     * type (if there is one registered).
     *
     * @param type The target type (specified by "type" attribute of
     *             &lt;invoke&gt; tag).
     */
    void unregisterInvokerClass(final String type) {
        invokerClasses.remove(type);
    }

    /**
     * Get the {@link Invoker} for this {@link TransitionTarget}.
     * May return <code>null</code>. A non-null <code>Invoker</code> will be
     * returned if and only if the <code>TransitionTarget</code> is
     * currently active and contains an &lt;invoke&gt; child.
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     *         invoker class is registered against that type,
     *         <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot
     *                          be instantiated.
     */
    public Invoker newInvoker(final String type)
    throws InvokerException {
        Class<? extends Invoker> invokerClass = invokerClasses.get(type);
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for type \""
                + type + "\"");
        }
        Invoker invoker;
        try {
            invoker = invokerClass.newInstance();
        } catch (InstantiationException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iae) {
            throw new InvokerException(iae.getMessage(), iae.getCause());
        }
        return invoker;
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
     * Get the completion status for this composite
     * {@link EnterableState}.
     *
     * @param state The <code>EnterableState</code>.
     * @return The completion status.
     *
     * @since 0.7
     */
    @SuppressWarnings("boxing")
    public boolean isDone(final EnterableState state) {
        if (completions.containsKey(state)) {
            return completions.get(state);
        }
        return false;
    }

    /**
     * Set the completion status for this composite
     * {@link EnterableState}.
     *
     * @param state The EnterableState.
     * @param done The completion status.
     *
     * @since 0.7
     */
    @SuppressWarnings("boxing")
    public void setDone(final EnterableState state,
            final boolean done) {
        completions.put(state, done);
    }
}

