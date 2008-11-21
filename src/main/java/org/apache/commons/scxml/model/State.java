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

import java.util.LinkedHashMap;
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
    private static final long serialVersionUID = 2L;

    /**
     * The Map containing immediate children of this State, keyed by
     * their IDs. Incompatible with the parallel or invoke property.
     */
    private Map<String, TransitionTarget> children;

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
     * Applies to composite states only. If one of its final children is
     * active, its parent is marked done. This property is reset upon
     * re-entry.
     *
     * @deprecated Will be removed in v1.0
     */
    private boolean done = false;

    /**
     * Constructor.
     */
    public State() {
        this.children = new LinkedHashMap<String, TransitionTarget>();
    }

    /**
     * Is this state a &quot;final&quot; state.
     *
     * @return boolean Returns the isFinal.
     * @deprecated Use {@link #isFinal()} instead
     */
    public final boolean getIsFinal() {
        return isFinal;
    }

    /**
     * Set whether this is a &quot;final&quot; state.
     *
     * @param isFinal
     *            The isFinal to set.
     * @deprecated Use {@link #setFinal(boolean)} instead
     */
    public final void setIsFinal(final boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Is this state a &quot;final&quot; state.
     *
     * @return boolean Returns the isFinal.
     *
     * @since 0.7
     */
    public final boolean isFinal() {
        return isFinal;
    }

    /**
     * Set whether this is a &quot;final&quot; state.
     *
     * @param isFinal
     *            The isFinal to set.
     *
     * @since 0.7
     */
    public final void setFinal(final boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Get the Parallel child (may be null).
     *
     * @return Parallel Returns the parallel.
     *
     * @deprecated &lt;parallel&gt; no longer needs an enclosing
     *             &lt;state&gt; element.
     */
    public final Parallel getParallel() {
        return parallel;
    }

    /**
     * Set the Parallel child.
     *
     * @param parallel
     *            The parallel to set.
     *
     * @deprecated &lt;parallel&gt; no longer needs an enclosing
     *             &lt;state&gt; element.
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
        target.setParent(this);
    }

    /**
     * Get the initial state's ID.
     *
     * @return The initial state's string ID.
     */
    public final String getFirst() {
        if (initial != null) {
            return initial.getTransition().getNext();
        }
        return null;
    }

    /**
     * Set the initial state by its ID string.
     *
     * @param target
     *            The initial target's ID to set.
     */
    public final void setFirst(final String target) {
        Transition t = new Transition();
        t.setNext(target);
        Initial ini = new Initial();
        ini.setTransition(t);
        ini.setParent(this);
        this.initial = ini;
    }

    /**
     * Get the map of child states (may be empty).
     *
     * @return Map Returns the children.
     */
    public final Map<String, TransitionTarget> getChildren() {
        return children;
    }

    /**
     * Add a child state.
     *
     * @param state
     *            a child state
     *
     * @deprecated Use {@link #addChild(TransitionTarget)} instead.
     */
    public final void addChild(final State state) {
        this.children.put(state.getId(), state);
        state.setParent(this);
    }

    /**
     * Add a child transition target.
     *
     * @param tt
     *            a child transition target
     *
     * @since 0.7
     */
    public final void addChild(final TransitionTarget tt) {
        this.children.put(tt.getId(), tt);
        tt.setParent(this);
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
     * @deprecated &lt;parallel&gt; now represents an orthogonal state, rather
     *             than denoting that the enclosing state is orthogonal, as
     *             it did in previous SCXML WDs.
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
     * @deprecated Will be removed in v1.0, in favor of
     *             <code>SCInstance#isDone(TransitionTarget)</code>
     */
    public final boolean isDone() {
        return done;
    }

    /**
     * Update the done property, which is set if this is a parallel state,
     * and one its final states is active.
     *
     * @param done The done to set.
     * @deprecated Will be removed in v1.0, in favor of
     *             <code>SCInstance#setDone(TransitionTarget)</code>
     */
    public final void setDone(final boolean done) {
        this.done = done;
    }
}

