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
package org.apache.commons.scxml2.env;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.PathResolver;

/**
 * A PathResolver implementation that resolves against a base URL.
 *
 * @see org.apache.commons.scxml2.PathResolver
 */
public class URLResolver implements PathResolver, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Implementation independent log category. */
    private Log log = LogFactory.getLog(PathResolver.class);

    /** The base URL to resolve against. */
    private URL baseURL = null;

    /**
     * Constructor.
     *
     * @param baseURL The base URL to resolve against
     */
    public URLResolver(final URL baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * Uses URL(URL, String) constructor to combine URL's.
     * @see org.apache.commons.scxml2.PathResolver#resolvePath(java.lang.String)
     */
    public String resolvePath(final String ctxPath) {
        URL combined;
        try {
            combined = new URL(baseURL, ctxPath);
            return combined.toString();
        } catch (MalformedURLException e) {
            log.error("Malformed URL", e);
        }
        return null;
    }

    /**
     * @see org.apache.commons.scxml2.PathResolver#getResolver(java.lang.String)
     */
    public PathResolver getResolver(final String ctxPath) {
        URL combined;
        try {
            combined = new URL(baseURL, ctxPath);
            return new URLResolver(combined);
        } catch (MalformedURLException e) {
            log.error("Malformed URL", e);
        }
        return null;
    }

}

