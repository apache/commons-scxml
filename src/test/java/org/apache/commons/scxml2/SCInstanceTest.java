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

import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.scxml2.env.SimpleContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SCInstanceTest {

    private SCXMLExecutor executor;
    private SCInstance instance;
    
    @Before
    public void setUp() {
        executor = new SCXMLExecutor();
        instance = executor.getSCInstance();
    }
    
    @Test
    public void testGetRootContextNull() {
        Assert.assertNull(instance.getRootContext());
    }
    
    @Test
    public void testGetRootContext() {
        Context context = new SimpleContext();
        context.set("name", "value");
        
        instance.setRootContext(context);
        Assert.assertEquals("value", instance.getRootContext().get("name"));
    }
    
    @Test
    public void testGetRootContextEvaluator() throws Exception {
        Evaluator evaluator = new JexlEvaluator();

        executor.setEvaluator(evaluator);

        Assert.assertTrue(instance.getRootContext() instanceof JexlContext);
    }
    
    @Test
    public void testGetContext() {
        State target = new State();
        target.setId("1");
        
        Context context = new SimpleContext();
        context.set("name", "value");
        
        instance.setContext(target, context);
        
        Assert.assertEquals("value", instance.getContext(target).get("name"));
    }
    
    @Test
    public void testGetContextNullParent() throws Exception {
        State target = new State();
        target.setId("1");

        Context context = new SimpleContext();
        context.set("name", "value");
        instance.setRootContext(context);

        Evaluator evaluator = new JexlEvaluator();
        executor.setEvaluator(evaluator);

        Assert.assertEquals("value", instance.getContext(target).get("name"));
        Assert.assertEquals("value", instance.lookupContext(target).get("name"));
    }

    @Test
    public void testGetContextParent() throws Exception {
        State target = new State();
        target.setId("1");
        
        State parent = new State();
        parent.setId("parent");
        
        target.setParent(parent);

        Context context = new SimpleContext();
        context.set("name", "value");
        instance.setRootContext(context);

        Evaluator evaluator = new JexlEvaluator();
        executor.setEvaluator(evaluator);

        Assert.assertEquals("value", instance.getContext(target).get("name"));
        Assert.assertEquals("value", instance.lookupContext(target).get("name"));
    }

    @Test
    public void testGetLastConfigurationNull() {
        History history = new History();
        
        Set<EnterableState> returnConfiguration = instance.getLastConfiguration(history);
        
        Assert.assertEquals(0, returnConfiguration.size());
    }

    @Test
    public void testGetLastConfiguration() {
        History history = new History();
        history.setId("1");
        
        Set<EnterableState> configuration = new HashSet<EnterableState>();
        EnterableState tt1 = new State();
        EnterableState tt2 = new State();
        configuration.add(tt1);
        configuration.add(tt2);
        
        instance.setLastConfiguration(history, configuration);  
        
        Set<EnterableState> returnConfiguration = instance.getLastConfiguration(history);
        
        Assert.assertEquals(2, returnConfiguration.size());
        Assert.assertTrue(returnConfiguration.contains(tt1));
        Assert.assertTrue(returnConfiguration.contains(tt2));
    }
    
    @Test
    public void testIsEmpty() {
        Assert.assertTrue(instance.isEmpty(new History()));
    }
    
    @Test
    public void testIsEmptyFalse() {
        History history = new History();
        history.setId("1");
        
        Set<EnterableState> configuration = new HashSet<EnterableState>();
        EnterableState tt1 = new State();
        configuration.add(tt1);
        
        instance.setLastConfiguration(history, configuration);  

        Assert.assertFalse(instance.isEmpty(history));
    }
    
    @Test
    public void testReset() {
        History history = new History();
        history.setId("1");

        Set<EnterableState> configuration = new HashSet<EnterableState>();
        EnterableState tt1 = new State();
        configuration.add(tt1);
        
        instance.setLastConfiguration(history, configuration);  

        instance.reset(history);
        
        Assert.assertTrue(instance.isEmpty(history));
    }
    
}
