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
package org.apache.commons.scxml.env.faces;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.env.SimpleContext;

/**
 * <p>A Faces Session Context.</p>
 *
 * <p>Since the &quot;session map&quot; is obtained from a
 * <code>FacesContext</code> object using the environment agnostic
 * <code>getExternalContext()</code>, this <code>Context</code>
 * will be useful in Servlet as well as Portlet environments.</p>
 *
 */
public class SessionContext extends SimpleContext {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** The map of session scoped variables. */
    private Map sessionMap;
    /** Bark if FacesContext is null. */
    private static final String ERR_HOST_FACES_CTX_NULL =
        "Host FacesContext cannot be null";

    /**
     * Constructor.
     *
     * @param fc The current FacesContext
     */
    public SessionContext(final FacesContext fc) {
        this(fc, null);
    }

    /**
     * Constructor.
     *
     * @param fc The current FacesContext
     * @param parent A parent Context, can be null
     */
    public SessionContext(final FacesContext fc, final Context parent) {
        super(parent);
        if (fc == null) {
            throw new IllegalArgumentException(ERR_HOST_FACES_CTX_NULL);
        } else {
          // only retain the session map
          this.sessionMap = fc.getExternalContext().getSessionMap();
        }

    }

    /**
     * Get the value of the given variable in this Context.
     *
     * @param name The name of the variable
     * @return The value (or null)
     * @see org.apache.commons.scxml.Context#get(java.lang.String)
     */
    public Object get(final String name) {
        Object value = getVars().get(name);
        if (value == null) {
            value = sessionMap.get(name);
        }
        return value;
    }

    /**
     * Does the given variable exist in this Context.
     *
     * @param name The name of the variable
     * @return boolean true if the variable exists
     * @see org.apache.commons.scxml.Context#has(java.lang.String)
     */
    public boolean has(final String name) {
        return (sessionMap.containsKey(name) || getVars().containsKey(name));
    }

}

