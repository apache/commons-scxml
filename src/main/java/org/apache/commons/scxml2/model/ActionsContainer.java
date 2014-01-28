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

import java.util.List;

/**
 * An <code>ActionsContainer</code> is an entity that holds a list of <code>Action</code> elements.
 */
public interface ActionsContainer {

    /** The <if> ActionsContainer element name */
    String ELEM_IF = "if";
    /** The <foreach> ActionsContainer element name */
    String ELEM_FOREACH = "foreach";

    /**
     * Get the Document element type for this &lt;container&gt;.
     *
     * @return Returns the element type
     */
    String getContainerElementName();
    /**
     * Get the executable actions contained in this &lt;container&gt;.
     *
     * @return Returns the actions.
     */
    List<Action> getActions();

    /**
     * Add an Action to the list of executable actions contained in
     * this &lt;container&gt;.
     *
     * @param action The action to add.
     */
    void addAction(final Action action);
}
