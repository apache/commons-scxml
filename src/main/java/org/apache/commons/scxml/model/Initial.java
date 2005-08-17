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

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;initial&gt; SCXML pseudo state element.
 * 
 */
public class Initial extends TransitionTarget {
    
    /**
     * Constructor
     */
    public Initial() {
        super();
    }
    
    /**
     * A conditionless transition that is always enabled and will be taken 
     * as soon as the state is entered. The target of the transition must 
     * be a descendant of the parent state of initial.
     */
    private Transition transition;
    
    /**
     * Get the initial transition
     * 
     * @return Returns the transition.
     */
    public Transition getTransition() {
        return transition;
    }
    
    /**
     * Set the initial transition
     * 
     * @param transition The transition to set.
     */
    public void setTransition(Transition transition) {
        this.transition = transition;
    }
    
}
