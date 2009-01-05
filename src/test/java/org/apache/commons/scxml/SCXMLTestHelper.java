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
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.Tracer;
import org.apache.commons.scxml.env.jexl.JexlEvaluator;
import org.apache.commons.scxml.io.SCXMLDigester;
import org.apache.commons.scxml.io.SCXMLParser;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;
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

    // Generate a unique sequence number for the serialization files
    private static int sequence=0;

    private synchronized static String getSequenceNumber(){
        return Integer.toString(++sequence);
    }

    public static SCXML digest(final URL url) throws Exception {
        return digest(url, null, null);
    }

    public static SCXML digest(final URL url, final List customActions) throws Exception {
        return digest(url, null, customActions);
    }

    public static SCXML digest(final URL url, final ErrorHandler errHandler) throws Exception {
        return digest(url, errHandler, null);
    }

    public static SCXML digest(final URL url, final ErrorHandler errHandler,
            final List customActions) throws Exception {
        Assert.assertNotNull(url);
        // SAX ErrorHandler may be null
        SCXML scxml = null;
        scxml = SCXMLDigester.digest(url, errHandler, customActions);
        Assert.assertNotNull(scxml);
        SCXML roundtrip = testModelSerializability(scxml);
        Assert.assertNotNull(roundtrip);
        return roundtrip;
    }

    public static SCXML parse(final URL url) throws Exception {
        return parse(url, null, null);
    }

    public static SCXML parse(final URL url, final List customActions) throws Exception {
        return parse(url, null, customActions);
    }

    public static SCXML parse(final URL url, final ErrorHandler errHandler) throws Exception {
        return parse(url, errHandler, null);
    }

    public static SCXML parse(final URL url, final ErrorHandler errHandler,
            final List customActions) throws Exception {
        Assert.assertNotNull(url);
        // SAX ErrorHandler may be null
        SCXML scxml = null;
        scxml = SCXMLParser.parse(url, errHandler, customActions);
        Assert.assertNotNull(scxml);
        SCXML roundtrip = testModelSerializability(scxml);
        return roundtrip;
    }

    public static SCXMLExecutor getExecutor(final URL url) throws Exception {
        SCXML scxml = parse(url);
        Evaluator evaluator = new JexlEvaluator();
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final Evaluator evaluator) throws Exception {
        SCXML scxml = parse(url);
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(final URL url,
            final ErrorHandler errHandler) throws Exception {
        SCXML scxml = parse(url, errHandler);
        Evaluator evaluator = new JexlEvaluator();
        return getExecutor(evaluator, scxml);
    }

    public static SCXMLExecutor getExecutor(SCXML scxml) throws Exception {
        return getExecutor(scxml, null);
    }

    public static SCXMLExecutor getExecutor(SCXML scxml,
            SCXMLSemantics semantics) throws Exception {
        Evaluator evaluator = new JexlEvaluator();
        Context context = evaluator.newContext(null);
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(context, evaluator, scxml, ed, trc, semantics);
    }

    public static SCXMLExecutor getExecutor(Evaluator evaluator, SCXML scxml) throws Exception {
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        Assert.assertNotNull("Null evaluator", evaluator);
        Context context = evaluator.newContext(null);
        return getExecutor(context, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(final URL url, final Context ctx,
            final Evaluator evaluator) throws Exception {
        SCXML scxml = parse(url);
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(ctx, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(final SCXML scxml,
            final Context ctx, final Evaluator evaluator) throws Exception {
        EventDispatcher ed = new SimpleDispatcher();
        Tracer trc = new Tracer();
        return getExecutor(ctx, evaluator, scxml, ed, trc);
    }

    public static SCXMLExecutor getExecutor(Context context,
            Evaluator evaluator, SCXML scxml, EventDispatcher ed, Tracer trc) throws Exception {
        return getExecutor(context, evaluator, scxml, ed, trc, null);
    }

    public static SCXMLExecutor getExecutor(Context context,
            Evaluator evaluator, SCXML scxml, EventDispatcher ed,
            Tracer trc, SCXMLSemantics semantics) throws Exception {
        Assert.assertNotNull(evaluator);
        Assert.assertNotNull(context);
        Assert.assertNotNull(scxml);
        Assert.assertNotNull(ed);
        Assert.assertNotNull(trc);
        SCXMLExecutor exec = null;
        if (semantics == null) {
            exec = new SCXMLExecutor(evaluator, ed, trc);
        } else {
            exec = new SCXMLExecutor(evaluator, ed, trc, semantics);
        }
        Assert.assertNotNull(exec);
        exec.addListener(scxml, trc);
        exec.setRootContext(context);
        exec.setSuperStep(true);
        exec.setStateMachine(scxml);
        exec.go();
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

    public static Set fireEvent(SCXMLExecutor exec, String name) throws Exception {
        TriggerEvent[] evts = {new TriggerEvent(name,
                TriggerEvent.SIGNAL_EVENT, null)};
        exec.triggerEvents(evts);
        return exec.getCurrentStatus().getStates();
    }

    public static Set fireEvent(SCXMLExecutor exec, TriggerEvent te) throws Exception {
        exec.triggerEvent(te);
        return exec.getCurrentStatus().getStates();
    }

    public static Set fireEvents(SCXMLExecutor exec, TriggerEvent[] evts) throws Exception {
        exec.triggerEvents(evts);
        return exec.getCurrentStatus().getStates();
    }

    public static void assertPostTriggerState(SCXMLExecutor exec,
            String triggerEventName, String expectedStateId) throws Exception {
        assertPostTriggerState(exec, new TriggerEvent(triggerEventName,
                TriggerEvent.SIGNAL_EVENT), expectedStateId);
    }

    public static void assertPostTriggerStates(SCXMLExecutor exec,
            String triggerEventName, String[] expectedStateIds) throws Exception {
        assertPostTriggerStates(exec, new TriggerEvent(triggerEventName,
                TriggerEvent.SIGNAL_EVENT), expectedStateIds);
    }

    public static void assertPostTriggerState(SCXMLExecutor exec,
            TriggerEvent triggerEvent, String expectedStateId) throws Exception {
        Set currentStates = fireEvent(exec, triggerEvent);
        Assert.assertEquals("Expected 1 simple (leaf) state with id '"
            + expectedStateId + "' on firing event " + triggerEvent
            + " but found " + currentStates.size() + " states instead.",
            1, currentStates.size());
        Assert.assertEquals(expectedStateId, ((State)currentStates.iterator().
            next()).getId());
    }

    public static void assertPostTriggerStates(SCXMLExecutor exec,
            TriggerEvent triggerEvent, String[] expectedStateIds) throws Exception {
        if (expectedStateIds == null || expectedStateIds.length == 0) {
            Assert.fail("Must specify an array of one or more "
                + "expected state IDs");
        }
        Set currentStates = fireEvent(exec, triggerEvent);
        int n = expectedStateIds.length;
        Assert.assertEquals("Expected " + n + " simple (leaf) state(s) "
            + " on firing event " + triggerEvent + " but found "
            + currentStates.size() + " states instead.",
            n, currentStates.size());
        List expectedStateIdList = new ArrayList(Arrays.asList(expectedStateIds));
        Iterator iter = currentStates.iterator();
        while (iter.hasNext()) {
            String stateId = ((State) iter.next()).getId();
            if(!expectedStateIdList.remove(stateId)) {
                Assert.fail("Expected state with id '" + stateId
                    + "' in current states on firing event "
                    + triggerEvent);
            }
        }
        Assert.assertEquals("More states in current configuration than those"
            + "specified in the expected state ids '" + expectedStateIds
            + "'", 0, expectedStateIdList.size());
    }

    public static SCXML testModelSerializability(final SCXML scxml) throws Exception {
        File fileDir = new File(SERIALIZATION_DIR);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            System.err.println("SKIPPED SERIALIZATION: Failed directory creation");
            return scxml;
        }
        String filename = SERIALIZATION_FILE_PREFIX
            + getSequenceNumber() + SERIALIZATION_FILE_SUFFIX;
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
            String msg = nse.getMessage();
            // Check that it is the DOM that caused the problem
            if (msg.startsWith("org.apache.crimson.tree.")){
                // <data> nodes failed serialization, test cases do not add
                // other non-serializable context data
                System.err.println("SERIALIZATION ERROR: The DOM implementation"
                    + " in use is not serializable: " + msg);
                return scxml;                
            } else {
                throw nse;
            }
        }
        return roundtrip;
    }

    public static SCXMLExecutor testExecutorSerializability(final SCXMLExecutor exec) throws Exception {
        File fileDir = new File(SERIALIZATION_DIR);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            System.err.println("SKIPPED SERIALIZATION: Failed directory creation");
            return exec;
        }
        String filename = SERIALIZATION_FILE_PREFIX
            + getSequenceNumber() + SERIALIZATION_FILE_SUFFIX;
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
            String msg = nse.getMessage();
            // Check that it is the DOM that caused the problem
            if (msg.startsWith("org.apache.crimson.tree.")){
                // <data> nodes failed serialization, test cases do not add
                // other non-serializable context data
                System.err.println("SERIALIZATION ERROR: The DOM implementation"
                    + " in use is not serializable: " + msg);
                return exec;                
            } else {
                throw nse;
            }
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

