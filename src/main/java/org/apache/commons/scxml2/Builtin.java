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
package org.apache.commons.scxml2;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.w3c.dom.*;

/**
 * Implementations of builtin functions defined by the SCXML
 * specification.
 *
 * The current version of the specification defines one builtin
 * predicate In()
 */
public class Builtin implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Implements the In() predicate for SCXML documents. The method
     * name chosen is different since &quot;in&quot; is a reserved token
     * in some expression languages.
     *
     * Does this state belong to the given Set of States.
     * Simple ID based comparator, assumes IDs are unique.
     *
     * @param allStates The Set of State objects to look in
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public static boolean isMember(final Set<? extends TransitionTarget> allStates,
            final String state) {
        for (TransitionTarget tt : allStates) {
            if (state.equals(tt.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implements the Data() function for Commons SCXML documents, that
     * can be used to obtain a node from one of the XML data trees.
     * Manifests within "location" attribute of &lt;assign&gt; element,
     * for Commons JEXL and Commons EL based documents.
     *
     * @param namespaces The current document namespaces map at XPath location
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, or null if no nodes match.
     */
    public static Node dataNode(final Map<String, String> namespaces, final Object data,
            final String path) {
        if (data == null || !(data instanceof Node)) {
            Log log = LogFactory.getLog(Builtin.class);
            log.error("Data(): Cannot evaluate an XPath expression"
                + " in the absence of a context Node, null returned");
            return null;
        }
        Node dataNode = (Node) data;
        NodeList result = null;
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            if (namespaces == null || namespaces.size() == 0) {
                Log log = LogFactory.getLog(Builtin.class);
                if (log.isDebugEnabled()) {
                    log.debug("Turning off namespaced XPath evaluation since "
                        + "no namespace information is available for path: "
                        + path);
                }
            } else {
                xpath.setNamespaceContext(new ExpressionNSContext(namespaces));
            }
            result = (NodeList) xpath.evaluate(path, dataNode,
                XPathConstants.NODESET);
        } catch (XPathExpressionException xee) {
            Log log = LogFactory.getLog(Builtin.class);
            log.error(xee.getMessage(), xee);
            return null;
        }
        int length = result.getLength();
        if (length == 0) {
            Log log = LogFactory.getLog(Builtin.class);
            log.warn("Data(): No nodes matching the XPath expression \""
                + path + "\", returning null");
            return null;
        } else {
            if (length > 1) {
                Log log = LogFactory.getLog(Builtin.class);
                log.warn("Data(): Multiple (" + length + ") nodes matching XPath expression \""
                    + path + "\", returning first");
            }
            return result.item(0);
        }
    }

    /**
     * A variant of the Data() function for Commons SCXML documents,
     * coerced to a Double, a Long or a String, whichever succeeds,
     * in that order.
     * Manifests within rvalue expressions in the document,
     * for Commons JEXL and Commons EL based documents..
     *
     * @param namespaces The current document namespaces map at XPath location
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, coerced to a String, or null
     *         if no nodes match.
     */
    public static Object data(final Map<String, String> namespaces, final Object data, final String path) {
        Object retVal = null;
        String strVal = getNodeValue(dataNode(namespaces, data, path));
        // try as a double
        try {
            double d = Double.parseDouble(strVal);
            retVal = new Double(d);
        } catch (NumberFormatException notADouble) {
            // else as a long
            try {
                long l = Long.parseLong(strVal);
                retVal = new Long(l);
            } catch (NumberFormatException notALong) {
                // fallback to string
                retVal = strVal;
            }
        }
        return retVal;
    }

    /**
     * Retrieve a DOM node value as a string depending on its type.
     *
     * @param node A node to be retreived
     * @return The value as a string
     */
    private static String getNodeValue(final Node node) {
        String result = "";
        if (node == null) {
            return result;
        }
        switch(node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                result = node.getNodeValue();
                break;
            case Node.ELEMENT_NODE:
                if (node.hasChildNodes()) {
                    Node child = node.getFirstChild();
                    StringBuilder buf = new StringBuilder();
                    while (child != null) {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            buf.append(((CharacterData) child).getData());
                        }
                        child = child.getNextSibling();
                    }
                    result = buf.toString();
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                result = ((CharacterData) node).getData();
                break;
            default:
                String err = "Trying to get value of a strange Node type: " + node.getNodeType();
                throw new IllegalArgumentException(err);
        }
        return result.trim();
    }

    /**
     * XPath {@link NamespaceContext} for Commons SCXML expressions.
     *
     * <b>Code duplication:</b> Also in XPathEvaluator.java. Class is not
     * meant to be part of any public API and will be removed when parser
     * is no longer using Commons Digester.
     */
    private static final class ExpressionNSContext
    implements Serializable, NamespaceContext {

        /** Serial version UID. */
        private static final long serialVersionUID = 8620558582288851315L;
        private final Map<String, String> namespaces;

        /**
         * Constructor.
         *
         * @param namespaces The current namespace map.
         */
        ExpressionNSContext(final Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        /**
         * @see NamespaceContext#getNamespaceURI(String)
         */
        @Override
        public String getNamespaceURI(final String prefix) {
            return namespaces.get(prefix);
        }

        /**
         * @see NamespaceContext#getPrefix(String)
         *
         * First matching key in iteration order is returned, and the
         * iteration order depends on the underlying <code>namespaces</code>
         * {@link Map} implementation.
         */
        @Override
        public String getPrefix(final String namespaceURI) {
            return (String) getKeys(namespaceURI, true);
        }

        /**
         * @see NamespaceContext#getPrefixes(String)
         *
         * The iteration order depends on the underlying <code>namespaces</code>
         * {@link Map} implementation.
         */
        @Override
        @SuppressWarnings("unchecked")
        public Iterator<String> getPrefixes(final String namespaceURI) {
            return (Iterator<String>) getKeys(namespaceURI, false);
        }

        /**
         * Get prefix key(s) for given namespaceURI value.
         *
         * If <code>one</code>, first matching key in iteration order is
         * returned, and the iteration order depends on the underlying
         * <code>namespaces</code> {@link Map} implementation.
         * Otherwise, an iterator to all matching keys is returned.
         *
         * @param value The value whose key is required
         * @param one At most one matching key is returned
         * @return The required prefix key(s)
         */
        private Object getKeys(final String value, final boolean one) {
            List<String> prefixes = new LinkedList<String>();
            if (namespaces.containsValue(value)) {
                for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                    String v = entry.getValue();
                    if ((value == null && v == null) ||
                            (value != null && value.equals(v))) {
                        String prefix = entry.getKey();
                        if (one) {
                            return prefix;
                        } else {
                            prefixes.add(prefix);
                        }
                    }
                }
            }
            return one ? null : prefixes.iterator();
        }

    }

}

