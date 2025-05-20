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
 * An abstract base class for state elements in SCXML that can be entered, such as State, Parallel or Final.
 */
public abstract class EnterableState extends TransitionTarget implements DocumentOrder {

    /**
     * The document order of this state
     */
    private int order;

    /**
     * List of optional OnEntry elements holding executable content to be run upon
     * entering this transition target.
     */
    private final List<OnEntry> onEntries;

    /**
     * List of optional OnExit elements holding executable content to be run upon
     * exiting this transition target.
     */
    private final List<OnExit> onExits;

    public EnterableState() {
        onEntries = new ArrayList<>();
        onExits = new ArrayList<>();
    }

    /**
     * Adds an OnEntry element
     *
     * @param onEntry The onEntry to add.
     */
    public final void addOnEntry(final OnEntry onEntry) {
        onEntry.setParent(this);
        onEntries.add(onEntry);
    }

    /**
     * Add an OnExit element
     *
     * @param onExit The onExit to add.
     */
    public final void addOnExit(final OnExit onExit) {
        onExit.setParent(this);
        onExits.add(onExit);
    }

    /**
     * Gets the OnEntry elements.
     *
     * @return the onEntry elements
     */
    public final List<OnEntry> getOnEntries() {
        return onEntries;
    }

    /**
     * Gets the OnExit elements
     *
     * @return the onExit elements
     */
    public final List<OnExit> getOnExits() {
        return onExits;
    }

    /**
     * @return the document order of this state
     * @see DocumentOrder
     */
    @Override
    public final int getOrder() {
        return order;
    }

    /**
     * Check whether this is an atomic state.
     * <p>
     * An atomic state is a state of type Final or of type State without children,
     * </p>
     * @return true if this is an atomic state.
     */
    public abstract boolean isAtomicState();

    /**
     * Sets the document order of this state
     * @param order the document order
     * @see DocumentOrder
     */
    public final void setOrder(final int order) {
        this.order = order;
    }
}
