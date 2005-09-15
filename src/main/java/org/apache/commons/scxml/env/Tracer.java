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
package org.apache.commons.scxml.env;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.SCXMLListener;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple logger connected to Jakarta Commons Logging.
 *
 */
public class Tracer implements ErrorReporter, SCXMLListener, ErrorHandler {

    /** A Logger for the Tracer. */
    private static Log log = LogFactory.getLog(Tracer.class);

    /**
     * Constructor.
     */
    public Tracer() {
        super();
    }

    /**
     * @see ErrorReporter#onError(String, String, Object)
     */
    public void onError(final String errorCode, final String errDetail,
            final Object errCtx) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes
        String errCode = errorCode.intern();
        StringBuffer msg = new StringBuffer();
        msg.append(errCode).append(" (");
        msg.append(errDetail).append("): ");
        if (errCode == ErrorReporter.NO_INITIAL) {
            if (errCtx instanceof SCXML) {
                //determineInitialStates
                msg.append("<SCXML>");
            } else if (errCtx instanceof State) {
                //determineInitialStates
                //determineTargetStates
                msg.append("State " + Tracer.getTTPath((State) errCtx));
            }
        } else if (errCode == ErrorReporter.UNKNOWN_ACTION) {
            //executeActionList
            msg.append("Action: " + errCtx.getClass().getName());
        } else if (errCode == ErrorReporter.NON_DETERMINISTIC) {
            //filterTransitionSet
            msg.append(" [");
            if (errCtx instanceof HashSet) {
                for (Iterator i = ((Set) errCtx).iterator(); i.hasNext();) {
                    Transition t = (Transition) i.next();
                    msg.append(transToString(t.getParent(), t.getTarget(), t));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
            }
            msg.append(']');
        } else if (errCode == ErrorReporter.ILLEGAL_CONFIG) {
            //isLegalConfig
            if (errCtx instanceof Map.Entry) {
                TransitionTarget tt = (TransitionTarget)
                    (((Map.Entry) errCtx).getKey());
                Set vals = (Set) (((Map.Entry) errCtx).getValue());
                msg.append(Tracer.getTTPath(tt) + " : [");
                for (Iterator i = vals.iterator(); i.hasNext();) {
                    TransitionTarget tx = (TransitionTarget) i.next();
                    msg.append(Tracer.getTTPath(tx));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            } else if (errCtx instanceof Set) {
                Set vals = (Set) errCtx;
                msg.append("<SCXML> : [");
                for (Iterator i = vals.iterator(); i.hasNext();) {
                    TransitionTarget tx = (TransitionTarget) i.next();
                    msg.append(Tracer.getTTPath(tx));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            }
        }
        log.warn(msg.toString());
    }

    /**
     * @see SCXMLListener#onEntry(TransitionTarget)
     */
    public void onEntry(final TransitionTarget state) {
        log.info(Tracer.getTTPath(state));
    }

    /**
     * @see SCXMLListener#onExit(TransitionTarget)
     */
    public void onExit(final TransitionTarget state) {
        log.info(Tracer.getTTPath(state));
    }

    /**
* @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition)
     */
    public void onTransition(final TransitionTarget from,
            final TransitionTarget to, final Transition transition) {
        log.info(transToString(from, to, transition));
    }

    /**
     * Create a human readable log view of this transition.
     *
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that is taken
     * @return String The human readable log entry
     */
    private static String transToString(final TransitionTarget from,
            final TransitionTarget to, final Transition transition) {
        StringBuffer buf = new StringBuffer("transition (");
        buf.append("event = ").append(transition.getEvent());
        buf.append(", cond = ").append(transition.getCond());
        buf.append(", from = ").append(Tracer.getTTPath(from));
        buf.append(", to = ").append(Tracer.getTTPath(to));
        buf.append(')');
        return buf.toString();
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(final SAXParseException exception)
    throws SAXException {
        log.warn(exception.getMessage(), exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(final SAXParseException exception)
    throws SAXException {
        log.error(exception.getMessage(), exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(final SAXParseException exception)
    throws SAXException {
        log.fatal(exception.getMessage(), exception);
    }

    /**
     * Write out this TransitionTarget location in a XPath style format.
     *
     * @param tt The TransitionTarget whose &quot;path&quot; is to needed
     * @return String The XPath style location of the TransitionTarget within
     *                the SCXML document
     */
    private static String getTTPath(final TransitionTarget tt) {
        TransitionTarget parent = tt.getParent();
        if (parent == null) {
            return "/" + tt.getId();
        } else {
            LinkedList pathElements = new LinkedList();
            pathElements.addFirst(tt);
            while (parent != null) {
                pathElements.addFirst(parent);
                parent = parent.getParent();
            }
            StringBuffer names = new StringBuffer();
            for (Iterator i = pathElements.iterator(); i.hasNext();) {
                TransitionTarget pathElement = (TransitionTarget) i.next();
                names.append('/').append(pathElement.getId());
            }
            return names.toString();
        }
    }

}

