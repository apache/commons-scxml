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

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;initial&gt; SCXML pseudo state element.
 *
 */
public class Initial implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The parent State of this initial
     */
    private State parent;

    /**
     * A conditionless transition that is always enabled and will be taken
     * as soon as the state is entered. The target of the transition must
     * be a descendant of the parent state of initial.
     */
    private SimpleTransition transition;

    /**
     * Indicator if this Initial was automatically generated and not loaded from the SCXML Document itself
     */
    private boolean generated;

    /**
     * Constructor.
     */
    public Initial() {
        super();
    }

    /**
     * Get the parent State.
     *
     * @return Returns the parent state
     */
    public final State getParent() {
        return parent;
    }


    /**
     * Set the parent TransitionTarget.
     *
     * @param parent The parent state to set
     */
    public final void setParent(final State parent) {
        this.parent = parent;
        if (transition != null) {
            transition.setParent(parent);
        }
    }

    /**
     * Get the initial transition.
     *
     * @return Returns the transition.
     */
    public final SimpleTransition getTransition() {
        return transition;
    }

    /**
     * Set the initial transition.
     *
     * @param transition The transition to set.
     */
    public final void setTransition(final SimpleTransition transition) {
        this.transition = transition;
        this.transition.setParent(getParent());
    }

    /**
     * @return true if this Initial was automatically generated and not loaded from the SCXML Document itself
     */
    public final boolean isGenerated() {
        return generated;
    }

    /**
     * Marks this Initial as automatically generated after loading the SCXML Document
     */
    public final void setGenerated() {
        this.generated = true;
    }
}

