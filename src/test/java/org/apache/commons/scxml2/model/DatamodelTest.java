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
package org.apache.commons.scxml2.model;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class DatamodelTest {

    // Test data
    private URL datamodel01jexl, datamodel02jexl, datamodel04jexl, datamodel05jexl;
    private SCXMLExecutor exec01, exec02;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        datamodel01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/datamodel-01.xml");
        datamodel02jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/datamodel-02.xml");
        datamodel04jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/datamodel-04.xml");
        datamodel05jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/datamodel-05.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        datamodel01jexl = datamodel02jexl = datamodel04jexl = datamodel05jexl = null;
    }

    /**
     * Test the stateless model, simultaneous executions
     */    
    @Test
    public void testDatamodelSimultaneousJexl() throws Exception {
        exec01 = SCXMLTestHelper.getExecutor(datamodel01jexl,
            new JexlContext(), new JexlEvaluator());
        Assert.assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(datamodel01jexl,
            new JexlContext(), new JexlEvaluator());
        Assert.assertNotNull(exec02);
        Assert.assertFalse(exec01 == exec02);
        runtest();
    }
    
    @Test
    public void testDatamodelNamespacePrefixedXPaths() throws Exception {
        exec01 = SCXMLTestHelper.getExecutor(datamodel02jexl,
            new JexlContext(), new JexlEvaluator());
        Assert.assertNotNull(exec01);
        exec02 = SCXMLTestHelper.getExecutor(datamodel02jexl,
                new JexlContext(), new JexlEvaluator());
        Assert.assertNotNull(exec02);
        Assert.assertFalse(exec01 == exec02);
        runtest();
    }
    
    @Test
    public void testDatamodel04Jexl() throws Exception {
        exec01 = SCXMLTestHelper.getExecutor(datamodel04jexl,
            new JexlContext(), new JexlEvaluator());
        Assert.assertNotNull(exec01);
        Set<EnterableState> currentStates = exec01.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("one", "1");
        payload.put("two", "2");
        TriggerEvent te = new TriggerEvent("ten.done", TriggerEvent.SIGNAL_EVENT, payload);
        SCXMLTestHelper.fireEvent(exec01, te);
        currentStates = exec01.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        SCXMLTestHelper.fireEvent(exec01, "twenty.done");
        currentStates = exec01.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
    }
    
    @Test
    public void testDatamodel05Jexl() throws Exception {
        exec01 = SCXMLTestHelper.getExecutor(datamodel05jexl);
        Assert.assertNotNull(exec01);
        SCXMLTestHelper.assertState(exec01, "end");
    }

    private void runtest() throws Exception {
        //// Interleaved
        // exec01
        Set<EnterableState> currentStates = exec01.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        exec01 = SCXMLTestHelper.testInstanceSerializability(exec01);
        currentStates = fireEvent("ten.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        // exec02
        currentStates = exec02.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("twenty.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
        exec01 = SCXMLTestHelper.testInstanceSerializability(exec01);
        // exec02
        currentStates = fireEvent("ten.done", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        exec02 = SCXMLTestHelper.testInstanceSerializability(exec02);
        currentStates = fireEvent("twenty.done", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
        currentStates = fireEvent("thirty.done", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("forty", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("thirty.done", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("forty", currentStates.iterator().next().getId());
    }

    private Set<EnterableState> fireEvent(String name, SCXMLExecutor exec) throws Exception {
        TriggerEvent[] evts = {new TriggerEvent(name,
                TriggerEvent.SIGNAL_EVENT, null)};
        exec.triggerEvents(evts);
        return exec.getCurrentStatus().getStates();
    }
}

