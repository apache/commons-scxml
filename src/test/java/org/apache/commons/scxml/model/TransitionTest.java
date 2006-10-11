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
package org.apache.commons.scxml.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TransitionTest extends TestCase {

    public TransitionTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TransitionTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TransitionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    private Transition transition;
    
    public void setUp() {
        transition = new Transition();
    }
    
    public void testGetRuntimeTargetNullNoParent() {
        transition.setTarget(null);
        
        assertNull(transition.getRuntimeTarget());
    }
    
    public void testGetRuntimeTargetNullWithParent() {
        State state = new State();
        state.setId("1");
        
        transition.setTarget(null);
        transition.setParent(state);
        
        assertEquals("1", transition.getRuntimeTarget().getId());
    }
    
    public void testGetRuntimeTarget() {
        State state = new State();
        state.setId("2");
        
        transition.setTarget(state);
        
        assertEquals("2", transition.getRuntimeTarget().getId());
    }
    
    public void testGetPath() {
        assertNotNull(transition.getPath());
    }
}
