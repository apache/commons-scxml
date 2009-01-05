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
package org.apache.commons.scxml.env.jexl;

import java.net.URL;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.model.State;

public class StaticMethodTest extends TestCase {

    private URL staticmethod;
    
    public StaticMethodTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(StaticMethodTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = {StaticMethodTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        staticmethod = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/static-method.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        staticmethod = null;
    }

    public void testJexlStaticMethodInvocation() throws Exception {
        Context jc = new JexlContext();
        jc.set("System", System.class);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(staticmethod,
                jc, new JexlEvaluator());
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("static", ((State)currentStates.iterator().
            next()).getId());
    }

}
