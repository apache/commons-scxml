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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActionTest {

    private Action action;

    @BeforeEach
    public void setUp() {
        action = new Assign();
    }
    @Test
    void testGetParentStateIsHistory() throws Exception {
        final Transition transition = new Transition();

        final History history = new History();
        history.setId("on");

        final State state = new State();
        state.setId("off");

        history.setParent(state);

        transition.setParent(history.getParent());
        action.setParent(transition);

        final TransitionTarget returnValue = action.getParentEnterableState();

        Assertions.assertEquals("off", returnValue.getId());
    }

    @Test
    void testGetParentStateIsInitial() throws Exception {
        final SimpleTransition transition = new SimpleTransition();

        final Initial initial = new Initial();

        final State state = new State();
        state.setId("off");

        initial.setParent(state);

        initial.setTransition(transition);
        action.setParent(transition);

        final TransitionTarget returnValue = action.getParentEnterableState();

        Assertions.assertEquals("off", returnValue.getId());
    }

    @Test
    void testGetParentStateIsParallel() throws Exception {
        final Transition transition = new Transition();

        final Parallel parallel = new Parallel();
        parallel.setId("on");

        final State state = new State();
        state.setId("off");

        parallel.setParent(state);

        transition.setParent(parallel);
        action.setParent(transition);

        final TransitionTarget returnValue = action.getParentEnterableState();

        Assertions.assertEquals("on", returnValue.getId());
    }

    @Test
    void testGetParentStateIsState() throws Exception {
        final Transition transition = new Transition();

        final State state = new State();
        state.setId("on");

        transition.setParent(state);
        action.setParent(transition);

        final TransitionTarget returnValue = action.getParentEnterableState();

        Assertions.assertEquals("on", returnValue.getId());
    }
}
