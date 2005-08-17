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
 * An abstract base class for executable elements in SCXML,
 * such as &lt;assign&gt;, &lt;log&gt; etc. 
 * 
 */
public abstract class Action {
    
    /**
     * Link to its parent or container
     */
    private Executable parent;
    
    /**
     * Constructor
     */
    public Action() {
        super();
    }
    
    /**
     * Get the Executable parent
     * 
     * @return Returns the parent.
     */
    public Executable getParent() {
        return parent;
    }
    
    /**
     * Set the Executable parent
     * 
     * @param parent The parent to set.
     */
    public void setParent(Executable parent) {
        this.parent = parent;
    }

    /**
     * Return the parent state
     * 
     * @return The parent State
     */
    public State getParentState() throws ModelException {
        TransitionTarget tt = parent.getParent();
        if (tt instanceof State) {
            State st = (State) tt;
            return st;
        } else if (tt instanceof Parallel || tt instanceof History) {
            State st = (State) tt.getParent();
            return st;
        } else {
            throw new ModelException("Unknown TransitionTarget subclass:"
                    + tt.getClass().getName());
        }
    }
}
