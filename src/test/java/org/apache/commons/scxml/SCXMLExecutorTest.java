/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.commons.scxml;

import java.net.URL;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.model.State;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLExecutor}.
 */
public class SCXMLExecutorTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public SCXMLExecutorTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SCXMLExecutorTest.class);
        suite.setName("SCXML Executor Tests");
        return suite;
    }

    // Test data
    private URL microwave01, microwave02, transitions01;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-01.xml");
        microwave02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-02.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01 = microwave02 = transitions01 = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLExecutorMicrowave01Sample() {
        exec = SCXMLTestHelper.getExecutor(microwave01);
        assertNotNull(exec);
    }

    public void testSCXMLExecutorMicrowave02Sample() {
        exec = SCXMLTestHelper.getExecutor(microwave02);
        assertNotNull(exec);
    }

    public void testSCXMLExecutorTransitions01Sample() {
        exec = SCXMLTestHelper.getExecutor(transitions01);
        assertNotNull(exec);
        try {
            Set currentStates = fireEvent("ten.done");
            assertTrue(currentStates.size() == 1);
            assertEquals("twenty_one", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("twenty_one.done");
            assertTrue(currentStates.size() == 1);
            assertEquals("twenty_two", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("twenty_two.done");
            assertTrue(exec.getCurrentStatus().getStates().size() == 3);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private Set fireEvent(String name) {
        TriggerEvent[] evts = {new TriggerEvent(name,
                TriggerEvent.SIGNAL_EVENT, null)};
        try {
            exec.triggerEvents(evts);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        return exec.getCurrentStatus().getStates();
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}

