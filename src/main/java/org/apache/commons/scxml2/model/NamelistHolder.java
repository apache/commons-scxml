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
package org.apache.commons.scxml2.model;

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.semantics.ErrorConstants;

/**
 * A <code>NamelistHolder</code> represents an element in the SCXML
 * document that may have a namelist attribute to
 * produce payload for events or external communication.
 */
public abstract class NamelistHolder extends ParamsContainer {

    /**
     * The namelist.
     */
    private String namelist;

    /**
     * Get the namelist.
     *
     * @return String Returns the namelist.
     */
    public final String getNamelist() {
        return namelist;
    }

    /**
     * Set the namelist.
     *
     * @param namelist The namelist to set.
     */
    public final void setNamelist(final String namelist) {
        this.namelist = namelist;
    }

    /**
     * Adds data to the payload data map based on the namelist which names are location expressions
     * (typically data ids or for example XPath variables). The names and the values they 'point' at
     * are added to the payload data map.
     * @param exctx The ActionExecutionContext
     * @param payload the payload data map to be updated
     * @throws ModelException if this action has not an EnterableState as parent
     * @throws SCXMLExpressionException if a malformed or invalid expression is evaluated
     * @see PayloadProvider#addToPayload(String, Object, java.util.Map)
     */
    protected void addNamelistDataToPayload(ActionExecutionContext exctx, Map<String, Object> payload)
            throws ModelException, SCXMLExpressionException {
        if (namelist != null) {
            EnterableState parentState = getParentEnterableState();
            Context ctx = exctx.getContext(parentState);
            try {
                ctx.setLocal(getNamespacesKey(), getNamespaces());
                Evaluator evaluator = exctx.getEvaluator();
                StringTokenizer tkn = new StringTokenizer(namelist);
                boolean xpathEvaluator = Evaluator.XPATH_DATA_MODEL.equals(evaluator.getSupportedDatamodel());
                while (tkn.hasMoreTokens()) {
                    String varName = tkn.nextToken();
                    Object varObj = evaluator.eval(ctx, varName);
                    if (varObj == null) {
                        //considered as a warning here
                        exctx.getErrorReporter().onError(ErrorConstants.UNDEFINED_VARIABLE,
                                varName + " = null", parentState);
                    }
                    if (xpathEvaluator && varName.startsWith("$")) {
                        varName = varName.substring(1);
                    }
                    addToPayload(varName, varObj, payload);
                }
            }
            finally {
                ctx.setLocal(getNamespacesKey(), null);
            }
        }
    }
}
