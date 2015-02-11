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
package org.apache.commons.scxml2.env;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCXMLIOProcessor;
import org.apache.commons.scxml2.TriggerEvent;

/**
 * <p>EventDispatcher implementation that can schedule <code>delay</code>ed
 * &lt;send&gt; events for the &quot;scxml&quot; <code>type</code>
 * attribute value (which is also the default). This implementation uses
 * J2SE <code>Timer</code>s.</p>
 *
 * <p>No other <code>type</code>s are processed. Subclasses may support
 * additional <code>type</code>s by overriding the
 * <code>send(...)</code> and <code>cancel(...)</code> methods and
 * delegating to their <code>super</code> counterparts for the
 * &quot;scxml&quot; <code>type</code>.</p>
 *
 */
public class SimpleDispatcher implements EventDispatcher, Serializable {

     /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * TimerTask implementation.
     */
    class DelayedEventTask extends TimerTask {

        /**
         * The ID of the &lt;send&gt; element.
         */
        private String id;

        /**
         * The event name.
         */
        private String event;

        /**
         * The event payload, if any.
         */
        private Object payload;

        /**
         * The target io processor
         */
        private SCXMLIOProcessor target;

        /**
         * Constructor for events with payload.
         *
         * @param id The ID of the send element.
         * @param event The name of the event to be triggered.
         * @param payload The event payload, if any.
         * @param target The target io processor
         */
        DelayedEventTask(final String id, final String event, final Object payload, SCXMLIOProcessor target) {
            super();
            this.id = id;
            this.event = event;
            this.payload = payload;
            this.target = target;
        }

        /**
         * What to do when timer expires.
         */
        @Override
        public void run() {
            timers.remove(id);
            target.addEvent(new TriggerEvent(event, TriggerEvent.SIGNAL_EVENT, payload));
            if (log.isDebugEnabled()) {
                log.debug("Fired event '" + event + "' as scheduled by "
                        + "<send> with id '" + id + "'");
            }
        }
    }

    /** Implementation independent log category. */
     private Log log = LogFactory.getLog(EventDispatcher.class);

    /**
     * The <code>Map</code> of active <code>Timer</code>s, keyed by
     * &lt;send&gt; element <code>id</code>s.
     */
    private Map<String, Timer> timers = Collections.synchronizedMap(new HashMap<String, Timer>());

    /**
     * Get the log instance.
     *
     * @return The current log instance
     */
    protected Log getLog() {
        return log;
    }

    /**
     * Sets the log instance
     *
     * @param log the new log instance
     */
    protected void setLog(Log log) {
        this.log = log;
    }

    /**
     * Get the current timers.
     *
     * @return The currently scheduled timers
     */
    protected Map<String, Timer> getTimers() {
        return timers;
    }

    /**
     * @see EventDispatcher#cancel(String)
     */
    public void cancel(final String sendId) {
        if (log.isInfoEnabled()) {
            log.info("cancel( sendId: " + sendId + ")");
        }
        if (!timers.containsKey(sendId)) {
            return; // done, we don't track this one or its already expired
        }
        Timer timer = timers.get(sendId);
        if (timer != null) {
            timer.cancel();
            if (log.isDebugEnabled()) {
                log.debug("Cancelled event scheduled by <send> with id '"
                        + sendId + "'");
            }
        }
        timers.remove(sendId);
    }

    /**
    @see EventDispatcher#send(java.util.Map, String, String, String, String, Object, Object, long)
     */
    public void send(final Map<String, SCXMLIOProcessor> ioProcessors, final String id, final String target,
            final String type, final String event, final Object data, final Object hints, final long delay) {
        if (log.isInfoEnabled()) {
            StringBuilder buf = new StringBuilder();
            buf.append("send ( id: ").append(id);
            buf.append(", target: ").append(target);
            buf.append(", type: ").append(type);
            buf.append(", event: ").append(event);
            buf.append(", data: ").append(String.valueOf(data));
            buf.append(", hints: ").append(String.valueOf(hints));
            buf.append(", delay: ").append(delay);
            buf.append(')');
            log.info(buf.toString());
        }

        // We only handle the "scxml" type (which is the default too) and optionally the #_internal target

        if (type == null || type.equalsIgnoreCase(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR) ||
                type.equals(SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR)) {

            SCXMLIOProcessor ioProcessor;

            boolean internal = false;

            if (target == null) {
                ioProcessor = ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR);
            }
            else if (ioProcessors.containsKey(target)) {
                ioProcessor = ioProcessors.get(target);
                internal = SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR.equals(target);
            }
            else if (SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR.equals(target)) {
                ioProcessor = ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR);
                internal = true;
            }
            else {
                // We know of no other target
                if (log.isWarnEnabled()) {
                    log.warn("<send>: Unavailable target - " + target);
                }
                ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).
                        addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
                return; // done
            }

            if (event == null) {
                if (log.isWarnEnabled()) {
                    log.warn("<send>: Cannot send without event name");
                }
                ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).
                        addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
            }

            else if (!internal && delay > 0L) {
                // Need to schedule this one
                Timer timer = new Timer(true);
                timer.schedule(new DelayedEventTask(id, event, data, ioProcessor), delay);
                timers.put(id, timer);
                if (log.isDebugEnabled()) {
                    log.debug("Scheduled event '" + event + "' with delay "
                            + delay + "ms, as specified by <send> with id '"
                            + id + "'");
                }
            }
            else {
                ioProcessor.addEvent(new TriggerEvent(event, TriggerEvent.SIGNAL_EVENT, data));
            }
        }
        else {
            if (log.isWarnEnabled()) {
                log.warn("<send>: Unsupported type - " + type);
            }
            ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).
                    addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
        }
    }
}

