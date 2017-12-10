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

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.env.AbstractBaseEvaluator;
import org.apache.commons.scxml2.env.EffectiveContextMap;
import org.apache.commons.scxml2.model.SCXML;

/**
 * Embedded JavaScript expression evaluator for SCXML expressions using the JDK 8+ Nashorn Script Engine.
 * <p>
 * Each JSEvaluator maintains a single {@link ScriptContext} instance to be used for only a single SCXML instance as
 * the Nashorn global state is shared through the {@link ScriptContext#ENGINE_SCOPE} binding.
 * </p>
 * <p>Sharing and reusing JSEvaluator instances for multiple SCXML instances therefore should <em>not</em> be done.</p>
 * <p>
 * As the JDK Script Engine state is <em>not</em> serializable, and neither are Javascript <code>native</code> Objects,
 * the {@link ScriptContext} state is <em>not</em> retained during serialization (transient).
 * </p>
 * <p>
 * SCXML instance (de)serialization using the javascript language therefore only will work reliably as long as no
 * Javascript native Objects are used/stored in the context nor (other) modifications are made to the Nashorn global state.
 * </p>
 */
public class JSEvaluator extends AbstractBaseEvaluator {

    /**
     * Unique context variable name used for temporary reference to assign data (thus must be a valid variable name)
     */
    private static final String ASSIGN_VARIABLE_NAME = "a"+UUID.randomUUID().toString().replace('-','x');

    public static final String SUPPORTED_DATA_MODEL = Evaluator.ECMASCRIPT_DATA_MODEL;

