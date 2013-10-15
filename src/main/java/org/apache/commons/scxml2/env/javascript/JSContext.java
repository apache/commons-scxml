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

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

/**
 * SCXML Context for use by the JSEvaluator. It is simply a 'no functionality'
 * extension of SimpleContext that has been implemented to reduce the impact
 * if the JSEvaluator requires additional functionality at a later stage.
 * <p>
 * Could easily be dispensed with.
 *
 */
public class JSContext extends SimpleContext {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    // CONSTRUCTORS

    /**
     * Default constructor - just invokes the SimpleContext default
     * constructor.
     */
    public JSContext() {
        super();
    }

    /**
     * Child constructor. Just invokes the identical SimpleContext
     * constructor.
     *
     * @param parent Parent context for this context.
     *
     */
    public JSContext(Context parent) {
        super(parent);
    }

}

