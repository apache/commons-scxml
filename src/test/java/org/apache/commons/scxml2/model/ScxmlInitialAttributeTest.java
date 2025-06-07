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
package org.apache.commons.scxml2.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringReader;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.jupiter.api.Test;

public class ScxmlInitialAttributeTest {

    private static final String SCXML_WITH_LEGAL_INITIAL =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\" initial=\"s1\">\n" +
            "  <state id=\"s1\">\n" +
            "    <transition event=\"end\" target=\"fine\" />\n" +
            "  </state>\n" +
            "  <final id=\"fine\"/>\n" +
            "</scxml>";

    private static final String SCXML_WITH_NO_INITIAL =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\">\n" +
            "  <state id=\"s1\">\n" +
            "    <transition event=\"end\" target=\"fine\" />\n" +
            "  </state>\n" +
            "  <final id=\"fine\"/>\n" +
            "</scxml>";

    private static final String SCXML_WITH_ILLEGAL_INITIAL =
            "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" version=\"1.0\" initial=\"nonexisting\">\n" +
            "  <state id=\"s1\">\n" +
            "    <transition event=\"end\" target=\"fine\" />\n" +
            "  </state>\n" +
            "  <final id=\"fine\"/>\n" +
            "</scxml>";

    @Test
    void testIllegalInitial() {
        // expected because of the non-existing initial state id
        assertThrows(
                ModelException.class,
                () -> SCXMLTestHelper.parse(new StringReader(SCXML_WITH_ILLEGAL_INITIAL), null),
                "SCXML reading should have failed due to the illegal state ID in SCXML.");
    }

    @Test
    void testInitial() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse(new StringReader(SCXML_WITH_LEGAL_INITIAL), null);
        assertEquals("s1", scxml.getInitial(), "The initial state ID reading was wrong.");
        final TransitionTarget tt = scxml.getInitialTransition().getTargets().iterator().next();
        assertEquals("s1", tt.getId(), "The initial state resolution was wrong.");
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        assertEquals(scxml.getTargets().get("s1"), exec.getStatus().getStates().iterator().next());
    }

    @Test
    void testNoInitial() throws Exception {
        final SCXML scxml = SCXMLTestHelper.parse(new StringReader(SCXML_WITH_NO_INITIAL), null);
        assertNull(scxml.getInitial());
        final TransitionTarget tt = scxml.getInitialTransition().getTargets().iterator().next();
        assertEquals("s1", tt.getId(), "The initial state resolution was wrong.");
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        assertEquals(scxml.getTargets().get("s1"), exec.getStatus().getStates().iterator().next());
    }
}
