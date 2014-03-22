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

import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;

/**
 * Listener interface for observable entities in the SCXML model.
 * Observable entities include {@link org.apache.commons.scxml2.model.SCXML}
 * instances (subscribe to all entry, exit and transition notifications),
 * {@link org.apache.commons.scxml2.model.State} instances (subscribe to
 * particular entry and exit notifications) and
 * {@link org.apache.commons.scxml2.model.Transition} instances (subscribe to
 * particular transitions).
 *
 */
public interface SCXMLListener {

    /**
     * Handle the entry into a EnterableState.
     *
     * @param state The EnterableState entered
     */
    void onEntry(EnterableState state);

    /**
     * Handle the exit out of a EnterableState.
     *
     * @param state The EnterableState exited
     */
    void onExit(EnterableState state);

    /**
     * Handle the transition.
     *
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition taken
     */
    void onTransition(TransitionTarget from, TransitionTarget to,
            Transition transition);

}

