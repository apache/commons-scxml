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
package org.apache.commons.scxml.env.jsp;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * A placeholder for a JspContext, to run tests against.
 */
public class MockJspContext extends JspContext
        implements VariableResolver {
    private Map vars;
    public MockJspContext() {
        super();
        vars = new HashMap();
    }
    public void setAttribute(String name, Object value) {
        vars.put(name, value);
    }
    public void setAttribute(String name, Object value, int scope) {
        setAttribute(name, value);
    }
    public Object getAttribute(String name) {
        return vars.get(name);
    }
    public Object getAttribute(String name, int scope) {
        return getAttribute(name);
    }
    public void removeAttribute(String name) {
        vars.remove(name);
    }
    public void removeAttribute(String name, int scope) {
        removeAttribute(name);
    }
    public Object findAttribute(String name) {
        return getAttribute(name);
    }
    public VariableResolver getVariableResolver() {
        return this;
    }
    public Object resolveVariable(String name) {
        return getAttribute(name);
    }
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }
    public int getAttributesScope(String name) {
        return 1;
    }
    public Enumeration getAttributeNamesInScope(int scope) {
        return null;
    }
    public JspWriter getOut() {
        return null;
    }
}

