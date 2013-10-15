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
package org.apache.commons.scxml2.env;

import java.net.URL;

import junit.framework.TestCase;

/**
 * Unit tests {@link org.apache.commons.scxml2.env.AbstractStateMachine}.
 */
public class AbstractStateMachineTest extends TestCase {

    /**
     * Construct a new instance of AbstractStateMachineTest with the specified name
     */
    public AbstractStateMachineTest(String name) {
        super(name);
    }

    public void testMoreThanOneScxmlDocument() throws Exception {
        URL fooScxmlDocument = getClass().getResource("foo.xml");
        URL barScxmlDocument = getClass().getResource("bar.xml");

        Foo f = new Foo(fooScxmlDocument);
        Bar b = new Bar(barScxmlDocument);

        assertTrue(f.fooCalled());
        assertTrue(b.barCalled());
    }

    private class Foo extends AbstractStateMachine {

        private boolean fooCalled;

        public Foo(final URL scxmlDocument) {
            super(scxmlDocument);
        }

        public void foo() {
            fooCalled = true;
        }

        public boolean fooCalled() {
            return fooCalled;
        }
    }

    private class Bar extends AbstractStateMachine {

        private boolean barCalled;

        public Bar(final URL scxmlDocument) {
            super(scxmlDocument);
        }

        public void bar() {
            barCalled = true;
        }

        public boolean barCalled() {
            return barCalled;
        }
    }
}
