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
package org.apache.commons.scxml.env;

import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.Context;

/** 
 * EL Context for SCXML interpreter.
 * 
 */
public class ELContext implements Context , VariableResolver {
    
    //let's make the log category implementation independent
    protected static Log log = LogFactory.getLog(Context.class);
    
    protected Context parent = null;
    protected HashMap vars = new HashMap();
    
    /**
     * Constructor
     *
     */
    public ELContext() {
        this(null);
    }

    /**
     * @param parent a parent ELContext, can be null 
     */
    public ELContext(Context parent) {
        this.parent = parent;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method searches the chain of parent Contexts for variable 
     * existence.
     * 
     * @see org.apache.commons.scxml.Context#set(java.lang.String, java.lang.Object)
     */
    public void set(String name, Object value) {
        if(vars.containsKey(name)) { //first try to override local
            setLocal(name, value);
        } else if(parent != null && parent.has(name)) { //then check for global
            parent.set(name, value);
        } else { //otherwise create a new local variable
            setLocal(name, value);
        }
    }

    /**
     * Get the value of this variable; delegating to parent
     * 
     * @see org.apache.commons.scxml.Context#get(java.lang.String)
     */
    public Object get(String name) {
        if(vars.containsKey(name)) {
            return vars.get(name);
        } else if(parent != null) {
            return parent.get(name);
        } else {
            return null;
        }
    }

    /**
     * Check if this variable exists, delegating to parent
     * 
     * @see org.apache.commons.scxml.Context#has(java.lang.String)
     */
    public boolean has(String name) {
        if(vars.containsKey(name)) {
            return true;
        } else if(parent != null && parent.has(name)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get an Iterator over all variables in this Context
     * 
     * @see org.apache.commons.scxml.Context#iterator()
     */
    public Iterator iterator() {
        return vars.entrySet().iterator();
    }

    /**
     * Clear this Context
     * 
     * @see org.apache.commons.scxml.Context#reset()
     */
    public void reset() {
        vars.clear();
    }

    /**
     * Get the parent Context, may be null
     * 
     * @see org.apache.commons.scxml.Context#getParent()
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method allows to shaddow a variable of the same name up the 
     * Context chain.
     * 
     * @see org.apache.commons.scxml.Context#setLocal(java.lang.String, java.lang.Object)
     */
    public void setLocal(String name, Object value) {
        vars.put(name, value);
        if(log.isDebugEnabled() && !name.equals("_ALL_STATES")) {
            log.debug(name + " = " + String.valueOf(value));
        }
    }

    /**
     * Resolves the specified variable. Returns null if the variable is 
     * not found. 
     * 
     * @see javax.servlet.jsp.el.VariableResolver#resolveVariable(java.lang.String)
     */
    public Object resolveVariable(String pName) throws ELException {
        return get(pName);
    }

}
