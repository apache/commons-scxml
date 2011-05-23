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
package org.apache.commons.scxml.env.jsp;

import java.net.URL;

import javax.servlet.jsp.JspContext;

import junit.framework.TestCase;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;

/**
 * Unit tests {@link org.apache.commons.scxml.env.jsp.RootContext}.
 */
public class RootContextTest extends TestCase {
    /**
     * Construct a new instance of ActionsTest with
     * the specified name
     */
    public RootContextTest(String name) {
        super(name);
    }

    // Test data
    private URL rootCtxSample;
    private ELEvaluator evaluator;
    private JspContext jspCtx;
    private RootContext rootCtx;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        rootCtxSample = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/jsp-rootctx-test.xml");
        evaluator = new ELEvaluator();
        jspCtx = new MockJspContext();
        jspCtx.setAttribute("foo", "1");
        rootCtx = new RootContext(jspCtx);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        rootCtxSample = null;
        evaluator = null;
        jspCtx = null;
        rootCtx = null;
        exec = null;
    }

    /**
     * Test the implementation
     */
    public void testRootContext() throws Exception {
        assertEquals("1", String.valueOf(rootCtx.get("foo")));
        exec = SCXMLTestHelper.getExecutor(rootCtxSample, rootCtx, evaluator);
        assertEquals("1", String.valueOf(jspCtx.getAttribute("foo")));
        assertEquals("2", String.valueOf(rootCtx.get("foo")));
        assertNull(jspCtx.getAttribute("bar"));
        ELContext ctx = (ELContext) SCXMLTestHelper.lookupContext(exec,
            "rootCtxTest");
        assertNotNull(ctx);
        assertNotNull(ctx.get("bar"));
        assertNull(jspCtx.getVariableResolver().resolveVariable("bar"));
        assertNotNull(ctx.resolveVariable("bar"));
        assertEquals(ctx.resolveVariable("bar"), "a brand new value");
        assertNotNull(ctx.getVars());
    }

}

