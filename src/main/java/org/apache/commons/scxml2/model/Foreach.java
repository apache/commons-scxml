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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.TriggerEvent;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;foreach&gt; SCXML element, which allows an SCXML application to iterate through a collection in the data model
 * and to execute the actions contained within it for each item in the collection.
 */
public class Foreach extends Action implements ActionsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private String array;
    private String item;
    private String index;

    /**
     * The set of executable elements (those that inheriting from
     * Action) that are contained in this &lt;if&gt; element.
     */
    private List<Action> actions;

    public Foreach() {
        super();
        this.actions = new ArrayList<Action>();
    }

    @Override
    public final String getContainerElementName() {
        return ELEM_FOREACH;
    }

    @Override
    public final List<Action> getActions() {
        return actions;
    }

    @Override
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    public String getArray() {
        return array;
    }

    public void setArray(final String array) {
        this.array = array;
    }

    public String getItem() {
        return item;
    }

    public void setItem(final String item) {
        this.item = item;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(final String index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final EventDispatcher evtDispatcher,
                        final ErrorReporter errRep, final SCInstance scInstance,
                        final Log appLog, final Collection<TriggerEvent> derivedEvents)
            throws ModelException, SCXMLExpressionException {
        Context ctx = scInstance.getContext(getParentTransitionTarget());
        Evaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        try {
            Object arrayObject = eval.eval(ctx,array);
            if (arrayObject != null && (arrayObject instanceof Iterable || arrayObject.getClass().isArray())) {
                if (arrayObject.getClass().isArray()) {
                    for (int currentIndex = 0, size = Array.getLength(arrayObject); currentIndex < size; currentIndex++) {
                        ctx.setLocal(item, Array.get(arrayObject, currentIndex));
                        ctx.setLocal(index, currentIndex);
                        // The "foreach" statement is a "container"
                        for (Action aa : actions) {
                            aa.execute(evtDispatcher, errRep, scInstance, appLog, derivedEvents);
                        }
                    }
                }
                else {
                    // Spec requires to iterate over a shallow copy of underlying array in a way that modifications to
                    // the collection during the execution of <foreach> must not affect the iteration behavior.
                    // For array objects (see above) this isn't needed, but for Iterables we don't have that guarantee
                    // so we make a copy first
                    ArrayList<Object> arrayList = new ArrayList<Object>();
                    for (Object value: (Iterable)arrayObject) {
                        arrayList.add(value);
                    }
                    int currentIndex = 0;
                    for (Object value : arrayList) {
                        ctx.setLocal(item, value);
                        if (index != null) {
                            ctx.setLocal(index, currentIndex);
                        }
                        // The "foreach" statement is a "container"
                        for (Action aa : actions) {
                            aa.execute(evtDispatcher, errRep, scInstance, appLog, derivedEvents);
                        }
                        currentIndex++;
                    }
                }
            }
            // else {} TODO: place the error 'error.execution' in the internal event queue. (section "3.12.2 Errors")
        }
        finally {
            ctx.setLocal(getNamespacesKey(), null);
        }
    }
}
