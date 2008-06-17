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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.model.Final;
import org.apache.commons.scxml.model.SCXML;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLParser}.
 */
public class SCXMLParserTest extends TestCase {
    /**
     * Construct a new instance of SCXMLDigesterTest with
     * the specified name
     */
    public SCXMLParserTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SCXMLParserTest.class);
        suite.setName("SCXML Parser Tests");
        return suite;
    }

    // Test data
    private URL microwave03, microwave04, scxmlinitialattr;
    private SCXML scxml;
    private String scxmlAsString;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-03.xml");
        microwave04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-04.xml");
        scxmlinitialattr = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/io/scxml-initial-attr.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave03 = microwave04 = null;
        scxml = null;
        scxmlAsString = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLParserMicrowave03Sample() {
        scxml = SCXMLTestHelper.parse(microwave03);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLParserMicrowave04Sample() {
        scxml = SCXMLTestHelper.parse(microwave04);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLParserInitialAttr() {
        scxml = SCXMLTestHelper.parse(scxmlinitialattr);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
        Final foo = (Final) scxml.getInitialTarget();
        assertEquals("foo", foo.getId());
    }

    private String serialize(final SCXML scxml) {
        scxmlAsString = SCXMLSerializer.serialize(scxml);
        assertNotNull(scxmlAsString);
        return scxmlAsString;
    }

     public static void main(String args[]) {
        TestRunner.run(suite());
    }
}

