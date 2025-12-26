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
 * &lt;cancel&gt; SCXML element.
 */
public class Cancel extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ID of the send message that should be canceled.
     */
    private String sendid;

    /**
     * The expression that evaluates to the ID of the send message that should be canceled.
     */
    private String sendidexpr;

    /**
     * Constructs a new instance.
     */
    public Cancel() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        final EnterableState parentState = getParentEnterableState();
        final Context ctx = exctx.getContext(parentState);
        final Evaluator eval = exctx.getEvaluator();

        String sendidValue = sendid;
        if (sendidValue == null && sendidexpr != null) {
            sendidValue = (String) eval.eval(ctx, sendidexpr);
            if ((sendidValue == null || sendidValue.trim().isEmpty())
                    && exctx.getAppLog().isWarnEnabled()) {
                exctx.getAppLog().warn("<send>: sendid expression \"" + sendidexpr
                        + "\" evaluated to null or empty String");
            }
        }

        exctx.getEventDispatcher().cancel(sendidValue);
    }

    /**
     * Gets the ID of the send message that should be canceled.
     *
     * @return the sendid.
     */
    public String getSendid() {
        return sendid;
    }

    /**
     * Gets the expression that evaluates to the ID of the send message that should be canceled.
     *
     * @return the expression that evaluates to the ID of the send message that should be canceled.
     */
    public String getSendidexpr() {
        return sendidexpr;
    }

    /**
     * Sets the ID of the send message that should be canceled.
     *
     * @param sendid The sendid to set.
     */
    public void setSendid(final String sendid) {
        this.sendid = sendid;
    }

    /**
     * Sets the expression that evaluates to the ID of the send message that should be canceled.
     *
     * @param sendidexpr the expression that evaluates to the ID of the send message that should be canceled.
     */
    public void setSendidexpr(final String sendidexpr) {
        this.sendidexpr = sendidexpr;
    }
}

