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

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.junit.Assert;
import org.junit.Test;
/**
 * Unit tests {@link org.apache.commons.scxml2.model.Assign}.
 * Unit tests {@link org.apache.commons.scxml2.model.Cancel}.
 * Unit tests {@link org.apache.commons.scxml2.model.Else}.
 * Unit tests {@link org.apache.commons.scxml2.model.ElseIf}.
 * Unit tests {@link org.apache.commons.scxml2.model.If}.
 * Unit tests {@link org.apache.commons.scxml2.model.Log}.
 * Unit tests {@link org.apache.commons.scxml2.model.Send}.
 * Unit tests {@link org.apache.commons.scxml2.model.Var}.
 */
public class ActionsTest {

    @Test
    public void testStateActions() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/actions-state-test.xml");
        exec.go();
        runTest(exec);
    }
    
    @Test
    public void testParallelActions() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/actions-parallel-test.xml");
        exec.go();
        runTest(exec);
    }
    
    @Test
    public void testInitialActions() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/model/actions-initial-test.xml");
        exec.go();
        runTest(exec);
    }

    private void runTest(SCXMLExecutor exec) throws Exception {
        Context ctx = SCXMLTestHelper.lookupContext(exec, "actionsTest");
        Assert.assertEquals(ctx.get("foo"), "foobar");
        Assert.assertEquals("Missed event transition",
            true, ctx.get("eventsent"));
    }
}

