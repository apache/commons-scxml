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

import java.util.UUID;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.XPathBuiltin;
import org.apache.commons.scxml2.model.SCXML;

/**
 * Embedded JavaScript expression evaluator for SCXML expressions. This
 * implementation is a just a 'thin' wrapper around the Javascript engine in
 * JDK 6 (based on on Mozilla Rhino 1.6.2).
 * <p>
 * Mozilla Rhino 1.6.2 does not support E4X so accessing the SCXML data model
 * is implemented in the same way as the JEXL expression evaluator i.e. using
 * the Data() function, for example,
 * &lt;assign location="Data(hotelbooking,'hotel/rooms')" expr="2" /&gt;
 * <p>
 */

public class JSEvaluator implements Evaluator {

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
        public Evaluator getEvaluator(final SCXML document) {
            return new JSEvaluator();
        }
    }

    /** Pattern for recognizing the SCXML In() special predicate. */
    private static final Pattern IN_FN = Pattern.compile("In\\(");
    /** Pattern for recognizing the Commons SCXML Data() builtin function. */
    private static final Pattern DATA_FN = Pattern.compile("Data\\(");
    /** Pattern for recognizing the Commons SCXML Location() builtin function. */
    private static final Pattern LOCATION_FN = Pattern.compile("Location\\(");

    // INSTANCE VARIABLES

    private ScriptEngineManager factory;

    // CONSTRUCTORS

    /**
     * Initialises the internal Javascript engine factory.
     */
    public JSEvaluator() {
        factory = new ScriptEngineManager();
    }

    // INSTANCE METHODS

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
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
     * Evaluates the expression using a new Javascript engine obtained from
     * factory instantiated in the constructor. The engine is supplied with
     * a new JSBindings that includes the SCXML Context and
     * <code>Data()</code> functions are replaced with an equivalent internal
     * Javascript function.
     *
     * @param context    SCXML context.
     * @param expression Expression to evaluate.
     *
     * @return Result of expression evaluation or <code>null</code>.
     *
     * @throws SCXMLExpressionException Thrown if the expression was invalid.
     */
    @Override
    public Object eval(Context context,String expression) throws SCXMLExpressionException {
        try {

            // ... initialize
            ScriptEngine engine   = factory.getEngineByName("JavaScript");
            Bindings     bindings = engine.getBindings     (ScriptContext.ENGINE_SCOPE);

            // ... replace built-in functions
            String jsExpression = IN_FN.matcher(expression).replaceAll("_builtin.In(");
            jsExpression = DATA_FN.matcher(jsExpression).replaceAll("_builtin.Data(");
            jsExpression = LOCATION_FN.matcher(jsExpression).replaceAll("_builtin.Location(");

            // ... evaluate
            JSBindings jsBindings = new JSBindings(context, bindings);
            jsBindings.put("_builtin", new JSFunctions(context));
            return engine.eval(jsExpression,jsBindings);

        } catch (Exception x) {
            throw new SCXMLExpressionException("Error evaluating ['" + expression + "'] " + x);
        }
    }

    /**
     * Evaluates a conditional expression using the <code>eval()</code> method and
     * casting the result to a Boolean.
     *
     * @param context    SCXML context.
     * @param expression Expression to evaluate.
     *
     * @return Boolean or <code>null</code>.
     *
     * @throws SCXMLExpressionException Thrown if the expression was invalid or did
     *                                  not return a boolean.
     */
    @Override
    public Boolean evalCond(Context context,String expression) throws SCXMLExpressionException {
        final Object result = eval(context,expression);

        if (result == null) {
           return Boolean.FALSE;
        }

        if (result instanceof Boolean) {
           return (Boolean)result;
        }

        throw new SCXMLExpressionException("Invalid boolean expression: " + expression);
    }

    /**
     * Evaluates a location expression using a new Javascript engine obtained from
     * factory instantiated in the constructor. The engine is supplied with
     * a new JSBindings that includes the SCXML Context and
     * <code>Data()</code> functions are replaced with an equivalent internal
     * Javascript function.
     *
     * @param context    FSM context.
     * @param expression Expression to evaluate.
     *
     * @throws SCXMLExpressionException Thrown if the expression was invalid.
     */
    @Override
    public Object evalLocation(Context context,String expression) throws SCXMLExpressionException {
        if (expression == null) {
            return null;
        }
        else if (context.has(expression)) {
            return expression;
        }
        return eval(context, expression);
    }

    /**
     * @see Evaluator#evalAssign(Context, String, Object, AssignType, String)
     */
    public void evalAssign(final Context ctx, final String location, final Object data, final AssignType type,
                           final String attr) throws SCXMLExpressionException {

        Object loc = evalLocation(ctx, location);
        if (loc != null) {

            if (XPathBuiltin.isXPathLocation(ctx, loc)) {
                XPathBuiltin.assign(ctx, loc, data, type, attr);
            }
            else {
                StringBuilder sb = new StringBuilder(location).append("=").append(ASSIGN_VARIABLE_NAME);
                try {
                    ctx.getVars().put(ASSIGN_VARIABLE_NAME, data);
                    eval(ctx, sb.toString());
                }
                finally {
                    ctx.getVars().remove(ASSIGN_VARIABLE_NAME);
                }
            }
        }
        else {
            throw new SCXMLExpressionException("evalAssign - cannot resolve location: '" + location + "'");
        }
    }

    /**
     * Executes the script using a new Javascript engine obtained from
     * factory instantiated in the constructor. The engine is supplied with
     * a new JSBindings that includes the SCXML Context and
     * <code>Data()</code> functions are replaced with an equivalent internal
     * Javascript function.
     *
     * @param ctx    SCXML context.
     * @param script Script to execute.
     *
     * @return Result of script execution or <code>null</code>.
     *
     * @throws SCXMLExpressionException Thrown if the script was invalid.
     */
    @Override
    public Object evalScript(Context ctx, String script)
    throws SCXMLExpressionException {
        return eval(ctx, script);
    }
}
