/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.env;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCXMLSystemContext;

/**
 * Simple Context wrapping a map of variables.
 */
public class SimpleContext implements Context, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Implementation independent log category. */
    private static final Log log = LogFactory.getLog(Context.class);
    /** The parent Context to this Context. */
    private final Context parent;
    /** The Map of variables and their values in this Context. */
    private Map<String, Object> vars;

    protected final SCXMLSystemContext systemContext;

    /**
     * Constructor.
     *
     */
    public SimpleContext() {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     */
    public SimpleContext(final Context parent) {
        this(parent, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     * @param initialVars A pre-populated initial variables map
     */
    public SimpleContext(final Context parent, final Map<String, Object> initialVars) {
        this.parent = parent;
        this.systemContext = parent instanceof SCXMLSystemContext ?
                (SCXMLSystemContext) parent : parent != null ? parent.getSystemContext() : null;
        if (initialVars == null) {
            setVars(new HashMap<>());
        } else {
            setVars(this.vars = initialVars);
        }
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method searches the chain of parent Contexts for variable
     * existence.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml2.Context#set(String, Object)
     */
    @Override
    public void set(final String name, final Object value) {
        if (getVars().containsKey(name)) { //first try to override local
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
     * @see org.apache.commons.scxml2.Context#get(String)
     */
    @Override
    public Object get(final String name) {
        final Object localValue = getVars().get(name);
        if (localValue != null) {
            return localValue;
        }
        if (parent != null) {
            return parent.get(name);
        }
        return null;
    }

    /**
     * Check if this variable exists, delegating to parent.
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     * @see org.apache.commons.scxml2.Context#has(String)
     */
    @Override
    public boolean has(final String name) {
        return (hasLocal(name) || (parent != null && parent.has(name)));
    }

    /**
     * Check if this variable exists, only checking this Context
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     * @see org.apache.commons.scxml2.Context#hasLocal(String)
     */
    @Override
    public boolean hasLocal(final String name) {
        return (getVars().containsKey(name));
    }

    /**
     * Clear this Context.
     *
     * @see org.apache.commons.scxml2.Context#reset()
     */
    @Override
    public void reset() {
        getVars().clear();
    }

    /**
     * Get the parent Context, may be null.
     *
     * @return Context The parent Context
     * @see org.apache.commons.scxml2.Context#getParent()
     */
    @Override
    public Context getParent() {
        return parent;
    }

    /**
     * Get the SCXMLSystemContext for this Context, should not be null unless this is the root Context
     *
     * @return The SCXMLSystemContext in a chained Context environment
     */
    @Override
    public final SCXMLSystemContext getSystemContext() {
        return systemContext;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method allows to shaddow a variable of the same name up the
     * Context chain.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml2.Context#setLocal(String, Object)
     */
    @Override
    public void setLocal(final String name, final Object value) {
        getVars().put(name, value);
        if (log.isDebugEnabled()) {
            log.debug(name + " = " + String.valueOf(value));
        }
    }

    /**
     * Set the variables map.
     *
     * @param vars The new Map of variables.
     */
    protected void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

    /**
     * Get the Map of all local variables in this Context.
     *
     * @return Returns the vars.
     */
    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    /**
     * Get the log used by this <code>Context</code> instance.
     *
     * @return Log The log being used.
     */
    protected Log getLog() {
        return log;
    }
}

