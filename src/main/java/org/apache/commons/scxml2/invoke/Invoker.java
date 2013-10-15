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

import java.util.Map;

import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.TriggerEvent;

/**
 * <p>The Invoker interface is used to define the possible interactions
 * between the parent state machine (executor) and the types of invocable
 * activities.</p>
 *
 * <p>Invocable activities must first register an Invoker implementation class
 * for the appropriate "target" (attribute of &lt;invoke&gt;) with the
 * parent <code>SCXMLExecutor</code>.</p>
 *
 * <p>The communication link between the parent state machine executor and
 * the invoked activity is a bi-directional events pipe.</p>
 *
 * <p>All events triggered on the parent state machine get forwarded to the
 * invoked activity. The processing semantics for these events depend
 * upon the "target", and thereby vary per concrete implementation of
 * this interface.</p>
 *
 * <p>The invoked activity in turn must fire a special "done" event
 * when it concludes. It may fire additional events before the "done"
 * event. The semantics of any additional events depend upon the
 * "target". The invoked activity must not fire any events after the "done"
 * event. The name of the special "done" event must be the ID of the parent
 * state wherein the corresponding &lt;invoke&gt; resides, with the String
 * ".invoke.done" appended.</p>
 *
 * <p>The Invoker "lifecycle" is outlined below:
 *  <ol>
 *   <li>Instantiation via {@link Class#newInstance()}
 *       (Invoker implementation requires accessible constructor).</li>
 *   <li>Configuration (setters for parent state ID and
 *       {@link SCInstance}).</li>
 *   <li>Initiation of invoked activity via invoke() method, passing
 *       the source URI and the map of params.</li>
 *   <li>Zero or more bi-directional event triggering.</li>
 *   <li>Either completion or cancellation.</li>
 *  </ol>
 * </p>
 *
 * <p><b>Note:</b> The semantics of &lt;invoke&gt; are necessarily
 * asynchronous, tending towards long(er) running interactions with external
 * processes. Implementations must not communicate with the parent state
 * machine executor in a synchronous manner. For synchronous
 * communication semantics, use &lt;event&gt; or custom actions instead.</p>
 */
public interface Invoker {

    /**
     * Set the state ID of the owning state for the &lt;invoke&gt;.
     * Implementations must use this ID for constructing the event name for
     * the special "done" event (and optionally, for other event names
     * as well).
     *
     * @param parentStateId The ID of the parent state.
     */
    void setParentStateId(String parentStateId);

    /**
     * Set the "context" of the parent state machine, which provides the
     * channel.
     *
     * @param scInstance The "context" of the parent state machine.
     */
    void setSCInstance(SCInstance scInstance);

    /**
     * Begin this invocation.
     *
     * @param source The source URI of the activity being invoked.
     * @param params The &lt;param&gt; values
     * @throws InvokerException In case there is a fatal problem with
     *                          invoking the source.
     */
    void invoke(String source, Map<String, Object> params)
    throws InvokerException;

    /**
     * Forwards the events triggered on the parent state machine
     * on to the invoked activity.
     *
     * @param evts
     *            an array of external events which triggered during the last
     *            time quantum
     *
     * @throws InvokerException In case there is a fatal problem with
     *                          processing the events forwarded by the
     *                          parent state machine.
     */
    void parentEvents(TriggerEvent[] evts)
    throws InvokerException;

    /**
     * Cancel this invocation.
     *
     * @throws InvokerException In case there is a fatal problem with
     *                          canceling this invoke.
     */
    void cancel()
    throws InvokerException;

}

