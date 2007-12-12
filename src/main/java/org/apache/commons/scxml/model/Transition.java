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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;transition&gt; SCXML element. Transition rules are triggered
 * by &quot;events&quot; and conditionalized via
 * &quot;guard-conditions&quot;.
 *
 */
public class Transition extends Executable
        implements NamespacePrefixesHolder {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Property that specifies the trigger for this transition.
     */
    private String event;

    /**
     * Optional guard condition.
     */
    private String cond;

    /**
     * Optional property that specifies the new state(s) or parallel
     * element to transition to. May be specified by reference or in-line.
     * If multiple state(s) are specified, they must belong to the regions
     * of the same parallel.
     */
    private List targets;

    /**
     * The transition target ID (used by XML Digester only).
     */
    private String next;

    /**
     * The path(s) for this transition, one per target, in the same order
     * as <code>targets</code>.
     * @see Path
     */
    private List paths;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map namespaces;

    /**
     * Constructor.
     */
    public Transition() {
        super();
        this.targets = new ArrayList();
        this.paths = new ArrayList();
    }

    /**
     * Get the guard condition (may be null).
     *
     * @return Returns the cond.
     */
    public final String getCond() {
        return cond;
    }

    /**
     * Set the guard condition.
     *
     * @param cond The cond to set.
     */
    public final void setCond(final String cond) {
        this.cond = cond;
    }

    /**
     * Get the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @return Returns the event.
     */
    public final String getEvent() {
        return event;
    }

    /**
     * Set the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @param event The event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    public final Map getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    public final void setNamespaces(final Map namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the transition target (may be null).
     *
     * @return Returns the target as specified in SCXML markup.
     * <p>Remarks: Is <code>null</code> for &quot;stay&quot; transitions.
     *  Returns parent (the source node) for &quot;self&quot; transitions.</p>
     *
     * @deprecated A transition may have multiple targets,
     *             use getTargets() instead.
     */
    public final TransitionTarget getTarget() {
        if (targets.size() > 0) {
            return (TransitionTarget) targets.get(0);
        }
        return null;
    }

    /**
     * Get the list of transition targets (may be an empty list).
     *
     * @return Returns the target(s) as specified in SCXML markup.
     * <p>Remarks: Is <code>empty</code> for &quot;stay&quot; transitions.
     * Contains parent (the source node) for &quot;self&quot; transitions.</p>
     *
     * @since 0.7
     */
    public final List getTargets() {
        return targets;
    }

    /**
     * Get the runtime transition target, which always resolves to
     * a TransitionTarget instance.
     *
     * @return Returns the actual target of a transition at runtime.
     * <p>Remarks: For both the &quot;stay&quot; and &quot;self&quot;
     * transitions it returns parent (the source node). This method should
     * never return <code>null</code>.</p>
     *
     * @deprecated A transition may have multiple targets,
     *             use getRuntimeTargets() instead.
     */
    public final TransitionTarget getRuntimeTarget() {
        return (TransitionTarget) getRuntimeTargets().get(0);
    }

    /**
     * Get the list of runtime transition target, which always contains
     * atleast one TransitionTarget instance.
     *
     * @return Returns the actual targets of a transition at runtime.
     * <p>Remarks: For both the &quot;stay&quot; and &quot;self&quot;
     * transitions it returns parent (the source node). This method should
     * never return an empty list or <code>null</code>.</p>
     *
     * @since 0.7
     */
    public final List getRuntimeTargets() {
        if (targets.size() == 0) {
            List runtimeTargets = new ArrayList();
            runtimeTargets.add(getParent());
            return runtimeTargets;
        }
        return targets;
    }

    /**
     * Set the transition target.
     *
     * @param target The target to set.
     * @deprecated Use setTargets(List) instead.
     */
    public final void setTarget(final TransitionTarget target) {
        this.targets.add(0, target);
    }

    /**
     * Get the ID of the transition target (may be null, if, for example,
     * the target is specified inline).
     *
     * @return String Returns the transition target ID
     *                (used by SCXML Digester only).
     * @see #getTargets()
     */
    public final String getNext() {
        return next;
    }

    /**
     * Set the transition target by specifying its ID.
     *
     * @param next The the transition target ID (used by SCXML Digester only).
     */
    public final void setNext(final String next) {
        this.next = next;
    }

    /**
     * Get the path of this transiton.
     *
     * @see Path
     * @return Path returns the transition path
     * @deprecated Use getPaths() instead.
     */
    public final Path getPath() {
        return (Path) getPaths().get(0);
    }

    /**
     * Get the path(s) of this transiton.
     *
     * @see Path
     * @return List returns the list of transition path(s)
     *
     * @since 0.7
     */
    public final List getPaths() {
        if (paths.size() == 0) {
            if (targets.size() > 0) {
                for (int i = 0; i < targets.size(); i++) {
                    paths.add(i, new Path(getParent(),
                        (TransitionTarget) targets.get(i)));
                }
            } else {
                paths.add(new Path(getParent(), null));
            }
        }
        return paths;
    }
}

