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
package org.apache.commons.scxml2.env.groovy;

import java.net.URL;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.junit.Assert;
import org.junit.Test;

public class GroovyClosureTest {

    @Test
    public void testGroovyClosure() throws Exception {
        URL groovyClosure = this.getClass().getClassLoader().getResource("org/apache/commons/scxml2/env/groovy/groovy-closure.xml");
        SCXML scxml = SCXMLTestHelper.parse(groovyClosure);
        Assert.assertNotNull(scxml);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml, new GroovyContext(), new GroovyEvaluator(true));
        Assert.assertNotNull(exec);
        Set<TransitionTarget> currentStates = SCXMLTestHelper.fireEvent(exec, "turn_on");
        Assert.assertEquals(2, currentStates.size());
        String id = ((State)currentStates.iterator().next()).getId();
        Assert.assertTrue(id.equals("closed") || id.equals("cooking"));
    }
}
