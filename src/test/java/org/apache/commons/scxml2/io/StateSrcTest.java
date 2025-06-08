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

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * Test white box nature of <state> element "src" attribute.
 */
class StateSrcTest {

    @Test
    void testBadSrcFragmentInclude() {
        final ModelException me = Assertions.assertThrows(
                ModelException.class,
                () -> SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/src-test-5.xml")),
                "Document with bad <state> src attribute shouldn't be parsed!");
        Assertions.assertTrue(me.getMessage() != null && me.getMessage().contains("URI Fragment in <state src="),
                "Unexpected error message for bad <state> 'src' URI fragment");
    }

    @Test
    void testBadSrcInclude() {
        final ModelException me = Assertions.assertThrows(
                ModelException.class,
                () -> SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/src-test-4.xml")),
                "Document with bad <state> src attribute shouldn't be parsed!");
        Assertions.assertTrue(me.getMessage() != null && me.getMessage().contains("Source attribute in <state src="),
                "Unexpected error message for bad <state> 'src' URI");
    }

    @Test
    void testRecursiveSrcInclude() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/io/src-test-1.xml");
        exec.go();
        Set<EnterableState> states = exec.getStatus().getStates();
        Assertions.assertEquals(1, states.size());
        Assertions.assertEquals("srctest3", states.iterator().next().getId());
        states = SCXMLTestHelper.fireEvent(exec, "src.test");
        Assertions.assertEquals(1, states.size());
        Assertions.assertEquals("srctest1end", states.iterator().next().getId());
        Assertions.assertTrue(exec.getStatus().isFinal());
    }
}

