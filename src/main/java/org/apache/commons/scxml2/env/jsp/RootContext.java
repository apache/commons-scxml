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

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

/**
 * EL Context for root SCXML element. Wrapper around the host JSP context.
 * Must treat variables in the host JSP environments as read-only.
 *
 */
@SuppressWarnings("serial")
public final class RootContext extends ELContext {

    /** Host JSP's VariableResolver. */
    private VariableResolver variableResolver;
    /** Bark if JSP Context is null. */
    private static final String ERR_HOST_JSP_CTX_NULL =
        "Host JSP Context cannot be null";

    /**
     * Constructor.
     *
     * @param ctx the host JspContext
     */
    public RootContext(final JspContext ctx) {
        super();
        if (ctx == null) {
            getLog().error(ERR_HOST_JSP_CTX_NULL);
            throw new IllegalArgumentException(ERR_HOST_JSP_CTX_NULL);
        } else {
          // only retain the VariableResolver
          this.variableResolver = ctx.getVariableResolver();
        }
    }

    /**
     * Get the value of the given variable in this Context.
     *
     * @param name The name of the variable
     * @return The value (or null)
     * @see org.apache.commons.scxml2.Context#get(java.lang.String)
     */
    @Override
    public Object get(final String name) {
        Object value = super.get(name);
        if (value == null) {
            try {
                value = variableResolver.resolveVariable(name);
            } catch (ELException ele) {
                getLog().error(ele.getMessage(), ele);
            }
        }
        return value;
    }

    /**
     * Does the given variable exist in this Context.
     *
     * @param name The name of the variable
     * @return boolean true if the variable exists
     * @see org.apache.commons.scxml2.Context#has(java.lang.String)
     */
    @Override
    public boolean has(final String name) {
        boolean exists = super.has(name);
        Object value = null;
        if (!exists) {
            try {
                value = variableResolver.resolveVariable(name);
            } catch (ELException ele) {
                getLog().error(ele.getMessage(), ele);
            }
            if (value != null) {
                exists = true;
            }
        }
        return exists;
    }

    /**
     * Get the VariableResolver associated with this root context.
     *
     * @return Returns the variableResolver.
     */
    public VariableResolver getVariableResolver() {
        return variableResolver;
    }

    /**
     * Set the VariableResolver associated with this root context.
     *
     * @param variableResolver The variableResolver to set.
     */
    public void setVariableResolver(final VariableResolver variableResolver) {
        this.variableResolver = variableResolver;
    }

    //--------------------------------------------------- Truth in advertising

    /**
     * Instances of this class are not serializable.
     *
     * @param out The object output stream.
     * @throws IOException Guaranteed to throw a NotSerializableException
     */
    private void writeObject(final ObjectOutputStream out)
    throws IOException {
        throw new NotSerializableException();
    }

    /**
     * Instances of this class are not serializable.
     *
     * @param in The object input stream.
     * @throws IOException Guaranteed to throw a NotSerializableException
     */
    private void readObject(final ObjectInputStream in)
    throws IOException {
        throw new NotSerializableException();
    }

}

