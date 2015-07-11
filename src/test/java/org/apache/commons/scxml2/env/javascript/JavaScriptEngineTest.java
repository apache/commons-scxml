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
package org.apache.commons.scxml2.env.javascript;

import static org.junit.Assert.assertEquals;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.scxml2.Context;
import org.junit.Before;
import org.junit.Test;

public class JavaScriptEngineTest {

    private ScriptEngine engine;

    private Context context;

    @Before
    public void before() throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");
        context = new JSContext();
    }

    @Test
    public void testSimpleEvaluation() throws Exception {
        Object ret = engine.eval("1.0 + 2.0");
        assertEquals(3.0, ret);
    }

    @Test
    public void testBindingsInput() throws Exception {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("x", 1.0);
        bindings.put("y", 2.0);

        Object ret = engine.eval("x + y;", bindings);
        assertEquals(3.0, ret);
    }

    @Test
    public void testBindingsInput_WithJSBindings() throws Exception {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        JSBindings jsBindings = new JSBindings(context, bindings);
        jsBindings.put("x", 1.0);
        jsBindings.put("y", 2.0);

        Object ret = engine.eval("x + y;", jsBindings);
        assertEquals(3.0, ret);
    }

    @Test
    public void testBindingsGlobal() throws Exception {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("x", 1.0);
        bindings.put("y", 2.0);
        bindings.put("z", 0.0);

        engine.eval("z = x + y;", bindings);
        assertEquals("z variable is expected to set to 3.0 in global, but it was " + bindings.get("z") + ".",
                     3.0, bindings.get("z"));
    }

    @Test
    public void testBindingsGlobal_WithJSBindings() throws Exception {
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        JSBindings jsBindings = new JSBindings(context, bindings);
        jsBindings.put("x", 1.0);
        jsBindings.put("y", 2.0);
        jsBindings.put("z", 0.0);

        engine.eval("z = x + y;", jsBindings);
        assertEquals("z variable is expected to set to 3.0 in global, but it was " + jsBindings.get("z") + ".",
                     3.0, jsBindings.get("z"));
    }
}
