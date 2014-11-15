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
package org.apache.commons.scxml2.env.jexl;

import java.util.Map;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

/**
 * JEXL Context implementation for Commons SCXML.
 *
 */
public class JexlContext extends SimpleContext
    implements org.apache.commons.jexl2.JexlContext {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public JexlContext() {
        super();
    }

    /**
     * Constructor with initial vars.
     * @param parent The parent context
     * @param initialVars The initial set of variables.
     */
    public JexlContext(final Context parent, final Map<String, Object> initialVars) {
        super(parent, initialVars);
    }

    /**
     * Constructor with parent context.
     *
     * @param parent The parent context.
     */
    public JexlContext(final Context parent) {
        super(parent);
    }
}

