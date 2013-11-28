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
package org.apache.commons.scxml2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.junit.Test;

public class ScxmlInitialAttributeTest {

    private static final String SCXML_WITH_LEGAL_INITIAL =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" initial=\"s1\">\n" +
            "  <state id=\"s1\">\n" +
            "    <transition event=\"end\" target=\"fine\" />\n" +
            "  </state>\n" +
            "  <final id=\"fine\"/>\n" +
            "</scxml>";

    private static final String SCXML_WITH_NO_INITIAL =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\">\n" +
            "  <state id=\"s1\">\n" +
            "    <transition event=\"end\" target=\"fine\" />\n" +
            "  </state>\n" +
            "  <final id=\"fine\"/>\n" +
            "</scxml>";

    private static final String SCXML_WITH_ILLEGAL_INITIAL =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" initial=\"nonexisting\">\n" +
            "  <state id=\"s1\">\n" +
            "    <transition event=\"end\" target=\"fine\" />\n" +
            "  </state>\n" +
            "  <final id=\"fine\"/>\n" +
            "</scxml>";

    @Test
    public void testInitial() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(new StringReader(SCXML_WITH_LEGAL_INITIAL), null);
        assertEquals("The initial state ID reading was wrong.", "s1", scxml.getInitial());
        assertNotNull(scxml.getInitialTarget());
        assertEquals("The initial state resolution was wrong.", "s1", scxml.getInitialTarget().getId());
        SCXMLExecutor exec = executeSCXML(scxml);
        assertEquals(scxml.getTargets().get("s1"), exec.getCurrentStatus().getStates().iterator().next());
    }

    @Test
    public void testNoInitial() throws Exception {
        SCXML scxml = SCXMLTestHelper.parse(new StringReader(SCXML_WITH_NO_INITIAL), null);
        assertNull(scxml.getInitial());
        assertNotNull("The initial state ID reading was wrong.", scxml.getInitialTarget());
        assertEquals("The initial state resolution was wrong.", "s1", scxml.getInitialTarget().getId());
        SCXMLExecutor exec = executeSCXML(scxml);
        assertEquals(scxml.getTargets().get("s1"), exec.getCurrentStatus().getStates().iterator().next());
    }

    @Test
    public void testIllegalInitial() throws Exception {
        try {
            SCXMLTestHelper.parse(new StringReader(SCXML_WITH_ILLEGAL_INITIAL), null);
            fail("SCXML reading should have failed due to the illegal state ID in SCXML.");
        } catch (ModelException e) {
            // expected because of the non-existing initial state id
        }
    }

    private SCXMLExecutor executeSCXML(SCXML scxml) throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml, new JexlContext(), new JexlEvaluator());
        exec.go();
        return exec;
    }

}
