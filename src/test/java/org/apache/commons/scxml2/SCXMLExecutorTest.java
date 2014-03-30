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

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.env.groovy.GroovyContext;
import org.apache.commons.scxml2.env.groovy.GroovyEvaluator;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class SCXMLExecutorTest {

    // Test data
    private URL microwave01jexl, microwave02jexl, microwave03jexl, microwave04jexl, microwave05jexl,
            microwave01grv, microwave02grv, microwave03grv, microwave04grv, microwave05grv, transitions01,
        transitions02, transitions03, transitions04, transitions05, transitions06, prefix01, send01, send02,
        transitionsWithCond01, transitionsEventVar;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        microwave01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-01.xml");
        microwave02jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-02.xml");
        microwave03jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-03.xml");
        microwave04jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-04.xml");
        microwave05jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-05.xml");
        microwave01grv = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/env/groovy/microwave-01.xml");
        microwave02grv = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/env/groovy/microwave-02.xml");
        microwave03grv = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/env/groovy/microwave-03.xml");
        microwave04grv = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/env/groovy/microwave-04.xml");
        microwave05grv = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/env/groovy/microwave-05.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-01.xml");
        transitions02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-02.xml");
        transitions03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-03.xml");
        transitions04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-04.xml");
        transitions05 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-05.xml");
        transitions06 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-06.xml");
        prefix01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/prefix-01.xml");
        send01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/send-01.xml");
        send02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/send-02.xml");
        transitionsWithCond01 = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/transitions-with-cond-01.xml");
        transitionsEventVar = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/transitions-event-variable.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        microwave01jexl = microwave02jexl =
            microwave04jexl = microwave05jexl = transitions01 = transitions02 = transitions03 =
            transitions04 = transitions05 = transitions06 = prefix01 = send01 = send02 = 
            transitionsWithCond01 = transitionsEventVar = null;
    }

    /**
     * Test the implementation
     */
    @Test
    public void testSCXMLExecutorMicrowave01JexlSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave01jexl);
        Assert.assertNotNull(exec);
        checkMicrowave01Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave02JexlSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave02jexl);
        Assert.assertNotNull(exec);
        checkMicrowave02Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave03JexlSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave03jexl);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        checkMicrowave01Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave04JexlSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave04jexl);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        checkMicrowave02Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave05JexlSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave05jexl);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        checkMicrowave02Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave01grvSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave01grv, new GroovyContext(), new GroovyEvaluator());
        Assert.assertNotNull(exec);
        checkMicrowave01Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave02grvSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave02grv, new GroovyContext(), new GroovyEvaluator());
        Assert.assertNotNull(exec);
        checkMicrowave02Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave03grvSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave03grv);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml, new GroovyContext(), new GroovyEvaluator());
        Assert.assertNotNull(exec);
        checkMicrowave01Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave04grvSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave04grv);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml, new GroovyContext(), new GroovyEvaluator());
        Assert.assertNotNull(exec);
        checkMicrowave02Sample();
    }

    @Test
    public void testSCXMLExecutorMicrowave05grvSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave05grv);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml, new GroovyContext(), new GroovyEvaluator());
        Assert.assertNotNull(exec);
        checkMicrowave02Sample();
    }

    @Test
    public void testSCXMLExecutorPrefix01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(prefix01);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorTransitions01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(transitions01);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_one", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "twenty_one.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty_two", currentStates.iterator().next().getId());
        SCXMLTestHelper.fireEvent(exec, "twenty_two.done");
        Assert.assertEquals(3, exec.getCurrentStatus().getStates().size());
    }

    @Test
    public void testSCXMLExecutorTransitions02Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(transitions02);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "ten.stay");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.self");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSCXMLExecutorTransitions03Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(transitions03);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
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
        SCXML scxml = SCXMLTestHelper.parse(transitions04);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
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
        SCXML scxml = SCXMLTestHelper.parse(transitions05);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "end");
    }

    @Test
    public void testSCXMLExecutorTransitions06Sample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(transitions06);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        SCXMLTestHelper.assertPostTriggerStates(exec, "start", new String[]{"one", "two"});
        SCXMLTestHelper.assertPostTriggerState(exec, "onetwo_three", "three");
        SCXMLTestHelper.assertPostTriggerStates(exec, "three_one", new String[]{"one", "two"});
        SCXMLTestHelper.assertPostTriggerState(exec, "two_four", "four");
    }

    @Test
    public void testSend01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(send01);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
    }

    @Test
    public void testSend02TypeSCXMLSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(send02);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ninety", currentStates.iterator().next().getId());
        Assert.assertTrue(exec.getCurrentStatus().isFinal());
    }

    @Test
    public void testSCXMLExecutorTransitionsWithCond01Sample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(transitionsWithCond01);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);

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
        SCXML scxml = SCXMLTestHelper.parse(transitionsEventVar);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("keyed", Boolean.TRUE);
        SCXMLTestHelper.assertPostTriggerState(exec, "open", payload, "opened");
    }

    private void checkMicrowave01Sample() throws Exception {
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("cooking", currentStates.iterator().next().getId());
    }

    private void checkMicrowave02Sample() throws Exception {
        Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assert.assertEquals(2, currentStates.size());
        String id = (currentStates.iterator().next()).getId();
        Assert.assertTrue(id.equals("closed") || id.equals("cooking"));
    }

}

