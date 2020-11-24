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
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StatusTest {

    private StateConfiguration stateConfiguration;
    private Status status;
    
    @BeforeEach
    public void setUp() {
        stateConfiguration = new StateConfiguration();
        status = new Status(stateConfiguration);
    }
    
    @Test
    public void testIsFinalStateFalse() {
        final State state = new State();

        stateConfiguration.enterState(state);
        
        Assertions.assertFalse(status.isFinal());
    }
    
    @Test
    public void testIsFinalStateHasParent() {
        final Final state = new Final();
        state.setParent(new State());
        
        stateConfiguration.enterState(state);

        Assertions.assertFalse(status.isFinal());
    }
    
    @Test
    public void testIsFinalState() {
        final Final state = new Final();

        stateConfiguration.enterState(state);

        Assertions.assertTrue(status.isFinal());
    }

    @Test
    public void testGetAllStatesEmptyStatus() {

        final Set<EnterableState> returnValue = status.getActiveStates();

        Assertions.assertEquals(0, returnValue.size());
    }

    @Test
    public void testGetAllStatesContainsParent() {
        final State parent = new State();
        parent.setId("0");
        stateConfiguration.enterState(parent);
        final State state = new State();
        state.setId("1");
        state.setParent(parent);
        stateConfiguration.enterState(state);

        final Set<EnterableState> returnValue = status.getActiveStates();

        Assertions.assertEquals(2, returnValue.size());
    }

    @Test
    public void testIsInState() {
        final State parent = new State();
        parent.setId("0");
        stateConfiguration.enterState(parent);
        final State state = new State();
        state.setId("1");
        state.setParent(parent);
        stateConfiguration.enterState(state);
        Assertions.assertTrue(status.isInState("0"));
        Assertions.assertTrue(status.isInState("1"));
        Assertions.assertFalse(status.isInState("2"));
    }
}
