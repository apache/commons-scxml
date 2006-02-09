/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.model.State;

public class TestStatus extends TestCase {

    public TestStatus(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestStatus.class);
        suite.setName("TestStatus");
        return suite;
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestStatus.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    private Status status;
    
    public void setUp() {
        status = new Status();
    }
    
    public void testIsFinalStateFalse() {
        State state = new State();
        state.setIsFinal(false);
        
        status.getStates().add(state);
        
        assertFalse(status.isFinal());
    }
    
    public void testIsFinalStateHasParent() {
        State state = new State();
        state.setIsFinal(true);
        state.setParent(new State());
        
        status.getStates().add(state);

        assertFalse(status.isFinal());
    }
    
    public void testIsFinalStateHasEvent() {
        State state = new State();
        state.setIsFinal(true);
        
        status.getStates().add(state);
        status.getEvents().add(new TriggerEvent("name", TriggerEvent.CALL_EVENT));
        
        assertFalse(status.isFinal());
    }
    
    public void testIsFinalState() {
        State state = new State();
        state.setIsFinal(true);
        
        status.getStates().add(state);
        
        assertTrue(status.isFinal());
    }
    
}
