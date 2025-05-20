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
package org.apache.commons.scxml2;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.TriggerEvent}.
 */
public class TriggerEventTest {

    // Test data
    private Map<String, String> payloadData;
    private Object payload1, payload2;
    private TriggerEvent te1, te2, te3, te4, te5, te6, te7;

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void setUp() {
        payloadData = new HashMap<>();
        payloadData.put("property1", "value1");
        payload1 = payloadData;
        payload2 = new Object();
        te1 = new EventBuilder("name1", TriggerEvent.CHANGE_EVENT).data(payload1).build();
        te2 = new EventBuilder("name1", TriggerEvent.CHANGE_EVENT).data(payload1).build();
        te3 = new EventBuilder("name2", TriggerEvent.CALL_EVENT).data(payload2).build();
        te4 = new EventBuilder("name2", TriggerEvent.CALL_EVENT).data(payload2).build();
        te5 = new EventBuilder("name3", TriggerEvent.SIGNAL_EVENT).build();
        te6 = new EventBuilder("name3", TriggerEvent.SIGNAL_EVENT).build();
        te7 = new EventBuilder("name3", TriggerEvent.TIME_EVENT).build();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {
        payloadData.clear();
        payloadData = null;
        payload1 = payload2 = null;
        te1 = te2 = te3 = te4 = te5 = te6 = te7 = null;
    }

    @Test
    public void testTriggerEventEquals() {
        Assertions.assertEquals(te1, te2);
        Assertions.assertEquals(te3, te4);
        Assertions.assertEquals(te5, te6);
        Assertions.assertNotEquals(te1, te4);
        Assertions.assertNotNull(te7);
    }

    /**
     * Test the implementation
     */
    @Test
    public void testTriggerEventGetters() {
        Assertions.assertEquals("name1", te1.getName());
        Assertions.assertEquals(2, te2.getType());
        Assertions.assertNull(te7.getData());
    }

    @Test
    public void testTriggerEventHashCode() {
        Assertions.assertEquals("TriggerEvent{name=name3, type=4}".hashCode(),
            te7.hashCode());
        Assertions.assertEquals("TriggerEvent{name=name3, type=3}".hashCode(),
            te5.hashCode());
    }

    @Test
    public void testTriggerEventToString() {
        Assertions.assertEquals("TriggerEvent{name=name3, type=4}", te7.toString());
        Assertions.assertEquals("TriggerEvent{name=name1, type=2, data="
            + "{property1=value1}}", te2.toString());
    }
}

