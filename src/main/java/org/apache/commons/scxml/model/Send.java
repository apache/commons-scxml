/*
 *
 *   Copyright 2005 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.scxml.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;send&gt; SCXML element.
 *
 */
public class Send extends Action {

    /**
     * The ID of the send message.
     */
    private String sendId;

    /**
     * An expression returning the target location of the event.
     */
    private String target;

    /**
     * The type of the Event I/O Processor that the event.
     * should be dispatched to
     */
    private String targetType;

    /**
     * The event is dispatched after the delay interval elapses.
     */
    private String delay;

    /**
     * The data containing information which may be used by the
     * implementing platform to configure the event processor.
     */
    private String hints;

    /**
     * The namelist to the sent.
     */
    private String namelist;

    /**
     * The list of external nodes associated with this &lt;send&gt; element.
     */
    private List externalNodes;

    /**
     * The type of event being generated.
     */
    private String event;

    /**
     * Constructor.
     */
    public Send() {
        super();
        this.externalNodes = new ArrayList();
    }

    /**
     * @return Returns the delay.
     */
    public final String getDelay() {
        return delay;
    }

    /**
     * @param delay The delay to set.
     */
    public final void setDelay(final String delay) {
        this.delay = delay;
    }

    /**
     * @return List Returns the list of externalnodes.
     */
    public final List getExternalNodes() {
        return externalNodes;
    }

    /**
     * @param externalNodes The externalnode to set.
     */
    public final void setExternalNodes(final List externalNodes) {
        this.externalNodes = externalNodes;
    }

    /**
     * @return String Returns the hints.
     */
    public final String getHints() {
        return hints;
    }

    /**
     * @param hints The hints to set.
     */
    public final void setHints(final String hints) {
        this.hints = hints;
    }

    /**
     * @return String Returns the namelist.
     */
    public final String getNamelist() {
        return namelist;
    }

    /**
     * @param namelist The namelist to set.
     */
    public final void setNamelist(final String namelist) {
        this.namelist = namelist;
    }

    /**
     * @return String Returns the sendId.
     */
    public final String getSendId() {
        return sendId;
    }

    /**
     * @param sendId The sendId to set.
     */
    public final void setSendId(final String sendId) {
        this.sendId = sendId;
    }

    /**
     * @return String Returns the target.
     */
    public final String getTarget() {
        return target;
    }

    /**
     * @param target The target to set.
     */
    public final void setTarget(final String target) {
        this.target = target;
    }

    /**
     * @return String Returns the targetType.
     */
    public final String getTargetType() {
        return targetType;
    }

    /**
     * @param targetType The targetType to set.
     */
    public final void setTargetType(final String targetType) {
        this.targetType = targetType;
    }

    /**
     * @param event The event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * @return String Returns the event.
     */
    public final String getEvent() {
        return event;
    }

}

