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
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.ModelException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Tests for 4.3.1 in WD-scxml-20080516
public class InvokeParamNameTest {

    private SCXMLExecutor exec;

    static String lastSource;
    static Map<String, Object> lastParams;
    
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
        lastSource = null;
        exec.triggerEvent(new TriggerEvent("test.trigger",
            TriggerEvent.SIGNAL_EVENT)); 
    }
    
    // Tests "param" element with "name" and "expr" attribute    
    @Test
    public void testNameAndExpr() throws Exception {
        trigger();
        Assert.assertTrue(lastSource.endsWith("TestSrc"));
        final Map.Entry<String, Object> e =
            lastParams.entrySet().iterator().next();
        Assert.assertEquals("ding", e.getKey());
        Assert.assertEquals("foo", e.getValue());
    }

    // Tests "param" element with a "name" attribute and "expr" attribute locating a node
    @Test
    public void testSoleNameLocation() throws Exception {
        trigger(); trigger();
        final Element e = (Element)lastParams.values().iterator().next();
        Assert.assertNotNull(e);
        Assert.assertEquals("bar", e.getNodeName());
        Assert.assertEquals(Node.TEXT_NODE, e.getFirstChild().getNodeType());
        Assert.assertEquals("foo", e.getFirstChild().getNodeValue());
    }

    public static class DummyInvoker implements Invoker {

        private String invokeId;

        public void invoke(String source, Map<String, Object> params)
        throws InvokerException {
            lastSource = source;
            lastParams = params;
        }

        public String lastSource() {
            return lastSource;
        }

        public Map<String, Object> lastParams() {
            return lastParams;
        }

        public void cancel() throws InvokerException {
            // Not needed
        }

        public void parentEvent(TriggerEvent evt) throws InvokerException {
            // Not needed
        }

        public String getInvokeId() {
            return invokeId;
        }

        public void setInvokeId(String invokeId) {
            this.invokeId = invokeId;
        }

        public void setParentSCXMLExecutor(SCXMLExecutor parentSCXMLExecutor) {
            // Not needed
        }
    }

}
