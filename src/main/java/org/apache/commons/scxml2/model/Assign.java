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
     * Defines the nature of the insertion to be performed, default {@link Evaluator.AssignType#REPLACE_CHILDREN}
     */
    private Evaluator.AssignType type;

    /**
     * The attribute name to add at the specified location when using {@link Evaluator.AssignType#ADD_ATTRIBUTE}
     */
    private String attr;

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

    public Evaluator.AssignType getType() {
        return type;
    }

    public void setType(final Evaluator.AssignType type) {
        this.type = type;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(final String attr) {
        this.attr = attr;
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
        if (src != null && src.trim().length() > 0) {
            data = getSrcNode();
        } else {
            data = evaluator.eval(ctx, expr);
        }

        evaluator.evalAssign(ctx, location, data, type, attr);
        if (exctx.getAppLog().isDebugEnabled()) {
            exctx.getAppLog().debug("<assign>: '" + location + "' updated");
        }
        // TODO: introduce a optional 'trace.change' setting or something alike to enable .change events,
       //        but don't do this by default as it can interfere with transitions not expecting such events
        /*
        if ((Evaluator.XPATH_DATA_MODEL.equals(evaluator.getSupportedDatamodel()) && location.startsWith("$") && ctx.has(location.substring(1))
                || ctx.has(location))) {
            TriggerEvent ev = new TriggerEvent(location + ".change", TriggerEvent.CHANGE_EVENT);
            exctx.getInternalIOProcessor().addEvent(ev);
        }
        */
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
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resolvedSrc);
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
