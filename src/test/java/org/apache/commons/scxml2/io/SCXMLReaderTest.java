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
package org.apache.commons.scxml2.io;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.io.SCXMLReader.Configuration;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.CustomActionWrapper;
import org.apache.commons.scxml2.model.Data;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ParsedValue;
import org.apache.commons.scxml2.model.ParsedValueContainer;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Send;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.io.SCXMLReader}.
 */
class SCXMLReaderTest {

    public static class MyAction extends Action implements ParsedValueContainer {

        private ParsedValue parsedValue;

        @Override
        public void execute(final ActionExecutionContext exctx) {
            // Not relevant to test
        }

        @Override
        public ParsedValue getParsedValue() {
            return parsedValue;
        }

        @Override
        public void setParsedValue(final ParsedValue parsedValue) {
            this.parsedValue = parsedValue;
        }
    }

    /**
     * Custom LogFactory implementation to capture log messages for logging verification.
     */
    public static class RecordingLogFactory extends LogFactoryImpl {
        @Override
        protected Log newInstance(final String name) throws LogConfigurationException {
            return new RecordingSimpleLog(name);
        }
    }

    /**
     * Custom Simple Log implemenation capturing log messages
     */
    public static class RecordingSimpleLog extends SimpleLog {

        private final List<String> messages = new LinkedList<>();

        RecordingSimpleLog(final String name) {
            super(name);
        }

        /**
         * Clear all the recorded log messages.
         */
        void clearMessages() {
            messages.clear();
        }

