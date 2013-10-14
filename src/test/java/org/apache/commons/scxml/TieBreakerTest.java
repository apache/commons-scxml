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
package org.apache.commons.scxml;

import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.scxml.model.TransitionTarget;
/**
 * Unit tests for testing conflict resolution amongst multiple transitions
 * within the {@link org.apache.commons.scxml.SCXMLExecutor}'s default
 * semantics.
 *
 * Upto v0.6, non-deterministic behavior leads to an error condition. Based
 * on the February 2007 WD, such non-determinism should now be resolved
 * based on document order and heirarchy of states within the state machine.
 * This class tests various such cases where more than one candidate
 * transition exists at a particular point, and tie-breaking rules are used
 * to make progress, rather than resulting in error conditions.
 */
public class TieBreakerTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public TieBreakerTest(String name) {
        super(name);
    }

    // Test data
    private URL tiebreaker01, tiebreaker02, tiebreaker03, tiebreaker04,
        tiebreaker05, tiebreaker06;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        tiebreaker01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/tie-breaker-01.xml");
        tiebreaker02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/tie-breaker-02.xml");
        tiebreaker03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/tie-breaker-03.xml");
        tiebreaker04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/tie-breaker-04.xml");
        tiebreaker05 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/tie-breaker-05.xml");
        tiebreaker06 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/tie-breaker-06.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        tiebreaker01 = tiebreaker02 = tiebreaker03 = tiebreaker04 =
            tiebreaker05 = tiebreaker06 = null;
    }

    /**
     * Test the implementation
     */
    public void testTieBreaker01() throws Exception {
        exec = SCXMLTestHelper.getExecutor(tiebreaker01);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("twenty", currentStates.iterator().next().getId());
    }

    public void testTieBreaker02() throws Exception {
        exec = SCXMLTestHelper.getExecutor(tiebreaker02);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("eleven", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("thirty", currentStates.iterator().next().getId());
    }

    public void testTieBreaker03() throws Exception {
        exec = SCXMLTestHelper.getExecutor(tiebreaker03);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("eleven", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("forty", currentStates.iterator().next().getId());
    }

    public void testTieBreaker04() throws Exception {
        exec = SCXMLTestHelper.getExecutor(tiebreaker04);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = SCXMLTestHelper.fireEvent(exec, "event_2");
        assertEquals(1, currentStates.size());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event_1");
        assertEquals(1, currentStates.size());
    }

    public void testTieBreaker05() throws Exception {
        exec = SCXMLTestHelper.getExecutor(tiebreaker05);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(3, currentStates.size());
        for (TransitionTarget tt : currentStates) {
            String id = tt.getId();
            assertTrue(id.equals("s11") || id.equals("s212")
                || id.equals("s2111"));
        }
        currentStates = SCXMLTestHelper.fireEvent(exec, "event1");
        assertEquals(3, currentStates.size());
        for (TransitionTarget tt : currentStates) {
            String id = tt.getId();
            assertTrue(id.equals("s12") || id.equals("s212")
                || id.equals("s2112"));
        }
    }

    public void testTieBreaker06() throws Exception {
        exec = SCXMLTestHelper.getExecutor(SCXMLTestHelper.parse(tiebreaker06));
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
    }
}

