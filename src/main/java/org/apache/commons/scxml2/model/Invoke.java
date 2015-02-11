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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.PathResolver;
import org.apache.commons.scxml2.SCXMLExecutionContext;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.semantics.ErrorConstants;
import org.w3c.dom.Node;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;invoke&gt; SCXML element.
 *
 */
public class Invoke extends NamelistHolder implements PathResolverHolder, ContentContainer {

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
     * {@link PathResolver} for resolving the "src" or "srcexpr" result.
     */
    private PathResolver pathResolver;

    /**
     * The &lt;content/&gt; of this invoke
     */
    private Content content;

    private EnterableState parent;

    /**
     * Get the identifier for this invoke (may be null).
     *
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this invoke.
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
     * Set the idlocation expression
     * @param idlocation The idlocation expression
     */
    public void setIdlocation(final String idlocation) {
        this.idlocation = idlocation;
    }

    /**
     * Get the type for this &lt;invoke&gt; element.
     *
     * @return String Returns the type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Set the type for this &lt;invoke&gt; element.
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
     * Get the URL for the external service.
     *
     * @return String The URL.
     */
    public final String getSrc() {
        return src;
    }

    /**
     * Set the URL for the external service.
     *
     * @param src The source URL.
     */
    public final void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the expression that evaluates to the source URL for the
     * external service.
     *
     * @return String The source expression.
     */
    public final String getSrcexpr() {
        return srcexpr;
    }

    /**
     * Set the expression that evaluates to the source URL for the
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
     * Set the flag indicating whether to forward events to the invoked process.
     * @param autoForward the flag
     */
    public final void setAutoForward(final Boolean autoForward) {
        this.autoForward = autoForward;
    }

    /**
     * Get the Finalize for this Invoke.
     *
     * @return Finalize The Finalize for this Invoke.
     */
    public final Finalize getFinalize() {
        return finalize;
    }

    /**
     * Set the Finalize for this Invoke.
     *
     * @param finalize The Finalize for this Invoke.
     */
    public final void setFinalize(final Finalize finalize) {
        this.finalize = finalize;
    }

    /**
     * Get the {@link PathResolver}.
     *
     * @return Returns the pathResolver.
     */
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * Set the {@link PathResolver}.
     *
     * @param pathResolver The pathResolver to set.
     */
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
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
    public void setContent(final Content content) {
        this.content = content;
    }

    /**
     * Get the parent EnterableState.
     *
     * @return Returns the parent state
     */
    public EnterableState getParentEnterableState() {
        return parent;
    }

    /**
     * Set the parent EnterableState.
     * @param parent The parent state to set
     */
    public void setParentEnterableState(final EnterableState parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent parameter cannot be null");
        }
        this.parent = parent;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ActionExecutionContext axctx) throws ModelException {
        EnterableState parentState = getParentEnterableState();
        Context ctx = axctx.getContext(parentState);
        SCXMLExecutionContext exctx = (SCXMLExecutionContext)ctx.getVars().get(getCurrentSCXMLExecutionContextKey());
        if (exctx == null) {
            throw new ModelException("Missing current SCXMLExecutionContext instance in context under key: "+ getCurrentSCXMLExecutionContextKey());
        }
        try {
            ctx.setLocal(getNamespacesKey(), getNamespaces());
            Evaluator eval = axctx.getEvaluator();

            String typeValue = type;
            if (typeValue == null && typeexpr != null) {
                typeValue = (String) getTextContentIfNodeResult(eval.eval(ctx, typeexpr));
                if (typeValue == null) {
                    throw new SCXMLExpressionException("<invoke> for state "+parentState.getId() +
                            ": type expression \"" + typeexpr + "\" evaluated to null or empty String");
                }
            }
            if (typeValue == null) {
                typeValue = SCXMLExecutionContext.SCXML_INVOKER_TYPE;
            }
            Invoker invoker = exctx.newInvoker(typeValue);

            String invokeId = getId();
            if (invokeId == null) {
                invokeId = parentState.getId() + "." + ctx.get(SCXMLSystemContext.SESSIONID_KEY);
            }
            if (getId() == null && getIdlocation() != null) {
                eval.evalAssign(ctx, idlocation, invokeId, Evaluator.AssignType.REPLACE_CHILDREN, null);
            }
            invoker.setInvokeId(invokeId);

            String src = getSrc();
            if (src == null && getSrcexpr() != null) {
                src = (String) getTextContentIfNodeResult(eval.eval(ctx, getSrcexpr()));
            }
            if (src != null) {
                PathResolver pr = getPathResolver();
                if (pr != null) {
                    src = getPathResolver().resolvePath(src);
                }
            }
            Node srcNode = null;
            if (src == null && getContent() != null) {
                Object contentValue;
                if (content.getExpr() != null) {
                    contentValue = eval.eval(ctx, content.getExpr());
                } else {
                    contentValue = content.getBody();
                }
                if (contentValue instanceof Node) {
                    srcNode = ((Node)contentValue).cloneNode(true);
                }
                else if (contentValue != null) {
                    src = String.valueOf(contentValue);
                }
            }
            if (src == null && srcNode == null) {
                throw new SCXMLExpressionException("<invoke> for state "+parentState.getId() +
                        ": no src and no content defined");
            }
            Map<String, Object> payloadDataMap = new HashMap<String, Object>();
            addNamelistDataToPayload(axctx, payloadDataMap);
            addParamsToPayload(axctx, payloadDataMap);
            invoker.setParentSCXMLExecutor(exctx.getSCXMLExecutor());
            if (src != null) {
                invoker.invoke(src, payloadDataMap);
            }
            // TODO: } else { invoker.invoke(srcNode, payloadDataMap); }
            exctx.registerInvoker(this, invoker);
        }
        catch (InvokerException e) {
            axctx.getErrorReporter().onError(ErrorConstants.EXECUTION_ERROR, e.getMessage(), this);
            axctx.getInternalIOProcessor().addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
        }
        catch (SCXMLExpressionException e) {
            axctx.getInternalIOProcessor().addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
            axctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), this);
        }
        finally {
            ctx.setLocal(getNamespacesKey(), null);
        }
    }
}