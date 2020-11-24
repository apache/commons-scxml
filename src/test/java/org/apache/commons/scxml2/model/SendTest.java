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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLIOProcessor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.EventBuilder;
import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SendTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testNamelistOrderPreserved() throws Exception {
        final List<Object> payloads = new ArrayList<>();
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/model/send-test-01.xml");
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml, null, new SimpleDispatcher() {
            @Override
            public void send(final Map<String, SCXMLIOProcessor> ioProcessors, final String id, final String target,
                    final String type, final String event, final Object data, final Object hints, final long delay) {
                payloads.add(data);
                super.send(ioProcessors, id, target, type, event, data, hints, delay);
            }
        });
        exec.go();
        final TriggerEvent te = new EventBuilder("event.foo", TriggerEvent.SIGNAL_EVENT).data(3).build();
        SCXMLTestHelper.fireEvent(exec, te);

        Assertions.assertFalse(payloads.isEmpty(), "Payloads empty.");
        Assertions.assertTrue(payloads.get(0) instanceof Map, "Payload is not a map.");
        final Map<String, Object> firstPayload = (Map<String, Object>) payloads.get(0);
        Assertions.assertEquals(2, firstPayload.size(), "Only two in the namelist data expected.");

        Assertions.assertEquals(1, firstPayload.get("one"), "Unexpected value for 'one'.");
        Assertions.assertEquals(2, firstPayload.get("two"), "Unexpected value for 'two'.");

        // Note: the standard allows specifying the value of the namelist attribute of the <send> element
        // as space-separated list of values, which implies an ordered sequence of items.
        final Iterator<String> it = firstPayload.keySet().iterator();
        Assertions.assertEquals("one", it.next(), "The first one in the namelist must be 'one'.");
        Assertions.assertEquals("two", it.next(), "The first one in the namelist must be 'two'.");
    }

    private long parseDelay(final String delayString) throws SCXMLExpressionException {
        return Send.parseDelay(delayString, true, delayString);
    }

    @Test
    public void testDelayExpression() throws Exception {
        Assertions.assertEquals(0L, parseDelay(".s"));
        Assertions.assertEquals(0L, parseDelay(".0s"));
        Assertions.assertEquals(1000L, parseDelay("1.s"));
        Assertions.assertEquals(1000L, parseDelay("1.0s"));
        Assertions.assertEquals(1500L, parseDelay("1.5s"));
        Assertions.assertEquals(500L, parseDelay(".5s"));
        Assertions.assertEquals(500L, parseDelay("0.5s"));
        Assertions.assertEquals(50L, parseDelay("0.05s"));
        Assertions.assertEquals(5L, parseDelay("0.005s"));
        Assertions.assertEquals(0L, parseDelay("0.0005s"));
        Assertions.assertEquals(0L, parseDelay(".9ms"));
        Assertions.assertEquals(1L, parseDelay("1.9ms"));
        Assertions.assertEquals(60000L, parseDelay("1m"));
        Assertions.assertEquals(60000L, parseDelay("1.0m"));
        Assertions.assertEquals(30000L, parseDelay(".5m"));
        Assertions.assertEquals(6000L, parseDelay(".1m"));
        Assertions.assertEquals(6000L, parseDelay(".10m"));
        Assertions.assertEquals(15000L, parseDelay(".25m"));
    }
}
