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
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

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
    @Override
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection<TriggerEvent> derivedEvents)
    throws ModelException, SCXMLExpressionException {
        Context ctx = scInstance.getContext(getParentState());
        Evaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Object varObj = eval.eval(ctx, expr);
        ctx.setLocal(getNamespacesKey(), null);
        ctx.setLocal(name, varObj);
        if (appLog.isDebugEnabled()) {
            appLog.debug("<var>: Defined variable '" + name
                + "' with initial value '" + String.valueOf(varObj) + "'");
        }
        TriggerEvent ev = new TriggerEvent(name + ".change",
                TriggerEvent.CHANGE_EVENT);
        derivedEvents.add(ev);
    }

}

