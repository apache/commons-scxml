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
package org.apache.commons.scxml;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.XPathContext;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public static boolean isMember(final Set<TransitionTarget> allStates,
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
            if (namespaces == null || namespaces.size() == 0) {
                Log log = LogFactory.getLog(Builtin.class);
                if (log.isDebugEnabled()) {
                    log.debug("Turning off namespaced XPath evaluation since "
                        + "no namespace information is available for path: "
                        + path);
                }
                result = XPathAPI.selectNodeList(dataNode, path);
            } else {
                XPathContext xpathSupport = new XPathContext();
                PrefixResolver prefixResolver =
                    new DataPrefixResolver(namespaces);
                XPath xpath = new XPath(path, null, prefixResolver,
                    XPath.SELECT);
                int ctxtNode = xpathSupport.getDTMHandleFromNode(dataNode);
                result = xpath.execute(xpathSupport, ctxtNode,
                    prefixResolver).nodelist();
            }
        } catch (TransformerException te) {
            Log log = LogFactory.getLog(Builtin.class);
            log.error(te.getMessage(), te);
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
                log.warn("Data(): Multiple nodes matching XPath expression"
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
    public static Object data(final Map<String, String> namespaces, final Object data,
            final String path) {
        Object retVal = null;
        String strVal = SCXMLHelper.getNodeValue(dataNode(namespaces,
            data, path));
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
     * Implements the Data() function for Commons SCXML documents, that
     * can be used to obtain a node from one of the XML data trees.
     * Manifests within "location" attribute of &lt;assign&gt; element,
     * for Commons JEXL and Commons EL based documents.
     *
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, or null if no nodes match.
     *
     * @deprecated Use {@link #dataNode(Map,Object,String)} instead
     */
    public static Node dataNode(final Object data, final String path) {
        if (data == null || !(data instanceof Node)) {
            Log log = LogFactory.getLog(Builtin.class);
            log.error("Data(): Cannot evaluate an XPath expression"
                + " in the absence of a context Node, null returned");
            return null;
        }
        Node dataNode = (Node) data;
        NodeList result = null;
        try {
            result = XPathAPI.selectNodeList(dataNode, path);
        } catch (TransformerException te) {
            Log log = LogFactory.getLog(Builtin.class);
            log.error(te.getMessage(), te);
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
                log.warn("Data(): Multiple nodes matching XPath expression"
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
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, coerced to a String, or null
     *         if no nodes match.
     *
     * @deprecated Use {@link #data(Map,Object,String)} instead
     */
    public static Object data(final Object data, final String path) {
        Object retVal = null;
        String strVal = SCXMLHelper.getNodeValue(dataNode(data, path));
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
     * Prefix resolver for XPaths pointing to &lt;data&gt; nodes.
     */
    private static class DataPrefixResolver implements PrefixResolver {

        /** Cached namespaces. */
        private Map<String, String> namespaces;

        /**
         * Constructor.
         * @param namespaces The prefix to namespace URI map.
         */
        private DataPrefixResolver(final Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }

        /** {@inheritDoc} */
        public String getNamespaceForPrefix(final String prefix) {
            return namespaces.get(prefix);
        }

        /** {@inheritDoc} */
        public String getNamespaceForPrefix(final String prefix,
                final Node nsContext) {
            return namespaces.get(prefix);
        }

        /** {@inheritDoc} */
        public String getBaseIdentifier() {
            return null;
        }

        /** {@inheritDoc} */
        public boolean handlesNullPrefixes() {
            return false;
        }

    }

}

