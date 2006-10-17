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
package org.apache.commons.scxml.model;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;cancel&gt; SCXML element.
 *
 */
public class Cancel extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public Cancel() {
        super();
    }

    /**
     * The ID of the send message that should be cancelled.
     */
    private String sendid;

    /**
     * Get the ID of the send message that should be cancelled.
     *
     * @return Returns the sendid.
     */
    public String getSendid() {
        return sendid;
    }

    /**
     * Set the ID of the send message that should be cancelled.
     *
     * @param sendid The sendid to set.
     */
    public void setSendid(final String sendid) {
        this.sendid = sendid;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection derivedEvents)
    throws ModelException, SCXMLExpressionException {
        evtDispatcher.cancel(sendid);
    }

}

