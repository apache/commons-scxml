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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.junit.Assert;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class DatamodelTest {

    /**
     * Test the stateless model, simultaneous executions
     */    
    @Test
    public void testDatamodelSimultaneousJexl() throws Exception {
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-01.xml");
        exec01.go();
        SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-01.xml");
        exec02.go();
        Assert.assertFalse(exec01 == exec02);
        runtest(exec01, exec02);
    }
    
    @Test
    public void testDatamodelNamespacePrefixedXPaths() throws Exception {
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-02.xml");
        exec01.go();
        SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-02.xml");
        exec02.go();
        Assert.assertFalse(exec01 == exec02);
        runtest(exec01, exec02);
    }
    
    @Test
    public void testDatamodel04Jexl() throws Exception {
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-04.xml");
        exec01.go();
        Set<EnterableState> currentStates = exec01.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("one", "1");
        payload.put("two", "2");
        TriggerEvent te = new TriggerEvent("done.state.ten", TriggerEvent.SIGNAL_EVENT, payload);
        SCXMLTestHelper.fireEvent(exec01, te);
        currentStates = exec01.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        SCXMLTestHelper.fireEvent(exec01, "done.state.twenty");
        currentStates = exec01.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
    }
    
    @Test
    public void testDatamodel05Jexl() throws Exception {
        SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-05.xml");
        exec01.go();
        SCXMLTestHelper.assertState(exec01, "end");
    }

    private void runtest(SCXMLExecutor exec01, SCXMLExecutor exec02) throws Exception {
        //// Interleaved
        // exec01
        Set<EnterableState> currentStates = exec01.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        exec01 = SCXMLTestHelper.testInstanceSerializability(exec01);
        currentStates = fireEvent("done.state.ten", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        // exec02
        currentStates = exec02.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("done.state.twenty", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
        exec01 = SCXMLTestHelper.testInstanceSerializability(exec01);
        // exec02
        currentStates = fireEvent("done.state.ten", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());
        exec02 = SCXMLTestHelper.testInstanceSerializability(exec02);
        currentStates = fireEvent("done.state.twenty", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
        currentStates = fireEvent("done.state.thirty", exec02);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("forty", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("done.state.thirty", exec01);
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("forty", currentStates.iterator().next().getId());
    }

    private Set<EnterableState> fireEvent(String name, SCXMLExecutor exec) throws Exception {
        TriggerEvent[] evts = {new TriggerEvent(name,
                TriggerEvent.SIGNAL_EVENT, null)};
        exec.triggerEvents(evts);
        return exec.getStatus().getStates();
    }
}

