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

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParallelTest {

    @Test
    void testParallel01() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/parallel-01.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "end");
    }

    @Test
    void testParallel02() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/parallel-02.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerStates(exec, "dummy.event", new String[] { "state01", "state02" });
        SCXMLTestHelper.assertPostTriggerState(exec, "event1", "state1");
    }

    @Test
    void testParallel03() throws Exception {
        final SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/parallel-03.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerStates(exec, "dummy.event", new String[] { "para11", "para21" });
        Object count = exec.getEvaluator().eval(exec.getGlobalContext(),"root.root.count");
        Assertions.assertEquals(5, count);
        SCXMLTestHelper.assertPostTriggerStates(exec, "foo", new String[]{"para12", "para21"});
        count = exec.getEvaluator().eval(exec.getGlobalContext(),"root.root.count");
        Assertions.assertEquals(7, count);
        SCXMLTestHelper.assertPostTriggerState(exec, "bar", "end");
        count = exec.getEvaluator().eval(exec.getGlobalContext(),"root.root.count");
        Assertions.assertEquals(14, count);
    }
}
