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

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.SCXML;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/** JUnit 3 test case for the JSEvaluator expression evaluator
 *  class. Includes basic tests for:
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

    private static final String BAD_EXPRESSION = ">";
    private static final String SCRIPT         = "<?xml version='1.0'?>" +
                                                 "<scxml xmlns        = 'http://www.w3.org/2005/07/scxml' " +
                                                        "xmlns:scxml  = 'http://commons.apache.org/scxml' " +
                                                        "initial = 'start' "  +
                                                        "version      = '1.0'>" +
                                                  "<datamodel>"           +
                                                  "<data id='forest'>"  +
                                                   "<tree xmlns=''>"      +
                                                   "<branch>"             +
                                                   "<twig>leaf</twig>"    +
                                                   "</branch>"            +
                                                   "</tree>"              +
                                                  "</data>"               +
                                                  "</datamodel>"          +
                                                  "<state id='start'>"              +
                                                  "<transition target='end' />"     +
                                                  "</state>"                        +
                                                  "<state id='end' final='true' />" +
                                                  "</scxml>";

    private static final TestItem[] SIMPLE_EXPRESSIONS = {
            new TestItem("'FIB: ' + (1 + 1 + 2 + 3 + 5)",new String("FIB: 12")),
            new TestItem("1 + 1 + 2 + 3 + 5",            new Double(12)),
            new TestItem("(1 + 1 + 2 + 3 + 5) == 12",    new Boolean(true)),
            new TestItem("(1 + 1 + 2 + 3 + 5) == 13",    new Boolean(false)),
    };

    private static final TestItem[] VAR_EXPRESSIONS = {
            new TestItem("'FIB: ' + fibonacci",new String("FIB: 12")),
            new TestItem("fibonacci * 2",      new Double(24)),
    };

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

    // TEST VARIABLES

    private SCXML         scxml;
    private Context       context;
    private Evaluator     evaluator;
    private SCXMLExecutor fsm;

    // TEST SETUP

    /**
     * Creates and initialises an SCXML data model in the context.
     *
     */
    @Before
    public void setUp() throws Exception {
            StringReader reader = new StringReader(SCRIPT);

            scxml     = SCXMLReader.read(reader);
            context   = new JSContext();
            evaluator = new JSEvaluator();
            fsm       = new SCXMLExecutor();

            context.set("_ALL_NAMESPACES",null);

            fsm.setEvaluator   (evaluator);
            fsm.setRootContext (context);
            fsm.setStateMachine(scxml);
            fsm.reset          ();
    }

    // CLASS METHODS

    /**
     * Standalone test runtime.
     *
     */
    public static void main(String args[]) {
        String[] testCaseName = {JSEvaluatorTest.class.getName()};

        junit.textui.TestRunner.main(testCaseName);
    }

    // INSTANCE METHOD TESTS

    /**
     * Ensures implementation of JSEvaluator default constructor and test basic
     * expression evaluation.
     *
     */    
    @Test
    public void testBasic() throws SCXMLExpressionException {
        Evaluator evaluator = new JSEvaluator();

        Assert.assertNotNull(evaluator);
        Assert.assertTrue   (((Boolean) evaluator.eval(context, "1+1 == 2")).booleanValue());
    }

    /**
     * Tests handling of illegal expressions.
     *
     */    
    @Test
    public void testIllegalExpresssion() {
        Evaluator evaluator = new JSEvaluator();

        Assert.assertNotNull(evaluator);

        try {
            evaluator.eval(context,BAD_EXPRESSION);
            Assert.fail          ("JSEvaluator should throw SCXMLExpressionException");

        } catch (SCXMLExpressionException x) {
            Assert.assertTrue("JSEvaluator: Incorrect error message",
                       x.getMessage().startsWith("Error evaluating ['" + BAD_EXPRESSION + "']"));
        }
    }

    /**
     * Tests evaluation with simple standard expressions.
     *
     */    
    @Test
    public void testStandardExpressions() throws Exception {
        for (TestItem item: SIMPLE_EXPRESSIONS) {
            Object eval = evaluator.eval(context,item.expression);
            System.out.println("testStandardExpressions:"+eval.getClass().getCanonicalName()+"="+eval.toString());
            Assert.assertEquals("Invalid result: " + item.expression,
                         item.result,
                         eval);
        }
    }

    /**
     * Tests evaluation with SCXML context variables.
     *
     */    
    @Test
    public void testVarExpressions() throws Exception {
        context.set("fibonacci",Integer.valueOf(12));

        for (TestItem item: VAR_EXPRESSIONS) {
            Assert.assertNotNull(context.get("fibonacci"));
            Assert.assertEquals (Integer.valueOf(12),context.get("fibonacci"));
            Assert.assertEquals ("Invalid result: " + item.expression,
                          item.result,
                          evaluator.eval(context,item.expression));
        }
    }

    /**
     * Tests evaluation with invalid SCXML context variables.
     *
     */    
    @Test
    public void testInvalidVarExpressions() {
        for (TestItem item: VAR_EXPRESSIONS) {
            try {
                Assert.assertNull    (context.get("fibonacci"));
                evaluator.eval(context,item.expression);
                Assert.fail          ("Evaluated invalid <var... expression: " + item.expression);

            } catch (SCXMLExpressionException x) {
                // expected, ignore
            }
        }
    }

    /**
     * Tests evaluation with SCXML data model expressions.
     *
     */    
    @Test
    public void testDataModelExpressions() throws Exception {
        Assert.assertEquals("Invalid result: " + "Data(forest,'tree/branch/twig')",
                     "leaf",
                     evaluator.eval(context,"Data(forest,'tree/branch/twig')"));
    }

    /**
     * Tests evaluation with invalid SCXML data model expressions.
     *
     */    
    @Test
    public void testInvalidDataModelExpressions() {
        Assert.assertNull(context.get("forestx"));

        try {
            evaluator.eval(context,"Data(forestx,'tree/branch/twig')");
            Assert.fail          ("Evaluated invalid Data() expression: " + "Data(forestx,'tree/branch/twig')");

        } catch (SCXMLExpressionException x) {
            // expected, ignore
        }
    }

    /**
     * Tests evaluation of SCXML data model locations.
     *
     */    
    @Test
    public void testDataModelLocations() throws Exception {
            Assert.assertNotNull(context.get("forest"));
            XPath  xpath = XPathFactory.newInstance().newXPath();
            Node   node  = (Node)   context.get("forest");
            Node   twig  = (Node)   xpath.evaluate("tree/branch/twig",        node,XPathConstants.NODE);

            Assert.assertTrue  ("Invalid result: " + "Data(forest,'tree/branch/twig')",
                          evaluator.evalLocation(context,"Data(forest,'tree/branch/twig')") instanceof Element);

            Assert.assertSame ("Incorrect node returned: " + "Data(forest,'tree/branch/twig')",
                         twig,
                         evaluator.evalLocation(context,"Data(forest,'tree/branch/twig')"));
    }

    /**
     * Tests evaluation of invalid SCXML data model locations.
     *
     */    
    @Test
    public void testInvalidDataModelLocations() throws Exception {
            Assert.assertNotNull(context.get("forest"));
            Assert.assertNull("Invalid result: " + "Data(forest,'tree/branch/twigx')",
                       evaluator.evalLocation(context,"Data(forest,'tree/branch/twigx')"));
    }

    /**
     * Tests evaluation of Javascript functions with variables from SCXML context.
     *
     */    
    @Test
    public void testScriptFunctions() throws Exception {
        context.set("FIVE",Integer.valueOf(5));
        Assert.assertEquals(Integer.valueOf(5),context.get("FIVE"));
        Assert.assertEquals("Invalid function result",Double.valueOf(120.0),evaluator.eval(context,FUNCTION));
    }


    // INNER CLASSES

    private static class TestItem {
        private String expression;
        private Object result;

        private TestItem(String expression,Object result) {
            this.expression = expression;
            this.result     = result;
        }
    }

}

