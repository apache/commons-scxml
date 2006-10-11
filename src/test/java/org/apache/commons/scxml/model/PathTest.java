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

public class PathTest extends TestCase {

    public PathTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(PathTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { PathTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    public void testConstructorNull() {
        Path path = new Path(null, null);
        
        assertNull(path.getScope());
    }

    public void testConstructorNullState() {
        Path path = new Path(new State(), null);
        
        assertTrue(path.getScope() instanceof State);
    }

    public void testConstructorStates() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);
        
        assertNull(path.getScope());
        assertEquals(1, path.getUpwardSegment().size());
        assertEquals("1", ((State)path.getUpwardSegment().get(0)).getId());

        assertEquals(1, path.getDownwardSegment().size());
        assertEquals("2", ((State)path.getDownwardSegment().get(0)).getId());
        
        assertFalse(path.isCrossRegion());
    }
    
    public void testConstructorSourceCrossRegion() {
        Parallel region = new Parallel();
        
        TransitionTarget source = new State();
        source.setId("1");
        source.setParent(region);
        
        TransitionTarget target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);
        
        assertTrue(path.isCrossRegion());
    }

    public void testConstructorTargetCrossRegion() {
        Parallel region = new Parallel();
        
        TransitionTarget source = new State();
        source.setId("1");
        
        TransitionTarget target = new State();
        target.setId("2");
        target.setParent(region);
        
        Path path = new Path(source, target);
        
        assertTrue(path.isCrossRegion());
    }

    public void testConstructorParentTarget() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");

        source.setParent(target);

        Path path = new Path(source, target);

        assertNull(path.getScope());
    }

    public void testConstructorParentSource() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");

        target.setParent(source);

        Path path = new Path(source, target);

        assertNull(path.getScope());
    }
    
    public void testConstructorParent() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");

        State parent = new State();
        parent.setId("parentid");
        
        target.setParent(parent);
        source.setParent(parent);

        Path path = new Path(source, target);

        assertEquals("parentid", path.getScope().getId());
    }
    
    public void testConstructorParentParallel() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");

        Parallel parent = new Parallel();
        parent.setId("parentid");
        
        target.setParent(parent);
        source.setParent(parent);

        Path path = new Path(source, target);

        assertNull(path.getScope());
    }
    
    public void testConstructorParentParallelParent() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");

        Parallel parent = new Parallel();
        parent.setId("parentid");
        
        State parentOfParent = new State();
        parentOfParent.setId("superParent");
 
        parent.setParent(parentOfParent);
        
        target.setParent(parent);
        source.setParent(parent);

        Path path = new Path(source, target);

        assertEquals("superParent", path.getScope().getId());
    }
    
    public void testGetRegionsExitedNull() {
        Path path = new Path(new State(), null);

        assertEquals(0, path.getRegionsExited().size());
    }
    
    public void testGetRegionsExitedNotRegion() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);

        assertEquals(0, path.getRegionsExited().size());
    }
    
    public void testGetRegionsExitedParallel() {
        TransitionTarget source = new Parallel();
        source.setId("1");

        TransitionTarget target = new Parallel();
        target.setId("2");
        
        Path path = new Path(source, target);

        assertEquals(0, path.getRegionsExited().size());
    }
    
    public void testGetRegionsExited() {
        Parallel region = new Parallel();
        
        TransitionTarget source = new State();
        source.setId("1");
        source.setParent(region);
        
        TransitionTarget target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);
        
        assertEquals(1, path.getRegionsExited().size());
        assertEquals("1", ((State)path.getRegionsExited().get(0)).getId());
    }

    public void testGetRegionsEnteredNull() {
        Path path = new Path(new State(), null);

        assertEquals(0, path.getRegionsEntered().size());
    }
    
    public void testGetRegionsEnteredNotRegion() {
        TransitionTarget source = new State();
        source.setId("1");

        TransitionTarget target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);

        assertEquals(0, path.getRegionsEntered().size());
    }
    
    public void testGetRegionsEnteredParallel() {
        TransitionTarget source = new Parallel();
        source.setId("1");

        TransitionTarget target = new Parallel();
        target.setId("2");
        
        Path path = new Path(source, target);

        assertEquals(0, path.getRegionsEntered().size());
    }
    
    public void testGetRegionsEntered() {
        Parallel region = new Parallel();
        
        TransitionTarget source = new State();
        source.setId("1");
        
        TransitionTarget target = new State();
        target.setId("2");
        target.setParent(region);
        
        Path path = new Path(source, target);
        
        assertEquals(1, path.getRegionsEntered().size());
        assertEquals("2", ((State)path.getRegionsEntered().get(0)).getId());
    }

}
