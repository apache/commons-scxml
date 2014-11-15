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

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.Assert;
import org.junit.Test;

public class ParallelTest {

    @Test
    public void testParallel01() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/parallel-01.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerState(exec, "foo", "end");
    }
    
    @Test
    public void testParallel02() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/parallel-02.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerStates(exec, "dummy.event", new String[] { "state01", "state02" });
        SCXMLTestHelper.assertPostTriggerState(exec, "event1", "state1");
    }
    
    @Test
    public void testParallel03() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/parallel-03.xml");
        exec.go();
        SCXMLTestHelper.assertPostTriggerStates(exec, "dummy.event", new String[] { "para11", "para21" });
        Object count = exec.getEvaluator().eval(exec.getRootContext(),"Data('string(root/root/count)')");
        Assert.assertEquals("5.0", count.toString());
        SCXMLTestHelper.assertPostTriggerStates(exec, "foo", new String[] { "para12", "para21" });
        count = exec.getEvaluator().eval(exec.getRootContext(),"Data('string(root/root/count)')");
        Assert.assertEquals("7.0", count.toString());
        SCXMLTestHelper.assertPostTriggerState(exec, "bar", "end");
        count = exec.getEvaluator().eval(exec.getRootContext(),"Data('string(root/root/count)')");
        Assert.assertEquals("14.0", count.toString());
    }
}