        /**
         * Return true if msg is found in any recorded log messages.
         * @param msg
         * @return
         */
        boolean containsMessage(final String msg) {
            for (final String message : messages) {
                if (message.contains(msg)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected boolean isLevelEnabled(final int logLevel) {
            return logLevel >= LOG_LEVEL_INFO;
        }

        @Override
        protected void log(final int type, final Object message, final Throwable t) {
            messages.add(message.toString());
        }
    }

    private static String oldLogFactoryProperty;

    @AfterAll
    public static void afterClass() {
        if (oldLogFactoryProperty == null) {
            System.clearProperty(LogFactory.FACTORY_PROPERTY);
        } else {
            System.setProperty(LogFactory.FACTORY_PROPERTY, oldLogFactoryProperty);
        }
    }

    @BeforeAll
    public static void beforeClass() {
        oldLogFactoryProperty = System.getProperty(LogFactory.FACTORY_PROPERTY);
        System.setProperty(LogFactory.FACTORY_PROPERTY, RecordingLogFactory.class.getName());
    }

    private Log scxmlReaderLog;

    private void assertContainsRecordedLogMessage(final String message) {
        if (scxmlReaderLog instanceof RecordingSimpleLog) {
            Assertions.assertTrue(((RecordingSimpleLog) scxmlReaderLog).containsMessage(message));
        }
    }

    private void assertNotContainsRecordedLogMessage(final String message) {
        if (scxmlReaderLog instanceof RecordingSimpleLog) {
            Assertions.assertFalse(((RecordingSimpleLog) scxmlReaderLog).containsMessage(message));
        }
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @BeforeEach
    public void before() {
        scxmlReaderLog = LogFactory.getLog(SCXMLReader.class);
        clearRecordedLogMessages();
    }

    private void clearRecordedLogMessages() {
        if (scxmlReaderLog instanceof RecordingSimpleLog) {
            ((RecordingSimpleLog) scxmlReaderLog).clearMessages();
        }
    }

    private String serialize(final SCXML scxml) throws IOException, XMLStreamException {
        final String scxmlAsString = SCXMLWriter.write(scxml);
        Assertions.assertNotNull(scxmlAsString);
        return scxmlAsString;
    }

    @Test
    void testDataWithSrcAndExprIsRejectedInStrictConfiguration() {
        final Configuration configuration = new Configuration();
        configuration.setStrict(true);
        configuration.setSilent(true);
        Assertions.assertThrows(org.apache.commons.scxml2.model.ModelException.class,
                () -> SCXMLReader.read(getClass().getResourceAsStream("data-with-src-and-expr.xml"), configuration));
    }

    @Test
    void testDataWithSrcAndExprUsesExprInNonStrictConfiguration() throws Exception {
        final Configuration configuration = new Configuration();
        configuration.setStrict(false);
        configuration.setSilent(true);
        final SCXML scxml = SCXMLReader.read(getClass().getResourceAsStream("data-with-src-and-expr.xml"), configuration);
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(scxml.getDatamodel());
        Assertions.assertNotNull(scxml.getDatamodel().getData());
        Assertions.assertEquals(1, scxml.getDatamodel().getData().size(), "Exactly one data element parsed.");
        final Data data = scxml.getDatamodel().getData().get(0);
        Assertions.assertNotNull(data);
        Assertions.assertEquals("'an expression'", data.getExpr());
    }

    @Test
    void testExprAttributeOfDataIsParsed() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/io/data-with-expr.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(scxml.getDatamodel());
        Assertions.assertNotNull(scxml.getDatamodel().getData());
        Assertions.assertEquals(1, scxml.getDatamodel().getData().size(), "Exactly one data element parsed.");
        final Data data = scxml.getDatamodel().getData().get(0);
        Assertions.assertNotNull(data);
        Assertions.assertEquals("'an expression'", data.getExpr());
    }

    @Test
    void testSCXMLInValidTransitionTargets1() {
        // ModelUpdater will fail on invalid transition targets
        Assertions.assertThrows(org.apache.commons.scxml2.model.ModelException.class,
                () -> SCXMLTestHelper.parse(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-invalid-transition-targets-test1.xml")));
    }

    @Test
    void testSCXMLInValidTransitionTargets2() {
        // ModelUpdater will fail on invalid transition targets
        Assertions.assertThrows(org.apache.commons.scxml2.model.ModelException.class,
                () -> SCXMLTestHelper.parse(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-invalid-transition-targets-test2.xml")));
    }

    @Test
    void testSCXMLReaderCustomActionWithBodyTextSample() throws Exception {
        final List<CustomAction> cas = new ArrayList<>();
        final CustomAction ca = new CustomAction("http://my.custom-actions.domain",
            "action", MyAction.class);
        cas.add(ca);
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/io/custom-action-body-test-1.xml", cas);
        final EnterableState state = (EnterableState) scxml.getInitialTransition().getTargets().iterator().next();
        Assertions.assertEquals("actions", state.getId());
        final List<Action> actions = state.getOnEntries().get(0).getActions();
        Assertions.assertEquals(1, actions.size());
        final MyAction my = (MyAction)((CustomActionWrapper)actions.get(0)).getAction();
        Assertions.assertNotNull(my);
        Assertions.assertTrue(my.getParsedValue() != null);
    }

    @Test
    void testSCXMLReaderGroovyClosure() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/groovy/groovy-closure.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(scxml.getGlobalScript());
        final String scxmlAsString = serialize(scxml);
        Assertions.assertNotNull(scxmlAsString);
        scxml = SCXMLTestHelper.parse(new StringReader(scxmlAsString), null);
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(scxml.getGlobalScript());
    }

    @Test
    void testSCXMLReaderInitialAttr() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/io/scxml-initial-attr.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
        final Final foo = (Final) scxml.getInitialTransition().getTargets().iterator().next();
        Assertions.assertEquals("foo", foo.getId());
    }

    /**
     * Test the implementation
     */
    @Test
    void testSCXMLReaderMicrowave03Sample() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/jexl/microwave-03.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
    }

    @Test
    void testSCXMLReaderMicrowave04Sample() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/jexl/microwave-04.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
    }

    @Test
    void testSCXMLReaderPrefix01Sample() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/prefix-01.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
    }

    @Test
    void testSCXMLReaderSend01Sample() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/send-01.xml");
        final State ten = (State) scxml.getInitialTransition().getTargets().iterator().next();
        Assertions.assertEquals("ten", ten.getId());
        final List<Transition> ten_done = ten.getTransitionsList("done.state.ten");
        Assertions.assertEquals(1, ten_done.size());
        final Transition ten2twenty = ten_done.get(0);
        final List<Action> actions = ten2twenty.getActions();
        Assertions.assertEquals(1, actions.size());
        final Send send = (Send) actions.get(0);
        Assertions.assertEquals("send1", send.getId());
        /* Serialize
        scxmlAsString = serialize(scxml);
        Assertions.assertNotNull(scxmlAsString);
        String expectedFoo2Serialization =
            "<foo xmlns=\"http://my.test.namespace\" id=\"foo2\">"
            + "<prompt xmlns=\"http://foo.bar.com/vxml3\">This is just"
            + " an example.</prompt></foo>";
        Assertions.assertFalse(scxmlAsString.indexOf(expectedFoo2Serialization) == -1);
        */
    }

