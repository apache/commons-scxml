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

import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.SCXML;
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
     * The stateMachine being executed.
     */
    private SCXML stateMachine;

    /**
     * The current status of the stateMachine.
     */
    private final Status currentStatus;

    /**
     * The owning state machine executor.
     */
    private transient SCXMLExecutor executor;

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
     * The root context.
     */
    private Context rootContext;

    /**
     * The initial script context
     */
    private Context globalScriptContext;

    /**
     * Constructor
     *
     * @param executor The executor that this instance is attached to.
     */
    SCInstance(final SCXMLExecutor executor) {
        this.currentStatus = new Status();
        this.executor = executor;
        this.contexts = Collections.synchronizedMap(new HashMap<EnterableState, Context>());
        this.histories = Collections.synchronizedMap(new HashMap<History, Set<EnterableState>>());
        this.completions = Collections.synchronizedMap(new HashMap<EnterableState, Boolean>());
    }

    /**
     * @return Return the state machine for this instance
     */
    public SCXML getStateMachine() {
        return stateMachine;
    }

    /**
     * Sets the state machine for this instance
     * @param stateMachine The state machine for this instance
     */
    void setStateMachine(SCXML stateMachine) {
        this.stateMachine = stateMachine;
        currentStatus.getStates().clear();
        contexts.clear();
        histories.clear();
        completions.clear();
    }

    /**
     * @return Returns the current status (active atomic states) for this instance
     */
    public Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Re-attach the executor
     * @param executor The executor that this instance will be re-attached to.
     */
    void setExecutor(SCXMLExecutor executor) {
        this.executor = executor;
    }

    /**
     * Get the root context.
     *
     * @return The root context.
     */
    public Context getRootContext() {
        if (rootContext == null && executor.getEvaluator() != null) {
            rootContext = executor.getEvaluator().newContext(null);
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
                globalScriptContext = executor.getEvaluator().newContext(getRootContext());
            }
        }
        return globalScriptContext;
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
                context = executor.getEvaluator().newContext(getGlobalScriptContext());
            } else {
                context = executor.getEvaluator().newContext(getContext(parent));
            }
            if (state instanceof TransitionalState) {
                Datamodel datamodel = ((TransitionalState)state).getDatamodel();
                SCXMLHelper.cloneDatamodel(datamodel, context, executor.getEvaluator(), null);
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

