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
package org.apache.commons.scxml2.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CustomActionTest {

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() {
        Hello.callbacks = 0;
    }

    @Test
    public void testAddBadCustomAction01() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomAction(null, "hello", Hello.class),
                "Added custom action with illegal namespace");
    }

    @Test
    public void testAddBadCustomAction02() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomAction("  ", "hello", Hello.class),
                "Added custom action with illegal namespace");
    }

    @Test
    public void testAddBadCustomAction03() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomAction("http://my.actions.domain/CUSTOM", "", Hello.class),
                "Added custom action with illegal local name");
    }

    @Test
    public void testAddBadCustomAction04() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomAction("http://my.actions.domain/CUSTOM", "  ", Hello.class),
                "Added custom action with illegal local name");
    }

    @Test
    public void testAddBadCustomAction05() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new CustomAction("http://www.w3.org/2005/07/scxml", "foo", Hello.class),
                "Added custom action in the SCXML namespace");
    }

    @Test
    public void testAddGoodCustomAction01() {
        new CustomAction("http://my.actions.domain/CUSTOM", "hello",
            Hello.class);
    }

    // Hello World example using custom <my:hello> action that generates an
    // event which has the payload examined with JEXL expressions
    @Test
    public void testCustomActionEventPayloadHelloWorldJexl() throws Exception {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        final CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "hello", Hello.class);
        final List<CustomAction> customActions = new ArrayList<>();
        customActions.add(ca);
        // (2) Parse the document
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/custom-hello-world-04-jexl.xml", customActions);
        // (3) Get a SCXMLExecutor
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        // (4) Single, final state
        Assertions.assertEquals("custom1", exec.getStatus().getStates().iterator().next().getId(),
                "Invalid intermediate state");
        // (5) Verify datamodel variable is correct
        Assertions.assertEquals("custom04a", exec.getGlobalContext().get("helloName1"),
                "Missing helloName1 in root context");

        // The custom action defined by Hello.class should be called
        // to execute() exactly once at this point (by onentry in init state).
        Assertions.assertEquals(1, Hello.callbacks);

        // (6) Check use of payload in non-initial state
        SCXMLTestHelper.fireEvent(exec, "custom.next");
        // (7) Verify correct end state
        Assertions.assertEquals("custom04b", exec.getGlobalContext().get("helloName1"),
                "Missing helloName1 in root context");
        Assertions.assertEquals("end", exec.getStatus().getStates().iterator().next().getId(),
                "Invalid final state");
        Assertions.assertTrue(exec.getStatus().isFinal());

        // The custom action defined by Hello.class should be called
        // to execute() exactly two times at this point (by onentry in custom2 state).
        Assertions.assertEquals(2, Hello.callbacks);
    }

    // Hello World example using custom <my:hello> action
    // as part of an external state source (src attribute)
    @Test
    public void testCustomActionExternalSrcHelloWorld() throws Exception {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        final CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "hello", Hello.class);
        final List<CustomAction> customActions = new ArrayList<>();
        customActions.add(ca);
        // (2) Parse the document
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/external-hello-world.xml", customActions);
        // (3) Get a SCXMLExecutor
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        // (4) Single, final state
        Assertions.assertEquals("custom", exec.getStatus().getStates().
            iterator().next().getId());

        // The custom action defined by Hello.class should be called
        // to execute() exactly twice at this point (one by <my:hello/> and the other by <my:hello/> in external).
        Assertions.assertEquals(2, Hello.callbacks);
    }

    // Hello World example using a custom <hello> action
    @Test
    public void testCustomActionHelloWorld() throws Exception {
        // (1) Form a list of custom actions defined in the SCXML
        //     document (and any included documents via "src" attributes)
        final CustomAction ca1 =
            new CustomAction("http://my.custom-actions.domain/CUSTOM1",
                             "hello", Hello.class);
        // Register the same action under a different name, just to test
        // multiple custom actions
        final CustomAction ca2 =
            new CustomAction("http://my.custom-actions.domain/CUSTOM2",
                             "bar", Hello.class);
        final List<CustomAction> customActions = new ArrayList<>();
        customActions.add(ca1);
        customActions.add(ca2);
        // (2) Parse the document
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/custom-hello-world-01.xml", customActions);
        // (3) Get a SCXMLExecutor
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        // (4) Single, final state
        Assertions.assertEquals("custom", exec.getStatus().getStates().
                iterator().next().getId());
        Assertions.assertTrue(exec.getStatus().isFinal());

        // The custom action defined by Hello.class should be called
        // to execute() exactly twice at this point (one by <my:hello/> and the other by <foo:bar/>).
        Assertions.assertEquals(2, Hello.callbacks);
    }

    // Hello World example using custom <my:send> action
    // (overriding SCXML local name "send")
    @Test
    public void testCustomActionOverrideLocalName() throws Exception {
        // (1) List of custom actions, use same local name as SCXML action
        final CustomAction ca =
            new CustomAction("http://my.custom-actions.domain/CUSTOM",
                             "send", Hello.class);
        final List<CustomAction> customActions = new ArrayList<>();
        customActions.add(ca);
        // (2) Parse the document
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/custom-hello-world-03.xml", customActions);
        // (3) Get a SCXMLExecutor
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        // (4) Single, final state
        Assertions.assertEquals("custom", exec.getStatus().getStates().
            iterator().next().getId());

        // The custom action defined by Hello.class should be called
        // to execute() exactly once at this point (by <my:send/>).
        Assertions.assertEquals(1, Hello.callbacks);
    }

    // Hello World example using the SCXML <log> action
    @Test
    public void testHelloWorld() throws Exception {
        // (1) Get a SCXMLExecutor
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/hello-world.xml");
        exec.go();
        // (2) Single, final state
        Assertions.assertEquals("hello", exec.getStatus().getStates().
                iterator().next().getId());
        Assertions.assertTrue(exec.getStatus().isFinal());
    }
}

