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
package org.apache.commons.scxml2.issues;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.EnterableState;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for issue 62.
 * FIXED
 */
public class Issue62Test {

    private URL test01, test02, test03;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        test01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/issues/issue62-01.xml");
        test02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/issues/issue62-02.xml");
        test03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/issues/issue62-03.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        test01 = test02 = null;
        exec = null;
    }
    
    @Test
    public void test01issue62() throws Exception {
        exec = SCXMLTestHelper.getExecutor(test01);
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("s1.1", currentStates.iterator().next().getId());
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "s1.1");
    }
    
    @Test
    public void test02issue62() throws Exception {
        exec = SCXMLTestHelper.getExecutor(test02);
        fragmenttest();
    }
    
    @Test
    public void test03issue62() throws Exception {
        exec = SCXMLTestHelper.getExecutor(SCXMLTestHelper.parse(test03));
        fragmenttest();
    }

    private void fragmenttest() throws Exception {
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("s1", currentStates.iterator().next().getId());
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "e1.1.1");
        SCXMLTestHelper.assertPostTriggerState(exec, "bar", "e1.1.2");
        SCXMLTestHelper.assertPostTriggerState(exec, "baz", "s3");
        Assert.assertTrue(exec.getCurrentStatus().isFinal());
    }
}

