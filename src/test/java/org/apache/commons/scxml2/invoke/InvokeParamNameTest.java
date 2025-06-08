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
package org.apache.commons.scxml2.invoke;

import java.util.Map;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLIOProcessor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.EventBuilder;
import org.apache.commons.scxml2.model.ModelException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Tests for 4.3.1 in WD-scxml-20080516
class InvokeParamNameTest {

    public static class DummyInvoker implements Invoker {

        private String invokeId;

        @Override
        public void cancel() {
            // Not needed
        }

        @Override
        public SCXMLIOProcessor getChildIOProcessor() {
            // not used
            return null;
        }

        @Override
        public String getInvokeId() {
            return invokeId;
        }

        @Override
        public void invoke(final String url, final Map<String, Object> params) {
            lastURL = url;
            lastParams = params;
        }

        @Override
        public void invokeContent(final String content, final Map<String, Object> params) {
            lastURL = null;
            lastParams = params;
        }

        public Map<String, Object> lastParams() {
            return lastParams;
        }

        public String lastURL() {
            return lastURL;
        }

        @Override
        public void parentEvent(final TriggerEvent evt) {
            // Not needed
        }

        @Override
        public void setInvokeId(final String invokeId) {
            this.invokeId = invokeId;
        }

        @Override
        public void setParentSCXMLExecutor(final SCXMLExecutor parentSCXMLExecutor) {
            // Not needed
        }
    }

    private static String lastURL;
    private static Map<String, Object> lastParams;

    private SCXMLExecutor exec;

    @BeforeEach
    public void setUp() throws Exception {
        exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/invoke/invoker-04.xml");
        exec.registerInvokerClass("x-test", DummyInvoker.class);
        exec.go();
    }

    @AfterEach
    public void tearDown() {
        exec.unregisterInvokerClass("x-test");
    }

    // Tests "param" element with "name" and "expr" attribute
    @Test
    void testNameAndExpr() throws Exception {
        trigger();
        Assertions.assertTrue(lastURL.endsWith("TestSrc"));
        final Map.Entry<String, Object> e =
            lastParams.entrySet().iterator().next();
        Assertions.assertEquals("ding", e.getKey());
        Assertions.assertEquals("foo", e.getValue());
    }

    // Tests "param" element with a "name" attribute and "expr" attribute locating a data id
    @Test
    void testSoleNameLocation() throws Exception {
        trigger(); trigger();
        final Map m = (Map)lastParams.values().iterator().next();
        Assertions.assertNotNull(m);
        Assertions.assertEquals("bar", m.keySet().iterator().next());
        Assertions.assertEquals("foo", m.get("bar"));
    }

    private void trigger() throws ModelException {
        lastParams = null;
        lastURL = null;
        exec.triggerEvent(new EventBuilder("test.trigger", TriggerEvent.SIGNAL_EVENT).build());
    }

}
