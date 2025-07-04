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
package org.apache.commons.scxml2.env.groovy;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.scxml2.Builtin;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

/**
 * Groovy {@link Script} base class for SCXML, providing the standard 'builtin' function {@link #In(String)},
 * as well as JEXL like convenience functions {@link #empty(Object)} and {@link #var(String)}.
 */
public abstract class GroovySCXMLScript extends Script {

    private GroovyContext context;

    protected GroovySCXMLScript() {
        super(null);
    }

    /**
     * The empty function mimics the behavior of the JEXL empty function, in that it returns true if the parameter is:
     * <ul>
     *     <li>null, or</li>
     *     <li>an empty String, or</li>
     *     <li>an zero length Array, or</li>
     *     <li>an empty Collection, or</li>
     *     <li>an empty Map</li>
     * </ul>
     * <p>
     *     Note: one difference with the JEXL language is that Groovy doesn't allow checking for undefined variables.<br>
     *     Before being able to check, Groovy will already have raised an MissingPropertyException if the variable cannot be found.<br>
     *     To work around this, the custom {@link #var(String)} function is available.
     * </p>
     *
     * @param obj the object to check if it is empty
     * @return true if the object is empty, false otherwise
     */
    public boolean empty(final Object obj) {
        return obj == null ||
                obj instanceof String && ((String)obj).isEmpty() ||
                obj.getClass().isArray() && Array.getLength(obj)==0 ||
                obj instanceof Collection && ((Collection)obj).size()==0 ||
                obj instanceof Map && ((Map)obj).isEmpty();
    }

    /**
     * Implements the In() predicate for SCXML documents ( see Builtin#isMember )
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public boolean In(final String state) {
        return Builtin.isMember(context, state);
    }

    @Override
    public void setBinding(final Binding binding) {
        super.setBinding(binding);
        this.context = ((GroovyContextBinding) binding).getContext();
    }

    /**
     * The var function can be used to check if a variable is defined,
     * <p>
     * In the Groovy language (implementation) you cannot check for an undefined variable directly:
     * Groovy will raise a MissingPropertyException before you get the chance.
     * </p>
     * <p>
     * The var function works around this by indirectly looking up the variable, which you therefore have to specify as a String.
     * </p>
     * <p>
     * So, use <code>var('name')</code>, not <code>var(name)</code>
     * </p>
     * <p>
     * Note: this function doesn't support object navigation, like <code>var('name.property')</code>.<br>
     * Instead, once you established a variable 'name' exists, you <em>thereafter</em> can use the standard Groovy
     * Safe Navigation operator (?.), like so: <code>name?.property</code>.<br>
     * See for more information: <a href="https://docs.codehaus.org/display/GROOVY/Operators#Operators-SafeNavigationOperator(?.)">Groovy SafeNavigationOperator</a>
     * </p>
     *
     * @param property the name of variable to check if it exists
     * @return true if the variable exists, false otherwise
     */
    public boolean var(final String property) {
        if (!context.has(property)) {
            try {
                getMetaClass().getProperty(this, property);
            } catch (final MissingPropertyException e) {
                return false;
            }
        }
        return true;
    }
}
