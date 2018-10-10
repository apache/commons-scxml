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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.StateConfiguration;
import org.apache.commons.scxml2.Status;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.system.EventVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavaScriptEngineTest {

    private JSEvaluator evaluator;
    private StateConfiguration stateConfiguration;
    private JSContext _systemContext;
    private JSContext context;

    @BeforeEach
    public void before() {
        evaluator = new JSEvaluator();
        _systemContext = new JSContext();
        SCXMLSystemContext systemContext = new SCXMLSystemContext(_systemContext);
        _systemContext.set(SCXMLSystemContext.SESSIONID_KEY, UUID.randomUUID().toString());
        _systemContext.set(SCXMLSystemContext.SCXML_NAME_KEY, "test");
        stateConfiguration = new StateConfiguration();
        systemContext.getPlatformVariables().put(SCXMLSystemContext.STATUS_KEY, new Status(stateConfiguration));
        context = new JSContext(systemContext);
    }

    @Test
    public void testInitScxmlSystemContext() throws Exception {
        assertEquals("test", evaluator.eval(context, "_name"));
    }

    @Test
    public void testScxmlEvent() throws Exception {
        assertTrue(evaluator.evalCond(context, "_event === undefined"));
        EventVariable event = new EventVariable("myEvent", EventVariable.TYPE_INTERNAL, null, null, null, null,"myData");
        _systemContext.setLocal(SCXMLSystemContext.EVENT_KEY, event);
        assertFalse(evaluator.evalCond(context, "_event === undefined"));
        assertTrue(evaluator.evalCond(context, "_event.name == 'myEvent'"));
        assertTrue(evaluator.evalCond(context, "_event.type == 'internal'"));
        assertTrue(evaluator.evalCond(context, "_event.data == 'myData'"));
        assertTrue(evaluator.evalCond(context, "_event.origin === undefined"));
    }

    @Test
    public void testScxmlInPredicate() throws Exception {
        assertFalse(evaluator.evalCond(context, "In('foo')"));
        Final foo = new Final();
        foo.setId("foo");
        stateConfiguration.enterState(foo);
        assertTrue(evaluator.evalCond(context, "In('foo')"));
    }

    @Test
    public void testCopyJavscriptGlobalsToScxmlContext() throws Exception {
        assertFalse(context.has("x"));
        evaluator.eval(context, ("x=3"));
        assertEquals(3, context.get("x"));
    }

    @Test
    public void testSharedJavscriptGlobalsRetainedAcrossInvocation() throws Exception {
        assertFalse(context.has("x"));
        evaluator.eval(context, ("x=3"));
        context.getVars().remove("x");
        assertFalse(context.has("x"));
        assertTrue(evaluator.evalCond(context, "x===3"));
    }

    @Test
    public void testJavscriptGlobalsNotRetainedAcrossEvaluatorInstances() throws Exception {
        assertFalse(context.has("x"));
        evaluator.eval(context, ("x=3"));
        assertEquals(3, context.get("x"));
        context.getVars().remove("x");
        assertFalse(context.has("x"));
        evaluator = new JSEvaluator();
        assertTrue(evaluator.evalCond(context, "typeof x=='undefined'"));
    }
}
