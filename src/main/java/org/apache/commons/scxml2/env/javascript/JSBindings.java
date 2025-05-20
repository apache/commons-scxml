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

package org.apache.commons.scxml2.env.javascript;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.apache.commons.scxml2.Context;

/**
 * JDK Javascript engine Bindings class that delegates to a SCXML context
 */
public class JSBindings implements Bindings {

    private JSContext  context;

    /**
     * Initialize the Bindings
     *
     * @param jsContext initial SCXML Context to use for script variables.
     * @throws IllegalArgumentException Thrown if <code>jsContext</code> is {@code null}.
     */
    public JSBindings(final JSContext jsContext) {
        setContext(jsContext);
    }

    /**
     * Does nothing - never invoked anyway
     */
    @Override
    public void clear() {
    }

    /**
     * Returns <code>true</code> if the SCXML context contains a variable identified by <code>key</code>.
     */
    @Override
    public boolean containsKey(final Object key) {
        return context.has(key.toString());
    }

    /**
     * Returns <code>true</code> if the SCXML context contains <code>value</code>.
     */
    @Override
    public boolean containsValue(final Object value) {
        return context.getVars().containsValue(value);
    }

    /**
     * Returns the SCXML context entry set.
     */
    @Override
    public Set<Map.Entry<String,Object>> entrySet() {
        return context.getVars().entrySet();
    }

    /**
     * Returns the value from the SCXML context identified by <code>key</code>.
     */
    @Override
    public Object get(final Object key) {
        return context.get(key.toString());
    }

    /**
     * Returns <code>true</code> if the SCXML context is empty.
     */
    @Override
    public boolean isEmpty() {
        return context.getVars().isEmpty();
    }

    /**
     * Returns the SCXML context key set
     */
    @Override
    public Set<String> keySet() {
        return context.getVars().keySet();
    }

    /**
     * The following delegation model is used to set values:
     * <ol>
     *   <li>Delegates to {@link Context#set(String,Object)} if the Context contains the key (name), else</li>
     *   <li>Delegates to {@link Context#setLocal(String, Object)}</li>
     * </ol>
     * @param name The variable name
     * @param value The variable value
     */
    @Override
    public Object put(final String name, final Object value) {
        Object old = null;
        if (context.has(name)) {
            old = context.get(name);
            context.set(name, value);
        } else {
            context.setLocal(name, value);
        }
        return old;
    }

    /**
     * Sets all entries in the provided map via {@link #put(String, Object)}
     * @param toMerge the map of variables to merge
     */
    @Override
    public void putAll(final Map<? extends String, ?> toMerge) {
        for (final Map.Entry<? extends String, ?> entry : toMerge.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes the named variable from the contained SCXML context.
     * @param name the variable name
     */
    @Override
    public Object remove(final Object name) {
        return context.getVars().remove(name);
    }

    /**
     * Sets or update the SCXML context delegate
     *
     * @param jsContext the SCXML context to use for script variables.
     * @throws IllegalArgumentException Thrown if <code>jsContext</code> is {@code null}.
     */
    public void setContext(final JSContext jsContext) {
        if (jsContext == null) {
            throw new IllegalArgumentException("SCXML context is required");
        }
        this.context = jsContext;
    }

    /**
     * Returns the size of the SCXML context size.
     */
    @Override
    public int size() {
        return context.getVars().size();
    }

    /**
     * Returns the SCXML context values.
     */
    @Override
    public Collection<Object> values() {
        return context.getVars().values();
    }
}
