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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;parallel&gt; SCXML element, which is a wrapper element to
 * encapsulate parallel state machines. For the &lt;parallel&gt; element
 * to be useful, each of its &lt;state&gt; substates must itself be
 * complex, that is, one with either &lt;state&gt; or &lt;parallel&gt;
 * children.
 *
 */
public class Parallel extends TransitionTarget {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The set of parallel state machines contained in this &lt;parallel&gt;.
     */
    private Set children;

    /**
     * Constructor.
     */
    public Parallel() {
        this.children = new LinkedHashSet();
    }

    /**
     * Get the set of parallel state machines contained in this Parallel.
     *
     * @return Returns the state.
     *
     * @deprecated Use getChildren() instead.
     */
    public final Set getStates() {
        return children;
    }

    /**
     * Add a State to the list of parallel state machines contained
     * in this Parallel.
     *
     * @param state The state to add.
     *
     * @deprecated Use addChild(TransitionTarget) instead.
     */
    public final void addState(final State state) {
        if (state != null) {
            this.children.add(state);
            state.setParent(this);
        }
    }

    /**
     * Get the set of child transition targets (may be empty).
     *
     * @return Set Returns the children.
     *
     * @since 0.7
     */
    public final Set getChildren() {
        return children;
    }

    /**
     * Add a child.
     *
     * @param tt A child transition target.
     *
     * @since 0.7
     */
    public final void addChild(final TransitionTarget tt) {
        // TODO: State is a sufficient enough type for the parameter
        this.children.add(tt);
        tt.setParent(this);
    }

}

