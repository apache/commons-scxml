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
package org.apache.commons.scxml2.env;

import java.io.Serializable;

import javax.xml.stream.Location;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.SCXMLListener;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple tracer connected to Apache Commons Logging.
 */
public class Tracer implements ErrorHandler, ErrorReporter,
                               SCXMLListener, Serializable, XMLReporter {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** ErrorHandler delegate. */
    private final ErrorHandler errHandler;
    /** ErrorReporter delegate. */
    private final ErrorReporter errReporter;
    /** SCXMLListener delegate. */
    private final SCXMLListener scxmlListener;
    /** XMLReporter delegate. */
    private final XMLReporter xmlReporter;

    /**
     * Constructs a new instance.
     */
    public Tracer() {
        errHandler = new SimpleErrorHandler();
        errReporter = new SimpleErrorReporter();
        scxmlListener = new SimpleSCXMLListener();
        xmlReporter = new SimpleXMLReporter();
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(final SAXParseException exception)
    throws SAXException {
        errHandler.error(exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(final SAXParseException exception)
    throws SAXException {
        errHandler.fatalError(exception);
    }

    /**
     * @see SCXMLListener#onEntry(EnterableState)
     */
    @Override
    public void onEntry(final EnterableState state) {
        scxmlListener.onEntry(state);
    }

    /**
     * @see ErrorReporter#onError(String, String, Object)
     */
    @Override
    public void onError(final String errCode, final String errDetail,
            final Object errCtx) {
        errReporter.onError(errCode, errDetail, errCtx);
    }

    /**
     * @see SCXMLListener#onExit(EnterableState)
     */
    @Override
    public void onExit(final EnterableState state) {
        scxmlListener.onExit(state);
    }

    /**
     * @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition,String)
     */
    @Override
    public void onTransition(final TransitionTarget from,
            final TransitionTarget to, final Transition transition, final String event) {
        scxmlListener.onTransition(from, to, transition, event);
    }

    /**
     * @see XMLReporter#report(String, String, Object, Location)
     */
	@Override
    public void report(final String message, final String errorType, final Object relatedInformation,
			final Location location)
	throws XMLStreamException {
		xmlReporter.report(message, errorType, relatedInformation, location);
	}

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(final SAXParseException exception)
    throws SAXException {
        errHandler.warning(exception);
    }

}

