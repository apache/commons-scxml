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

package org.apache.commons.scxml2.env.javascript;

import java.io.StringReader;
import java.util.Map;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test case for the JSEvaluator expression evaluator class.
 *  Includes basic tests for:
 *  <ul>
 *  <li> constructor
 *  <li> simple standard Javascript expressions
 *  <li> Javascript expressions referencing SCXML &lt;var..&gt; variables.
 *  <li> Javascript expressions referencing SCXML data model elements.
 *  <li> Javascript expressions referencing SCXML data model locations.
 *  <li> Javascript functions referencing SCXML context variables.
 *  </ul>
 */

public class JSEvaluatorTest {
    // TEST CONSTANTS

    private static class TestItem {
        private final String expression;
        private final Object result;

        private TestItem(final String expression,final Object result) {
            this.expression = expression;
            this.result     = result;
        }
    }
    private static final String BAD_EXPRESSION = ">";

    private static final String SCRIPT         = "<?xml version='1.0'?>" +
                                                 "<scxml xmlns = 'http://www.w3.org/2005/07/scxml'" +
                                                 "       xmlns:scxml = 'https://commons.apache.org/scxml'" +
                                                 "       datamodel = 'ecmascript'" +
                                                 "       initial = 'start'"  +
                                                 "       version = '1.0'>" +
                                                 "  <datamodel>" +
                                                 "    <data id='forest'>" +
                                                 "      { \"tree\" :" +
                                                 "        { \"branch\" :" +
                                                 "          { \"twig\" : \"leaf\" }" +
                                                 "        }" +
                                                 "      }" +
                                                 "    </data>" +
                                                 "  </datamodel>" +
                                                 "  <state id='start'>" +
                                                 "    <transition target='end'/>" +
                                                 "  </state>" +
                                                 "  <state id='end' final='true'/>" +
                                                 "</scxml>";

    private static final TestItem[] SIMPLE_EXPRESSIONS = {
            new TestItem("'FIB: ' + (1 + 1 + 2 + 3 + 5)", "FIB: 12"),
            new TestItem("1 + 1 + 2 + 3 + 5",            12), // Force comparison using intValue
            new TestItem("1.1 + 1.1 + 2.1 + 3.1 + 5.1",  12.5),
            new TestItem("(1 + 1 + 2 + 3 + 5) == 12",    true),
            new TestItem("(1 + 1 + 2 + 3 + 5) == 13",    false),
    };

    private static final TestItem[] VAR_EXPRESSIONS = {
            new TestItem("'FIB: ' + fibonacci", "FIB: 12"),
            new TestItem("fibonacci * 2",      24.0),
    };

    // TEST VARIABLES

    private static final String FUNCTION = "function factorial(N) {\r\n" +
                                                        "if (N == 1)\r\n"    +
                                                        "   return N;\r\n"   +
                                                        "\r\n"               +
                                                        "return N * factorial(N-1);\r\n" +
                                                "};\r\n" +
                                                "\r\n"   +
                                                "function fact5() {\r\n" +
                                                "         return factorial(FIVE);\r\n" +
                                                "};\r\n" +
                                                "\r\n" +
                                                "fact5()";
    private Context       context;

    // TEST SETUP

    private Evaluator     evaluator;

    // INSTANCE METHOD TESTS

    /**
     * Creates and initializes an SCXML data model in the context.
     *
     */
    @BeforeEach
    public void setUp() throws Exception {
        final SCXMLExecutor fsm = SCXMLTestHelper.getExecutor(SCXMLReader.read(new StringReader(SCRIPT)));
        fsm.go();
        evaluator = fsm.getEvaluator();
        context = fsm.getGlobalContext();
    }

    /**
     * Ensures implementation of JSEvaluator default constructor and test basic
     * expression evaluation.
     *
     */
    @Test
    public void testBasic() throws SCXMLExpressionException {
        final Evaluator evaluator = new JSEvaluator();

        Assertions.assertNotNull(evaluator);
        Assertions.assertTrue((Boolean) evaluator.eval(context, "1+1 == 2"));
    }

    /**
     * Tests evaluation with SCXML data model expressions.
     *
     */
    @Test
    public void testDataModelExpressions() throws Exception {
        Assertions.assertEquals("leaf",
                     evaluator.eval(context,"forest.tree.branch.twig"),
                "Invalid result: " + "forest.tree.branch.twig");
    }

