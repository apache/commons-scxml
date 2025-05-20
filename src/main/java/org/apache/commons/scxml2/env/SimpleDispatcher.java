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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.EventBuilder;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.ParentSCXMLIOProcessor;
import org.apache.commons.scxml2.SCXMLIOProcessor;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.ActionExecutionError;

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
 */
public class SimpleDispatcher implements EventDispatcher, Serializable {

     /**
     * TimerTask implementation.
     */
    final class DelayedEventTask extends TimerTask {

        /**
         * The ID of the &lt;send&gt; element.
         */
        private final String id;

        /**
         * The event
         */
        private final TriggerEvent event;

        /**
         * The target io processor
         */
        private final SCXMLIOProcessor target;

        /**
         * Constructor for events with payload.
         *
         * @param id The ID of the send element.
         * @param event The event to be triggered.
         * @param target The target io processor
         */
        DelayedEventTask(final String id, final TriggerEvent event, final SCXMLIOProcessor target) {
            this.id = id;
            this.event = event;
            this.target = target;
        }

        /**
         * What to do when timer expires.
         */
        @Override
        public void run() {
            timers.remove(id);
            target.addEvent(event);
            if (log.isDebugEnabled()) {
                log.debug("Fired event '" + event.getName() + "' as scheduled by "
                        + "<send> with id '" + id + "'");
            }
        }
    }

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Implementation independent log category. */
     private static final Log log = LogFactory.getLog(EventDispatcher.class);

    /**
     * The <code>Map</code> of active <code>Timer</code>s, keyed by
     * &lt;send&gt; element <code>id</code>s.
     */
    private final Map<String, Timer> timers = Collections.synchronizedMap(new HashMap<String, Timer>());

    /**
     * @see EventDispatcher#cancel(String)
     */
    @Override
    public void cancel(final String sendId) {
        if (log.isInfoEnabled()) {
            log.info("cancel( sendId: " + sendId + ")");
        }
        if (!timers.containsKey(sendId)) {
            return; // done, we don't track this one or its already expired
        }
        final Timer timer = timers.get(sendId);
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
     * Gets the log instance.
     *
     * @return The current log instance
     */
    protected Log getLog() {
        return log;
    }

    /**
     * Gets the current timers.
     *
     * @return The currently scheduled timers
     */
    protected Map<String, Timer> getTimers() {
        return timers;
    }

    @Override
    public SimpleDispatcher newInstance() {
        return new SimpleDispatcher();
    }

    /**
    @see EventDispatcher#send(java.util.Map, String, String, String, String, Object, Object, long)
     */
    @Override
    public void send(final Map<String, SCXMLIOProcessor> ioProcessors, final String id, final String target,
            final String type, final String event, final Object data, final Object hints, final long delay) {
        if (log.isInfoEnabled()) {
            final String buf =
                    "send ( id: " + id +
                    ", target: " + target +
                    ", type: " + type +
                    ", event: " + event +
                    ", data: " + String.valueOf(data) +
                    ", hints: " + String.valueOf(hints) +
                    ", delay: " + delay +
                    ')';
            log.info(buf);
        }

        // We only handle the "scxml" type (which is the default too) and optionally the #_internal target

        if (type != null && !type.equalsIgnoreCase(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR) && !type.equals(SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR)) {
            ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR)
                    .addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).sendId(id).build());
            throw new ActionExecutionError(true, "<send>: Unsupported type - " + type);
        }
        final String originType = SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR;
        SCXMLIOProcessor ioProcessor;

        boolean internal = false;

        String origin = target;
        if (target == null) {
            ioProcessor = ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR);
            origin = SCXMLIOProcessor.SCXML_EVENT_PROCESSOR;
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
            if (target.startsWith(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX)) {
                ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).addEvent(
                        new EventBuilder(TriggerEvent.ERROR_COMMUNICATION, TriggerEvent.ERROR_EVENT)
                                .sendId(id).build());
                throw new ActionExecutionError(true, "<send>: Unavailable target - " + target);
            }
            ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).addEvent(
                    new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT)
                            .sendId(id).build());
            throw new ActionExecutionError(true, "<send>: Invalid or unsupported target - " + target);
        }

        if (event == null) {
            ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR)
                    .addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).sendId(id).build());
            throw new ActionExecutionError(true, "<send>: Cannot send without event name");
        }
        final EventBuilder eventBuilder = new EventBuilder(event, TriggerEvent.SIGNAL_EVENT)
                .sendId(id)
                .data(data);
        if (!internal) {
            eventBuilder.origin(origin).originType(originType);
            if (SCXMLIOProcessor.PARENT_EVENT_PROCESSOR.equals(target)) {
                eventBuilder.invokeId(((ParentSCXMLIOProcessor)ioProcessor).getInvokeId());
            }
            if (delay > 0L) {
                // Need to schedule this one
                final Timer timer = new Timer(true);
                timer.schedule(new DelayedEventTask(id, eventBuilder.build(), ioProcessor), delay);
                timers.put(id, timer);
                if (log.isDebugEnabled()) {
                    log.debug("Scheduled event '" + event + "' with delay "
                            + delay + "ms, as specified by <send> with id '"
                            + id + "'");
                }
                return;
            }
        }
        ioProcessor.addEvent(eventBuilder.build());
    }
}

