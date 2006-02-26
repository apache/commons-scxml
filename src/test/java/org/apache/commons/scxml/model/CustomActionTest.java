/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml.model;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.digester.Digester;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.io.SCXMLDigester;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;

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

    private URL hello01, custom01;
    private Digester digester;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        hello01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/hello-world.xml");
        custom01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/custom-hello-world.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        hello01 = custom01 = null;
        digester = null;
        exec = null;
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
        // (1) Get Digester with "default" rules for parsing SCXML documents
        digester = SCXMLDigester.newInstance(null, null);
        // (2) Register the "custom" action(s)
        SCXMLDigester.addCustomAction(digester,
            "http://my.custom-actions.domain/CUSTOM", "hello", Hello.class);
        // (3) Parse the SCXML document containing the custom action(s)
        SCXML scxml = null;
        try {
            scxml = (SCXML) digester.parse(custom01.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
        // (4) Wire up the object model for the SCXMLExecutor
        SCXMLDigester.updateSCXML(scxml);
        // (5) Get a SCXMLExecutor
        exec = SCXMLTestHelper.getExecutor(scxml);
        // (6) Fire events, proceed as usual
        assertEquals("custom", ((State) exec.getCurrentStatus().getStates().
                iterator().next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

}

