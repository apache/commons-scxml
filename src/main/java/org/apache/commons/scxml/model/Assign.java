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

import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.PathResolver;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.semantics.ErrorConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;assign&gt; SCXML element.
 *
 */
public class Assign extends Action implements PathResolverHolder {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

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
     * {@link PathResolver} for resolving the "src" result.
     */
    private PathResolver pathResolver;

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
     * {@inheritDoc}
     */
    @Override
    public void execute(final EventDispatcher evtDispatcher,
            final ErrorReporter errRep, final SCInstance scInstance,
            final Log appLog, final Collection<TriggerEvent> derivedEvents)
    throws ModelException, SCXMLExpressionException {
        TransitionTarget parentTarget = getParentTransitionTarget();
        Context ctx = scInstance.getContext(parentTarget);
        Evaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        // "location" gets preference over "name"
        if (!SCXMLHelper.isStringEmpty(location)) {
            Node oldNode = eval.evalLocation(ctx, location);
            if (oldNode != null) {
                //// rvalue may be ...
                // a Node, if so, import it at location
                Node newNode = null;
                try {
                    if (src != null && src.trim().length() > 0) {
                        newNode = getSrcNode();
                    } else {
                        newNode = eval.evalLocation(ctx, expr);
                    }
                    // Remove all children
                    Node removeChild = oldNode.getFirstChild();
                    while (removeChild != null) {
                        Node nextChild = removeChild.getNextSibling();
                        oldNode.removeChild(removeChild);
                        removeChild = nextChild;
                    }
                    if (newNode != null) {
                        // Adopt new children
                        for (Node child = newNode.getFirstChild();
                                child != null;
                                child = child.getNextSibling()) {
                            Node importedNode = oldNode.getOwnerDocument().
                                importNode(child, true);
                            oldNode.appendChild(importedNode);
                        }
                    }
                } catch (SCXMLExpressionException see) {
                    // or something else, stuff toString() into lvalue
                    Object valueObject = eval.eval(ctx, expr);
                    SCXMLHelper.setNodeValue(oldNode, valueObject.toString());
                }
                if (appLog.isDebugEnabled()) {
                    appLog.debug("<assign>: data node '" + oldNode.getNodeName()
                        + "' updated");
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
                errRep.onError(ErrorConstants.UNDEFINED_VARIABLE, name
                    + " = null", parentTarget);
            } else {
                Object varObj = null;
                if (src != null && src.trim().length() > 0) {
                    varObj = getSrcNode();
                } else {
                    varObj = eval.eval(ctx, expr);
                }
                ctx.set(name, varObj);
                if (appLog.isDebugEnabled()) {
                    appLog.debug("<assign>: Set variable '" + name + "' to '"
                        + String.valueOf(varObj) + "'");
                }
                TriggerEvent ev = new TriggerEvent(name + ".change",
                    TriggerEvent.CHANGE_EVENT);
                derivedEvents.add(ev);
            }
        }
        ctx.setLocal(getNamespacesKey(), null);
    }

    /**
     * Get the {@link Node} the "src" attribute points to.
     *
     * @return The node the "src" attribute points to.
     */
    private Node getSrcNode() {
        String resolvedSrc = src;
        if (pathResolver != null) {
            resolvedSrc = pathResolver.resolvePath(src);
        }
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                parse(resolvedSrc);
        } catch (FactoryConfigurationError t) {
            logError(t);
        } catch (SAXException e) {
            logError(e);
        } catch (IOException e) {
            logError(e);
        } catch (ParserConfigurationException e) {
            logError(e);
        }
        if (doc == null) {
            return null;
        }
        return doc.getDocumentElement();
    }

    /**
     * @param throwable
     */
    private void logError(Throwable throwable) {
        org.apache.commons.logging.Log log = LogFactory.
            getLog(Assign.class);
        log.error(throwable.getMessage(), throwable);
    }

}
