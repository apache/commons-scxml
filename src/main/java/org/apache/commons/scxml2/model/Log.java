/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.model;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;log&gt; SCXML element.
 */
public class Log extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An expression evaluating to a string to be logged.
     */
    private String expr;

    /**
     * An expression which returns string which may be used, for example,
     * to indicate the purpose of the log.
     */
    private String label;

    /**
     * Constructs a new instance.
     */
    public Log() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        final Context ctx = exctx.getContext(getParentEnterableState());
        final Evaluator eval = exctx.getEvaluator();
        exctx.getAppLog().info(label + ": " + String.valueOf(eval.eval(ctx, expr)));
    }

    /**
     * Gets the log expression.
     *
     * @return the expression.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Gets the log label.
     *
     * @return the label.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Sets the log expression.
     *
     * @param expr The expr to set.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Sets the log label.
     *
     * @param label The label to set.
     */
    public final void setLabel(final String label) {
        this.label = label;
    }
}

