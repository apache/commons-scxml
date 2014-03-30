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

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;script&gt; SCXML element.
 *
 * TODO src attribute support
 */
public class Script extends Action implements BodyContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private boolean globalScript;
    private String body;

    /**
     * Constructor.
     */
    public Script() {
        super();
    }

    public boolean isGlobalScript() {
        return globalScript;
    }

    public void setGlobalScript(final boolean globalScript) {
        this.globalScript = globalScript;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Get the script to execute.
     *
     * @return The script to execute.
     */
    public String getScript() {
        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        Context ctx = isGlobalScript() ? exctx.getScInstance().getGlobalScriptContext() :
                exctx.getScInstance().getContext(getParentEnterableState());
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Evaluator eval = exctx.getEvaluator();
        eval.evalScript(ctx, getScript());
        ctx.setLocal(getNamespacesKey(), null);
    }

}

