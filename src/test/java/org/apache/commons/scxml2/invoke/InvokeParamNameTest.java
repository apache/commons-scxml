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
package org.apache.commons.scxml2.invoke;

import java.util.Map;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLIOProcessor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.EventBuilder;
import org.apache.commons.scxml2.model.ModelException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// Tests for 4.3.1 in WD-scxml-20080516
public class InvokeParamNameTest {

    private SCXMLExecutor exec;

    private static String lastURL;
    private static Map<String, Object> lastParams;
    
    @Before
    public void setUp() throws Exception {
        exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/invoke/invoker-04.xml");
        exec.registerInvokerClass("x-test", DummyInvoker.class);
        exec.go();
    }
    
    @After
    public void tearDown() {
        exec.unregisterInvokerClass("x-test");    
    }
    
    private void trigger() throws ModelException {
        lastParams = null;
        lastURL = null;
        exec.triggerEvent(new EventBuilder("test.trigger", TriggerEvent.SIGNAL_EVENT).build());
    }
    
    // Tests "param" element with "name" and "expr" attribute    
    @Test
    public void testNameAndExpr() throws Exception {
        trigger();
        Assert.assertTrue(lastURL.endsWith("TestSrc"));
        final Map.Entry<String, Object> e =
            lastParams.entrySet().iterator().next();
        Assert.assertEquals("ding", e.getKey());
        Assert.assertEquals("foo", e.getValue());
    }

    // Tests "param" element with a "name" attribute and "expr" attribute locating a data id
    @Test
    public void testSoleNameLocation() throws Exception {
        trigger(); trigger();
        final Map m = (Map)lastParams.values().iterator().next();
        Assert.assertNotNull(m);
        Assert.assertEquals("bar", m.keySet().iterator().next());
        Assert.assertEquals("foo", m.get("bar"));
    }

    public static class DummyInvoker implements Invoker {

        private String invokeId;

        @Override
        public void invoke(String url, Map<String, Object> params)
        throws InvokerException {
            lastURL = url;
            lastParams = params;
        }

        @Override
        public void invokeContent(String content, Map<String, Object> params)
                throws InvokerException {
            lastURL = null;
            lastParams = params;
        }

        public String lastURL() {
            return lastURL;
        }

        public Map<String, Object> lastParams() {
            return lastParams;
        }

        @Override
        public void cancel() throws InvokerException {
            // Not needed
        }

        @Override
        public void parentEvent(TriggerEvent evt) throws InvokerException {
            // Not needed
        }

        @Override
        public String getInvokeId() {
            return invokeId;
        }

        @Override
        public void setInvokeId(String invokeId) {
            this.invokeId = invokeId;
        }

        @Override
        public void setParentSCXMLExecutor(SCXMLExecutor parentSCXMLExecutor) {
            // Not needed
        }

        @Override
        public SCXMLIOProcessor getChildIOProcessor() {
            // not used
            return null;
        }
    }

}
