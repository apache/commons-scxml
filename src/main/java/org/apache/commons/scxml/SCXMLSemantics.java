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

import java.util.List;
import java.util.Set;

import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * <p>The purpose of this interface is to separate the interpretation algorithm
 * from the <code>SCXMLExecutor</code> and therefore make it pluggable.</p>
 *
 * <p>Semantics agnostic utility functions and common operators as defined in
 * UML can be found in the <code>SCXMLHelper</code> or attached directly to
 * the SCXML model elements. Some of the possible semantic interpretations
 * are, for example:</p>
 *
 * <ul>
 * <li>STATEMATE
 * <li>RHAPSODY
 * <li>ROOMCharts
 * <li>UML 1.5
 * <li>UML 2.0
 * </ul>
 *
 * <p>Specific semantics can be created by subclassing
 * <code>org.apache.commons.scxml.semantics.SCXMLSemanticsImpl</code>.</p>
 */
public interface SCXMLSemantics {

    /**
     * Optional post processing immediately following Digester. May be used
     * for removing pseudo-states etc.
     *
     * @param input
     *            SCXML state machine
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     * @param errRep
     *            ErrorReporter callback
     */
    SCXML normalizeStateMachine(final SCXML input, final ErrorReporter errRep);

    /**
     * Determining the initial state(s) for this state machine.
     *
     * @param input
     *            SCXML state machine
     * @param states
     *            a set of States to populate
     * @param entryList
     *            a list of States and Parallels to enter
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void determineInitialStates(final SCXML input, final Set<TransitionTarget> states,
            final List<TransitionTarget> entryList, final ErrorReporter errRep,
            final SCInstance scInstance)
    throws ModelException;

    /**
     * Executes all OnExit/Transition/OnEntry transitional actions.
     *
     * @param step
     *            provides EntryList, TransitList, ExitList gets
     *            updated its AfterStatus/Events
     * @param stateMachine
     *            state machine - SCXML instance
     * @param evtDispatcher
     *            the event dispatcher - EventDispatcher instance
     * @param errRep
     *            error reporter
     * @param scInstance
     *            The state chart instance
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void executeActions(final Step step, final SCXML stateMachine,
            final EventDispatcher evtDispatcher, final ErrorReporter errRep,
            final SCInstance scInstance)
    throws ModelException;

    /**
     * Enumerate all the reachable transitions.
     *
     * @param stateMachine
     *            a state machine to traverse
     * @param step
     *            with current status and list of transitions to populate
     * @param errRep
     *            ErrorReporter callback
     */
    void enumerateReachableTransitions(final SCXML stateMachine,
            final Step step, final ErrorReporter errRep);

    /**
     * Filter the transitions set, eliminate those whose guard conditions
     * are not satisfied.
     *
     * @param step
     *            with current status
     * @param evtDispatcher
     *            the event dispatcher - EventDispatcher instance
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void filterTransitionsSet(final Step step,
            final EventDispatcher evtDispatcher, final ErrorReporter errRep,
            final SCInstance scInstance)
    throws ModelException;

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param step The current Step
     * @param errorReporter The ErrorReporter for the current environment
     * @param scInstance The state chart instance
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void followTransitions(final Step step, final ErrorReporter errorReporter,
            final SCInstance scInstance)
    throws ModelException;

    /**
     * Go over the exit list and update history information for
     * relevant states.
     *
     * @param step
     *            The current Step
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     */
    void updateHistoryStates(final Step step, final ErrorReporter errRep,
            final SCInstance scInstance);

    /**
     * Forward events to invoked activities, execute finalize handlers.
     *
     * @param events
     *            The events to be forwarded
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void processInvokes(final TriggerEvent[] events,
            final ErrorReporter errRep, final SCInstance scInstance)
    throws ModelException;

    /**
     * Initiate any new invoked activities.
     *
     * @param step
     *            The current Step
     * @param errRep
     *            ErrorReporter callback
     * @param scInstance
     *            The state chart instance
     *
     */
    void initiateInvokes(final Step step, final ErrorReporter errRep,
            final SCInstance scInstance);

}

