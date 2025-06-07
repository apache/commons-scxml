/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
    void testEffectiveContextMapMergeStragegy() {
        final SimpleContext rootContext = new SimpleContext();
        rootContext.set("key", "root");
        final SimpleContext parentContext = new SimpleContext(rootContext);
        parentContext.setLocal("key", "parent");
        final SimpleContext effectiveContext = new SimpleContext(parentContext, new EffectiveContextMap(parentContext));
        Assertions.assertEquals("parent", effectiveContext.get("key"));
        // ensure EffectiveContextMap provides complete local variable shadowing
        for (final Map.Entry<String,Object> entry : effectiveContext.getVars().entrySet()) {
            if (entry.getKey().equals("key")) {
                Assertions.assertEquals("parent", entry.getValue());
            }
        }
    }

    @Test
    void testGetNull() {
        final Object value = context.get("key");

        Assertions.assertNull(value);
    }

    @Test
    void testGetParentNull() {
        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);

        Assertions.assertNull(context.get("differentKey"));
    }

    @Test
    void testGetParentValue() {
        final Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "differentValue");

        final SimpleContext parentContext = new SimpleContext(null, parentVars);

        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);

        Assertions.assertEquals("differentValue", context.get("differentKey"));
    }

    @Test
    void testGetParentWrongValue() {
        final Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "differentValue");

        final SimpleContext parentContext = new SimpleContext(null, parentVars);

        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);

        Assertions.assertNull(context.get("reallyDifferentKey"));
    }

    @Test
    void testGetValue() {
        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);

        Assertions.assertEquals("value", context.get("key"));
    }

    @Test
    void testHasNullParent() {
        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);

        Assertions.assertFalse(context.has("differentKey"));
    }

    @Test
    void testHasParentCorrectKey() {
        final Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "value");

        final SimpleContext parentContext = new SimpleContext(null, parentVars);

        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);

        Assertions.assertTrue(context.has("differentKey"));
    }

    @Test
    void testHasParentWrongKey() {
        final Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("key", "value");

        final SimpleContext parentContext = new SimpleContext(null, parentVars);

        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);

        Assertions.assertFalse(context.has("differentKey"));
    }

    @Test
    void testHasTrue() {
        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);

        Assertions.assertTrue(context.has("key"));
    }

    @Test
    void testNestedEffectiveContextMapWrappingFails() {
        final SimpleContext rootContext = new SimpleContext();
        rootContext.set("key", "root");
        final SimpleContext rootEffectiveContext = new SimpleContext(rootContext, new EffectiveContextMap(rootContext));
        final SimpleContext parentContext = new SimpleContext(rootEffectiveContext);
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new EffectiveContextMap(parentContext),
                "Nested EffectiveContextMap wrapping should fail"
        );
    }

    @Test
    void testSetVarsChangeValue() {
        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);

        context.set("key", "newValue");

        Assertions.assertEquals("newValue", context.get("key"));
    }

    @Test
    void testSetVarsEmpty() {
        final Map<String, Object> vars = new HashMap<>();
        context.setVars(vars);

        context.set("key", "newValue");

        Assertions.assertEquals("newValue", context.get("key"));
    }

    @Test
    void testSetVarsParent() {
        final Map<String, Object> parentVars = new HashMap<>();
        parentVars.put("differentKey", "differentValue");

        final SimpleContext parentContext = new SimpleContext(null, parentVars);

        final Map<String, Object> vars = new HashMap<>();
        vars.put("key", "value");

        context.setVars(vars);
        context = new SimpleContext(parentContext, parentVars);

        context.set("differentKey", "newValue");

        Assertions.assertEquals("newValue", context.get("differentKey"));
    }
}
