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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.PathResolver;
import org.apache.commons.scxml2.SCXMLConstants;
import org.apache.commons.scxml2.SCXMLExecutionContext;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.EventBuilder;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.io.ContentParser;
import org.apache.commons.scxml2.semantics.ErrorConstants;
import org.w3c.dom.Element;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;invoke&gt; SCXML element.
 */
public class Invoke extends Action implements ContentContainer, ParamsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The default context variable key under which the current SCXMLExecutionContext is provided
     */
    private static final String CURRENT_EXECUTION_CONTEXT_KEY = "_CURRENT_EXECUTION_CONTEXT";

    /**
     * Identifier for this Invoke.
     * */
    private String id;

    /**
     * Path expression evaluating to a location within a previously defined XML data tree.
     */
    private String idlocation;

    /**
     * The type of target to be invoked.
     */
    private String type;

    /**
     * An expression defining the type of the target to be invoked.
     */
    private String typeexpr;

    /**
     * The source URL for the external service.
     */
    private String src;

    /**
     * The expression that evaluates to the source URL for the
     * external service.
     */
    private String srcexpr;

    /**
     * A flag indicating whether to forward events to the invoked process.
     */
    private Boolean autoForward;

    /**
     * The &lt;finalize&gt; child, may be null.
     */
    private Finalize finalize;

    /**
     * The &lt;content/&gt; of this invoke
     */
    private Content content;

    private EnterableState parent;

    /**
     * This invoke index in the parent (TransitionalState) defined invokers
     */
    private int invokeIndex;

    /**
     * The List of the params to be sent
     */
    private final List<Param> paramsList = new ArrayList<>();

    /**
     * The namelist.
     */
    private String namelist;

    /**
     * Gets the identifier for this invoke (may be null).
     *
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Sets the identifier for this invoke.
     *
     * @param id The id to set.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the idlocation
     */
    public String getIdlocation() {
        return idlocation;
    }

    /**
     * Sets the idlocation expression
     * @param idlocation The idlocation expression
     */
    public void setIdlocation(final String idlocation) {
        this.idlocation = idlocation;
    }

    /**
     * Gets the type for this &lt;invoke&gt; element.
     *
     * @return String Returns the type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Sets the type for this &lt;invoke&gt; element.
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
     * Gets the URL for the external service.
     *
     * @return String The URL.
     */
    public final String getSrc() {
        return src;
    }

    /**
     * Sets the URL for the external service.
     *
     * @param src The source URL.
     */
    public final void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Gets the expression that evaluates to the source URL for the
     * external service.
     *
     * @return String The source expression.
     */
    public final String getSrcexpr() {
        return srcexpr;
    }

    /**
     * Sets the expression that evaluates to the source URL for the
     * external service.
     *
     * @param srcexpr The source expression.
     */
    public final void setSrcexpr(final String srcexpr) {
        this.srcexpr = srcexpr;
    }


    /**
     * @return Returns true if all external events should be forwarded to the invoked process.
     */
    public final boolean isAutoForward() {
        return autoForward != null && autoForward;
    }

    /**
     * @return Returns the flag indicating whether to forward events to the invoked process.
     */
    public final Boolean getAutoForward() {
        return autoForward;
    }

    /**
     * Sets the flag indicating whether to forward events to the invoked process.
     * @param autoForward the flag
     */
    public final void setAutoForward(final Boolean autoForward) {
        this.autoForward = autoForward;
    }

    /**
     * Gets the Finalize for this Invoke.
     *
     * @return Finalize The Finalize for this Invoke.
     */
    public final Finalize getFinalize() {
        return finalize;
    }

    /**
     * Sets the Finalize for this Invoke.
     *
     * @param finalize The Finalize for this Invoke.
     */
    public final void setFinalize(final Finalize finalize) {
        this.finalize = finalize;
    }

    /**
     * Gets the list of {@link Param}s.
     *
     * @return List The params list.
     */
    @Override
    public List<Param> getParams() {
        return paramsList;
    }

    /**
     * Gets the namelist.
     *
     * @return String Returns the namelist.
     */
    public final String getNamelist() {
        return namelist;
    }

    /**
     * Sets the namelist.
     *
     * @param namelist The namelist to set.
     */
    public final void setNamelist(final String namelist) {
        this.namelist = namelist;
    }

    /**
     * Enforce identity equality only
     * @param other other object to compare with
     * @return this == other
     */
    @Override
    public final boolean equals(final Object other) {
        return this == other;
    }

    /**
     * Enforce returning identity based hascode
     * @return {@link System#identityHashCode(Object) System.identityHashCode(this)}
     */
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Returns the content
     *
     * @return the content
     */
    @Override
    public Content getContent() {
        return content;
    }

    /**
     * @return The local context variable name under which the current SCXMLExecutionContext is provided to the Invoke
     */
    public String getCurrentSCXMLExecutionContextKey() {
        return CURRENT_EXECUTION_CONTEXT_KEY;
    }

    /**
     * Sets the content
     *
     * @param content the content to set
     */
    @Override
    public void setContent(final Content content) {
        this.content = content;
    }

    /**
     * Gets the parent EnterableState.
     *
     * @return Returns the parent state
     */
    @Override
    public EnterableState getParentEnterableState() {
        return parent;
    }

    /**
     * Sets the parent EnterableState.
     * @param parent The parent state to set
     * @param invokeIndex the invoke index in the parent (TransitionalState) defined invokers
     */
    public void setParentEnterableState(final EnterableState parent, final int invokeIndex) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent parameter cannot be null");
        }
        this.parent = parent;
        this.invokeIndex = invokeIndex;
    }

    @Override
    public void execute(final ActionExecutionContext axctx) throws ModelException {
        final EnterableState parentState = getParentEnterableState();
        final Context ctx = axctx.getContext(parentState);
        final SCXMLExecutionContext exctx = (SCXMLExecutionContext)ctx.getVars().get(getCurrentSCXMLExecutionContextKey());
        if (exctx == null) {
            throw new ModelException("Missing current SCXMLExecutionContext instance in context under key: "+ getCurrentSCXMLExecutionContextKey());
        }
        try {
            final Evaluator eval = axctx.getEvaluator();

            String typeValue = type;
            if (typeValue == null && typeexpr != null) {
                typeValue = (String)eval.eval(ctx, typeexpr);
                if (typeValue == null) {
                    throw new SCXMLExpressionException("<invoke> for state "+parentState.getId() +
                            ": type expression \"" + typeexpr + "\" evaluated to null or empty String");
                }
            }
            if (typeValue == null) {
                typeValue = SCXMLExecutionContext.SCXML_INVOKER_TYPE;
            }
            final Invoker invoker = exctx.newInvoker(typeValue);

            String invokeId = getId();
            if (invokeId == null) {
                invokeId = parentState.getId() + "." + ctx.get(SCXMLSystemContext.SESSIONID_KEY) + "." + invokeIndex;
            }
            if (getId() == null && getIdlocation() != null) {
                eval.evalAssign(ctx, idlocation, invokeId);
            }
            invoker.setInvokeId(invokeId);

            String src = getSrc();
            if (src == null && getSrcexpr() != null) {
                src = (String)eval.eval(ctx, getSrcexpr());
            }
            if (src != null) {
                final PathResolver pr = exctx.getStateMachine().getPathResolver();
                if (pr != null) {
                    src = pr.resolvePath(src);
                }
            }
            Object contentValue = null;
            if (src == null && content != null) {
                if (content.getExpr() != null) {
                    try {
                        contentValue = eval.eval(ctx, content.getExpr());
                    } catch (final SCXMLExpressionException e) {
                        exctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION,
                                TriggerEvent.ERROR_EVENT).build());
                        exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR,
                                "Failed to evaluate <invoke> <content> expression due to error: "+ e.getMessage()
                                        + ", Using empty value instead.", getParent());
                        contentValue = "";
                    }
                } else if (content.getParsedValue() != null) {
                    contentValue = content.getParsedValue().getValue();
                }
                if (contentValue instanceof String) {
                    // inline content
                } else if (contentValue instanceof Element) {
                    // xml based content (must be assigned through data)
                    final Element contentElement = (Element)contentValue;
                    if (!SCXMLConstants.ELEM_SCXML.equals(contentElement.getLocalName()) || !SCXMLConstants.XMLNS_SCXML.equals(contentElement.getNamespaceURI())) {
                        throw new ActionExecutionError("<invoke> for state "+parentState.getId() +
                                ": invalid <content> definition");
                    }
                    // statemachine definition: transform to string as we cannot (yet) pass XML directly to invoker
                    try {
                        contentValue = ContentParser.DEFAULT_PARSER.toXml(contentElement);
                    }
                    catch (final IOException e) {
                        throw new ActionExecutionError("<invoke> for state "+parentState.getId() +
                                ": invalid <content><scxml> definition");
                    }
                } else {
                    throw new ActionExecutionError("<invoke> for state "+parentState.getId() +
                            ": invalid <content> definition");
                }
            }
            if (src == null && contentValue == null) {
                throw new ActionExecutionError("<invoke> for state "+parentState.getId() +
                        ": no src and no content defined");
            }
            final Map<String, Object> payloadDataMap = new HashMap<>();
            PayloadBuilder.addNamelistDataToPayload(parentState, ctx, eval, exctx.getErrorReporter(), namelist, payloadDataMap);
            PayloadBuilder.addParamsToPayload(ctx, eval, paramsList, payloadDataMap);
            invoker.setParentSCXMLExecutor(exctx.getSCXMLExecutor());
            if (src != null) {
                invoker.invoke(src, payloadDataMap);
            }
            else {
                invoker.invokeContent((String)contentValue, payloadDataMap);
            }
            exctx.registerInvoker(this, invoker);
        }
        catch (InvokerException|ActionExecutionError|SCXMLExpressionException e) {
            axctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
            if (e.getMessage() != null) {
                axctx.getErrorReporter().onError(e instanceof SCXMLExpressionException
                        ? ErrorConstants.EXPRESSION_ERROR : ErrorConstants.EXECUTION_ERROR, e.getMessage(), this);
            }
        }
    }
}