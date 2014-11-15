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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;

/**
 * A <code>ParamsContainer</code> represents an element in the SCXML
 * document that may have one or more &lt;param/&gt; children which are used to
 * produce payload for events or external communication.
 */
public abstract class ParamsContainer extends PayloadProvider {

    /**
     * The List of the params to be sent
     */
    private final List<Param> paramsList = new ArrayList<Param>();

    /**
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    public List<Param> getParams() {
        return paramsList;
    }

    /**
     * Adds data to the payload data map based on the {@link Param}s of this {@link ParamsContainer}
     * @param exctx The ActionExecutionContext
     * @param payload the payload data map to be updated
     * @throws ModelException if this action has not an EnterableState as parent
     * @throws SCXMLExpressionException if a malformed or invalid expression is evaluated
     * @see PayloadProvider#addToPayload(String, Object, java.util.Map)
     */
    protected void addParamsToPayload(ActionExecutionContext exctx, Map<String, Object> payload)
            throws ModelException, SCXMLExpressionException {
        if (!paramsList.isEmpty()) {
            EnterableState parentState = getParentEnterableState();
            Context ctx = exctx.getContext(parentState);
            try {
                ctx.setLocal(getNamespacesKey(), getNamespaces());
                Evaluator evaluator = exctx.getEvaluator();
                Object paramValue;
                for (Param p : paramsList) {
                    if (p.getExpr() != null) {
                        paramValue = evaluator.eval(ctx, p.getExpr());
                    }
                    else if (p.getLocation() != null) {
                        paramValue = evaluator.eval(ctx, p.getLocation());
                    }
                    else {
                        // ignore invalid param definition
                        continue;
                    }
                    addToPayload(p.getName(), paramValue, payload);
                }
            }
            finally {
                ctx.setLocal(getNamespacesKey(), null);
            }
        }
    }
}
