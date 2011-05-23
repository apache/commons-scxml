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
package org.apache.commons.scxml.env.rhino;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Tests for class {@link RhinoContext}.
 */
public class RhinoContextTest extends TestCase {

    /**
     * Constructs a test case with the given name.
     *
     * @param name
     *            The name of the test case.
     */
    public RhinoContextTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @throws Exception
     */
    public void testSetStringValue() throws Exception {
        Context context = new RhinoContext();
        context.set("myVar", "stringValue");
        Object returnObject = context.get("myVar");
        Assert.assertTrue(returnObject instanceof String);
        Assert.assertEquals("stringValue", returnObject);
    }

    /**
     * @throws Exception
     */
    public void testSetIntValue() throws Exception {
        Context context = new RhinoContext();
        context.set("myVar", new Integer(27));
        Object returnObject = context.get("myVar");
        Assert.assertTrue(returnObject instanceof Integer);
        Assert.assertEquals(27, ((Integer)returnObject).intValue());
    }

    /**
     * @throws Exception
     */
    public void testSetXMLValue() throws Exception {
        Context context = new RhinoContext();
        String xmlString = "<foo><bar>bar</bar></foo>";
        context.set("root", xmlString);
        Object returnObject = context.get("root");
        Assert.assertTrue(returnObject instanceof Scriptable);
    }

    /**
     * @throws Exception
     */
    public void testSetMapValueWithStringEntry() throws Exception {
        Context context = new RhinoContext();
        Map<String, String> map = new HashMap<String, String>();
        map.put("myVar", "stringValue");
        context.set("theMap", map);
        Object returnObject = context.get("theMap");
        Assert.assertTrue(returnObject instanceof Scriptable);
        Assert.assertEquals("stringValue", ((Scriptable)returnObject).get("myVar", null));
    }

    /**
     * @throws Exception
     */
    public void testSetMapValueWithMapEntry() throws Exception {
        Context context = new RhinoContext();
        Map<String, Map<String, String>> map1 = new HashMap<String, Map<String, String>>();
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("myVar", "stringValue");
        map1.put("map2", map2);
        context.set("map1", map1);
        Object returnObject = context.get("map1");
        Assert.assertTrue(returnObject instanceof Scriptable);
        returnObject = ((Scriptable)returnObject).get("map2", null);
        Assert.assertTrue(returnObject instanceof Scriptable);
        Assert.assertEquals("stringValue", ((Scriptable)returnObject).get("myVar", null));
    }

    /**
     * @throws Exception
     */
    public void testSetMapValueWithXMLStringEntry() throws Exception {
        RhinoContext context = new RhinoContext();
        Map<String, String> map = new HashMap<String, String>();
        map.put("myVar", "<a xmlns:e='http://foo.bar.de'><b><e:c>foo</e:c></b></a>");
        Map<String, String> namespaceMap = new HashMap<String, String>();
        namespaceMap.put("e", "http://foo.bar.de");
        context.set("_ALL_NAMESPACES", namespaceMap);
        context.set("theMap", map);
        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        Script s = cx.compileString("theMap.myVar..e::c.toString()", "", 1, null);
        Object returnObject = s.exec(cx, context.getScope());
        org.mozilla.javascript.Context.exit();
        Assert.assertTrue(returnObject instanceof String);
        Assert.assertEquals("foo", returnObject.toString());
    }

    /**
     * @throws Exception
     */
    public void testCompileStringWithNodeValue() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element fooElement = doc.createElement("foo");
        Element barElement = doc.createElement("bar");
        fooElement.appendChild(barElement);
        barElement.appendChild(doc.createTextNode("blub"));

        RhinoContext context = new RhinoContext();
        context.set("foobar", fooElement);

        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        Script s = cx.compileString("var test = foobar.bar.toString()", "", 1, null);
        s.exec(cx, context.getScope());
        org.mozilla.javascript.Context.exit();

        Object returnValue = context.get("test");
        Assert.assertTrue( returnValue instanceof String);
        Assert.assertEquals("blub", returnValue);
    }

    public void testSetLocalWithMap() throws Exception {
        Evaluator evaluator = new RhinoEvaluator();
        Context ctx = evaluator.newContext(null);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("name", "theEventName");
        m.put("data", new Integer(4711));
        ctx.setLocal("_event", m);
        Object eventname = evaluator.eval(ctx, "_event.name");
        assertTrue(eventname instanceof String);
        assertEquals("theEventName", eventname);
        Object evateData = evaluator.eval(ctx, "_event.data");
        assertTrue(evateData instanceof Integer);
        assertEquals(new Integer(4711), evateData);
    }

    public void testSetLocalWithMapInMap() throws Exception {
        Evaluator evaluator = new RhinoEvaluator();
        Context ctx = evaluator.newContext(null);
        Map<String, Object> m1 = new HashMap<String, Object>();
        m1.put("name", "theEventName");
        Map<String, String> m2 = new HashMap<String, String>();
        m2.put("foo", "bar");
        m1.put("data", m2);
        ctx.setLocal("_event", m1);
        Object eventname = evaluator.eval(ctx, "_event.name");
        assertTrue(eventname instanceof String);
        assertEquals("theEventName", eventname);
        Object foo = evaluator.eval(ctx, "_event.data.foo");
        assertEquals("bar", foo);
    }

    public void testSetLocalWithNullValue() throws Exception {
        Context context = new RhinoContext();
        context.set("myVar", "stringValue");
        Object returnObject = context.get("myVar");
        Assert.assertTrue(returnObject instanceof String);
        Assert.assertEquals("stringValue", returnObject);
        context.set("myVar", null);
        returnObject = context.get("myVar");
        Assert.assertNull(returnObject);
    }

}

