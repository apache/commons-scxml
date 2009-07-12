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
package org.apache.commons.scxml.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Test suite for SCXML model package.
 *
 */
public class ModelTestSuite extends TestCase {
    
    /**
     * Construct a new instance.
     */
    public ModelTestSuite(String name) {
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
        suite.setName("Commons-SCXML Model Tests");
        suite.addTest(ActionsTest.suite());
        suite.addTest(ActionTest.suite());
        suite.addTest(AssignTest.suite());
        suite.addTest(CustomActionTest.suite());
        suite.addTest(DatamodelTest.suite());
        suite.addTest(HistoryTest.suite());
        suite.addTest(ParallelTest.suite());
        suite.addTest(PathTest.suite());
        suite.addTest(ScriptTest.suite());
        suite.addTest(StateTest.suite());
        suite.addTest(StatelessModelTest.suite());
        suite.addTest(TransitionTest.suite());
        return suite;
    }
}
