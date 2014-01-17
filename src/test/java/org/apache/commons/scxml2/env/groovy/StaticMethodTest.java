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
package org.apache.commons.scxml2.env.groovy;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StaticMethodTest {

    private URL staticmethod;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        staticmethod = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/groovy/static-method.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        staticmethod = null;
    }
    
    @Test
    public void testGroovyStaticMethodInvocation() throws Exception {
        Context jc = new GroovyContext();
        jc.set("System", System.class);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(staticmethod,
                jc, new GroovyEvaluator());
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("static", currentStates.iterator().next().getId());
    }

}
