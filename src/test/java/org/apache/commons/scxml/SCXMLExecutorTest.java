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

import org.apache.commons.scxml.env.SimpleContext;
import org.apache.commons.scxml.env.jsp.ELEvaluator;
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
    private URL microwave01jsp, microwave02jsp, microwave01jexl,
        microwave02jexl, transitions01, send02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave01jsp = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-01.xml");
        microwave02jsp = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-02.xml");
        microwave01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-01.xml");
        microwave02jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-02.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-01.xml");
        send02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/send-02.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01jsp = microwave02jsp = microwave01jexl = microwave02jexl =
            transitions01 = send02 = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLExecutorMicrowave01JspSample() {
        exec = SCXMLTestHelper.getExecutor(microwave01jsp,
            new SimpleContext(), new ELEvaluator());
        assertNotNull(exec);
        checkMicrowave01Sample();
    }

    public void testSCXMLExecutorMicrowave02JspSample() {
        exec = SCXMLTestHelper.getExecutor(microwave02jsp,
            new SimpleContext(), new ELEvaluator());
        assertNotNull(exec);
        checkMicrowave02Sample();
    }

    public void testSCXMLExecutorMicrowave01JexlSample() {
        exec = SCXMLTestHelper.getExecutor(microwave01jexl);
        assertNotNull(exec);
        checkMicrowave01Sample();
    }

    public void testSCXMLExecutorMicrowave02JexlSample() {
        exec = SCXMLTestHelper.getExecutor(microwave02jexl);
        assertNotNull(exec);
        checkMicrowave02Sample();
    }

    public void testSCXMLExecutorTransitions01Sample() {
        exec = SCXMLTestHelper.getExecutor(transitions01);
        assertNotNull(exec);
        try {
            Set currentStates = fireEvent("ten.done");
            assertEquals(1, currentStates.size());
            assertEquals("twenty_one", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("twenty_one.done");
            assertEquals(1, currentStates.size());
            assertEquals("twenty_two", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("twenty_two.done");
            assertEquals(3, exec.getCurrentStatus().getStates().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testSendTargettypeSCXMLSample() {
        exec = SCXMLTestHelper.getExecutor(send02);
        assertNotNull(exec);
        try {
            Set currentStates = exec.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ninety", ((State)currentStates.iterator().
                next()).getId());
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

    private void checkMicrowave01Sample() {
        try {
            Set currentStates = fireEvent("turn_on");
            assertEquals(1, currentStates.size());
            assertEquals("cooking", ((State)currentStates.iterator().
                next()).getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void checkMicrowave02Sample() {
        try {
            Set currentStates = fireEvent("turn_on");
            assertEquals(2, currentStates.size());
            String id = ((State)currentStates.iterator().next()).getId();
            assertTrue(id.equals("closed") || id.equals("cooking"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}

