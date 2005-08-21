/*
 *
 *   Copyright 2005 The Apache Software Foundation.
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
package org.apache.commons.scxml.env;

import java.util.Iterator;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

/**
 * EL Context for root SCXML element. Wrapper around the host JSP context.
 * Must treat variables in the host JSP environments as read-only.
 *
 */
public final class RootContext extends ELContext {

    /** Host JSP's VariableResolver. */
    private VariableResolver vr;
    /**
     * Constructor.
     *
     * @param ctx the host JspContext
     */
    public RootContext(final JspContext ctx) {
        super();
        if (ctx == null) {
            log.error("Host JSP Context cannot be null");
        }
        // only retain the VariableResolver
        this.vr = ctx.getVariableResolver();
    }

    /**
     * Get the value of the given variable in this Context.
     *
     * @param name The name of the variable
     * @return The value (or null)
     * @see org.apache.commons.scxml.Context#get(java.lang.String)
     */
    public Object get(final String name) {
        Object value = super.get(name);
        if (value == null) {
            try {
                value = vr.resolveVariable(name);
            } catch (ELException ele) {
                log.error(ele.getMessage(), ele);
            }
        }
        return value;
    }

    /**
     * Get the Iterator.
     *
     * @see org.apache.commons.scxml.Context#iterator()
     */
    public Iterator iterator() {
        // The reason why this method body exists is to emphasize that
        // read-only (JSP) variables are not included in the Iterator
        return super.iterator();
    }

}

