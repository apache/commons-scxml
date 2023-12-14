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
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;transition&gt; SCXML element. Transition rules are triggered
 * by &quot;events&quot; and conditionalized via
 * &quot;guard-conditions&quot;.
 */
public class Transition extends SimpleTransition implements DocumentOrder {

    /**
     * The document order of this transition
     */
    private int order;

    /**
     * Property that specifies the trigger(s) for this transition.
     */
    private String event;

    /**
     * This transition event descriptors
     */
    private List<String> events = Collections.emptyList();

    /**
     * Indicator for a event-less transition
     */
    private boolean noEvents;

    /**
     * Indicator for a transition matching all events (*)
     */
    private boolean allEvents;

    /**
     * Optional guard condition.
     */
    private String cond;

    /**
     * Constructs a new instance.
     */
    public Transition() {
    }

    /**
     * Gets the guard condition (may be null).
     *
     * @return Returns the cond.
     */
    public String getCond() {
        return cond;
    }

    /**
     * Gets the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @return Returns the event.
     */
    public String getEvent() {
        return event;
    }

    /**
     * @return The list of this transition event descriptors
     */
    public final List<String> getEvents() {
        return events;
    }

    /**
     * @return the document order of this transition
     * @see DocumentOrder
     */
    @Override
    public final int getOrder() {
        return order;
    }

    /**
     * @return True if this transition matches any events (*)
     */
    public final boolean isAllEventsTransition() {
        return allEvents;
    }

    /**
     * @return True if this transition is event-less
     */
    public final boolean isNoEventsTransition() {
        return noEvents;
    }

    /**
     * Sets the guard condition.
     *
     * @param cond The cond to set.
     */
    public void setCond(final String cond) {
        this.cond = cond;
    }

    /**
     * Sets the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @param event The event to set.
     */
    public void setEvent(final String event) {
        this.event = event == null ? null : event.trim();
        if (this.event != null) {
            // 'event' is a space separated list of event descriptors
            events = new ArrayList<>();
            final StringTokenizer st = new StringTokenizer(this.event);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.equals("*") || token.equals(".*")) {
                    events.clear();
                    events.add("*");
                    break;
                }
                if (token.endsWith("*")) {
                    token = token.substring(0, token.length()-1);
                }
                if (token.endsWith(".")) {
                    token = token.substring(0, token.length()-1);
                }
                if (!token.isEmpty()) {
                    events.add(token);
                }
            }
        }
        else {
            events = Collections.emptyList();
        }
        noEvents = events.isEmpty();
        allEvents = !noEvents && events.get(0).equals("*");
    }

    /**
     * Sets the document order of this transition
     * @param order the document order
     * @see DocumentOrder
     */
    public final void setOrder(final int order) {
        this.order = order;
    }
}
