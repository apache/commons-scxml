/* Copyright 2005 The Apache Software Foundation.
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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.scxml.env.SimpleErrorReporter;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestSCXMLHelper extends TestCase {

    public TestSCXMLHelper(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestSCXMLHelper.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestSCXMLHelper.class.getName()};
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
        Set states = new HashSet();
        
        Set returnValue = SCXMLHelper.getAncestorClosure(states, new HashSet());
        
        assertEquals(0, returnValue.size());
    }
    
    public void testGetAncestorClosureUpperBoundNotNullAndContains() {
        Set states = new HashSet();
        TransitionTarget state = new State();
        state.setId("1");
        states.add(state);
        
        Set upperBounds = new HashSet();
        upperBounds.add(state);
        
        Set returnValue = SCXMLHelper.getAncestorClosure(states, upperBounds);
        
        assertEquals(1, returnValue.size());
        assertEquals("1", ((TransitionTarget)returnValue.toArray()[0]).getId());
    }
    
    public void testGetAncestorClosureContainsParent() {
        Set states = new HashSet();
        TransitionTarget state = new State();
        state.setId("1");
        state.setParent(state);
        states.add(state);
        
        Set upperBounds = new HashSet();
        
        Set returnValue = SCXMLHelper.getAncestorClosure(states, upperBounds);
        
        assertEquals(1, returnValue.size());
        assertEquals("1", ((TransitionTarget)returnValue.toArray()[0]).getId());
    }
    
    public void testIsLegalConfigNoStates() {
        Set states = new HashSet();
        
        assertTrue(SCXMLHelper.isLegalConfig(states, new SimpleErrorReporter()));
    }
    
    /*
     * Not able to test the return values on ErrorReporter.
     */
    public void testIsLegalConfigInvalidParallel() {
        Set states = new HashSet();
        Parallel parallel = new Parallel();

        Parallel parent = new Parallel();

        State state1 = new State();
        state1.setId("1");
        State state2 = new State();
        state2.setId("2");
        
        parent.addState(state1);
        parent.addState(state2);
        
        parallel.setParent(parent);
        
        states.add(parallel);
        
        SimpleErrorReporter errorReporter = new SimpleErrorReporter();
        
        assertFalse(SCXMLHelper.isLegalConfig(states, errorReporter));
    }

}
