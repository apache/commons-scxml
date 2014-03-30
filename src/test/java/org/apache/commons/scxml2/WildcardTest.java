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
package org.apache.commons.scxml2;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.model.EnterableState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.SCXMLExecutor}.
 * Testing wildcard event matching (*)
 */
public class WildcardTest {

    // Test data
    private URL wildcard01, wildcard02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        wildcard01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/wildcard-01.xml");
        wildcard02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/wildcard-02.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        wildcard01 = wildcard02 = null;
    }

    /**
     * Test the SCXML documents, usage of "_event.data"
     */
    @Test
    public void testWildcard01Sample() throws Exception {
    	exec = SCXMLTestHelper.getExecutor(wildcard01);
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state1", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "foo.bar.baz");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state4", currentStates.iterator().next().getId());
    }

    @Test
    public void testWildcard02Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(SCXMLTestHelper.parse(wildcard02));
        Assert.assertNotNull(exec);
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("state2", currentStates.iterator().next().getId());
    }
}
