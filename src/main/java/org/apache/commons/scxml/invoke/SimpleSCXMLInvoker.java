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
package org.apache.commons.scxml.invoke;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.SCInstance;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.SimpleErrorHandler;
import org.apache.commons.scxml.env.SimpleErrorReporter;
import org.apache.commons.scxml.env.SimpleSCXMLListener;
import org.apache.commons.scxml.io.SCXMLDigester;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;
import org.xml.sax.SAXException;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document
 * may not contain external namespace elements, further invokes etc.
 */
public class SimpleSCXMLInvoker implements Invoker, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Parent state ID. */
    private String parentStateId;
    /** Event prefix, all events sent to the parent executor must begin
     *  with this prefix. */
    private String eventPrefix;
    /** Invoking document's SCInstance. */
    private SCInstance parentSCInstance;
    /** The invoked state machine executor. */
    private SCXMLExecutor executor;
    /** Cancellation status. */
    private boolean cancelled;

    //// Constants
    /** Prefix for all events sent to the parent state machine. */
    private static String invokePrefix = ".invoke.";
    /** Suffix for invoke done event. */
    private static String invokeDone = "done";
    /** Suffix for invoke cancel response event. */
    private static String invokeCancelResponse = "cancel.response";

    /**
     * {@inheritDoc}.
     */
    public void setParentStateId(final String parentStateId) {
        this.parentStateId = parentStateId;
        this.eventPrefix = parentStateId + invokePrefix;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    public void setSCInstance(final SCInstance scInstance) {
        this.parentSCInstance = scInstance;
    }

    /**
     * {@inheritDoc}.
     */
    public void invoke(final String source, final Map params)
    throws InvokerException {
        SCXML scxml = null;
        try {
            scxml = SCXMLDigester.digest(new URL(source),
                new SimpleErrorHandler());
        } catch (ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        } catch (IOException ioe) {
            throw new InvokerException(ioe.getMessage(), ioe.getCause());
        } catch (SAXException se) {
            throw new InvokerException(se.getMessage(), se.getCause());
        }
        Evaluator eval = parentSCInstance.getEvaluator();
        executor = new SCXMLExecutor(eval,
            new SimpleDispatcher(), new SimpleErrorReporter());
        Context rootCtx = eval.newContext(null);
        for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            rootCtx.setLocal((String) entry.getKey(), entry.getValue());
        }
        executor.setRootContext(rootCtx);
        executor.setStateMachine(scxml);
        executor.addListener(scxml, new SimpleSCXMLListener());
        executor.registerInvokerClass("scxml", this.getClass());
        try {
            executor.go();
        } catch (ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
        if (executor.getCurrentStatus().isFinal()) {
            TriggerEvent te = new TriggerEvent(eventPrefix + invokeDone,
                TriggerEvent.SIGNAL_EVENT);
            new AsyncTrigger(parentSCInstance.getExecutor(), te).start();
        }
    }

    /**
     * {@inheritDoc}.
     */
    public void parentEvents(final TriggerEvent[] evts)
    throws InvokerException {
        if (cancelled) {
            return; // no further processing should take place
        }
        boolean doneBefore = executor.getCurrentStatus().isFinal();
        try {
            executor.triggerEvents(evts);
        } catch (ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
        if (!doneBefore && executor.getCurrentStatus().isFinal()) {
            TriggerEvent te = new TriggerEvent(eventPrefix + invokeDone,
                TriggerEvent.SIGNAL_EVENT);
            new AsyncTrigger(parentSCInstance.getExecutor(), te).start();
        }
    }

    /**
     * {@inheritDoc}.
     */
    public void cancel()
    throws InvokerException {
        cancelled = true;
        TriggerEvent te = new TriggerEvent(eventPrefix
            + invokeCancelResponse, TriggerEvent.SIGNAL_EVENT);
        new AsyncTrigger(parentSCInstance.getExecutor(), te).start();
    }

}

