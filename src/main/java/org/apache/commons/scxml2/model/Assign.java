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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.PathResolver;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.semantics.ErrorConstants;
import org.w3c.dom.*;
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
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        Evaluator eval = exctx.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        // "location" gets preference over "name"
        if (location != null) {
            Node oldNode = eval.evalLocation(ctx, location);
            if (oldNode != null) {
                //// rvalue may be ...
                // a Node, if so, import it at location
                Node newNode;
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
                    setNodeValue(oldNode, valueObject.toString());
                }
                if (exctx.getAppLog().isDebugEnabled()) {
                    exctx.getAppLog().debug("<assign>: data node '" + oldNode.getNodeName()
                        + "' updated");
                }
                /* TODO: send to notificationRegistry instead?
                TriggerEvent ev = new TriggerEvent(name + ".change",
                    TriggerEvent.CHANGE_EVENT);
                exctx.addInternalEvent(ev);
                */
            } else {
                exctx.getAppLog().error("<assign>: location does not point to"
                    + " a <data> node");
            }
        } else {
            // lets try "name" (usage as in Sep '05 WD, useful with <var>)
            if (!ctx.has(name)) {
                exctx.getErrorReporter().onError(ErrorConstants.UNDEFINED_VARIABLE, name
                    + " = null", parentState);
            } else {
                Object varObj;
                if (src != null && src.trim().length() > 0) {
                    varObj = getSrcNode();
                } else {
                    varObj = eval.eval(ctx, expr);
                }
                ctx.set(name, varObj);
                if (exctx.getAppLog().isDebugEnabled()) {
                    exctx.getAppLog().debug("<assign>: Set variable '" + name + "' to '"
                        + String.valueOf(varObj) + "'");
                }
                TriggerEvent ev = new TriggerEvent(name + ".change", TriggerEvent.CHANGE_EVENT);
                exctx.getInternalIOProcessor().addEvent(ev);
            }
        }
        ctx.setLocal(getNamespacesKey(), null);
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
     * @param throwable The throwable to log about
     */
    private void logError(Throwable throwable) {
        org.apache.commons.logging.Log log = LogFactory.
            getLog(Assign.class);
        log.error(throwable.getMessage(), throwable);
    }

}
