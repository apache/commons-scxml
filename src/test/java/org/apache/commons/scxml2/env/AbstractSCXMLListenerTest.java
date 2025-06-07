/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.env;

import org.apache.commons.scxml2.SCXMLListener;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.env.AbstractSCXMLListener}.
 */
public class AbstractSCXMLListenerTest {

    // Test data
    private State to;
    private State from;
    private Transition transition;
    private boolean heardOnEntry;
    private boolean heardOnExit;
    private boolean heardOnTransition;

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
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
    @AfterEach
    public void tearDown() {
        to = null;
        from = null;
        transition = null;
    }

    @Test
    void testAbstractSCXMLListener0() {
        final SCXMLListener listener0 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(EnterableState)
                 */
                @Override
                public void onEntry(final EnterableState state) {
                    heardOnEntry = true;
                }

                /**
                 * @see SCXMLListener#onExit(EnterableState)
                 */
                @Override
                public void onExit(final EnterableState state) {
                    heardOnExit = true;
                }

                /**
                 * @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition,String)
                 */
                @Override
                public void onTransition(final TransitionTarget from, final TransitionTarget to,
                                         final Transition transition, final String event) {
                    heardOnTransition = true;
                }
            };

        Assertions.assertFalse(heardOnEntry, "heardOnEntry == false");
        Assertions.assertFalse(heardOnExit, "heardOnExit == false");
        Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
        listener0.onEntry(to);
        listener0.onExit(to);
        listener0.onTransition(from, to, transition, null);
        Assertions.assertTrue(heardOnEntry, "heardOnEntry");
        Assertions.assertTrue(heardOnExit, "heardOnExit");
        Assertions.assertTrue(heardOnTransition, "heardOnTransition");
    }

    @Test
    void testAbstractSCXMLListener1() {
        final SCXMLListener listener1 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(EnterableState)
                 */
                @Override
                public void onEntry(final EnterableState state) {
                    heardOnEntry = true;
                }

                /**
                 * @see SCXMLListener#onExit(EnterableState)
                 */
                @Override
                public void onExit(final EnterableState state) {
                    heardOnExit = true;
                }
            };

        Assertions.assertFalse(heardOnEntry, "heardOnEntry == false");
        Assertions.assertFalse(heardOnExit, "heardOnExit == false");
        Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
        listener1.onEntry(to);
        listener1.onExit(to);
        listener1.onTransition(from, to, transition, null);
        Assertions.assertTrue(heardOnEntry, "heardOnEntry");
        Assertions.assertTrue(heardOnExit, "heardOnExit");
        Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
    }

    @Test
    void testAbstractSCXMLListener2() {
        final SCXMLListener listener2 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(EnterableState)
                 */
                @Override
                public void onEntry(final EnterableState state) {
                    heardOnEntry = true;
                }
            };

            Assertions.assertFalse(heardOnEntry, "heardOnEntry == false");
            Assertions.assertFalse(heardOnExit, "heardOnExit == false");
            Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
        listener2.onEntry(to);
        listener2.onExit(to);
        listener2.onTransition(from, to, transition, null);
        Assertions.assertTrue(heardOnEntry, "heardOnEntry");
        Assertions.assertFalse(heardOnExit, "heardOnExit == false");
        Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
    }

    @Test
    void testAbstractSCXMLListener3() {
        final SCXMLListener listener3 = new AbstractSCXMLListener() {
                // empty
            };

            Assertions.assertFalse(heardOnEntry, "heardOnEntry == false");
            Assertions.assertFalse(heardOnExit, "heardOnExit == false");
            Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
        listener3.onEntry(to);
        listener3.onExit(to);
        listener3.onTransition(from, to, transition, null);
        Assertions.assertFalse(heardOnEntry, "heardOnEntry == false");
        Assertions.assertFalse(heardOnExit, "heardOnExit == false");
        Assertions.assertFalse(heardOnTransition, "heardOnTransition == false");
    }
}
