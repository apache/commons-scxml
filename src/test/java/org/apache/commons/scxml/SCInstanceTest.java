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

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.scxml.env.SimpleContext;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SCInstanceTest extends TestCase {

    public SCInstanceTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SCInstanceTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { SCInstanceTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    private SCInstance instance;
    
    public void setUp() {
        instance = new SCInstance(null);
    }
    
    public void testGetRootContextNull() {
        assertNull(instance.getRootContext());
    }
    
    public void testGetRootContext() {
        Context context = new SimpleContext();
        context.set("name", "value");
        
        instance.setRootContext(context);
        assertEquals("value", instance.getRootContext().get("name"));
    }
    
    public void testGetRootContextEvaluator() {
        Evaluator evaluator = new JexlEvaluator();
        
        instance.setEvaluator(evaluator);
        
        assertTrue(instance.getRootContext() instanceof JexlContext);
    }
    
    public void testGetContext() {
        TransitionTarget target = new State();
        target.setId("1");
        
        Context context = new SimpleContext();
        context.set("name", "value");
        
        instance.setContext(target, context);
        
        assertEquals("value", instance.getContext(target).get("name"));
    }
    
    public void testGetContextNullParent() {
        TransitionTarget target = new State();
        target.setId("1");

        Context context = new SimpleContext();
        context.set("name", "value");
        instance.setRootContext(context);

        Evaluator evaluator = new JexlEvaluator();
        instance.setEvaluator(evaluator);

        assertEquals("value", instance.getContext(target).get("name"));
        assertEquals("value", instance.lookupContext(target).get("name"));
    }

    public void testGetContextParent() {
        TransitionTarget target = new State();
        target.setId("1");
        
        State parent = new State();
        parent.setId("parent");
        
        target.setParent(parent);

        Context context = new SimpleContext();
        context.set("name", "value");
        instance.setRootContext(context);

        Evaluator evaluator = new JexlEvaluator();
        instance.setEvaluator(evaluator);

        assertEquals("value", instance.getContext(target).get("name"));
        assertEquals("value", instance.lookupContext(target).get("name"));
    }

    public void testGetLastConfigurationNull() {
        History history = new History();
        
        Set returnConfiguration = instance.getLastConfiguration(history);
        
        assertEquals(0, returnConfiguration.size());
    }


    public void testGetLastConfiguration() {
        History history = new History();
        history.setId("1");
        
        Set configuration = new HashSet();
        configuration.add("value1");
        configuration.add("value2");
        
        instance.setLastConfiguration(history, configuration);  
        
        Set returnConfiguration = instance.getLastConfiguration(history);
        
        assertEquals(2, returnConfiguration.size());
        assertTrue(returnConfiguration.contains("value1"));
        assertTrue(returnConfiguration.contains("value2"));
    }
    
    public void testIsEmpty() {
        assertTrue(instance.isEmpty(new History()));
    }
    
    public void testIsEmptyFalse() {
        History history = new History();
        history.setId("1");
        
        Set configuration = new HashSet();
        configuration.add("value1");
        configuration.add("value2");
        
        instance.setLastConfiguration(history, configuration);  

        assertFalse(instance.isEmpty(history));
    }
    
    public void testReset() {
        History history = new History();
        history.setId("1");
        
        Set configuration = new HashSet();
        configuration.add("value1");
        configuration.add("value2");
        
        instance.setLastConfiguration(history, configuration);  

        instance.reset(history);
        
        assertTrue(instance.isEmpty(history));
    }
    
}
