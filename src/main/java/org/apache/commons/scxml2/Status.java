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
import java.util.Set;

import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Final;

/**
 * The immutable encapsulation of the current state of a state machine.
 *
 */
public class Status implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private final StateConfiguration configuration;


    public Status(StateConfiguration configuration) {
        this.configuration = configuration;
    }

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
        if (configuration.getStates().size() == 1) {
            EnterableState es = configuration.getStates().iterator().next();
            if (es instanceof Final && es.getParent() == null) {
                return (Final)es;
            }
        }
        return null;
    }

    /**
     * Get the atomic states configuration (leaf only).
     *
     * @return Returns the atomic states configuration - simple (leaf) states only.
     */
    public Set<EnterableState> getStates() {
        return configuration.getStates();
    }

    /**
     * Get the active states configuration.
     *
     * @return active states configuration including simple states and their
     *         complex ancestors up to the root.
     */
    public Set<EnterableState> getActiveStates() {
        return configuration.getActiveStates();
    }

    public boolean isInState(final String state) {
        for (EnterableState es : configuration.getActiveStates()) {
            if (state.equals(es.getId())) {
                return true;
            }
        }
        return false;
    }
}

