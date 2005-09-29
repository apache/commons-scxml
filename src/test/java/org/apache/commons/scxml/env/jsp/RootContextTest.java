/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml.env;

import java.net.URL;

import javax.servlet.jsp.JspContext;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;

/**
 * Unit tests {@link org.apache.commons.scxml.env.RootContext}.
 */
public class RootContextTest extends TestCase {
    /**
     * Construct a new instance of ActionsTest with
     * the specified name
     */
    public RootContextTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RootContextTest.class);
        suite.setName("SCXML Env RootContext (wraps JSP Context) Tests");
        return suite;
    }

    // Test data
    private URL rootCtxSample;
    private ELEvaluator evaluator;
    private JspContext jspCtx;
    private RootContext ctx;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        rootCtxSample = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp-rootctx-test.xml");
        evaluator = new ELEvaluator();
        jspCtx = new MockJspContext();
        jspCtx.setAttribute("foo", "1");
        ctx = new RootContext(jspCtx);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        rootCtxSample = null;
        evaluator = null;
        jspCtx = null;
        ctx = null;
        exec = null;
    }

    /**
     * Test the implementation
     */
    public void testRootContext() {
        assertEquals("1", String.valueOf(ctx.get("foo")));
        exec = SCXMLTestHelper.getExecutor(rootCtxSample, ctx, evaluator);
        assertEquals("1", String.valueOf(jspCtx.getAttribute("foo")));
        assertEquals("2", String.valueOf(ctx.get("foo")));
        assertNull(jspCtx.getAttribute("bar"));
        assertNotNull(ctx.get("bar"));
        try {
            assertNull(jspCtx.getVariableResolver().resolveVariable("bar"));
            assertNotNull(ctx.resolveVariable("bar"));
            assertEquals(ctx.resolveVariable("bar"), "a brand new value");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertNotNull(ctx.iterator());
    }

     public static void main(String args[]) {
        TestRunner.run(suite());
    }

}

