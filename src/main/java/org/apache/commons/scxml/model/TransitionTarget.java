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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for elements in SCXML that can serve as a
 * &lt;target&gt; for a &lt;transition&gt;, such as State or Parallel.
 *
 */
public abstract class TransitionTarget implements Serializable, Observable {

    /**
     * Identifier for this transition target. Other parts of the SCXML
     * document may refer to this &lt;state&gt; using this ID.
     */
    private String id;

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

    /**
     * Optional property holding the data model for this transition target.
     */
    private Datamodel datamodel;

    /**
     * The parent of this transition target (may be null, if the parent
     * is the SCXML document root).
     */
    private TransitionTarget parent;

    /**
     * List of history states owned by a given state (applies to non-leaf
     * states).
     */
    private List<History> history;

    /**
     * Constructor.
     */
    public TransitionTarget() {
        super();
        onEntry = new OnEntry(); //empty defaults
        onEntry.setParent(this);
        onExit = new OnExit();   //empty defaults
        onExit.setParent(this);
        parent = null;
        history = new ArrayList<History>();
    }

    /**
     * Get the identifier for this transition target (may be null).
     *
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this transition target.
     *
     * @param id The id to set.
     */
    public final void setId(final String id) {
        this.id = id;
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

    /**
     * Get the data model for this transition target.
     *
     * @return Returns the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Set the data model for this transition target.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
    }

    /**
     * Get the parent TransitionTarget.
     *
     * @return Returns the parent state
     * (null if parent is &lt;scxml&gt; element)
     */
    public final TransitionTarget getParent() {
        return parent;
    }

    /**
     * Set the parent TransitionTarget.
     *
     * @param parent The parent state to set
     */
    public final void setParent(final TransitionTarget parent) {
        this.parent = parent;
    }

    /**
     * Get the parent State.
     *
     * @return The parent State
     */
    public final State getParentState() {
        TransitionTarget tt = this.getParent();
        if (tt == null) {
            return null;
        } else {
            if (tt instanceof State) {
                return (State) tt;
            } else { //tt is Parallel
                return tt.getParentState();
            }
        }
    }

    /**
     * This method is used by XML digester.
     *
     * @param h
     *            History pseudo state
     *
     * @since 0.7
     */
    public final void addHistory(final History h) {
        history.add(h);
        h.setParent(this);
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
        return (!history.isEmpty());
    }

    /**
     * Get the list of history pseudo states for this state.
     *
     * @return a list of all history pseudo states contained by a given state
     *         (can be empty)
     * @see #hasHistory()
     *
     * @since 0.7
     */
    public final List<History> getHistory() {
        return history;
    }

}

