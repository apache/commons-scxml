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

import java.io.Serializable;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * The class in this SCXML object model that corresponds to the SCXML
 * &lt;data&gt; child element of the &lt;datamodel&gt; element.
 *
 */
public class Data implements NamespacePrefixesHolder, Serializable {

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
     * The child XML data tree, parsed as a Node
     * instance.
     */
    private Node node;

    /**
     * The parsed value for the child XML data tree or the external src (with early-binding), to be cloned before usage
     */
    private Object value;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation. Easier than to scrape node
     * above, given the Builtin API.
     */
    private Map<String, String> namespaces;

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
     * Get the child XML data tree.
     *
     * @return Node The child XML data tree, parsed as a standalone DocumentFragment <code>Node</code>.
     */
    public final Node getNode() {
        return node;
    }

    /**
     * Set the child XML data tree.
     *
     * @param node The child XML data tree, parsed as a standalone DocumentFragment <code>Node</code>.
     */
    public final void setNode(final Node node) {
        this.node = node;
    }

    /**
     * Get the parsed value for the child XML data tree or the external src (with early-binding), to be cloned before usage.
     * @see #setValue(Object)
     * @return The parsed Data value
     */
    public final Object getValue() {
        return value;
    }

    /**
     * Sets the parsed value for the child XML data tree or the external src (with early-binding), to be cloned before usage.
     * @param value a serializable object:
     * <ul>
     *   <li>"Raw" JSON mapped object tree (array->ArrayList, object->LinkedHashMap based)</li>
     *   <li>XML Node (equals {@link #getNode()})</li>
     *   <li>space-normalized String</li>
     * </ul>
     */
    public final void setValue(final Object value) {
        this.value = value;
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

