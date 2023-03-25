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
package org.apache.commons.scxml2.model;

/**
 * The class in this SCXML object model that corresponds to the SCXML
 * &lt;data&gt; child element of the &lt;datamodel&gt; element.
 */
public class Data implements ParsedValueContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The identifier of this data instance.
     * For backwards compatibility this is also the name.
     */
    private String id;

    /**
     * The URL to get the data from.
     */
    private String src;

    /**
     * The expression that evaluates to the value of this data instance.
     */
    private String expr;

    /**
     * The data element body value, or the value from external {@link #getSrc()}, may be null.
     */
    private ParsedValue dataValue;

    /**
     * Get the id.
     *
     * @return String An identifier.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The identifier.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the URL for external data.
     *
     * @return String The URL.
     */
    public final String getSrc() {
        return src;
    }

    /**
     * Set the URL for external data.
     *
     * @param src The source URL.
     */
    public final void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the expression that evaluates to the value of this data instance.
     *
     * @return String The expression.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression that evaluates to the value of this data instance.
     *
     * @param expr The expression.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the data value
     *
     * @return The data value
     */
    public final ParsedValue getParsedValue() {
        return dataValue;
    }

    /**
     * Set the data value
     *
     * @param dataValue The data value
     */
    public final void setParsedValue(final ParsedValue dataValue) {
        this.dataValue = dataValue;
    }
}

