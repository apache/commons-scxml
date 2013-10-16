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
package org.apache.commons.scxml2;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.TriggerEvent}.
 */
public class TriggerEventTest {

    // Test data
    private Map<String, String> payloadData;
    private Object payload1, payload2;
    private TriggerEvent te1, te2, te3, te4, te5, te6, te7;

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void setUp() {
        payloadData = new HashMap<String, String>();
        payloadData.put("property1", "value1");
        payload1 = payloadData;
        payload2 = new Object();
        te1 = new TriggerEvent("name1", TriggerEvent.CHANGE_EVENT, payload1);
        te2 = new TriggerEvent("name1", TriggerEvent.CHANGE_EVENT, payload1);
        te3 = new TriggerEvent("name2", TriggerEvent.CALL_EVENT, payload2);
        te4 = new TriggerEvent("name2", TriggerEvent.CALL_EVENT, payload2);
        te5 = new TriggerEvent("name3", TriggerEvent.SIGNAL_EVENT);
        te6 = new TriggerEvent("name3", TriggerEvent.SIGNAL_EVENT);
        te7 = new TriggerEvent("name3", TriggerEvent.TIME_EVENT);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void tearDown() {
        payloadData.clear();
        payloadData = null;
        payload1 = payload2 = null;
        te1 = te2 = te3 = te4 = te5 = te6 = te7 = null;
    }

    /**
     * Test the implementation
     */
    @Test
    public void testTriggerEventGetters() {
        Assert.assertEquals(te1.getName(), "name1");
        Assert.assertEquals(te2.getType(), 2);
        Assert.assertNull(te7.getPayload());
    }

    @Test
    public void testTriggerEventEquals() {
        Assert.assertTrue(te1.equals(te2));
        Assert.assertTrue(te3.equals(te4));
        Assert.assertTrue(te5.equals(te6));
        Assert.assertFalse(te1.equals(te4));
        Assert.assertFalse(te7.equals(null));
    }

    @Test
    public void testTriggerEventToString() {
        Assert.assertEquals("TriggerEvent{name=name3,type=4}", te7.toString());
        Assert.assertEquals("TriggerEvent{name=name1,type=2,payload="
            + "{property1=value1}}", te2.toString());
    }

    @Test
    public void testTriggerEventHashCode() {
        Assert.assertEquals("TriggerEvent{name=name3,type=4}".hashCode(),
            te7.hashCode());
        Assert.assertEquals("TriggerEvent{name=name3,type=3}".hashCode(),
            te5.hashCode());
    }
}

