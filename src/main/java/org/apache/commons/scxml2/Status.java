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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Final;

/**
 * The encapsulation of the current state of a state machine.
 *
 */
public class Status implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The states that are currently active.
     */
    private final Set<EnterableState> states = new HashSet<EnterableState>();

    /**
     * @return Whether the state machine terminated AND we reached a top level Final state.
     */
    public boolean isFinal() {
        return getFinalState() != null;
    }

    /**
     * @return Returns the single top level final state in which the state machine terminated, or null otherwise
     */
    public Final getFinalState() {
        if (states.size() == 1) {
            EnterableState es = states.iterator().next();
            if (es instanceof Final && es.getParent() == null) {
                return (Final)es;
            }
        }
        return null;
    }

    /**
     * Get the states configuration (leaf only).
     *
     * @return Returns the states configuration - simple (leaf) states only.
     */
    public Set<EnterableState> getStates() {
        return states;
    }

    /**
     * Get the complete states configuration.
     *
     * @return complete states configuration including simple states and their
     *         complex ancestors up to the root.
     */
    public Set<EnterableState> getAllStates() {
        Set<EnterableState> allStates = new HashSet<EnterableState>(states.size() * 2);
        for (EnterableState es : states) {
            EnterableState state = es;
            while (state != null && allStates.add(state)) {
                state = state.getParent();
            }
        }
        return allStates;
    }

    /**
     * Clears the status
     */
    public void clear() {
        states.clear();
    }
}

