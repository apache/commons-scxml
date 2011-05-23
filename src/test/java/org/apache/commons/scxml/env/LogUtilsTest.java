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
package org.apache.commons.scxml.env;

import junit.framework.TestCase;

import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

public class LogUtilsTest extends TestCase {

    public LogUtilsTest(String testName) {
        super(testName);
    }

    public void testGetTTPathParentNull() {
        TransitionTarget target = new State();
        target.setId("ID");
        
        assertEquals("/ID", LogUtils.getTTPath(target));
    }
    
    public void testGetTTPathParent() {
        TransitionTarget target = new State();
        target.setId("ID");

        TransitionTarget parent1 = new State();
        parent1.setId("parent1");

        TransitionTarget parent2 = new State();
        parent2.setId("parent2");

        parent1.setParent(parent2);
        target.setParent(parent1);
        
        assertEquals("/parent2/parent1/ID", LogUtils.getTTPath(target));
    }
    
    public void testTransToString() {
        TransitionTarget targetTo = new State();
        targetTo.setId("TO");

        TransitionTarget targetFrom = new State();
        targetFrom.setId("FROM");
        
        Transition transition = new Transition();
        transition.setCond("condition");
        transition.setEvent("event happened");
        
        assertEquals( "transition (event = event happened, cond = condition, from = /FROM, to = /TO)", 
                                        LogUtils.transToString(targetFrom, targetTo, transition));
    }

}
