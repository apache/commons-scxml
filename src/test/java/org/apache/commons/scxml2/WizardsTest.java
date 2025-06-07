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
package org.apache.commons.scxml2;

import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.model.EnterableState;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests
 */
public class WizardsTest {

    static class TestEventDispatcher extends SimpleDispatcher {

        // If you change this, you must also change testWizard02Sample()
        int callback = 0;

        @Override
        public void cancel(final String sendId) {
            // should never be called
            Assertions.fail("<cancel> TestEventDispatcher callback unexpected");
        }
        @Override
        @SuppressWarnings("unchecked")
        public void send(final Map<String, SCXMLIOProcessor> ioProcessors, final String id, final String target, final String type,
                final String event, final Object data, final Object hints, final long delay) {
            if ("foo".equals(type)) {
                final Map<String, Object> params = (Map<String, Object>)data;
                final int i = (Integer) params.get("aValue");
                switch (callback) {
                    case 0:
                        Assertions.assertEquals(2, i); // state2
                        callback++;
                        break;
                    case 1:
                        Assertions.assertEquals(4, i); // state4
                        callback++;
                        break;
                    default:
                        Assertions.fail("More than 2 TestEventDispatcher <send> callbacks for type \"foo\"");
                }
            }
            else {
                super.send(ioProcessors, id, target, type, event, data, hints, delay);
            }
        }
    }

    /**
     * Test the wizard style SCXML documents, and send usage
     */
    @Test
    void testWizard01Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/wizard-01.xml");
        exec.go();
        Assertions.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state1", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event2");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state2", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event4");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state4", currentStates.iterator().next().getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "event3");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state3", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event3"); // ensure we stay put
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state3", currentStates.iterator().next().getId());
    }

    @Test
    void testWizard02Sample() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/wizard-02.xml");
        exec.setEventdispatcher(new TestEventDispatcher());
        exec.go();
        // If you change this, you must also change
        // the TestEventDispatcher
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state2", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "event4");
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("state4", currentStates.iterator().next().getId());
    }
}
