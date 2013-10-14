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

import junit.framework.TestCase;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.env.jsp.ELContext;
import org.apache.commons.scxml.env.jsp.ELEvaluator;
/**
 * Unit tests {@link org.apache.commons.scxml.model.Assign}.
 * Unit tests {@link org.apache.commons.scxml.model.Cancel}.
 * Unit tests {@link org.apache.commons.scxml.model.Else}.
 * Unit tests {@link org.apache.commons.scxml.model.ElseIf}.
 * Unit tests {@link org.apache.commons.scxml.model.Exit}.
 * Unit tests {@link org.apache.commons.scxml.model.If}.
 * Unit tests {@link org.apache.commons.scxml.model.Log}.
 * Unit tests {@link org.apache.commons.scxml.model.Send}.
 * Unit tests {@link org.apache.commons.scxml.model.Var}.
 */
public class ActionsTest extends TestCase {
    /**
     * Construct a new instance of ActionsTest with
     * the specified name
     */
    public ActionsTest(String name) {
        super(name);
    }

    // Test data
    private URL actionsSample01, actionsSample02, actionsSample03;
    private ELEvaluator evaluator;
    private ELContext ctx;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        actionsSample01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/actions-state-test.xml");
        actionsSample02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/actions-parallel-test.xml");
        actionsSample03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/model/actions-initial-test.xml");
        evaluator = new ELEvaluator();
        ctx = new ELContext();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        actionsSample01 = actionsSample02 = actionsSample03 = null;
        evaluator = null;
        ctx = null;
        exec = null;
    }

    /**
     * Test the implementation
     */
    public void testStateActions() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(actionsSample01);
        runTest(scxml);
    }

    public void testParallelActions() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(actionsSample02);
        runTest(scxml);
    }

    public void testInitialActions() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(actionsSample03);
        runTest(scxml);
    }

    private void runTest(SCXML scxml) throws Exception {
        exec = SCXMLTestHelper.getExecutor(scxml, ctx, evaluator);
        ELContext ctx = (ELContext) SCXMLTestHelper.lookupContext(exec,
            "actionsTest");
        assertEquals((String) ctx.get("foo"), "foobar");
        assertEquals("Missed event transition",
            "true", (String) ctx.get("eventsent"));
    }
}

