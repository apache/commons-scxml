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

import junit.framework.TestCase;

import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
/**
 * Unit tests {@link org.apache.commons.scxml.io.SCXMLDigester}.
 */
public class SCXMLDigesterTest extends TestCase {
    /**
     * Construct a new instance of SCXMLDigesterTest with
     * the specified name
     */
    public SCXMLDigesterTest(String name) {
        super(name);
    }

    // Test data
    private URL microwave01, microwave02, transitions01, send01, prefix01;
    private SCXML scxml;
    private String scxmlAsString;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-01.xml");
        microwave02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-02-legacy.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-01-legacy.xml");
        send01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/send-01.xml");
        prefix01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/prefix-01.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01 = microwave02 = transitions01 = prefix01 = send01 = null;
        scxml = null;
        scxmlAsString = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLDigesterMicrowave01Sample() throws Exception {
        scxml = SCXMLTestHelper.digest(microwave01);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLDigesterMicrowave02Sample() throws Exception {
        scxml = SCXMLTestHelper.digest(microwave02);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLDigesterTransitions01Sample() throws Exception {
        scxml = SCXMLTestHelper.digest(transitions01);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLDigesterPrefix01Sample() throws Exception {
        scxml = SCXMLTestHelper.digest(prefix01);
        assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        assertNotNull(scxmlAsString);
    }

    public void testSCXMLDigesterSend01Sample() throws Exception {
        // Digest
        scxml = SCXMLTestHelper.digest(send01);
        State ten = (State) scxml.getInitialTarget();
        assertEquals("ten", ten.getId());
        List ten_done = ten.getTransitionsList("ten.done");
        assertEquals(1, ten_done.size());
        Transition ten2twenty = (Transition) ten_done.get(0);
        List actions = ten2twenty.getActions();
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

    private String serialize(final SCXML scxml) {
        scxmlAsString = SCXMLSerializer.serialize(scxml);
        assertNotNull(scxmlAsString);
        return scxmlAsString;
    }

}

