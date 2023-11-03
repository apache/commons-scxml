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

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.env.SimpleErrorReporter;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.SCXML;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 * Testing <invoke>
 */
public class InvokeTest {

    @Test
    public void testExecuteInvokeAfterAllInternalEventsAreProcessed() throws Exception {
        final SCXML scxml = SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/invoke/invoker-05.xml"));
        final SCXMLExecutor exec = new SCXMLExecutor(null, new SimpleDispatcher(), new SimpleErrorReporter());
        exec.setStateMachine(scxml);
        exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
        exec.go();
        while (exec.isRunning()) {
            exec.triggerEvents();
        }
        Assertions.assertEquals("success", exec.getStatus().getStates().iterator().next().getId());
    }

    /**
     * Test the SCXML documents, usage of &lt;invoke&gt;
     */
    @Test
    public void testInvoke01Sample() throws Exception {
        final SCXML scxml = SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/invoke/invoker-01.xml"));
        final SCXMLExecutor exec = new SCXMLExecutor(null, new SimpleDispatcher(), new SimpleErrorReporter());
        exec.setStateMachine(scxml);
        exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("invoker", currentStates.iterator().next().getId());
    }

    @Test
    public void testInvoke02Sample() throws Exception {
        final SCXML scxml = SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/invoke/invoker-02.xml"));
        final SCXMLExecutor exec = new SCXMLExecutor(null, new SimpleDispatcher(), new SimpleErrorReporter());
        exec.setStateMachine(scxml);
        exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
    }

    @Test
    public void testInvoke03Sample() throws Exception {
        final SCXML scxml = SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/invoke/invoker-03.xml"));
        final SCXMLExecutor exec = new SCXMLExecutor(null, new SimpleDispatcher(), new SimpleErrorReporter());
        exec.setStateMachine(scxml);
        exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        SCXMLTestHelper.fireEvent(exec, "s1.next");
        SCXMLTestHelper.fireEvent(exec, "state1.next");
    }
}

