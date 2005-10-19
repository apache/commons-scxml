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
package org.apache.commons.scxml.env.faces;

import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.ReferenceSyntaxException;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.env.jsp.ELEvaluator;

/**
 * <p>EL evaluator, for evaluating the expressions in SCXML documents used
 * to specify Shale dialogs.</p>
 *
 * <p>This evaluator uses the following premises for evaluation:
 *   <ol>
 *     <li>JSF method binding expressions will take the form:
 *         #{...}
 *         These will return a <code>java.lang.String</code> object as
 *         required by the methods associated with
 *         <code>org.apache.shale.dialog.ActionState</code></li>
 *     <li>JSP 2.0 EL expressions will take the form:
 *         ${...}</li>
 *   </ol>
 * </p>
 *
 * <p>This evaluator delegates to the following expression evaluators:
 *   <ol>
 *     <li>JSF method binding expressions get delegated to the JSF
 *         implementation (Ex: Apache MyFaces).</li>
 *     <li>JSP expressions get delegated to the Apache Jakarta Commons
 *         EL ExpressionEvaluator implementation. This evaluator adds
 *         ability to evaluate special predicates defined by SCXML.</li>
 *   </ol>
 * </p>
 */
public class ShaleDialogELEvaluator extends ELEvaluator {

    /**
     * Shale ActionState method parameters.
     *
     * see: org.apache.shale.dialog.faces.DialogNavigationHandler.PARAMETERS
     */
    private static final Object[] PARAMETERS = new Object[0];
    /**
     * Shale ActionState method signature.
     *
     * see: org.apache.shale.dialog.faces.DialogNavigationHandler.SIGNATURE
     */
    private static final Class[] SIGNATURE = new Class[0];

    // We do not have the privilege of having specific element attributes
    // associated with specific types of expressions
    /** Pattern for recognizing the method / value binding expression. */
    private static Pattern jsfBindingExpr =
        Pattern.compile("^\\s*#\\{.*\\}\\s*$");
    /** FacesContext for this request. */
    private FacesContext context;

    /**
     * Constructor.
     */
    public ShaleDialogELEvaluator() {
        super();
    }

    /**
     * Set per request context.
     *
     * @param context The FacesContext for this request.
     */
    public void setFacesContext(final FacesContext context) {
        this.context = context;
    }

    /**
     * Evaluate an expression.
     *
     * @param ctx variable context
     * @param expr expression
     * @return Object The result of the evaluation
     * @throws SCXMLExpressionException For a malformed expression
     * @see Evaluator#eval(Context, String)
     */
    public Object eval(final Context ctx, final String expr)
    throws SCXMLExpressionException {
        if (jsfBindingExpr.matcher(expr).matches()) {
            return invokeShaleActionStateMethod(expr);
        }
        return super.eval(ctx, expr);
    }

    /**
     * Invoke method binding expression for Shale <code>ActionState</code>.
     * Shale requires return type to be a <code>java.lang.String</code>.
     *
     * @param expr Method binding expression
     * @return String Method return value
     * @throws SCXMLExpressionException Re-throw potential Faces
     *                  exceptions as a SCXMLExpressionException.
     */
    private String invokeShaleActionStateMethod(final String expr)
    throws SCXMLExpressionException {
        try {
            MethodBinding mb = context.getApplication().
                createMethodBinding(expr, SIGNATURE);
            return (String) mb.invoke(context, PARAMETERS);
        } catch (ReferenceSyntaxException rse) {
            throw new SCXMLExpressionException(rse);
        } catch (MethodNotFoundException mnfe) {
            throw new SCXMLExpressionException(mnfe);
        } catch (EvaluationException ee) {
            throw new SCXMLExpressionException(ee);
        }
    }

}

