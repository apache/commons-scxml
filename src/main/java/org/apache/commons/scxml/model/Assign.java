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
 * &lt;assign&gt; SCXML element.
 * 
 */
public class Assign extends Action {
    
    /**
     * Left hand side expression evaluating to a previously 
     * defined variable
     */
    private String name;

    /**
     * Expression evaluating to the new value of the variable.
     */
    private String expr;
    
    /**
     * Constructor
     */
    public Assign() {
        super();
    }
    
    /**
     * Get the expr that will evaluate to the new value
     * 
     * @return Returns the expr.
     */
    public String getExpr() {
        return expr;
    }
    
    /**
     * Set the expr that will evaluate to the new value
     * 
     * @param expr The expr to set.
     */
    public void setExpr(String expr) {
        this.expr = expr;
    }
    
    /**
     * Get the variable to be assigned a new value
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the variable to be assigned a new value
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
