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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Final;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
/**
 * Unit tests {@link org.apache.commons.scxml.io.SCXMLParser}.
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
    private URL microwave01, microwave02, transitions01, prefix01, send01,
        microwave03, microwave04, scxmlinitialattr;
    private SCXML scxml;
    private String scxmlAsString;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-01.xml");
        microwave02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-02.xml");
        microwave03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-03.xml");
        microwave04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-04.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-01.xml");
        send01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/send-01.xml");
        prefix01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/prefix-01.xml");
        scxmlinitialattr = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/io/scxml-initial-attr.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01 = microwave02 = microwave03 = microwave04 = transitions01 = prefix01 = send01 = null;
        scxml = null;
        scxmlAsString = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLParserMicrowave01Sample() {
        scxml = SCXMLTestHelper.parse(microwave01);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLParserMicrowave02Sample() {
        scxml = SCXMLTestHelper.parse(microwave02);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

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

    public void testSCXMLParserTransitions01Sample() {
        scxml = SCXMLTestHelper.parse(transitions01);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLParserPrefix01Sample() {
        scxml = SCXMLTestHelper.parse(prefix01);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLParserSend01Sample() {
        // Digest
        scxml = SCXMLTestHelper.parse(send01);
        State ten = (State) scxml.getInitialTarget();
        assertEquals("ten", ten.getId());
        List<Transition> ten_done = ten.getTransitionsList("ten.done");
        assertEquals(1, ten_done.size());
        Transition ten2twenty = (Transition) ten_done.get(0);
        List<Action> actions = ten2twenty.getActions();
        assertEquals(1, actions.size());
        Send send = (Send) actions.get(0);
        assertEquals("send1", send.getSendid());
        /* Serialize
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
        String expectedFoo2Serialization =
            "<foo xmlns=\"http://my.test.namespace\" id=\"foo2\">"
            + "<prompt xmlns=\"http://foo.bar.com/vxml3\">This is just"
            + " an example.</prompt></foo>";
        assertFalse(scxmlAsString.indexOf(expectedFoo2Serialization) == -1);
        */
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

