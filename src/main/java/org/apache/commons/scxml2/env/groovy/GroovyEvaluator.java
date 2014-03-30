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

import groovy.lang.Script;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.env.EffectiveContextMap;
import org.w3c.dom.Node;

/**
 * Evaluator implementation enabling use of Groovy expressions in SCXML documents.
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
            if (script == null || script.length() == 0) {
                return script;
            }
            StringBuffer sb = null;
            Matcher m = GROOVY_OPERATOR_ALIASES_PATTERN.matcher(script);
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

    public GroovyEvaluator(boolean useInitialScriptAsBaseScript) {
        this.useInitialScriptAsBaseScript = useInitialScriptAsBaseScript;
        this.scriptCache = newScriptCache();
    }

    /**
     * Overridable factory method to create the GroovyExtendableScriptCache for this GroovyEvaluator.
     * <p>
     * The default implementation configures the scriptCache to use the {@link #scriptPreProcessor GroovyEvaluator scriptPreProcessor}
     * and the {@link GroovySCXMLScript} as script base class.
     * </p>
     */
    protected GroovyExtendableScriptCache newScriptCache() {
        GroovyExtendableScriptCache scriptCache = new GroovyExtendableScriptCache();
        scriptCache.setScriptPreProcessor(getScriptPreProcessor());
        scriptCache.setScriptBaseClass(GroovySCXMLScript.class.getName());
        return scriptCache;
    }

    @SuppressWarnings("unchecked")
    protected Script getScript(GroovyContext groovyContext, String scriptBaseClassName, String scriptSource) {
        Script script = scriptCache.getScript(scriptBaseClassName, scriptSource);
        script.setBinding(groovyContext.getBinding());
        return script;
    }

    @SuppressWarnings("unused")
    public void clearCache() {
        scriptCache.clearCache();
    }

    public GroovyExtendableScriptCache.ScriptPreProcessor getScriptPreProcessor() {
        return scriptPreProcessor;
    }

    /* SCXMLEvaluator implementation methods */

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

        GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            return getScript(getEffectiveContext(groovyCtx), groovyCtx.getScriptBaseClass(), expr).run();
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
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

        GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            return (Boolean)getScript(getEffectiveContext(groovyCtx), groovyCtx.getScriptBaseClass(), expr).run();
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalCond('" + expr + "'): " + exMessage, e);
        }
    }

    /**
     * @see Evaluator#evalLocation(Context, String)
     */
    @Override
    public Node evalLocation(final Context ctx, final String expr) throws SCXMLExpressionException {
        if (expr == null) {
            return null;
        }

        if (!(ctx instanceof GroovyContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            effective.setEvaluatingLocation(true);
            return (Node)getScript(effective, groovyCtx.getScriptBaseClass(), expr).run();
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalLocation('" + expr + "'): " + exMessage, e);
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

        GroovyContext groovyCtx = (GroovyContext) ctx;
        if (groovyCtx.getGroovyEvaluator() == null) {
            groovyCtx.setGroovyEvaluator(this);
        }
        try {
            final GroovyContext effective = getEffectiveContext(groovyCtx);
            effective.setEvaluatingLocation(true);
            boolean inGlobalContext = groovyCtx.getParent() instanceof SCXMLSystemContext;
            Script script = getScript(effective, groovyCtx.getScriptBaseClass(), scriptSource);
            Object result = script.run();
            if (inGlobalContext && useInitialScriptAsBaseScript) {
                groovyCtx.setScriptBaseClass(script.getClass().getName());
            }
            return result;
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("evalScript('" + scriptSource + "'): " + exMessage, e);
        }
    }

    protected ClassLoader getGroovyClassLoader() {
        return scriptCache.getGroovyClassLoader();
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
     * Create a new context which is the summation of contexts from the
     * current state to document root, child has priority over parent
     * in scoping rules.
     *
     * @param nodeCtx The GroovyContext for this state.
     * @return The effective GroovyContext for the path leading up to
     *         document root.
     */
    private GroovyContext getEffectiveContext(final GroovyContext nodeCtx) {
        return new GroovyContext(new EffectiveContextMap(nodeCtx), this);
    }
}