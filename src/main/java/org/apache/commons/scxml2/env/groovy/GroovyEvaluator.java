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
package org.apache.commons.scxml2.env.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.w3c.dom.Node;

/**
 * Evaluator implementation enabling use of Groovy expressions in
 * SCXML documents.
 * <P>
 * This implementation itself is thread-safe, so you can keep singleton for efficiency.
 * </P>
 */
public class GroovyEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Error message if evaluation context is not a GroovyContext. */
    private static final String ERR_CTX_TYPE = "Error evaluating Groovy "
            + "expression, Context must be a org.apache.commons.scxml2.env.groovy.GroovyContext";

    /** Constructor. */
    public GroovyEvaluator() {
        super();
    }

    /**
     * Evaluate an expression.
     *
     * @param ctx variable context
     * @param expr expression
     * @return a result of the evaluation
     * @throws SCXMLExpressionException For a malformed expression
     * @see Evaluator#eval(Context, String)
     */
    public Object eval(final Context ctx, final String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }

        GroovyContext groovyCtx = null;

        if (ctx instanceof GroovyContext) {
            groovyCtx = (GroovyContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            GroovyShell shell = createGroovyShell(effective);
            return shell.evaluate(expr);
        } catch (Exception e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + e.getMessage(), e);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    public Boolean evalCond(final Context ctx, final String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }

        GroovyContext groovyCtx = null;

        if (ctx instanceof GroovyContext) {
            groovyCtx = (GroovyContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            GroovyShell shell = createGroovyShell(effective);
            return (Boolean) shell.evaluate(expr);
        } catch (Exception e) {
            throw new SCXMLExpressionException("evalCond('" + expr + "'):" + e.getMessage(), e);
        }
    }

    /**
     * @see Evaluator#evalLocation(Context, String)
     */
    public Node evalLocation(final Context ctx, final String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }

        GroovyContext groovyCtx = null;

        if (ctx instanceof GroovyContext) {
            groovyCtx = (GroovyContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            effective.setEvaluatingLocation(true);
            GroovyShell shell = createGroovyShell(effective);
            return (Node) shell.evaluate(expr);
        } catch (Exception e) {
            throw new SCXMLExpressionException("evalLocation('" + expr + "'):" + e.getMessage(), e);
        }
    }

    /**
     * @see Evaluator#evalScript(Context, String)
     */
    public Object evalScript(final Context ctx, final String script) throws SCXMLExpressionException {
        if (script == null) {
            return null;
        }

        GroovyContext groovyCtx = null;

        if (ctx instanceof GroovyContext) {
            groovyCtx = (GroovyContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            effective.setEvaluatingLocation(true);
            GroovyShell shell = createGroovyShell(effective);
            return shell.evaluate(script);
        } catch (Exception e) {
            throw new SCXMLExpressionException("evalScript('" + script + "'):" + e.getMessage(), e);
        }
    }

    /**
     * Create a new child context.
     *
     * @param parent parent context
     * @return new child context
     * @see Evaluator#newContext(Context)
     */
    public Context newContext(final Context parent) {
        return new GroovyContext(parent);
    }

    /**
     * Create a GroovyShell instance.
     * @param context
     * @return
     */
    protected GroovyShell createGroovyShell(GroovyContext groovyContext) {
        Binding binding = new Binding();
        GroovyBuiltin builtin = new GroovyBuiltin(groovyContext);
        MethodClosure dataClosure = new MethodClosure(builtin, "Data");
        MethodClosure inClosure = new MethodClosure(builtin, "In");
        binding.setProperty("Data", dataClosure);
        binding.setProperty("In", inClosure);
        GroovyBinding groovyBinding = new GroovyBinding(groovyContext, binding);
        GroovyShell shell = new GroovyShell(groovyBinding);
        return shell;
    }

    /**
     * Create a new context which is the summation of contexts from the
     * current state to document root, child has priority over parent
     * in scoping rules.
     *
     * @param nodeCtx The GroovyContext for this state.
     * @return The effective GroovyContext for the path leading up to
     *         document root.
     */
    private GroovyContext getEffectiveContext(final GroovyContext nodeCtx) {
        return new GroovyContext(new EffectiveContextMap(nodeCtx));
    }

    /**
     * The map that will back the effective context for the
     * {@link GroovyEvaluator}. The effective context enables the chaining of
     * {@link Context}s all the way from the current state node to the root.
     *
     */
    private static final class EffectiveContextMap extends AbstractMap<String, Object> {

        /** The {@link Context} for the current state. */
        private final Context leaf;

        /** Constructor. */
        public EffectiveContextMap(final GroovyContext ctx) {
            super();
            this.leaf = ctx;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            Set<Map.Entry<String, Object>> entrySet = new HashSet<Map.Entry<String, Object>>();
            Context current = leaf;
            while (current != null) {
                entrySet.addAll(current.getVars().entrySet());
                current = current.getParent();
            }
            return entrySet;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object put(final String key, final Object value) {
            Object old = leaf.get(key);
            if (leaf.has(key)) {
                leaf.set(key, value);
            } else {
                leaf.setLocal(key, value);
            }
            return old;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object get(final Object key) {
            Context current = leaf;
            while (current != null) {
                if (current.getVars().containsKey(key)) {
                    return current.getVars().get(key);
                }
                current = current.getParent();
            }
            return null;
        }
    }

}