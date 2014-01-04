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

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

/**
 * Groovy Context implementation for Commons SCXML.
 */
public class GroovyContext extends SimpleContext {

    private static final long serialVersionUID = 1L;

    /**
     * Internal flag to indicate whether it is to evaluate a location
     * that returns a Node within an XML data tree.
     */
    private boolean evaluatingLocation = false;

    /**
     * Constructor.
     */
    public GroovyContext() {
        super();
    }

    /**
     * Constructor with initial vars.
     *
     * @param initialVars The initial set of variables.
     */
    public GroovyContext(final Map<String, Object> initialVars) {
        super(initialVars);
    }

    /**
     * Constructor with parent context.
     *
     * @param parent The parent context.
     */
    public GroovyContext(final Context parent) {
        super(parent);
    }

    /**
     * Returns the internal flag to indicate whether it is to evaluate a location
     * that returns a Node within an XML data tree.
     */
    public boolean isEvaluatingLocation() {
        return evaluatingLocation;
    }

    /**
     * Sets the internal flag to indicate whether it is to evaluate a location
     * that returns a Node within an XML data tree.
     */
    public void setEvaluatingLocation(boolean evaluatingLocation) {
        this.evaluatingLocation = evaluatingLocation;
    }

}
