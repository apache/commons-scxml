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
package org.apache.commons.scxml2.env.groovy;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.StateConfiguration;
import org.apache.commons.scxml2.Status;
import org.apache.commons.scxml2.model.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroovyEvaluatorTest {

    private static final String BAD_EXPRESSION = ">";
    private Context ctx;

    @BeforeEach
    public void before() {
        ctx = new GroovyContext(new SCXMLSystemContext(new GroovyContext()), null);
    }

    @Test
    public void testBuiltInFunctions() throws SCXMLExpressionException {
        final Evaluator eval = new GroovyEvaluator();
        final StateConfiguration stateConfiguration = new StateConfiguration();
        final Status status = new Status(stateConfiguration);
        ctx.getSystemContext().getPlatformVariables().put(SCXMLSystemContext.STATUS_KEY, status);

        final State state1 = new State();
        state1.setId("state1");
        stateConfiguration.enterState(state1);
        Assertions.assertTrue(eval.evalCond(ctx, "In('state1')"));
    }

    @Test
    public void testErrorMessage() {
        final Evaluator eval = new GroovyEvaluator();
        Assertions.assertNotNull(eval);
        final SCXMLExpressionException e = Assertions.assertThrows(
                SCXMLExpressionException.class,
                () -> eval.eval(ctx, BAD_EXPRESSION),
                "GroovyEvaluator should throw SCXMLExpressionException");
        Assertions.assertTrue(e.getMessage().startsWith("eval('" + BAD_EXPRESSION + "'):"),
                "GroovyEvaluator: Incorrect error message");
    }

    @Test
    public void testEval() throws SCXMLExpressionException {
        final Evaluator eval = new GroovyEvaluator();
        Assertions.assertEquals(2, eval.eval(ctx, "1 + 1"));
    }

    @Test
    public void testPreprocessScript() {
        final GroovyEvaluator evaluator = new GroovyEvaluator();
        Assertions.assertEquals("x &&  x || x  !  x == x <  x <= x != x >  x >= x", evaluator.getScriptPreProcessor().
                preProcess("x and x or x not x eq x lt x le x ne x gt x ge x"));
        Assertions.assertEquals("and x OR x\n ! \nx\n== x < \nx(le)x ne. xgt x ge", evaluator.getScriptPreProcessor().
                 preProcess("and x OR x\nnot\nx\neq x lt\nx(le)x ne. xgt x ge"));
    }

    @Test
    public void testPristine() throws SCXMLExpressionException {
        final Evaluator eval = new GroovyEvaluator();
        Assertions.assertTrue(eval.evalCond(ctx, "1 + 1 == 2"));
    }

    @Test
    public void testScript() throws SCXMLExpressionException {
        final Evaluator eval = new GroovyEvaluator();
        ctx.set("x", 3);
        ctx.set("y", 0);
        final String script =
            "if ((x * 2) == 5) {" +
                "y = 1;\n" +
            "} else {\n" +
                "y = 2;\n" +
            "}";
        Assertions.assertEquals(2, eval.evalScript(ctx, script));
        Assertions.assertEquals(2, ctx.get("y"));
    }
}
