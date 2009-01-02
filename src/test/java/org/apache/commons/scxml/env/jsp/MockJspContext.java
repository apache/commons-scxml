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
    private Map<String, Object> vars;
    public MockJspContext() {
        super();
        vars = new HashMap<String, Object>();
    }
    @Override
    public void setAttribute(String name, Object value) {
        vars.put(name, value);
    }
    @Override
    public void setAttribute(String name, Object value, int scope) {
        setAttribute(name, value);
    }
    @Override
    public Object getAttribute(String name) {
        return vars.get(name);
    }
    @Override
    public Object getAttribute(String name, int scope) {
        return getAttribute(name);
    }
    @Override
    public void removeAttribute(String name) {
        vars.remove(name);
    }
    @Override
    public void removeAttribute(String name, int scope) {
        removeAttribute(name);
    }
    @Override
    public Object findAttribute(String name) {
        return getAttribute(name);
    }
    @Override
    public VariableResolver getVariableResolver() {
        return this;
    }
    public Object resolveVariable(String name) {
        return getAttribute(name);
    }
    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return null;
    }
    @Override
    public int getAttributesScope(String name) {
        return 1;
    }
    @Override
    public Enumeration<String> getAttributeNamesInScope(int scope) {
        return null;
    }
    @Override
    public JspWriter getOut() {
        return null;
    }
}

