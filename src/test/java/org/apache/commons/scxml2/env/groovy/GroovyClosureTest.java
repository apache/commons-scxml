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
package org.apache.commons.scxml2.env.groovy;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.EnterableState;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroovyClosureTest {

    @Test
    void testGroovyClosure() throws Exception {
        final URL groovyClosure = SCXMLTestHelper.getResource("org/apache/commons/scxml2/env/groovy/groovy-closure.xml");
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor(groovyClosure, new GroovyEvaluator(true));
        exec.go();
        final Set<EnterableState> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assertions.assertEquals(2, currentStates.size());
        final String id = currentStates.iterator().next().getId();
        Assertions.assertTrue(id.equals("closed") || id.equals("cooking"));
    }
}
