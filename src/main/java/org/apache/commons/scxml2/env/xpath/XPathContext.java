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
package org.apache.commons.scxml2.env.xpath;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

/**
 * A {@link Context} implementation for XPath environments.
 *
 */
public class XPathContext extends SimpleContext
implements Context, XPathVariableResolver {

    /** Serial version UID. */
    private static final long serialVersionUID = -6803159294612685806L;

    /**
     * No argument constructor.
     *
     */
    public XPathContext() {
        super();
    }

    /**
     * Constructor for cascading contexts.
     *
     * @param parent The parent context. Can be null.
     */
    public XPathContext(final Context parent) {
        super(parent);
    }

    /**
     * Resolve variable by checking the backing {@link Context}.
     * TODO: Investigate alternatives to String representation.
     *
     * @param variableName The QName whose String representation is the
     *                     variable in the backing {@link Context}
     * @return The variable value.
     */
    @Override
    public Object resolveVariable(final QName variableName) {
        return get(variableName.toString());
    }

}
