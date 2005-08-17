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

import java.util.HashSet;
import java.util.Set;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;history&gt; SCXML pseudo state element.
 * 
 * @author Rahul Akolkar
 * @author Jaroslav Gergic
 */
public class History extends TransitionTarget {
    
    /**
     * Whether this is a shallow or deep history, the default is shallow.
     */
    private boolean isDeep;

    /**
     * A conditionless transition representing the default history state 
     * and indicates the state to transition to if the parent state has 
     * never been entered before.
     */
    private Transition transition;
    
    /**
     * The configuration when the parent of this pseudo state was last
     * exited.
     */
    private Set lastConfiguration;

    /**
     * Default no-args constructor for XML Digester
     */
    public History() {
        super();
    }
    
    /**
     * Get the transition
     * 
     * @return Returns the transition.
     */
    public Transition getTransition() {
        return transition;
    }
    
    /**
     * Set the transition
     * 
     * @param transition The transition to set.
     */
    public void setTransition(Transition transition) {
        this.transition = transition;
    }
    
    /**
     * Is this history &quot;deep&quot; (as against &quot;shallow&quot;)
     * 
     * @return Returns whether this is a &quot;deep&quot; history
     */
    public boolean isDeep() {
        return isDeep;
    }
    
    /**
     * This method is invoked by XML digester when parsing SCXML markup
     * 
     * @param type The history type, which can be &quot;shallow&quot; or 
     * &quot;deep&quot;
     */
    public void setType(String type) {
        if(type.equals("deep")) {
            isDeep = true;
        }
        //shallow is by default
    }
    
    /**
     * Get the last configuration for this history
     * 
     * @return Returns the lastConfiguration.
     */
    public Set getLastConfiguration() {
        return lastConfiguration;
    }
    
    /**
     * Set the last configuration for this history
     * 
     * @param lc The lastConfiguration to set.
     */
    public void setLastConfiguration(Set lc) {
        if(this.lastConfiguration == null) {
            this.lastConfiguration = new HashSet(lc.size());
        } else {
            this.lastConfiguration.clear();
        }
        this.lastConfiguration.addAll(lc);
    }
    
    /**
     * Check whether we have prior history
     * 
     * @return Whether we have a non-empty last configuration
     */
    public boolean isEmpty() {
        return (lastConfiguration == null || lastConfiguration.isEmpty()) ?
                true : false;
    }

}
