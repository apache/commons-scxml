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
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.scxml2.Context;

/**
 * A map that will back the effective {@link Context} for an {@link org.apache.commons.scxml2.Evaluator} execution.
 * The effective context enables the chaining of contexts all the way from the current state node to the root.
 */
public final class EffectiveContextMap extends AbstractMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The {@link org.apache.commons.scxml2.Context} for the current state. */
    private final Context leaf;

    /** Constructor. */
    public EffectiveContextMap(final Context ctx) {
        super();
        this.leaf = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<Entry<String, Object>>();
        Context current = leaf;
        while (current != null) {
            entrySet.addAll(current.getVars().entrySet());
            current = current.getParent();
        }
        return entrySet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(final String key, final Object value) {
        Object old = leaf.get(key);
        if (leaf.has(key)) {
            leaf.set(key, value);
        } else {
            leaf.setLocal(key, value);
        }
        return old;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final Object key) {
        if (key != null) {
            Context current = leaf;
            while (current != null) {
                if (current.getVars().containsKey(key.toString())) {
                    return current.getVars().get(key);
                }
                current = current.getParent();
            }
        }
        return null;
    }
}
