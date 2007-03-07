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
package org.apache.commons.scxml.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;state&gt; SCXML element.
 *
 */
public class State extends TransitionTarget {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Map containing immediate children of this State, keyed by
     * their IDs. Incompatible with the parallel or invoke property.
     */
    private Map children;

    /**
     * The Parallel child, which defines a set of parallel substates.
     * May occur 0 or 1 times. Incompatible with the state or invoke property.
     */
    private Parallel parallel;

    /**
     * The Invoke child, which defines an external process that should
     * be invoked, immediately after the onentry executable content,
     * and the transitions become candidates after the invoked
     * process has completed its execution.
     * May occur 0 or 1 times. Incompatible with the state or parallel
     * property.
     */
    private Invoke invoke;

    /**
     * Boolean property indicating whether this is a final state or not.
     * Default value is false . Final states may not have substates or
     * outgoing transitions.
     */
    private boolean isFinal;

    /**
     * A child which identifies initial state for state machines that
     * have substates.
     */
    private Initial initial;

    /**
     * A list of outgoing Transitions from this state, by document order.
     */
    private List transitions;

    /**
     * List of history states owned by a given state (applies to non-leaf
     * states).
     */
    private List history;

    /**
     * Applies to composite states only. If one of its final children is
     * active, its parent is marked done. This property is reset upon
     * re-entry.
     */
    private boolean done = false;

    /**
     * Constructor.
     */
    public State() {
        this.children = new LinkedHashMap();
        this.transitions = new ArrayList();
        this.history = new ArrayList();
    }

    /**
     * Is this state a &quot;final&quot; state.
     *
     * @return boolean Returns the isFinal.
     */
    public final boolean getIsFinal() {
        return isFinal;
    }

    /**
     * Set whether this is a &quot;final&quot; state.
     *
     * @param isFinal
     *            The isFinal to set.
     */
    public final void setIsFinal(final boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Get the Parallel child (may be null).
     *
     * @return Parallel Returns the parallel.
     */
    public final Parallel getParallel() {
        return parallel;
    }

    /**
     * Set the Parallel child.
     *
     * @param parallel
     *            The parallel to set.
     */
    public final void setParallel(final Parallel parallel) {
        this.parallel = parallel;
    }

    /**
     * Get the Invoke child (may be null).
     *
     * @return Invoke Returns the invoke.
     */
    public final Invoke getInvoke() {
        return invoke;
    }

    /**
     * Set the Invoke child.
     *
     * @param invoke
     *            The invoke to set.
     */
    public final void setInvoke(final Invoke invoke) {
        this.invoke = invoke;
    }

    /**
     * Get the initial state.
     *
     * @return Initial Returns the initial state.
     */
    public final Initial getInitial() {
        return initial;
    }

    /**
     * Set the initial state.
     *
     * @param target
     *            The target to set.
     */
    public final void setInitial(final Initial target) {
        this.initial = target;
    }

    /**
     * Get the map of all outgoing transitions from this state.
     *
     * @return Map Returns the transitions Map.
     * @deprecated Use getTransitionsList() instead
     */
    public final Map getTransitions() {
        Map transitionsMap = new HashMap();
        for (int i = 0; i < transitions.size(); i++) {
            Transition transition = (Transition) transitions.get(i);
            String event = transition.getEvent();
            if (!transitionsMap.containsKey(event)) {
                List eventTransitions = new ArrayList();
                eventTransitions.add(transition);
                transitionsMap.put(event, eventTransitions);
            } else {
                ((List) transitionsMap.get(event)).add(transition);
            }
        }
        return transitionsMap;
    }

    /**
     * Get the list of all outgoing transitions from this state, that
     * will be candidates for being fired on the given event.
     *
     * @param event The event
     * @return List Returns the candidate transitions for given event
     */
    public final List getTransitionsList(final String event) {
        List matchingTransitions = null; // since we returned null upto v0.6
        for (int i = 0; i < transitions.size(); i++) {
            Transition t = (Transition) transitions.get(i);
            if ((event == null && t.getEvent() == null)
                    || (event != null && event.equals(t.getEvent()))) {
                if (matchingTransitions == null) {
                    matchingTransitions = new ArrayList();
                }
                matchingTransitions.add(t);
            }
        }
        return matchingTransitions;
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
    }

    /**
     * Get the map of child states (may be empty).
     *
     * @return Map Returns the children.
     */
    public final Map getChildren() {
        return children;
    }

    /**
     * Add a child state.
     *
     * @param state
     *            a child state
     */
    public final void addChild(final State state) {
        this.children.put(state.getId(), state);
        state.setParent(this);
    }

    /**
     * Get the outgoing transitions for this state as a java.util.List.
     *
     * @return List Returns the transitions list.
     */
    public final List getTransitionsList() {
        return transitions;
    }

    /**
     * This method is used by XML digester.
     *
     * @param h
     *            History pseudo state
     */
    public final void addHistory(final History h) {
        history.add(h);
    }

    /**
     * Does this state have a history pseudo state.
     *
     * @return boolean true if a given state contains at least one
     *                 history pseudo state
     */
    public final boolean hasHistory() {
        return (!history.isEmpty());
    }

    /**
     * Get the list of history pseudo states for this state.
     *
     * @return a list of all history pseudo states contained by a given state
     *         (can be empty)
     * @see #hasHistory()
     */
    public final List getHistory() {
        return history;
    }

    /**
     * Check whether this is a simple (leaf) state (UML terminology).
     *
     * @return true if this is a simple state, otherwise false
     */
    public final boolean isSimple() {
        if (parallel == null && children.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Check whether this is a composite state (UML terminology).
     *
     * @return true if this is a composite state, otherwise false
     */
    public final boolean isComposite() {
        if (parallel == null && children.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether it is a region state (directly nested to parallel - UML
     * terminology).
     *
     * @return true if this is a region state, otherwise false
     * @see Parallel
     */
    public final boolean isRegion() {
        if (getParent() instanceof Parallel) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether it is a orthogonal state, that is, it owns a parallel
     * (UML terminology).
     *
     * @return true if this is a orthogonal state, otherwise false
     */
    public final boolean isOrthogonal() {
        if (parallel != null) {
            return true;
        }
        return false;
    }

    /**
     * In case this is a parallel state, check if one its final states
     * is active.
     *
     * @return Returns the done.
     */
    public final boolean isDone() {
        return done;
    }

    /**
     * Update the done property, which is set if this is a parallel state,
     * and one its final states is active.
     *
     * @param done The done to set.
     */
    public final void setDone(final boolean done) {
        this.done = done;
    }
}

