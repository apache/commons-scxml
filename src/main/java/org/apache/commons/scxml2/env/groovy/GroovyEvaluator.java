/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.env.groovy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.env.AbstractBaseEvaluator;
import org.apache.commons.scxml2.env.EffectiveContextMap;
import org.apache.commons.scxml2.model.SCXML;

import groovy.lang.Script;

/**
 * Evaluator implementation enabling use of Groovy expressions in SCXML documents.
 * <p>
 * This implementation itself is thread-safe, so you can keep singleton for efficiency.
 * </p>
 */
public class GroovyEvaluator extends AbstractBaseEvaluator {

    public static class GroovyEvaluatorProvider implements EvaluatorProvider {

        @Override
        public Evaluator getEvaluator() {
            return new GroovyEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new GroovyEvaluator();
        }

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATA_MODEL;
        }
    }

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    public static final String SUPPORTED_DATA_MODEL = "groovy";

    /** Error message if evaluation context is not a GroovyContext. */
    private static final String ERR_CTX_TYPE = "Error evaluating Groovy "
            + "expression, Context must be a org.apache.commons.scxml2.env.groovy.GroovyContext";

    protected static final GroovyExtendableScriptCache.ScriptPreProcessor scriptPreProcessor = new GroovyExtendableScriptCache.ScriptPreProcessor () {

        /**
         * Pattern for case-sensitive matching of the Groovy operator aliases, delimited by whitespace
         */
        public final Pattern GROOVY_OPERATOR_ALIASES_PATTERN = Pattern.compile("(?<=\\s)(and|or|not|eq|lt|le|ne|gt|ge)(?=\\s)");

        /**
         * Groovy operator aliases mapped to their underlying Groovy operator
         */
        public final Map<String, String> GROOVY_OPERATOR_ALIASES = Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("and", "&& "); put("or",  "||"); put("not", " ! ");
            put("eq",  "==");  put("lt",  "< "); put("le",  "<=");
            put("ne",  "!=");  put("gt",  "> "); put("ge",  ">=");
        }});

        @Override
        public String preProcess(final String script) {
            if (script == null || script.isEmpty()) {
                return script;
            }
            StringBuffer sb = null;
            final Matcher m = GROOVY_OPERATOR_ALIASES_PATTERN.matcher(script);
            while (m.find()) {
                if (sb == null) {
                    sb = new StringBuffer();
                }
                m.appendReplacement(sb, GROOVY_OPERATOR_ALIASES.get(m.group()));
            }
            if (sb != null) {
                m.appendTail(sb);
                return sb.toString();
            }
            return script;
        }
    };

    private final boolean useInitialScriptAsBaseScript;
    private final GroovyExtendableScriptCache scriptCache;

    public GroovyEvaluator() {
        this(false);
    }

    public GroovyEvaluator(final boolean useInitialScriptAsBaseScript) {
        this.useInitialScriptAsBaseScript = useInitialScriptAsBaseScript;
        this.scriptCache = newScriptCache();
    }

    @SuppressWarnings("unused")
    public void clearCache() {
        scriptCache.clearCache();
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
    @Override
    public Object eval(final Context ctx, final String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }

        if (!(ctx instanceof GroovyContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        final GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            return getScript(getEffectiveContext(groovyCtx), groovyCtx.getScriptBaseClass(), expr).run();
        }
        catch (final Exception e) {
            final String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("eval('" + expr + "'): " + exMessage, e);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    @Override
    public Boolean evalCond(final Context ctx, final String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }

        if (!(ctx instanceof GroovyContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        final GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            final Object result = getScript(getEffectiveContext(groovyCtx), groovyCtx.getScriptBaseClass(), expr).run();
            return result == null ? Boolean.FALSE : (Boolean)result;
        } catch (final Exception e) {
            final String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalCond('" + expr + "'): " + exMessage, e);
        }
    }

    /**
     * @see Evaluator#evalScript(Context, String)
     */
    @Override
    public Object evalScript(final Context ctx, final String scriptSource) throws SCXMLExpressionException {
        if (scriptSource == null) {
            return null;
        }

        if (!(ctx instanceof GroovyContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        final GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            final boolean inGlobalContext = groovyCtx.getParent() instanceof SCXMLSystemContext;
            final Script script = getScript(effective, groovyCtx.getScriptBaseClass(), scriptSource);
            final Object result = script.run();
            if (inGlobalContext && useInitialScriptAsBaseScript) {
                groovyCtx.setScriptBaseClass(script.getClass().getName());
            }
            return result;
        } catch (final Exception e) {
            final String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalScript('" + scriptSource + "'): " + exMessage, e);
        }
    }

    /* SCXMLEvaluator implementation methods */

    /**
     * Create a new context which is the summation of contexts from the
     * current state to document root, child has priority over parent
     * in scoping rules.
     *
     * @param nodeCtx The GroovyContext for this state.
     * @return The effective GroovyContext for the path leading up to
     *         document root.
     */
    protected GroovyContext getEffectiveContext(final GroovyContext nodeCtx) {
        return new GroovyContext(nodeCtx, new EffectiveContextMap(nodeCtx), this);
    }

    protected ClassLoader getGroovyClassLoader() {
        return scriptCache.getGroovyClassLoader();
    }

    protected Script getScript(final GroovyContext groovyContext, final String scriptBaseClassName, final String scriptSource) {
        final Script script = scriptCache.getScript(scriptBaseClassName, scriptSource);
        script.setBinding(groovyContext.getBinding());
        return script;
    }

    public GroovyExtendableScriptCache.ScriptPreProcessor getScriptPreProcessor() {
        return scriptPreProcessor;
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    /**
     * Create a new child context.
     *
     * @param parent parent context
     * @return new child context
     * @see Evaluator#newContext(Context)
     */
    @Override
    public Context newContext(final Context parent) {
        return new GroovyContext(parent, this);
    }

    /**
     * Overridable factory method to create the GroovyExtendableScriptCache for this GroovyEvaluator.
     * <p>
     * The default implementation configures the scriptCache to use the {@link #scriptPreProcessor GroovyEvaluator scriptPreProcessor}
     * and the {@link GroovySCXMLScript} as script base class.
     * </p>
     *
     * @return GroovyExtendableScriptCache for this GroovyEvaluator
     */
    protected GroovyExtendableScriptCache newScriptCache() {
        final GroovyExtendableScriptCache scriptCache = new GroovyExtendableScriptCache();
        scriptCache.setScriptPreProcessor(getScriptPreProcessor());
        scriptCache.setScriptBaseClass(GroovySCXMLScript.class.getName());
        return scriptCache;
    }

    @Override
    public boolean requiresGlobalContext() {
        return false;
    }
}