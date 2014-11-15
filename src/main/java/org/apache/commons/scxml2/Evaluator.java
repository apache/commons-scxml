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

/**
 * Interface for a component that may be used by the SCXML engines to
 * evaluate the expressions within the SCXML document.
 *
 */
public interface Evaluator {

    /** SCXML 1.0 Null Data Model name **/
    String NULL_DATA_MODEL = "null";

    /** SCXML 1.0 ECMAScript Data Model name **/
    String ECMASCRIPT_DATA_MODEL = "ecmascript";

    /** SCXML 1.0 XPath Data Model name **/
    String XPATH_DATA_MODEL = "xpath";

    /** Default Data Model name **/
    String DEFAULT_DATA_MODEL = "";

    /**
     * The allowable types of <assign/> including and in particular when using XPath
     */
    enum AssignType {

        REPLACE_CHILDREN("replacechildren"),
        FIRST_CHILD("firstchild"),
        LAST_CHILD("lastchild"),
        PREVIOUS_SIBLING("previoussibling"),
        NEXT_SIBLING("nextsibling"),
        REPLACE("replace"),
        DELETE("delete"),
        ADD_ATTRIBUTE("addattribute");

        private final String value;

        private AssignType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static AssignType fromValue(String value) {
            for (AssignType type : AssignType.values()) {
                if (type.value().equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * Get the datamodel type supported by this Evaluator
     * @return The supported datamodel type
     */
    String getSupportedDatamodel();

    /**
     * Evaluate an expression returning a data value
     *
     * @param ctx variable context
     * @param expr expression
     * @return the result of the evaluation
     * @throws SCXMLExpressionException A malformed expression exception
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
     * @throws SCXMLExpressionException A malformed expression exception
     */
    Boolean evalCond(Context ctx, String expr)
    throws SCXMLExpressionException;

    /**
     * Evaluate a location that returns a data assignable reference or list of references.
     * Manifests as "location" attributes of &lt;assign&gt; element.
     *
     * @param ctx variable context
     * @param expr expression
     * @return The location result.
     * @throws SCXMLExpressionException A malformed expression exception
     */
    Object evalLocation(Context ctx, String expr)
    throws SCXMLExpressionException;

    /**
     * Assigns data to a location
     *
     * @param ctx variable context
     * @param location location expression
     * @param data the data to assign.
     * @param type the type of assignment to perform, null assumes {@link AssignType#REPLACE_CHILDREN}
     * @param attr the name of the attribute to add when using type {@link AssignType#ADD_ATTRIBUTE}
     * @throws SCXMLExpressionException A malformed expression exception
     */
    void evalAssign(Context ctx, String location, Object data, AssignType type, String attr)
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

