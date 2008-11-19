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
package org.apache.commons.scxml.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml.ErrorReporter;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExpressionException;
import org.apache.commons.scxml.TriggerEvent;

/**
 * An abstract base class for executable elements in SCXML,
 * such as &lt;assign&gt;, &lt;log&gt; etc.
 *
 */
public abstract class Action implements NamespacePrefixesHolder,
        Serializable {

    /**
     * Link to its parent or container.
     */
    private Executable parent;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";

    /**
     * Constructor.
     */
    public Action() {
        super();
        this.parent = null;
        this.namespaces = null;
    }

    /**
     * Get the Executable parent.
     *
     * @return Returns the parent.
     */
    public final Executable getParent() {
        return parent;
    }

    /**
     * Set the Executable parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final Executable parent) {
        this.parent = parent;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Return the {@link TransitionTarget} whose {@link Context} this action
     * executes in.
     *
     * @return The parent {@link TransitionTarget}
     * @throws ModelException For an unknown TransitionTarget subclass
     *
     * @since 0.9
     */
    public final TransitionTarget getParentTransitionTarget()
    throws ModelException {
        TransitionTarget tt = parent.getParent();
        if (tt instanceof State || tt instanceof Parallel) {
            return tt;
        } else if (tt instanceof History || tt instanceof Initial) {
            return tt.getParent();
        } else {
            throw new ModelException("Unknown TransitionTarget subclass:"
                    + tt.getClass().getName());
        }
    }

    /**
     * Execute this action instance.
     *
     * @param evtDispatcher The EventDispatcher for this execution instance
     * @param errRep        The ErrorReporter to broadcast any errors
     *                      during execution.
     * @param scInstance    The state machine execution instance information.
     * @param appLog        The application Log.
     * @param derivedEvents The collection to which any internal events
     *                      arising from the execution of this action
     *                      must be added.
     *
     * @throws ModelException If the execution causes the model to enter
     *                        a non-deterministic state.
     * @throws SCXMLExpressionException If the execution involves trying
     *                        to evaluate an expression which is malformed.
     */
    public abstract void execute(final EventDispatcher evtDispatcher,
        final ErrorReporter errRep, final SCInstance scInstance,
        final Log appLog, final Collection<TriggerEvent> derivedEvents)
    throws ModelException, SCXMLExpressionException;

    /**
     * Return the key under which the current document namespaces are saved
     * in the parent state's context.
     *
     * @return The namespaces key
     */
    protected static String getNamespacesKey() {
        return NAMESPACES_KEY;
    }

}

