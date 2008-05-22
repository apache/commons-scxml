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
package org.apache.commons.scxml.env.xpath;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.env.xpath.FunctionResolver.FunctionKey;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An {@link Evaluator} implementation for XPath environments.
 *
 */
public class XPathEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -3578920670869493294L;
    /** Pattern for recognizing the Commons SCXML Data() builtin function. */
    private static final Pattern dataFct = Pattern.compile("Data\\(");

    /** The factory specialized for the Commons SCXML environment. */
    private final XPathFactory factory;
    /** The XPathFunctionResolver in use. */
    private final FunctionResolver fnResolver;
    /** The dummyContextNode node for XPath evaluation. */
    private final Document dummyContextNode;

    /**
     * No argument constructor.
     */
    public XPathEvaluator() throws InstantiationException {
        fnResolver = new FunctionResolver();
        factory = XPathFactory.newInstance();
        factory.setXPathFunctionResolver(fnResolver);
        dummyContextNode = getDummyContextNode();
    }

    /**
     * Constructor supporting user-defined {@link XPathFunction}s.
     *
     * @param functions The user-defined XPath functions to use.
     */
    public XPathEvaluator(final Map<FunctionKey, XPathFunction> functions)
    throws InstantiationException {
        this();
        fnResolver.addFunctions(functions);
    }

    /**
     * @see Evaluator#eval(Context, String)
     */
    @Override
    public Object eval(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        XPath xpath = getXPath(ctx);
        try {
            return xpath.evaluate(expr, dummyContextNode, XPathConstants.STRING);
        } catch (XPathExpressionException xee) {
            xee.printStackTrace();
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    @Override
    public Boolean evalCond(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        XPath xpath = getXPath(ctx);
        try {
            return (Boolean) xpath.evaluate(expr, dummyContextNode, XPathConstants.BOOLEAN);
        } catch (XPathExpressionException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalLocation(Context, String)
     */
    @Override
    public Node evalLocation(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        String evalExpr = dataFct.matcher(expr).
            replaceFirst("DataNode(");
        XPath xpath = getXPath(ctx);
        try {
            return (Node) xpath.evaluate(evalExpr, dummyContextNode, XPathConstants.NODE);
        } catch (XPathExpressionException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#newContext(Context)
     */
    @Override
    public Context newContext(final Context parent) {
        return new XPathContext(parent);
    }

    /**
     * Get configures XPath from the factory.
     */
    @SuppressWarnings("unchecked")
    private XPath getXPath(final Context ctx) throws SCXMLExpressionException {
        if (!(ctx instanceof XPathContext)) {
            throw new SCXMLExpressionException("XPathEvaluator needs XPathContext");
        }
        XPathContext xctx = (XPathContext) ctx;
        factory.setXPathVariableResolver(xctx);
        fnResolver.setContext(xctx);
        XPath xpath = factory.newXPath();
        NamespaceContext nsCtx =
            new ExpressionNSContext((Map<String, String>) ctx.get("_ALL_NAMESPACES"));
        xpath.setNamespaceContext(nsCtx);
        return xpath;
    }

    /**
     * Create dummy context node for XPath evaluation context.
     */
    private Document getDummyContextNode() throws InstantiationException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new InstantiationException("Cannot create dummy context " +
                " node for XPath evaluator");
        }
    }


    /**
     * XPath {@link NamespaceContext} for Commons SCXML expressions.
     *
     * <b>Code duplication:</b> Also in Builtin.java. Class is not meant to be
     * part of any public API and will be removed when parser is no longer
     * using Commons Digester.
     */
    private static final class ExpressionNSContext
    implements Serializable, NamespaceContext {

        /** Serial version UID. */
        private static final long serialVersionUID = 8620558582288851315L;
        /** Map supplied by digester. */
        private final Map<String, String> namespaces;

        /**
         * Constructor.
         *
         * @param namespaces The current namespace map.
         */
        ExpressionNSContext(final Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        /**
         * @see NamespaceContext#getNamespaceURI(String)
         */
        @Override
        public String getNamespaceURI(final String prefix) {
            return namespaces.get(prefix);
        }

        /**
         * @see NamespaceContext#getPrefix(String)
         *
         * First matching key in iteration order is returned, and the
         * iteration order depends on the underlying <code>namespaces</code>
         * {@link Map} implementation.
         */
        @Override
        public String getPrefix(final String namespaceURI) {
            return (String) getKeys(namespaceURI, true);
        }

        /**
         * @see NamespaceContext#getPrefixes(String)
         *
         * The iteration order depends on the underlying <code>namespaces</code>
         * {@link Map} implementation.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<String> getPrefixes(final String namespaceURI) {
            return (Iterator<String>) getKeys(namespaceURI, false);
        }

        /**
         * Get prefix key(s) for given namespaceURI value.
         *
         * If <code>one</code>, first matching key in iteration order is
         * returned, and the iteration order depends on the underlying
         * <code>namespaces</code> {@link Map} implementation.
         * Otherwise, an iterator to all matching keys is returned.
         *
         * @param value The value whose key is required
         * @param one At most one matching key is returned
         * @return The required prefix key(s)
         */
        private Object getKeys(final String value, final boolean one) {
            List<String> prefixes = new LinkedList<String>();
            if (namespaces.containsValue(value)) {
                for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                    String v = entry.getValue();
                    if ((value == null && v == null) ||
                            (value != null && value.equals(v))) {
                        String prefix = entry.getKey();
                        if (one) {
                            return prefix;
                        } else {
                            prefixes.add(prefix);
                        }
                    }
                }
            }
            return one ? null : prefixes.iterator();
        }

    }

}
