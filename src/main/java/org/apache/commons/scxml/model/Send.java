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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.semantics.ErrorConstants;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;send&gt; SCXML element.
 *
 */
public class Send extends Action implements ExternalContent {

    /**
     * The default targettype.
     */
    private static final String TARGETTYPE_SCXML = "scxml";

    /**
     * The spec mandated derived event when target cannot be reached
     * for TARGETTYPE_SCXML.
     */
    private static final String EVENT_ERR_SEND_TARGETUNAVAILABLE =
        "error.send.targetunavailable";

    /**
     * The ID of the send message.
     */
    private String sendid;

    /**
     * An expression returning the target location of the event.
     */
    private String target;

    /**
     * The type of the Event I/O Processor that the event.
     * should be dispatched to
     */
    private String targettype;

    /**
     * The event is dispatched after the delay interval elapses.
     */
    private String delay;

    /**
     * The data containing information which may be used by the
     * implementing platform to configure the event processor.
     */
    private String hints;

    /**
     * The namelist to the sent.
     */
    private String namelist;

    /**
     * The list of external nodes associated with this &lt;send&gt; element.
     */
    private List externalNodes;

    /**
     * The type of event being generated.
     */
    private String event;

    /**
     * OutputFormat used to serialize external nodes.
     *
    private static final OutputFormat format;
    static {
        format = new OutputFormat();
        format.setOmitXMLDeclaration(true);
    }
    */

    /**
     * Constructor.
     */
    public Send() {
        super();
        this.externalNodes = new ArrayList();
        this.targettype = TARGETTYPE_SCXML;
    }

    /**
     * Get the delay.
     *
     * @return Returns the delay.
     */
    public final String getDelay() {
        return delay;
    }

    /**
     * Set the delay.
     *
     * @param delay The delay to set.
     */
    public final void setDelay(final String delay) {
        this.delay = delay;
    }

    /**
     * Get the list of external namespaced child nodes.
     *
     * @return List Returns the list of externalnodes.
     */
    public final List getExternalNodes() {
        return externalNodes;
    }

    /**
     * Set the list of external namespaced child nodes.
     *
     * @param externalNodes The externalnode to set.
     */
    public final void setExternalNodes(final List externalNodes) {
        this.externalNodes = externalNodes;
    }

    /**
     * Get the hints for this &lt;send&gt; element.
     *
     * @return String Returns the hints.
     */
    public final String getHints() {
        return hints;
    }

    /**
     * Set the hints for this &lt;send&gt; element.
     *
     * @param hints The hints to set.
     */
    public final void setHints(final String hints) {
        this.hints = hints;
    }

    /**
     * Get the namelist.
     *
     * @return String Returns the namelist.
     */
    public final String getNamelist() {
        return namelist;
    }

    /**
     * Set the namelist.
     *
     * @param namelist The namelist to set.
     */
    public final void setNamelist(final String namelist) {
        this.namelist = namelist;
    }

    /**
     * Get the identifier for this &lt;send&gt; element.
     *
     * @return String Returns the sendid.
     */
    public final String getSendid() {
        return sendid;
    }

    /**
     * Set the identifier for this &lt;send&gt; element.
     *
     * @param sendid The sendid to set.
     */
    public final void setSendid(final String sendid) {
        this.sendid = sendid;
    }

    /**
     * Get the target for this &lt;send&gt; element.
     *
     * @return String Returns the target.
     */
    public final String getTarget() {
        return target;
    }

    /**
     * Set the target for this &lt;send&gt; element.
     *
     * @param target The target to set.
     */
    public final void setTarget(final String target) {
        this.target = target;
    }

    /**
     * Get the target type for this &lt;send&gt; element.
     *
     * @return String Returns the targettype.
     */
    public final String getTargettype() {
        return targettype;
    }

    /**
     * Set the target type for this &lt;send&gt; element.
     *
     * @param targettype The targettype to set.
     */
    public final void setTargettype(final String targettype) {
        this.targettype = targettype;
    }

    /**
     * Get the event to send.
     *
     * @param event The event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Set the event to send.
     *
     * @return String Returns the event.
     */
    public final String getEvent() {
        return event;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection derivedEvents)
    throws ModelException, SCXMLExpressionException {
        // Lets see if we should handle it ourselves
        if (targettype != null && targettype.trim().toLowerCase().
                equals(TARGETTYPE_SCXML)) {
            if (SCXMLHelper.isStringEmpty(target)) {
                derivedEvents.add(new TriggerEvent(event,
                    TriggerEvent.SIGNAL_EVENT));
            } else {
                // We know of no other
                appLog.warn("<send>: Unavailable target - " + target);
                derivedEvents.add(new TriggerEvent(
                    EVENT_ERR_SEND_TARGETUNAVAILABLE,
                    TriggerEvent.ERROR_EVENT));
            }
            // short-circuit the EventDispatcher
            return;
        }
        // Else, let the EventDispatcher take care of it
        State parentState = getParentState();
        Context ctx = scInstance.getContext(parentState);
        Evaluator eval = scInstance.getEvaluator();
        Object hintsValue = null;
        if (!SCXMLHelper.isStringEmpty(hints)) {
            hintsValue = eval.eval(ctx, hints);
        }
        Map params = null;
        if (!SCXMLHelper.isStringEmpty(namelist)) {
            StringTokenizer tkn = new StringTokenizer(namelist);
            params = new HashMap(tkn.countTokens());
            while (tkn.hasMoreTokens()) {
                String varName = tkn.nextToken();
                Object varObj = ctx.get(varName);
                if (varObj == null) {
                    //considered as a warning here
                    errRep.onError(ErrorConstants.UNDEFINED_VARIABLE,
                            varName + " = null", parentState);
                }
                params.put(varName, varObj);
            }
        }
        long wait = 0L;
        if (delay != null && delay.length() > 0) {
            try {
                wait = Long.parseLong(delay.trim());
            } catch (NumberFormatException nfe) {
                appLog.warn("Could not parse delay for <send>, "
                    + "it will be treated as immediate", nfe);
            }
        }
        evtDispatcher.send(sendid, target, targettype, event, params,
            hintsValue, wait, externalNodes);
    }

}

