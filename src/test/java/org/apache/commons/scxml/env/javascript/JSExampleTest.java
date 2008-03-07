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

package org.apache.commons.scxml.env.javascript;

import java.net.URL;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;

/**
 * SCXML application for the example JavaScript scripts.
 *
 */
public class JSExampleTest extends TestCase {

    public JSExampleTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(JSExampleTest.class);
        suite.setName("SCXML JavaScript Environment Example Tests");
        return suite;
    }

    // Test data
    private URL example01;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        example01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/javascript/example-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        example01 = null;
    }

    // TEST METHODS

    public void testExample01Sample() {
        
		SCXML scxml = SCXMLTestHelper.parse(example01);
		Evaluator evaluator = new JSEvaluator();
        Context context = new JSContext();
		exec = SCXMLTestHelper.getExecutor(scxml, context, evaluator);

        assertNotNull(exec);
        try {
            Set currentStates = exec.getCurrentStatus().getStates();
            assertEquals(1, currentStates.size());
            assertEquals("end", ((State)currentStates.iterator().
                next()).getId());
        } catch (Exception e) {
            fail(e.getMessage());
        }
   }

}

