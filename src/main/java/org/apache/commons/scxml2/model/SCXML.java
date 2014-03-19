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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;scxml&gt; root element, and serves as the &quot;document
 * root&quot;.
 *
 */
public class SCXML implements Serializable, Observable,
                              NamespacePrefixesHolder {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The SCXML XMLNS.
     */
    public static final String XMLNS = "http://www.w3.org/2005/07/scxml";

    /**
     * Reserved prefix for auto generated TransitionTarget id values
     */
    public static final String GENERATED_TT_ID_PREFIX = "_generated_tt_id_";

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
     * The initial Transition for the SCXML executor.
     */
    private Transition initialTransition;

    /**
     * The initial transition target ID (used by XML Digester only).
     */
    private String initial;

    /**
     * The name for this state machine.
     */
    private String name;

    /**
     * The profile in use.
     */
    private String profile;

    /**
     * The exmode for this document.
     */
    private String exmode;

    /**
     * Optional property holding the data model for this SCXML document.
     * This gets merged with the root context and potentially hides any
     * (namesake) variables in the root context.
     */
    private Datamodel datamodel;

    /**
     * Optional property holding the initial script for this SCXML document.
     */
    private Script globalScript;

    /**
     * The immediate child targets of this SCXML document root.
     */
    private Map<String, TransitionTarget> children;

    /**
     * A global map of all States and Parallels associated with this
     * state machine, keyed by their id.
     */
    private Map<String, TransitionTarget> targets;

    /**
     * The XML namespaces defined on the SCXML document root node,
     * preserved primarily for serialization.
     */
    private Map<String, String> namespaces;

    /**
     * The next auto-generated transition target unique id value
     * @see #generateTransitionTargetId()
     */

    private long ttNextId;

    /**
     * Constructor.
     */
    public SCXML() {
        this.children = new LinkedHashMap<String, TransitionTarget>();
        this.targets = new HashMap<String, TransitionTarget>();
    }

    /**
     * Simple unique TransitionTarget id value generation
     * @return a unique TransitionTarget id for this SCXML instance
     */
    public final String generateTransitionTargetId() {
        return GENERATED_TT_ID_PREFIX +ttNextId++;
    }

    public final Script getGlobalScript() {
        return globalScript;
    }

    public final void setGlobalScript(Script script) {
        this.globalScript = script;
    }

    /**
     * Get the initial Transition.
     *
     * @return Returns the initial transition for this state machine.
     *
     * @since 2.0
     */
    public final Transition getInitialTransition() {
        return initialTransition;
    }

    /**
     * Set the initial Transition.
     * <p>Note: the initial transition can/may not have executable content!</p>
     *
     * @param initialTransition The initial transition to set.
     *
     * @since 2.0
     */
    public final void setInitialTransition(final Transition initialTransition) {
        this.initialTransition = initialTransition;
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
     * Get the immediate child targets of the SCXML root.
     *
     * @return Map Returns map of the child targets.
     *
     * @since 0.7
     */
    public final Map<String, TransitionTarget> getChildren() {
        return children;
    }

    /**
     * Get the first immediate child target of the SCXML root. Return null if there's no child.
     *
     * @return TransitionTarget Returns the first immediate child target of the SCXML root. Return null if there's no child.
     *
     * @since 2.0
     */
    public final TransitionTarget getFirstChild() {
        if (!children.isEmpty()) {
            return children.values().iterator().next();
        }
        return null;
    }

    /**
     * Add an immediate child target of the SCXML root.
     *
     * @param tt The transition target to be added to the states Map.
     *
     * @since 0.7
     */
    public final void addChild(final TransitionTarget tt) {
        children.put(tt.getId(), tt);
    }

    /**
     * Get the targets map, which is a Map of all States and Parallels
     * associated with this state machine, keyed by their id.
     *
     * @return Map Returns the targets.
     */
    public final Map<String, TransitionTarget> getTargets() {
        return targets;
    }

    /**
     * Add a target to this SCXML document.
     *
     * @param target The target to be added to the targets Map.
     */
    public final void addTarget(final TransitionTarget target) {
        targets.put(target.getId(), target);
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
     * Get the namespace definitions specified on the SCXML element.
     * May be <code>null</code>.
     *
     * @return The namespace definitions specified on the SCXML element,
     *         may be <code>null</code>.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the namespace definitions specified on the SCXML element.
     *
     * @param namespaces The namespace definitions specified on the
     *                   SCXML element.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the the initial transition target.
     *
     * @return String Returns the initial transition target ID
     * @see #getInitialTransition()
     */
    public final String getInitial() {
        return initial;
    }

    /**
     * Set the initial transition target.
     *
     * @param initial The initial transition target
     * @see #setInitialTransition(Transition)
     */
    public final void setInitial(final String initial) {
        this.initial = initial;
    }

    /**
     * Get the name for this state machine.
     *
     * @return The name for this state machine.
     */
	public String getName() {
		return name;
	}

	/**
	 * Set the name for this state machine.
	 *
	 * @param name The name for this state machine.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the profile in use for this state machine.
	 *
	 * @return The profile in use.
	 */
	public String getProfile() {
		return profile;
	}

	/**
	 * Set the profile in use for this state machine.
	 *
	 * @param profile The profile to be used.
	 */
	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * Get the exmode in use for this state machine.
	 *
	 * @return The exmode in use.
	 */
	public String getExmode() {
		return exmode;
	}

	/**
	 * Set the exmode to be used for this state machine.
	 *
	 * @param exmode The exmode to be used.
	 */
	public void setExmode(String exmode) {
		this.exmode = exmode;
	}

}

