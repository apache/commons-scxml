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
package org.apache.commons.scxml2.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransitionTest {

    private Transition transition;
    
    @Before
    public void setUp() {
        transition = new Transition();
    }
        
    @Test
    public void testGetRuntimeTargetNullNoParent() {
        Assert.assertTrue(transition.getRuntimeTargets().size() > 0);
    }
        
    @Test
    public void testGetRuntimeTargetNullWithParent() {
        State state = new State();
        state.setId("1");
        
        transition.setParent(state);
        
        Assert.assertEquals("1", (transition.getRuntimeTargets().get(0)).getId());
    }
        
    @Test
    public void testGetRuntimeTarget() {
        State state = new State();
        state.setId("2");
        
        transition.getTargets().add(state);
        
        Assert.assertEquals("2", (transition.getRuntimeTargets().get(0)).getId());
    }
        
    @Test
    public void testGetPath() {
        Assert.assertTrue(transition.getPaths().size() > 0);
    }
}
