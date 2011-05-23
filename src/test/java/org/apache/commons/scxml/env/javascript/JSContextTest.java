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

package org.apache.commons.scxml.env.javascript;

import junit.framework.TestCase;

import org.apache.commons.scxml.env.SimpleContext;

/**
 * JUnit 3 test case for the JSContext SCXML Context implementation for
 * the Javascript expression evaluator.
 *
 */
public class JSContextTest extends TestCase {
        // TEST CONSTANTS

        // TEST VARIABLES

        // TEST SETUP

        // CLASS METHODS

        /**
         * Standalone test runtime.
         *
         */
        public static void main(String args[]) {
            String[] testCaseName = {JSContextTest.class.getName()};

            junit.textui.TestRunner.main(testCaseName);
        }

        // CONSTRUCTORS

        /**
         * Initialises the test case with the supplied name.
         *
         */
        public JSContextTest(String testName) {
                super(testName);
        }

        // INSTANCE METHOD TESTS

        /**
         * Tests implementation of JSContext default constructor.
         *
         */
        public void testDefaultConstructor() {
            assertNotNull("Error in JSContext default constructor",new JSContext());
        }

        /**
         * Tests implementation of JSContext 'child' constructor.
         *
         */
        public void testChildConstructor() {
                assertNotNull("Error in JSContext child constructor",new JSContext(new SimpleContext()));
        }

}

