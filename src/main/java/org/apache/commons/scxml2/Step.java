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
import java.util.Collection;
import java.util.List;

import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Transition;

/**
 * A logical unit of progression in the execution of a SCXML model.
 *
 */
public class Step {

    /**
     * The external events in this step.
     */
    private Collection<TriggerEvent> externalEvents;

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
     * The list of TransitionTargets that were entered during this step by default
     */
    private List<EnterableState> defaultEntryList;
    /**
     * The list of Transitions taken during this step.
     */
    private List<Transition> transitList;

    /**
     * Constructor.
      */
    public Step() {
        this(null, null);
    }

    /**
     * @param externalEvents The external events received in this
     *     unit of progression
     * @param beforeStatus The before status
     */
    public Step(final Collection<TriggerEvent> externalEvents, final Status beforeStatus) {
        if (externalEvents != null) {
            this.externalEvents = externalEvents;
        } else {
            this.externalEvents = new ArrayList<TriggerEvent>();
        }
        if (beforeStatus != null) {
            this.beforeStatus = beforeStatus;
        } else {
            this.beforeStatus = new Status();
        }
        this.afterStatus = new Status();
        this.exitList = new ArrayList<EnterableState>();
        this.entryList = new ArrayList<EnterableState>();
        this.defaultEntryList = new ArrayList<EnterableState>();
        this.transitList = new ArrayList<Transition>();
    }

    /**
     * @return Returns the afterStatus.
     */
    public Status getAfterStatus() {
        return afterStatus;
    }

    /**
     * @param afterStatus The afterStatus to set.
     */
    public void setAfterStatus(final Status afterStatus) {
        this.afterStatus = afterStatus;
    }

    /**
     * @return Returns the beforeStatus.
     */
    public Status getBeforeStatus() {
        return beforeStatus;
    }

    /**
     * @param beforeStatus The beforeStatus to set.
     */
    public void setBeforeStatus(final Status beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    /**
     * @return Returns the entryList.
     */
    public List<EnterableState> getEntryList() {
        return entryList;
    }

    /**
     * @return Returns the defaultEntryList.
     */
    public List<EnterableState> getDefaultEntryList() {
        return defaultEntryList;
    }

    /**
     * @return Returns the exitList.
     */
    public List<EnterableState> getExitList() {
        return exitList;
    }

    /**
     * @return Returns the externalEvents.
     */
    public Collection<TriggerEvent> getExternalEvents() {
        return externalEvents;
    }

    /**
     * @return Returns the transitList.
     */
    public List<Transition> getTransitList() {
        return transitList;
    }

}

