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
package org.apache.commons.scxml.model;

import java.io.Serializable;
import java.util.Map;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;param&gt; SCXML element.
 *
 */
public class Param implements NamespacePrefixesHolder, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The param name.
     */
    private String name;

    /**
     * The param expression, may be null.
     */
    private String expr;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    /**
     * Default no-args constructor for Digester.
     */
    public Param() {
        name = null;
        expr = null;
    }
    /**
     * Get the name for this param.
     *
     * @return String The param name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name for this param.
     *
     * @param name The param name.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the expression for this param value.
     *
     * @return String The expression for this param value.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression for this param value.
     *
     * @param expr The expression for this param value.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

}

