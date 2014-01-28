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
package org.apache.commons.scxml2.env.jexl;

import java.net.URL;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.model.SCXML;
import org.junit.Assert;
import org.junit.Test;

/**
 * Simple test for SCXML <foreach/>
 */
public class ForeachTest {

    @Test
    public void testForeach() throws Exception {
        URL document = this.getClass().getClassLoader().getResource("org/apache/commons/scxml2/env/jexl/foreach.xml");
        SCXML scxml = SCXMLTestHelper.parse(document);
        Assert.assertNotNull(scxml);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml, new JexlContext(), new JexlEvaluator());
        Assert.assertNotNull(exec);
        Assert.assertTrue(exec.getCurrentStatus().isFinal());
    }
}
