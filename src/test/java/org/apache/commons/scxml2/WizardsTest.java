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

import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.model.EnterableState;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests
 */
public class WizardsTest {

    /**
     * Test the wizard style SCXML documents, and send usage
     */
    @Test
    public void testWizard01Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/wizard-01.xml");
        exec.go();
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state1", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event2");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state2", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event4");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state4", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event3");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state3", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event3"); // ensure we stay put
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state3", currentStates.iterator().next().getId());
    }

    @Test
    public void testWizard02Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/wizard-02.xml");
        exec.setEventdispatcher(new TestEventDispatcher());
        exec.go();
        // If you change this, you must also change
        // the TestEventDispatcher
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state2", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event4");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state4", currentStates.iterator().next().getId());
    }

    static class TestEventDispatcher extends SimpleDispatcher {
        private static final long serialVersionUID = 1L;
        // If you change this, you must also change testWizard02Sample()

        int callback = 0;

        @SuppressWarnings("unchecked")
        public void send(Map<String, SCXMLIOProcessor> ioProcessors, String id, String target, String type,
                String event, Object data, Object hints, long delay) {
            if ("foo".equals(type)) {
                Map<String, Object> params = (Map<String, Object>)data;
                int i = ((Integer) params.get("aValue"));
                switch (callback) {
                    case 0:
                        Assert.assertTrue(i == 2); // state2
                        callback++;
                        break;
                    case 1:
                        Assert.assertTrue(i == 4); // state4
                        callback++;
                        break;
                    default:
                        Assert.fail("More than 2 TestEventDispatcher <send> callbacks for type \"foo\"");
                }
            }
            else {
                super.send(ioProcessors, id, target, type, event, data, hints, delay);
            }
        }
        public void cancel(String sendId) {
            // should never be called
            Assert.fail("<cancel> TestEventDispatcher callback unexpected");
        }
    }
}
