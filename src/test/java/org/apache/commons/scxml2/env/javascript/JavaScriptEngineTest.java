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
package org.apache.commons.scxml2.env.javascript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.StateConfiguration;
import org.apache.commons.scxml2.Status;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.system.EventVariable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JavaScriptEngineTest {

    private JSEvaluator evaluator;
    private StateConfiguration stateConfiguration;
    private JSContext systemContext;
    private JSContext context;

    @BeforeEach
    public void before() {
        evaluator = new JSEvaluator();
        systemContext = new JSContext();
        final SCXMLSystemContext localSystemContext = new SCXMLSystemContext(systemContext);
        systemContext.set(SCXMLSystemContext.SESSIONID_KEY, UUID.randomUUID().toString());
        systemContext.set(SCXMLSystemContext.SCXML_NAME_KEY, "test");
        stateConfiguration = new StateConfiguration();
        localSystemContext.getPlatformVariables().put(SCXMLSystemContext.STATUS_KEY, new Status(stateConfiguration));
        context = new JSContext(localSystemContext);
    }

    @Test
    void testCopyJavscriptGlobalsToScxmlContext() throws Exception {
        assertFalse(context.has("x"));
        evaluator.eval(context, "x=3");
        assertEquals(3, context.get("x"));
    }

    @Test
    void testInitScxmlSystemContext() throws Exception {
        assertEquals("test", evaluator.eval(context, "_name"));
    }

    @Test
    void testJavscriptGlobalsNotRetainedAcrossEvaluatorInstances() throws Exception {
        assertFalse(context.has("x"));
        evaluator.eval(context, "x=3");
        assertEquals(3, context.get("x"));
        context.getVars().remove("x");
        assertFalse(context.has("x"));
        evaluator = new JSEvaluator();
        assertTrue(evaluator.evalCond(context, "typeof x=='undefined'"));
    }

    @Test
    void testScxmlEvent() throws Exception {
        assertTrue(evaluator.evalCond(context, "_event === undefined"));
        final EventVariable event = new EventVariable("myEvent", EventVariable.TYPE_INTERNAL, null, null, null, null,"myData");
        systemContext.setLocal(SCXMLSystemContext.EVENT_KEY, event);
        assertFalse(evaluator.evalCond(context, "_event === undefined"));
        assertTrue(evaluator.evalCond(context, "_event.name == 'myEvent'"));
        assertTrue(evaluator.evalCond(context, "_event.type == 'internal'"));
        assertTrue(evaluator.evalCond(context, "_event.data == 'myData'"));
        assertTrue(evaluator.evalCond(context, "_event.origin === undefined"));
    }

    @Test
    void testScxmlInPredicate() throws Exception {
        assertFalse(evaluator.evalCond(context, "In('foo')"));
        final Final foo = new Final();
        foo.setId("foo");
        stateConfiguration.enterState(foo);
        assertTrue(evaluator.evalCond(context, "In('foo')"));
    }

    @Test
    void testSharedJavscriptGlobalsRetainedAcrossInvocation() throws Exception {
        assertFalse(context.has("x"));
        evaluator.eval(context, "x=3");
        context.getVars().remove("x");
        assertFalse(context.has("x"));
        assertTrue(evaluator.evalCond(context, "x===3"));
    }
}
