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
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

    /**
     * Constructs a new instance.
     *
     * @param ctx context of the current leave state node
     */
    public EffectiveContextMap(final Context ctx) {
        Context current = ctx;
        while (current != null) {
            if (current.getVars() instanceof EffectiveContextMap) {
                throw new IllegalArgumentException("Context or parent Context already wrapped by EffectiveContextMap");
            }
            current = current.getParent();
        }
        this.leaf = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        final Map<String, Object> map = new HashMap<>();
        mergeVars(leaf, map);
        return Collections.unmodifiableMap(map).entrySet();
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

    /**
     * Parent Context first merging of all Context vars, to ensure same named 'local' vars shadows parent var
     *
     * @param leaf current leaf Context
     * @param map Map to merge vars into
     */
    protected void mergeVars(final Context leaf, final Map<String, Object> map) {
        if (leaf != null) {
            mergeVars(leaf.getParent(), map);
            map.putAll(leaf.getVars());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(final String key, final Object value) {
        final Object old = leaf.get(key);
        if (leaf.has(key)) {
            leaf.set(key, value);
        } else {
            leaf.setLocal(key, value);
        }
        return old;
    }
}
