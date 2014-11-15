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
package org.apache.commons.scxml2.env.minimal;

import java.io.Serializable;

import org.apache.commons.scxml2.Builtin;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.model.SCXML;

/**
 * Minimal Evaluator implementing and providing support for the SCXML Null Data Model.
 * <p>
 * The SCXML Null Data Model only supports the SCXML "In(stateId)" builtin function.
 * </p>
 */
public class MinimalEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    public static final String SUPPORTED_DATA_MODEL = Evaluator.NULL_DATA_MODEL;

    public static class MinimalEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATA_MODEL;
        }

        @Override
        public Evaluator getEvaluator() {
            return new MinimalEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new MinimalEvaluator();
        }
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    @Override
    public Object eval(final Context ctx, final String expr) throws SCXMLExpressionException {
        return expr;
    }

    @Override
    public Boolean evalCond(final Context ctx, final String expr) throws SCXMLExpressionException {
        // only support the "In(stateId)" predicate
        String predicate = expr != null ? expr.trim() : "";
        if (predicate.startsWith("In(") && predicate.endsWith(")")) {
            String stateId = predicate.substring(3, predicate.length()-1);
            return Builtin.isMember(ctx, stateId);
        }
        return false;
    }

    @Override
    public Object evalLocation(final Context ctx, final String expr) throws SCXMLExpressionException {
        return expr;
    }

    @Override
    public void evalAssign(final Context ctx, final String location, final Object data, final AssignType type, final String attr) throws SCXMLExpressionException {
        throw new UnsupportedOperationException("Assign expressions are not supported by the \"null\" datamodel");
    }

    @Override
    public Object evalScript(final Context ctx, final String script) throws SCXMLExpressionException {
        throw new UnsupportedOperationException("Scripts are not supported by the \"null\" datamodel");
    }

    @Override
    public Context newContext(final Context parent) {
        return parent instanceof MinimalContext ? parent : new MinimalContext(parent);
    }
}
