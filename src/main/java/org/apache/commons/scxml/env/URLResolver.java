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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.taglibs.rdc.scxml.PathResolver;

/**
 * A PathResolver implementation that resolves against a base URL.
 *  
 * @see org.apache.taglibs.rdc.scxml.PathResolver
 * @author Jaroslav Gergic
 */
public class URLResolver implements PathResolver {
    
    URL baseURL = null;
    
    /**
     * @param baseURL
     */
    public URLResolver(URL baseURL) {
        this.baseURL = baseURL;
    }
    
    /**
     * Uses URL(URL, String) constructor to combine URL's
     * @see org.apache.taglibs.rdc.scxml.PathResolver#resolvePath(java.lang.String)
     */
    public String resolvePath(String ctxPath) {
        URL combined;
        try {
            combined = new URL(baseURL, ctxPath);
            return combined.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @see org.apache.taglibs.rdc.scxml.PathResolver#getResolver(java.lang.String)
     */
    public PathResolver getResolver(String ctxPath) {
        URL combined;
        try {
            combined = new URL(baseURL, ctxPath);
            return new URLResolver(combined);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
