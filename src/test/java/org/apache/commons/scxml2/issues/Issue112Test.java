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
package org.apache.commons.scxml2.issues;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.SCXML;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test cases for issue 112.
 * OPEN
 */
public class Issue112Test {

    /**
     * Tear down instance variables required by this test case.
     */
    @AfterEach
    public void tearDown() {
        Application.QUEUE.clear();
    }

    // Example using a custom <my:enqueue> action that generates more events during event processing.
    // An external event queue is used by <my:enqueue> instead of SCXMLExecutor#triggerEvent(TriggerEvent)    
    @Test
    public void test01issue112() throws Exception {

        final CustomAction ca1 =
            new CustomAction("http://my.custom-actions.domain/CUSTOM", "enqueue", Enqueue.class);
        final List<CustomAction> customActions = new ArrayList<>();
        customActions.add(ca1);

        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/issues/queue-01.xml", customActions);
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        Assertions.assertEquals("init", exec.getStatus().getStates().
                iterator().next().getId());

        // Add an event, other external events could be added to the queue at any time (this test only adds one).
        Application.QUEUE.add("next");

        // Rest of the events in this test are added by custom action invocation during processing of the one above.
        // Same concept applies to adding events in listeners, invokes and WRT AbstractStateMachine, state handlers.
        while (!Application.QUEUE.isEmpty()) {
            SCXMLTestHelper.fireEvent(exec, Application.QUEUE.remove());
        }

        Assertions.assertTrue(exec.getStatus().isFinal());
        Assertions.assertEquals("end", exec.getStatus().getStates().
                iterator().next().getId());

    }

    /**
     * A custom action that generates external events.
     */
    public static class Enqueue extends Action {

        private String event;

        public String getEvent() {
            return event;
        }

        public void setEvent(final String event) {
            this.event = event;
        }

        @Override
        public void execute(final ActionExecutionContext exctx) {

            Application.QUEUE.add(event);

        }

    }

    // Test external event queue
    private static final class Application {
        private static final Queue<String> QUEUE = new LinkedList<>();
    }

}

