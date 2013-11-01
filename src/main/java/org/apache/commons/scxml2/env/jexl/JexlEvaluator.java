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

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.Script;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.w3c.dom.Node;

/**
 * Evaluator implementation enabling use of JEXL expressions in
 * SCXML documents.
 * <P>
 * This implementation itself is thread-safe, so you can keep singleton
 * for efficiency of the internal <code>JexlEngine</code> member.
 * </P>
 */
public class JexlEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Error message if evaluation context is not a JexlContext. */
    private static final String ERR_CTX_TYPE = "Error evaluating JEXL "
        + "expression, Context must be a org.apache.commons.jexl2.JexlContext";

    /** The internal JexlEngine instance to use. */
    private transient volatile JexlEngine jexlEngine;

    /** Constructor. */
    public JexlEvaluator() {
        super();
        // create the internal JexlEngine initially
        jexlEngine = createJexlEngine();
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
    public Object eval(final Context ctx, final String expr)
    throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        JexlContext jexlCtx = null;
        if (ctx instanceof JexlContext) {
            jexlCtx = (JexlContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        Expression exp = null;
        try {
            final JexlContext effective = getEffectiveContext(jexlCtx);
            exp = getJexlEngine().createExpression(expr);
            return exp.evaluate(effective);
        } catch (Exception e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):"
                + e.getMessage(), e);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    public Boolean evalCond(final Context ctx, final String expr)
    throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        JexlContext jexlCtx = null;
        if (ctx instanceof JexlContext) {
            jexlCtx = (JexlContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        Expression exp = null;
        try {
            final JexlContext effective = getEffectiveContext(jexlCtx);
            exp = getJexlEngine().createExpression(expr);
            return (Boolean) exp.evaluate(effective);
        } catch (Exception e) {
            throw new SCXMLExpressionException("evalCond('" + expr + "'):"
                + e.getMessage(), e);
        }
    }

    /**
     * @see Evaluator#evalLocation(Context, String)
     */
    public Node evalLocation(final Context ctx, final String expr)
    throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }
        JexlContext jexlCtx = null;
        if (ctx instanceof JexlContext) {
            jexlCtx = (JexlContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        Expression exp = null;
        try {
            final JexlContext effective = getEffectiveContext(jexlCtx);
            effective.setEvaluatingLocation(true);
            exp = getJexlEngine().createExpression(expr);
            return (Node) exp.evaluate(effective);
        } catch (Exception e) {
            throw new SCXMLExpressionException("evalLocation('" + expr + "'):"
                + e.getMessage(), e);
        }
    }

    /**
     * @see Evaluator#evalScript(Context, String)
     */
    public Object evalScript(final Context ctx, final String script)
    throws SCXMLExpressionException {
        if (script == null) {
            return null;
        }
        JexlContext jexlCtx = null;
        if (ctx instanceof JexlContext) {
            jexlCtx = (JexlContext) ctx;
        } else {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        Script jexlScript = null;
        try {
            final JexlContext effective = getEffectiveContext(jexlCtx);
            effective.setEvaluatingLocation(true);
            jexlScript = getJexlEngine().createScript(script);
            return jexlScript.execute(effective);
        } catch (Exception e) {
            throw new SCXMLExpressionException("evalScript('" + script + "'):"
                + e.getMessage(), e);
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
        return new JexlContext(parent);
    }

    /**
     * Create the internal JexlEngine member during the initialization.
     * This method can be overriden to specify more detailed options
     * into the JexlEngine.
     * @return
     */
    protected JexlEngine createJexlEngine() {
        JexlEngine engine = new JexlEngine();
        // With null prefix, define top-level user defined functions.
        // See javadoc of org.apache.commons.jexl2.JexlEngine#setFunctions(Map<String,Object> funcs) for detail.
        Map<String, Object> funcs = new HashMap<String, Object>();
        funcs.put(null, JexlBuiltin.class);
        engine.setFunctions(funcs);
        engine.setCache(256);
        return engine;
    }

    /**
     * Returns the internal JexlEngine if existing.
     * Otherwise, it creates a new engine by invoking {@link #createJexlEngine()}.
     * <P>
     * <EM>NOTE: The internal JexlEngine instance can be null when this is deserialized.</EM>
     * </P>
     * @return
     */
    private JexlEngine getJexlEngine() {
        JexlEngine engine = jexlEngine;
        if (engine == null) {
            synchronized (this) {
                engine = jexlEngine;
                if (engine == null) {
                    jexlEngine = engine = createJexlEngine();
                }
            }
        }
        return engine;
    }

    /**
     * Create a new context which is the summation of contexts from the
     * current state to document root, child has priority over parent
     * in scoping rules.
     *
     * @param nodeCtx The JexlContext for this state.
     * @return The effective JexlContext for the path leading up to
     *         document root.
     */
    private JexlContext getEffectiveContext(final JexlContext nodeCtx) {
        return new JexlContext(new EffectiveContextMap(nodeCtx));
    }

    /**
     * The map that will back the effective context for the
     * {@link JexlEvaluator}. The effective context enables the chaining of
     * {@link Context}s all the way from the current state node to the root.
     *
     */
    private static final class EffectiveContextMap extends AbstractMap<String, Object> {

        /** The {@link Context} for the current state. */
        private final Context leaf;

        /** Constructor. */
        public EffectiveContextMap(final JexlContext ctx) {
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

