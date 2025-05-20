/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.env.groovy;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GroovyContextTest {

    @Test
    public void testNew() {
        final GroovyContext ctx = new GroovyContext();
        Assertions.assertNotNull(ctx);
    }

    @Test
    public void testPrepopulated() {
        final Map<String, Object> m = new HashMap<>();
        m.put("foo", "bar");
        final GroovyContext ctx = new GroovyContext(null, m, null);
        Assertions.assertNotNull(ctx);
        Assertions.assertEquals(1, ctx.getVars().size());
        final String fooValue = (String) ctx.get("foo");
        Assertions.assertEquals("bar", fooValue);
    }

    @Test
    public void testSetVars() {
        final GroovyContext ctx = new GroovyContext();
        Assertions.assertNotNull(ctx);
        ctx.set("foo", "bar");
        Assertions.assertEquals(1, ctx.getVars().size());
        final String fooValue = (String) ctx.get("foo");
        Assertions.assertEquals("bar", fooValue);
    }

}
