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
package org.apache.commons.scxml2;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.scxml2.model.EnterableState;

/**
 * The current active states of a state machine
 */
public class StateConfiguration implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The states that are currently active.
     */
    private final Set<EnterableState> activeStates = new HashSet<EnterableState>();
    private final Set<EnterableState> activeStatesSet = Collections.unmodifiableSet(activeStates);

    /**
     * The atomic states that are currently active.
     */
    private final Set<EnterableState> atomicStates = new HashSet<EnterableState>();
    private final Set<EnterableState> atomicStatesSet = Collections.unmodifiableSet(atomicStates);

    /**
     * Get the active states
     *
     * @return active states including simple states and their
     *         complex ancestors up to the root.
     */
    public Set<EnterableState> getActiveStates() {
        return  activeStatesSet;
    }

    /**
     * Get the current atomic states (leaf only).
     *
     * @return Returns the atomic states - simple (leaf) states only.
     */
    public Set<EnterableState> getStates() {
        return  atomicStatesSet;
    }

    /**
     * Enter an active state
     * If the state is atomic also record it add it to the current states
     * @param state state to enter
     */
    public void enterState(final EnterableState state) {
        if (!activeStates.add(state)) {
            throw new IllegalStateException("State "+state.getId()+" already added.");
        }
        if (state.isAtomicState()) {
            if (!atomicStates.add(state)) {
                throw new IllegalStateException("Atomic state "+state.getId()+" already added.");
            }
        }
    }

    /**
     * Exit an active state
     * If the state is atomic also remove it from current states
     * @param state state to exit
     */
    public void exitState(final EnterableState state) {
        if (!activeStates.remove(state)) {
            throw new IllegalStateException("State "+state.getId()+" not active.");
        }
        atomicStates.remove(state);
    }

    /**
     * Clear the state configuration
     */
    public void clear() {
        activeStates.clear();
        atomicStates.clear();
    }
}
