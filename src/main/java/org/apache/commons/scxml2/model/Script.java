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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.TriggerEvent;

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
    
    private String body;

    /**
     * Constructor.
     */
    public Script() {
        super();
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
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection<TriggerEvent> derivedEvents)
    throws ModelException, SCXMLExpressionException {
        Context ctx = scInstance.getContext(getParentTransitionTarget());
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Evaluator eval = scInstance.getEvaluator();
        eval.evalScript(ctx, getScript());
        ctx.setLocal(getNamespacesKey(), null);
    }

}

