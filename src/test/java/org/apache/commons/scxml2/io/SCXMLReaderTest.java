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
package org.apache.commons.scxml2.io;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.EventDispatcher;
import org.apache.commons.scxml2.SCInstance;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.io.SCXMLReader.Configuration;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.Data;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ExternalContent;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Send;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.Transition;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

/**
 * Unit tests {@link org.apache.commons.scxml2.io.SCXMLReader}.
 */
public class SCXMLReaderTest {

    private static String oldLogFactoryProperty;

    // Test data
    private URL transitions01, prefix01, send01,
        microwave03, microwave04, scxmlinitialattr, action01,
        scxmlWithInvalidElems, groovyClosure;
    private SCXML scxml;
    private String scxmlAsString;

    private Log scxmlReaderLog;

    @BeforeClass
    public static void beforeClass() {
        oldLogFactoryProperty = System.getProperty(LogFactory.FACTORY_PROPERTY);
        System.setProperty(LogFactory.FACTORY_PROPERTY, RecordingLogFactory.class.getName());
    }

    @AfterClass
    public static void afterClass() {
        if (oldLogFactoryProperty == null) {
            System.clearProperty(LogFactory.FACTORY_PROPERTY);
        } else {
            System.setProperty(LogFactory.FACTORY_PROPERTY, oldLogFactoryProperty);
        }
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Before
    public void before() {
        microwave03 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-03.xml");
        microwave04 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/env/jexl/microwave-04.xml");
        transitions01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/transitions-01.xml");
        send01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/send-01.xml");
        prefix01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/prefix-01.xml");
        scxmlinitialattr = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/io/scxml-initial-attr.xml");
        action01 = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/io/custom-action-body-test-1.xml");
        scxmlWithInvalidElems = this.getClass().getClassLoader().
            getResource("org/apache/commons/scxml2/io/scxml-with-invalid-elems.xml");
        groovyClosure = this.getClass().getClassLoader().
                getResource("org/apache/commons/scxml2/env/groovy/groovy-closure.xml");

        scxmlReaderLog = LogFactory.getLog(SCXMLReader.class);
        clearRecordedLogMessages();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @After
    public void after() {
        microwave03 = microwave04 = transitions01 = prefix01 = send01 = action01 =
                scxmlinitialattr = scxmlWithInvalidElems = groovyClosure = null;
        scxml = null;
        scxmlAsString = null;
    }

    /**
     * Test the implementation
     */    
    @Test
    public void testSCXMLReaderMicrowave03Sample() throws Exception {
        scxml = SCXMLTestHelper.parse(microwave03);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
    }
    
    @Test
    public void testSCXMLReaderMicrowave04Sample() throws Exception {
        scxml = SCXMLTestHelper.parse(microwave04);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
    }
    
    @Test
    public void testSCXMLReaderTransitions01Sample() throws Exception {
        scxml = SCXMLTestHelper.parse(transitions01);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
    }
    
    @Test
    public void testSCXMLReaderPrefix01Sample() throws Exception {
        scxml = SCXMLTestHelper.parse(prefix01);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
    }
    
    @Test
    public void testSCXMLReaderSend01Sample() throws Exception {
        // Digest
        scxml = SCXMLTestHelper.parse(send01);
        State ten = (State) scxml.getInitialTransition().getTargets().get(0);
        Assert.assertEquals("ten", ten.getId());
        List<Transition> ten_done = ten.getTransitionsList("ten.done");
        Assert.assertEquals(1, ten_done.size());
        Transition ten2twenty = ten_done.get(0);
        List<Action> actions = ten2twenty.getActions();
        Assert.assertEquals(1, actions.size());
        Send send = (Send) actions.get(0);
        Assert.assertEquals("send1", send.getSendid());
        /* Serialize
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
        String expectedFoo2Serialization =
            "<foo xmlns=\"http://my.test.namespace\" id=\"foo2\">"
            + "<prompt xmlns=\"http://foo.bar.com/vxml3\">This is just"
            + " an example.</prompt></foo>";
        Assert.assertFalse(scxmlAsString.indexOf(expectedFoo2Serialization) == -1);
        */
    }
    
    @Test
    public void testSCXMLReaderInitialAttr() throws Exception {
        scxml = SCXMLTestHelper.parse(scxmlinitialattr);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
        Final foo = (Final) scxml.getInitialTransition().getTargets().get(0);
        Assert.assertEquals("foo", foo.getId());
    }
    
    @Test
    public void testSCXMLReaderCustomActionWithBodyTextSample() throws Exception {
        List<CustomAction> cas = new ArrayList<CustomAction>();
        CustomAction ca = new CustomAction("http://my.custom-actions.domain",
            "action", MyAction.class);
        cas.add(ca);
        scxml = SCXMLTestHelper.parse(action01, cas);
        EnterableState state = (EnterableState) scxml.getInitialTransition().getTargets().get(0);
        Assert.assertEquals("actions", state.getId());
        List<Action> actions = state.getOnEntry().getActions();
        Assert.assertEquals(1, actions.size());
        MyAction my = (MyAction) actions.get(0);
        Assert.assertNotNull(my);
        Assert.assertTrue(my.getExternalNodes().size() > 0);
    }

    @Test
    public void testSCXMLReaderWithInvalidElements() throws Exception {
        // In the default lenient/verbose mode (strict == false && silent == false),
        // the model exception should be just logged without a model exception.
        Configuration configuration = new Configuration();
        scxml = SCXMLReader.read(scxmlWithInvalidElems, configuration);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
        Final foo = (Final) scxml.getInitialTransition().getTargets().get(0);
        Assert.assertEquals("foo", foo.getId());
        Datamodel dataModel = scxml.getDatamodel();
        Assert.assertNotNull(dataModel);
        List<Data> dataList = dataModel.getData();
        Assert.assertEquals(1, dataList.size());
        Assert.assertEquals("time", dataList.get(0).getId());
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <datamodel>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child  of <datamodel>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <onentry>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <final>");

        // In the lenient/silent mode (strict == false && silent == true),
        // no model exception is logged.
        clearRecordedLogMessages();
        scxml = null;
        configuration = new Configuration();
        configuration.setStrict(false);
        configuration.setSilent(true);
        scxml = SCXMLReader.read(scxmlWithInvalidElems, configuration);
        Assert.assertNotNull(scxml);
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
        foo = (Final) scxml.getInitialTransition().getTargets().get(0);
        Assert.assertEquals("foo", foo.getId());
        dataModel = scxml.getDatamodel();
        Assert.assertNotNull(dataModel);
        dataList = dataModel.getData();
        Assert.assertEquals(1, dataList.size());
        Assert.assertEquals("time", dataList.get(0).getId());
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child  of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <onentry>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <final>");

        // In strict/verbose mode (strict == true && silent == false), it should fail to read the model and catch a model exception
        // with warning logs because of the invalid <baddata> element.
        clearRecordedLogMessages();
        scxml = null;
        configuration = new Configuration();
        configuration.setStrict(true);
        configuration.setSilent(false);
        try {
            scxml = SCXMLReader.read(scxmlWithInvalidElems, configuration);
            Assert.fail("In strict mode, it should have thrown a model exception.");
        } catch (ModelException e) {
            Assert.assertTrue(e.getMessage().contains("Ignoring unknown or invalid element <baddata>"));
        }
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <datamodel>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child  of <datamodel>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <onentry>");
        assertContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <final>");

        // In strict/silent mode (strict == true && silent == true), it should fail to read the model and catch a model exception
        // without warning logs because of the invalid <baddata> element.
        clearRecordedLogMessages();
        scxml = null;
        configuration = new Configuration();
        configuration.setStrict(true);
        configuration.setSilent(true);
        try {
            scxml = SCXMLReader.read(scxmlWithInvalidElems, configuration);
            Assert.fail("In strict mode, it should have thrown a model exception.");
        } catch (ModelException e) {
            Assert.assertTrue(e.getMessage().contains("Ignoring unknown or invalid element <baddata>"));
        }
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <baddata> in namespace \"http://www.example.com/scxml\" as child  of <datamodel>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <trace> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <onentry>");
        assertNotContainsRecordedLogMessage("Ignoring unknown or invalid element <onbeforeexit> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <final>");
    }

    @Test
    public void testSCXMLReaderGroovyClosure() throws Exception {
        scxml = SCXMLTestHelper.parse(groovyClosure);
        Assert.assertNotNull(scxml);
        Assert.assertNotNull(scxml.getGlobalScript());
        scxmlAsString = serialize(scxml);
        Assert.assertNotNull(scxmlAsString);
        scxml = SCXMLTestHelper.parse(new StringReader(scxmlAsString), null);
        Assert.assertNotNull(scxml);
        Assert.assertNotNull(scxml.getGlobalScript());
    }

    private String serialize(final SCXML scxml) throws IOException, XMLStreamException {
        scxmlAsString = SCXMLWriter.write(scxml);
        Assert.assertNotNull(scxmlAsString);
        return scxmlAsString;
    }

    private void assertContainsRecordedLogMessage(final String message) {
        if (scxmlReaderLog instanceof RecordingSimpleLog) {
            Assert.assertTrue(((RecordingSimpleLog) scxmlReaderLog).containsMessage(
                    "Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <datamodel>"));
        }
    }

    private void assertNotContainsRecordedLogMessage(final String message) {
        if (scxmlReaderLog instanceof RecordingSimpleLog) {
            Assert.assertFalse(((RecordingSimpleLog) scxmlReaderLog).containsMessage(
                    "Ignoring unknown or invalid element <baddata> in namespace \"http://www.w3.org/2005/07/scxml\" as child  of <datamodel>"));
        }
    }

    private void clearRecordedLogMessages() {
        if (scxmlReaderLog instanceof RecordingSimpleLog) {
            ((RecordingSimpleLog) scxmlReaderLog).clearMessages();
        }
    }

    public static class MyAction extends Action implements ExternalContent {
        private static final long serialVersionUID = 1L;

        private List<Node> nodes = new ArrayList<Node>();

        @Override
        public void execute(EventDispatcher evtDispatcher,
                ErrorReporter errRep, SCInstance scInstance, Log appLog,
                Collection<TriggerEvent> derivedEvents)
        throws ModelException, SCXMLExpressionException {
            // Not relevant to test
        }

        @Override
        public List<Node> getExternalNodes() {
            return nodes;
        }

    }

    /**
     * Custom LogFactory implementation to capture log messages for logging verification.
     */
    public static class RecordingLogFactory extends LogFactoryImpl {
        @Override
        protected Log newInstance(String name) throws LogConfigurationException {
            return new RecordingSimpleLog(name);
        }
    }

    /**
     * Custom Simple Log implemenation capturing log messages
     */
    public static class RecordingSimpleLog extends SimpleLog {

        private static final long serialVersionUID = 1L;

        private List<String> messages = new LinkedList<String>();

        public RecordingSimpleLog(String name) {
            super(name);
        }

        /**
         * Clear all the recorded log messages.
         */
        public void clearMessages() {
            messages.clear();
        }

        /**
         * Return true if msg is found in any recorded log messages.
         * @param msg
         * @return
         */
        public boolean containsMessage(final String msg) {
            for (String message : messages) {
                if (message.contains(msg)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected boolean isLevelEnabled(int logLevel) {
            return (logLevel >= LOG_LEVEL_INFO);
        }

        @Override
        protected void log(int type, Object message, Throwable t) {
            super.log(type, message, t);
            messages.add(message.toString());
        }
    }
}

