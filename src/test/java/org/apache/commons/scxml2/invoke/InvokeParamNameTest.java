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

import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.model.ModelException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Tests for 4.3.1 in WD-scxml-20080516
public class InvokeParamNameTest extends TestCase {
    
    public InvokeParamNameTest(String testName) {
        super(testName);
    }

    private URL invoker04;
    private SCXMLExecutor exec;

    static String lastSource;
    static Map<String, Object> lastParams;
    
    @Override
    public void setUp() throws Exception {
        invoker04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/invoke/invoker-04.xml");
        exec = SCXMLTestHelper.getExecutor(invoker04,
                new JexlContext(), new JexlEvaluator());
        exec.registerInvokerClass("x-test", DummyInvoker.class);
    }
    
    @Override
    public void tearDown() {
        exec.unregisterInvokerClass("x-test");    
        invoker04 = null;
    }
    
    private void trigger() throws ModelException {
        lastParams = null;
        lastSource = null;
        exec.triggerEvent(new TriggerEvent("test.trigger",
            TriggerEvent.SIGNAL_EVENT)); 
    }
    
    // Tests "param" element with "name" and "expr" attribute
    public void testNameAndExpr() throws Exception {
        trigger();
        assertTrue(lastSource.endsWith("TestSrc"));
        final Map.Entry<String, Object> e =
            lastParams.entrySet().iterator().next();
        assertEquals("ding", e.getKey());
        assertEquals("foo", e.getValue());
    }

    // Tests "param" element with only a "name" attribute 
    public void testSoleNameLocation() throws Exception {
        trigger(); trigger();
        final Element e = (Element)lastParams.values().iterator().next();
        assertNotNull(e);
        assertEquals("bar", e.getNodeName());
        assertEquals(Node.TEXT_NODE, e.getFirstChild().getNodeType());
        assertEquals("foo", e.getFirstChild().getNodeValue());
    }

    // Tests "param" element with a single, wrong "name" attribute
    public void testWrongNameLocation() throws Exception {
        trigger(); trigger(); trigger();
        assertEquals(1, exec.getCurrentStatus().getEvents().size());
        final TriggerEvent evt = exec.getCurrentStatus().getEvents().iterator().next(); 
        assertTrue(evt.getName().endsWith("error.illegalalloc"));
    }

    public static class DummyInvoker implements Invoker {

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

        public void parentEvents(TriggerEvent[] evts) throws InvokerException {
            // Not needed
        }

        public void setParentStateId(String parentStateId) {
            // Not needed    
        }

        public void setSCInstance(SCInstance scInstance) {
            // Not needed    
        }
    }

}
