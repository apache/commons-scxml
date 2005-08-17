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
package org.apache.commons.scxml;

import java.util.Iterator;

/**
 * A Context or &quot;scope&quot; for storing variables; usually tied to
 * a SCXML root or State object
 *
 */
public interface Context {
    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method searches the chain of parent Contexts for variable 
     * existence.
     * 
     * @param name The variable name
     * @param value The variable value
     */
    public void set(String name, Object value);

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method allows to shaddow a variable of the same name up the 
     * Context chain.
     * 
     * @param name The variable name
     * @param value The variable value
     */
    public void setLocal(String name, Object value);

    /**
     * Get the value of this variable; delegating to parent
     * 
     * @param name The name of the variable
     * @return The value (or null)
     */
    public Object get(String name);

    /**
     * Check if this variable exists, delegating to parent
     * 
     * @param name The name of the variable
     * @return Whether a variable with the name exists in this Context
     */
    public boolean has(String name);

    /**
     * Get an Iterator over all variables in this Context
     * 
     * @return Local entries iterator (Map.Entry)
     * To get parent entries, call getParent().iterator().
     * @see #getParent()
     */
    public Iterator iterator();

    /**
     * Clear this Context
     */
    public void reset();

    /**
     * Get the parent Context, may be null
     * 
     * @return The parent Context in a chained Context environment
     */
    public Context getParent();

}
