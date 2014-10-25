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

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.EnterableState;
import org.junit.Assert;
import org.junit.Test;

public class StaticMethodTest {

    @Test
    public void testGroovyStaticMethodInvocation() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/env/groovy/static-method.xml");
        exec.getRootContext().set("System", System.class);
        exec.go();
        Set<EnterableState> currentStates = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("static", currentStates.iterator().next().getId());
    }

    @Test
    public void mytest() throws Exception {
        Object o1, o2 = new Object();
        o1 = new Object();

        o2 = null;
        o1 = null;
        if (val(o1,o2)) {
            System.out.println("hello");
        }
        else {
            System.out.println("boo");
        }
    }

    public boolean val(Object o1, Object o2) {
        return (o1 == o2);
    }
}
