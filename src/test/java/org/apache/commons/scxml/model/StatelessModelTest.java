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
import java.util.Iterator;
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
    private URL stateless01jexl, stateless01jsp, stateless01par;
    private SCXML scxml01jexl, scxml01jsp, scxml01par, scxml02par;
    private SCXMLExecutor exec01, exec02;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        stateless01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/stateless-01.xml");
        stateless01jsp = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/stateless-01.xml");
        stateless01par = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/stateless-parallel-01.xml");
        scxml01jexl = SCXMLTestHelper.digest(stateless01jexl);
        scxml01jsp = SCXMLTestHelper.digest(stateless01jsp);
        scxml01par = SCXMLTestHelper.digest(stateless01par);
        scxml02par = SCXMLTestHelper.digest(stateless01par);
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

    /**
     * Test sharing a single SCXML object between two executors
     */
    public void testStatelessModelParallelSharedSCXML() {
        exec01 = SCXMLTestHelper.getExecutor(scxml01par);
        assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(scxml01par);
        assertNotNull(exec02);
        assertFalse(exec01 == exec02);

        Set<TransitionTarget> currentStates = exec01.getCurrentStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec01");

        currentStates = exec02.getCurrentStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec02");

        currentStates = fireEvent("state1.event", exec01);
        checkParallelStates(currentStates, "state1.final", "state2.init", "exec01");

        currentStates = fireEvent("state2.event", exec02);
        checkParallelStates(currentStates, "state1.init", "state2.final", "exec02");

        currentStates = fireEvent("state2.event", exec01);
        checkParallelStates(currentStates, "next", null, "exec01");

        currentStates = fireEvent("state1.event", exec02);
        checkParallelStates(currentStates, "next", null, "exec02");
    }

    /**
     * Test sharing two SCXML objects between one executor (not recommended)
     */
    public void testStatelessModelParallelSwapSCXML() {
        exec01 = SCXMLTestHelper.getExecutor(scxml01par);
        assertNotNull(exec01);
        assertTrue(scxml01par != scxml02par);

        Set<TransitionTarget> currentStates = exec01.getCurrentStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec01");

        currentStates = fireEvent("state1.event", exec01);
        checkParallelStates(currentStates, "state1.final", "state2.init", "exec01");
        exec01.setStateMachine(scxml02par);

        currentStates = fireEvent("state2.event", exec01);
        checkParallelStates(currentStates, "next", null, "exec01");
    }

    private void checkParallelStates(Set<TransitionTarget> currentStates,
            String s1, String s2, String label) {
        Iterator<TransitionTarget> i = currentStates.iterator();
        assertTrue("Not enough states", i.hasNext());
        String cs1 = i.next().getId();
        String cs2 = null;
        if (s2 != null) {
            assertTrue("Not enough states, found one state: " + cs1, i.hasNext());
            cs2 = i.next().getId();
            assertFalse("Too many states", i.hasNext());
            if (s2.equals(cs2)) {
                cs2 = null;
            } else if (s1.equals(cs2)) {
                cs2 = null;
            } else {
                fail(label + " in unexpected state " + cs2);
            }
        } else {
            assertFalse("Too many states", i.hasNext());
        }
        if (s1 != null && s1.equals(cs1)) {
            return;
        }
        if (s2 != null && s2.equals(cs1)) {
            return;
        }
        fail(label + " in unexpected state " + cs1);
    }

    private void runSimultaneousTest() {
        try {
            //// Interleaved
            // exec01
            Set<TransitionTarget> currentStates = exec01.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ten", currentStates.iterator().next().getId());
            currentStates = fireEvent("ten.done", exec01);
            assertEquals(1, currentStates.size());
            assertEquals("twenty", currentStates.iterator().next().getId());
            // exec02
            currentStates = exec02.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ten", currentStates.iterator().next().getId());
            // exec01
            currentStates = fireEvent("twenty.done", exec01);
            assertEquals(1, currentStates.size());
            assertEquals("thirty", currentStates.iterator().next().getId());
            // exec02
            currentStates = fireEvent("ten.done", exec02);
            assertEquals(1, currentStates.size());
            assertEquals("twenty", currentStates.iterator().next().getId());
            currentStates = fireEvent("twenty.done", exec02);
            assertEquals(1, currentStates.size());
            assertEquals("thirty", currentStates.iterator().next().getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void runSequentialTest() {
        try {
            Set<TransitionTarget> currentStates = exec01.getCurrentStatus().getStates();
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

    private Set<TransitionTarget> fireEvent(String name, SCXMLExecutor exec) {
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

