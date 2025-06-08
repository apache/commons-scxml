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

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
/**
 * Unit tests for &lt;assign&gt; element, particular the "src" attribute.
 */
class AssignTest {

    @Test
    void testAssignDeep() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/assign-test-02.xml");
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("assign3", currentStates.iterator().next().getId());
        Assertions.assertTrue(exec.getStatus().isFinal());
    }

    @Test
    void testAssignSrc() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/assign-test-01.xml");
        exec.go();
        final Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assertions.assertEquals(1, currentStates.size());
        Assertions.assertEquals("assign3", currentStates.iterator().next().getId());
        Assertions.assertTrue(exec.getStatus().isFinal());
    }
}

