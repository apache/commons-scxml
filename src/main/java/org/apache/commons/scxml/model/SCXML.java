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

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.NotificationRegistry;
import org.apache.commons.scxml.Observable;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.SCXMLListener;

import java.util.HashMap;
import java.util.Map;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;scxml&gt; root element, and serves as the &quot;document
 * root&quot;. It is also associated with the root Context, via which
 * the SCXMLExecutor may access and the query state of the host
 * environment.
 *
 */
public class SCXML implements Observable {

    /**
     * The SCXML XMLNS.
     */
    public static final String XMLNS = "http://www.w3.org/2005/07/SCXML";

    /**
     * The xmlns attribute on the root &lt;smxml&gt; element.
     * This must match XMLNS above.
     */
    private String xmlns;

    /**
     * The SCXML version of this document.
     */
    private String version;

    /**
     * The initial State for the SCXML executor.
     */
    private State initialState;

    /**
     * The initial state ID (used by XML Digester only).
     */
    private transient String initialstate;

    /**
     * The immediate child states of this SCXML document root.
     */
    private Map states;

    /**
     * The notification registry.
     */
    private NotificationRegistry notifReg;

    /**
     * A global map of all States and Parallels associated with this
     * state machine, keyed by their id.
     */
    private Map targets;

    /**
     * The root Context which interfaces with the host environment.
     */
    private Context rootContext;

    /**
     * Constructor.
     */
    public SCXML() {
        this.states = new HashMap();
        this.notifReg = new NotificationRegistry();
        this.targets = new HashMap();
    }

    /**
     * Get the initial State.
     *
     * @return State Returns the initialstate.
     */
    public final State getInitialState() {
        return initialState;
    }

    /**
     * Set the initial State.
     *
     * @param initialState The initialstate to set.
     */
    public final void setInitialState(final State initialState) {
        this.initialState = initialState;
    }

    /**
     * Get the children states.
     *
     * @return Map Returns map of the child states.
     */
    public final Map getStates() {
        return states;
    }

    /**
     * Add a child state.
     *
     * @param state The state to be added to the states Map.
     */
    public final void addState(final State state) {
        states.put(state.getId(), state);
    }

    /**
     * Get the targets map, whichis a Map of all States and Parallels
     * associated with this state machine, keyed by their id.
     *
     * @return Map Returns the targets.
     */
    public final Map getTargets() {
        return targets;
    }

    /**
     * Add a target to this SCXML document.
     *
     * @param target The target to be added to the targets Map.
     */
    public final void addTarget(final TransitionTarget target) {
        String id = target.getId();
        if (!SCXMLHelper.isStringEmpty(id)) {
            // Target is not anonymous, so makes sense to map it
            targets.put(id, target);
        }
    }

    /**
     * Get the SCXML document version.
     *
     * @return Returns the version.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Set the SCXML document version.
     *
     * @param version The version to set.
     */
    public final void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Get the xmlns of this SCXML document.
     *
     * @return Returns the xmlns.
     */
    public final String getXmlns() {
        return xmlns;
    }

    /**
     * Set the xmlns of this SCXML document.
     *
     * @param xmlns The xmlns to set.
     */
    public final void setXmlns(final String xmlns) {
        this.xmlns = xmlns;
    }

    /**
     * Get the notification registry.
     *
     * @return NotificationRegistry Returns the notifReg.
     */
    public final NotificationRegistry getNotificationRegistry() {
        return notifReg;
    }

    /**
     * Get the ID of the initial state.
     *
     * @return String Returns the initial state ID (used by XML Digester only).
     * @see #getInitialState()
     */
    public final String getInitialstate() {
        return initialstate;
    }

    /**
     * Set the ID of the initial state.
     *
     * @param initialstate The initial state ID (used by XML Digester only).
     * @see #setInitialState(State)
     */
    public final void setInitialstate(final String initialstate) {
        this.initialstate = initialstate;
    }

    /**
     * Get the root Context for this document.
     *
     * @return Returns the rootContext.
     */
    public final Context getRootContext() {
        return rootContext;
    }

    /**
     * Set the root Context for this document.
     *
     * @param rootContext The rootContext to set.
     */
    public final void setRootContext(final Context rootContext) {
        this.rootContext = rootContext;
    }

    /**
     * Register a listener to this document root.
     *
     * @param lst The SCXMLListener to add
     * Remarks: Only valid if StateMachine is non null!
     */
    public final void addListener(final SCXMLListener lst) {
        notifReg.addListener(this, lst);
    }

    /**
     * Deregister a listener from this document root.
     *
     * @param lst The SCXMLListener to remove
     * Remarks: Only valid if StateMachine is non null!
     */
    public final void removeListener(final SCXMLListener lst) {
        notifReg.removeListener(this, lst);
    }

}

