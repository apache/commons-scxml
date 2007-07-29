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
package org.apache.commons.scxml.model;

import java.net.URL;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
/**
 * Unit tests for &lt;assign&gt; element, particular the "src" attribute.
 */
public class AssignTest extends TestCase {
    /**
     * Construct a new instance of AssignTest with
     * the specified name
     */
    public AssignTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AssignTest.class);
        suite.setName("SCXML Model Assign Tests");
        return suite;
    }

    // Test data
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        URL assignSample = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/assign-test.xml");
        exec = SCXMLTestHelper.getExecutor(assignSample);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        exec = null;
    }

    /**
     * Test the implementation
     */
    public void testAssignSrc() {
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("assign3", ((State)currentStates.iterator().
            next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

     public static void main(String args[]) {
        TestRunner.run(suite());
    }
}

