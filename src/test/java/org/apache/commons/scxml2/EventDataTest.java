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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.SCXML;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link SCXMLExecutor}.
 * Testing special variable "_event.data"
 */
class EventDataTest {

    public static class ConnectionAlertingPayload {
        private int line;
        ConnectionAlertingPayload(final int line) {
            this.line = line;
        }
        @SuppressWarnings("unsed")
        public int getLine() {
            return line;
        }
        @SuppressWarnings("unsed")
        public void setLine(final int line) {
            this.line = line;
        }
    }

    /**
     * Test the SCXML documents, usage of "_event.data"
     */
    @Test
    void testEventdata01Sample() throws Exception {
    	final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/eventdata-01.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state1", currentStates.iterator().next().getId());
        TriggerEvent te = new EventBuilder("event.foo", TriggerEvent.SIGNAL_EVENT).data(3).build();
        currentStates = SCXMLTestHelper.fireEvent(exec, te);
        assertEquals(1, currentStates.size());
        assertEquals("state3", currentStates.iterator().next().getId());
        final TriggerEvent[] evts = { te,
            new EventBuilder("event.bar", TriggerEvent.SIGNAL_EVENT).data(6).build()};
        currentStates = SCXMLTestHelper.fireEvents(exec, evts);
        assertEquals(1, currentStates.size());
        assertEquals("state6", currentStates.iterator().next().getId());
        te = new EventBuilder("event.baz", TriggerEvent.SIGNAL_EVENT).data(7).build();
        currentStates = SCXMLTestHelper.fireEvent(exec, te);
        assertEquals(1, currentStates.size());
        assertEquals("state7", currentStates.iterator().next().getId());
    }

    @Test
    void testEventdata02Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/eventdata-02.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state0", currentStates.iterator().next().getId());
        final TriggerEvent te1 = new EventBuilder("connection.alerting", TriggerEvent.SIGNAL_EVENT).data("line2").build();
        currentStates = SCXMLTestHelper.fireEvent(exec, te1);
        assertEquals(1, currentStates.size());
        assertEquals("state2", currentStates.iterator().next().getId());
        final TriggerEvent te2 = new EventBuilder("connection.alerting", TriggerEvent.SIGNAL_EVENT)
                .data(new ConnectionAlertingPayload(4)).build();
        currentStates = SCXMLTestHelper.fireEvent(exec, te2);
        assertEquals(1, currentStates.size());
        assertEquals("state4", currentStates.iterator().next().getId());
    }

    @Test
    void testEventdata03Sample() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/eventdata-03.xml");
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ten", currentStates.iterator().next().getId());
        final TriggerEvent te = new EventBuilder("event.foo", TriggerEvent.SIGNAL_EVENT).build();
        currentStates = SCXMLTestHelper.fireEvent(exec, te);
        assertEquals(1, currentStates.size());
        assertEquals("thirty", currentStates.iterator().next().getId());
    }

    @Test
    void testEventdata04Sample() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/jexl/eventdata-03.xml");
        final Tracer trc = new Tracer();
        final SCXMLExecutor exec = new SCXMLExecutor(null, null, trc);
        exec.addListener(scxml, trc);
        exec.setStateMachine(scxml);
        exec.go();
        Thread.sleep(200); // let the 100 delay lapse
    }
}
