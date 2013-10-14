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
package org.apache.commons.scxml.env.jexl;

import java.util.Map;

import org.apache.commons.scxml.Builtin;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.env.SimpleContext;

/**
 * JEXL Context implementation for Commons SCXML.
 *
 */
public class JexlContext extends SimpleContext
    implements org.apache.commons.jexl.JexlContext {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public JexlContext() {
        super();
        getVars().put("_builtin", new Builtin());
    }

    /**
     * Constructor with initial vars.
     *
     * @param initialVars The initial set of variables.
     */
    public JexlContext(final Map<String, Object> initialVars) {
        super(initialVars);
        getVars().put("_builtin", new Builtin());
    }

    /**
     * Constructor with parent context.
     *
     * @param parent The parent context.
     */
    public JexlContext(final Context parent) {
        super(parent);
        getVars().put("_builtin", new Builtin());
    }

    /**
     * Set the variables map.
     *
     * @param vars The new variables map.
     *
     * @see org.apache.commons.jexl.JexlContext#setVars(Map)
     * @see org.apache.commons.scxml.env.SimpleContext#setVars(Map)
     */
    @Override
    @SuppressWarnings("unchecked")
    // Accomodate legacy signature org.apache.commons.jexl.JexlContext#setVars(Map)
    public void setVars(final Map vars) {
        super.setVars(vars);
        getVars().put("_builtin", new Builtin());
    }

    /**
     * Clear this Context.
     *
     * @see org.apache.commons.scxml.Context#reset()
     */
    @Override
    public void reset() {
        super.reset();
        getVars().put("_builtin", new Builtin());
    }

}

