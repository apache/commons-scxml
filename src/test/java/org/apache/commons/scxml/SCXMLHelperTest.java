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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.env.MockErrorReporter;
import org.apache.commons.scxml.env.SimpleErrorReporter;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.semantics.ErrorConstants;

public class SCXMLHelperTest extends TestCase {

    public SCXMLHelperTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SCXMLHelperTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { SCXMLHelperTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    public void testIsStringEmptyNull() {
        assertTrue(SCXMLHelper.isStringEmpty(null));
    }
    
    public void testIsStringEmptyZeroLength() {
        assertTrue(SCXMLHelper.isStringEmpty(""));
    }

    public void testIsStringEmpty() {
        assertFalse(SCXMLHelper.isStringEmpty("value"));
    }

    public void testIsDescendantNullParent() {
        TransitionTarget state = new State();
        TransitionTarget context = new State();
        
        assertFalse(SCXMLHelper.isDescendant(state, context));
    }
    
    public void testIsDescendantNotEqual() {
        TransitionTarget state = new State();
        state.setParent(new State());
        TransitionTarget context = new State();
        
        assertFalse(SCXMLHelper.isDescendant(state, context));
    }
    
    public void testIsDescendantEqual() {
        TransitionTarget state = new State();
        TransitionTarget context = new State();
        state.setParent(context);
        
        assertTrue(SCXMLHelper.isDescendant(state, context));
    }
    
    public void testIsDescendantParentEqual() {
        TransitionTarget state = new State();
        TransitionTarget context = new State();
        TransitionTarget parent = new State();

        parent.setParent(context);
        state.setParent(parent);
        
        assertTrue(SCXMLHelper.isDescendant(state, context));
    }
    
    public void testGetAncestorClosureEmptySet() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();
        
        Set returnValue = SCXMLHelper.getAncestorClosure(states, new HashSet<TransitionTarget>());
        
        assertEquals(0, returnValue.size());
    }
    
    public void testGetAncestorClosureUpperBoundNotNullAndContains() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();
        TransitionTarget state = new State();
        state.setId("1");
        states.add(state);
        
        Set<TransitionTarget> upperBounds = new HashSet<TransitionTarget>();
        upperBounds.add(state);
        
        Set returnValue = SCXMLHelper.getAncestorClosure(states, upperBounds);
        
        assertEquals(1, returnValue.size());
        assertEquals("1", ((TransitionTarget)returnValue.toArray()[0]).getId());
    }
    
    public void testGetAncestorClosureContainsParent() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();
        TransitionTarget state = new State();
        state.setId("1");
        state.setParent(state);
        states.add(state);
        
        Set<TransitionTarget> upperBounds = new HashSet<TransitionTarget>();
        
        Set returnValue = SCXMLHelper.getAncestorClosure(states, upperBounds);
        
        assertEquals(1, returnValue.size());
        assertEquals("1", ((TransitionTarget)returnValue.toArray()[0]).getId());
    }
    
    public void testIsLegalConfigNoStates() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();
        
        assertTrue(SCXMLHelper.isLegalConfig(states, new SimpleErrorReporter()));
    }
    
    public void testIsLegalConfigInvalidParallel() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();
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
        
        assertFalse(SCXMLHelper.isLegalConfig(states, errorReporter));
        assertEquals(ErrorConstants.ILLEGAL_CONFIG, errorReporter.getErrCode());
        assertEquals("Not all AND states active for parallel 4", errorReporter.getErrDetail());
    }
    
    public void testIsLegalConfigMultipleTopLevel() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();

        State state1 = new State();
        state1.setId("1");
        State state2 = new State();
        state2.setId("2");
        
        states.add(state1);
        states.add(state2);
        
        MockErrorReporter errorReporter = new MockErrorReporter();
        
        assertTrue(SCXMLHelper.isLegalConfig(states, errorReporter));
        assertEquals(ErrorConstants.ILLEGAL_CONFIG, errorReporter.getErrCode());
        assertEquals("Multiple top-level OR states active!", errorReporter.getErrDetail());
    }
    
    public void testIsLegalConfigMultipleStatesActive() {
        Set<TransitionTarget> states = new HashSet<TransitionTarget>();

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
        
        assertFalse(SCXMLHelper.isLegalConfig(states, errorReporter));
        assertEquals(ErrorConstants.ILLEGAL_CONFIG, errorReporter.getErrCode());
        assertEquals("Multiple OR states active for state parentid", errorReporter.getErrDetail());
    }
    
    public void testGetLCASameTarget() {
        TransitionTarget target = new State();
        target.setId("1");
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target, target);
        
        assertEquals("1", returnValue.getId());
    }

    public void testGetLCAIsDescendant() {
        TransitionTarget target = new State();
        target.setId("1");

        TransitionTarget parent = new State();
        parent.setId("2");

        target.setParent(parent);
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target, parent);
        
        assertEquals("2", returnValue.getId());
    }
    
    public void testGetLCAIsDescendantReverse() {
        TransitionTarget target = new State();
        target.setId("1");

        TransitionTarget parent = new State();
        parent.setId("2");

        parent.setParent(target); // reversed
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target, parent);
        
        assertEquals("1", returnValue.getId());
    }

    public void testGetLCANull() {
        TransitionTarget target = new State();
        target.setId("1");

        TransitionTarget notParent = new State();
        notParent.setId("2");

        TransitionTarget returnValue = SCXMLHelper.getLCA(target, notParent);
        
        assertNull(returnValue);
    }

    public void testGetLCADistantAncestor() {
        TransitionTarget target1 = new State();
        target1.setId("1");

        TransitionTarget target2 = new State();
        target2.setId("2");

        TransitionTarget parent = new State();
        parent.setId("3");

        target1.setParent(parent);
        target2.setParent(parent);
        
        TransitionTarget returnValue = SCXMLHelper.getLCA(target1, target2);
        
        assertEquals("3", returnValue.getId());
    }
}
