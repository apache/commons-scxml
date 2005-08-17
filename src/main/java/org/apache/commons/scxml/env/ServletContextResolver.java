/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
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
package org.apache.taglibs.rdc.scxml.env;

import javax.servlet.ServletContext;

import org.apache.taglibs.rdc.scxml.PathResolver;

/**
 * A wrapper around ServletContext that implements PathResolver
 * 
 * @see org.apache.taglibs.rdc.scxml.PathResolver
 * @author Jaroslav Gergic
 */
public class ServletContextResolver implements PathResolver {
    
    ServletContext ctx = null;

    /**
     * @param ctx The ServletContext instance for this RDC runtime.
     */
    public ServletContextResolver(ServletContext ctx) {
        this.ctx = ctx;
    }
    
    /**
     * Delegates to the underlying ServletContext.getRealPath(String)
     * 
     * @param ctxPath context sensitive path, can be a relative URL
     * @return resolved path (an absolute URL) or <code>null</code>
     * @see org.apache.taglibs.rdc.scxml.PathResolver#resolvePath(java.lang.String)
     */
    public String resolvePath(String ctxPath) {
        return ctx.getRealPath(ctxPath);
    }

    /**
     * Retrieve the PathResolver rooted at the given path.
     * 
     * @param ctxPath context sensitive path, can be a relative URL
     * @return returns a new resolver rooted at ctxPath
     * @see org.apache.taglibs.rdc.scxml.PathResolver#getResolver(java.lang.String)
     */
    public PathResolver getResolver(String ctxPath) {
        return this;
    }

}
