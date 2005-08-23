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

import org.apache.commons.scxml.env.ELEvaluator;
import org.apache.commons.scxml.env.ELContext;
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
    private URL microwave01, microwave02;
    private Evaluator evaluator;
    private Context ctx;
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
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01 = microwave02 = null;
        evaluator = null;
        ctx = null;
        scxml = null;
        scxmlAsString = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLDigester() {
        scxml = digest(microwave01);
        scxmlAsString = serialize(scxml);
        scxml = digest(microwave02);
        scxmlAsString = serialize(scxml);
    }

    private SCXML digest(final URL url) {
        assertNotNull(url);
        evaluator = new ELEvaluator();
        ctx = new ELContext();
        try {
            scxml = SCXMLDigester.digest(url,
                null, ctx, evaluator);
        } catch (Exception e) {
            fail();
        }
        assertNotNull(scxml);
        return scxml;
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

