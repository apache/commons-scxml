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
package org.apache.commons.scxml2.model;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;state&gt; SCXML element.
 *
 */
public class State extends TransitionalState {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The id of the initial child of this composite, corresponding with the state initial attribute
     */
    private String first;

    /**
     * A child which identifies initial state for state machines that
     * have substates.
     */
    private Initial initial;

    /**
     * Constructor.
     */
    public State() {
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
        this.first = null;
        this.initial = target;
        target.setParent(this);
    }

    /**
     * Get the initial state's ID.
     *
     * @return The initial state's string ID.
     */
    public final String getFirst() {
        return first;
    }

    /**
     * Set the initial state by its ID string.
     *
     * @param target
     *            The initial target's ID to set.
     */
    public final void setFirst(final String target) {
        this.first = target;
        SimpleTransition t = new SimpleTransition();
        t.setNext(target);
        Initial ini = new Initial();
        ini.setGenerated();
        ini.setTransition(t);
        ini.setParent(this);
        this.initial = ini;
    }

    /**
     * Check whether this is a simple (leaf) state (UML terminology).
     *
     * @return true if this is a simple state, otherwise false
     */
    public final boolean isSimple() {
        return getChildren().isEmpty();
    }

    /**
     * Check whether this is a composite state (UML terminology).
     *
     * @return true if this is a composite state, otherwise false
     */
    public final boolean isComposite() {
        return !isSimple();
    }

    /**
     * Checks whether it is a region state (directly nested to parallel - UML
     * terminology).
     *
     * @return true if this is a region state, otherwise false
     * @see Parallel
     */
    public final boolean isRegion() {
        return getParent() instanceof  Parallel;
    }

    /**
     * Adds an EnterableState (State, Final or Parallel) child
     * @param es the child to add
     */
    @Override
    public final void addChild(final EnterableState es) {
        super.addChild(es);
    }
}

