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
package org.apache.commons.scxml.model;

import org.apache.commons.scxml.NotificationRegistry;
import org.apache.commons.scxml.Observable;
import org.apache.commons.scxml.SCXMLListener;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;transition&gt; SCXML element. Transition rules are triggered 
 * by &quot;events&quot; and conditionalized via 
 * &quot;guard-conditions&quot;.
 * 
 */
public class Transition extends Executable implements Observable {
    
    /**
     * Property that specifies the trigger for this transition.
     */
    private String event;

    /**
     * Optional guard condition.
     */
    private String cond;

    /**
     * Optional property that specifies the new state or parallel 
     * element to transition to. May be specified by reference or in-line.
     */
    private TransitionTarget target;

    /**
     * The transition target ID (used by XML Digester only)
     */
    private String next;
    
    /**
     * The notification registry.
     */
    private NotificationRegistry notifReg;

    /**
     * The path for this transition.
     * @see Path
     */    
    private Path path = null;
    
    /**
     * Constructor
     */
    public Transition() {
        super();
    }

    /**
     * Register a listener to this document root
     * 
     * @param lst The SCXMLListener to add
     */
    public void addListener(SCXMLListener lst) {
        notifReg.addListener(this, lst);
    }

    /**
     * Deregister a listener from this document root
     * 
     * @param lst The SCXMLListener to remove
     */
    public void removeListener(SCXMLListener lst) {
        notifReg.removeListener(this, lst);
    }

    /**
     * Get the guard condition (may be null)
     * 
     * @return Returns the cond.
     */
    public String getCond() {
        return cond;
    }
    
    /**
     * Set the guard condition
     * 
     * @param cond The cond to set.
     */
    public void setCond(String cond) {
        this.cond = cond;
    }
    
    /**
     * Get the event that will trigger this transition (pending 
     * evaluation of the guard condition in favor)
     * 
     * @return Returns the event.
     */
    public String getEvent() {
        return event;
    }
    
    /**
     * Set the event that will trigger this transition (pending 
     * evaluation of the guard condition in favor)
     * 
     * @param event The event to set.
     */
    public void setEvent(String event) {
        this.event = event;
    }
    
    /**
     * Get the transition target (may be null)
     * 
     * @return Returns the target as specified in SCXML markup.
     * <p>Remarks: Is <code>null</code> for &quot;stay&quot; transitions.
     *  Returns parent (the source node) for &quot;self&quot; transitions.</p>
     */
    public TransitionTarget getTarget() {
        return target;
    }
    
    /**
     * Get the runtime transition target, which always resolves to
     * a TransitionTarget instance.
     * 
     * @return Returns the actual target of a transition at runtime.
     * <p>Remarks: For both the &quot;stay&quot; and &quot;self&quot; 
     * transitions it returns parent (the source node). This method should 
     * never return <code>null</code>.</p>
     */
    public TransitionTarget getRuntimeTarget() {
        return (target != null) ? target : parent;
    }

    
    /**
     * Set the transition target
     * 
     * @param target The target to set.
     */
    public void setTarget(TransitionTarget target) {
        this.target = target;
    }
    
    /**
     * Get the ID of the transition target (may be null, if, for example,
     * the target is specified inline)
     * 
     * @return Returns the transition target ID (used by SCXML Digester only).
     * @see #getTarget()
     */
    public String getNext() {
        return next;
    }
    
    /**
     * Set the transition target by specifying its ID
     * 
     * @param next The the transition target ID (used by SCXML Digester only).
     * @see #setTarget(TransitionTarget)
     */
    public void setNext(String next) {
        this.next = next;
    }
    
    /**
     * Supply this Transition object a handle to the notification
     * registry. Called by the Digester after instantiation.
     * 
     * @param reg The notification registry
     */
    public void setNotificationRegistry(NotificationRegistry reg) {
        notifReg = reg;
    }
    
    /**
     * Get the notification registry.
     * 
     * @return The notification registry.
     */
    public NotificationRegistry getNotificationRegistry() {
        return notifReg;
    }

    /**
     * Get the path of this transiton.
     * 
     * @see Path
     * @return returns the transition path
     */
    public Path getPath() {
        if(path == null) {
            path = new Path(getParent(), getTarget());
        }
        return path;
    }
    
}
