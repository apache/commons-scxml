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
package org.apache.commons.scxml.env;

import java.io.Serializable;

import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Custom {@link XMLReporter} that logs the StAX parsing warnings in the
 * SCXML document.
 *
 * @since 1.0
 */
public class SimpleXMLReporter implements XMLReporter, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Log. */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Constructor.
     */
    public SimpleXMLReporter() {
        super();
    }

    /**
     * @see XMLReporter#report(String, String, Object, Location)
     */
    public void report(final String message, final String errorType, final Object relatedInformation,
            final Location location)
    throws XMLStreamException {
        if (log.isWarnEnabled()) {
            log.warn("[" + errorType + "] " + message + " (" + relatedInformation + ") at " + location);
        }

    }

}
