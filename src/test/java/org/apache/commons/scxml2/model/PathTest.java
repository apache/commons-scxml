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
import org.junit.Test;

public class PathTest {

    @Test
    public void testConstructorNull() {
        Path path = new Path(null, null);
        
        Assert.assertNull(path.getPathScope());
    }
    
    @Test
    public void testConstructorNullState() {
        Path path = new Path(new State(), null);
        
        Assert.assertTrue(path.getPathScope() instanceof State);
    }
    
    @Test
    public void testConstructorStates() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);
        
        Assert.assertNull(path.getPathScope());
        Assert.assertEquals(1, path.getUpwardSegment().size());
        Assert.assertEquals("1", path.getUpwardSegment().get(0).getId());

        Assert.assertEquals(1, path.getDownwardSegment().size());
        Assert.assertEquals("2", path.getDownwardSegment().get(0).getId());
        
        Assert.assertFalse(path.isCrossRegion());
    }
    
    @Test
    public void testConstructorSourceCrossRegion() {
        Parallel region = new Parallel();

        State source = new State();
        source.setId("1");
        source.setParent(region);

        State target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);
        
        Assert.assertTrue(path.isCrossRegion());
    }
    
    @Test
    public void testConstructorTargetCrossRegion() {
        Parallel region = new Parallel();

        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");
        target.setParent(region);
        
        Path path = new Path(source, target);
        
        Assert.assertTrue(path.isCrossRegion());
    }
    
    @Test
    public void testConstructorParentTarget() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");

        source.setParent(target);

        Path path = new Path(source, target);

        Assert.assertNull(path.getPathScope());
    }
    
    @Test
    public void testConstructorParentSource() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");

        target.setParent(source);

        Path path = new Path(source, target);

        Assert.assertNull(path.getPathScope());
    }
    
    @Test
    public void testConstructorParent() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");

        State parent = new State();
        parent.setId("parentid");
        
        target.setParent(parent);
        source.setParent(parent);

        Path path = new Path(source, target);

        Assert.assertEquals("parentid", path.getPathScope().getId());
    }
    
    @Test
    public void testConstructorParentParallel() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");

        Parallel parent = new Parallel();
        parent.setId("parentid");
        
        target.setParent(parent);
        source.setParent(parent);

        Path path = new Path(source, target);

        Assert.assertEquals("parentid", path.getPathScope().getId());
    }
    
    @Test
    public void testConstructorParentParallelParent() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");

        Parallel parent = new Parallel();
        parent.setId("parentid");
        
        State parentOfParent = new State();
        parentOfParent.setId("superParent");
 
        parent.setParent(parentOfParent);
        
        target.setParent(parent);
        source.setParent(parent);

        Path path = new Path(source, target);

        Assert.assertEquals("parentid", path.getPathScope().getId());
    }
    
    @Test
    public void testGetRegionsExitedNull() {
        Path path = new Path(new State(), null);

        Assert.assertEquals(0, path.getRegionsExited().size());
    }
    
    @Test
    public void testGetRegionsExitedNotRegion() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);

        Assert.assertEquals(0, path.getRegionsExited().size());
    }
    
    @Test
    public void testGetRegionsExitedParallel() {
        Parallel source = new Parallel();
        source.setId("1");

        Parallel target = new Parallel();
        target.setId("2");
        
        Path path = new Path(source, target);

        Assert.assertEquals(0, path.getRegionsExited().size());
    }
    
    @Test
    public void testGetRegionsExited() {
        Parallel region = new Parallel();

        State source = new State();
        source.setId("1");
        source.setParent(region);

        State target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);
        
        Assert.assertEquals(1, path.getRegionsExited().size());
        Assert.assertEquals("1", (path.getRegionsExited().get(0)).getId());
    }
    
    @Test
    public void testGetRegionsEnteredNull() {
        Path path = new Path(new State(), null);

        Assert.assertEquals(0, path.getRegionsEntered().size());
    }
    
    @Test
    public void testGetRegionsEnteredNotRegion() {
        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");
        
        Path path = new Path(source, target);

        Assert.assertEquals(0, path.getRegionsEntered().size());
    }
    
    @Test
    public void testGetRegionsEnteredParallel() {
        Parallel source = new Parallel();
        source.setId("1");

        Parallel target = new Parallel();
        target.setId("2");
        
        Path path = new Path(source, target);

        Assert.assertEquals(0, path.getRegionsEntered().size());
    }
    
    @Test
    public void testGetRegionsEntered() {
        Parallel region = new Parallel();

        State source = new State();
        source.setId("1");

        State target = new State();
        target.setId("2");
        target.setParent(region);
        
        Path path = new Path(source, target);
        
        Assert.assertEquals(1, path.getRegionsEntered().size());
        Assert.assertEquals("2", (path.getRegionsEntered().get(0)).getId());
    }

}
