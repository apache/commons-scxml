/*
 *
 *   Copyright 2005 The Apache Software Foundation.
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
package org.apache.commons.scxml.model;

import org.apache.commons.scxml.NotificationRegistry;
import org.apache.commons.scxml.Observable;
import org.apache.commons.scxml.SCXMLListener;

/**
 * An abstract base class for elements in SCXML that can serve as a
 * &lt;target&gt; for a &lt;transition&gt;, such as State or Parallel.
 *
 */
public abstract class TransitionTarget implements Observable {

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
     * The parent of this transition target (may be null, if the parent
     * is the SCXML document root).
     */
    private TransitionTarget parent;

    /**
     * The notification registry.
     */
    private NotificationRegistry notifReg;

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
        notifReg = null;
    }

    /**
     * Register a listener to this document root.
     *
     * @param lst The SCXMLListener to add
     */
    public final void addListener(final SCXMLListener lst) {
        notifReg.addListener(this, lst);
    }

    /**
     * Deregister a listener from this document root.
     *
     * @param lst The SCXMLListener to remove
     */
    public final void removeListener(final SCXMLListener lst) {
        notifReg.removeListener(this, lst);
    }

    /**
     * Supply this TransitionTarget object a handle to the notification
     * registry. Called by the Digester after instantiation.
     *
     * @param reg The notification registry
     */
    public final void setNotificationRegistry(final NotificationRegistry reg) {
        notifReg = reg;
    }

    /**
     * Get the notification registry.
     *
     * @return The notification registry.
     */
    public final NotificationRegistry getNotificationRegistry() {
        return notifReg;
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

}

