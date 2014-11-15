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
package org.apache.commons.scxml2.env.minimal;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

/**
 * MinimalContext implementation for the SCXML Null Data Model.
 * <p>
 * This context disables any access to the parent context (other than through getParent()) and
 * ignores setting any local data.
 * </p>
 * <p>
 * The MinimalContext requires a none MinimalContext based parent Context for creation.
 * If the parent context is of type MinimalContext, <em>its</em> parent will be used as the parent of the
 * new MinimalContext instance
 * </p>
 */
public class MinimalContext extends SimpleContext {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    private static Context getMinimalContextParent(final Context parent) {
        if (parent != null) {
            if (parent instanceof MinimalContext) {
                return getMinimalContextParent(parent.getParent());
            }
            return parent;
        }
        throw new IllegalStateException("A MinimalContext instance requires a non MinimalContext based parent.");
    }

    public MinimalContext(final Context parent) {
        super(getMinimalContextParent(parent));
    }

    @Override
    public void set(final String name, final Object value) {
    }

    @Override
    public Object get(final String name) {
        return null;
    }

    @Override
    public boolean has(final String name) {
        return false;
    }

    @Override
    public boolean hasLocal(final String name) {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void setLocal(final String name, final Object value) {
    }

    @Override
    protected void setVars(final Map<String, Object> vars) {
    }

    @Override
    public Map<String, Object> getVars() {
        return Collections.emptyMap();
    }
}
