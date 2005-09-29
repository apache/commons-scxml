/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.scxml.env.jsp.EnvJspTestSuite;
import org.apache.commons.scxml.model.ModelTestSuite;

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
        suite.addTest(SCXMLTestSuite.suite());
        suite.addTest(EnvTestSuite.suite());
        suite.addTest(EnvJspTestSuite.suite());
        suite.addTest(ModelTestSuite.suite());
        return suite;
    }
}
