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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.scxml2.PathResolver;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;invoke&gt; SCXML element.
 *
 */
public class Invoke implements NamespacePrefixesHolder, PathResolverHolder,
        Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The type of target to be invoked.
     */
    private String type;

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
     * The List of the params to be sent to the invoked process.
     */
    private final List<Param> paramsList;

    /**
     * The &lt;finalize&gt; child, may be null.
     */
    private Finalize finalize;

    /**
     * {@link PathResolver} for resolving the "src" or "srcexpr" result.
     */
    private PathResolver pathResolver;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    /**
     * Default no-args constructor for Digester.
     */
    public Invoke() {
        paramsList = Collections.synchronizedList(new ArrayList<Param>());
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
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    public final List<Param> params() {
        return paramsList;
    }

    /**
     * Add this param to this invoke.
     *
     * @param param The invoke parameter.
     */
    public final void addParam(final Param param) {
        paramsList.add(param);
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

}

