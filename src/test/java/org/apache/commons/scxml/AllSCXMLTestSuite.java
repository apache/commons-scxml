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
package org.apache.commons.scxml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.scxml.env.EnvTestSuite;
import org.apache.commons.scxml.env.faces.EnvFacesTestSuite;
import org.apache.commons.scxml.env.javascript.EnvJavaScriptTestSuite;
import org.apache.commons.scxml.env.jexl.EnvJexlTestSuite;
import org.apache.commons.scxml.env.jsp.EnvJspTestSuite;
import org.apache.commons.scxml.env.servlet.EnvServletTestSuite;
import org.apache.commons.scxml.invoke.InvokeTestSuite;
import org.apache.commons.scxml.io.IOTestSuite;
import org.apache.commons.scxml.issues.IssuesTestSuite;
import org.apache.commons.scxml.model.ModelTestSuite;
import org.apache.commons.scxml.semantics.SemanticsTestSuite;
import org.apache.commons.scxml.test.TestingTestSuite;

/**
 * Test suite for [SCXML].
 *
 * Organization adapted from test suite for [lang].
 */
public class AllSCXMLTestSuite extends TestCase {
    
    /**
     * Construct a new instance.
     */
    public AllSCXMLTestSuite(String name) {
        super(name);
    }

    /**
     * Command-line interface.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * Get the suite of tests
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.setName("Commons-SCXML (all) Tests");
        suite.addTest(EnvFacesTestSuite.suite());
        suite.addTest(EnvJavaScriptTestSuite.suite());
        suite.addTest(EnvJexlTestSuite.suite());
        suite.addTest(EnvJspTestSuite.suite());
        suite.addTest(EnvServletTestSuite.suite());
        suite.addTest(EnvTestSuite.suite());
        suite.addTest(InvokeTestSuite.suite());
        suite.addTest(IOTestSuite.suite());
        suite.addTest(IssuesTestSuite.suite());
        suite.addTest(ModelTestSuite.suite());
        suite.addTest(SCXMLTestSuite.suite());
        suite.addTest(SemanticsTestSuite.suite());
        suite.addTest(TestingTestSuite.suite());
        return suite;
    }
}
