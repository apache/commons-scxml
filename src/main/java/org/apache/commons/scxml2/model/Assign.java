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

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.w3c.dom.Node;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;assign&gt; SCXML element.
 *
 */
public class Assign extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Left hand side expression evaluating to a location within
     * a previously defined XML data tree.
     */
    private String location;

    /**
     * The source location where the new source data for this location exists.
     */
    private String src;

    /**
     * Expression evaluating to the new value of the variable.
     */
    private String expr;

    /**
     * The assign definition parsed as a standalone DocumentFragment Node (only used by the SCXMLWriter)
     */
    private Node node;

    /**
     * The parsed value for the child XML data tree or the content of the external src
     */
    private Object value;

    /**
     * Constructor.
     */
    public Assign() {
        super();
    }

    /**
     * Get the expr that will evaluate to the new value.
     *
     * @return Returns the expr.
     */
    public String getExpr() {
        return expr;
    }

    /**
     * Set the expr that will evaluate to the new value.
     *
     * @param expr The expr to set.
     */
    public void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the location for a previously defined XML data tree.
     *
     * @return Returns the location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Set the location for a previously defined XML data tree.
     *
     * @param location The location.
     */
    public void setLocation(final String location) {
        this.location = location;
    }

    /**
     * Get the source where the new XML instance for this location exists.
     *
     * @return Returns the source.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Set the source where the new XML instance for this location exists.
     *
     * @param src The source.
     */
    public void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the assign definition parsed as standalone DocumentFragment Node.
     *
     * @return Node The assign definition parsed as a standalone DocumentFragment <code>Node</code>.
     */
    public final Node getNode() {
        return node;
    }

    /**
     * Set the assign definition parsed as standalone DocumentFragment Node.
     *
     * @param node The child XML data tree, parsed as a standalone DocumentFragment <code>Node</code>.
     */
    public final void setNode(final Node node) {
        this.node = node;
    }

    /**
     * Get the parsed value for the child XML data tree or the content of the external src
     * @see #setValue(Object)
     * @return The parsed value
     */
    public final Object getValue() {
        return value;
    }

    /**
     * Sets the parsed value for the child XML data tree or the content of the external src
     * @param value a serializable object:
     * <ul>
     *   <li>"Raw" JSON mapped object tree (array->ArrayList, object->LinkedHashMap based)</li>
     *   <li>XML Node (equals {@link #getNode()})</li>
     *   <li>space-normalized String</li>
     * </ul>
     */
    public final void setValue(final Object value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        Evaluator evaluator = exctx.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Object data;
        if (expr != null) {
            data = evaluator.eval(ctx, expr);
        }
        else {
            data = evaluator.cloneData(value);
        }

        evaluator.evalAssign(ctx, location, data);
        if (exctx.getAppLog().isDebugEnabled()) {
            exctx.getAppLog().debug("<assign>: '" + location + "' updated");
        }
        // TODO: introduce a optional 'trace.change' setting or something alike to enable .change events,
       //        but don't do this by default as it can interfere with transitions not expecting such events
        /*
            TriggerEvent ev = new TriggerEvent(location + ".change", TriggerEvent.CHANGE_EVENT);
            exctx.getInternalIOProcessor().addEvent(ev);
        */
        ctx.setLocal(getNamespacesKey(), null);
    }
}
