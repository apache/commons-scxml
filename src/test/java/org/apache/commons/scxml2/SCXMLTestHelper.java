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
package org.apache.commons.scxml2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.io.SCXMLReader.Configuration;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
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

    public static SCXML parse(final URL url) throws Exception {
        return parse(url, null);
    }

    public static SCXML parse(final URL url, final List<CustomAction> customActions) throws Exception {
        Assert.assertNotNull(url);
        SCXML scxml = null;
        Configuration configuration = new Configuration(null, null, customActions);
        scxml = SCXMLReader.read(url, configuration);
        Assert.assertNotNull(scxml);
        SCXML roundtrip = testModelSerializability(scxml);
        return roundtrip;
    }

    public static SCXML parse(final Reader scxmlReader, final List<CustomAction> customActions) throws Exception {
        Assert.assertNotNull(scxmlReader);
        SCXML scxml = null;
        Configuration configuration = new Configuration(null, null, customActions);
        scxml = SCXMLReader.read(scxmlReader, configuration);
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
        exec.addListener(scxml, trc);
        exec.setRootContext(context);
        exec.setSuperStep(true);
        exec.setStateMachine(scxml);
        exec.go();
        Assert.assertNotNull(exec);
        return exec;
    }

    public static TransitionTarget lookupTransitionTarget(SCXMLExecutor exec,
            String id) {
        return exec.getStateMachine().getTargets().get(id);
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

    public static void assertState(SCXMLExecutor exec,
            String expectedStateId) throws Exception {
        Set<TransitionTarget> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals("Expected 1 simple (leaf) state with id '"
            + expectedStateId + "' but found " + currentStates.size() + " states instead.",
            1, currentStates.size());
        Assert.assertEquals(expectedStateId, currentStates.iterator().
            next().getId());
    }

    public static Set<TransitionTarget> fireEvent(SCXMLExecutor exec, String name) throws Exception {
        TriggerEvent[] evts = {new TriggerEvent(name,
                TriggerEvent.SIGNAL_EVENT, null)};
        exec.triggerEvents(evts);
        return exec.getCurrentStatus().getStates();
    }

    public static Set<TransitionTarget> fireEvent(SCXMLExecutor exec, TriggerEvent te) throws Exception {
        exec.triggerEvent(te);
        return exec.getCurrentStatus().getStates();
    }

    public static Set<TransitionTarget> fireEvents(SCXMLExecutor exec, TriggerEvent[] evts) throws Exception {
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
        Set<TransitionTarget> currentStates = fireEvent(exec, triggerEvent);
        Assert.assertEquals("Expected 1 simple (leaf) state with id '"
            + expectedStateId + "' on firing event " + triggerEvent
            + " but found " + currentStates.size() + " states instead.",
            1, currentStates.size());
        Assert.assertEquals(expectedStateId, currentStates.iterator().
            next().getId());
    }

    public static void assertPostTriggerStates(SCXMLExecutor exec,
            TriggerEvent triggerEvent, String[] expectedStateIds) throws Exception {
        if (expectedStateIds == null || expectedStateIds.length == 0) {
            Assert.fail("Must specify an array of one or more "
                + "expected state IDs");
        }
        Set<TransitionTarget> currentStates = fireEvent(exec, triggerEvent);
        int n = expectedStateIds.length;
        Assert.assertEquals("Expected " + n + " simple (leaf) state(s) "
            + " on firing event " + triggerEvent + " but found "
            + currentStates.size() + " states instead.",
            n, currentStates.size());
        List<String> expectedStateIdList = new ArrayList<String>(Arrays.asList(expectedStateIds));
        for (TransitionTarget tt : currentStates) {
            String stateId = tt.getId();
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
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        String filename = SERIALIZATION_FILE_PREFIX
            + getSequenceNumber() + SERIALIZATION_FILE_SUFFIX;
        SCXML roundtrip = null;
        ObjectOutputStream out =
            new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(scxml);
        out.close();
        ObjectInputStream in =
            new ObjectInputStream(new FileInputStream(filename));
        roundtrip = (SCXML) in.readObject();
        in.close();
        return roundtrip;
    }

    public static SCXMLExecutor testExecutorSerializability(final SCXMLExecutor exec) throws Exception {
        File fileDir = new File(SERIALIZATION_DIR);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        String filename = SERIALIZATION_FILE_PREFIX
            + getSequenceNumber() + SERIALIZATION_FILE_SUFFIX;
        SCXMLExecutor roundtrip = null;
        ObjectOutputStream out =
            new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(exec);
        out.close();
        ObjectInputStream in =
            new ObjectInputStream(new FileInputStream(filename));
        roundtrip = (SCXMLExecutor) in.readObject();
        in.close();
        return roundtrip;
    }

    /**
     * Parses a String containing XML source into a {@link Document}.
     *
     * @param xml The XML source as a String.
     * @return The parsed {@link Document}.
     */
    public static Document stringToXMLDocument(final String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException("Exception parsing String to Node:\n" + xml);
        }
    }

    /**
     * Get the active leaf state for this executor instance.
     * Assumes no usage of &lt;parallel&gt;.
     *
     * @param exec The {@link SCXMLExecutor} instance whose active state is
     *             being queried.
     * @return The <code>id</code> of the active state.
     */
    public static String getCurrentState(SCXMLExecutor exec) {
        Set<TransitionTarget> current = exec.getCurrentStatus().getStates();
        TransitionTarget active = current.iterator().next();
        return active.getId();
    }

    /**
     * Set the active leaf state for this executor instance.
     * Assumes no usage of &lt;parallel&gt;.
     *
     * @param exec The {@link SCXMLExecutor} instance whose active state is
     *             to be set.
     * @param id The <code>id</code> of the state to be made active.
     */
    public static void setCurrentState(SCXMLExecutor exec, final String id) {
        try {
            exec.reset();
        } catch (ModelException me) {
            throw new IllegalArgumentException("Provided SCXMLExecutor "
                + "instance cannot be reset.");
        }
        TransitionTarget active = exec.getStateMachine().getTargets().get(id);
        if (active == null) {
            throw new IllegalArgumentException("No target with id '" + id
                + "' present in state machine.");
        }
        Set<TransitionTarget> current = exec.getCurrentStatus().getStates();
        current.clear();
        current.add(active);
    }

    public static String removeCarriageReturns(final String original) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < original.length(); i++) {
            char c = original.charAt(i);
            if (c != '\r') {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * Discourage instantiation.
     */
    private SCXMLTestHelper() {
        super();
    }

}
