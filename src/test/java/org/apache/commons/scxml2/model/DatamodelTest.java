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
package org.apache.commons.scxml2.model;

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.EventBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 */
public class DatamodelTest {

    private Set<EnterableState> fireEvent(final String name, final SCXMLExecutor exec) throws Exception {
        final TriggerEvent[] evts = {new EventBuilder(name, TriggerEvent.SIGNAL_EVENT).build()};
        exec.triggerEvents(evts);
        return exec.getStatus().getStates();
    }

    private void runtest(SCXMLExecutor exec01, SCXMLExecutor exec02) throws Exception {
        //// Interleaved
        // exec01
        Set<EnterableState> currentStates = exec01.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        exec01 = SCXMLTestHelper.testInstanceSerializability(exec01);
        currentStates = fireEvent("done.state.ten", exec01);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
        // exec02
        currentStates = exec02.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("ten", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("done.state.twenty", exec01);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("thirty", currentStates.iterator().next().getId());
        exec01 = SCXMLTestHelper.testInstanceSerializability(exec01);
        // exec02
        currentStates = fireEvent("done.state.ten", exec02);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("twenty", currentStates.iterator().next().getId());
        exec02 = SCXMLTestHelper.testInstanceSerializability(exec02);
        currentStates = fireEvent("done.state.twenty", exec02);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("thirty", currentStates.iterator().next().getId());
        currentStates = fireEvent("done.state.thirty", exec02);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("forty", currentStates.iterator().next().getId());
        // exec01
        currentStates = fireEvent("done.state.thirty", exec01);
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("forty", currentStates.iterator().next().getId());
    }

    @Test
    public void testDatamodel05Groovy() throws Exception {
        final SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/datamodel-05.xml");
        exec01.go();
        SCXMLTestHelper.assertState(exec01, "end");
    }

    @Test
    public void testDatamodel05Javascript() throws Exception {
        final SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/javascript/datamodel-05.xml");
        exec01.go();
        SCXMLTestHelper.assertState(exec01, "end");
    }

    @Test
    public void testDatamodel05Jexl() throws Exception {
        final SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-05.xml");
        exec01.go();
        SCXMLTestHelper.assertState(exec01, "end");
    }

    /**
     * Test the stateless model (Groovy), simultaneous executions
     */
    @Test
    public void testDatamodelSimultaneousGroovy() throws Exception {
        final SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/datamodel-01.xml");
        exec01.go();
        final SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/datamodel-01.xml");
        exec02.go();
        Assertions.assertFalse(exec01 == exec02);
        runtest(exec01, exec02);
    }

    /**
     * Test the stateless model (Javascript), simultaneous executions
     */
    @Test
    public void testDatamodelSimultaneousJavascript() throws Exception {
        final SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/javascript/datamodel-01.xml");
        exec01.go();
        final SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/javascript/datamodel-01.xml");
        exec02.go();
        Assertions.assertFalse(exec01 == exec02);
        runtest(exec01, exec02);
    }

    /**
     * Test the stateless model (jexl), simultaneous executions
     */
    @Test
    public void testDatamodelSimultaneousJexl() throws Exception {
        final SCXMLExecutor exec01 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-01.xml");
        exec01.go();
        final SCXMLExecutor exec02 = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/jexl/datamodel-01.xml");
        exec02.go();
        Assertions.assertFalse(exec01 == exec02);
        runtest(exec01, exec02);
    }
}

