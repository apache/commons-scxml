/*
 *
 *   Copyright 2005 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.commons.scxml;

import java.util.List;
import java.util.Set;

import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;

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
     * @param input
     *            SCXML state machine
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     * @param errRep
     *            ErrorReporter callback
     */
    SCXML normalizeStateMachine(final SCXML input, final ErrorReporter errRep);

    /**
     * @param input
     *            SCXML state machine [in]
     * @param states
     *            a set of States to populate [out]
     * @param entryList
     *            a list of States and Parallels to enter [out]
     * @param errRep
     *            ErrorReporter callback [inout]
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void determineInitialStates(final SCXML input, final Set states,
            final List entryList, final ErrorReporter errRep)
    throws ModelException;

    /**
     * Exectutes all OnExit/Transition/OnEntry transitional actions.
     *
     * @param step
     *            [inout] provides EntryList, TransitList, ExitList gets
     *            updated its AfterStatus/Events
     * @param exec
     *            [inout] execution environment - SCXMLExecutor instance
     * @param errRep
     *            [out[ error reporter
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void executeActions(final Step step, final SCXML stateMachine,
            final Evaluator eval, final EventDispatcher evtDispatcher,
            final ErrorReporter errRep)
    throws ModelException;

    /**
     * @param stateMachine
     *            a SM to traverse [in]
     * @param step
     *            with current status and list of transitions to populate
     *            [inout]
     * @param errRep
     *            ErrorReporter callback [inout]
     */
    void enumerateReachableTransitions(final SCXML stateMachine,
            final Step step, final ErrorReporter errRep);

    /**
     * @param step
     *            [inout]
     * @param evaluator
     *            guard condition evaluator
     * @param errRep
     *            ErrorReporter callback [inout]
     */
    void filterTransitionsSet(final Step step, final Evaluator evaluator,
            final ErrorReporter errRep);

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param step The current Step
     * @param errorReporter The ErrorReporter for the current environment
     *
     * @throws ModelException
     *             in case there is a fatal SCXML object model problem.
     */
    void followTransitions(final Step step, final ErrorReporter errorReporter)
    throws ModelException;

    /**
     * Go over the exit list and update history information for
     * relevant states.
     *
     * @param step
     *            [inout]
     * @param errRep
     *            ErrorReporter callback [inout]
     */
    void updateHistoryStates(final Step step, final ErrorReporter errRep);

}

