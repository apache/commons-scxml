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
package org.apache.commons.scxml;

import java.net.URL;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.commons.scxml.model.TransitionTarget;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLExecutor}.
 * Testing wildcard event matching (*)
 */
public class WildcardTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public WildcardTest(String name) {
        super(name);
    }

    // Test data
    private URL wildcard01, wildcard02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        wildcard01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/wildcard-01.xml");
        wildcard02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/wildcard-02.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        wildcard01 = wildcard02 = null;
    }

    /**
     * Test the SCXML documents, usage of "_eventdata"
     */
    public void testWildcard01Sample() throws Exception {
    	exec = SCXMLTestHelper.getExecutor(wildcard01);
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state1", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "foo.bar.baz");
        assertEquals(1, currentStates.size());
        assertEquals("state4", currentStates.iterator().next().getId());
    }

    public void testWildcard02Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(SCXMLTestHelper.parse(wildcard02));
        assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("state2", currentStates.iterator().next().getId());
    }
}
