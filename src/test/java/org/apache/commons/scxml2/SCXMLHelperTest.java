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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.scxml2.env.MockErrorReporter;
import org.apache.commons.scxml2.env.SimpleErrorReporter;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.semantics.ErrorConstants;
import org.junit.Assert;
import org.junit.Test;

public class SCXMLHelperTest {

    @Test
    public void testIsStringEmptyNull() {
        Assert.assertTrue(SCXMLHelper.isStringEmpty(null));
    }
    
    @Test
    public void testIsStringEmptyZeroLength() {
        Assert.assertTrue(SCXMLHelper.isStringEmpty(""));
    }

    @Test
    public void testIsStringEmpty() {
        Assert.assertFalse(SCXMLHelper.isStringEmpty("value"));
    }

    @Test
    public void testIsDescendantNullParent() {
        State state = new State();
        State context = new State();
        
        Assert.assertFalse(SCXMLHelper.isDescendant(state, context));
    }
    
    @Test
    public void testIsDescendantNotEqual() {
        State state = new State();
        state.setParent(new State());
        State context = new State();
        
        Assert.assertFalse(SCXMLHelper.isDescendant(state, context));
    }
    
    @Test
    public void testIsDescendantEqual() {
        State state = new State();
        State context = new State();
        state.setParent(context);
        
        Assert.assertTrue(SCXMLHelper.isDescendant(state, context));
    }
    
    @Test
    public void testIsDescendantParentEqual() {
        State state = new State();
        State context = new State();
        State parent = new State();

        parent.setParent(context);
        state.setParent(parent);
        
        Assert.assertTrue(SCXMLHelper.isDescendant(state, context));
    }
    
    @Test
    public void testGetAncestorClosureEmptySet() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();
        
        Set<EnterableState> returnValue = SCXMLHelper.getAncestorClosure(states, new HashSet<TransitionTarget>());
        
        Assert.assertEquals(0, returnValue.size());
    }
    
    @Test
    public void testGetAncestorClosureUpperBoundNotNullAndContains() {
        Set<EnterableState> states = new HashSet<EnterableState>();
        EnterableState state = new State();
        state.setId("1");
        states.add(state);
        
        Set<EnterableState> upperBounds = new HashSet<EnterableState>();
        upperBounds.add(state);
        
        Set<EnterableState> returnValue = SCXMLHelper.getAncestorClosure(states, upperBounds);
        
        Assert.assertEquals(1, returnValue.size());
        Assert.assertEquals("1", ((TransitionTarget)returnValue.toArray()[0]).getId());
    }
    
    @Test
    public void testGetAncestorClosureContainsParent() {
        Set<EnterableState> states = new HashSet<EnterableState>();
        State parent = new State();
        parent.setId("0");
        State state = new State();
        state.setId("1");
        state.setParent(parent);
        states.add(state);
        
        Set<EnterableState> upperBounds = new HashSet<EnterableState>();
        
        Set<EnterableState> returnValue = SCXMLHelper.getAncestorClosure(states, upperBounds);
        
        Assert.assertEquals(2, returnValue.size());
    }
    
    @Test
    public void testIsLegalConfigNoStates() {
        Set<EnterableState> states = new HashSet<EnterableState>();
        
        Assert.assertTrue(SCXMLHelper.isLegalConfig(states, new SimpleErrorReporter()));
    }
    
    @Test
    public void testIsLegalConfigInvalidParallel() {
        Set<EnterableState> states = new HashSet<EnterableState>();
        Parallel parallel = new Parallel();

        Parallel parent = new Parallel();
        parent.setId("4");

        State state1 = new State();
        state1.setId("1");
        State state2 = new State();
        state2.setId("2");
        
        parent.addChild(state1);
        parent.addChild(state2);
        
        parallel.setParent(parent);
        
        states.add(parallel);
        
        MockErrorReporter errorReporter = new MockErrorReporter();
        
        Assert.assertFalse(SCXMLHelper.isLegalConfig(states, errorReporter));
        Assert.assertEquals(ErrorConstants.ILLEGAL_CONFIG, errorReporter.getErrCode());
        Assert.assertEquals("Not all AND states active for parallel 4", errorReporter.getErrDetail());
    }
    
    @Test
    public void testIsLegalConfigMultipleTopLevel() {
        Set<EnterableState> states = new HashSet<EnterableState>();

        State state1 = new State();
        state1.setId("1");
        State state2 = new State();
        state2.setId("2");
        
        states.add(state1);
        states.add(state2);
        
        MockErrorReporter errorReporter = new MockErrorReporter();
        
        Assert.assertTrue(SCXMLHelper.isLegalConfig(states, errorReporter));
        Assert.assertEquals(ErrorConstants.ILLEGAL_CONFIG, errorReporter.getErrCode());
        Assert.assertEquals("Multiple top-level OR states active!", errorReporter.getErrDetail());
    }
    
    @Test
    public void testIsLegalConfigMultipleStatesActive() {
        Set<EnterableState> states = new HashSet<EnterableState>();

        State state1 = new State();
        state1.setId("1");
        
        State state2 = new State();
        state2.setId("2");

        State parent = new State();
        parent.setId("parentid");
        
        state2.setParent(parent);
        state1.setParent(parent);

        states.add(state1);
        states.add(state2);
        
        MockErrorReporter errorReporter = new MockErrorReporter();
        
        Assert.assertFalse(SCXMLHelper.isLegalConfig(states, errorReporter));
        Assert.assertEquals(ErrorConstants.ILLEGAL_CONFIG, errorReporter.getErrCode());
        Assert.assertEquals("Multiple OR states active for state parentid", errorReporter.getErrDetail());
    }
    
    @Test
    public void testGetLCASameTarget() {
        State target = new State();
        target.setId("1");
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target, target);
        
        Assert.assertEquals("1", returnValue.getId());
    }

    @Test
    public void testGetLCAIsDescendant() {
        State target = new State();
        target.setId("1");

        State parent = new State();
        parent.setId("2");

        target.setParent(parent);
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target, parent);
        
        Assert.assertEquals("2", returnValue.getId());
    }
    
    @Test
    public void testGetLCAIsDescendantReverse() {
        State target = new State();
        target.setId("1");

        State parent = new State();
        parent.setId("2");

        parent.setParent(target); // reversed
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target, parent);
        
        Assert.assertEquals("1", returnValue.getId());
    }

    @Test
    public void testGetLCANull() {
        State target = new State();
        target.setId("1");

        State notParent = new State();
        notParent.setId("2");

        TransitionTarget returnValue = SCXMLHelper.getLCA(target, notParent);
        
        Assert.assertNull(returnValue);
    }

    @Test
    public void testGetLCADistantAncestor() {
        State target1 = new State();
        target1.setId("1");

        State target2 = new State();
        target2.setId("2");

        State parent = new State();
        parent.setId("3");

        target1.setParent(parent);
        target2.setParent(parent);
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target1, target2);
        
        Assert.assertEquals("3", returnValue.getId());
    }
}
