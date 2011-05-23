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

import junit.framework.TestCase;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;

public class HistoryTest extends TestCase {
    /**
     * Construct a new instance of HistoryTest with
     * the specified name
     */
    public HistoryTest(String testName) {
        super(testName);
    }

    // Test data
    private History history;
    private URL shallow01, deep01, defaults01;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */   
    public void setUp() {
        history = new History();
        shallow01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/history-shallow-01.xml");
        deep01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/history-deep-01.xml");
        defaults01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/history-default-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        history = null;
        shallow01 = deep01 = defaults01 = null;
        exec = null;
    }

    /**
     * Test the implementation
     */
    public void testSetTypeDeep() {
        history.setType("deep");
        
        assertTrue(history.isDeep());
    }
    
    public void testSetTypeNotDeep() {
        history.setType("shallow");
        
        assertFalse(history.isDeep());
    }

    public void testShallowHistory01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(shallow01);
        runHistoryFlow();
    }

    public void testDeepHistory01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(deep01);
        runHistoryFlow();
    }

    public void testHistoryDefaults01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(defaults01);
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state11", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "state.next");
        assertEquals(1, currentStates.size());
        assertEquals("state211", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "state.next");
        assertEquals(1, currentStates.size());
        assertEquals("state31", ((State)currentStates.iterator().
            next()).getId());
    }

    private void runHistoryFlow() throws Exception {
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("phase1", ((State)currentStates.iterator().
            next()).getId());
        assertEquals("phase1", pauseAndResume());
        assertEquals("phase2", nextPhase());
        // pause and resume couple of times for good measure
        assertEquals("phase2", pauseAndResume());
        assertEquals("phase2", pauseAndResume());
        assertEquals("phase3", nextPhase());
        assertEquals("phase3", pauseAndResume());
        exec.reset();
        currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("phase1", ((State)currentStates.iterator().
            next()).getId());
    }

    private String pauseAndResume() throws Exception {
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "flow.pause");
        assertEquals(1, currentStates.size());
        assertEquals("interrupted", ((State)currentStates.iterator().
            next()).getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "flow.resume");
        assertEquals(1, currentStates.size());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        return ((State)currentStates.iterator().next()).getId();
    }

    private String nextPhase() throws Exception {
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "phase.done");
        assertEquals(1, currentStates.size());
        return ((State)currentStates.iterator().next()).getId();
    }

}
