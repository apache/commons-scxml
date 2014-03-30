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
package org.apache.commons.scxml2;

import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

/**
 * <p>The purpose of this interface is to separate the the
 * <a href="http://www.w3.org/TR/2014/CR-scxml-20140313/#AlgorithmforSCXMLInterpretation">
 *     W3C SCXML Algorithm for SCXML Interpretation</a>
 * from the <code>SCXMLExecutor</code> and therefore make it pluggable.</p>
 *
 * <p>Semantics agnostic utility functions and common operators as defined in
 * UML can be found in the <code>SCXMLHelper</code> or attached directly to
 * the SCXML model elements.</p>
 *
 * <p>Specific semantics can be created by subclassing
 * <code>org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl</code>.</p>
 */
public interface SCXMLSemantics {

    /**
     * Optional post processing immediately following SCXMLReader. May be used
     * for removing pseudo-states etc.
     *
     * @param input  SCXML state machine
     * @param errRep ErrorReporter callback
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     */
    SCXML normalizeStateMachine(final SCXML input, final ErrorReporter errRep);

    public void executeGlobalScript(final SCXMLExecutionContext context, final Step step) throws ModelException;

    public void exitStates(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    public void executeTransitionContent(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    public void enterStates(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    /**
     * Determining the initial state(s) for this state machine.
     *
     * @param ctx  provides the execution context
     * @param step provides target states and entry list to fill in [out]
     *
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object model problem.
     */
    void determineInitialStates(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    /**
     * Executes all OnExit/Transition/OnEntry transitional actions.
     *
     * @param ctx  provides the execution context
     * @param step provides EntryList, TransitList, ExitList gets updated its AfterStatus/Events
     *
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object model problem.
     */
    void executeActions(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    /**
     * Enumerate all the reachable transitions.
     *
     * @param ctx  provides the execution context
     * @param step with current status and list of transitions to populate
     */
    void enumerateReachableTransitions(final SCXMLExecutionContext ctx, final Step step);

    /**
     * Filter the transitions set, eliminate those whose guard conditions
     * are not satisfied.
     *
     * @param ctx  provides the execution context
     * @param step with current status
     *
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object model problem.
     */
    void filterTransitionsSet(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param ctx  provides the execution context
     * @param step The current Step
     *
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object model problem.
     */
    void followTransitions(final SCXMLExecutionContext ctx, final Step step) throws ModelException;

    /**
     * Go over the exit list and update history information for
     * relevant states.
     *
     * @param ctx  provides the execution context
     * @param step The current Step
     */
    void updateHistoryStates(final SCXMLExecutionContext ctx, final Step step);

    /**
     * Forward events to invoked activities, execute finalize handlers.
     *
     * @param ctx    provides the execution context
     * @param event The event to be forwarded
     *
     * @throws org.apache.commons.scxml2.model.ModelException in case there is a fatal SCXML object model problem.
     */
    void processInvokes(final SCXMLExecutionContext ctx, final TriggerEvent event) throws ModelException;

    /**
     * Initiate any new invoked activities.
     *
     * @param ctx  provides the execution context
     * @param step The current Step
     */
    void initiateInvokes(final SCXMLExecutor executor, final SCXMLExecutionContext ctx, final Step step);
}

