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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.model.TransitionTarget;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLExecutor}.
 * Testing special variable "_eventdata"
 */
public class EventDataTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public EventDataTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(EventDataTest.class);
        suite.setName("SCXML Executor Tests, _eventdata special variable");
        return suite;
    }

    // Test data
    private URL eventdata01, eventdata02, eventdata03;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        eventdata01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-01.xml");
        eventdata02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-02.xml");
        eventdata03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/eventdata-03.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        eventdata01 = eventdata02 = eventdata03 = null;
    }

    /**
     * Test the SCXML documents, usage of "_eventdata"
     */
    public void testEventdata01Sample() {
    	exec = SCXMLTestHelper.getExecutor(eventdata01);
        assertNotNull(exec);
        try {
            Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("state1", currentStates.iterator().next().getId());
            TriggerEvent te = new TriggerEvent("event.foo",
                TriggerEvent.SIGNAL_EVENT, new Integer(3));
            currentStates = SCXMLTestHelper.fireEvent(exec, te);
            assertEquals(1, currentStates.size());
            assertEquals("state3", currentStates.iterator().next().getId());
            TriggerEvent[] evts = new TriggerEvent[] { te,
                new TriggerEvent("event.bar", TriggerEvent.SIGNAL_EVENT,
                new Integer(6))};
            currentStates = SCXMLTestHelper.fireEvents(exec, evts);
            assertEquals(1, currentStates.size());
            assertEquals("state6", currentStates.iterator().next().getId());
            te = new TriggerEvent("event.baz",
                TriggerEvent.SIGNAL_EVENT, new Integer(7));
            currentStates = SCXMLTestHelper.fireEvent(exec, te);
            assertEquals(1, currentStates.size());
            assertEquals("state7", currentStates.iterator().next().getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testEventdata02Sample() {
    	exec = SCXMLTestHelper.getExecutor(eventdata02);
        assertNotNull(exec);
        try {
            Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("state0", currentStates.iterator().next().getId());
            TriggerEvent te1 = new TriggerEvent("connection.alerting",
                TriggerEvent.SIGNAL_EVENT, "line2");
            currentStates = SCXMLTestHelper.fireEvent(exec, te1);
            assertEquals(1, currentStates.size());
            assertEquals("state2", currentStates.iterator().next().getId());
            TriggerEvent te2 = new TriggerEvent("connection.alerting",
                TriggerEvent.SIGNAL_EVENT,
                new ConnectionAlertingPayload(4));
            currentStates = SCXMLTestHelper.fireEvent(exec, te2);
            assertEquals(1, currentStates.size());
            assertEquals("state4", currentStates.iterator().next().getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testEventdata03Sample() {
        exec = SCXMLTestHelper.getExecutor(eventdata03);
        assertNotNull(exec);
        try {
            Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("ten", currentStates.iterator().next().getId());
            TriggerEvent te = new TriggerEvent("event.foo",
                TriggerEvent.SIGNAL_EVENT);
            currentStates = SCXMLTestHelper.fireEvent(exec, te);
            assertEquals(1, currentStates.size());
            assertEquals("thirty", currentStates.iterator().next().getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public static class ConnectionAlertingPayload {
        private int line;
        public ConnectionAlertingPayload(int line) {
            this.line = line;
        }
        public void setLine(int line) {
            this.line = line;
        }
        public int getLine() {
            return line;
        }
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
