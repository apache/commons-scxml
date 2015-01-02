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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.Assert;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class SCXMLExecutorTest {

    /**
     * Test the implementation
     */
    @Test
    public void testSCXMLExecutorMicrowave01JexlSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-01.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave02JexlSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-02.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave03JexlSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-03.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave04JexlSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-04.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave05JexlSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-05.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave01grvSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-01.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave02grvSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-02.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave03grvSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-03.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave04grvSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-04.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave05grvSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-05.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorPrefix01Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/prefix-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorTransitions01Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-01.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_one", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.twenty_one");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_two", currentStates.iterator().next().getId());
        SCXMLTestHelper.fireEvent(exec, "done.state.twenty_two");
        Assert.assertEquals(3, exec.getStatus().getStates().size());
    }

    @Test
    public void testSCXMLExecutorTransitions02Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-02.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "ten.stay");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.self");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorTransitions03Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-03.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(3, currentStates.size());
        Set<String> expected = new HashSet<String>();
        expected.add("twenty_one_2");
        expected.add("twenty_two_2");
        expected.add("twenty_three_2");
        for (TransitionTarget tt : currentStates) {
            if (!expected.remove(tt.getId())) {
                Assert.fail("'" + tt.getId()
                    + "' is not an expected current state ID");
            }
        }
    }

    @Test
    public void testSCXMLExecutorTransitions04Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-04.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(3, currentStates.size());
        Set<String> expected = new HashSet<String>();
        expected.add("twenty_one_1");
        expected.add("twenty_two_1");
        expected.add("twenty_three_1");
        for (TransitionTarget tt : currentStates) {
            if (!expected.remove(tt.getId())) {
                Assert.fail("'" + tt.getId()
                    + "' is not an expected current state ID");
            }
        }
        currentStates = SCXMLTestHelper.fireEvent(exec, "bar");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", (currentStates.iterator().
            next()).getId());
    }

    @Test
    public void testSCXMLExecutorTransitions05Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-05.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "end");
    }

    @Test
    public void testSCXMLExecutorTransitions06Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-06.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerStates(exec, "start", new String[]{"one", "two"});
        SCXMLTestHelper.assertPostTriggerState(exec, "onetwo_three", "three");
        SCXMLTestHelper.assertPostTriggerStates(exec, "three_one", new String[]{"one", "two"});
        SCXMLTestHelper.assertPostTriggerState(exec, "two_four", "four");
    }

    @Test
    public void testSend01Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/send-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSend02TypeSCXMLSample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/send-02.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ninety", currentStates.iterator().next().getId());
        Assert.assertTrue(exec.getStatus().isFinal());
    }

    @Test
    public void testSCXMLExecutorTransitionsWithCond01Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-with-cond-01.xml");
        exec.go();
        Map<String, Object> payload = new HashMap<String, Object>();

        // with _event.data set to true, transition should happen as expected.
        payload.put("keyed", Boolean.TRUE);
        SCXMLTestHelper.assertPostTriggerState(exec, "open", payload, "opened");
        // turn back to closed
        SCXMLTestHelper.assertPostTriggerState(exec, "close", payload, "closed");

        // with _event.data set to false, transition shouldn't happen as expected.
        payload.put("keyed", Boolean.FALSE);
        SCXMLTestHelper.assertPostTriggerState(exec, "open", payload, "closed");

        // with _event.data set to null, transition shouldn't happen as expected.
        payload.clear();
        SCXMLTestHelper.assertPostTriggerState(exec, "open", payload, "closed");

        // with _event.data set to null, transition shouldn't happen as expected.
        SCXMLTestHelper.assertPostTriggerState(exec, "open", null, "closed");

        // transition to locked for testing
        SCXMLTestHelper.assertPostTriggerState(exec, "lock", null, "locked");
        // due to intentional expression syntax error, it catches an exception and so treat the cond as false
        SCXMLTestHelper.assertPostTriggerState(exec, "unlock", null, "locked");
    }

    @Test
    public void testSCXMLExecutorSystemEventVariable() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-event-variable.xml");
        exec.go();
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("keyed", Boolean.TRUE);
        SCXMLTestHelper.assertPostTriggerState(exec, "open", payload, "opened");
    }

    @Test
    public void testSCXMLExecutorSetConfiguration() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-01.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_one", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.twenty_one");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_two", currentStates.iterator().next().getId());
        Set<String> stateIds = new HashSet<String>();
        stateIds.add("twenty_one");
        exec.setConfiguration(stateIds);
        Assert.assertEquals(1, exec.getStatus().getStates().size());
        SCXMLTestHelper.fireEvent(exec, "done.state.twenty_one");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_two", currentStates.iterator().next().getId());
    }

    private void checkMicrowave01Sample(SCXMLExecutor exec) throws Exception {
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("cooking", currentStates.iterator().next().getId());
    }

    private void checkMicrowave02Sample(SCXMLExecutor exec) throws Exception {
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assert.assertEquals(2, currentStates.size());
        String id = (currentStates.iterator().next()).getId();
        Assert.assertTrue(id.equals("closed") || id.equals("cooking"));
    }
}
