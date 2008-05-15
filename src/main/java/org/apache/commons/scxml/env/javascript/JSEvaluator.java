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

package org.apache.commons.scxml.env.javascript;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.scxml.Builtin;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLHelper;
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
 * NOTES:
 * <ol>
 *   <li>To use _eventdatamap with the Javascript evaluator replace all
 *     _eventdatamap[event] operators with _eventdatamap.get(event) or
 *     _eventdatamap.put(event,data).<br/>
 *     (the SCXML _eventdatamap is implemented as a Java HashMap and the
 *     Rhino interpreter does not implement the [] operator on Java Maps).
 *   </li>
 * </ol>
 *
 */

public class JSEvaluator implements Evaluator {

    // CONSTANTS

    private static final Pattern XPATH =
        Pattern.compile("Data\\s*\\(\\s*(\\w+)\\s*,\\s*((?:'.*?')|(?:\".*?\"))\\s*\\)");

    // INSTANCE VARIABLES

    private ScriptEngineManager factory;

    // CONSTRUCTORS

    /**
     * Initialises the internal Javascript engine factory.
     */
    public JSEvaluator() {
        factory = new ScriptEngineManager();

        factory.put("xpath",this);
    }

    // INSTANCE METHODS

    /**
     * Creates a child context.
     *
     * @param  context FSM parent context.
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
    @SuppressWarnings("unchecked")
    @Override
    public Object eval(Context context,String expression) throws SCXMLExpressionException {
        try {
            // ... initialise

            Matcher      matcher  = XPATH.matcher   (expression);
            StringBuffer buffer   = new StringBuffer();
            ScriptEngine engine   = factory.getEngineByName("JavaScript");
            Bindings     bindings = engine.getBindings     (ScriptContext.ENGINE_SCOPE);

            // ... replace Data() functions

            while (matcher.find()) {
                  matcher.appendReplacement(buffer,"xpath.evaluate(_ALL_NAMESPACES," + matcher.group(1) + "," + matcher.group(2) + ")");
            }

            matcher.appendTail(buffer);

            // ... evaluate

            return engine.eval(buffer.toString(),new JSBindings(context,bindings));
        } catch (Throwable x) {
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
           return null;
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
    @SuppressWarnings("unchecked")
    @Override
    public Node evalLocation(Context context,String expression) throws SCXMLExpressionException {
        try {
            // ... initialise

            Matcher        matcher  = XPATH.matcher   (expression);
            StringBuffer   buffer   = new StringBuffer();
            ScriptEngine   engine   = factory.getEngineByName("JavaScript");
            Bindings       bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

            // ... replace Data() function

            while (matcher.find()) {
                  matcher.appendReplacement(buffer,"xpath.node(_ALL_NAMESPACES," + matcher.group(1) + "," + matcher.group(2) + ")");
            }

            matcher.appendTail(buffer);

            // ... evaluate

            return (Node) engine.eval(buffer.toString(),new JSBindings(context,bindings));
        } catch (Throwable x) {
            throw new SCXMLExpressionException("Error evaluating ['" + expression + "'] " + x);
        }
    }

    /**
     * Implementation of Javascript function equivalent for the Data() function when
     * used in an SCXML <code>expr</code> attribute.
     * <p>
     * NOTE: Only declared public for access by script engine - not intended to be
     *       used by anything else.
     *
     * @param namespaces SCXML namespace map.
     * @param node       Data() function root node.
     * @param query      Data() function expression.
     *
     * @return Value stored at SCXML data model node represented by the <code>query</code>
     *         expression or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public Object evaluate(Map namespaces,Object node,String query) {
        return SCXMLHelper.getNodeValue(node(namespaces,node,query));
    }

    /**
     * Implementation of Javascript function equivalent for the Data() function when used
     * in an SCXML <code>location</code> attribute.
     * <p>
     * NOTE: Only declared public for access by script engine - not intended to be
     *       used by anything else.
     *
     * @param namespaces SCXML namespace map.
     * @param node       Data() function root node.
     * @param query      Data() function expression.
     *
     * @return Node at SCXML data model node represented by the <code>query</code>
     *         expression or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public Node node(Map namespaces,Object node,String query) {
        return Builtin.dataNode(namespaces,node,query);
    }

}

