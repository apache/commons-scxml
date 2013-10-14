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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class SimpleContextTest extends TestCase {

    public SimpleContextTest(String testName) {
        super(testName);
    }

    private SimpleContext context;

    @Override
    protected void setUp() throws Exception {
        context = new SimpleContext();
    }
    
    public void testHasTrue() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        assertTrue(context.has("key"));
    }

    public void testHasNullParent() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        assertFalse(context.has("differentKey"));
    }
    
    public void testHasParentWrongKey() {
        Map<String, Object> parentVars = new HashMap<String, Object>();
        parentVars.put("key", "value");
        
        SimpleContext parentContext = new SimpleContext(parentVars);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        assertFalse(context.has("differentKey"));
    }

    public void testHasParentCorrectKey() {
        Map<String, Object> parentVars = new HashMap<String, Object>();
        parentVars.put("differentKey", "value");
        
        SimpleContext parentContext = new SimpleContext(parentVars);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        assertTrue(context.has("differentKey"));
    }
    
    public void testGetNull() {
        Object value = context.get("key");
        
        assertNull(value);
    }
    
    public void testGetValue() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        assertEquals("value", context.get("key"));
    }
    
    public void testGetParentValue() {
        Map<String, Object> parentVars = new HashMap<String, Object>();
        parentVars.put("differentKey", "differentValue");
        
        SimpleContext parentContext = new SimpleContext(parentVars);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        assertEquals("differentValue", context.get("differentKey"));
    }
    
    public void testGetParentNull() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        assertNull(context.get("differentKey"));
    }
    
    public void testGetParentWrongValue() {
        Map<String, Object> parentVars = new HashMap<String, Object>();
        parentVars.put("differentKey", "differentValue");
        
        SimpleContext parentContext = new SimpleContext(parentVars);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        assertNull(context.get("reallyDifferentKey"));
    }

    public void testSetVarsChangeValue() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        context.set("key", "newValue");
        
        assertEquals("newValue", context.get("key"));
    }

    public void testSetVarsEmpty() {
        Map<String, Object> vars = new HashMap<String, Object>();
        context.setVars(vars);
        
        context.set("key", "newValue");
        
        assertEquals("newValue", context.get("key"));
    }
    
    public void testSetVarsParent() {
        Map<String, Object> parentVars = new HashMap<String, Object>();
        parentVars.put("differentKey", "differentValue");
        
        SimpleContext parentContext = new SimpleContext(parentVars);
        
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        context.set("differentKey", "newValue");
        
        assertEquals("newValue", context.get("differentKey"));
    }
}
