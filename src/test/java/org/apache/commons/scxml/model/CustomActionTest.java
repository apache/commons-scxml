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
package org.apache.commons.scxml.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.env.jsp.ELEvaluator;

public class CustomActionTest extends TestCase {

    public CustomActionTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(CustomActionTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { CustomActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    private URL hello01, custom01, external01, override01, payload01, payload02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        hello01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/hello-world.xml");
        custom01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/custom-hello-world-01.xml");
        external01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/external-hello-world.xml");
        override01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/custom-hello-world-03.xml");
        payload01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/custom-hello-world-04-jexl.xml");
        payload02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/custom-hello-world-04-el.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        hello01 = custom01 = external01 = payload01 = payload02 = null;
        exec = null;
    }

    public void testAddGoodCustomAction01() throws Exception {
        new CustomAction("http://my.actions.domain/CUSTOM", "hello",
            Hello.class);
    }

    public void testAddBadCustomAction01() {
        try {
            new CustomAction(null, "hello", Hello.class);
            fail("Added custom action with illegal namespace");
        } catch (IllegalArgumentException iae) {
            // Expected
        }
    }

    public void testAddBadCustomAction02() {
        try {
            new CustomAction("  ", "hello", Hello.class);
            fail("Added custom action with illegal namespace");
        } catch (IllegalArgumentException iae) {
            // Expected
        }
    }

    public void testAddBadCustomAction03() {
        try {
            new CustomAction("http://my.actions.domain/CUSTOM", "",
                Hello.class);
            fail("Added custom action with illegal local name");
        } catch (IllegalArgumentException iae) {
            // Expected
        }
    }

    public void testAddBadCustomAction04() {
        try {
            new CustomAction("http://my.actions.domain/CUSTOM", "  ",
                Hello.class);
            fail("Added custom action with illegal local name");
        } catch (IllegalArgumentException iae) {
            // Expected
        }
    }

    public void testAddBadCustomAction05() {
        try {            
            new CustomAction("http://www.w3.org/2005/07/scxml", "foo",
                Hello.class);
            fail("Added custom action in the SCXML namespace");
        } catch (IllegalArgumentException iae) {
            // Expected
        }
    }

    // Hello World example using the SCXML <log> action
    public void testHelloWorld() {
        // (1) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(hello01);
        // (2) Single, final state
        assertEquals("hello", ((State) exec.getCurrentStatus().getStates().
                iterator().next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

    // Hello World example using a custom <hello> action
    public void testCustomActionHelloWorld() {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        CustomAction ca1 =
            new CustomAction("http://my.custom-actions.domain/CUSTOM1",
                             "hello", Hello.class);
        // Register the same action under a different name, just to test
        // multiple custom actions
        CustomAction ca2 =
            new CustomAction("http://my.custom-actions.domain/CUSTOM2",
                             "bar", Hello.class);
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.add(ca1);
        customActions.add(ca2);
        // (2) Parse the document with a custom digester.
        SCXML scxml = SCXMLTestHelper.parse(custom01, customActions);
        // (3) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(scxml);
        // (4) Single, final state
        assertEquals("custom", ((State) exec.getCurrentStatus().getStates().
                iterator().next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

    // Hello World example using custom <my:hello> action
    // as part of an external state source (src attribute)
    public void testCustomActionExternalSrcHelloWorld() {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "hello", Hello.class);
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.add(ca);
        // (2) Parse the document with a custom digester.
        SCXML scxml = SCXMLTestHelper.parse(external01, customActions);
        // (3) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(scxml);
        // (4) Single, final state
        assertEquals("custom", ((State) exec.getCurrentStatus().getStates().
            iterator().next()).getId());
    }

    // Hello World example using custom <my:send> action
    // (overriding SCXML local name "send")
    public void testCustomActionOverrideLocalName() {
        // (1) List of custom actions, use same local name as SCXML action
        CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "send", Hello.class);
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.add(ca);
        // (2) Parse the document with a custom digester.
        SCXML scxml = SCXMLTestHelper.parse(override01, customActions);
        // (3) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(scxml);
        // (4) Single, final state
        assertEquals("custom", ((State) exec.getCurrentStatus().getStates().
            iterator().next()).getId());
    }

    // The custom action defined by Hello.class should be called
    // to execute() exactly 5 times upto this point
    public void testCustomActionCallbacks() {
        assertEquals(5, Hello.callbacks);
    }

    // Hello World example using custom <my:hello> action that generates an
    // event which has the payload examined with JEXL expressions
    public void testCustomActionEventPayloadHelloWorldJexl() {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "hello", Hello.class);
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.add(ca);
        // (2) Parse the document with a custom digester.
        SCXML scxml = SCXMLTestHelper.parse(payload01, customActions);
        // (3) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(scxml);
        // (4) Single, final state
        assertEquals("Invalid intermediate state",
                     "custom1", ((State) exec.getCurrentStatus().getStates().
                                iterator().next()).getId());
        // (5) Verify datamodel variable is correct
        assertEquals("Missing helloName1 in root context", "custom04a",
                     (String) exec.getRootContext().get("helloName1"));
        // (6) Check use of payload in non-initial state
        SCXMLTestHelper.fireEvent(exec, "custom.next");
        // (7) Verify correct end state
        assertEquals("Missing helloName1 in root context", "custom04b",
                (String) exec.getRootContext().get("helloName1"));
        assertEquals("Invalid final state",
                "end", ((State) exec.getCurrentStatus().getStates().
                iterator().next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

    // Hello World example using custom <my:hello> action that generates an
    // event which has the payload examined with EL expressions
    public void testCustomActionEventPayloadHelloWorldEL() {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "hello", Hello.class);
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.add(ca);
        // (2) Parse the document with a custom digester.
        SCXML scxml = SCXMLTestHelper.parse(payload02, customActions);
        // (3) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(new ELEvaluator(), scxml);
        // (4) Single, final state
        assertEquals("Invalid final state",
                     "custom", ((State) exec.getCurrentStatus().getStates().
                                iterator().next()).getId());
        // (5) Verify datamodel variable is correct
        assertEquals("Missing helloName1 in root context", "custom04",
                     (String) exec.getRootContext().get("helloName1"));
    }

}

