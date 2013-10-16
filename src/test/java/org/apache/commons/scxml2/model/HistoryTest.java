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
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HistoryTest {

    // Test data
    private History history;
    private URL shallow01, deep01, defaults01, parallel01;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */   
    @Before
    public void setUp() {
        history = new History();
        shallow01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/history-shallow-01.xml");
        deep01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/history-deep-01.xml");
        defaults01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/history-default-01.xml");
        parallel01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/history-parallel-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        history = null;
        shallow01 = deep01 = defaults01 = parallel01 = null;
        exec = null;
    }

    /**
     * Test the implementation
     */    
    @Test
    public void testSetTypeDeep() {
        history.setType("deep");
        
        Assert.assertTrue(history.isDeep());
    }
        
    @Test
    public void testSetTypeNotDeep() {
        history.setType("shallow");
        
        Assert.assertFalse(history.isDeep());
    }
    
    @Test
    public void testShallowHistory01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(shallow01);
        runHistoryFlow();
    }
    
    @Test
    public void testDeepHistory01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(deep01);
        runHistoryFlow();
    }
    
    @Test
    public void testHistoryDefaults01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(defaults01);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state11", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "state.next");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state211", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "state.next");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state31", currentStates.iterator().next().getId());
    }
    
    @Test
    public void testHistoryParallel01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(parallel01);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        SCXMLTestHelper.assertState(exec, "off_call");
        SCXMLTestHelper.assertPostTriggerStates(exec, "dial", new String[] { "talking", "on_call" });
        SCXMLTestHelper.assertPostTriggerStates(exec, "consult", new String[] { "consult_talking", "on_consult" });
        // Next line uses history to go back to on call and talking
        SCXMLTestHelper.assertPostTriggerStates(exec, "alternate", new String[] { "talking", "on_call" });
        // Hold
        SCXMLTestHelper.assertPostTriggerStates(exec, "hold", new String[] { "held", "on_call" });
        SCXMLTestHelper.assertPostTriggerStates(exec, "consult", new String[] { "consult_talking", "on_consult" });
        // Next line uses history to go back to on call and on hold
        SCXMLTestHelper.assertPostTriggerStates(exec, "alternate", new String[] { "held", "on_call" });
    }

    private void runHistoryFlow() throws Exception {
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("phase1", currentStates.iterator().next().getId());
        Assert.assertEquals("phase1", pauseAndResume());
        Assert.assertEquals("phase2", nextPhase());
        // pause and resume couple of times for good measure
        Assert.assertEquals("phase2", pauseAndResume());
        Assert.assertEquals("phase2", pauseAndResume());
        Assert.assertEquals("phase3", nextPhase());
        Assert.assertEquals("phase3", pauseAndResume());
        exec.reset();
        currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("phase1", ((State)currentStates.iterator().
            next()).getId());
    }

    private String pauseAndResume() throws Exception {
        Set<TransitionTarget> currentStates = SCXMLTestHelper.fireEvent(exec, "flow.pause");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("interrupted", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "flow.resume");
        Assert.assertEquals(1, currentStates.size());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        return ((State)currentStates.iterator().next()).getId();
    }

    private String nextPhase() throws Exception {
        Set<TransitionTarget> currentStates = SCXMLTestHelper.fireEvent(exec, "phase.done");
        Assert.assertEquals(1, currentStates.size());
        return ((State)currentStates.iterator().next()).getId();
    }

}
