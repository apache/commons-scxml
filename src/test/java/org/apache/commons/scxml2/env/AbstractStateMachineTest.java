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

import org.apache.commons.scxml2.model.ModelException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests {@link org.apache.commons.scxml2.env.AbstractStateMachine}.
 */
public class AbstractStateMachineTest {

    private class Bar extends AbstractStateMachine {

        private boolean barCalled;

        Bar(final URL scxmlDocument) throws ModelException {
            super(scxmlDocument);
        }

        public void bar() {
            barCalled = true;
        }

        boolean barCalled() {
            return barCalled;
        }
    }

    private class Foo extends AbstractStateMachine {

        private boolean fooCalled;

        Foo(final URL scxmlDocument) throws ModelException {
            super(scxmlDocument);
        }

        public void foo() {
            fooCalled = true;
        }

        boolean fooCalled() {
            return fooCalled;
        }
    }

    @Test
    public void testMoreThanOneScxmlDocument() throws Exception {
        final URL fooScxmlDocument = getClass().getResource("foo.xml");
        final URL barScxmlDocument = getClass().getResource("bar.xml");

        final Foo f = new Foo(fooScxmlDocument);
        final Bar b = new Bar(barScxmlDocument);

        Assertions.assertTrue(f.fooCalled());
        Assertions.assertTrue(b.barCalled());
    }
}
