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
package org.apache.commons.scxml2.model;

import java.net.URL;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.model.Assign}.
 * Unit tests {@link org.apache.commons.scxml2.model.Cancel}.
 * Unit tests {@link org.apache.commons.scxml2.model.Else}.
 * Unit tests {@link org.apache.commons.scxml2.model.ElseIf}.
 * Unit tests {@link org.apache.commons.scxml2.model.If}.
 * Unit tests {@link org.apache.commons.scxml2.model.Log}.
 * Unit tests {@link org.apache.commons.scxml2.model.Send}.
 * Unit tests {@link org.apache.commons.scxml2.model.Var}.
 */
public class ActionsTest {

    // Test data
    private URL actionsSample01, actionsSample02, actionsSample03;
    private JexlEvaluator evaluator;
    private JexlContext ctx;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        actionsSample01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/model/actions-state-test.xml");
        actionsSample02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/model/actions-parallel-test.xml");
        actionsSample03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/model/actions-initial-test.xml");
        evaluator = new JexlEvaluator();
        ctx = new JexlContext();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        actionsSample01 = actionsSample02 = actionsSample03 = null;
        evaluator = null;
        ctx = null;
        exec = null;
    }

    /**
     * Test the implementation
     */    
    @Test
    public void testStateActions() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(actionsSample01);
        runTest(scxml);
    }
    
    @Test
    public void testParallelActions() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(actionsSample02);
        runTest(scxml);
    }
    
    @Test
    public void testInitialActions() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(actionsSample03);
        runTest(scxml);
    }

    private void runTest(SCXML scxml) throws Exception {
        exec = SCXMLTestHelper.getExecutor(scxml, ctx, evaluator);
        JexlContext ctx = (JexlContext) SCXMLTestHelper.lookupContext(exec,
            "actionsTest");
        Assert.assertEquals(ctx.get("foo"), "foobar");
        Assert.assertEquals("Missed event transition",
            true, ctx.get("eventsent"));
    }
}

