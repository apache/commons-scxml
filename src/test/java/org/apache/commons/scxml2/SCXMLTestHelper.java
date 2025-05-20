/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.scxml2.env.SimpleDispatcher;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.io.SCXMLReader.Configuration;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.jupiter.api.Assertions;

/**
 * Helper methods for running SCXML unit tests.
 */
public class SCXMLTestHelper {

    /**
     * Serialized Commons SCXML object model temporary store.
     * Assumes the default build artifacts are generated in the
     * "target" directory (so it can be removed via a clean build).
     */
    private static final Path SERIALIZATION_DIR = Paths.get("target/serialization");
    private static final String SERIALIZATION_FILE_PREFIX = "scxml";
    private static final String SERIALIZATION_FILE_SUFFIX = ".ser";

    // Generate a unique sequence number for the serialization files
    private static int sequence=0;

    public static void assertPostTriggerState(final SCXMLExecutor exec,
            final String triggerEventName, final Object data, final String expectedStateId) throws Exception {
        assertPostTriggerState(exec, new EventBuilder(triggerEventName, TriggerEvent.SIGNAL_EVENT)
                .data(data).build(), expectedStateId);
    }

    public static void assertPostTriggerState(final SCXMLExecutor exec,
            final String triggerEventName, final String expectedStateId) throws Exception {
        assertPostTriggerState(exec, triggerEventName, null, expectedStateId);
    }

    public static void assertPostTriggerState(final SCXMLExecutor exec,
            final TriggerEvent triggerEvent, final String expectedStateId) throws Exception {
        final Set<EnterableState> currentStates = fireEvent(exec, triggerEvent);
        Assertions.assertEquals(1, currentStates.size(),
                "Expected 1 simple (leaf) state with id '"
                        + expectedStateId + "' on firing event " + triggerEvent
                        + " but found " + currentStates.size() + " states instead.");
        Assertions.assertEquals(expectedStateId, currentStates.iterator().
            next().getId());
    }

    public static void assertPostTriggerStates(final SCXMLExecutor exec,
            final String triggerEventName, final Object data, final String[] expectedStateIds) throws Exception {
        assertPostTriggerStates(exec, new EventBuilder(triggerEventName, TriggerEvent.SIGNAL_EVENT)
                .data(data).build(), expectedStateIds);
    }

    public static void assertPostTriggerStates(final SCXMLExecutor exec,
            final String triggerEventName, final String[] expectedStateIds) throws Exception {
        assertPostTriggerStates(exec, triggerEventName, null, expectedStateIds);
    }

    public static void assertPostTriggerStates(final SCXMLExecutor exec,
            final TriggerEvent triggerEvent, final String[] expectedStateIds) throws Exception {
        if (expectedStateIds == null || expectedStateIds.length == 0) {
            Assertions.fail("Must specify an array of one or more "
                + "expected state IDs");
        }
        final Set<EnterableState> currentStates = fireEvent(exec, triggerEvent);
        final int n = expectedStateIds.length;
        Assertions.assertEquals(n, currentStates.size(),
                "Expected " + n + " simple (leaf) state(s) "
                        + " on firing event " + triggerEvent + " but found "
                        + currentStates.size() + " states instead.");
        final List<String> expectedStateIdList = new ArrayList<>(Arrays.asList(expectedStateIds));
        for (final TransitionTarget tt : currentStates) {
            final String stateId = tt.getId();
            if(!expectedStateIdList.remove(stateId)) {
                Assertions.fail("Expected state with id '" + stateId
                    + "' in current states on firing event "
                    + triggerEvent);
            }
        }
        Assertions.assertEquals(0, expectedStateIdList.size(),
                "More states in current configuration than those"
                        + "specified in the expected state ids '" + Arrays.toString(expectedStateIds) + "'");
    }

