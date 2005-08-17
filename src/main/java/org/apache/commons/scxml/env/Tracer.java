/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
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
package org.apache.taglibs.rdc.scxml.env;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.rdc.scxml.ErrorReporter;
import org.apache.taglibs.rdc.scxml.SCXMLListener;
import org.apache.taglibs.rdc.scxml.model.SCXML;
import org.apache.taglibs.rdc.scxml.model.State;
import org.apache.taglibs.rdc.scxml.model.Transition;
import org.apache.taglibs.rdc.scxml.model.TransitionTarget;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A simple logger connected to Jakarta Commons Logging.
 * 
 * @author Jaroslav Gergic
 */
public class Tracer implements ErrorReporter, SCXMLListener, ErrorHandler {

    private static Log log = LogFactory.getLog(Tracer.class);
    
    /**
     * Constructor
     */
    public Tracer() {
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.ErrorReporter#onError(java.lang.String, java.lang.String, java.lang.Object)
     */
    public void onError(String errCode, String errDetail, Object errCtx) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes
        errCode = errCode.intern();
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
                msg.append("State " + Tracer.getTTPath((State)errCtx));
            }
        } else if (errCode == ErrorReporter.UNKNOWN_ACTION) {
            //executeActionList
            msg.append("Action: " + errCtx.getClass().getName());
        } else if (errCode == ErrorReporter.NON_DETERMINISTIC) {
            //filterTransitionSet
            msg.append(" [");
            if(errCtx instanceof HashSet) {
                Iterator i = ((HashSet)errCtx).iterator();
                while(i.hasNext()) {
                    Transition t = (Transition)i.next();
                    msg.append(transToString(t.getParent(), t.getTarget(), t));
                    if(i.hasNext()) {
                        msg.append(", ");
                    }
                }
            }
            msg.append(']');
        } else if (errCode == ErrorReporter.ILLEGAL_CONFIG) {
            //isLegalConfig
            if (errCtx instanceof Map.Entry) {
                TransitionTarget tt = (TransitionTarget)(((Map.Entry)errCtx).getKey());
                HashSet vals = (HashSet)(((Map.Entry)errCtx).getValue());
                msg.append(Tracer.getTTPath(tt) + " : [");
                Iterator i = vals.iterator();
                while(i.hasNext()) {
                    TransitionTarget tx = (TransitionTarget)i.next();
                    msg.append(Tracer.getTTPath(tx));
                    if(i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            } else if (errCtx instanceof HashSet) {
                HashSet vals = (HashSet)(errCtx);
                msg.append("<SCXML> : [");
                Iterator i = vals.iterator();
                while(i.hasNext()) {
                    TransitionTarget tx = (TransitionTarget)i.next();
                    msg.append(Tracer.getTTPath(tx));
                    if(i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            }
        }
        log.warn(msg.toString());
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.SCXMLListener#onEntry(org.apache.taglibs.rdc.scxml.model.TransitionTarget)
     */
    public void onEntry(TransitionTarget state) {
        log.info(Tracer.getTTPath(state));
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.SCXMLListener#onExit(org.apache.taglibs.rdc.scxml.model.TransitionTarget)
     */
    public void onExit(TransitionTarget state) {
        log.info(Tracer.getTTPath(state));
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.SCXMLListener#onTransition(org.apache.taglibs.rdc.scxml.model.TransitionTarget, org.apache.taglibs.rdc.scxml.model.TransitionTarget, org.apache.taglibs.rdc.scxml.model.Transition)
     */
    public void onTransition(TransitionTarget from, TransitionTarget to,
            Transition transition) {
        log.info(transToString(from, to, transition));
    }
    
    private static final String transToString(TransitionTarget from,
            TransitionTarget to, Transition transition) {
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
    public void warning(SAXParseException exception) throws SAXException {
        log.warn(exception.getMessage(), exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        log.error(exception.getMessage(), exception);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        log.fatal(exception.getMessage(), exception);
    }

    private static final String getTTPath(TransitionTarget tt) {
        if(tt.getParent() == null) {
            return "/" + tt.getId();
        } else {
            LinkedList ll = new LinkedList();
            while(tt != null) {
                ll.addFirst(tt);
                tt = tt.getParent();
            }
            Iterator i = ll.iterator();
            StringBuffer names = new StringBuffer();
            while(i.hasNext()) {
                TransitionTarget tmp = (TransitionTarget)i.next();
                names.append('/').append(tmp.getId());
            }
            return names.toString();
        }
    }

}
