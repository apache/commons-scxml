/*
 *
 *   Copyright 2006 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.scxml.PathResolver;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;invoke&gt; SCXML element.
 *
 */
public class Invoke {

    /**
     * The type of target to be invoked.
     */
    private String targettype;

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
     * The Map of the params to be sent to the invoked process.
     */
    private Map params;

    /**
     * The &lt;finalize&gt; child, may be null.
     */
    private Finalize finalize;

    /**
     * {@link PathResolver} for resolving the "src" or "srcexpr" result.
     */
    private PathResolver pathResolver;

    /**
     * Default no-args constructor for Digester.
     */
    public Invoke() {
        params = new HashMap();
    }

    /**
     * Get the target type for this &lt;invoke&gt; element.
     *
     * @return String Returns the targettype.
     */
    public final String getTargettype() {
        return targettype;
    }

    /**
     * Set the target type for this &lt;invoke&gt; element.
     *
     * @param targettype The targettype to set.
     */
    public final void setTargettype(final String targettype) {
        this.targettype = targettype;
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
     * Get the params Map.
     *
     * @return Map The params map.
     */
    public final Map getParams() {
        return params;
    }

    /**
     * Add this param to this invoke.
     *
     * @param param The invoke parameter.
     */
    public final void addParam(final Param param) {
        params.put(param.getName(), param.getExpr());
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

}

