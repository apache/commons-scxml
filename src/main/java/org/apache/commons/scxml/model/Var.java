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

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;var&gt; SCXML element.
 * 
 * @author Rahul Akolkar
 * @author Jaroslav Gergic
 */
public class Var extends Action {
    
    /**
     * The name of the variable to be created
     */
    private String name;

    /**
     * The expression that evaluates to the initial value of the variable
     */
    private String expr;
    
    /**
     * Constructor
     */
    public Var() {
        super();
    }
    
    /**
     * Get the expression that evaluates to the initial value 
     * of the variable
     * 
     * @return Returns the expr.
     */
    public String getExpr() {
        return expr;
    }
    
    /**
     * Set the expression that evaluates to the initial value 
     * of the variable
     * 
     * @param expr The expr to set.
     */
    public void setExpr(String expr) {
        this.expr = expr;
    }
    
    /**
     * Get the name of the (new) variable.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name of the (new) variable.
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
