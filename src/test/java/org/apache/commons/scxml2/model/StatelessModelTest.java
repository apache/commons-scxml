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
package org.apache.commons.scxml2.model;

import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.SimpleSCXMLListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class    StatelessModelTest {

    // Test data
    private URL stateless01jexl, stateless01par;
    private SCXML scxml01jexl, scxml01par, scxml02par;
    private SCXMLExecutor exec01, exec02;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() throws Exception {
        stateless01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/stateless-01.xml");
        stateless01par = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/model/stateless-parallel-01.xml");
        scxml01jexl = SCXMLTestHelper.parse(stateless01jexl);
        scxml01par = SCXMLTestHelper.parse(stateless01par);
        scxml02par = SCXMLTestHelper.parse(stateless01par);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        stateless01jexl = null;
    }

    /**
     * Test the stateless model, simultaneous executions, JEXL expressions
     */    
    @Test
    public void testStatelessModelSimultaneousJexl() throws Exception {
    	// parse once, use many times
        exec01 = SCXMLTestHelper.getExecutor(scxml01jexl);
        Assert.assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(scxml01jexl);
        Assert.assertNotNull(exec02);
        Assert.assertFalse(exec01 == exec02);
        runSimultaneousTest();
    }

    /**
     * Test the stateless model, sequential executions, JEXL expressions
     */    
    @Test
    public void testStatelessModelSequentialJexl() throws Exception {
        // rinse and repeat
        for (int i = 0; i < 3; i++) {
            exec01 = SCXMLTestHelper.getExecutor(scxml01jexl);
            Assert.assertNotNull(exec01);
            runSequentialTest();
        }
    }

    /**
     * Test sharing a single SCXML object between two executors
     */    
    @Test
    public void testStatelessModelParallelSharedSCXML() throws Exception {
        exec01 = SCXMLTestHelper.getExecutor(scxml01par);
        Assert.assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(scxml01par);
        Assert.assertNotNull(exec02);
        Assert.assertFalse(exec01 == exec02);

        Set<EnterableState> currentStates = exec01.getCurrentStatus().getStates();
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
    @Test
    public void testStatelessModelParallelSwapSCXML() throws Exception {
        exec01 = SCXMLTestHelper.getExecutor(scxml01par);
        Assert.assertNotNull(exec01);
        Assert.assertTrue(scxml01par != scxml02par);

        Set<EnterableState> currentStates = exec01.getCurrentStatus().getStates();
        checkParallelStates(currentStates, "state1.init", "state2.init", "exec01");

        currentStates = fireEvent("state1.event", exec01);
        checkParallelStates(currentStates, "state1.final", "state2.init", "exec01");
        exec01.setStateMachine(scxml02par);
        exec01.addListener(scxml02par, new SimpleSCXMLListener());

        currentStates = fireEvent("state2.event", exec01);
        checkParallelStates(currentStates, "next", null, "exec01");
    }
     */

    private void checkParallelStates(Set<EnterableState> currentStates,
            String s1, String s2, String label) {
        Iterator<EnterableState> i = currentStates.iterator();
        Assert.assertTrue("Not enough states", i.hasNext());
        String cs1 = i.next().getId();
        String cs2;
        if (s2 != null) {
            Assert.assertTrue("Not enough states, found one state: " + cs1, i.hasNext());
            cs2 = i.next().getId();
            Assert.assertFalse("Too many states", i.hasNext());
            if (s2.equals(cs2)) {
                cs2 = null;
            } else if (s1.equals(cs2)) {
                cs2 = null;
            } else {
                Assert.fail(label + " in unexpected state " + cs2);
            }
        } else {
            Assert.assertFalse("Too many states", i.hasNext());
        }
        if (s1 != null && s1.equals(cs1)) {
            return;
        }
        if (s2 != null && s2.equals(cs1)) {
            return;
        }
        Assert.fail(label + " in unexpected state " + cs1);
    }

    private void runSimultaneousTest() throws Exception {
        //// Interleaved
        // exec01
        Set<EnterableState> currentStates = exec01.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = fireEvent("ten.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        // exec02
        currentStates = exec02.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("twenty.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
        // exec02
        currentStates = fireEvent("ten.done", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        currentStates = fireEvent("twenty.done", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
    }

    private void runSequentialTest() throws Exception {
        Set<EnterableState> currentStates = exec01.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", (currentStates.iterator().
            next()).getId());
        currentStates = fireEvent("ten.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", (currentStates.iterator().
            next()).getId());
        currentStates = fireEvent("twenty.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", (currentStates.iterator().
            next()).getId());
    }

    private Set<EnterableState> fireEvent(String name, SCXMLExecutor exec) throws Exception {
        TriggerEvent[] evts = {new TriggerEvent(name, TriggerEvent.SIGNAL_EVENT, null)};
        exec.triggerEvents(evts);
        return exec.getCurrentStatus().getStates();
    }
}

