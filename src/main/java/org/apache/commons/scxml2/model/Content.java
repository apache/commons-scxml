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

import org.w3c.dom.Node;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;content&gt; SCXML element.
 *
 */
public class Content implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The param expression, may be null.
     */
    private String expr;

    /**
     * The body of this content parsed as Node, may be null.
     */
    private Node body;

    /**
     * The parsed content of the body of this content, may be null.
     */
    private Object value;

    /**
     * Get the expression for this content.
     *
     * @return String The expression for this content.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression for this content.
     *
     * @param expr The expression for this content.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Returns the content body as Node
     *
     * @return the content body as Node
     */
    public Node getBody() {
        return body;
    }

    /**
     * Sets the content body as Node
     * @param body the content body as Node
     */
    public void setBody(final Node body) {
        this.body = body;
    }

    /**
     * Returns the parsed content of the body
     * @return the parsed content of the body
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the parsed content of the body
     * @param value the parsed content of the body
     */
    public void setValue(final Object value) {
        this.value = value;
    }
}
