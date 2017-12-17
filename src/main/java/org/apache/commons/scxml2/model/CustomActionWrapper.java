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
package org.apache.commons.scxml2.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.SCXMLExpressionException;

/**
 * CustomActionWrapper wraps a custom action and keeps track of its XML element name, and attributes and namespaces
 * for writing the custom action (back) to XML
 */
public class CustomActionWrapper extends Action {

    /**
     * The custom Action instance
     */
    private Action action;

    /**
     * The custom action XML element prefix;
     */
    private String prefix;

    /**
     * The custom action XML element local name;
     */
    private String localName;

    /**
     * The custom XML namespaces in effect for the custom action element
     */
    private final Map<String, String> namespaces = new HashMap<>();

    /**
     * The attributes defined on the custom action element
     */
    private Map<String, String> attributes;

    public Action getAction() {
        return action;
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    /**
     * @return the custom action element prefix (might be null)
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the custom action XML element prefix
     * @param prefix custom action XML element prefix
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return the custom action XML element local name
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Set the custom action XML element local name
     * @param localName custom action XML element local name
     */
    public void setLocalName(final String localName) {
        this.localName = localName;
    }

    /**
     * Get the custom XML namespaces in effect for this custom action
     *
     * @return Returns the map of namespaces.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Get the attributes defined on the custom action element
     *
     * @return Returns the map of attributes.
     */
    public final Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Set the attributes defined on the custom action element
     * @param attributes the attributes to set
     */
    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Executable getParent() {
        return action.getParent();
    }

    @Override
    public void setParent(final Executable parent) {
        action.setParent(parent);
    }

    @Override
    public EnterableState getParentEnterableState() throws ModelException {
        return action.getParentEnterableState();
    }

    @Override
    public void execute(final ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException, ActionExecutionError {
        action.execute(exctx);
    }
}
