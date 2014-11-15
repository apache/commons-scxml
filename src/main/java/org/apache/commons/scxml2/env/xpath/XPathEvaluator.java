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
package org.apache.commons.scxml2.env.xpath;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.PackageFunctions;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EvaluatorProvider;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.env.EffectiveContextMap;
import org.apache.commons.scxml2.model.SCXML;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>An {@link Evaluator} implementation for XPath environments.</p>
 *
 * <p>Does not support the &lt;script&gt; module, throws
 * {@link UnsupportedOperationException} if attempted.</p>
 */
public class XPathEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -3578920670869493294L;

    public static final String SUPPORTED_DATA_MODEL = Evaluator.XPATH_DATA_MODEL;

    /**
     * Internal 'marker' list used for collecting the NodePointer results of an {@link #evalLocation(Context, String)}
     */
    private static class NodePointerList extends ArrayList<NodePointer> {
    }

    public static class XPathEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATA_MODEL;
        }

        @Override
        public Evaluator getEvaluator() {
            return new XPathEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new XPathEvaluator();
        }
    }

    private static final JXPathContext jxpathRootContext = JXPathContext.newContext(null);

    static {
        FunctionLibrary xpathFunctions = new FunctionLibrary();
        xpathFunctions.addFunctions(new ClassFunctions(XPathFunctions.class, null));
        // also restore default generic JXPath functions
        xpathFunctions.addFunctions(new PackageFunctions("", null));
        jxpathRootContext.setFunctions(xpathFunctions);
    }

    private JXPathContext jxpathContext;

    /**
     * No argument constructor.
     */
    public XPathEvaluator() {
        jxpathContext = jxpathRootContext;
    }

    /**
     * Constructor supporting user-defined JXPath {@link Functions}.
     *
     * @param functions The user-defined JXPath functions to use.
     */
    public XPathEvaluator(final Functions functions) {
        jxpathContext = JXPathContext.newContext(jxpathRootContext, null);
        jxpathContext.setFunctions(functions);
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    /**
     * @see Evaluator#eval(Context, String)
     */
    @Override
    public Object eval(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        try {
            List list = getContext(ctx).selectNodes(expr);
            if (list.isEmpty()) {
                return null;
            }
            else if (list.size() == 1) {
                return list.get(0);
            }
            return list;
        } catch (JXPathException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalCond(Context, String)
     */
    @Override
    public Boolean evalCond(final Context ctx, final String expr)
            throws SCXMLExpressionException {
        try {
            return (Boolean)getContext(ctx).getValue(expr, Boolean.class);
        } catch (JXPathException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalLocation(Context, String)
     */
    @Override
    public Object evalLocation(final Context ctx, final String expr) throws SCXMLExpressionException {
        JXPathContext context = getContext(ctx);
        try {
            Iterator iterator = context.iteratePointers(expr);
            Object pointer;
            NodePointerList pointerList = null;
            while (iterator.hasNext()) {
                pointer = iterator.next();
                if (pointer != null && pointer instanceof NodePointer && ((NodePointer)pointer).getNode() != null) {
                    if (pointerList == null) {
                        pointerList = new NodePointerList();
                    }
                    pointerList.add((NodePointer)pointer);
                }
            }
            return pointerList;
        } catch (JXPathException xee) {
            throw new SCXMLExpressionException(xee.getMessage(), xee);
        }
    }

    /**
     * @see Evaluator#evalAssign(Context, String, Object, AssignType, String)
     */
    public void evalAssign(final Context ctx, final String location, final Object data, final AssignType type,
                           final String attr) throws SCXMLExpressionException {

        Object loc = evalLocation(ctx, location);
        if (isXPathLocation(ctx, loc)) {
            assign(ctx, loc, data, type, attr);
        }
        else {
            throw new SCXMLExpressionException("evalAssign - cannot resolve location: '" + location + "'");
        }
    }

    /**
     * @see Evaluator#evalScript(Context, String)
     */
    public Object evalScript(Context ctx, String script)
    throws SCXMLExpressionException {
        throw new UnsupportedOperationException("Scripts are not supported by the XPathEvaluator");
    }

    /**
     * @see Evaluator#newContext(Context)
     */
    @Override
    public Context newContext(final Context parent) {
        return new XPathContext(parent);
    }

    /**
     * Determine if an {@link Evaluator#evalLocation(Context, String)} returned result represents an XPath location
     * @param ctx variable context
     * @param data result data from {@link Evaluator#evalLocation(Context, String)}
     * @return true if the data represents an XPath location
     */
    @SuppressWarnings("unused")
    public boolean isXPathLocation(final Context ctx, Object data) {
        return data instanceof NodePointerList;
    }

    /**
     * Assigns data to a location
     *
     * @param ctx variable context
     * @param location location expression
     * @param data the data to assign.
     * @param type the type of assignment to perform, null assumes {@link Evaluator.AssignType#REPLACE_CHILDREN}
     * @param attr the name of the attribute to add when using type {@link Evaluator.AssignType#ADD_ATTRIBUTE}
     * @throws SCXMLExpressionException A malformed expression exception
     * @see Evaluator#evalAssign(Context, String, Object, Evaluator.AssignType, String)
     */
    public void assign(final Context ctx, final Object location, final Object data, final AssignType type,
                       final String attr) throws SCXMLExpressionException {
        if (!isXPathLocation(ctx, location)) {
            throw new SCXMLExpressionException("assign requires a NodePointerList as location but is of type: " +
                    (location==null ? "(null)" : location.getClass().getName()));
        }
        for (NodePointer pointer : (NodePointerList)location) {
            Object node = pointer.getNode();
            if (node != null) {
                if (node instanceof Node) {
                    assign(ctx, (Node)node, pointer.asPath(), data, type != null ? type : AssignType.REPLACE_CHILDREN, attr);
                }
                else if (pointer instanceof VariablePointer) {
                    if (type == AssignType.DELETE) {
                        pointer.remove();
                    }
                    VariablePointer vp = (VariablePointer)pointer;
                    Object variable = vp.getNode();
                    if (variable instanceof Node) {
                        assign(ctx, (Node)variable, pointer.asPath(), data, type != null ? type : AssignType.REPLACE_CHILDREN, attr);
                    }
                    else if (type == null || type == AssignType.REPLACE) {
                        String variableName = vp.getName().getName();
                        if (data instanceof CharacterData) {
                            ctx.set(variableName, ((CharacterData)data).getNodeValue());
                        }
                        else {
                            ctx.set(variableName, data);
                        }
                    }
                    else {
                        throw new SCXMLExpressionException("Unsupported assign type +" +
                                type.name()+" for XPath variable "+pointer.asPath());
                    }
                }
                else {
                    throw new SCXMLExpressionException("Unsupported XPath location pointer " +
                            pointer.getClass().getName()+" for location "+pointer.asPath());
                }
            }
            // else: silent ignore - NodePointerList should not have pointers without node
        }
    }

    @SuppressWarnings("unused")
    protected void assign(final Context ctx, final Node node, final String nodePath, final Object data,
                          final AssignType type, final String attr) throws SCXMLExpressionException {

        if (type == AssignType.DELETE) {
            node.getParentNode().removeChild(node);
        }
        else if (node instanceof Element) {
            Element element = (Element)node;
            if (type == AssignType.ADD_ATTRIBUTE) {
                if (attr == null) {
                    throw new SCXMLExpressionException("Missing required attribute name for adding attribute at " +
                            nodePath);
                }
                if (data == null) {
                    throw new SCXMLExpressionException("Missing required data value for adding attribute " +
                            attr + " to location " + nodePath);
                }
                element.setAttribute(attr, data.toString());
            }
            else {
                Node dataNode = null;
                if (type != AssignType.REPLACE_CHILDREN) {
                    if (data == null) {
                        throw new SCXMLExpressionException("Missing required data value for assign type "+type.name());
                    }
                    dataNode = data instanceof Node
                            ? element.getOwnerDocument().importNode((Node)data, true)
                            : element.getOwnerDocument().createTextNode(data.toString());
                }
                switch (type) {
                    case REPLACE_CHILDREN:
                        // quick way to delete all children
                        element.setTextContent(null);
                        if (data instanceof Node) {
                            element.appendChild(element.getOwnerDocument().importNode((Node)data, true));
                        }
                        else if (data instanceof List) {
                            for (Object dataElement : (List)data) {
                                if (dataElement instanceof Node) {
                                    element.appendChild(element.getOwnerDocument().importNode((Node)dataElement, true));
                                }
                                else if (dataElement != null) {
                                    element.appendChild(element.getOwnerDocument().createTextNode(dataElement.toString()));
                                }
                            }
                        }
                        else if (data instanceof NodeList) {
                            NodeList list = (NodeList)data;
                            for (int i = 0, size = list.getLength(); i < size; i++)
                            element.appendChild(element.getOwnerDocument().importNode(list.item(i), true));
                        }
                        else {
                            element.appendChild(element.getOwnerDocument().createTextNode(data.toString()));
                        }
                        // else if data == null: already taken care of above
                        break;
                    case FIRST_CHILD:
                        element.insertBefore(dataNode, element.getFirstChild());
                        break;
                    case LAST_CHILD:
                        element.appendChild(dataNode);
                        break;
                    case PREVIOUS_SIBLING:
                        element.getParentNode().insertBefore(dataNode, element);
                        break;
                    case NEXT_SIBLING:
                        element.getParentNode().insertBefore(dataNode, element.getNextSibling());
                        break;
                    case REPLACE:
                        element.getParentNode().replaceChild(dataNode, element);
                        break;
                }
            }
        }
        else if (node instanceof CharacterData) {
            if (type != AssignType.REPLACE) {
                throw new SCXMLExpressionException("Assign type "+ type.name() +
                        " not supported for character data node at " + nodePath);
            }
            ((CharacterData)node).setData(data.toString());
        }
        else if (node instanceof Attr) {
            if (type != AssignType.REPLACE) {
                throw new SCXMLExpressionException("Assign type "+ type.name() +
                        " not supported for node attribute at " + nodePath);
            }
            ((Attr)node).setValue(data.toString());
        }
        else {
            throw new SCXMLExpressionException("Unsupported assign location Node type "+node.getNodeType());
        }
    }


    @SuppressWarnings("unchecked")
    protected JXPathContext getContext(final Context ctx) throws SCXMLExpressionException {
        JXPathContext context = JXPathContext.newContext(jxpathContext, new EffectiveContextMap(ctx));
        context.setVariables(new ContextVariables(ctx));
        Map<String, String> namespaces = (Map<String, String>) ctx.get(Context.NAMESPACES_KEY);
        if (namespaces != null) {
            for (String prefix : namespaces.keySet()) {
                context.registerNamespace(prefix, namespaces.get(prefix));
            }
        }
        return context;
    }
}
