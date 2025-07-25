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

import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogUtilsTest {

    @Test
    void testGetTTPathParent() {
        final State target = new State();
        target.setId("ID");

        final State parent1 = new State();
        parent1.setId("parent1");

        final State parent2 = new State();
        parent2.setId("parent2");

        parent1.setParent(parent2);
        target.setParent(parent1);

        Assertions.assertEquals("/parent2/parent1/ID", LogUtils.getTTPath(target));
    }

    @Test
    void testGetTTPathParentNull() {
        final State target = new State();
        target.setId("ID");

        Assertions.assertEquals("/ID", LogUtils.getTTPath(target));
    }

    @Test
    void testTransToString() {
        final State targetTo = new State();
        targetTo.setId("TO");

        final State targetFrom = new State();
        targetFrom.setId("FROM");

        final Transition transition = new Transition();
        transition.setCond("condition");
        transition.setEvent("event happened");

        Assertions.assertEquals( "(event = event happened, cond = condition, from = /FROM, to = /TO)",
                                        LogUtils.transToString(targetFrom, targetTo, transition, transition.getEvent()));
    }

}
