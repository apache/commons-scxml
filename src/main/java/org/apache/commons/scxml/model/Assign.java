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
package org.apache.commons.scxml.model;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.TriggerEvent;
import org.w3c.dom.Node;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;assign&gt; SCXML element.
 *
 */
public final class Assign extends Action {

    /**
     * Left hand side expression evaluating to a previously
     * defined variable.
     */
    private String name;

    /**
     * Left hand side expression evaluating to a location within
     * a previously defined XML data tree.
     */
    private String location;

    /**
     * The source where the new XML instance for this location exists.
     */
    private String src;

    /**
     * Expression evaluating to the new value of the variable.
     */
    private String expr;

    /**
     * Constructor.
     */
    public Assign() {
        super();
    }

    /**
     * Get the variable to be assigned a new value.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the variable to be assigned a new value.
     *
     * @param name The name to set.
     */
    public void setName(final String name) {
        this.name = name;
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
     * {@inheritDoc}
     */
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection derivedEvents)
    throws ModelException, SCXMLExpressionException {
        State parentState = getParentState();
        Context ctx = scInstance.getContext(parentState);
        Evaluator eval = scInstance.getEvaluator();
        // "location" gets preference over "name"
        if (!SCXMLHelper.isStringEmpty(location)) {
            Node oldNode = eval.evalLocation(ctx, location);
            if (oldNode != null) {
                //// rvalue may be ...
                // a Node, if so, import it at location
                Node newNode = null;
                try {
                    newNode = eval.evalLocation(ctx, expr);
                    if (newNode != null) {
                        // adopt children, possible spec clarification needed
                        Node importedNode = oldNode.getOwnerDocument().
                            importNode(newNode, true);
                        for (Node child = importedNode.getFirstChild();
                            child != null; child = child.getNextSibling()) {
                                oldNode.appendChild(child);
                        }
                    }
                } catch (SCXMLExpressionException see) {
                    // or something else, stuff toString() into lvalue
                    Object valueObject = eval.eval(ctx, expr);
                    SCXMLHelper.setNodeValue(oldNode, valueObject.toString());
                }
                TriggerEvent ev = new TriggerEvent(name + ".change",
                    TriggerEvent.CHANGE_EVENT);
                derivedEvents.add(ev);
            } else {
                appLog.error("<assign>: location does not point to"
                    + " a <data> node");
            }
        } else {
            // lets try "name" (usage as in Sep '05 WD, useful with <var>)
            if (!ctx.has(name)) {
                errRep.onError(ErrorReporter.UNDEFINED_VARIABLE, name
                    + " = null", parentState);
            } else {
                Object varObj = eval.eval(ctx, expr);
                ctx.set(name, varObj);
                TriggerEvent ev = new TriggerEvent(name + ".change",
                    TriggerEvent.CHANGE_EVENT);
                derivedEvents.add(ev);
            }
        }
    }

}

