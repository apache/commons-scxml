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
package org.apache.commons.scxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.Tracer;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.io.SCXMLDigester;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.TransitionTarget;
import org.xml.sax.ErrorHandler;
/**
 * Helper methods for running SCXML unit tests.
 */
public class SCXMLTestHelper {

    /**
     * Serialized Commons SCXML object model temporary store.
     * Assumes the default build artifacts are generated in the
     * "target" directory (so it can be removed via a clean build).
     */
    public static final String SERIALIZATION_DIR = "target/serialization";
    public static final String SERIALIZATION_FILE_PREFIX =
        SERIALIZATION_DIR + "/scxml";
    public static final String SERIALIZATION_FILE_SUFFIX = ".ser";

    public static SCXML digest(final URL url) {
        return digest(url, null, null);
    }

    public static SCXML digest(final URL url, final List customActions) {
        return digest(url, null, customActions);
    }

    public static SCXML digest(final URL url, final ErrorHandler errHandler) {
        return digest(url, errHandler, null);
    }

    public static SCXML digest(final URL url, final ErrorHandler errHandler,
            final List customActions) {
        Assert.assertNotNull(url);
        // SAX ErrorHandler may be null
        SCXML scxml = null;
        try {
            scxml = SCXMLDigester.digest(url, errHandler, customActions);
        } catch (Exception e) {
            Log log = LogFactory.getLog(SCXMLTestHelper.class);
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(scxml);
        SCXML roundtrip = testModelSerializability(scxml);
        Assert.assertNotNull(roundtrip);
        return roundtrip;
    }

    public static SCXML parse(final URL url) {
        return parse(url, null, null);
    }

    public static SCXML parse(final URL url, final List customActions) {
        return parse(url, null, customActions);
    }

    public static SCXML parse(final URL url, final ErrorHandler errHandler) {
        return parse(url, errHandler, null);
    }

    public static SCXML parse(final URL url, final ErrorHandler errHandler,
            final List customActions) {
        Assert.assertNotNull(url);
        // SAX ErrorHandler may be null
        SCXML scxml = null;
        try {
            scxml = SCXMLParser.parse(url, errHandler, customActions);
        } catch (Exception e) {
            Log log = LogFactory.getLog(SCXMLTestHelper.class);
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(scxml);
        SCXML roundtrip = testModelSerializability(scxml);
        return roundtrip;
    }

    public static SCXMLExecutor getExecutor(final URL url) {
        SCXML scxml = digest(url);
        Evaluator evaluator = new JexlEvaluator();
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final Evaluator evaluator) {
        SCXML scxml = digest(url);
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final ErrorHandler errHandler) {
        SCXML scxml = digest(url, errHandler);
        Evaluator evaluator = new JexlEvaluator();
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(SCXML scxml) {
        return getExecutor(scxml, null);
    }

    public static SCXMLExecutor getExecutor(SCXML scxml,
            SCXMLSemantics semantics) {
        Evaluator evaluator = new JexlEvaluator();
        Context context = evaluator.newContext(null);
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(context, evaluator, scxml, ed, trc, semantics);
    }

    public static SCXMLExecutor getExecutor(Evaluator evaluator, SCXML scxml) {
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        Assert.assertNotNull("Null evaluator", evaluator);
        Context context = evaluator.newContext(null);
        return getExecutor(context, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(final URL url, final Context ctx,
            final Evaluator evaluator) {
        SCXML scxml = digest(url);
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(ctx, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(final SCXML scxml,
            final Context ctx, final Evaluator evaluator) {
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(ctx, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(Context context,
            Evaluator evaluator, SCXML scxml, EventDispatcher ed, Tracer trc) {
        return getExecutor(context, evaluator, scxml, ed, trc, null);
    }

    public static SCXMLExecutor getExecutor(Context context,
            Evaluator evaluator, SCXML scxml, EventDispatcher ed,
            Tracer trc, SCXMLSemantics semantics) {
        Assert.assertNotNull(evaluator);
        Assert.assertNotNull(context);
        Assert.assertNotNull(scxml);
        Assert.assertNotNull(ed);
        Assert.assertNotNull(trc);
        SCXMLExecutor exec = null;
        try {
            if (semantics == null) {
                exec = new SCXMLExecutor(evaluator, ed, trc);
            } else {
                exec = new SCXMLExecutor(evaluator, ed, trc, semantics);
            }
            exec.addListener(scxml, trc);
            exec.setRootContext(context);
            exec.setSuperStep(true);
            exec.setStateMachine(scxml);
            exec.go();
        } catch (Exception e) {
            Log log = LogFactory.getLog(SCXMLTestHelper.class);
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(exec);
        return exec;
    }

    public static TransitionTarget lookupTransitionTarget(SCXMLExecutor exec,
            String id) {
        return (TransitionTarget) exec.getStateMachine().getTargets().get(id);
    }

    public static Context lookupContext(SCXMLExecutor exec,
            TransitionTarget tt) {
        return exec.getSCInstance().lookupContext(tt);
    }

    public static Context lookupContext(SCXMLExecutor exec,
            String id) {
        TransitionTarget tt = lookupTransitionTarget(exec, id);
        if (tt == null) {
            return null;
        }
        return exec.getSCInstance().lookupContext(tt);
    }

    public static Set fireEvent(SCXMLExecutor exec, String name) {
        TriggerEvent[] evts = {new TriggerEvent(name,
                TriggerEvent.SIGNAL_EVENT, null)};
        try {
            exec.triggerEvents(evts);
        } catch (Exception e) {
            Log log = LogFactory.getLog(SCXMLTestHelper.class);
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        return exec.getCurrentStatus().getStates();
    }

    public static Set fireEvent(SCXMLExecutor exec, TriggerEvent te) {
        TriggerEvent[] evts = { te };
        try {
            exec.triggerEvents(evts);
        } catch (Exception e) {
            Log log = LogFactory.getLog(SCXMLTestHelper.class);
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        return exec.getCurrentStatus().getStates();
    }

    public static Set fireEvents(SCXMLExecutor exec, TriggerEvent[] evts) {
        try {
            exec.triggerEvents(evts);
        } catch (Exception e) {
            Log log = LogFactory.getLog(SCXMLTestHelper.class);
            log.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        return exec.getCurrentStatus().getStates();
    }

    public static SCXML testModelSerializability(final SCXML scxml) {
        File fileDir = new File(SERIALIZATION_DIR);
        if (!fileDir.exists() && !fileDir.mkdir()) {
            System.err.println("SKIPPED SERIALIZATION: Failed directory creation");
            return scxml;
        }
        String filename = SERIALIZATION_FILE_PREFIX
            + System.currentTimeMillis() + SERIALIZATION_FILE_SUFFIX;
        SCXML roundtrip = null;
        try {
            ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(scxml);
            out.close();
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(filename));
            roundtrip = (SCXML) in.readObject();
            in.close();
        } catch (NotSerializableException nse) {
            // <data> nodes failed serialization
            System.err.println("SERIALIZATION ERROR: The DOM implementation"
                + " in use is not serializable");
            return scxml;
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch(ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return roundtrip;
    }

    public static SCXMLExecutor testExecutorSerializability(final SCXMLExecutor exec) {
        File fileDir = new File(SERIALIZATION_DIR);
        if (!fileDir.exists() && !fileDir.mkdir()) {
            System.err.println("SKIPPED SERIALIZATION: Failed directory creation");
            return exec;
        }
        String filename = SERIALIZATION_FILE_PREFIX
            + System.currentTimeMillis() + SERIALIZATION_FILE_SUFFIX;
        SCXMLExecutor roundtrip = null;
        try {
            ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(exec);
            out.close();
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(filename));
            roundtrip = (SCXMLExecutor) in.readObject();
            in.close();
        } catch (NotSerializableException nse) {
            // <data> nodes failed serialization, test cases do not add
            // other non-serializable context data
            System.err.println("SERIALIZATION ERROR: The DOM implementation"
                + " in use is not serializable");
            return exec;
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch(ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return roundtrip;
    }

    /**
     * Discourage instantiation.
     */
    private SCXMLTestHelper() {
        super();
    }

}

