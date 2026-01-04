/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
     * Constructs a new instance.
     */
    public SimpleContext() {
        this(null, null);
    }

    /**
     * Constructs a new instance.
     *
     * @param parent A parent Context, can be null
     */
    public SimpleContext(final Context parent) {
        this(parent, null);
    }

    /**
     * Constructs a new instance.
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
     * Gets the value of this variable; delegating to parent.
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
     * Gets the log used by this {@code Context} instance.
     *
     * @return Log The log being used.
     */
    protected Log getLog() {
        return log;
    }

    /**
     * Gets the parent Context, may be null.
     *
     * @return Context The parent Context
     * @see org.apache.commons.scxml2.Context#getParent()
     */
    @Override
    public Context getParent() {
        return parent;
    }

    /**
     * Gets the SCXMLSystemContext for this Context, should not be null unless this is the root Context
     *
     * @return The SCXMLSystemContext in a chained Context environment
     */
    @Override
    public final SCXMLSystemContext getSystemContext() {
        return systemContext;
    }

    /**
     * Gets the Map of all local variables in this Context.
     *
     * @return the vars.
     */
    @Override
    public Map<String, Object> getVars() {
        return vars;
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
        return hasLocal(name) || parent != null && parent.has(name);
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
        return getVars().containsKey(name);
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
        if (getVars().containsKey(name) || parent == null || !parent.has(name)) { //first try to override local
            setLocal(name, value);
        } else { //then check for global
            parent.set(name, value);
        }
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
     * Sets the variables map.
     *
     * @param vars The new Map of variables.
     */
    protected void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }
}

