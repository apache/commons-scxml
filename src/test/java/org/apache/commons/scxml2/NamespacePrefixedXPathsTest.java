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
 * Unit tests for namespace prefixes in XPaths pointing bits in a &lt;data&gt;.
 */
public class NamespacePrefixedXPathsTest {

    // Test data
    private URL datamodel03jexl;
    private SCXMLExecutor exec01;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() throws Exception {
        datamodel03jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/datamodel-03.xml");
        exec01 = SCXMLTestHelper.getExecutor(datamodel03jexl);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        datamodel03jexl = null;
        exec01 = null;
    }

    /**
     * Test the XPath evaluation
     */
    // JEXL
    @Test
    public void testNamespacePrefixedXPathsJexl() throws Exception {
        runtest(exec01);
    }

    // Same test, since same documents (different expression languages)
    private void runtest(SCXMLExecutor exec) throws Exception {
        // must be in state "ten" at the onset
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("ten", currentStates.iterator().next().getId());

        // should move to "twenty"
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("twenty", currentStates.iterator().next().getId());

        // This is set while exiting "ten"
        Double retval = (Double) exec.getRootContext().get("retval");
        Assert.assertEquals(Double.valueOf("11"), retval);

        // On to "thirty"
        currentStates = SCXMLTestHelper.fireEvent(exec, "twenty.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("thirty", currentStates.iterator().next().getId());
        exec = SCXMLTestHelper.testInstanceSerializability(exec);

        // Tests XPath on SCXML actions, set while exiting "twenty"
        String retvalstr = (String) exec.getRootContext().get("retval");
        Assert.assertEquals("Equal to 20", retvalstr);

        // and so on ...
        currentStates = SCXMLTestHelper.fireEvent(exec, "thirty.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("forty", currentStates.iterator().next().getId());

        currentStates = SCXMLTestHelper.fireEvent(exec, "forty.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("fifty", currentStates.iterator().next().getId());

        currentStates = SCXMLTestHelper.fireEvent(exec, "fifty.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("sixty", (currentStates.iterator().
            next()).getId());

        currentStates = SCXMLTestHelper.fireEvent(exec, "sixty.done");
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("seventy", currentStates.iterator().next().getId());

        // done
        Assert.assertTrue(exec.getCurrentStatus().isFinal());
    }
}

