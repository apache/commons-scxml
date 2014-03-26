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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class for containers of executable elements in SCXML,
 * such as &lt;onentry&gt; and &lt;onexit&gt;.
 *
 */
public abstract class Executable implements Serializable {

    /**
     * The set of executable elements (those that inheriting from
     * Action) that are contained in this Executable.
     */
    private List<Action> actions;

    /**
     * The parent container, for traceability.
     */
    private EnterableState parent;

    /**
     * Constructor.
     */
    public Executable() {
        super();
        this.actions = new ArrayList<Action>();
    }

    /**
     * Get the executable actions contained in this Executable.
     *
     * @return Returns the actions.
     */
    public final List<Action> getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in
     * this Executable.
     *
     * @param action The action to add.
     */
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     * Get the EnterableState parent.
     *
     * @return Returns the parent.
     */
    public EnterableState getParent() {
        return parent;
    }

    /**
     * Set the EnterableState parent.
     *
     * @param parent The parent to set.
     */
    protected void setParent(final EnterableState parent) {
        this.parent = parent;
    }
}

