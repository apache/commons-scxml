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
package org.apache.commons.scxml.io;

import java.net.URL;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.env.SimpleErrorHandler;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.xml.sax.SAXException;
/**
 * Unit tests {@link org.apache.commons.scxml.io.SCXMLDigester}
 * Test white box nature of <state> element "src" attribute.
 */
public class StateSrcTest extends TestCase {
    /**
     * Construct a new instance of SCXMLDigesterTest with
     * the specified name
     */
    public StateSrcTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StateSrcTest.class);
        suite.setName("SCXML Digester Tests");
        return suite;
    }

    // Test data
    private URL src01, src04, src05;
    private SCXML scxml;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        src01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/io/src-test-1.xml");
        src04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/io/src-test-4.xml");
        src05 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/io/src-test-5.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        src01 = src04 = src05 = null;
        scxml = null;
        exec = null;
    }

    /**
     * Test the implementation
     */
    public void testRecursiveSrcInclude() {
        scxml = SCXMLTestHelper.digest(src01);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        Set states = exec.getCurrentStatus().getStates();
        assertEquals(1, states.size());
        assertEquals("srctest3", ((State) states.iterator().next()).getId());
        states = SCXMLTestHelper.fireEvent(exec, "src.test");
        assertEquals(1, states.size());
        assertEquals("srctest1end", ((State) states.iterator().next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

    public void testBadSrcInclude() {
        try {
            scxml = SCXMLParser.parse(src04, new SimpleErrorHandler());
            fail("Document with bad <state> src attribute shouldn't be parsed!");
        } catch (SAXException me) {
            assertTrue("Unexpected error message for bad <state> 'src' URI",
                me.getMessage() != null && me.getMessage().indexOf("Source attribute in <state src=") != -1);
        } catch (Exception e) {
            fail("Unexpected exception [" + e.getClass().getName() + ":" +
                e.getMessage() + "]");
        }
    }

    public void testBadSrcFragmentInclude() {
        try {
            scxml = SCXMLParser.parse(src05, new SimpleErrorHandler());
            fail("Document with bad <state> src attribute shouldn't be parsed!");
        } catch (SAXException me) {
            assertTrue("Unexpected error message for bad <state> 'src' URI fragment",
                me.getMessage() != null && me.getMessage().indexOf("URI Fragment in <state src=") != -1);
        } catch (Exception e) {
            fail("Unexpected exception [" + e.getClass().getName() + ":" +
                e.getMessage() + "]");
        }
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}

