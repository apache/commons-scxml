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
package org.apache.commons.scxml.env.rhino;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.w3c.dom.Node;

/**
 * @see Evaluator
 */
public class RhinoEvaluator implements Evaluator, Serializable {

    /** Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /** Pattern for recognizing the SCXML In() special predicate. */
    private static final Pattern IN_FN = Pattern.compile("In\\(");
    // And E4X over Data()

    /**
     * Constructor.
     */
    public RhinoEvaluator() {
        super();
    }

    /**
     * @see Evaluator#eval(org.apache.commons.scxml.Context, String)
     */
    public Object eval(final org.apache.commons.scxml.Context ctx, final String expr)
            throws SCXMLExpressionException {
        if(expr == null) {
            return null;
        }

        RhinoContext rhinocx = getRhinoContext(ctx);
        Context cx = Context.enter();
        String jsExpression = IN_FN.matcher(expr).
            replaceAll("Packages.org.apache.commons.scxml.Builtin.isMember(_ALL_STATES, ");

        try {
            Script compliedScript = cx.compileString(jsExpression, "RhinoEvaluator", 1, null);
            return compliedScript.exec(cx, rhinocx.getScope());
        } catch(Exception e) {
            throw new SCXMLExpressionException("eval('" + expr + "'):" + jsExpression + ":" + e.getMessage(), e);
        } finally {
            Context.exit();
        }

    }

    /**
     * @see Evaluator#evalCond(org.apache.commons.scxml.Context, String)
     */
    public Boolean evalCond(final org.apache.commons.scxml.Context ctx,
            final String expr)
            throws SCXMLExpressionException {
        if(expr == null) {
            return null;
        }

        RhinoContext rhinocx = getRhinoContext(ctx);
        Context cx = Context.enter();
        String jsExpression = IN_FN.matcher(expr).
            replaceAll("Packages.org.apache.commons.scxml.Builtin.isMember(_ALL_STATES, ");

        try {
            Script compliedScript = cx.compileString(jsExpression, "RhinoEvaluator", 1, null);
            return (Boolean) compliedScript.exec(cx, rhinocx.getScope());
        } catch(Exception e) {
            throw new SCXMLExpressionException("evalCond('" + expr + "'):" + e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    /**
     * @see Evaluator#evalLocation(org.apache.commons.scxml.Context, String)
     * @deprecated because of E4X availability of Rhino
     */
    public Node evalLocation(final org.apache.commons.scxml.Context ctx, final String expr)
    throws SCXMLExpressionException {
        if(expr == null) {
            return null;
        }

        // We cannot convert back the child elements, so location will not work!
        throw new SCXMLExpressionException("evalLocation('" + expr
            + "'): You should use E4X in script tag instead of location.");
    }

    /**
     * @see Evaluator#evalScript(org.apache.commons.scxml.Context, String)
     */
    public Object evalScript(org.apache.commons.scxml.Context ctx, String script)
            throws SCXMLExpressionException {
        return eval(ctx, script);
    }

    /**
     * @see Evaluator#newContext(org.apache.commons.scxml.Context)
     */
    public org.apache.commons.scxml.Context newContext(
            final org.apache.commons.scxml.Context parent) {
        return new RhinoContext(parent);
    }

    /**
     * Gets the RhinoContext.
     *
     * @param ctx The Context
     * @return RhinoContext
     * @throws SCXMLExpressionException If Context is not an instance of RhinoContext
     */
    private RhinoContext getRhinoContext(org.apache.commons.scxml.Context ctx)
            throws SCXMLExpressionException {
        if(ctx instanceof RhinoContext) {
            return (RhinoContext)ctx;
        } else {
            throw new SCXMLExpressionException("Error evaluating Rhino expression. "
                + "Context must be a org.apache.commons.scxml.env.rhino.RhinoContext");
        }
    }

}

