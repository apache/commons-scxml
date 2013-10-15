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
package org.apache.commons.scxml2.env.rhino;

import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;

public class RhinoExampleTest extends TestCase {

    public RhinoExampleTest(String name) {
        super(name);
    }

    // Test data
    private URL example01;
    private SCXMLExecutor exec;

    @Override
    protected void setUp() throws Exception {
        example01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/rhino/example-01.xml");
    }

    @Override
    protected void tearDown() throws Exception {
        example01 = null;
    }

    // TEST METHODS

    public void testExample01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(example01, new RhinoEvaluator());
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ten", ((State)currentStates.iterator().next()).getId());
        SCXMLTestHelper.assertPostTriggerState(exec, "ten.done", "twenty");
        SCXMLTestHelper.assertPostTriggerState(exec, "twenty.done", "thirty");
        SCXMLTestHelper.assertPostTriggerState(exec, "thirty.done", "forty");
    }

}

