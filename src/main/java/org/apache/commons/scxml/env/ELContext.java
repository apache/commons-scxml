/*
 *
 *   Copyright 2005 The Apache Software Foundation.
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
import java.util.Map;

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

    /** Implementation independent log category. */
    protected static final Log LOG = LogFactory.getLog(Context.class);
    /** The parent Context to this Context. */
    protected Context parent = null;
    /** The Map of variables and their values in this Context. */
    protected Map vars = new HashMap();

    /**
     * Constructor.
     *
     */
    public ELContext() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param parent a parent ELContext, can be null
     */
    public ELContext(final Context parent) {
        this.parent = parent;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method searches the chain of parent Contexts for variable
     * existence.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml.Context#set(String, Object)
     */
    public void set(final String name, final Object value) {
        if (vars.containsKey(name)) { //first try to override local
            setLocal(name, value);
        } else if (parent != null && parent.has(name)) { //then check for global
            parent.set(name, value);
        } else { //otherwise create a new local variable
            setLocal(name, value);
        }
    }

    /**
     * Get the value of this variable; delegating to parent.
     *
     * @param name The variable name
     * @return Object The variable value
     * @see org.apache.commons.scxml.Context#get(java.lang.String)
     */
    public Object get(final String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            return null;
        }
    }

    /**
     * Check if this variable exists, delegating to parent.
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     * @see org.apache.commons.scxml.Context#has(java.lang.String)
     */
    public boolean has(final String name) {
        if (vars.containsKey(name)) {
            return true;
        } else if (parent != null && parent.has(name)) {
            return true;
        }
        return false;
    }

    /**
     * Get an Iterator over all variables in this Context.
     *
     * @return Iterator The Iterator over all variables in this Context
     * @see org.apache.commons.scxml.Context#iterator()
     */
    public Iterator iterator() {
        return vars.entrySet().iterator();
    }

    /**
     * Clear this Context.
     *
     * @see org.apache.commons.scxml.Context#reset()
     */
    public void reset() {
        vars.clear();
    }

    /**
     * Get the parent Context, may be null.
     *
     * @return Context The parent Context
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
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml.Context#setLocal(String, Object)
     */
    public void setLocal(final String name, final Object value) {
        vars.put(name, value);
        if (LOG.isDebugEnabled() && !name.equals("_ALL_STATES")) {
            LOG.debug(name + " = " + String.valueOf(value));
        }
    }

    /**
     * Resolves the specified variable. Returns null if the variable is
     * not found.
     *
     * @param pName The variable to resolve
     * @return Object The value of the variable, or null, if it does not
     *                exist
     * @throws ELException While resolving the variable
     * @see javax.servlet.jsp.el.VariableResolver#resolveVariable(String)
     */
    public Object resolveVariable(final String pName) throws ELException {
        return get(pName);
    }

}

