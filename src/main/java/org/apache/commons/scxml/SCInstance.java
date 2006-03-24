/*
 *
 *   Copyright 2006 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.scxml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml.model.Datamodel;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * The <code>SCInstance</code> performs book-keeping functions for
 * a particular execution of a state chart represented by a
 * <code>SCXML</code> object.
 */
public class SCInstance {

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
     * The evaluator for expressions.
     */
    private Evaluator evaluator;

    /**
     * The root context.
     */
    private Context rootContext;

    /**
     * Constructor.
     */
    SCInstance() {
        this.notificationRegistry = new NotificationRegistry();
        this.contexts = new HashMap();
        this.histories = new HashMap();
        this.evaluator = null;
        this.rootContext = null;
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

}

