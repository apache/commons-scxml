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
package org.apache.commons.scxml2.env;

import org.apache.commons.scxml2.SCXMLListener;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
     * Set up instance variables required by this test case.
     */
    @Before
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
    @After
    public void tearDown() {
        to = null;
        from = null;
        transition = null;
    }

    @Test
    public void testAbstractSCXMLListener0() {
        SCXMLListener listener0 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(EnterableState)
                 */
                @Override
                public void onEntry(EnterableState state) {
                    heardOnEntry = true;
                }

                /**
                 * @see SCXMLListener#onExit(EnterableState)
                 */
                @Override
                public void onExit(EnterableState state) {
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

        Assert.assertFalse("heardOnEntry == false", heardOnEntry);
        Assert.assertFalse("heardOnExit == false", heardOnExit);
        Assert.assertFalse("heardOnTransition == false", heardOnTransition);
        listener0.onEntry(to);
        listener0.onExit(to);
        listener0.onTransition(from, to, transition);
        Assert.assertTrue("heardOnEntry", heardOnEntry);
        Assert.assertTrue("heardOnExit", heardOnExit);
        Assert.assertTrue("heardOnExit", heardOnTransition);
    }

    @Test
    public void testAbstractSCXMLListener1() {
        SCXMLListener listener1 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(EnterableState)
                 */
                @Override
                public void onEntry(EnterableState state) {
                    heardOnEntry = true;
                }

                /**
                 * @see SCXMLListener#onExit(EnterableState)
                 */
                @Override
                public void onExit(EnterableState state) {
                    heardOnExit = true;
                }
            };

        Assert.assertFalse("heardOnEntry == false", heardOnEntry);
        Assert.assertFalse("heardOnExit == false", heardOnExit);
        Assert.assertFalse("heardOnTransition == false", heardOnTransition);
        listener1.onEntry(to);
        listener1.onExit(to);
        listener1.onTransition(from, to, transition);
        Assert.assertTrue("heardOnEntry", heardOnEntry);
        Assert.assertTrue("heardOnExit", heardOnExit);
        Assert.assertFalse("heardOnTransition == false", heardOnTransition);
    }

    @Test
    public void testAbstractSCXMLListener2() {
        SCXMLListener listener2 = new AbstractSCXMLListener() {
                /**
                 * @see SCXMLListener#onEntry(EnterableState)
                 */
                @Override
                public void onEntry(EnterableState state) {
                    heardOnEntry = true;
                }
            };

            Assert.assertFalse("heardOnEntry == false", heardOnEntry);
            Assert.assertFalse("heardOnExit == false", heardOnExit);
            Assert.assertFalse("heardOnTransition == false", heardOnTransition);
        listener2.onEntry(to);
        listener2.onExit(to);
        listener2.onTransition(from, to, transition);
        Assert.assertTrue("heardOnEntry", heardOnEntry);
        Assert.assertFalse("heardOnExit == false", heardOnExit);
        Assert.assertFalse("heardOnTransition == false", heardOnTransition);
    }

    @Test
    public void testAbstractSCXMLListener3() {
        SCXMLListener listener3 = new AbstractSCXMLListener() {
                // empty
            };

            Assert.assertFalse("heardOnEntry == false", heardOnEntry);
            Assert.assertFalse("heardOnExit == false", heardOnExit);
            Assert.assertFalse("heardOnTransition == false", heardOnTransition);
        listener3.onEntry(to);
        listener3.onExit(to);
        listener3.onTransition(from, to, transition);
        Assert.assertFalse("heardOnEntry == false", heardOnEntry);
        Assert.assertFalse("heardOnExit == false", heardOnExit);
        Assert.assertFalse("heardOnTransition == false", heardOnTransition);
    }
}
