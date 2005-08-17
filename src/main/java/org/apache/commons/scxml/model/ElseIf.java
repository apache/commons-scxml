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
 * &lt;elseif&gt; SCXML element.
 * 
 */
public class ElseIf extends Action {
    
    /**
     * An conditional expression which can be evaluated to true or false.
     */
    private String cond;
    
    /**
     * Constructor
     */
    public ElseIf() {
        super();
    }
    
    /**
     * Get the conditional expression
     * 
     * @return Returns the cond.
     */
    public String getCond() {
        return cond;
    }
    
    /**
     * Set the conditional expression
     * 
     * @param cond The cond to set.
     */
    public void setCond(String cond) {
        this.cond = cond;
    }
    
}
