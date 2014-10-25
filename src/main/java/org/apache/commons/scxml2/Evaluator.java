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
package org.apache.commons.scxml2;

import org.w3c.dom.Node;

/**
 * Interface for a component that may be used by the SCXML engines to
 * evaluate the expressions within the SCXML document.
 *
 */
public interface Evaluator {

    /**
     * Get the datamodel type supported by this Evaluator
     * @return The supported datamodel type
     */
    String getSupportedDatamodel();

    /**
     * Evaluate an expression.
     *
     * @param ctx variable context
     * @param expr expression
     * @return a result of the evaluation
     * @throws SCXMLExpressionException A malformed exception
     */
    Object eval(Context ctx, String expr)
    throws SCXMLExpressionException;

    /**
     * Evaluate a condition.
     * Manifests as "cond" attributes of &lt;transition&gt;,
     * &lt;if&gt; and &lt;elseif&gt; elements.
     *
     * @param ctx variable context
     * @param expr expression
     * @return true/false
     * @throws SCXMLExpressionException A malformed exception
     */
    Boolean evalCond(Context ctx, String expr)
    throws SCXMLExpressionException;

    /**
     * Evaluate a location that returns a Node within an XML data tree.
     * Manifests as "location" attributes of &lt;assign&gt; element.
     *
     * @param ctx variable context
     * @param expr expression
     * @return The location node.
     * @throws SCXMLExpressionException A malformed exception
     */
    Node evalLocation(Context ctx, String expr)
    throws SCXMLExpressionException;

    /**
     * Evaluate a script.
     * Manifests as &lt;script&gt; element.
     *
     * @param ctx variable context
     * @param script The script
     * @return The result of the script execution.
     * @throws SCXMLExpressionException A malformed script
     */
    Object evalScript(Context ctx, String script)
    throws SCXMLExpressionException;

    /**
     * Create a new child context.
     *
     * @param parent parent context
     * @return new child context
     */
    Context newContext(Context parent);

}

