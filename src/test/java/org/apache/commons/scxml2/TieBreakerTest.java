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
package org.apache.commons.scxml2;

import java.util.Set;

import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.Assert;
import org.junit.Test;
/**
 * Unit tests for testing conflict resolution amongst multiple transitions
 * within the {@link org.apache.commons.scxml2.SCXMLExecutor}'s default
 * semantics.
 *
 * Upto v0.6, non-deterministic behavior leads to an error condition. Based
 * on the February 2007 WD, such non-determinism should now be resolved
 * based on document order and heirarchy of states within the state machine.
 * This class tests various such cases where more than one candidate
 * transition exists at a particular point, and tie-breaking rules are used
 * to make progress, rather than resulting in error conditions.
 */
public class TieBreakerTest {

    @Test
    public void testTieBreaker01() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/tie-breaker-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testTieBreaker02() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/tie-breaker-02.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("eleven", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
    }

    @Test
    public void testTieBreaker03() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/tie-breaker-03.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("eleven", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("forty", currentStates.iterator().next().getId());
    }

    @Test
    public void testTieBreaker04() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/tie-breaker-04.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "event_2");
        Assert.assertEquals(1, currentStates.size());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event_1");
        Assert.assertEquals(1, currentStates.size());
    }

    @Test
    public void testTieBreaker05() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/tie-breaker-05.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(3, currentStates.size());
        for (TransitionTarget tt : currentStates) {
            String id = tt.getId();
            Assert.assertTrue(id.equals("s11") || id.equals("s212")
                || id.equals("s2111"));
        }
        currentStates = SCXMLTestHelper.fireEvent(exec, "event1");
        Assert.assertEquals(3, currentStates.size());
        for (TransitionTarget tt : currentStates) {
            String id = tt.getId();
            Assert.assertTrue(id.equals("s12") || id.equals("s212")
                || id.equals("s2112"));
        }
    }

    @Test
    public void testTieBreaker06() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/tie-breaker-06.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
    }
}
