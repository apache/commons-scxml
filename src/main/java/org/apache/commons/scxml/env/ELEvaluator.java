/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
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
package org.apache.taglibs.rdc.scxml.env;

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
import org.apache.taglibs.rdc.scxml.Context;
import org.apache.taglibs.rdc.scxml.Evaluator;
import org.apache.taglibs.rdc.scxml.SCXMLExpressionException;
import org.apache.taglibs.rdc.scxml.model.TransitionTarget;

/**
 * EL engine interface for SCXML Interpreter.
 * 
 * @author Jaroslav Gergic
 */
public class ELEvaluator implements Evaluator {

    //let's make the log category implementation independent
    private static Log log = LogFactory.getLog(Evaluator.class);
    private FunctionMapper fm = new FunctWrapper();
    private static Pattern inFct = Pattern.compile("In\\(");

    ExpressionEvaluator ee = null;

    /**
     * Constructor
     */
    public ELEvaluator() {
        ee = new ExpressionEvaluatorImpl();
    }

    /**
     * Evaluate an expression
     * 
     * @param ctx variable context
     * @param expr expression
     * @return a result of the evaluation
     * @throws SCXMLExpressionException
     * @see org.apache.taglibs.rdc.scxml.Evaluator#eval(org.apache.taglibs.rdc.scxml.Context, java.lang.String)
     */
    public Object eval(Context ctx, String expr) 
    throws SCXMLExpressionException {
        VariableResolver vr = null;
        if(ctx instanceof VariableResolver) {
            vr = (VariableResolver)ctx;
        } else {
            vr = new CtxWrapper(ctx);
        }
        try {
            expr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            Object rslt = ee.evaluate(expr, Object.class, vr, fm);
            if(log.isTraceEnabled()) {
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
     * @see org.apache.taglibs.rdc.scxml.Evaluator#newContext(org.apache.taglibs.rdc.scxml.Context)
     */
    public Context newContext(Context parent) {
        //for now, we do not support nested variable contexts
        //world is flat ;)
        if(parent != null) {
            return parent;
        } else {
            return new ELContext(null);
        }
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.Evaluator#evalCond(org.apache.taglibs.rdc.scxml.Context, java.lang.String)
     */
    public Boolean evalCond(Context ctx, String expr) 
    throws SCXMLExpressionException {
        VariableResolver vr = null;
        if(ctx instanceof VariableResolver) {
            vr = (VariableResolver)ctx;
        } else {
            vr = new CtxWrapper(ctx);
        }
        try {
            expr = inFct.matcher(expr).replaceAll("In(_ALL_STATES, ");
            Boolean rslt = (Boolean) ee.evaluate(expr, Boolean.class, vr, fm);
            if(log.isDebugEnabled()) {
                log.debug(expr + " = " + String.valueOf(rslt));                
            }
            return rslt;
        } catch (ELException e) {
            throw new SCXMLExpressionException(e);
        } 
    }
    
    /**
     * A Context wrapper that implements VariableResolver
     */
    class CtxWrapper implements VariableResolver {
        Context ctx = null;
        CtxWrapper(Context ctx) {
            this.ctx = ctx;
        }
        public Object resolveVariable(String pName) throws ELException {
            Object rslt = ctx.get(pName);
            if(rslt == null) {
                throw new ELException("Variable " + pName + "does not exist!");
            }
            return rslt;
        }
    }

    /**
     * A simple function mapper for SCXML defined functions
     */
    class FunctWrapper implements FunctionMapper {

        /**
         * @see javax.servlet.jsp.el.FunctionMapper#resolveFunction(java.lang.String, java.lang.String)
         */
        public Method resolveFunction(String prefix, String localName) {
            if(localName.equals("In")) {
                Class attrs[] = new Class[2];
                attrs[0] = Set.class;
                attrs[1] = String.class;
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
    public static final boolean isMember(Set allStates, String state) {
        Iterator i = allStates.iterator();
        while(i.hasNext()) {
            TransitionTarget tt = (TransitionTarget)i.next();
            if(state.equals(tt.getId())) {
                return true;
            }
        }
        return false;
    }

}