    @Test
    void testSCXMLReaderTransitions01Sample() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/transitions-01.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
    }

    @Test
    void testSCXMLReaderWithInvalidElements() throws Exception {
        // In the default lenient/verbose mode (strict == false && silent == false),
        // the model exception should be just logged without a model exception.
        final Configuration configuration = new Configuration();
        final SCXML scxml = SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-with-invalid-elems.xml"),
                        configuration);
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
        Final foo = (Final) scxml.getInitialTransition().getTargets().iterator().next();
        Assertions.assertEquals("foo", foo.getId());
        Datamodel dataModel = scxml.getDatamodel();
        Assertions.assertNotNull(dataModel);
        List<Data> dataList = dataModel.getData();
        Assertions.assertEquals(1, dataList.size());
        Assertions.assertEquals("time", dataList.get(0).getId());
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <datamodel>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child of <datamodel>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <onentry>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <final>");

        // In the lenient/silent mode (strict == false && silent == true),
        // no model exception is logged.
        clearRecordedLogMessages();
        final Configuration configuration2 = new Configuration();
        configuration2.setStrict(false);
        configuration2.setSilent(true);
        SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-with-invalid-elems.xml"),
                configuration2);
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(serialize(scxml));
        foo = (Final) scxml.getInitialTransition().getTargets().iterator().next();
        Assertions.assertEquals("foo", foo.getId());
        dataModel = scxml.getDatamodel();
        Assertions.assertNotNull(dataModel);
        dataList = dataModel.getData();
        Assertions.assertEquals(1, dataList.size());
        Assertions.assertEquals("time", dataList.get(0).getId());
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <onentry>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <final>");

        // In strict/verbose mode (strict == true && silent == false), it should fail to read the model and catch a model exception
        // with warning logs because of the invalid <baddata> element.
        clearRecordedLogMessages();
        final Configuration configuration3 = new Configuration();
        configuration3.setStrict(true);
        configuration3.setSilent(false);
        ModelException e = Assertions.assertThrows(
                ModelException.class,
                () ->  SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-with-invalid-elems.xml"),
                    configuration3),
                "In strict mode, it should have thrown a model exception.");
        Assertions.assertTrue(e.getMessage().contains("Ignoring unknown or invalid element <baddata>"));

        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <onentry>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <final>");

        // In strict/silent mode (strict == true && silent == true), it should fail to read the model and catch a model exception
        // without warning logs because of the invalid <baddata> element.
        clearRecordedLogMessages();
        final Configuration configuration4 = new Configuration();
        configuration4.setStrict(true);
        configuration4.setSilent(true);
        e = Assertions.assertThrows(
                ModelException.class,
                () ->  SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-with-invalid-elems.xml"),
                    configuration4),
                "In strict mode, it should have thrown a model exception.");
        Assertions.assertTrue(e.getMessage().contains("Ignoring unknown or invalid element <baddata>"));
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <onentry>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child of <final>");
    }

    @Test
    void testSCXMLValidTransitionTargets() throws Exception {
        // ModelUpdater will fail on invalid transition targets
        SCXMLTestHelper.parse(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/scxml-valid-transition-targets-test.xml"));
    }

    @Test
    void testSrcAttributeOfDataIsParsed() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/io/data-with-src.xml");
        Assertions.assertNotNull(scxml);
        Assertions.assertNotNull(scxml.getDatamodel());
        Assertions.assertNotNull(scxml.getDatamodel().getData());
        Assertions.assertEquals(1, scxml.getDatamodel().getData().size(), "Exactly one data element parsed.");
        final Data data = scxml.getDatamodel().getData().get(0);
        Assertions.assertNotNull(data);
        Assertions.assertEquals("http://www.w3.org/TR/sxcml", data.getSrc());
    }
}

