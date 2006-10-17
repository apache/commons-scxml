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

import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple tracer connected to Jakarta Commons Logging.
 *
 */
public class Tracer implements ErrorHandler, ErrorReporter,
                               SCXMLListener, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** ErrorHandler delegate. */
    private ErrorHandler errHandler;
    /** ErrorReporter delegate. */
    private ErrorReporter errReporter;
    /** SCXMLListener delegate. */
    private SCXMLListener scxmlListener;

    /**
     * Constructor.
     */
    public Tracer() {
        super();
        errHandler = new SimpleErrorHandler();
        errReporter = new SimpleErrorReporter();
        scxmlListener = new SimpleSCXMLListener();
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(final SAXParseException exception)
    throws SAXException {
        errHandler.warning(exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(final SAXParseException exception)
    throws SAXException {
        errHandler.error(exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(final SAXParseException exception)
    throws SAXException {
        errHandler.fatalError(exception);
    }

    /**
     * @see ErrorReporter#onError(String, String, Object)
     */
    public void onError(final String errCode, final String errDetail,
            final Object errCtx) {
        errReporter.onError(errCode, errDetail, errCtx);
    }

    /**
     * @see SCXMLListener#onEntry(TransitionTarget)
     */
    public void onEntry(final TransitionTarget target) {
        scxmlListener.onEntry(target);
    }

    /**
     * @see SCXMLListener#onExit(TransitionTarget)
     */
    public void onExit(final TransitionTarget target) {
        scxmlListener.onExit(target);
    }

    /**
* @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition)
     */
    public void onTransition(final TransitionTarget from,
            final TransitionTarget to, final Transition transition) {
        scxmlListener.onTransition(from, to, transition);
    }

}

