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

import java.net.URL;
import java.util.List;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StateTest {

    // Test data
    private State state;
    private URL state01;
    private SCXMLExecutor exec;

    @Before
    public void setUp() {
        state = new State();
        state01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/model/state-01.xml");
    }

    @After
    public void tearDown() {
        state01 = null;
        exec = null;
    }
    
    @Test
    public void testGetTransitionsListNull() {
        Assert.assertNull(state.getTransitionsList("event"));
    }
        
    @Test
    public void testGetTransitionsList() {
        
        state.getTransitionsList().add(new Transition());
        
        Assert.assertNotNull(state.getTransitionsList(null));
    }
        
    @Test
    public void testAddTransitionDoesNotContainKey() {
        Transition transition = new Transition();
        transition.setEvent("event");
        
        state.addTransition(transition);
        
        List<Transition> events = state.getTransitionsList("event");
        
        Assert.assertEquals(1, events.size());
        Assert.assertEquals("event", events.get(0).getEvent());
    }
        
    @Test
    public void testAddTransitionContainKey() {
        Transition transition1 = new Transition();
        transition1.setEvent("event");

        Transition transition2 = new Transition();
        transition2.setEvent("event");

        state.addTransition(transition1);
        state.addTransition(transition2);
        
        List<Transition> events = state.getTransitionsList("event");
        
        Assert.assertEquals(2, events.size());
    }
        
    @Test
    public void testGetTransitionList() {
        Transition transition1 = new Transition();
        transition1.setEvent("event");

        Transition transition2 = new Transition();
        transition2.setEvent("event");

        state.addTransition(transition1);
        state.addTransition(transition2);
        
        List<Transition> events = state.getTransitionsList();
        
        Assert.assertEquals(2, events.size());
    }
        
    @Test
    public void testHasHistoryEmpty() {
        Assert.assertFalse(state.hasHistory());
    }
    
    @Test
    public void testHasHistory() {
        History history = new History();
        
        state.addHistory(history);
        
        Assert.assertTrue(state.hasHistory());
    }
        
    @Test
    public void testIsSimple() {
        Assert.assertTrue(state.isSimple());
    }
        
    @Test
    public void testIsSimpleHasChildren() {
        State state1 = new State();
        
        // redundant cast to remove deprecation warning
        // could be removed in v1.0
        state.addChild(state1);
        
        Assert.assertFalse(state.isSimple());
    }
        
    @Test
    public void testIsCompositeFalse() {
        Assert.assertFalse(state.isComposite());
    }
        
    @Test
    public void testIsCompositeParallel() {
        State child = new State();
        
        state.addChild(child);
        
        Assert.assertTrue(state.isComposite());
    }
        
    @Test
    public void testIsCompositeHasChildren() {
        State state1 = new State();
        
        // redundant cast to remove deprecation warning
        // could be removed in v1.0
        state.addChild(state1);
        
        Assert.assertTrue(state.isComposite());
    }
        
    @Test
    public void testIsRegion() {
        state.setParent(new Parallel());
        
        Assert.assertTrue(state.isRegion());
    }
        
    @Test
    public void testIsRegionNotParallel() {
        state.setParent(new State());
        
        Assert.assertFalse(state.isRegion());
    }
    
    @Test
    public void testInitialAttribute() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(state01);
        Assert.assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        Assert.assertNotNull(exec);
        Assert.assertEquals("s11", exec.getCurrentStatus().getStates().iterator().next().getId());
    }

}
