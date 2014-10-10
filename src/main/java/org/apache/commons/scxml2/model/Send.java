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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.semantics.ErrorConstants;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.CharacterData;

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
     * The default target type.
     */
    private static final String TYPE_SCXML = "scxml";

    /**
     * The spec mandated derived event when target cannot be reached
     * for TYPE_SCXML.
     */
    private static final String EVENT_ERR_SEND_TARGETUNAVAILABLE =
        "error.send.targetunavailable";

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

    /**
     * The ID of the send message.
     */
    private String id;

    /**
     * Path expression evaluating to a location within a previously defined XML data tree.
     */
    private String idlocation;

    /**
     * The target location of the event.
     */
    private String target;


    /**
     * An expression specifying the target location of the event.
     */
    private String targetexpr;

    /**
     * The type of the Event I/O Processor that the event should be dispatched to.
     */
    private String type;

    /**
     * An expression defining the type of the Event I/O Processor that the event should be dispatched to.
     */
    private String typeexpr;

    /**
     * The delay the event is dispatched after.
     */
    private String delay;

    /**
     * An expression defining the delay the event is dispatched after.
     */
    private String delayexpr;

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
     * An expression defining the type of event being generated.
     */
    private String eventexpr;

    /**
     * Constructor.
     */
    public Send() {
        super();
        this.externalNodes = new ArrayList<Node>();
    }

    /**
     * @return the idlocation
     */
    public String getIdlocation() {
        return idlocation;
    }

    /**
     * Set the idlocation expression
     * @param idlocation The idlocation expression
     */
    public void setIdlocation(final String idlocation) {
        this.idlocation = idlocation;
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
     * @return The delay expression
     */
    public String getDelayexpr() {
        return delayexpr;
    }

    /**
     * Set the delay expression
     * @param delayexpr The delay expression to set
     */
    public void setDelayexpr(final String delayexpr) {
        this.delayexpr = delayexpr;
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
     * @return String Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this &lt;send&gt; element.
     *
     * @param id The id to set.
     */
    public final void setId(final String id) {
        this.id = id;
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
     * @return The target expression
     */
    public String getTargetexpr() {
        return targetexpr;
    }

    /**
     * Set the target expression
     * @param targetexpr The target expression to set
     */
    public void setTargetexpr(final String targetexpr) {
        this.targetexpr = targetexpr;
    }

    /**
     * Get the type for this &lt;send&gt; element.
     *
     * @return String Returns the type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Set the type for this &lt;send&gt; element.
     *
     * @param type The type to set.
     */
    public final void setType(final String type) {
        this.type = type;
    }

    /**
     * @return The type expression
     */
    public String getTypeexpr() {
        return typeexpr;
    }

    /**
     * Sets the type expression
     * @param typeexpr The type expression to set
     */
    public void setTypeexpr(final String typeexpr) {
        this.typeexpr = typeexpr;
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
     * @return The event expression
     */
    public String getEventexpr() {
        return eventexpr;
    }

    /**
     * Sets the event expression
     * @param eventexpr The event expression to set
     */
    public void setEventexpr(final String eventexpr) {
        this.eventexpr = eventexpr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        // Send attributes evaluation
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Evaluator eval = exctx.getEvaluator();
        // Most attributes of <send> are expressions so need to be
        // evaluated before the EventDispatcher callback
        Object hintsValue = null;
        if (hints != null) {
            hintsValue = eval.eval(ctx, hints);
        }
        if (id == null) {
            id = ((SCXMLSystemContext)exctx.getGlobalContext().getParent()).generateSessionId();
            if (idlocation != null) {
                Node location = eval.evalLocation(ctx, idlocation);
                if (location != null) {
                    setNodeValue(location, id);
                }
                else {
                    throw new ModelException("<send>: idlocation does not point to a <data> node");
                }
            }
        }
        String targetValue = target;
        if (targetValue == null && targetexpr != null) {
            targetValue = (String) eval.eval(ctx, targetexpr);
            if ((targetValue == null || targetValue.trim().length() == 0)
                    && exctx.getAppLog().isWarnEnabled()) {
                exctx.getAppLog().warn("<send>: target expression \"" + targetexpr
                    + "\" evaluated to null or empty String");
            }
        }
        String typeValue = type;
        if (typeValue == null && typeexpr != null) {
            typeValue = (String) eval.eval(ctx, typeexpr);
            if ((typeValue == null || typeValue.trim().length() == 0)
                    && exctx.getAppLog().isWarnEnabled()) {
                exctx.getAppLog().warn("<send>: type expression \"" + typeexpr
                    + "\" evaluated to null or empty String");
            }
        }
        if (typeValue == null) {
            // must default to 'scxml' when unspecified
            typeValue = TYPE_SCXML;
        }
        else if (!TYPE_SCXML.equals(typeValue) && typeValue.trim().equalsIgnoreCase(TYPE_SCXML)) {
            typeValue = TYPE_SCXML;
        }
        Map<String, Object> params = null;
        if (namelist != null) {
            StringTokenizer tkn = new StringTokenizer(namelist);
            params = new HashMap<String, Object>(tkn.countTokens());
            while (tkn.hasMoreTokens()) {
                String varName = tkn.nextToken();
                Object varObj = ctx.get(varName);
                if (varObj == null) {
                    //considered as a warning here
                    exctx.getErrorReporter().onError(ErrorConstants.UNDEFINED_VARIABLE,
                            varName + " = null", parentState);
                }
                params.put(varName, varObj);
            }
        }
        long wait = 0L;
        String delayString = delay;
        if (delayString == null && delayexpr != null) {
            Object delayValue = eval.eval(ctx, delay);
            if (delayValue != null) {
                delayString = delayValue.toString();
            }
        }
        if (delayString != null) {
            wait = parseDelay(delayString, exctx.getAppLog());
        }
        String eventValue = event;
        if (eventValue == null && eventexpr != null) {
            eventValue = (String) eval.eval(ctx, eventexpr);
            if ((eventValue == null || eventValue.trim().length() == 0) && exctx.getAppLog().isWarnEnabled()) {
                throw new SCXMLExpressionException("<send>: event expression \"" + eventexpr
                        + "\" evaluated to null or empty String");
            }
        }
        // Lets see if we should handle it ourselves
        if (typeValue != null && TYPE_SCXML.equals(typeValue)) {
            if (eventValue == null) {
                // event required when type == http://www.w3.org/TR/scxml/#SCXMLEventProcessor
                throw new ModelException("Event parameter is required for <send> with type=\"scxml\"");
            }
            if (targetValue == null || targetValue.trim().length() == 0) {
                // TODO: Remove both short-circuit passes in v1.0
                if (wait == 0L) {
                    if (exctx.getAppLog().isDebugEnabled()) {
                        exctx.getAppLog().debug("<send>: Enqueued event '" + eventValue
                            + "' with no delay");
                    }
                    exctx.getInternalIOProcessor().addEvent(
                            new TriggerEvent(eventValue, TriggerEvent.SIGNAL_EVENT, params));
                    return;
                }
            } else {
                // We know of no other
                if (exctx.getAppLog().isWarnEnabled()) {
                    exctx.getAppLog().warn("<send>: Unavailable target - "
                        + targetValue);
                }
                exctx.getInternalIOProcessor().addEvent(
                        new TriggerEvent(EVENT_ERR_SEND_TARGETUNAVAILABLE, TriggerEvent.ERROR_EVENT));
                // short-circuit the EventDispatcher
                return;
            }
        }
        ctx.setLocal(getNamespacesKey(), null);
        if (exctx.getAppLog().isDebugEnabled()) {
            exctx.getAppLog().debug("<send>: Dispatching event '" + eventValue
                + "' to target '" + targetValue + "' of target type '"
                + typeValue + "' with suggested delay of " + wait
                + "ms");
        }
        // Else, let the EventDispatcher take care of it
        exctx.getEventDispatcher().send(id, targetValue, typeValue, eventValue,
            params, hintsValue, wait, externalNodes);
    }

    /**
     * Parse delay.
     *
     * @param delayString The String value of the delay, in CSS2 format
     * @param appLog The application log
     * @return The parsed delay in milliseconds
     * @throws SCXMLExpressionException If the delay cannot be parsed
     */
    private long parseDelay(final String delayString, final Log appLog)
    throws SCXMLExpressionException {

        long wait = 0L;
        long multiplier = 1L;

        if (delayString != null && delayString.trim().length() > 0) {

            String trimDelay = delayString.trim();
            String numericDelay = trimDelay;
            if (trimDelay.endsWith(MILLIS)) {
                numericDelay = trimDelay.substring(0, trimDelay.length() - 2);
            } else if (trimDelay.endsWith(SECONDS)) {
                multiplier = MILLIS_IN_A_SECOND;
                numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
            } else if (trimDelay.endsWith(MINUTES)) { // Not CSS2
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

    /**
     * Set node value, depending on its type, from a String.
     *
     * @param node A Node whose value is to be set
     * @param value The new value
     */
    private void setNodeValue(final Node node, final String value) {
        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                node.setNodeValue(value);
                break;
            case Node.ELEMENT_NODE:
                //remove all text children
                if (node.hasChildNodes()) {
                    Node child = node.getFirstChild();
                    while (child != null) {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            node.removeChild(child);
                        }
                        child = child.getNextSibling();
                    }
                }
                //create a new text node and append
                Text txt = node.getOwnerDocument().createTextNode(value);
                node.appendChild(txt);
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                ((CharacterData) node).setData(value);
                break;
            default:
                String err = "Trying to set value of a strange Node type: "
                        + node.getNodeType();
                throw new IllegalArgumentException(err);
        }
    }
}

