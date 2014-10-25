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
package org.apache.commons.scxml2.io;

import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.junit.Assert;
import org.junit.Test;
/**
 * Test white box nature of <state> element "src" attribute.
 */
public class StateSrcTest {

    @Test
    public void testRecursiveSrcInclude() throws Exception {
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor("org/apache/commons/scxml2/io/src-test-1.xml");
        exec.go();
        Set<EnterableState> states = exec.getCurrentStatus().getStates();
        Assert.assertEquals(1, states.size());
        Assert.assertEquals("srctest3", states.iterator().next().getId());
        states = SCXMLTestHelper.fireEvent(exec, "src.test");
        Assert.assertEquals(1, states.size());
        Assert.assertEquals("srctest1end", states.iterator().next().getId());
        Assert.assertTrue(exec.getCurrentStatus().isFinal());
    }
    
    @Test
    public void testBadSrcInclude() throws Exception {
        try {
            SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/src-test-4.xml"));
            Assert.fail("Document with bad <state> src attribute shouldn't be parsed!");
        } catch (ModelException me) {
            Assert.assertTrue("Unexpected error message for bad <state> 'src' URI",
                me.getMessage() != null && me.getMessage().contains("Source attribute in <state src="));
        }
    }
    
    @Test
    public void testBadSrcFragmentInclude() throws Exception {
        try {
            SCXMLReader.read(SCXMLTestHelper.getResource("org/apache/commons/scxml2/io/src-test-5.xml"));
            Assert.fail("Document with bad <state> src attribute shouldn't be parsed!");
        } catch (ModelException me) {
            Assert.assertTrue("Unexpected error message for bad <state> 'src' URI fragment",
                me.getMessage() != null && me.getMessage().contains("URI Fragment in <state src="));
        }
    }
}

