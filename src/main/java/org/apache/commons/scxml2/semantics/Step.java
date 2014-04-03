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
package org.apache.commons.scxml2.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.SimpleTransition;
import org.apache.commons.scxml2.model.TransitionalState;

/**
 * A logical unit of progression in the execution of a SCXML model.
 *
 */
public class Step {

    /**
     * The event in this step.
     */
    private TriggerEvent event;

    /**
     * The set of states that were exited during this step.
     */
    private Set<EnterableState> exitSet;

    /**
     * The set of states that were entered during this step.
     */
    private Set<EnterableState> entrySet;

    /**
     * The set of states that were entered during this step by default
     */
    private Set<EnterableState> defaultEntrySet;

    private Map<TransitionalState, SimpleTransition> defaultHistoryTransitionEntryMap;
    /**
     * The list of Transitions taken during this step.
     */
    private List<SimpleTransition> transitList;

    /**
     * @param event The event received in this unit of progression
     */
    public Step(TriggerEvent event) {
        this.event = event;
        this.exitSet = new HashSet<EnterableState>();
        this.entrySet = new HashSet<EnterableState>();
        this.defaultEntrySet = new HashSet<EnterableState>();
        this.defaultHistoryTransitionEntryMap = new HashMap<TransitionalState, SimpleTransition>();
        this.transitList = new ArrayList<SimpleTransition>();
    }

    /**
     * @return Returns the entrySet.
     */
    public Set<EnterableState> getEntrySet() {
        return entrySet;
    }

    /**
     * @return Returns the defaultEntrySet.
     */
    public Set<EnterableState> getDefaultEntrySet() {
        return defaultEntrySet;
    }

    /**
     * @return Returns the defaultHistoryTransitionEntryMap.
     */
    public Map<TransitionalState, SimpleTransition> getDefaultHistoryTransitionEntryMap() {
        return defaultHistoryTransitionEntryMap;
    }

    /**
     * @return Returns the exitSet.
     */
    public Set<EnterableState> getExitSet() {
        return exitSet;
    }

    /**
     * @return Returns the current event.
     */
    public TriggerEvent getEvent() {
        return event;
    }

    /**
     * @return Returns the transitList.
     */
    public List<SimpleTransition> getTransitList() {
        return transitList;
    }
}

