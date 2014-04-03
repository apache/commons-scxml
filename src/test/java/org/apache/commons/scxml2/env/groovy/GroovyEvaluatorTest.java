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
package org.apache.commons.scxml2.env.groovy;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GroovyEvaluatorTest {

    private String BAD_EXPRESSION = ">";
    private Context ctx;

    @Before
    public void before() {
        ctx = new GroovyContext();
    }

    @Test
    public void testEval() throws SCXMLExpressionException {
        Evaluator eval = new GroovyEvaluator();
        Assert.assertEquals(new Integer(2), eval.eval(ctx, "1 + 1"));
    }

    @Test
    public void testPristine() throws SCXMLExpressionException {
        Evaluator eval = new GroovyEvaluator();
        Assert.assertTrue(eval.evalCond(ctx, "1 + 1 == 2"));
    }

    @Test
    public void testBuiltInFunctions() throws SCXMLExpressionException {
        Evaluator eval = new GroovyEvaluator();

        Set<TransitionTarget> allStates = new HashSet<TransitionTarget>();
        State state1 = new State();
        state1.setId("state1");
        allStates.add(state1);

        ctx.setLocal(SCXMLSystemContext.ALL_STATES_KEY, allStates);

        Assert.assertTrue(eval.evalCond(ctx, "In('state1')"));
    }

    @Test
    public void testScript() throws SCXMLExpressionException {
        Evaluator eval = new GroovyEvaluator();
        ctx.set("x", 3);
        ctx.set("y", 0);
        String script = 
            "if ((x * 2) == 5) {" +
                "y = 1;\n" +
            "} else {\n" +
                "y = 2;\n" +
            "}";
        Assert.assertEquals(2, eval.evalScript(ctx, script));
        Assert.assertEquals(2, ctx.get("y"));
    }

    @Test
    public void testErrorMessage() {
        Evaluator eval = new GroovyEvaluator();
        Assert.assertNotNull(eval);
        try {
            eval.eval(ctx, BAD_EXPRESSION);
            Assert.fail("GroovyEvaluator should throw SCXMLExpressionException");
        } catch (SCXMLExpressionException e) {
            Assert.assertTrue("GroovyEvaluator: Incorrect error message",
                e.getMessage().startsWith("eval('" + BAD_EXPRESSION + "'):"));
        }
    }

    @Test
    public void testPreprocessScript() {
        GroovyEvaluator evaluator = new GroovyEvaluator();
        Assert.assertEquals("x &&  x || x  !  x == x <  x <= x != x >  x >= x", evaluator.getScriptPreProcessor().
                preProcess("x and x or x not x eq x lt x le x ne x gt x ge x"));
        Assert.assertEquals("and x OR x\n ! \nx\n== x < \nx(le)x ne. xgt x ge", evaluator.getScriptPreProcessor().
                 preProcess("and x OR x\nnot\nx\neq x lt\nx(le)x ne. xgt x ge"));
    }
}
