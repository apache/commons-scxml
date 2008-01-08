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
import org.w3c.dom.Node;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;send&gt; SCXML element.
 *
 */
public class Send extends Action implements ExternalContent {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

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
    private List<Node> externalNodes;

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
        this.externalNodes = new ArrayList<Node>();
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
    public final List<Node> getExternalNodes() {
        return externalNodes;
    }

    /**
     * Set the list of external namespaced child nodes.
     *
     * @param externalNodes The externalnode to set.
     */
    public final void setExternalNodes(final List<Node> externalNodes) {
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
            final Log appLog, final Collection<TriggerEvent> derivedEvents)
    throws ModelException, SCXMLExpressionException {
        // Send attributes evaluation
        State parentState = getParentState();
        Context ctx = scInstance.getContext(parentState);
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Evaluator eval = scInstance.getEvaluator();
        // Most attributes of <send> are expressions so need to be
        // evaluated before the EventDispatcher callback
        Object hintsValue = null;
        if (!SCXMLHelper.isStringEmpty(hints)) {
            hintsValue = eval.eval(ctx, hints);
        }
        String targetValue = target;
        if (!SCXMLHelper.isStringEmpty(target)) {
            targetValue = (String) eval.eval(ctx, target);
            if (SCXMLHelper.isStringEmpty(targetValue)
                    && appLog.isWarnEnabled()) {
                appLog.warn("<send>: target expression \"" + target
                    + "\" evaluated to null or empty String");
            }
        }
        String targettypeValue = targettype;
        if (!SCXMLHelper.isStringEmpty(targettype)) {
            targettypeValue = (String) eval.eval(ctx, targettype);
            if (SCXMLHelper.isStringEmpty(targettypeValue)
                    && appLog.isWarnEnabled()) {
                appLog.warn("<send>: targettype expression \"" + targettype
                    + "\" evaluated to null or empty String");
            }
        } else {
            // must default to 'scxml' when unspecified
            targettypeValue = TARGETTYPE_SCXML;
        }
        Map<String, Object> params = null;
        if (!SCXMLHelper.isStringEmpty(namelist)) {
            StringTokenizer tkn = new StringTokenizer(namelist);
            params = new HashMap<String, Object>(tkn.countTokens());
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
        long wait = parseDelay(appLog);
        // Lets see if we should handle it ourselves
        if (targettypeValue != null
              && targettypeValue.trim().equalsIgnoreCase(TARGETTYPE_SCXML)) {
            if (SCXMLHelper.isStringEmpty(targetValue)) {
                // TODO: Remove both short-circuit passes in v1.0
                if (wait == 0L) {
                    if (appLog.isDebugEnabled()) {
                        appLog.debug("<send>: Enqueued event '" + event
                            + "' with no delay");
                    }
                    derivedEvents.add(new TriggerEvent(event,
                        TriggerEvent.SIGNAL_EVENT, params));
                    return;
                }
            } else {
                // We know of no other
                if (appLog.isWarnEnabled()) {
                    appLog.warn("<send>: Unavailable target - "
                        + targetValue);
                }
                derivedEvents.add(new TriggerEvent(
                    EVENT_ERR_SEND_TARGETUNAVAILABLE,
                    TriggerEvent.ERROR_EVENT));
                // short-circuit the EventDispatcher
                return;
            }
        }
        ctx.setLocal(getNamespacesKey(), null);
        if (appLog.isDebugEnabled()) {
            appLog.debug("<send>: Dispatching event '" + event
                + "' to target '" + targetValue + "' of target type '"
                + targettypeValue + "' with suggested delay of " + wait
                + "ms");
        }
        // Else, let the EventDispatcher take care of it
        evtDispatcher.send(sendid, targetValue, targettypeValue, event,
            params, hintsValue, wait, externalNodes);
    }

    /**
     * Parse delay.
     *
     * @param appLog The application log
     * @return The parsed delay in milliseconds
     * @throws SCXMLExpressionException If the delay cannot be parsed
     */
    private long parseDelay(final Log appLog)
    throws SCXMLExpressionException {

        long wait = 0L;
        long multiplier = 1L;

        if (!SCXMLHelper.isStringEmpty(delay)) {

            String trimDelay = delay.trim();
            String numericDelay = trimDelay;
            if (trimDelay.endsWith(MILLIS)) {
                numericDelay = trimDelay.substring(0, trimDelay.length() - 2);
            } else if (trimDelay.endsWith(SECONDS)) {
                multiplier = MILLIS_IN_A_SECOND;
                numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
            } else if (trimDelay.endsWith(MINUTES)) {
                multiplier = MILLIS_IN_A_MINUTE;
                numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
            }

            try {
                wait = Long.parseLong(numericDelay);
            } catch (NumberFormatException nfe) {
                appLog.error(nfe.getMessage(), nfe);
                throw new SCXMLExpressionException(nfe.getMessage(), nfe);
            }
            wait *= multiplier;

        }

        return wait;

    }

    /** The suffix in the delay string for milliseconds. */
    private static final String MILLIS = "ms";

    /** The suffix in the delay string for seconds. */
    private static final String SECONDS = "s";

    /** The suffix in the delay string for minutes. */
    private static final String MINUTES = "m";

    /** The number of milliseconds in a second. */
    private static final long MILLIS_IN_A_SECOND = 1000L;

    /** The number of milliseconds in a minute. */
    private static final long MILLIS_IN_A_MINUTE = 60000L;

}

