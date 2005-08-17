/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
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
package org.apache.taglibs.rdc.scxml.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;send&gt; SCXML element.
 * 
 * @author Rahul Akolkar
 * @author Jaroslav Gergic
 */
public class Send extends Action {
    
    /**
     * The ID of the send message
     */
    private String sendId;
    
    /**
     * An expression returning the target location of the event
     */
    private String target;

    /**
     * The type of the Event I/O Processor that the event
     * should be dispatched to
     */
    private String targetType;

    /**
     * The event is dispatched after the delay interval elapses
     */
    private String delay;

    /**
     * The data containing information which may be used by the 
     * implementing platform to configure the event processor
     */
    private String hints;

    /**
     * The namelist to the sent
     */
    private String namelist;

    /**
     * The list of external nodes associated with this &lt;send&gt; element
     */
    private List externalNodes;

    /**
     * The type of event being generated
     */
    private String event;
    
    /**
     * Constructor
     */
    public Send() {
        super();
        this.externalNodes = new ArrayList();
    }
    
    /**
     * @return Returns the delay.
     */
    public String getDelay() {
        return delay;
    }
    
    /**
     * @param delay The delay to set.
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }
    
    /**
     * @return Returns the externalnode.
     */
    public List getExternalNodes() {
        return externalNodes;
    }
    
    /**
     * @param externalnode The externalnode to set.
     */
    public void setExternalNodes(List externalNodes) {
        this.externalNodes = externalNodes;
    }
    
    /**
     * @return Returns the hints.
     */
    public String getHints() {
        return hints;
    }
    
    /**
     * @param hints The hints to set.
     */
    public void setHints(String hints) {
        this.hints = hints;
    }
    
    /**
     * @return Returns the namelist.
     */
    public String getNamelist() {
        return namelist;
    }
    
    /**
     * @param namelist The namelist to set.
     */
    public void setNamelist(String namelist) {
        this.namelist = namelist;
    }
    
    /**
     * @return Returns the sendId.
     */
    public String getSendId() {
        return sendId;
    }
    
    /**
     * @param sendId The sendId to set.
     */
    public void setSendId(String sendId) {
        this.sendId = sendId;
    }
    
    /**
     * @return Returns the target.
     */
    public String getTarget() {
        return target;
    }
    
    /**
     * @param target The target to set.
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * @return Returns the targetType.
     */
    public String getTargetType() {
        return targetType;
    }
    
    /**
     * @param targetType The targetType to set.
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * @param event The event to set.
     */
    public void setEvent(String event) {
        this.event = event;
    }

    /**
     * @return Returns the event.
     */
    public String getEvent() {
        return event;
    }
    
}
