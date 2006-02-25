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
package org.apache.commons.scxml.model;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.TriggerEvent;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;var&gt; SCXML element.
 *
 */
public class Var extends Action {

    /**
     * The name of the variable to be created.
     */
    private String name;

    /**
     * The expression that evaluates to the initial value of the variable.
     */
    private String expr;

    /**
     * Constructor.
     */
    public Var() {
        super();
    }

    /**
     * Get the expression that evaluates to the initial value
     * of the variable.
     *
     * @return String Returns the expr.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression that evaluates to the initial value
     * of the variable.
     *
     * @param expr The expr to set.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the name of the (new) variable.
     *
     * @return String Returns the name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name of the (new) variable.
     *
     * @param name The name to set.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection derivedEvents)
    throws ModelException, SCXMLExpressionException {
        Context ctx = scInstance.getContext(getParentState());
        Evaluator eval = scInstance.getEvaluator();
        Object varObj = eval.eval(ctx, expr);
        ctx.setLocal(name, varObj);
        TriggerEvent ev = new TriggerEvent(name + ".change",
                TriggerEvent.CHANGE_EVENT);
        derivedEvents.add(ev);
    }

}