    public static class JSEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATA_MODEL;
        }

        @Override
        public Evaluator getEvaluator() {
            return new JSEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final boolean strict) {
            return new JSEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new JSEvaluator();
        }
        @Override
        public Evaluator getEvaluator(final boolean strict, final SCXML document) {
            return new JSEvaluator();
        }
    }

    private static final String SCXML_SYSTEM_CONTEXT = "_scxmlSystemContext";

    /** Error message if evaluation context is not a JexlContext. */
    private static final String ERR_CTX_TYPE = "Error evaluating JavaScript "
            + "expression, Context must be a org.apache.commons.scxml2.env.javascript.JSContext";

    /** Nashorn ScriptEngine **/
    private transient ScriptEngine engine;

    /** Nashorn Global initialization script, loaded from <code>init_global.js</code> classpath resource */
    private static String initGlobalsScript;

    /** ScriptContext for a single SCXML instance (JSEvaluator also cannot be shared between SCXML instances) */
    private transient ScriptContext scriptContext;

    /**
     * Initialize the singleton Javascript ScriptEngine to be used with a separate ScriptContext for each SCXML instance
     * not sharing their global scope, see {@link #getScriptContext(JSContext)}.
     * <p>
     * The SCXML required protected system variables and (possible) other Javascript global initializations are defined
     * in a <code>init_global.js</code> script which is pre-loaded as (classpath) resource, to be executed once during
     * initialization of a new Javascript (Nashorn) Global.
     * </p>
     */
    protected synchronized void initEngine() {
        if (engine == null) {
            engine = new ScriptEngineManager().getEngineByName("JavaScript");
            if (initGlobalsScript == null) {
                try {
                    initGlobalsScript = IOUtils.toString(JSEvaluator.class.getResourceAsStream("init_global.js"), "UTF-8");
                }
                catch (IOException ioe) {
                    throw new RuntimeException("Failed to load init_global.js from classpath", ioe);
                }
            }
        }
    }

    /**
     * Get the singleton ScriptEngine, initializing it on first access
     * @return The ScriptEngine
     */
    protected ScriptEngine getEngine() {
        if (engine == null) {
            initEngine();
        }
        return engine;
    }

    /**
     * Get the current ScriptContext or create a new one.
     * <p>
     * The ScriptContext is (to be) shared across invocations for the same SCXML instance as it holds the Javascript 'global'
     * context.
     * </p>
     * <p>
     * The ScriptContext is using a {@link ScriptContext#ENGINE_SCOPE} as provided by the engine, which in case of Nashorn
     * is bound to the Javscript global context. Note: do <em>not</em> confuse this with the {@link ScriptContext#GLOBAL_SCOPE} binding.
     * </p>
     * <p>For a newly created ScriptContext (and thus a new Javascript global context), the Javascript global context is
     * initialized with the required and protected SCXML system variables and builtin In() operator via the
     * <code>init_global.js</code> script, loaded as classpath resource.</p>
     * <p>
     * The SCXML system variables are bound as <code>"_scxmlSystemContext"</code> variable in the ENGINE_SCOPE
     * as needed for the <code>init_global.js</code> script in the global context.
     * This variable is bound to the ENGINE_SCOPE to ensure it cannot be 'shadowed' by an overriding variable assignment.
     * </p>
     * The provided SCXML Context variables are bound via the GLOBAL_SCOPE using a {@link JSBindings} wrapper for each
     * invocation.
     * </p>
     * <p>
     * As the GLOBAL_SCOPE SCXML context variables <em>can</em> be overridden, which will result in new 'shadow'
     * variables in the ENGINE_SCOPE, as well as new variables can be added to the ENGINE_SCOPE during script evaluation,
     * after script execution all ENGINE_SCOPE variables (except the <code>"_scxmlSystemContext"</code> variable) must be
     * copied/merged into the SCXML context to synchronize the SCXML context.
     * </p>
     * @param jsContext The current SCXML context
     * @return The SCXML instance shared ScriptContext
     * @throws ScriptException Thrown if the initialization of the Global Javascript engine itself failed
     */
    protected ScriptContext getScriptContext(JSContext jsContext) throws ScriptException {
        if (scriptContext == null) {
            scriptContext = new SimpleScriptContext();
            scriptContext.setBindings(getEngine().createBindings(), ScriptContext.ENGINE_SCOPE);
            scriptContext.setBindings(new JSBindings(jsContext), ScriptContext.GLOBAL_SCOPE);
            scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put(SCXML_SYSTEM_CONTEXT, jsContext.getSystemContext().getVars());
            getEngine().eval(initGlobalsScript, scriptContext);
        }
        else {
            // ensure updated / replaced SystemContext is used (like after SCXML instance go/reset)
            scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put(SCXML_SYSTEM_CONTEXT, jsContext.getSystemContext().getVars());
            ((JSBindings)scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE)).setContext(jsContext);
        }
        return scriptContext;
    }

    @Override
    public boolean isStrict() {
        return false;
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    /**
     * Javascript engine semantics, using a retained global state, requires global SCXML context execution
     * @return true
     */
    @Override
    public boolean requiresGlobalContext() {
        return true;
    }

    /**
     * Creates a child context.
     *
     * @return Returns a new child JSContext.
     *
     */
    @Override
    public Context newContext(Context parent) {
        return new JSContext(parent);
    }

    /**
     * Evaluates a Javascript expression using an SCXML instance shared {@link #getScriptContext(JSContext)}.
     * <p>
     * After evaluation all the resulting Javascript Global context (in {@link ScriptContext#ENGINE_SCOPE} are first
     * copied/merged back into the SCXML context, before the evaluation result (if any) is returned.
     * </p>
     * @param context    SCXML context.
     * @param expression Expression to evaluate.
     * @return Result of expression evaluation or <code>null</code>.
     * @throws SCXMLExpressionException Thrown if the expression was invalid or the execution raised an error itself.
     */
    @Override
    public Object eval(Context context, String expression) throws SCXMLExpressionException {
        if (expression == null) {
            return null;
        }
        if (!(context instanceof JSContext)) {
            throw new SCXMLExpressionException(ERR_CTX_TYPE);
        }

        try {
            JSContext effectiveContext = getEffectiveContext((JSContext)context);
            ScriptContext scriptContext = getScriptContext(effectiveContext);
            Object ret = getEngine().eval(expression, scriptContext);
            // copy Javascript global variables to SCXML context.
            copyJavascriptGlobalsToScxmlContext(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE), effectiveContext);
            return ret;
        } catch (Exception e) {
            String exMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
            throw new SCXMLExpressionException("eval('" + expression + "'): " + exMessage, e);
        }
    }

    /**
     * Evaluates a conditional expression using the <code>eval()</code> method and
     * casting the result to a Boolean.
     *
     * @param context    SCXML context.
     * @param expression Expression to evaluate.
     *
     * @return Boolean casted result.
     *
     * @throws SCXMLExpressionException Thrown if the expression was invalid.
     */
    @Override
    public Boolean evalCond(Context context, String expression) throws SCXMLExpressionException {
        return (Boolean)eval(context, "Boolean("+expression+")");
    }

    /**
     * @see Evaluator#evalAssign(Context, String, Object)
     */
    public void evalAssign(final Context ctx, final String location, final Object data) throws SCXMLExpressionException {
        StringBuilder sb = new StringBuilder(location).append("=").append(ASSIGN_VARIABLE_NAME);
        try {
            ctx.getVars().put(ASSIGN_VARIABLE_NAME, data);
            eval(ctx, sb.toString());
        } catch (SCXMLExpressionException e) {
            if (e.getCause() != null && e.getCause() != null && e.getCause().getMessage() != null) {
                throw new SCXMLExpressionException("Error evaluating assign to location=\"" + location + "\": " + e.getCause().getMessage());
            }
            throw e;
        } finally {
            ctx.getVars().remove(ASSIGN_VARIABLE_NAME);
        }
    }

    /**
     * Executes the Javascript script using the <code>eval()</code> method
     *
     * @param ctx    SCXML context.
     * @param script Script to execute.
     *
     * @return Result of script execution or <code>null</code>.
     *
     * @throws SCXMLExpressionException Thrown if the script was invalid.
     */
    @Override
    public Object evalScript(Context ctx, String script) throws SCXMLExpressionException {
        return eval(ctx, script);
    }

    /**
     * Create a new context which is the summation of contexts from the
     * current state to document root, child has priority over parent
     * in scoping rules.
     *
     * @param nodeCtx The JSContext for this state.
     * @return The effective JSContext for the path leading up to
     *         document root.
     */
    protected JSContext getEffectiveContext(final JSContext nodeCtx) {
        return new JSContext(nodeCtx, new EffectiveContextMap(nodeCtx));
    }

    /**
     * Copy the Javscript global context (i.e. nashorn Global instance) variables to SCXML {@code jsContext}
     * in order to make sure all the new global variables set by the JavaScript engine after evaluation are
     * available from {@link JSContext} instance as well.
     * <p>Note: the internal <code>"_scxmlSystemContext</code> variable is always skipped.</p>
     * @param global The Javascript Bindings holding the Javascript Global context variables
     * @param jsContext The SCXML context to copy/merge the variables into
     */
    private void copyJavascriptGlobalsToScxmlContext(final Bindings global, final JSContext jsContext) {
        if (global != null) {
            for (String key : global.keySet()) {
                if (!SCXML_SYSTEM_CONTEXT.equals(key)) {
                    jsContext.set(key, global.get(key));
                }
            }
        }
    }

    /**
     * When directly injecting data in the local context, wrap Java array and List objects with a native Javascript
     * Array
     * @param ctx SCXML context
     * @param id context id of the data
     * @param data data to inject
     * @throws SCXMLExpressionException
     */
    public void injectData(final Context ctx, final String id, final Object data) throws SCXMLExpressionException {
        ctx.setLocal(id, data);
        if (data != null && (data.getClass().isArray() || data instanceof List)) {
            // use Nashorn extension: Java.from function
            ctx.setLocal(id, eval(ctx, "Java.from("+id+")"));
        }
    }
}
