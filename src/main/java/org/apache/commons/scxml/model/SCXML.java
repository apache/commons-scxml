/*
 *
 *   Copyright 2005-2006 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.scxml.SCXMLHelper;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;scxml&gt; root element, and serves as the &quot;document
 * root&quot;.
 *
 */
public class SCXML {

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
     * Optional property holding the data model for this SCXML document.
     * This gets merged with the root context and potentially hides any
     * (namesake) variables in the root context.
     */
    private Datamodel datamodel;

    /**
     * The immediate child states of this SCXML document root.
     */
    private Map states;

    /**
     * A global map of all States and Parallels associated with this
     * state machine, keyed by their id.
     */
    private Map targets;

    /**
     * Constructor.
     */
    public SCXML() {
        this.states = new HashMap();
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
     * Get the data model placed at document root.
     *
     * @return Returns the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Set the data model at document root.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
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

}

