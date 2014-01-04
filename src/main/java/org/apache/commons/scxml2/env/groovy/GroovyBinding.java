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
package org.apache.commons.scxml2.env.groovy;

import groovy.lang.Binding;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.scxml2.Context;

/**
 * Wrapper class for the Groovy Binding class that extends the
 * wrapped Binding to search the SCXML context for variables and predefined
 * functions that do not exist in the wrapped Binding.
 */
public class GroovyBinding extends Binding {

    private Context context;
    private Binding binding;

    /**
     * Initialises the internal Bindings delegate and SCXML context.
     * @param context  SCXML Context to use for script variables.
     * @param binding GroovyShell bindings for variables.
     * @throws IllegalArgumentException Thrown if either <code>context</code>
     *         or <code>binding</code> is <code>null</code>.
     */
    public GroovyBinding(Context context, Binding binding) {
        if (context == null) {
            throw new IllegalArgumentException("Invalid SCXML context");
        }

        if (binding == null) {
            throw new IllegalArgumentException("Invalid GroovyShell Binding");
        }

        this.context = context;
        this.binding = binding;
    }

    @Override
    public Object getVariable(String name) {
        if (context.has(name)) {
            return context.get(name);
        }

        return binding.getVariable(name);
    }

    @Override
    public void setVariable(String name, Object value) {
        if (context.has(name)) {
            context.set(name, value);
        } else if (binding.hasVariable(name)) {
            binding.setVariable(name, value);
        } else {
            context.setLocal(name, value);
        }
    }

    @Override
    public boolean hasVariable(String name) {
        if (context.has(name)) {
            return true;
        }

        return binding.hasVariable(name);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Map getVariables() {
        Map<String, Object> variables = new LinkedHashMap<String, Object>(binding.getVariables());
        variables.putAll(context.getVars());
        return variables;
    }

    @Override
    public Object getProperty(String property) {
        return binding.getProperty(property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        binding.setProperty(property, newValue);
    }

}
