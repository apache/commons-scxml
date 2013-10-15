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

package org.apache.commons.scxml2.env.javascript;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.commons.scxml2.Context;

/**
 * Wrapper class for the JDK Javascript engine Bindings class that extends the
 * wrapped Bindings to search the SCXML context for variables and predefined
 * functions that do not exist in the wrapped Bindings.
 *
 */
public class JSBindings implements Bindings {

    // INSTANCE VARIABLES

    private Bindings bindings;
    private Context  context;

    // CONSTRUCTORS

    /**
     * Initialises the internal Bindings delegate and SCXML context.
     *
     * @param context  SCXML Context to use for script variables.
     * @param bindings Javascript engine bindings for Javascript variables.
     *
     * @throws IllegalArgumentException Thrown if either <code>context</code>
     *         or <code>bindings</code> is <code>null</code>.
     *
     */
    public JSBindings(Context context,Bindings bindings) {
        // ... validate

        if (context == null) {
           throw new IllegalArgumentException("Invalid SCXML context");
        }

        if (bindings == null) {
           throw new IllegalArgumentException("Invalid script Bindings");
        }

         // ... initialise

         this.bindings = bindings;
         this.context  = context;
    }

    // INSTANCE METHODS

    /**
     * Returns <code>true</code> if the wrapped Bindings delegate
     * or SCXML context  contains a variable identified by
     * <code>key</code>.
     *
     */
    @Override
    public boolean containsKey(Object key) {
        if (bindings.containsKey(key))
           return true;

        return context.has(key.toString());
    }

    /**
     * Returns a union of the wrapped Bindings entry set and the
     * SCXML context entry set.
     * <p>
     * NOTE: doesn't seem to be invoked ever. Not thread-safe.
     *
     */
    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();

        keys.addAll(context.getVars().keySet());
        keys.addAll(bindings.keySet());

        return keys;
    }

    /**
     * Returns the combined size of the wrapped Bindings entry set and the
     * SCXML context entry set.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     *
     */
    @Override
    public int size() {
        Set<String> keys = new HashSet<String>();

        keys.addAll(context.getVars().keySet());
        keys.addAll(bindings.keySet());

        return keys.size();
    }

    /**
     * Returns <code>true</code> if the wrapped Bindings delegate
     * or SCXML context contains <code>value</code>.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public boolean containsValue(Object value) {
        if (bindings.containsValue(value))
           return true;

        return context.getVars().containsValue(value);
    }

    /**
     * Returns a union of the wrapped Bindings entry set and the
     * SCXML context entry set.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public Set<Map.Entry<String,Object>> entrySet() {
        return union().entrySet();
    }

    /**
     * Returns a union of the wrapped Bindings value list and the
     * SCXML context value list.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public Collection<Object> values() {
        return union().values();
    }

    /**
     * Returns a <code>true</code> if both the Bindings delegate and
     * the SCXML context maps are empty.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public boolean isEmpty() {
        if (!bindings.isEmpty())
           return false;

        return context.getVars().isEmpty();
    }

    /**
     * Returns the value from the wrapped Bindings delegate
     * or SCXML context contains identified by <code>key</code>.
     *
     */
    @Override
    public Object get(Object key) {
        if (bindings.containsKey(key))
           return bindings.get(key);

        return context.get(key.toString());
    }

    /**
     * The following delegation model is used to set values:
     * <ol>
     *   <li>Delegates to {@link Context#set(String,Object)} if the
     *       {@link Context} contains the key (name), else</li>
     *   <li>Delegates to the wrapped {@link Bindings#put(String, Object)}
     *       if the {@link Bindings} contains the key (name), else</li>
     *   <li>Delegates to {@link Context#setLocal(String, Object)}</li>
     * </ol>
     *
     */
    @Override
    public Object put(String name, Object value) {
        Object old = context.get(name);
        if (context.has(name)) {
            context.set(name, value);
        } else if (bindings.containsKey(name)) {
            return bindings.put(name,value);
        } else {
            context.setLocal(name, value);
        }
        return old;
    }

    /**
     * Delegates to the wrapped Bindings <code>putAll</code> method i.e. does
     * not store variables in the SCXML context.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> list) {
            bindings.putAll(list);
    }

    /**
     * Removes the object from the wrapped Bindings instance or the contained
     * SCXML context. Not entirely sure about this implementation but it
     * follows the philosophy of using the Javascript Bindings as a child context
     * of the SCXML context.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public Object remove(Object key) {
        if (bindings.containsKey(key))
           return bindings.remove(key);

        if (context.has(key.toString()))
           return context.getVars().remove(key);

        return Boolean.FALSE;
    }

    /**
     * Delegates to the wrapped Bindings <code>clear</code> method. Does not clear
     * the SCXML context.
     * <p>
     * NOTE: doesn't seem to be invoked ever so not sure if it works in
     *       context. Not thread-safe.
     */
    @Override
    public void clear() {
            bindings.clear();
    }

    /**
     * Internal method to create a union of the SCXML context and the Javascript
     * Bindings. Does a heavyweight copy - and so far only invoked by the
     * not used methods.
     */
    private Bindings union() {
        Bindings set = new SimpleBindings();

        set.putAll(context.getVars());

        for (String key: bindings.keySet())
            set.put(key,bindings.get(key));

        return set;
    }

}

