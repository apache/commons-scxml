/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for state elements in SCXML that can be transitioned out from, such as State or Parallel.
 */
public abstract class TransitionalState extends EnterableState {

    /**
     * A list of outgoing Transitions from this state, by document order.
     */
    private final List<Transition> transitions;

    /**
     * Optional property holding the data model for this state.
     */
    private Datamodel datamodel;

    /**
     * List of history states owned by a given state (applies to non-leaf
     * states).
     */
    private final List<History> history;

    /**
     * The Invoke children, each which defines an external process that should
     * be invoked, immediately after the onentry executable content,
     * and the transitions become candidates after the invoked
     * process has completed its execution.
     * May occur 0 or more times.
     */
    private final List<Invoke> invokes;

    /**
     * The set of EnterableState children contained in this TransitionalState
     */
    private final List<EnterableState> children;

    public TransitionalState() {
        transitions = new ArrayList<>();
        history = new ArrayList<>();
        children = new ArrayList<>();
        invokes = new ArrayList<>();
    }

    /**
     * Add a child.
     *
     * @param es A child enterable state.
     * @since 0.7
     */
    protected void addChild(final EnterableState es) {
        children.add(es);
        es.setParent(this);
    }

    /**
     * @param h History pseudo state
     * @since 0.7
     */
    public final void addHistory(final History h) {
        history.add(h);
        h.setParent(this);
    }

    /**
     * Sets the Invoke child.
     *
     * @param invoke
     *            The invoke to set.
     */
    public final void addInvoke(final Invoke invoke) {
        invoke.setParentEnterableState(this, this.invokes.size());
        this.invokes.add(invoke);
    }

    /**
     * Add a transition to the map of all outgoing transitions for
     * this state.
     *
     * @param transition
     *            The transitions to set.
     */
    public final void addTransition(final Transition transition) {
        transitions.add(transition);
        transition.setParent(this);
    }

    /**
     * Gets the ancestor of this TransitionalState at specified level
     * @param level the level of the ancestor to return, zero being top
     * @return the ancestor at specified level
     */
    @Override
    public TransitionalState getAncestor(final int level) {
        return (TransitionalState)super.getAncestor(level);
    }

    /**
     * Gets the set of child transition targets (may be empty).
     *
     * @return the children.
     * @since 0.7
     */
    public final List<EnterableState> getChildren() {
        return children;
    }

    /**
     * Gets the data model for this transition target.
     *
     * @return the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Gets the list of history pseudo states for this state.
     *
     * @return a list of all history pseudo states contained by a given state
     *         (can be empty)
     * @see #hasHistory()
     * @since 0.7
     */
    public final List<History> getHistory() {
        return history;
    }

    /**
     * Gets the Invoke children (may be empty).
     *
     * @return Invoke Returns the invoke.
     */
    public final List<Invoke> getInvokes() {
        return invokes;
    }

    /**
     * Gets the TransitionalState (State or Parallel) parent.
     *
     * @return the parent.
     */
    @Override
    public TransitionalState getParent() {
        return (TransitionalState)super.getParent();
    }

    /**
     * Gets the outgoing transitions for this state as a java.util.List.
     *
     * @return List Returns the transitions list.
     */
    public final List<Transition> getTransitionsList() {
        return transitions;
    }

    /**
     * Gets the list of all outgoing transitions from this state, that
     * will be candidates for being fired on the given event.
     *
     * @param event The event
     * @return List Returns the candidate transitions for given event
     */
    public final List<Transition> getTransitionsList(final String event) {
        List<Transition> matchingTransitions = null; // since we returned null upto v0.6
        for (final Transition t : transitions) {
            if (event == null && t.getEvent() == null
                    || event != null && event.equals(t.getEvent())) {
                if (matchingTransitions == null) {
                    matchingTransitions = new ArrayList<>();
                }
                matchingTransitions.add(t);
            }
        }
        return matchingTransitions;
    }

    /**
     * Does this state have a history pseudo state.
     *
     * @return boolean true if a given state contains at least one
     *                 history pseudo state
     *
     * @since 0.7
     */
    public final boolean hasHistory() {
        return !history.isEmpty();
    }

    /**
     * Sets the data model for this transition target.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
    }

    /**
     * Sets the TransitionalState parent
     *
     * @param parent The parent to set.
     */
    public final void setParent(final TransitionalState parent) {
        super.setParent(parent);
    }

    /**
     * Update TransitionTarget descendants their ancestors
     */
    @Override
    protected void updateDescendantsAncestors() {
        super.updateDescendantsAncestors();
        for (final History h : history) {
            // reset ancestors
            h.updateDescendantsAncestors();
        }
        for (final TransitionTarget child : children) {
            child.updateDescendantsAncestors();
        }
    }
}
