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

import com.custom.Payload;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.env.jexl.JexlEvaluatorBuilder;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class SCXMLExecutorTest {

    /**
     * Test the implementation
     */
    @Test
    public void testSCXMLExecutorMicrowave01JexlSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-01.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave02JexlSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-02.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave03JexlSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-03.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave04JexlSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-04.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave05JexlSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/microwave-05.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave01grvSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-01.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave02grvSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-02.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave03grvSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-03.xml");
        exec.go();
        checkMicrowave01Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave04grvSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-04.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorMicrowave05grvSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/microwave-05.xml");
        exec.go();
        checkMicrowave02Sample(exec);
    }

    @Test
    public void testSCXMLExecutorPrefix01Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/prefix-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorTransitions01Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-01.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty_one", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.twenty_one");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty_two", currentStates.iterator().next().getId());
        SCXMLTestHelper.fireEvent(exec, "done.state.twenty_two");
        Assertions.assertEquals(3, exec.getStatus().getStates().size());
    }

    @Test
    public void testSCXMLExecutorTransitions02Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-02.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "ten.stay");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.self");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorTransitions03Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-03.xml");
        exec.go();
        final Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(3, currentStates.size());
        final Set<String> expected = new HashSet<>();
        expected.add("twenty_one_2");
        expected.add("twenty_two_2");
        expected.add("twenty_three_2");
        for (final TransitionTarget tt : currentStates) {
            if (!expected.remove(tt.getId())) {
                Assertions.fail("'" + tt.getId()
                    + "' is not an expected current state ID");
            }
        }
    }

    @Test
    public void testSCXMLExecutorTransitions04Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-04.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(3, currentStates.size());
        final Set<String> expected = new HashSet<>();
        expected.add("twenty_one_1");
        expected.add("twenty_two_1");
        expected.add("twenty_three_1");
        for (final TransitionTarget tt : currentStates) {
            if (!expected.remove(tt.getId())) {
                Assertions.fail("'" + tt.getId()
                    + "' is not an expected current state ID");
            }
        }
        currentStates = SCXMLTestHelper.fireEvent(exec, "bar");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("thirty", (currentStates.iterator().
            next()).getId());
    }

    @Test
    public void testSCXMLExecutorTransitions05Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-05.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "end");
    }

    @Test
    public void testSCXMLExecutorTransitions06Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-06.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerStates(exec, "start", new String[]{"one", "two"});
        SCXMLTestHelper.assertPostTriggerState(exec, "onetwo_three", "three");
        SCXMLTestHelper.assertPostTriggerStates(exec, "three_one", new String[]{"one", "two"});
        SCXMLTestHelper.assertPostTriggerState(exec, "two_four", "four");
    }

    @Test
    public void testSend01Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/send-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSend02TypeSCXMLSample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/send-02.xml");
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ninety", currentStates.iterator().next().getId());
        Assertions.assertTrue(exec.getStatus().isFinal());
    }

    @Test
    public void testSCXMLExecutorTransitionsWithCond01Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-with-cond-01.xml");
        exec.go();
        final Map<String, Object> payload = new HashMap<>();

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
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-event-variable.xml");
        exec.go();
        final Map<String, Object> payload = new HashMap<>();
        payload.put("keyed", Boolean.TRUE);
        SCXMLTestHelper.assertPostTriggerState(exec, "open", payload, "opened");
    }

    @Test
    public void testSCXMLExecutorSetConfiguration() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/transitions-01.xml");
        exec.go();
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.ten");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty_one", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "done.state.twenty_one");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty_two", currentStates.iterator().next().getId());
        final Set<String> stateIds = new HashSet<>();
        stateIds.add("twenty_one");
        exec.setConfiguration(stateIds);
        Assertions.assertEquals(1, exec.getStatus().getStates().size());
        SCXMLTestHelper.fireEvent(exec, "done.state.twenty_one");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty_two", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorFinalDoneData() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/final-donedata.xml");
        Assertions.assertNull(exec.getFinalDoneData());
        exec.go();
        Assertions.assertEquals("done", exec.getFinalDoneData());
    }

    @Test
    public void testSCXMLExecutorWithExternalPayloadObject() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/external-payload.xml");
        final JexlEvaluator evaluator = new JexlEvaluatorBuilder()
                .addAllowedPackage("com.custom")
                .build();

        exec.setEvaluator(evaluator);

        exec.go();
        SCXMLTestHelper.assertPostTriggerState(exec, "done", new Payload(1, "someString"), "end");
        Assertions.assertEquals(1, exec.getGlobalContext().getVars().get("idFromEventPayload"));
    }

    private void checkMicrowave01Sample(final SCXMLExecutor exec) throws Exception {
        final Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("cooking", currentStates.iterator().next().getId());
    }

    private void checkMicrowave02Sample(final SCXMLExecutor exec) throws Exception {
        final Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assertions.assertEquals(2, currentStates.size());
        final String id = (currentStates.iterator().next()).getId();
        Assertions.assertTrue(id.equals("closed") || id.equals("cooking"));
    }
}
