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

package org.apache.commons.scxml2.env.javascript;

import org.apache.commons.scxml2.env.SimpleContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for the JSContext SCXML Context implementation for
 * the Javascript expression evaluator.
 */
public class JSContextTest {
        /**
         * Tests implementation of JSContext default constructor.
         *
         */
        @Test
        public void testDefaultConstructor() {
            Assertions.assertNotNull(new JSContext(), "Error in JSContext default constructor");
        }

        /**
         * Tests implementation of JSContext 'child' constructor.
         *
         */
        @Test
        public void testChildConstructor() {
                Assertions.assertNotNull(new JSContext(new SimpleContext()), "Error in JSContext child constructor");
        }
}

