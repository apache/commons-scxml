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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * The status before this step.
     */
    private Status beforeStatus;

    /**
     * The status after this step.
     */
    private Status afterStatus;

    /**
     * The list of TransitionTargets that were exited during this step.
     */
    private List<EnterableState> exitList;

    /**
     * The list of TransitionTargets that were entered during this step.
     */
    private List<EnterableState> entryList;

    /**
     * The set of TransitionTargets that were entered during this step by default
     */
    private Set<EnterableState> defaultEntrySet;

    private Map<String, SimpleTransition> defaultHistoryTransitionEntryMap;
    /**
     * The list of Transitions taken during this step.
     */
    private List<SimpleTransition> transitList;

    /**
     * The set of activated states which invokes need to be invoked after the current macro step.
     */
    private Set<TransitionalState> statesToInvoke;

    /**
     * @param event The event received in this unit of progression
     * @param beforeStatus The before status
     */
    public Step(TriggerEvent event, final Status beforeStatus) {
        this.event = event;
        if (beforeStatus != null) {
            this.beforeStatus = beforeStatus;
        } else {
            this.beforeStatus = new Status();
        }
        this.afterStatus = new Status();
        this.exitList = new ArrayList<EnterableState>();
        this.entryList = new ArrayList<EnterableState>();
        this.defaultEntrySet = new HashSet<EnterableState>();
        this.defaultHistoryTransitionEntryMap = new HashMap<String, SimpleTransition>();
        this.transitList = new ArrayList<SimpleTransition>();
        this.statesToInvoke = new HashSet<TransitionalState>();
    }

    /**
     * @return Returns the afterStatus.
     */
    public Status getAfterStatus() {
        return afterStatus;
    }

    /**
     * @return Returns the beforeStatus.
     */
    public Status getBeforeStatus() {
        return beforeStatus;
    }

    /**
     * @return Returns the entryList.
     */
    public List<EnterableState> getEntryList() {
        return entryList;
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
    public Map<String, SimpleTransition> getDefaultHistoryTransitionEntryMap() {
        return defaultHistoryTransitionEntryMap;
    }

    /**
     * @return Returns the exitList.
     */
    public List<EnterableState> getExitList() {
        return exitList;
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

    /**
     * @return Returns the set of activated states which invokes need to be invoked after the current macro step.
     */
    public Set<TransitionalState> getStatesToInvoke() {
        return statesToInvoke;
    }
}