    public static void assertState(final SCXMLExecutor exec, final String expectedStateId) {
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size(),
                "Expected 1 simple (leaf) state with id '" + expectedStateId +
                        "' but found " + currentStates.size() + " states instead.");
        Assertions.assertEquals(expectedStateId, currentStates.iterator().
            next().getId());
    }

    public static Set<EnterableState> fireEvent(final SCXMLExecutor exec, final String name) throws Exception {
        return fireEvent(exec, name, null);
    }

    public static Set<EnterableState> fireEvent(final SCXMLExecutor exec, final String name, final Object data) throws Exception {
        final TriggerEvent[] evts = {new EventBuilder(name, TriggerEvent.SIGNAL_EVENT).data(data).build()};
        exec.triggerEvents(evts);
        return exec.getStatus().getStates();
    }

    public static Set<EnterableState> fireEvent(final SCXMLExecutor exec, final TriggerEvent te) throws Exception {
        exec.triggerEvent(te);
        return exec.getStatus().getStates();
    }

    public static Set<EnterableState> fireEvents(final SCXMLExecutor exec, final TriggerEvent[] evts) throws Exception {
        exec.triggerEvents(evts);
        return exec.getStatus().getStates();
    }

    public static SCXMLExecutor getExecutor(final SCXML scxml) throws Exception {
        return getExecutor(scxml, null);
    }

    public static SCXMLExecutor getExecutor(final SCXML scxml, final Evaluator evaluator) throws Exception {
        return getExecutor(scxml, evaluator, new SimpleDispatcher());
    }

    public static SCXMLExecutor getExecutor(final SCXML scxml, final Evaluator evaluator, final EventDispatcher eventDispatcher) throws Exception {
        final Tracer trc = new Tracer();
        final SCXMLExecutor exec = new SCXMLExecutor(evaluator, eventDispatcher, trc);
        exec.setStateMachine(scxml);
        exec.addListener(scxml, trc);
        return exec;
    }

    public static SCXMLExecutor getExecutor(final String scxmlResource) throws Exception {
        return getExecutor(parse(scxmlResource), null);
    }

    public static SCXMLExecutor getExecutor(final URL url) throws Exception {
        return getExecutor(parse(url), null);
    }

    public static SCXMLExecutor getExecutor(final URL url, final Evaluator evaluator) throws Exception {
        return getExecutor(parse(url), evaluator);
    }

    public static URL getResource(final String name) {
        return SCXMLTestHelper.class.getClassLoader().getResource(name);
    }

    private synchronized static String getSequenceNumber() {
        return Integer.toString(++sequence);
    }

    public static Context lookupContext(final SCXMLExecutor exec, final String id) {
        final TransitionTarget tt = lookupTransitionTarget(exec, id);
        if (tt == null || !(tt instanceof EnterableState)) {
            return null;
        }
        return exec.getSCInstance().lookupContext((EnterableState)tt);
    }

    public static TransitionTarget lookupTransitionTarget(final SCXMLExecutor exec, final String id) {
        return exec.getStateMachine().getTargets().get(id);
    }

    public static SCXML parse(final Reader scxmlReader, final List<CustomAction> customActions) throws Exception {
        Assertions.assertNotNull(scxmlReader);
        final Configuration configuration = new Configuration(null, null, customActions);
        final SCXML scxml = SCXMLReader.read(scxmlReader, configuration);
        Assertions.assertNotNull(scxml);
        return testModelSerializability(scxml);
    }

    public static SCXML parse(final String scxmlResource) throws Exception {
        Assertions.assertNotNull(scxmlResource);
        return parse(getResource(scxmlResource), null);
    }

    public static SCXML parse(final String scxmlResource, final List<CustomAction> customActions) throws Exception {
        Assertions.assertNotNull(scxmlResource);
        return parse(getResource(scxmlResource), customActions);
    }

    public static SCXML parse(final URL url) throws Exception {
        return parse(url, null);
    }

    public static SCXML parse(final URL url, final List<CustomAction> customActions) throws Exception {
        Assertions.assertNotNull(url);
        final Configuration configuration = new Configuration(null, null, customActions);
        final SCXML scxml = SCXMLReader.read(url, configuration);
        Assertions.assertNotNull(scxml);
        return testModelSerializability(scxml);
    }

    public static SCXMLExecutor testInstanceSerializability(final SCXMLExecutor exec) throws Exception {
        Files.createDirectories(SERIALIZATION_DIR);
        final Path file = SERIALIZATION_DIR.resolve(SERIALIZATION_FILE_PREFIX + getSequenceNumber() + SERIALIZATION_FILE_SUFFIX);
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(exec.detachInstance());
        }
        try (ObjectInputStream in = new SCInstanceObjectInputStream(Files.newInputStream(file))) {
            exec.attachInstance((SCInstance) in.readObject());
        }
        return exec;
    }

    public static SCXML testModelSerializability(final SCXML scxml) throws Exception {
        Files.createDirectories(SERIALIZATION_DIR);
        final Path file = SERIALIZATION_DIR.resolve(SERIALIZATION_FILE_PREFIX + getSequenceNumber() + SERIALIZATION_FILE_SUFFIX);
        SCXML roundtrip;
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))) {
            out.writeObject(scxml);
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
            roundtrip = (SCXML) in.readObject();
        }
        return roundtrip;
    }

    /**
     * Discourage instantiation.
     */
    private SCXMLTestHelper() {
    }
}
