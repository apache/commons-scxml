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
package org.apache.commons.scxml2.env.jsp;

import java.net.URL;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.jsp.JspContext;

/**
 * Unit tests {@link org.apache.commons.scxml2.env.jsp.RootContext}.
 */
public class RootContextTest {

    // Test data
    private URL rootCtxSample;
    private ELEvaluator evaluator;
    private JspContext jspCtx;
    private RootContext rootCtx;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        rootCtxSample = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jsp/jsp-rootctx-test.xml");
        evaluator = new ELEvaluator();
        jspCtx = new MockJspContext();
        jspCtx.setAttribute("foo", "1");
        rootCtx = new RootContext(jspCtx);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
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
    @Test
    public void testRootContext() throws Exception {
        Assert.assertEquals("1", String.valueOf(rootCtx.get("foo")));
        exec = SCXMLTestHelper.getExecutor(rootCtxSample, rootCtx, evaluator);
        Assert.assertEquals("1", String.valueOf(jspCtx.getAttribute("foo")));
        Assert.assertEquals("2", String.valueOf(rootCtx.get("foo")));
        Assert.assertNull(jspCtx.getAttribute("bar"));
        ELContext ctx = (ELContext) SCXMLTestHelper.lookupContext(exec,
            "rootCtxTest");
        Assert.assertNotNull(ctx);
        Assert.assertNotNull(ctx.get("bar"));
        Assert.assertNull(jspCtx.getVariableResolver().resolveVariable("bar"));
        Assert.assertNotNull(ctx.resolveVariable("bar"));
        Assert.assertEquals(ctx.resolveVariable("bar"), "a brand new value");
        Assert.assertNotNull(ctx.getVars());
    }
}

