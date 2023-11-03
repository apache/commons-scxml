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

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.TriggerEvent;

/**
 * ActionExecutionError is a specific RuntimeException used for communicating the execution failure of an action.
 * This exception when thrown from within {@link Action#execute(ActionExecutionContext)} will:
 * <ul>
 *     <li>stop further execution of (possible) following action within the same executable content block</li>
 *     <li>if not yet {@link #isEventRaised()} raise the internal error event {@link TriggerEvent#ERROR_EXECUTION}</li>
 *     <li>if a non-null error message is provided with the exception, report an error message with {@link ErrorReporter}</li>
 * </ul>
 * @see <a href="https://www.w3.org/TR/2015/REC-scxml-20150901/#EvaluationofExecutableContent">SCXML spec 4.9 Evaluation of Executable Content</a>
 */
public final class ActionExecutionError extends RuntimeException {

    private final boolean eventRaised;

    public ActionExecutionError() {
        this(false, null);
    }

    public ActionExecutionError(final boolean eventRaised) {
        this(eventRaised, null);
    }

    public ActionExecutionError(final boolean eventRaised, final String message) {
        super(message);
        this.eventRaised = eventRaised;
    }

    public ActionExecutionError(final String message) {
        this(false, message);
    }

    public boolean isEventRaised() {
        return eventRaised;
    }
}
