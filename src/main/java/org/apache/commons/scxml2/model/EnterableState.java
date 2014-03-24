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
 * An abstract base class for state elements in SCXML that can be entered, such as State, Parallel or Final.
 */
public abstract class EnterableState extends TransitionTarget implements DocumentOrder {

    /**
     * The document order of this state
     */
    private int order;

    /**
     * Optional property holding executable content to be run upon
     * entering this transition target.
     */
    private OnEntry onEntry;

    /**
     * Optional property holding executable content to be run upon
     * exiting this transition target.
     */
    private OnExit onExit;

    public EnterableState() {
        super();
        onEntry = new OnEntry(); //empty defaults
        onEntry.setParent(this);
        onExit = new OnExit();   //empty defaults
        onExit.setParent(this);
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
     * Sets the document order of this state
     * @param order the document order
     * @see DocumentOrder
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * Get the onentry property.
     *
     * @return Returns the onEntry.
     */
    public final OnEntry getOnEntry() {
        return onEntry;
    }

    /**
     * Set the onentry property.
     *
     * @param onEntry The onEntry to set.
     */
    public final void setOnEntry(final OnEntry onEntry) {
        this.onEntry = onEntry;
        this.onEntry.setParent(this);
    }

    /**
     * Get the onexit property.
     *
     * @return Returns the onExit.
     */
    public final OnExit getOnExit() {
        return onExit;
    }

    /**
     * Set the onexit property.
     *
     * @param onExit The onExit to set.
     */
    public final void setOnExit(final OnExit onExit) {
        this.onExit = onExit;
        this.onExit.setParent(this);
    }
}
