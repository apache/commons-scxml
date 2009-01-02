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
package org.apache.commons.scxml.env;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * Unit tests {@link org.apache.commons.scxml.env.AbstractSCXMLListener}.
 */
public class AbstractSCXMLListenerTest extends TestCase {
    /**
     * Construct a new instance of AbstractSCXMLListenerTest with the specified name
     */
    public AbstractSCXMLListenerTest(String name) {
        super(name);
    }

    // Test data
    private TransitionTarget to;
    private TransitionTarget from;
    private Transition transition;
    private boolean heardOnEntry;
    private boolean heardOnExit;
    private boolean heardOnTransition;

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() {
        to = new State();
        from = new State();
        transition = new Transition();
        heardOnEntry = false;
        heardOnExit = false;
        heardOnTransition = false;
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        to = null;
        from = null;
        transition = null;
    }

    public void testAbstractSCXMLListener0() {
        SCXMLListener listener0 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(TransitionTarget)
                 */
                @Override
                public void onEntry(TransitionTarget state) {
                    heardOnEntry = true;
                }

                /**
                 * @see SCXMLListener#onExit(TransitionTarget)
                 */
                @Override
                public void onExit(TransitionTarget state) {
                    heardOnExit = true;
                }

                /**
                 * @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition)
                 */
                @Override
                public void onTransition(TransitionTarget from, TransitionTarget to,
                                         Transition transition) {
                    heardOnTransition = true;
                }
            };

        assertFalse("heardOnEntry == false", heardOnEntry);
        assertFalse("heardOnExit == false", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
        listener0.onEntry(to);
        listener0.onExit(to);
        listener0.onTransition(from, to, transition);
        assertTrue("heardOnEntry", heardOnEntry);
        assertTrue("heardOnExit", heardOnExit);
        assertTrue("heardOnExit", heardOnTransition);
    }

    public void testAbstractSCXMLListener1() {
        SCXMLListener listener1 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(TransitionTarget)
                 */
                @Override
                public void onEntry(TransitionTarget state) {
                    heardOnEntry = true;
                }

                /**
                 * @see SCXMLListener#onExit(TransitionTarget)
                 */
                @Override
                public void onExit(TransitionTarget state) {
                    heardOnExit = true;
                }
            };

        assertFalse("heardOnEntry == false", heardOnEntry);
        assertFalse("heardOnExit == false", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
        listener1.onEntry(to);
        listener1.onExit(to);
        listener1.onTransition(from, to, transition);
        assertTrue("heardOnEntry", heardOnEntry);
        assertTrue("heardOnExit", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
    }

    public void testAbstractSCXMLListener2() {
        SCXMLListener listener2 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(TransitionTarget)
                 */
                @Override
                public void onEntry(TransitionTarget state) {
                    heardOnEntry = true;
                }
            };

        assertFalse("heardOnEntry == false", heardOnEntry);
        assertFalse("heardOnExit == false", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
        listener2.onEntry(to);
        listener2.onExit(to);
        listener2.onTransition(from, to, transition);
        assertTrue("heardOnEntry", heardOnEntry);
        assertFalse("heardOnExit == false", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
    }

    public void testAbstractSCXMLListener3() {
        SCXMLListener listener3 = new AbstractSCXMLListener() {
                // empty
            };

        assertFalse("heardOnEntry == false", heardOnEntry);
        assertFalse("heardOnExit == false", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
        listener3.onEntry(to);
        listener3.onExit(to);
        listener3.onTransition(from, to, transition);
        assertFalse("heardOnEntry == false", heardOnEntry);
        assertFalse("heardOnExit == false", heardOnExit);
        assertFalse("heardOnTransition == false", heardOnTransition);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AbstractSCXMLListenerTest.class);
        suite.setName("AbstractSCXMLListener Tests");
        return suite;
    }

    public static void main(String args[]) {
        TestRunner.run(suite());
    }
}
