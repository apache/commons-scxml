/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.model;

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HistoryTest {

    private String nextPhase(final SCXMLExecutor exec) throws Exception {
        final Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.phase");
        Assertions.assertEquals(1, currentStates.size());
        return currentStates.iterator().next().getId();
    }

    private String pauseAndResume(SCXMLExecutor exec) throws Exception {
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "flow.pause");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("interrupted", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "flow.resume");
        Assertions.assertEquals(1, currentStates.size());
        SCXMLTestHelper.testInstanceSerializability(exec);
        return currentStates.iterator().next().getId();
    }

    private void runHistoryFlow(final SCXMLExecutor exec) throws Exception {
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("phase1", currentStates.iterator().next().getId());
        Assertions.assertEquals("phase1", pauseAndResume(exec));
        Assertions.assertEquals("phase2", nextPhase(exec));
        // pause and resume couple of times for good measure
        Assertions.assertEquals("phase2", pauseAndResume(exec));
        Assertions.assertEquals("phase2", pauseAndResume(exec));
        Assertions.assertEquals("phase3", nextPhase(exec));
        Assertions.assertEquals("phase3", pauseAndResume(exec));
        exec.reset();
        currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("phase1", currentStates.iterator().next().getId());
    }

    @Test
    void testDeepHistory01() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/history-deep-01.xml");
        exec.go();
        runHistoryFlow(exec);
    }

    @Test
    void testHistoryDefaults01() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/history-default-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state11", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "state.next");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state211", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "state.next");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state31", currentStates.iterator().next().getId());
    }

    @Test
    void testHistoryParallel01() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/history-parallel-01.xml");
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
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

    @Test
    void testSetTypeDeep() {
        final History history = new History();
        history.setType("deep");

        Assertions.assertTrue(history.isDeep());
    }

    @Test
    void testSetTypeNotDeep() {
        final History history = new History();
        history.setType("shallow");

        Assertions.assertFalse(history.isDeep());
    }

    @Test
    void testShallowHistory01() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/history-shallow-01.xml");
        exec.go();
        runHistoryFlow(exec);
    }

}
