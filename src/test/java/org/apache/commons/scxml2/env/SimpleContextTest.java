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
package org.apache.commons.scxml2.env;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleContextTest {

    private SimpleContext context;

    @BeforeEach
    public void setUp() {
        context = new SimpleContext();
    }
    
    @Test
    public void testHasTrue() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        Assertions.assertTrue(context.has("key"));
    }

    @Test
    public void testHasNullParent() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        Assertions.assertFalse(context.has("differentKey"));
    }
    
    @Test
    public void testHasParentWrongKey() {
        Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("key", "value");
        
        SimpleContext parentContext = new SimpleContext(null, parentVars);
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        Assertions.assertFalse(context.has("differentKey"));
    }

    @Test
    public void testHasParentCorrectKey() {
        Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "value");
        
        SimpleContext parentContext = new SimpleContext(null, parentVars);
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        Assertions.assertTrue(context.has("differentKey"));
    }
    
    @Test
    public void testGetNull() {
        Object value = context.get("key");
        
        Assertions.assertNull(value);
    }
    
    @Test
    public void testGetValue() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        Assertions.assertEquals("value", context.get("key"));
    }
    
    @Test
    public void testGetParentValue() {
        Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "differentValue");
        
        SimpleContext parentContext = new SimpleContext(null, parentVars);
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        Assertions.assertEquals("differentValue", context.get("differentKey"));
    }
    
    @Test
    public void testGetParentNull() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        Assertions.assertNull(context.get("differentKey"));
    }
    
    @Test
    public void testGetParentWrongValue() {
        Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "differentValue");
        
        SimpleContext parentContext = new SimpleContext(null, parentVars);
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        Assertions.assertNull(context.get("reallyDifferentKey"));
    }

    @Test
    public void testSetVarsChangeValue() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        
        context.set("key", "newValue");
        
        Assertions.assertEquals("newValue", context.get("key"));
    }

    @Test
    public void testSetVarsEmpty() {
        Map<String, Object> vars = new HashMap<>();
        context.setVars(vars);
        
        context.set("key", "newValue");
        
        Assertions.assertEquals("newValue", context.get("key"));
    }
    
    @Test
    public void testSetVarsParent() {
        Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "differentValue");
        
        SimpleContext parentContext = new SimpleContext(null, parentVars);
        
        Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");
        
        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);
        
        context.set("differentKey", "newValue");
        
        Assertions.assertEquals("newValue", context.get("differentKey"));
    }

    @Test
    public void testNestedEffectiveContextMapWrappingFails() {
        SimpleContext rootContext = new SimpleContext();
        rootContext.set("key", "root");
        SimpleContext rootEffectiveContext = new SimpleContext(rootContext, new EffectiveContextMap(rootContext));
        SimpleContext parentContext = new SimpleContext(rootEffectiveContext);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new EffectiveContextMap(parentContext),
                "Nested EffectiveContextMap wrapping should fail"
        );
    }

    @Test
    public void testEffectiveContextMapMergeStragegy() {
        SimpleContext rootContext = new SimpleContext();
        rootContext.set("key", "root");
        SimpleContext parentContext = new SimpleContext(rootContext);
        parentContext.setLocal("key", "parent");
        SimpleContext effectiveContext = new SimpleContext(parentContext, new EffectiveContextMap(parentContext));
        Assertions.assertEquals("parent", effectiveContext.get("key"));
        // ensure EffectiveContextMap provides complete local variable shadowing
        for (Map.Entry<String,Object> entry : effectiveContext.getVars().entrySet()) {
            if (entry.getKey().equals("key")) {
                Assertions.assertEquals("parent", entry.getValue());
            }
        }
    }
}
