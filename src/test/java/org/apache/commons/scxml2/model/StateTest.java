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
package org.apache.commons.scxml2.model;

import java.util.List;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    void testAddTransitionContainKey() {
        final Transition transition1 = new Transition();
        transition1.setEvent("event");

        final Transition transition2 = new Transition();
        transition2.setEvent("event");

        final State state = new State();
        state.addTransition(transition1);
        state.addTransition(transition2);

        final List<Transition> events = state.getTransitionsList("event");

        Assertions.assertEquals(2, events.size());
    }

    @Test
    void testAddTransitionDoesNotContainKey() {
        final Transition transition = new Transition();
        transition.setEvent("event");

        final State state = new State();
        state.addTransition(transition);

        final List<Transition> events = state.getTransitionsList("event");

        Assertions.assertEquals(1, events.size());
        Assertions.assertEquals("event", events.get(0).getEvent());
    }

    @Test
    void testGetTransitionList() {
        final Transition transition1 = new Transition();
        transition1.setEvent("event");

        final Transition transition2 = new Transition();
        transition2.setEvent("event");

        final State state = new State();
        state.addTransition(transition1);
        state.addTransition(transition2);

        final List<Transition> events = state.getTransitionsList();

        Assertions.assertEquals(2, events.size());
    }

    @Test
    void testGetTransitionsList() {
        final State state = new State();
        state.getTransitionsList().add(new Transition());
        Assertions.assertNotNull(state.getTransitionsList(null));
    }

    @Test
    void testGetTransitionsListNull() {
        final State state = new State();
        Assertions.assertNull(state.getTransitionsList("event"));
    }

    @Test
    void testHasHistory() {
        final History history = new History();

        final State state = new State();
        state.addHistory(history);

        Assertions.assertTrue(state.hasHistory());
    }

    @Test
    void testHasHistoryEmpty() {
        final State state = new State();
        Assertions.assertFalse(state.hasHistory());
    }

    @Test
    void testInitialAttribute() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/state-01.xml");
        exec.go();
        Assertions.assertEquals("s11", exec.getStatus().getStates().iterator().next().getId());
    }

    @Test
    void testIsCompositeFalse() {
        final State state = new State();
        Assertions.assertFalse(state.isComposite());
    }

    @Test
    void testIsCompositeHasChildren() {
        final State state1 = new State();

        final State state = new State();
        state.addChild(state1);

        Assertions.assertTrue(state.isComposite());
    }

    @Test
    void testIsCompositeParallel() {
        final State child = new State();

        final State state = new State();
        state.addChild(child);

        Assertions.assertTrue(state.isComposite());
    }

    @Test
    void testIsRegion() {
        final State state = new State();
        state.setParent(new Parallel());

        Assertions.assertTrue(state.isRegion());
    }

    @Test
    void testIsRegionNotParallel() {
        final State state = new State();
        state.setParent(new State());

        Assertions.assertFalse(state.isRegion());
    }

    @Test
    void testIsSimple() {
        final State state = new State();
        Assertions.assertTrue(state.isSimple());
    }

    @Test
    void testIsSimpleHasChildren() {
        final State state1 = new State();

        final State state = new State();
        state.addChild(state1);

        Assertions.assertFalse(state.isSimple());
    }
}
