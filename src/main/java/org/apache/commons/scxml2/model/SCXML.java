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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.scxml2.PathResolver;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;scxml&gt; root element, and serves as the &quot;document
 * root&quot;.
 */
public class SCXML implements Serializable, Observable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Reserved prefix for auto generated TransitionTarget id values
     */
    public static final String GENERATED_TT_ID_PREFIX = "_generated_tt_id_";

    /**
     * The predefined observableId with value 0 (zero) for this SCXML state machine
     */
    private static final Integer SCXML_OBSERVABLE_ID = 0;

    /**
     * The SCXML version of this document.
     */
    private String version;

    /**
     * The initial Transition for the SCXML executor.
     */
    private SimpleTransition initialTransition;

    /**
     * The initial transition target ID
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
     * optional flag indicating if this document uses late or early (default) binding
     */
    private Boolean lateBinding;

    /**
     * The datamodel name as specified as "datamodel" attribute on this document
     */
    private String datamodelName;

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
     * used to resolve SCXML context sensitive paths
     */
    private PathResolver pathResolver;

    /**
     * The immediate child targets of this SCXML document root.
     */
    private final List<EnterableState> children;

    /**
     * A global map of all States and Parallels associated with this
     * state machine, keyed by their id.
     */
    private final Map<String, TransitionTarget> targets;

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
     * Constructs a new instance.
     */
    public SCXML() {
        this.children = new ArrayList<>();
        this.targets = new HashMap<>();
    }

    /**
     * Add an immediate child of the SCXML root.
     *
     * @param es The child to be added.
     * @since 0.7
     */
    public final void addChild(final EnterableState es) {
        children.add(es);
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
     * Simple unique TransitionTarget id value generation
     * @return a unique TransitionTarget id for this SCXML instance
     */
    public final String generateTransitionTargetId() {
        return GENERATED_TT_ID_PREFIX +ttNextId++;
    }

    /**
     * Gets the immediate child targets of the SCXML root.
     *
     * @return List Returns list of the child targets.
     * @since 0.7
     */
    public final List<EnterableState> getChildren() {
        return children;
    }

    /**
     * Gets the data model placed at document root.
     *
     * @return the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Gets the datamodel name as specified as attribute on this document
     * @return The datamodel name of this document
     */
    public String getDatamodelName() {
        return datamodelName;
    }

    /**
	 * Gets the exmode in use for this state machine.
	 *
	 * @return The exmode in use.
	 */
	public String getExmode() {
		return exmode;
	}

    /**
     * Gets the first immediate child of the SCXML root. Return null if there's no child.
     *
     * @return the first immediate child of the SCXML root. Return null if there's no child.
     * @since 2.0
     */
    public final EnterableState getFirstChild() {
        if (!children.isEmpty()) {
            return children.get(0);
        }
        return null;
    }

    public final Script getGlobalScript() {
        return globalScript;
    }

    /**
     * Gets the initial transition target.
     *
     * @return String Returns the initial transition target ID
     * @see #getInitialTransition()
     */
    public final String getInitial() {
        return initial;
    }

    /**
     * Gets the initial Transition.
     *
     * @return the initial transition for this state machine.
     * @since 2.0
     */
    public final SimpleTransition getInitialTransition() {
        return initialTransition;
    }

    /**
     * Gets the name for this state machine.
     *
     * @return The name for this state machine.
     */
	public String getName() {
		return name;
	}

    /**
     * Gets the namespace definitions specified on the SCXML element.
     * May be {@code null}.
     *
     * @return The namespace definitions specified on the SCXML element,
     *         may be {@code null}.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer getObservableId() {
        return SCXML_OBSERVABLE_ID;
    }

    /**
     * Gets the {@link PathResolver}.
     *
     * @return the pathResolver.
     */
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
	 * Gets the profile in use for this state machine.
	 *
	 * @return The profile in use.
	 */
	public String getProfile() {
		return profile;
	}

    /**
     * Gets the targets map, which is a Map of all States and Parallels
     * associated with this state machine, keyed by their id.
     *
     * @return Map Returns the targets.
     */
    public final Map<String, TransitionTarget> getTargets() {
        return targets;
    }

    /**
     * Gets the SCXML document version.
     *
     * @return the version.
     */
    public final String getVersion() {
        return version;
    }

    public final Boolean isLateBinding() {
        return lateBinding;
    }

    /**
     * Sets the data model at document root.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
    }

    /**
     * Sets the datamodel name as specified as attribute on this document
     * @param datamodelName The datamodel name
     */
    public void setDatamodelName(final String datamodelName) {
        this.datamodelName = datamodelName;
    }

    /**
	 * Sets the exmode to be used for this state machine.
	 *
	 * @param exmode The exmode to be used.
	 */
	public void setExmode(final String exmode) {
		this.exmode = exmode;
	}

    public final void setGlobalScript(final Script script) {
        this.globalScript = script;
    }

    /**
     * Sets the initial transition target.
     *
     * @param initial The initial transition target
     * @see #setInitialTransition(SimpleTransition)
     */
    public final void setInitial(final String initial) {
        this.initial = initial;
    }

	/**
     * Sets the initial Transition.
     * <p>Note: the initial transition can/may not have executable content!</p>
     *
     * @param initialTransition The initial transition to set.
     * @since 2.0
     */
    public final void setInitialTransition(final SimpleTransition initialTransition) {
        this.initialTransition = initialTransition;
    }

	public final void setLateBinding(final Boolean lateBinding) {
        this.lateBinding = lateBinding;
    }

	/**
	 * Sets the name for this state machine.
	 *
	 * @param name The name for this state machine.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
     * Sets the namespace definitions specified on the SCXML element.
     *
     * @param namespaces The namespace definitions specified on the
     *                   SCXML element.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

	/**
     * Sets the {@link PathResolver}.
     *
     * @param pathResolver The pathResolver to set.
     */
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    /**
	 * Sets the profile in use for this state machine.
	 *
	 * @param profile The profile to be used.
	 */
	public void setProfile(final String profile) {
		this.profile = profile;
	}

    /**
     * Sets the SCXML document version.
     *
     * @param version The version to set.
     */
    public final void setVersion(final String version) {
        this.version = version;
    }
}

