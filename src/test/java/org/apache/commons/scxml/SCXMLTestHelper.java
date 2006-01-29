/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml;

import java.net.URL;

import junit.framework.Assert;

import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.Tracer;
import org.apache.commons.scxml.env.jexl.JexlContext;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.io.SCXMLDigester;
import org.apache.commons.scxml.model.SCXML;

import org.xml.sax.ErrorHandler;
/**
 * Helper methods for running SCXML unit tests.
 */
public class SCXMLTestHelper {

    public static SCXML digest(final URL url) {
        return digest(url, null);
    }

    public static SCXML digest(final URL url, final ErrorHandler errHandler) {
        Assert.assertNotNull(url);
        // SAX ErrorHandler may be null
        SCXML scxml = null;
        try {
            scxml = SCXMLDigester.digest(url, errHandler);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(scxml);
        return scxml;
    }

    public static SCXMLExecutor getExecutor(final URL url) {
        SCXML scxml = digest(url, null);
        Evaluator evaluator = new JexlEvaluator();
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final Evaluator evaluator) {
        SCXML scxml = digest(url, null);
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final ErrorHandler errHandler) {
        SCXML scxml = digest(url, errHandler);
        Evaluator evaluator = new JexlEvaluator();
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(Evaluator evaluator, SCXML scxml) {
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        Context context = new JexlContext();
        return getExecutor(context, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(final URL url, final Context ctx,
            final Evaluator evaluator) {
        SCXML scxml = digest(url, null);
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(ctx, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(Context context,
            Evaluator evaluator, SCXML scxml, EventDispatcher ed, Tracer trc) {
        Assert.assertNotNull(evaluator);
        Assert.assertNotNull(context);
        Assert.assertNotNull(scxml);
        Assert.assertNotNull(ed);
        Assert.assertNotNull(trc);
        SCXMLExecutor exec = null;
        try {
            exec = new SCXMLExecutor(evaluator, ed, trc);
            exec.addListener(scxml, trc);
            exec.setRootContext(context);
            exec.setSuperStep(true);
            exec.setStateMachine(scxml);
            exec.go();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(exec);
        return exec;
    }

    /**
     * Discourage instantiation.
     */
    private SCXMLTestHelper() {
        super();
    }

}

