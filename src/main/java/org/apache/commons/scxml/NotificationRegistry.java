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
package org.apache.commons.scxml;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.scxml.model.Observable;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * The registry where SCXML listeners are recorded for nodes of
 * interest such as the <code>SCXML</code> root,
 * <code>TransitionTarget</code>s and <code>Transition</code>s.
 * The notification registry keeps track of all
 * <code>SCXMLListener</code>s attached and notifies relevant
 * listeners of the events that interest them.
 *
 */
public final class NotificationRegistry implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Map of all listeners keyed by Observable.
     */
    private final Map<Observable, Set<SCXMLListener>> regs;

    /**
     * Constructor.
     */
    public NotificationRegistry() {
        this.regs = Collections.synchronizedMap(new HashMap<Observable, Set<SCXMLListener>>());
    }

    /**
     * Register this SCXMLListener for this Observable.
     *
     * @param source The observable this listener wants to listen to
     * @param lst The listener
     */
    synchronized void addListener(final Observable source, final SCXMLListener lst) {
        Set<SCXMLListener> entries = regs.get(source);
        if (entries == null) {
            entries = new HashSet<SCXMLListener>();
            regs.put(source, entries);
        }
        entries.add(lst);
    }

    /**
     * Deregister this SCXMLListener for this Observable.
     *
     * @param source The observable this listener wants to stop listening to
     * @param lst The listener
     */
    synchronized void removeListener(final Observable source, final SCXMLListener lst) {
        Set<SCXMLListener> entries = regs.get(source);
        if (entries != null) {
            entries.remove(lst);
            if (entries.size() == 0) {
                regs.remove(source);
            }
        }
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * entered.
     *
     * @param source The Observable
     * @param state The TransitionTarget that was entered
     */
    public synchronized void fireOnEntry(final Observable source,
            final TransitionTarget state) {
        Set<SCXMLListener> entries = regs.get(source);
        if (entries != null) {
            for (SCXMLListener lst : entries) {
                lst.onEntry(state);
            }
        }
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * exited.
     *
     * @param source The Observable
     * @param state The TransitionTarget that was exited
     */
    public synchronized void fireOnExit(final Observable source,
            final TransitionTarget state) {
        Set<SCXMLListener> entries = regs.get(source);
        if (entries != null) {
            for (SCXMLListener lst : entries) {
                lst.onExit(state);
            }
        }
    }

    /**
     * Inform all relevant listeners of a transition that has occured.
     *
     * @param source The Observable
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that was taken
     */
    public synchronized void fireOnTransition(final Observable source,
            final TransitionTarget from, final TransitionTarget to,
            final Transition transition) {
        Set<SCXMLListener> entries = regs.get(source);
        if (entries != null) {
            for (SCXMLListener lst : entries) {
                lst.onTransition(from, to, transition);
            }
        }
    }

}

