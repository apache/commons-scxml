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

package org.apache.commons.scxml2.env.xpath;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SCXML application for the XPath example.
 *
 */
public class XPathExampleTest {

    // Test data
    private URL example01, example02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        example01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/xpath/example-01.xml");
        example02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/xpath/example-02.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        example01 = example02 = null;
    }

    // TEST METHODS
    @Test
    public void testExample01Sample() throws Exception {

        SCXML scxml = SCXMLTestHelper.parse(example01);
        Evaluator evaluator = null;
        evaluator = new XPathEvaluator();
        Context context = new XPathContext(null);
        exec = SCXMLTestHelper.getExecutor(scxml, context, evaluator);

        Assert.assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("mid", ((State)currentStates.iterator().
            next()).getId());

        String payload = "<test xmlns=''><status>complete</status></test>";
        SCXMLTestHelper.assertPostTriggerState(exec,
            new TriggerEvent("foo", TriggerEvent.SIGNAL_EVENT,
                SCXMLTestHelper.stringToXMLDocument(payload)),
            "end");

    }
    
    @Test
    public void testExample02Sample() throws Exception {

        SCXML scxml = SCXMLTestHelper.parse(example02);
        Evaluator evaluator = null;
        evaluator = new XPathEvaluator();
        Context context = new XPathContext(null);
        exec = SCXMLTestHelper.getExecutor(scxml, context, evaluator);

        Assert.assertNotNull(exec);
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("end", ((State)currentStates.iterator().
            next()).getId());

    }

}

