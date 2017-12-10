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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.env.AbstractBaseEvaluator;
import org.apache.commons.scxml2.env.EffectiveContextMap;
import org.apache.commons.scxml2.model.SCXML;

/**
 * Evaluator implementation enabling use of JEXL expressions in
 * SCXML documents.
 * <P>
 * This implementation itself is thread-safe, so you can keep singleton
 * for efficiency of the internal <code>JexlEngine</code> member.
 * </P>
 */
public class JexlEvaluator extends AbstractBaseEvaluator {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Unique context variable name used for temporary reference to assign data (thus must be a valid variable name)
     */
    private static final String ASSIGN_VARIABLE_NAME = "a"+UUID.randomUUID().toString().replace('-','x');

    public static final String SUPPORTED_DATA_MODEL = "jexl";

    public static class JexlEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATA_MODEL;
        }

        @Override
        public Evaluator getEvaluator() {
            return new JexlEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final boolean strict) {
            return new JexlEvaluator(strict);
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new JexlEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final boolean strict, final SCXML document) {
            return new JexlEvaluator(strict);
        }
    }

    /** Error message if evaluation context is not a JexlContext. */
    private static final String ERR_CTX_TYPE = "Error evaluating JEXL "
        + "expression, Context must be a org.apache.commons.scxml2.env.jexl.JexlContext";



    /** The internal JexlEngine instance to use. */
    private transient volatile JexlEngine jexlEngine;

    /** The current JexlEngine silent mode, stored locally to be reapplied after deserialization of the engine */
    private boolean jexlEngineSilent;
    /** The current JexlEngine strict mode, stored locally to be reapplied after deserialization of the engine */
    private boolean jexlEngineStrict;

    /** Constructor. */
    public JexlEvaluator() {
        this(false);
    }

    public JexlEvaluator(final boolean strict) {
        super();
        jexlEngineStrict = strict;
        // create the internal JexlEngine initially
        jexlEngine = getJexlEngine();
        jexlEngineSilent = jexlEngine.isSilent();
        jexlEngineStrict = jexlEngine.isStrict();
    }

    /**
     * Checks whether the internal Jexl engine throws JexlException during evaluation.
     * @return true if silent, false (default) otherwise
     */
    public boolean isJexlEngineSilent() {
        return jexlEngineSilent;
    }

    @Override
    public boolean isStrict() {
        return jexlEngineStrict;
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    @Override
    public boolean requiresGlobalContext() {
        return false;
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
        if (!(ctx instanceof JexlContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        try {
            final JexlContext effective = getEffectiveContext((JexlContext)ctx);
            JexlExpression exp = getJexlEngine().createExpression(expr);
            return exp.evaluate(effective);
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("eval('" + expr + "'): " + exMessage, e);
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
        if (!(ctx instanceof JexlContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        try {
            final JexlContext effective = getEffectiveContext((JexlContext)ctx);
            JexlExpression exp = getJexlEngine().createExpression(expr);
            final Object result = exp.evaluate(effective);
            return result == null ? Boolean.FALSE : (Boolean)result;
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalCond('" + expr + "'): " + exMessage, e);
        }
    }

    /**
     * @see Evaluator#evalAssign(Context, String, Object)
     */
    public void evalAssign(final Context ctx, final String location, final Object data) throws SCXMLExpressionException {
        StringBuilder sb = new StringBuilder(location).append("=").append(ASSIGN_VARIABLE_NAME);
        try {
            ctx.getVars().put(ASSIGN_VARIABLE_NAME, data);
            eval(ctx, sb.toString());
        }
        finally {
            ctx.getVars().remove(ASSIGN_VARIABLE_NAME);
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
        if (!(ctx instanceof JexlContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }
        try {
            final JexlContext effective = getEffectiveContext((JexlContext) ctx);
            final JexlScript jexlScript = getJexlEngine().createScript(script);
            return jexlScript.execute(effective);
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalScript('" + script + "'): " + exMessage, e);
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
     * @return new JexlEngine instance
     */
    protected JexlEngine createJexlEngine() {
        // With null prefix, define top-level user defined functions.
        // See javadoc of org.apache.commons.jexl2.JexlEngine#setFunctions(Map<String,Object> funcs) for detail.
        Map<String, Object> funcs = new HashMap<>();
        funcs.put(null, JexlBuiltin.class);
        return new JexlBuilder().namespaces(funcs).strict(jexlEngineStrict).silent(jexlEngineSilent).cache(256).create();
    }

    /**
     * Returns the internal JexlEngine if existing.
     * Otherwise, it creates a new engine by invoking {@link #createJexlEngine()}.
     * <P>
     * <EM>NOTE: The internal JexlEngine instance can be null when this is deserialized.</EM>
     * </P>
     * @return the current JexlEngine
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
    protected JexlContext getEffectiveContext(final JexlContext nodeCtx) {
        return new JexlContext(nodeCtx, new EffectiveContextMap(nodeCtx));
    }
}

