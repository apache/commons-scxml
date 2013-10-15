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
package org.apache.commons.scxml2.invoke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.ModelException;

/**
 * Trigger the given {@link TriggerEvent} (or array of) on the given
 * state machine executor asynchronously, once.
 */
class AsyncTrigger implements Runnable {

    /** The state machine executor. */
    private final SCXMLExecutor executor;
    /** The event(s) to be triggered. */
    private final TriggerEvent[] events;
    /** The log. */
    private final Log log = LogFactory.getLog(AsyncTrigger.class);

    /**
     * Constructor.
     *
     * @param executor The {@link SCXMLExecutor} to trigger the event on.
     * @param event The {@link TriggerEvent}.
     */
    AsyncTrigger(final SCXMLExecutor executor, final TriggerEvent event) {
        this.executor = executor;
        this.events = new TriggerEvent[1];
        this.events[0] = event;
    }

    /**
     * Fire the trigger(s) asynchronously.
     */
    public void start() {
        new Thread(this).start();
    }

    /**
     * Fire the event(s).
     */
    public void run() {
        try {
            synchronized (executor) {
                executor.triggerEvents(events);
            }
        } catch (ModelException me) {
            log.error(me.getMessage(), me);
        }
    }

}

