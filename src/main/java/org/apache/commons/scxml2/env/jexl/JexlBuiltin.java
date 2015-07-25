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
package org.apache.commons.scxml2.env.jexl;

import org.apache.commons.scxml2.Builtin;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.XPathBuiltin;

/**
 * Global JEXL namespace functor, providing the standard SCXML In() operator and the Commons SCXML extensions
 * for Data() and Location() to support XPath datamodel access.
 */
public final class JexlBuiltin {
    /**
     * The context currently in use for evaluation.
     */
    private final JexlContext context;

    /**
     * Creates a new instance, wraps the context.
     * @param ctxt the context in use
     */
    public JexlBuiltin(final JexlContext ctxt) {
        context = ctxt;
    }

    /**
     * Provides the SCXML standard In() predicate for SCXML documents.
     * @param state The State ID to compare with
     * @return true if this state is currently active
     */
    public boolean In(final String state) {
        return Builtin.isMember(context, state);
    }

    /**
     * Provides the Commons SCXML Data() predicate extension for SCXML documents.
     * @param expression the XPath expression
     * @return the data matching the expression
     * @throws SCXMLExpressionException A malformed expression exception
     */
    public Object Data(final String expression) throws SCXMLExpressionException {
        return XPathBuiltin.eval(context, expression);
    }

    /**
     * Provides the Commons SCXML Location() predicate extension for SCXML documents.
     * @param expression the XPath expression
     * @return the location matching the expression
     * @throws SCXMLExpressionException A malformed expression exception
     */
    public Object Location(final String expression) throws SCXMLExpressionException {
        return XPathBuiltin.evalLocation(context, expression);
    }
}
