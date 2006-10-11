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

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;transition&gt; SCXML element. Transition rules are triggered
 * by &quot;events&quot; and conditionalized via
 * &quot;guard-conditions&quot;.
 *
 */
public class Transition extends Executable {

    /**
     * Property that specifies the trigger for this transition.
     */
    private String event;

    /**
     * Optional guard condition.
     */
    private String cond;

    /**
     * Optional property that specifies the new state or parallel
     * element to transition to. May be specified by reference or in-line.
     */
    private TransitionTarget target;

    /**
     * The transition target ID (used by XML Digester only).
     */
    private String next;

    /**
     * The path for this transition.
     * @see Path
     */
    private Path path;

    /**
     * Constructor.
     */
    public Transition() {
        super();
        this.target = null;
        this.path = null;
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
     * Get the transition target (may be null).
     *
     * @return Returns the target as specified in SCXML markup.
     * <p>Remarks: Is <code>null</code> for &quot;stay&quot; transitions.
     *  Returns parent (the source node) for &quot;self&quot; transitions.</p>
     */
    public final TransitionTarget getTarget() {
        return target;
    }

    /**
     * Get the runtime transition target, which always resolves to
     * a TransitionTarget instance.
     *
     * @return Returns the actual target of a transition at runtime.
     * <p>Remarks: For both the &quot;stay&quot; and &quot;self&quot;
     * transitions it returns parent (the source node). This method should
     * never return <code>null</code>.</p>
     */
    public final TransitionTarget getRuntimeTarget() {
        if (target != null) {
            return target;
        }
        return getParent();
    }


    /**
     * Set the transition target.
     *
     * @param target The target to set.
     */
    public final void setTarget(final TransitionTarget target) {
        this.target = target;
    }

    /**
     * Get the ID of the transition target (may be null, if, for example,
     * the target is specified inline).
     *
     * @return String Returns the transition target ID
     *                (used by SCXML Digester only).
     * @see #getTarget()
     */
    public final String getNext() {
        return next;
    }

    /**
     * Set the transition target by specifying its ID.
     *
     * @param next The the transition target ID (used by SCXML Digester only).
     * @see #setTarget(TransitionTarget)
     */
    public final void setNext(final String next) {
        this.next = next;
    }

    /**
     * Get the path of this transiton.
     *
     * @see Path
     * @return Path returns the transition path
     */
    public final Path getPath() {
        if (path == null) {
            path = new Path(getParent(), getTarget());
        }
        return path;
    }

}

