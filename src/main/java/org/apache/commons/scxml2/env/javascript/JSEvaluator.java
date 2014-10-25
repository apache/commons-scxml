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

import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.scxml2.Builtin;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.model.SCXML;
import org.w3c.dom.Node;

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

    // CONSTANTS

    private static final String SUPPORTED_DATAMODEL = "ecmascript";

    public static class JSEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATAMODEL;
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

    // INSTANCE VARIABLES

    private ScriptEngineManager factory;

    // CONSTRUCTORS

    /**
     * Initialises the internal Javascript engine factory.
     */
    public JSEvaluator() {
        factory = new ScriptEngineManager();
        factory.put("_builtin", new Builtin());
    }

    // INSTANCE METHODS

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATAMODEL;
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
            String jsExpression = IN_FN.matcher(expression).replaceAll("_builtin.isMember("+SCXMLSystemContext.ALL_STATES_KEY +", ");
            jsExpression = DATA_FN.matcher(jsExpression).replaceAll("_builtin.data("+Context.NAMESPACES_KEY+", ");

            // ... evaluate
            return engine.eval(jsExpression,new JSBindings(context,bindings));

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
        Object object;

        if ((object = eval(context,expression)) == null) {
           return Boolean.FALSE;
        }

        if (object instanceof Boolean) {
           return (Boolean) object;
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
    public Node evalLocation(Context context,String expression) throws SCXMLExpressionException {
        try {

            // ... initialize
            ScriptEngine engine   = factory.getEngineByName("JavaScript");
            Bindings     bindings = engine.getBindings     (ScriptContext.ENGINE_SCOPE);

            // ... replace built-in functions
            String jsExpression = IN_FN.matcher(expression).replaceAll("_builtin.isMember(_ALL_STATES, ");
            jsExpression = DATA_FN.matcher(jsExpression).replaceFirst("_builtin.dataNode("+Context.NAMESPACES_KEY+", ");
            jsExpression = DATA_FN.matcher(jsExpression).replaceAll("_builtin.data("+Context.NAMESPACES_KEY+", ");

            // ... evaluate
            return (Node) engine.eval(jsExpression,new JSBindings(context,bindings));

        } catch (Exception x) {
            throw new SCXMLExpressionException("Error evaluating ['" + expression + "'] " + x);
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

