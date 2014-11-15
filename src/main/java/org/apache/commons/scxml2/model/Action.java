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

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.w3c.dom.Node;

/**
 * An abstract base class for executable elements in SCXML,
 * such as &lt;assign&gt;, &lt;log&gt; etc.
 *
 */
public abstract class Action implements NamespacePrefixesHolder,
        Serializable {

    /**
     * Link to its parent or container.
     */
    private Executable parent;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    /**
     * Constructor.
     */
    public Action() {
        super();
        this.parent = null;
        this.namespaces = null;
    }

    /**
     * Get the Executable parent.
     *
     * @return Returns the parent.
     */
    public final Executable getParent() {
        return parent;
    }

    /**
     * Set the Executable parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final Executable parent) {
        this.parent = parent;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Return the {@link EnterableState} whose {@link org.apache.commons.scxml2.Context} this action
     * executes in.
     *
     * @return The parent {@link EnterableState}
     * @throws ModelException For an unknown EnterableState subclass
     *
     * @since 0.9
     */
    public EnterableState getParentEnterableState()
    throws ModelException {
        if (parent == null && this instanceof Script && ((Script)this).isGlobalScript()) {
            // global script doesn't have a EnterableState
            return null;
        }
        else if (parent == null) {
            throw new ModelException("Action "
                    + this.getClass().getName() + " instance missing required parent TransitionTarget");
        }
        TransitionTarget tt = parent.getParent();
        if (tt instanceof EnterableState) {
            return (EnterableState)tt;
        } else if (tt instanceof History) {
            return ((History)tt).getParent();
        } else {
            throw new ModelException("Unknown TransitionTarget subclass:"
                    + (tt != null ? tt.getClass().getName() : "(null)"));
        }
    }

    /**
     * Execute this action instance.
     *
     * @param exctx The ActionExecutionContext for this execution instance
     *
     * @throws ModelException If the execution causes the model to enter
     *                        a non-deterministic state.
     * @throws SCXMLExpressionException If the execution involves trying
     *                        to evaluate an expression which is malformed.
     */
    public abstract void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException;

    /**
     * Return the key under which the current document namespaces are saved
     * in the parent state's context.
     *
     * @return The namespaces key
     */
    protected static String getNamespacesKey() {
        return Context.NAMESPACES_KEY;
    }

    /**
     * Convenient method to convert a possible {@link Node} result from an expression evaluation to a String
     * using its {@link Node#getTextContent()} method.
     * @param result the result to convert
     * @return its text content if the result is a {@link Node} otherwise the unmodified result itself
     */
    protected Object getTextContentIfNodeResult(final Object result) {
        if (result instanceof Node) {
            return ((Node)result).getTextContent();
        }
        return result;
    }
}

