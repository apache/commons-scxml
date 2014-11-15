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

import org.apache.commons.scxml2.Builtin;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.XPathBuiltin;

/**
 * Custom Javascript engine function providing the SCXML In() predicate and the Commons SCXML extensions
 * for Data() and Location() to support XPath datamodel access.
 */
public class JSFunctions {

    /**
     * The context currently in use for evaluation.
     */
    private Context ctx;

    /**
     * Creates a new instance, wraps the context.
     * @param ctx the context in use
     */
    public JSFunctions(Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Provides the SCXML standard In() predicate for SCXML documents.
     * @param state The State ID to compare with
     * @return true if this state is currently active
     */
    public boolean In(final String state) {
        return Builtin.isMember(ctx, state);
    }

    /**
     * Provides the Commons SCXML Data() predicate extension for SCXML documents.
     * @param expression the XPath expression
     * @return the data matching the expression
     */
    public Object Data(String expression) throws SCXMLExpressionException {
        return XPathBuiltin.eval(ctx, expression);
    }

    /**
     * Provides the Commons SCXML Location() predicate extension for SCXML documents.
     * @param expression the XPath expression
     * @return the location matching the expression
     */
    public Object Location(String expression) throws SCXMLExpressionException {
        return XPathBuiltin.evalLocation(ctx, expression);
    }
}
