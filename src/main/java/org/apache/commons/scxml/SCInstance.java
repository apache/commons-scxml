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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml.invoke.Invoker;
import org.apache.commons.scxml.invoke.InvokerException;
import org.apache.commons.scxml.model.Datamodel;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * The <code>SCInstance</code> performs book-keeping functions for
 * a particular execution of a state chart represented by a
 * <code>SCXML</code> object.
 */
public class SCInstance implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The notification registry.
     */
    private NotificationRegistry notificationRegistry;

    /**
     * The <code>Map</code> of <code>Context</code>s per
     * <code>TransitionTarget</code>.
     */
    private Map contexts;

    /**
     * The <code>Map</code> of last known configurations per
     * <code>History</code>.
     */
    private Map histories;

    /**
     * <code>Map</code> for recording the run to completion status of
     * composite states.
     */
    private Map completions;

    /**
     * The <code>Invoker</code> classes <code>Map</code>, keyed by
     * &lt;invoke&gt; target types (specified using "targettype" attribute).
     */
    private Map invokerClasses;

    /**
     * The <code>Map</code> of active <code>Invoker</code>s, keyed by
     * (leaf) <code>State</code>s.
     */
    private Map invokers;

    /**
     * The evaluator for expressions.
     */
    private Evaluator evaluator;

    /**
     * The root context.
     */
    private Context rootContext;

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
        this.contexts = Collections.synchronizedMap(new HashMap());
        this.histories = Collections.synchronizedMap(new HashMap());
        this.invokerClasses = Collections.synchronizedMap(new HashMap());
        this.invokers = Collections.synchronizedMap(new HashMap());
        this.completions = Collections.synchronizedMap(new HashMap());
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
    void setNotificationRegistry(final NotificationRegistry notifRegistry) {
        this.notificationRegistry = notifRegistry;
    }

    /**
     * Get the <code>Context</code> for this <code>TransitionTarget</code>.
     * If one is not available it is created.
     *
     * @param transitionTarget The TransitionTarget.
     * @return The Context.
     */
    public Context getContext(final TransitionTarget transitionTarget) {
        Context context = (Context) contexts.get(transitionTarget);
        if (context == null) {
            TransitionTarget parent = transitionTarget.getParent();
            if (parent == null) {
                // docroot
                context = evaluator.newContext(getRootContext());
            } else {
                context = evaluator.newContext(getContext(parent));
            }
            Datamodel datamodel = transitionTarget.getDatamodel();
            SCXMLHelper.cloneDatamodel(datamodel, context, evaluator, null);
            contexts.put(transitionTarget, context);
        }
        return context;
    }

    /**
     * Get the <code>Context</code> for this <code>TransitionTarget</code>.
     * May return <code>null</code>.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @return The Context.
     */
    Context lookupContext(final TransitionTarget transitionTarget) {
        return (Context) contexts.get(transitionTarget);
    }

    /**
     * Set the <code>Context</code> for this <code>TransitionTarget</code>.
     *
     * @param transitionTarget The TransitionTarget.
     * @param context The Context.
     */
    void setContext(final TransitionTarget transitionTarget,
            final Context context) {
        contexts.put(transitionTarget, context);
    }

    /**
     * Get the last configuration for this history.
     *
     * @param history The history.
     * @return Returns the lastConfiguration.
     */
    public Set getLastConfiguration(final History history) {
        Set lastConfiguration = (Set) histories.get(history);
        if (lastConfiguration == null) {
            lastConfiguration = new HashSet();
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
            final Set lc) {
        Set lastConfiguration = getLastConfiguration(history);
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
        Set lastConfiguration = (Set) histories.get(history);
        if (lastConfiguration == null || lastConfiguration.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Resets the history state.
     *
     * @param history The history.
     * @see org.apache.commons.scxml.SCXMLExecutor#reset()
     */
    public void reset(final History history) {
        Set lastConfiguration = (Set) histories.get(history);
        if (lastConfiguration != null) {
            lastConfiguration.clear();
        }
    }

    /**
     * Get the {@link SCXMLExecutor} this instance is attached to.
     *
     * @return The SCXMLExecutor this instance is attached to.
     * @see org.apache.commons.scxml.SCXMLExecutor
     */
    public SCXMLExecutor getExecutor() {
        return executor;
    }

    /**
     * Register an {@link Invoker} class for this target type.
     *
     * @param targettype The target type (specified by "targettype"
     *                   attribute of &lt;invoke&gt; tag).
     * @param invokerClass The <code>Invoker</code> <code>Class</code>.
     */
    void registerInvokerClass(final String targettype,
            final Class invokerClass) {
        invokerClasses.put(targettype, invokerClass);
    }

    /**
     * Remove the {@link Invoker} class registered for this target
     * type (if there is one registered).
     *
     * @param targettype The target type (specified by "targettype"
     *                   attribute of &lt;invoke&gt; tag).
     */
    void unregisterInvokerClass(final String targettype) {
        invokerClasses.remove(targettype);
    }

    /**
     * Get the {@link Invoker} for this {@link TransitionTarget}.
     * May return <code>null</code>. A non-null <code>Invoker</code> will be
     * returned if and only if the <code>TransitionTarget</code> is
     * currently active and contains an &lt;invoke&gt; child.
     *
     * @param targettype The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     *         invoker class is registered against that type,
     *         <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot
     *                          be instantiated.
     */
    public Invoker newInvoker(final String targettype)
    throws InvokerException {
        Class invokerClass = (Class) invokerClasses.get(targettype);
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for "
                + "targettype \"" + targettype + "\"");
        }
        Invoker invoker = null;
        try {
            invoker = (Invoker) invokerClass.newInstance();
        } catch (InstantiationException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iae) {
            throw new InvokerException(iae.getMessage(), iae.getCause());
        }
        return invoker;
    }

    /**
    * Get the {@link Invoker} for this {@link TransitionTarget}.
     * May return <code>null</code>. A non-null {@link Invoker} will be
     * returned if and only if the {@link TransitionTarget} is
     * currently active and contains an &lt;invoke&gt; child.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @return The Invoker.
     */
    public Invoker getInvoker(final TransitionTarget transitionTarget) {
        return (Invoker) invokers.get(transitionTarget);
    }

    /**
     * Set the {@link Invoker} for this {@link TransitionTarget}.
     *
     * @param transitionTarget The TransitionTarget.
     * @param invoker The Invoker.
     */
    public void setInvoker(final TransitionTarget transitionTarget,
            final Invoker invoker) {
        invokers.put(transitionTarget, invoker);
    }

    /**
     * Return the Map of {@link Invoker}s currently "active".
     *
     * @return The map of invokers.
     */
    public Map getInvokers() {
        return invokers;
    }

    /**
     * Get the completion status for this composite
     * {@link TransitionTarget}.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @return The completion status.
     */
    public boolean isDone(final TransitionTarget transitionTarget) {
        Boolean done = (Boolean) completions.get(transitionTarget);
        if (done == null) {
            return false;
        } else {
            return done.booleanValue();
        }
    }

    /**
     * Set the completion status for this composite
     * {@link TransitionTarget}.
     *
     * @param transitionTarget The TransitionTarget.
     * @param done The completion status.
     */
    public void setDone(final TransitionTarget transitionTarget,
            final boolean done) {
        completions.put(transitionTarget, done ? Boolean.TRUE : Boolean.FALSE);
    }

}