    /**
     * Tests evaluation of SCXML data model locations.
     *
     */
    @Test
    public void testDataModelLocations() throws Exception {
        Assertions.assertTrue(evaluator.eval(context, "forest") instanceof Map,
                "Invalid result: forest instanceof Map");

        Assertions.assertTrue(evaluator.eval(context, "forest.tree.branch.twig") instanceof String,
                "Invalid result: forest.tree.branch.twig instanceof String");
    }

    /**
     * Tests handling of illegal expressions.
     *
     */
    @Test
    public void testIllegalExpresssion() {
        final Evaluator evaluator = new JSEvaluator();

        Assertions.assertNotNull(evaluator);
        final SCXMLExpressionException x= Assertions.assertThrows(
                SCXMLExpressionException.class,
                () ->  evaluator.eval(context,BAD_EXPRESSION),
                "JSEvaluator should throw SCXMLExpressionException");
        Assertions.assertTrue(x.getMessage().startsWith("eval('" + BAD_EXPRESSION + "')"),
                "JSEvaluator: Incorrect error message");
    }

    /**
     * Tests evaluation with invalid SCXML data model expressions.
     *
     */
    @Test
    public void testInvalidDataModelExpressions() {
        Assertions.assertNull(context.get("forestx"));
        Assertions.assertThrows(
                SCXMLExpressionException.class,
                () -> evaluator.eval(context,"forestx.tree.branch.twig"),
                "Evaluated invalid DataModel expression: " + "forestx.tree.branch.twig");
    }

    /**
     * Tests evaluation of invalid SCXML data model locations.
     *
     */
    @Test
    public void testInvalidDataModelLocations() throws Exception {
            Assertions.assertNotNull(context.get("forest"));
            Assertions.assertNull(evaluator.eval(context,"forest.tree.branch.twigx"),
                    "Invalid result: " + "forest.tree.branch.twigx");
    }

    /**
     * Tests evaluation with invalid SCXML context variables.
     *
     */
    @Test
    public void testInvalidVarExpressions() {
        for (final TestItem item: VAR_EXPRESSIONS) {
            Assertions.assertNull(context.get("fibonacci"));
            Assertions.assertThrows(
                    SCXMLExpressionException.class,
                    () -> evaluator.eval(context,item.expression),
                    "Evaluated invalid <var... expression: " + item.expression);
        }
    }

    @Test
    public void testScript() throws SCXMLExpressionException {
        final Evaluator evaluator = new JSEvaluator();
        context.set("x", 3);
        context.set("y", 0);
        final String script =
            "if ((x * 2.0) == 5.0) {" +
                "y = 1.0;\n" +
            "} else {\n" +
                "y = 2.0;\n" +
            "}";
        Assertions.assertEquals(2.0, evaluator.evalScript(context, script));
        Assertions.assertEquals(2.0, context.get("y"));
    }

    /**
     * Tests evaluation of Javascript functions with variables from SCXML context.
     *
     */
    @Test
    public void testScriptFunctions() throws Exception {
        context.set("FIVE", 5);
        Assertions.assertEquals(5,context.get("FIVE"));
        Assertions.assertEquals(120.0, evaluator.eval(context,FUNCTION), "Invalid function result");
    }

    /**
     * Tests evaluation with simple standard expressions.
     *
     */
    @Test
    public void testStandardExpressions() throws Exception {
        for (final TestItem item: SIMPLE_EXPRESSIONS) {
            final Object eval = evaluator.eval(context,item.expression);
            if (item.result instanceof Integer && eval instanceof Number) {
                Assertions.assertEquals(((Integer) item.result).intValue(),
                        ((Number) eval).intValue(),
                        "Invalid result: " + item.expression);
            } else {
                Assertions.assertEquals(item.result,
                        eval,
                        "Invalid result: " + item.expression);
            }
        }
    }


    // INNER CLASSES

    /**
     * Tests evaluation with SCXML context variables.
     *
     */
    @Test
    public void testVarExpressions() throws Exception {
        context.set("fibonacci", 12.0);

        for (final TestItem item: VAR_EXPRESSIONS) {
            Assertions.assertNotNull(context.get("fibonacci"));
            Assertions.assertEquals (12.0,context.get("fibonacci"));
            Assertions.assertEquals(item.result,
                    evaluator.eval(context,item.expression),
                    "Invalid result: " + item.expression);
        }
    }

}

