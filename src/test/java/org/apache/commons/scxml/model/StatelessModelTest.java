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
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.jsp.ELContext;
import org.apache.commons.scxml.env.jsp.ELEvaluator;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLExecutor}.
 */
public class StatelessModelTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public StatelessModelTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StatelessModelTest.class);
        suite.setName("SCXML Executor Tests");
        return suite;
    }

    // Test data
    private URL stateless01jexl, stateless01jsp;
    private SCXML scxml01jexl, scxml01jsp;
    private SCXMLExecutor exec01, exec02;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        stateless01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/stateless-01.xml");
        stateless01jsp = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/stateless-01.xml");
        scxml01jexl = SCXMLTestHelper.digest(stateless01jexl);
        scxml01jsp = SCXMLTestHelper.digest(stateless01jsp);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        stateless01jexl = null;
    }

    /**
     * Test the stateless model, simultaneous executions, JEXL expressions
     */
    public void testStatelessModelSimultaneousJexl() {
    	// parse once, use many times
        exec01 = SCXMLTestHelper.getExecutor(scxml01jexl);
        assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(scxml01jexl);
        assertNotNull(exec02);
        assertFalse(exec01 == exec02);
        runSimultaneousTest();
    }

    /**
     * Test the stateless model, sequential executions, JEXL expressions
     */
    public void testStatelessModelSequentialJexl() {
        // rinse and repeat
        for (int i = 0; i < 3; i++) {
            exec01 = SCXMLTestHelper.getExecutor(scxml01jexl);
            assertNotNull(exec01);
            runSequentialTest();
        }
    }

    /**
     * Test the stateless model, simultaneous executions, EL expressions
     */
    public void testStatelessModelSimultaneousEl() {
    	// parse once, use many times
        exec01 = SCXMLTestHelper.getExecutor(scxml01jsp,
            new ELContext(), new ELEvaluator());
        assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(scxml01jsp,
            new ELContext(), new ELEvaluator());
        assertNotNull(exec02);
        assertFalse(exec01 == exec02);
        runSimultaneousTest();
    }

    /**
     * Test the stateless model, sequential executions, EL expressions
     */
    public void testStatelessModelSequentialEl() {
        // rinse and repeat
        for (int i = 0; i < 3; i++) {
            exec01 = SCXMLTestHelper.getExecutor(scxml01jsp,
                new ELContext(), new ELEvaluator());
            assertNotNull(exec01);
            runSequentialTest();
        }
    }

    private void runSimultaneousTest() {
        try {
            //// Interleaved
            // exec01
            Set currentStates = exec01.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ten", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("ten.done", exec01);
            assertEquals(1, currentStates.size());
            assertEquals("twenty", ((State)currentStates.iterator().
                next()).getId());
            // exec02
            currentStates = exec02.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ten", ((State)currentStates.iterator().
                next()).getId());
            // exec01
            currentStates = fireEvent("twenty.done", exec01);
            assertEquals(1, currentStates.size());
            assertEquals("thirty", ((State)currentStates.iterator().
                next()).getId());
            // exec02
            currentStates = fireEvent("ten.done", exec02);
            assertEquals(1, currentStates.size());
            assertEquals("twenty", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("twenty.done", exec02);
            assertEquals(1, currentStates.size());
            assertEquals("thirty", ((State)currentStates.iterator().
                next()).getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void runSequentialTest() {
        try {
            Set currentStates = exec01.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ten", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("ten.done", exec01);
            assertEquals(1, currentStates.size());
            assertEquals("twenty", ((State)currentStates.iterator().
                next()).getId());
            currentStates = fireEvent("twenty.done", exec01);
            assertEquals(1, currentStates.size());
            assertEquals("thirty", ((State)currentStates.iterator().
                next()).getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }    	
    }

    private Set fireEvent(String name, SCXMLExecutor exec) {
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

