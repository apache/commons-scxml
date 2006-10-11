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
package org.apache.commons.scxml.env.servlet;

import javax.servlet.ServletContext;

import org.apache.commons.scxml.PathResolver;

/**
 * A wrapper around ServletContext that implements PathResolver.
 *
 * @see org.apache.commons.scxml.PathResolver
 */
public class ServletContextResolver implements PathResolver {

    /** Cannot accept a null ServletContext, it will just throw
     *  NullPointerException down the road. */
    private static final String ERR_SERVLET_CTX_NULL =
        "ServletContextResolver cannot be instantiated with a null"
        + " ServletContext";

    /** The SevletContext we will use to resolve paths. */
    private ServletContext ctx = null;

    /**
     * Constructor.
     *
     * @param ctx The ServletContext instance for this web application.
     */
    public ServletContextResolver(final ServletContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException(ERR_SERVLET_CTX_NULL);
        }
        this.ctx = ctx;
    }

    /**
     * Delegates to the underlying ServletContext's getRealPath(String).
     *
     * @param ctxPath context sensitive path, can be a relative URL
     * @return resolved path (an absolute URL) or <code>null</code>
     * @see org.apache.commons.scxml.PathResolver#resolvePath(java.lang.String)
     */
    public String resolvePath(final String ctxPath) {
        return ctx.getRealPath(ctxPath);
    }

    /**
     * Retrieve the PathResolver rooted at the given path.
     *
     * @param ctxPath context sensitive path, can be a relative URL
     * @return returns a new resolver rooted at ctxPath
     * @see org.apache.commons.scxml.PathResolver#getResolver(java.lang.String)
     */
    public PathResolver getResolver(final String ctxPath) {
        return this;
    }

}

