/*
 *
 *   Copyright 2005 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.scxml.env;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.el.ExpressionEvaluatorImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * EL engine interface for SCXML Interpreter.
 *
 */
public class ELEvaluator implements Evaluator {

    /** Implementation independent log category. */
    private static Log log = LogFactory.getLog(Evaluator.class);
    /** Function Mapper for SCXML expressions. */
    private FunctionMapper fm = new FunctWrapper();
    /** Pattern for recognizing the SCXML In() special predicate. */
    private static Pattern inFct = Pattern.compile("In\\(");

    /** The expression evaluator implementation for the JSP/EL environment. */
    private ExpressionEvaluator ee = null;

    /**
     * Constructor.
     */
    public ELEvaluator() {
        ee = new ExpressionEvaluatorImpl();
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
        VariableResolver vr = null;
        if (ctx instanceof VariableResolver) {
            vr = (VariableResolver) ctx;
        } else {
            vr = new CtxWrapper(ctx);
        }
        try {
            String evalExpr = inFct.matcher(expr).
                replaceAll("In(_ALL_STATES, ");
            Object rslt = ee.evaluate(evalExpr, Object.class, vr, fm);
            if (log.isTraceEnabled()) {
                log.trace(expr + " = " + String.valueOf(rslt));
            }
            return rslt;
        } catch (ELException e) {
            throw new SCXMLExpressionException(e);
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
        //for now, we do not support nested variable contexts
        //world is flat ;)
        if (parent != null) {
            return parent;
        } else {
            return new ELContext(null);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    public Boolean evalCond(final Context ctx, final String expr)
    throws SCXMLExpressionException {
        VariableResolver vr = null;
        if (ctx instanceof VariableResolver) {
            vr = (VariableResolver) ctx;
        } else {
            vr = new CtxWrapper(ctx);
        }
        try {
            String evalExpr = inFct.matcher(expr).
                replaceAll("In(_ALL_STATES, ");
            Boolean rslt = (Boolean) ee.evaluate(evalExpr, Boolean.class,
                vr, fm);
            if (log.isDebugEnabled()) {
                log.debug(expr + " = " + String.valueOf(rslt));
            }
            return rslt;
        } catch (ELException e) {
            throw new SCXMLExpressionException(e);
        }
    }

    /**
     * A Context wrapper that implements VariableResolver.
     */
    static class CtxWrapper implements VariableResolver {
        /** Context to be wrapped. */
        private Context ctx = null;
        /**
         * Constructor.
         * @param ctx The Context to be wrapped.
         */
        CtxWrapper(final Context ctx) {
            this.ctx = ctx;
        }
        /** @see VariableResolver#resolveVariable(String) */
        public Object resolveVariable(final String pName) throws ELException {
            Object rslt = ctx.get(pName);
            if (rslt == null) {
                throw new ELException("Variable " + pName + "does not exist!");
            }
            return rslt;
        }
    }

    /**
     * A simple function mapper for SCXML defined functions.
     */
    static class FunctWrapper implements FunctionMapper {

        /**
         * @see FunctionMapper#resolveFunction(String, String)
         */
        public Method resolveFunction(final String prefix,
                final String localName) {
            if (localName.equals("In")) {
                Class[] attrs = new Class[] {Set.class, String.class};
                try {
                    return ELEvaluator.class.getMethod("isMember", attrs);
                } catch (SecurityException e) {
                    log.error("resolving isMember(Set, String)", e);
                } catch (NoSuchMethodException e) {
                    log.error("resolving isMember(Set, String)", e);
                }
            }
            return null;
        }
    }

    /**
     * Does this state belong to the Set of these States.
     * Simple ID based comparator
     *
     * @param allStates The Set of State objects to look in
     * @param state The State to compare with
     * @return Whether this State belongs to this Set
     */
    public static final boolean isMember(final Set allStates,
            final String state) {
        for (Iterator i = allStates.iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            if (state.equals(tt.getId())) {
                return true;
            }
        }
        return false;
    }

}

