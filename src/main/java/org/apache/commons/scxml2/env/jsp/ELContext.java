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
package org.apache.commons.scxml2.env.jsp;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

/**
 * EL Context for SCXML interpreter.
 *
 */
public class ELContext extends SimpleContext implements VariableResolver {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     */
    public ELContext() {
        super();
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     */
    public ELContext(final Context parent) {
        super(parent);
    }

    /**
     * Resolves the specified variable. Returns null if the variable is
     * not found.
     *
     * @param pName The variable to resolve
     * @return Object The value of the variable, or null, if it does not
     *                exist
     * @throws ELException While resolving the variable
     * @see javax.servlet.jsp.el.VariableResolver#resolveVariable(String)
     */
    public Object resolveVariable(final String pName) throws ELException {
        return get(pName);
    }

}

