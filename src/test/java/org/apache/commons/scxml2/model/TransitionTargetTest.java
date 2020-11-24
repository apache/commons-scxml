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
package org.apache.commons.scxml2.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TransitionTargetTest {

    @Test
    public void testIsDescendantNullParent() {
        final State state = new State();
        final State context = new State();

        Assertions.assertFalse(state.isDescendantOf(context));
    }

    @Test
    public void testIsDescendantNotEqual() {
        final State state = new State();
        state.setParent(new State());
        final State context = new State();

        Assertions.assertFalse(state.isDescendantOf(context));
    }

    @Test
    public void testIsDescendantEqual() {
        final State state = new State();
        final State context = new State();
        state.setParent(context);

        Assertions.assertTrue(state.isDescendantOf(context));
    }

    @Test
    public void testIsDescendantParentEqual() {
        final State state = new State();
        final State context = new State();
        final State parent = new State();

        parent.setParent(context);
        state.setParent(parent);

        Assertions.assertTrue(state.isDescendantOf(context));
    }
}
