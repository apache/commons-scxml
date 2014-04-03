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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatusTest {

    private Status status;
    
    @Before
    public void setUp() {
        status = new Status();
    }
    
    @Test
    public void testIsFinalStateFalse() {
        State state = new State();

        status.getStates().add(state);
        
        Assert.assertFalse(status.isFinal());
    }
    
    @Test
    public void testIsFinalStateHasParent() {
        Final state = new Final();
        state.setParent(new State());
        
        status.getStates().add(state);

        Assert.assertFalse(status.isFinal());
    }
    
    @Test
    public void testIsFinalState() {
        Final state = new Final();
        
        status.getStates().add(state);
        
        Assert.assertTrue(status.isFinal());
    }

    @Test
    public void testGetAllStatesEmptyStatus() {

        Set<EnterableState> returnValue = status.getAllStates();

        Assert.assertEquals(0, returnValue.size());
    }

    @Test
    public void testGetAllStatesContainsParent() {
        State parent = new State();
        parent.setId("0");
        State state = new State();
        state.setId("1");
        state.setParent(parent);
        status.getStates().add(state);

        Set<EnterableState> returnValue = status.getAllStates();

        Assert.assertEquals(2, returnValue.size());
    }
}
