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
package org.apache.commons.scxml;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.scxml.env.SimpleContext;
import org.apache.commons.scxml.env.jsp.ELEvaluator;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;
/**
 * Unit tests {@link org.apache.commons.scxml.SCXMLExecutor}.
 */
public class SCXMLExecutorTest extends TestCase {
    /**
     * Construct a new instance of SCXMLExecutorTest with
     * the specified name
     */
    public SCXMLExecutorTest(String name) {
        super(name);
    }

    // Test data
    private URL microwave01jsp, microwave02jsp, microwave01jexl,
        microwave02jexl, microwave03jexl, microwave04jexl, microwave05jexl, transitions01,
        transitions02, transitions03, transitions04, prefix01, send01, send02;
    private SCXMLExecutor exec;

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() {
        microwave01jsp = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-01.xml");
        microwave02jsp = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jsp/microwave-02.xml");
        microwave01jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-01.xml");
        microwave02jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-02.xml");
        microwave03jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-03.xml");
        microwave04jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-04.xml");
        microwave05jexl = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/env/jexl/microwave-05.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-01.xml");
        transitions02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-02.xml");
        transitions03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-03.xml");
        transitions04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/transitions-04.xml");
        prefix01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/prefix-01.xml");
        send01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/send-01.xml");
        send02 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml/send-02.xml");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        microwave01jsp = microwave02jsp = microwave01jexl = microwave02jexl =
            microwave04jexl = microwave05jexl = transitions01 = transitions02 = transitions03 =
            transitions04 = prefix01 = send01 = send02 = null;
    }

    /**
     * Test the implementation
     */
    public void testSCXMLExecutorMicrowave01JspSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave01jsp,
            new SimpleContext(), new ELEvaluator());
        assertNotNull(exec);
        checkMicrowave01Sample();
    }

    public void testSCXMLExecutorMicrowave02JspSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave02jsp,
            new SimpleContext(), new ELEvaluator());
        assertNotNull(exec);
        checkMicrowave02Sample();
    }

    public void testSCXMLExecutorMicrowave01JexlSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave01jexl);
        assertNotNull(exec);
        checkMicrowave01Sample();
    }

    public void testSCXMLExecutorMicrowave02JexlSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(microwave02jexl);
        assertNotNull(exec);
        checkMicrowave02Sample();
    }

    // Uses SCXMLParser (latest WD)
    public void testSCXMLExecutorMicrowave03JexlSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave03jexl);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        checkMicrowave01Sample();
    }

    // Uses SCXMLParser (latest WD)
    public void testSCXMLExecutorMicrowave04JexlSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave04jexl);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        checkMicrowave02Sample();
    }

    // Uses SCXMLParser (latest WD)
    public void testSCXMLExecutorMicrowave05JexlSample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(microwave05jexl);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        checkMicrowave02Sample();
    }
    
    public void testSCXMLExecutorPrefix01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(prefix01);
        assertNotNull(exec);
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ten", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("twenty", ((State)currentStates.iterator().
            next()).getId());
    }

    public void testSCXMLExecutorTransitions01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(transitions01);
        assertNotNull(exec);
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("twenty_one", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "twenty_one.done");
        assertEquals(1, currentStates.size());
        assertEquals("twenty_two", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "twenty_two.done");
        assertEquals(3, exec.getCurrentStatus().getStates().size());
    }

    public void testSCXMLExecutorTransitions02Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(transitions02);
        assertNotNull(exec);
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "ten.stay");
        assertEquals(1, currentStates.size());
        assertEquals("ten", ((State)currentStates.iterator().
            next()).getId());
        exec = SCXMLTestHelper.testExecutorSerializability(exec);
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.self");
        assertEquals(1, currentStates.size());
        assertEquals("ten", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("twenty", ((State)currentStates.iterator().
            next()).getId());
    }

    public void testSCXMLExecutorTransitions03Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(transitions03);
        assertNotNull(exec);
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(3, currentStates.size());
        Set expected = new HashSet();
        expected.add("twenty_one_2");
        expected.add("twenty_two_2");
        expected.add("twenty_three_2");
        for (Iterator i = currentStates.iterator(); i.hasNext(); ) {
            TransitionTarget tt = (TransitionTarget) i.next();
            if (!expected.remove(tt.getId())) {
                fail("'" + tt.getId()
                    + "' is not an expected current state ID");
            }
        }
    }

    // Uses SCXMLParser (latest WD)
    public void testSCXMLExecutorTransitions04Sample() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(transitions04);
        assertNotNull(scxml);
        exec = SCXMLTestHelper.getExecutor(scxml);
        assertNotNull(exec);
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(3, currentStates.size());
        Set expected = new HashSet();
        expected.add("twenty_one_1");
        expected.add("twenty_two_1");
        expected.add("twenty_three_1");
        for (Iterator i = currentStates.iterator(); i.hasNext(); ) {
            TransitionTarget tt = (TransitionTarget) i.next();
            if (!expected.remove(tt.getId())) {
                fail("'" + tt.getId()
                    + "' is not an expected current state ID");
            }
        }
        currentStates = SCXMLTestHelper.fireEvent(exec, "bar");
        assertEquals(1, currentStates.size());
        assertEquals("thirty", ((State)currentStates.iterator().
            next()).getId());
    }

    public void testSend01Sample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(send01);
        assertNotNull(exec);
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ten", ((State)currentStates.iterator().
            next()).getId());
        currentStates = SCXMLTestHelper.fireEvent(exec, "ten.done");
        assertEquals(1, currentStates.size());
        assertEquals("twenty", ((State)currentStates.iterator().
            next()).getId());
    }

    public void testSend02TypeSCXMLSample() throws Exception {
        exec = SCXMLTestHelper.getExecutor(send02);
        assertNotNull(exec);
        Set currentStates = exec.getCurrentStatus().getStates();
        assertEquals(1, currentStates.size());
        assertEquals("ninety", ((State)currentStates.iterator().
            next()).getId());
        assertTrue(exec.getCurrentStatus().isFinal());
    }

    private void checkMicrowave01Sample() throws Exception {
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        assertEquals(1, currentStates.size());
        assertEquals("cooking", ((State)currentStates.iterator().
            next()).getId());
    }

    private void checkMicrowave02Sample() throws Exception {
        Set currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        assertEquals(2, currentStates.size());
        String id = ((State)currentStates.iterator().next()).getId();
        assertTrue(id.equals("closed") || id.equals("cooking"));
    }
}
