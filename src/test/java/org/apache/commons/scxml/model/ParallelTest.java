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

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ParallelTest extends TestCase {

    public ParallelTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(ParallelTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { ParallelTest.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    // Test data
    private URL parallel01, parallel02;
    private SCXMLExecutor exec;
    
    public void setUp() {
        parallel01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/parallel-01.xml");
        parallel02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/parallel-02.xml");
    }

    public void tearDown() {
        parallel01 = parallel02 = null;
        exec = null;
    }

    public void testParallel01() {
    	SCXML scxml = SCXMLTestHelper.parse(parallel01);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        try {
            SCXMLTestHelper.assertPostTriggerState(exec, "foo", "end");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void testParallel02() {
    	SCXML scxml = SCXMLTestHelper.parse(parallel02);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        try {
        	SCXMLTestHelper.assertPostTriggerStates(exec, "dummy.event", new String[] { "state01", "state02" });
            SCXMLTestHelper.assertPostTriggerState(exec, "event1", "state1");
        } catch (Exception e) {
        	e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
