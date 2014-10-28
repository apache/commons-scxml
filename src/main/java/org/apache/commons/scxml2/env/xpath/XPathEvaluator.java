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
package org.apache.commons.scxml2.env.xpath;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.env.EffectiveContextMap;
import org.apache.commons.scxml2.model.SCXML;
import org.w3c.dom.Node;

/**
 * <p>An {@link Evaluator} implementation for XPath environments.</p>
 *
 * <p>Does not support the &lt;script&gt; module, throws
 * {@link UnsupportedOperationException} if attempted.</p>
 */
public class XPathEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -3578920670869493294L;

    private static final String SUPPORTED_DATAMODEL = "xpath";

    public static class XPathEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATAMODEL;
        }

        @Override
        public Evaluator getEvaluator() {
            return new XPathEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new XPathEvaluator();
        }
    }

    private static final JXPathContext jxpathRootContext = JXPathContext.newContext(null);

    static {
        FunctionLibrary xpathFunctions = new FunctionLibrary();
        xpathFunctions.addFunctions(new ClassFunctions(XPathFunctions.class, null));
        // also restore default generic JXPath functions
        xpathFunctions.addFunctions(new PackageFunctions("", null));
        jxpathRootContext.setFunctions(xpathFunctions);
    }

    private JXPathContext jxpathContext;

    /**
     * No argument constructor.
     */
    public XPathEvaluator() {
        jxpathContext = jxpathRootContext;
    }

    /**
     * Constructor supporting user-defined JXPath {@link Functions}.
     *
     * @param functions The user-defined JXPath functions to use.
     */
    public XPathEvaluator(final Functions functions) {
        jxpathContext = JXPathContext.newContext(jxpathRootContext, null);
        jxpathContext.setFunctions(functions);
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATAMODEL;
    }

    /**
     * @see Evaluator#eval(Context, String)
     */
    @Override
    public Object eval(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        JXPathContext context = getContext(ctx);
        try {
            return context.getValue(expr, String.class);
        } catch (JXPathException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    @Override
    public Boolean evalCond(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        JXPathContext context = getContext(ctx);
        try {
            return (Boolean)context.getValue(expr, Boolean.class);
        } catch (JXPathException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalLocation(Context, String)
     */
    @Override
    public Node evalLocation(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        JXPathContext context = getContext(ctx);
        try {
            return (Node)context.selectSingleNode(expr);
        } catch (JXPathException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalScript(Context, String)
     */
    public Object evalScript(Context ctx, String script)
    throws SCXMLExpressionException {
        throw new UnsupportedOperationException("Scripts are not supported by the XPathEvaluator");
    }

    /**
     * @see Evaluator#newContext(Context)
     */
    @Override
    public Context newContext(final Context parent) {
        return new XPathContext(parent);
    }


    @SuppressWarnings("unchecked")
    private JXPathContext getContext(final Context ctx) throws SCXMLExpressionException {
        JXPathContext context = JXPathContext.newContext(jxpathContext, new EffectiveContextMap(ctx));
        context.setVariables(new ContextVariables(ctx));
        Map<String, String> namespaces = (Map<String, String>) ctx.get(Context.NAMESPACES_KEY);
        if (namespaces != null) {
            for (String prefix : namespaces.keySet()) {
                context.registerNamespace(prefix, namespaces.get(prefix));
            }
        }
        return context;
    }
}
