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
        implements NamespacePrefixesHolder, Observable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The Transition type: internal or external (default)
     * @see {@link #isTypeInternal()}
     */
    private TransitionType type;

    /**
     * Derived effective Transition type.
     * @see #isTypeInternal()
     */
    private Boolean typeInternal;

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
    private List<TransitionTarget> targets;

    /**
     * The transition target ID (used by XML Digester only).
     */
    private String next;

    /**
     * The path(s) for this transition, one per target, in the same order
     * as <code>targets</code>.
     * @see Path
     */
    private List<Path> paths;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    /**
     * Constructor.
     */
    public Transition() {
        super();
        this.targets = new ArrayList<TransitionTarget>();
        this.paths = new ArrayList<Path>();
    }

    /**
     * @return true if Transition type == internal or false if type == external (default)
     */
    public final TransitionType getType() {
        return type;
    }

    /**
     * Sets the Transition type
     * @param type the Transition type
     */
    public final void setType(final TransitionType type) {
        this.type = type;
    }

    /**
     * Returns the effective Transition type.
     * <p>
     * A transition type is only effectively internal if:
     * <ul>
     *   <li>its {@link #getType()} == {@link TransitionType#internal}</li>
     *   <li>its source state {@link #getParent()} {@link State#isComposite()}</li>
     *   <li>all its {@link #getTargets()} are proper descendants of its {@link #getParent()}</li>
     * </ul>
     * Otherwise it is treated (for determining its exit states) as if it is of type {@link TransitionType#external}
     * </p>
     * @see <a href="http://www.w3.org/TR/2014/CR-scxml-20140313/#SelectingTransitions">
     *     http://www.w3.org/TR/2014/CR-scxml-20140313/#SelectingTransitions</a>
     * </p>
     * @return true if the effective Transition type is {@link TransitionType#internal}
     */
    public final boolean isTypeInternal() {
        if (typeInternal == null) {
            // derive typeInternal

            boolean internal = TransitionType.internal == type;

            if (internal) {
                internal = (getParent() != null && getParent() instanceof State && ((State)getParent()).isComposite());
            }

            if (internal && targets.size() > 0) {
                for (Path p : getPaths()) {
                    // TODO: testing the following actual works and always is correct
                    if (p.getPathScope() == null || p.getPathScope() == getParent()) {
                        continue;
                    }
                    // not a proper descendant
                    internal = false;
                    break;
                }
            }

            typeInternal = internal;
        }
        return typeInternal;
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
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
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
    public final List<TransitionTarget> getTargets() {
        return targets;
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
    public final List<TransitionTarget> getRuntimeTargets() {
        if (targets.size() == 0) {
            List<TransitionTarget> runtimeTargets = new ArrayList<TransitionTarget>();
            runtimeTargets.add(getParent());
            return runtimeTargets;
        }
        return targets;
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
     * Get the path(s) of this transiton.
     *
     * @see Path
     * @return List returns the list of transition path(s)
     *
     * @since 0.7
     */
    public final List<Path> getPaths() {
        if (paths.size() == 0) {
            if (targets.size() > 0) {
                int i = 0;
                for (TransitionTarget tt : targets) {
                    paths.add(i++, new Path(getParent(), tt));
                }
            } else {
                paths.add(new Path(getParent(), null));
            }
        }
        return paths;
    }
}

