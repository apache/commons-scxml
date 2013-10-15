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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.TriggerEvent;

/**
 * Our custom &quot;hello world&quot; action.
 */
public class Hello extends Action {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** This is who we say hello to. */
    private String name;
    /** We count callbacks to execute() as part of the test suite. */
    public static int callbacks = 0;

    /** Public constructor is needed for the I in SCXML IO. */
    public Hello() {
        super();
    }

    /**
     * Get the name.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection<TriggerEvent> derivedEvents)
    throws ModelException, SCXMLExpressionException {
        if (appLog.isInfoEnabled()) {
            appLog.info("Hello " + name);
        }
        // For derived events payload testing
        TriggerEvent event =
            new TriggerEvent("helloevent", TriggerEvent.SIGNAL_EVENT, name);
        derivedEvents.add(event);
        callbacks++;
    }
}

