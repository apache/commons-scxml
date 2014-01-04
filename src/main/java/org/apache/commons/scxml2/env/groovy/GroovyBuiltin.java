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

import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.Builtin;
import org.apache.commons.scxml2.model.TransitionTarget;

/**
 * Global Groovy namespace functor, implements Data() and In() operators.
 * Cooperates with GroovyContext.
 */
public final class GroovyBuiltin {
    /**
     * The context currently in use for evaluation.
     */
    private final GroovyContext context;

    /**
     * Creates a new instance, wraps the context.
     * @param ctxt the context in use
     */
    public GroovyBuiltin(final GroovyContext ctxt) {
        context = ctxt;
    }

    /**
     * Gets the ALL_NAMESPACES map from context.
     * @return the ALL_NAMESPACES map
     */
    private Map<String, String> getNamespaces() {
        return (Map<String, String>) context.get("_ALL_NAMESPACES");
    }

    /**
     * Gets the ALL_STATES set from context.
     * @return the ALL_STATES set
     */
    private Set<TransitionTarget> getAllStates() {
        return (Set<TransitionTarget>) context.get("_ALL_STATES");
    }

    /**
     * Implements the Data() predicate for SCXML documents ( see Builtin#data ).
     * @param data the context node
     * @param path the XPath expression
     * @return the first node matching the path
     */
    public Object Data(final Object data, final String path) {
        // first call maps delegates to dataNode(), subsequent ones to data()
        if (context.isEvaluatingLocation()) {
            context.setEvaluatingLocation(false);
            return Builtin.dataNode(getNamespaces(), data, path);
        } else {
            return Builtin.data(getNamespaces(), data, path);
        }
    }

    /**
     * Implements the In() predicate for SCXML documents ( see Builtin#isMember )
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public boolean In(final String state) {
        return Builtin.isMember(getAllStates(), state);
    }
}
