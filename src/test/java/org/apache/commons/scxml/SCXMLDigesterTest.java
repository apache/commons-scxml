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
package org.apache.commons.scxml;

import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.model.SCXML;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLDigester}.
 */
public class SCXMLDigesterTest extends TestCase {
    /**
     * Construct a new instance of SCXMLDigesterTest with
     * the specified name
     */
    public SCXMLDigesterTest(String name) {
        super(name);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SCXMLDigesterTest.class);
        suite.setName("SCXML Digester Tests");
        return suite;
    }

    // Test data
    private URL microwave01, microwave02, transitions01;
    private SCXML scxml;
    private String scxmlAsString;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/microwave-01.xml");
        microwave02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/microwave-02.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01 = microwave02 = transitions01 = null;
        scxml = null;
        scxmlAsString = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLDigesterMicrowave01Sample() {
        scxml = SCXMLTestHelper.digest(microwave01);
        scxmlAsString = serialize(scxml);
    }

    public void testSCXMLDigesterMicrowave02Sample() {
        scxml = SCXMLTestHelper.digest(microwave02);
        scxmlAsString = serialize(scxml);
    }

    public void testSCXMLDigesterTransitions01Sample() {
        scxml = SCXMLTestHelper.digest(transitions01);
        scxmlAsString = serialize(scxml);
    }

    private String serialize(final SCXML scxml) {
        scxmlAsString = SCXMLDigester.serializeSCXML(scxml);
        assertNotNull(scxmlAsString);
        return scxmlAsString;
    }

     public static void main(String args[]) {
        TestRunner.run(suite());
    }
}

