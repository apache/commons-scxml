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
import org.junit.Assert;
import org.junit.Test;

public class SendTest {

    @Test
    public void testNamelistOrderPreserved() throws Exception {
        final List<Object> payloads = new ArrayList<Object>();
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
        TriggerEvent te = new EventBuilder("event.foo", TriggerEvent.SIGNAL_EVENT).data(new Integer(3)).build();
        SCXMLTestHelper.fireEvent(exec, te);

        Assert.assertFalse("Payloads empty.", payloads.isEmpty());
        Assert.assertTrue("Payload is not a map.", payloads.get(0) instanceof Map);
        Map<String, Object> firstPayload = (Map<String, Object>) payloads.get(0);
        Assert.assertEquals("Only two in the namelist data expected.", 2, firstPayload.size());

        Assert.assertEquals("Unexpected value for 'one'.", 1, firstPayload.get("one"));
        Assert.assertEquals("Unexpected value for 'two'.", 2, firstPayload.get("two"));

        // Note: the standard allows specifying the value of the namelist attribute of the <send> element
        // as space-separated list of values, which implies an ordered sequence of items.
        final Iterator<String> it = firstPayload.keySet().iterator();
        Assert.assertEquals("The first one in the namelist must be 'one'.", "one", it.next());
        Assert.assertEquals("The first one in the namelist must be 'two'.", "two", it.next());
    }

    private long parseDelay(String delayString) throws SCXMLExpressionException {
        return Send.parseDelay(delayString, true, delayString);
    }

    @Test
    public void testDelayExpression() throws Exception {
        Assert.assertEquals(0L, parseDelay(".s"));
        Assert.assertEquals(0L, parseDelay(".0s"));
        Assert.assertEquals(1000L, parseDelay("1.s"));
        Assert.assertEquals(1000L, parseDelay("1.0s"));
        Assert.assertEquals(1500L, parseDelay("1.5s"));
        Assert.assertEquals(500L, parseDelay(".5s"));
        Assert.assertEquals(500L, parseDelay("0.5s"));
        Assert.assertEquals(50L, parseDelay("0.05s"));
        Assert.assertEquals(5L, parseDelay("0.005s"));
        Assert.assertEquals(0L, parseDelay("0.0005s"));
        Assert.assertEquals(0L, parseDelay(".9ms"));
        Assert.assertEquals(1L, parseDelay("1.9ms"));
        Assert.assertEquals(60000L, parseDelay("1m"));
        Assert.assertEquals(60000L, parseDelay("1.0m"));
        Assert.assertEquals(30000L, parseDelay(".5m"));
        Assert.assertEquals(6000L, parseDelay(".1m"));
        Assert.assertEquals(6000L, parseDelay(".10m"));
        Assert.assertEquals(15000L, parseDelay(".25m"));
    }
}
