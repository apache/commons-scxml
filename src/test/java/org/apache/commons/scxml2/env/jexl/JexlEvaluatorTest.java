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
package org.apache.commons.scxml2.env.jexl;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JexlEvaluatorTest {

    private static final String BAD_EXPRESSION = ">";
    private final Context ctx = new JexlContext();

    @Test
    public void testErrorMessage() {
        final Evaluator eval = new JexlEvaluator();
        Assertions.assertNotNull(eval);
        final SCXMLExpressionException e = Assertions.assertThrows(
                SCXMLExpressionException.class,
                () -> eval.eval(ctx, BAD_EXPRESSION),
                "JexlEvaluator should throw SCXMLExpressionException");
        Assertions.assertTrue(e.getMessage().startsWith("eval('" + BAD_EXPRESSION + "'):"),
                "JexlEvaluator: Incorrect error message");
    }

    @Test
    public void testPristine() throws SCXMLExpressionException {
        final Evaluator eval = new JexlEvaluator();
        Assertions.assertTrue((Boolean) eval.eval(ctx, "1+1 eq 2"));
    }

    @Test
    public void testScript() throws SCXMLExpressionException {
        final Evaluator eval = new JexlEvaluator();
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
