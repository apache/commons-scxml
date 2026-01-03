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

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for class {@link MinimalEvaluator}.
 *
 * @see MinimalEvaluator
 *
 */
public class MinimalEvaluatorTest{


    @Test
    public void testEvalCond() throws SCXMLExpressionException {
        MinimalEvaluator minimalEvaluator = new MinimalEvaluator();
        SCXMLExecutor sCXMLExecutor = new SCXMLExecutor(minimalEvaluator, new SimpleDispatcher(), new Tracer(), new SCXMLSemanticsImpl());
        SCInstance sCInstance = sCXMLExecutor.detachInstance();
        Context context = sCInstance.getSystemContext();


        assertFalse(minimalEvaluator.evalCond(minimalEvaluator.newContext(context), "In("));
    }


    @Test
    public void testEvalCondWithNull() throws SCXMLExpressionException {
        MinimalEvaluator minimalEvaluator = new MinimalEvaluator();
        SCXMLExecutor sCXMLExecutor = new SCXMLExecutor(minimalEvaluator, new SimpleDispatcher(), new Tracer(), new SCXMLSemanticsImpl());
        SCInstance sCInstance = sCXMLExecutor.detachInstance();
        Context context = sCInstance.getSystemContext();


        assertFalse(minimalEvaluator.evalCond(minimalEvaluator.newContext(context), null));
    }


}