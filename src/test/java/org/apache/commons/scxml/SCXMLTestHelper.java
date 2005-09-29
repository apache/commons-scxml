/*
 * Copyright 2005 The Apache Software Foundation.
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

import org.apache.commons.scxml.env.jsp.ELEvaluator;
import org.apache.commons.scxml.env.jsp.ELContext;
import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.Tracer;
import org.apache.commons.scxml.model.SCXML;

import org.xml.sax.ErrorHandler;
/**
 * Helper methods for running SCXML unit tests.
 */
public class SCXMLTestHelper {

    public static SCXML digest(final URL url) {
        Evaluator evaluator = new ELEvaluator();
        Context ctx = new ELContext();
        return digest(url, null, ctx, evaluator);
    }

    public static SCXML digest(final URL url, final Context ctx,
            final Evaluator evaluator) {
        return digest(url, null, ctx, evaluator);
    }

    public static SCXML digest(final URL url, final ErrorHandler errHandler,
            final Context ctx, final Evaluator evaluator) {
        Assert.assertNotNull(url);
        Assert.assertNotNull(ctx);
        // SAX ErrorHandler may be null
        Assert.assertNotNull(evaluator);
        SCXML scxml = null;
        try {
            scxml = SCXMLDigester.digest(url, errHandler, ctx, evaluator);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(scxml);
        return scxml;
    }

    public static SCXMLExecutor getExecutor(final URL url) {
        Evaluator evaluator = new ELEvaluator();
        Context ctx = new ELContext();
        SCXML scxml = digest(url, null, ctx, evaluator);
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url, final Context ctx,
            final Evaluator evaluator) {
        SCXML scxml = digest(url, null, ctx, evaluator);
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final ErrorHandler errHandler,final Context ctx,
            final Evaluator evaluator) {
        SCXML scxml = digest(url, errHandler, ctx, evaluator);
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(Evaluator evaluator, SCXML scxml) {
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(Evaluator evaluator, SCXML scxml,
            EventDispatcher ed, Tracer trc) {
        Assert.assertNotNull(evaluator);
        Assert.assertNotNull(scxml);
        Assert.assertNotNull(ed);
        Assert.assertNotNull(trc);
        SCXMLExecutor exec = null;
        try {
            exec = new SCXMLExecutor(evaluator, ed, trc);
            scxml.addListener(trc);
            exec.setSuperStep(true);
            exec.setStateMachine(scxml);
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

