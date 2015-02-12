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
package org.apache.commons.scxml2.invoke;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLIOProcessor;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.SimpleSCXMLListener;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document
 * may not contain external namespace elements, further invokes etc.
 */
public class SimpleSCXMLInvoker implements Invoker, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Parent state ID. */
    private String parentStateId;
    /** Invoking parent SCXMLExecutor */
    private SCXMLExecutor parentSCXMLExecutor;
    /** The invoked state machine executor. */
    private SCXMLExecutor executor;
    /** Cancellation status. */
    private boolean cancelled;


    /**
     * {@inheritDoc}.
     */
    @Override
    public String getInvokeId() {
        return parentStateId;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setInvokeId(final String invokeId) {
        this.parentStateId = invokeId;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor parentSCXMLExecutor) {
        this.parentSCXMLExecutor = parentSCXMLExecutor;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        // not used
        return executor;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final String source, final Map<String, Object> params)
    throws InvokerException {
        SCXML scxml;
        try {
            scxml = SCXMLReader.read(new URL(source));
        } catch (ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        } catch (IOException ioe) {
            throw new InvokerException(ioe.getMessage(), ioe.getCause());
        } catch (XMLStreamException xse) {
            throw new InvokerException(xse.getMessage(), xse.getCause());
        }
        executor = new SCXMLExecutor(parentSCXMLExecutor);
        try {
            executor.setStateMachine(scxml);
        }
        catch (ModelException me) {
            throw new InvokerException(me);
        }
        Context rootCtx = executor.getRootContext();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            rootCtx.setLocal(entry.getKey(), entry.getValue());
        }
        executor.addListener(scxml, new SimpleSCXMLListener());
        try {
            executor.go();
        } catch (ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
        if (executor.getStatus().isFinal()) {
            TriggerEvent te = new TriggerEvent("done.invoke."+parentStateId, TriggerEvent.SIGNAL_EVENT);
            new AsyncTrigger(parentSCXMLExecutor, te).start();
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvent(final TriggerEvent evt)
    throws InvokerException {
        if (cancelled) {
            return; // no further processing should take place
        }
        boolean doneBefore = executor.getStatus().isFinal();
        executor.addEvent(evt);
        if (!doneBefore && executor.getStatus().isFinal()) {
            TriggerEvent te = new TriggerEvent("done.invoke."+parentStateId,TriggerEvent.SIGNAL_EVENT);
            new AsyncTrigger(parentSCXMLExecutor, te).start();
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel()
    throws InvokerException {
        cancelled = true;
        executor.addEvent(new TriggerEvent("cancel.invoke."+parentStateId, TriggerEvent.CANCEL_EVENT));
    }
}

