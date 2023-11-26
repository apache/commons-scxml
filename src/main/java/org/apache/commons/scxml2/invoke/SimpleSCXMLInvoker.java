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
import java.io.StringReader;
import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.scxml2.EventBuilder;
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
    /** invokeId ID. */
    private String invokeId;
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
    public void cancel()
    throws InvokerException {
        cancelled = true;
        executor.getParentSCXMLIOProcessor().close();
        executor.addEvent(new EventBuilder("cancel.invoke."+ invokeId, TriggerEvent.CANCEL_EVENT).build());
    }

    protected void execute(final SCXML scxml, final Map<String, Object> params) throws InvokerException {
        try {
            executor = new SCXMLExecutor(parentSCXMLExecutor, invokeId, scxml);
        }
        catch (final ModelException me) {
            throw new InvokerException(me);
        }
        executor.addListener(scxml, new SimpleSCXMLListener());
        try {
            executor.run(params);
        } catch (final ModelException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
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
    public String getInvokeId() {
        return invokeId;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final String url, final Map<String, Object> params)
    throws InvokerException {
        SCXML scxml;
        try {
            scxml = SCXMLReader.read(new URL(url));
        } catch (ModelException | IOException | XMLStreamException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
        execute(scxml, params);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invokeContent(final String content, final Map<String, Object> params)
            throws InvokerException {
        SCXML scxml;
        try {
            scxml = SCXMLReader.read(new StringReader(content));
        } catch (ModelException | IOException | XMLStreamException me) {
            throw new InvokerException(me.getMessage(), me.getCause());
        }
        execute(scxml, params);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvent(final TriggerEvent evt)
    throws InvokerException {
        if (!cancelled) {
            executor.addEvent(evt);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setInvokeId(final String invokeId) {
        this.invokeId = invokeId;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setParentSCXMLExecutor(final SCXMLExecutor parentSCXMLExecutor) {
        this.parentSCXMLExecutor = parentSCXMLExecutor;
    }
}

