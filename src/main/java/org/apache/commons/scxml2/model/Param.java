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
package org.apache.commons.scxml2.model;

import java.io.Serializable;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;param&gt; SCXML element.
 */
public class Param implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The param name.
     */
    private String name;

    /**
     * Left hand side expression evaluating to a location within
     * a previously defined XML data tree.
     */
    private String location;

    /**
     * The param expression, may be null.
     */
    private String expr;

    /**
     * Default no-args constructor
     */
    public Param() {
        name = null;
        expr = null;
    }
    /**
     * Gets the expression for this param value.
     *
     * @return String The expression for this param value.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Gets the location for a previously defined XML data tree.
     *
     * @return the location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the name for this param.
     *
     * @return String The param name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the expression for this param value.
     *
     * @param expr The expression for this param value.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Sets the location for a previously defined XML data tree.
     *
     * @param location The location.
     */
    public void setLocation(final String location) {
        this.location = location;
    }

    /**
     * Sets the name for this param.
     *
     * @param name The param name.
     */
    public final void setName(final String name) {
        this.name = name;
    }
}

