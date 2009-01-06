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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.SCXMLExpressionException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit 3 test case for the JSBinding implementation that imports
 * SCXML context variables into a JavaScript bindings. Includes tests
 * for:
 * <ul>
 * <li> constructor
 * </ul>
 */
public class JSBindingsTest extends TestCase {
    // TEST CONSTANTS

    private static final Map.Entry<String,Object> KOALA   = new AbstractMap.SimpleEntry<String,Object>("bear","koala");
    private static final Map.Entry<String,Object> GRIZZLY = new AbstractMap.SimpleEntry<String,Object>("bear","grizzly");
    private static final Map.Entry<String,Object> FELIX   = new AbstractMap.SimpleEntry<String,Object>("cat", "felix");
    private static final Map.Entry<String,Object> ROVER   = new AbstractMap.SimpleEntry<String,Object>("dog", "rover");

    // TEST VARIABLES

    // TEST SETUP

    /**
     * Creates and initializes an SCXML data model in the context.
     */
    @Override
    protected void setUp() throws Exception {
    }

    // CLASS METHODS

    /**
     * Stand-alone test runtime.
     */
    public static void main(String args[]) {
        String[] testCaseName = {JSBindingsTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    /**
     * Returns a JUnit test suite containing the JSBindingsTest class only.
     */
    public static Test suite() {
        return new TestSuite(JSBindingsTest.class);
    }

    // CONSTRUCTORS

    /**
     * Initializes the test case with a name.
     */
    public JSBindingsTest(String testName) {
        super(testName);
    }

    // INSTANCE METHOD TESTS

    /**
     * Tests implementation of JSBindings constructor.
     */
    public void testConstructor() throws SCXMLExpressionException {
        assertNotNull(new JSBindings(new JSContext(),new SimpleBindings()));
    }

    /**
     * Test implementation of JSBindings constructor with invalid SCXML context.
     */
    public void testInvalidContextConstructor() throws SCXMLExpressionException {
        try {
             assertNotNull(new JSBindings(null,new SimpleBindings()));
             fail("JSBindings constructor accepted invalid SCXML context");

        } catch (IllegalArgumentException x) {
             // expected, ignore
        }
    }

    /**
     * Test implementation of JSBindings constructor with invalid Javascript bindings.
     */
    public void testInvalidBindingsConstructor() throws SCXMLExpressionException {
        try {
             assertNotNull(new JSBindings(new JSContext(),null));
             fail("JSBindings constructor accepted invalid Javascript bindings");

        } catch (IllegalArgumentException x) {
             // expected, ignore
        }
    }

    /**
     * Tests the <code>containsKey</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testContainsKey() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertFalse("Invalid SCXML context",      context.has         ("bear"));
        assertFalse("Invalid Javascript bindings",bindings.containsKey("bear"));
        assertFalse("Invalid JSbindings",         jsx.containsKey     ("bear"));

        context.set("bear","koala");
        assertTrue ("Invalid SCXML context",      context.has         ("bear"));
        assertFalse("Invalid Javascript bindings",bindings.containsKey("bear"));
        assertTrue ("Invalid JSbindings",         jsx.containsKey     ("bear"));

        context.reset();
        bindings.put ("bear","grizzly");
        assertFalse  ("Invalid SCXML context",      context.has         ("bear"));
        assertTrue   ("Invalid Javascript bindings",bindings.containsKey("bear"));
        assertTrue   ("Invalid JSbindings",         jsx.containsKey     ("bear"));

        context.set ("bear","koala");
        bindings.put("bear","grizzly");
        assertTrue  ("Invalid SCXML context",      context.has         ("bear"));
        assertTrue  ("Invalid Javascript bindings",bindings.containsKey("bear"));
        assertTrue  ("Invalid JSbindings",         jsx.containsKey     ("bear"));
    }

    /**
     * Tests the <code>keySet</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testKeySet() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertFalse ("Invalid SCXML context",      context.has          ("bear"));
        assertFalse ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        assertFalse ("Invalid JSbindings",         jsx.keySet().contains("bear"));

        context.set   ("bear","koala");
        bindings.clear();
        assertTrue    ("Invalid SCXML context",      context.has          ("bear"));
        assertFalse   ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        assertTrue    ("Invalid JSbindings",         jsx.keySet().contains("bear"));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        assertFalse   ("Invalid SCXML context",      context.has          ("bear"));
        assertTrue    ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        assertTrue    ("Invalid JSbindings",         jsx.keySet().contains("bear"));

        context.reset ();
        bindings.clear();
        context.set  ("cat","felix");
        bindings.put ("dog","rover");
        assertFalse  ("Invalid SCXML context",      context.has          ("bear"));
        assertFalse  ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        assertTrue   ("Invalid SCXML context",      context.has          ("cat"));
        assertTrue   ("Invalid Javascript bindings",bindings.containsKey ("dog"));
        assertTrue   ("Invalid JSbindings",         jsx.keySet().contains("cat"));
        assertTrue   ("Invalid JSbindings",         jsx.keySet().contains("dog"));
    }

    /**
     * Tests the <code>size</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
   public void testSize() {
       Context    context  = new JSContext     ();
       Bindings   bindings = new SimpleBindings();
       JSBindings jsx      = new JSBindings    (context,bindings);

       assertFalse ("Invalid SCXML context",      context.has          ("bear"));
       assertFalse ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       assertEquals("Invalid JSbindings",0,jsx.size());

       context.set   ("bear","koala");
       bindings.clear();
       assertTrue    ("Invalid SCXML context",      context.has          ("bear"));
       assertFalse   ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       assertEquals  ("Invalid JSbindings",1,jsx.size());

       context.reset ();
       bindings.clear();
       bindings.put  ("bear","grizzly");
       assertFalse   ("Invalid SCXML context",      context.has          ("bear"));
       assertTrue    ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       assertEquals  ("Invalid JSbindings",1,jsx.size());

       context.reset ();
       bindings.clear();
       context.set   ("bear","koala");
       bindings.put  ("bear","grizzly");
       assertTrue    ("Invalid SCXML context",      context.has          ("bear"));
       assertTrue    ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       assertEquals  ("Invalid JSbindings",1,jsx.size());

       context.reset ();
       bindings.clear();
       context.set  ("cat","felix");
       bindings.put ("dog","rover");
       assertFalse  ("Invalid SCXML context",      context.has          ("bear"));
       assertFalse  ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       assertTrue   ("Invalid SCXML context",      context.has          ("cat"));
       assertTrue   ("Invalid Javascript bindings",bindings.containsKey ("dog"));
       assertEquals ("Invalid JSbindings",2,jsx.size());
    }

    /**
     * Tests the <code>containsValue</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testContainsValue() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertFalse("Invalid SCXML context",      context.getVars().containsValue("koala"));
        assertFalse("Invalid Javascript bindings",bindings.containsValue("koala"));
        assertFalse("Invalid JSbindings",         jsx.containsValue     ("koala"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        assertTrue    ("Invalid SCXML context",      context.getVars().containsValue("koala"));
        assertFalse   ("Invalid Javascript bindings",bindings.containsValue("koala"));
        assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("koala"));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        assertFalse   ("Invalid SCXML context",      context.getVars().containsValue("grizzly"));
        assertTrue    ("Invalid Javascript bindings",bindings.containsValue("grizzly"));
        assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("grizzly"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        assertTrue    ("Invalid SCXML context",      context.getVars().containsValue("koala"));
        assertTrue    ("Invalid Javascript bindings",bindings.containsValue("grizzly"));
        assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("koala"));
        assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("grizzly"));
    }

    /**
     * Tests the <code>entrySet</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testEntrySet() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertEquals("Invalid SCXML context",      0,context.getVars().entrySet().size());
        assertEquals("Invalid Javascript bindings",0,bindings.entrySet().size());
        assertEquals("Invalid JSbindings",         0,jsx.entrySet().size());

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        assertEquals  ("Invalid SCXML context",      1,context.getVars().entrySet().size());
        assertTrue    ("Invalid SCXML context",      context.getVars().entrySet().contains(KOALA));
        assertEquals  ("Invalid Javascript bindings",0,bindings.entrySet().size());
        assertFalse   ("Invalid Javascript bindings",bindings.entrySet().contains(KOALA));
        assertEquals  ("Invalid JSBindings",         1,jsx.entrySet().size());
        assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(KOALA));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        assertEquals  ("Invalid SCXML context",      0,context.getVars().entrySet().size());
        assertFalse   ("Invalid SCXML context",      context.getVars().entrySet().contains(GRIZZLY));
        assertEquals  ("Invalid Javascript bindings",1,bindings.entrySet().size());
        assertTrue    ("Invalid Javascript bindings",bindings.entrySet().contains(GRIZZLY));
        assertEquals  ("Invalid JSBindings",         1,jsx.entrySet().size());
        assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(GRIZZLY));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        assertEquals  ("Invalid SCXML context",      1,context.getVars().entrySet().size());
        assertTrue    ("Invalid SCXML context",      context.getVars().entrySet().contains(KOALA));
        assertEquals  ("Invalid Javascript bindings",1,bindings.entrySet().size());
        assertTrue    ("Invalid Javascript bindings",bindings.entrySet().contains(GRIZZLY));
        assertEquals  ("Invalid JSBindings",         1,jsx.entrySet().size());
        assertFalse   ("Invalid JSbindings",         jsx.entrySet().contains(KOALA));
        assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(GRIZZLY));

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        assertEquals  ("Invalid SCXML context",      1,context.getVars().entrySet().size());
        assertTrue    ("Invalid SCXML context",      context.getVars().entrySet().contains(FELIX));
        assertEquals  ("Invalid Javascript bindings",1,bindings.entrySet().size());
        assertTrue    ("Invalid Javascript bindings",bindings.entrySet().contains(ROVER));
        assertEquals  ("Invalid JSBindings",         2,jsx.entrySet().size());
        assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(FELIX));
        assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(ROVER));
    }

    /**
     * Tests the <code>values</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testValues() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertEquals("Invalid SCXML context",      0,context.getVars().values().size());
        assertEquals("Invalid Javascript bindings",0,bindings.values().size());
        assertEquals("Invalid JSbindings",         0,jsx.values().size());

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        assertEquals  ("Invalid SCXML context",      1,context.getVars().values().size());
        assertTrue    ("Invalid SCXML context",      context.getVars().values().contains(KOALA.getValue()));
        assertEquals  ("Invalid Javascript bindings",0,bindings.values().size());
        assertFalse   ("Invalid Javascript bindings",bindings.values().contains(KOALA.getValue()));
        assertEquals  ("Invalid JSBindings",         1,jsx.values().size());
        assertTrue    ("Invalid JSbindings",         jsx.values().contains(KOALA.getValue()));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        assertEquals  ("Invalid SCXML context",      0,context.getVars().values().size());
        assertFalse   ("Invalid SCXML context",      context.getVars().values().contains(GRIZZLY.getValue()));
        assertEquals  ("Invalid Javascript bindings",1,bindings.values().size());
        assertTrue    ("Invalid Javascript bindings",bindings.values().contains(GRIZZLY.getValue()));
        assertEquals  ("Invalid JSBindings",         1,jsx.values().size());
        assertTrue    ("Invalid JSbindings",         jsx.values().contains(GRIZZLY.getValue()));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        assertEquals  ("Invalid SCXML context",      1,context.getVars().values().size());
        assertTrue    ("Invalid SCXML context",      context.getVars().values().contains(KOALA.getValue()));
        assertEquals  ("Invalid Javascript bindings",1,bindings.values().size());
        assertTrue    ("Invalid Javascript bindings",bindings.values().contains(GRIZZLY.getValue()));
        assertEquals  ("Invalid JSBindings",         1,jsx.values().size());
        assertFalse   ("Invalid JSbindings",         jsx.values().contains(KOALA.getValue()));
        assertTrue    ("Invalid JSbindings",         jsx.values().contains(GRIZZLY.getValue()));

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        assertEquals  ("Invalid SCXML context",      1,context.getVars().values().size());
        assertTrue    ("Invalid SCXML context",      context.getVars().values().contains(FELIX.getValue()));
        assertEquals  ("Invalid Javascript bindings",1,bindings.values().size());
        assertTrue    ("Invalid Javascript bindings",bindings.values().contains(ROVER.getValue()));
        assertEquals  ("Invalid JSBindings",         2,jsx.values().size());
        assertTrue    ("Invalid JSbindings",         jsx.values().contains(FELIX.getValue()));
        assertTrue    ("Invalid JSbindings",         jsx.values().contains(ROVER.getValue()));
    }

    /**
     * Tests the <code>isEmpty</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testIsEmpty() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertTrue("Invalid SCXML context",      context.getVars().isEmpty());
        assertTrue("Invalid Javascript bindings",bindings.isEmpty());
        assertTrue("Invalid JSbindings",         jsx.isEmpty());

        context.set   ("bear","koala");
        bindings.clear();
        assertFalse   ("Invalid SCXML context",      context.getVars().isEmpty());
        assertTrue    ("Invalid Javascript bindings",bindings.isEmpty());
        assertFalse   ("Invalid JSbindings",         jsx.isEmpty());

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        assertTrue    ("Invalid SCXML context",      context.getVars().isEmpty());
        assertFalse   ("Invalid Javascript bindings",bindings.isEmpty());
        assertFalse   ("Invalid JSbindings",         jsx.isEmpty());

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        assertFalse   ("Invalid SCXML context",      context.getVars().isEmpty());
        assertFalse   ("Invalid Javascript bindings",bindings.isEmpty());
        assertFalse   ("Invalid JSbindings",         jsx.isEmpty());

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        assertFalse   ("Invalid SCXML context",      context.getVars().isEmpty());
        assertFalse   ("Invalid Javascript bindings",bindings.isEmpty());
        assertFalse   ("Invalid JSbindings",         jsx.isEmpty());
    }

    /**
     * Tests the <code>get</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testGet() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertNull("Invalid SCXML context",      context.get ("bear"));
        assertNull("Invalid Javascript bindings",bindings.get("bear"));
        assertNull("Invalid JSbindings",         jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        assertEquals  ("Invalid SCXML context","koala",context.get ("bear"));
        assertNull    ("Invalid Javascript bindings",  bindings.get("bear"));
        assertNotNull ("Invalid JSbindings",           jsx.get     ("bear"));
        assertEquals  ("Invalid JSbindings","koala",   jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        assertNull    ("Invalid SCXML context",                context.get ("bear"));
        assertNotNull ("Invalid Javascript bindings",          bindings.get("bear"));
        assertEquals  ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertNotNull ("Invalid JSbindings",                   jsx.get     ("bear"));
        assertEquals  ("Invalid JSbindings","grizzly",         jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        assertEquals  ("Invalid SCXML context","koala",context.get ("bear"));
        assertNotNull ("Invalid Javascript bindings",          bindings.get("bear"));
        assertEquals  ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertNotNull ("Invalid JSbindings",                   jsx.get     ("bear"));
        assertEquals  ("Invalid JSbindings","grizzly",         jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        assertNotNull ("Invalid SCXML context",              context.get ("cat"));
        assertEquals  ("Invalid SCXML context","felix",      context.get ("cat"));
        assertNotNull ("Invalid Javascript bindings",        bindings.get("dog"));
        assertEquals  ("Invalid Javascript bindings","rover",bindings.get("dog"));
        assertNotNull ("Invalid JSbindings",                 jsx.get     ("cat"));
        assertEquals  ("Invalid JSbindings","felix",         jsx.get     ("cat"));
        assertNotNull ("Invalid JSbindings",                 jsx.get     ("dog"));
        assertEquals  ("Invalid JSbindings","rover",         jsx.get     ("dog"));
    }

    /**
     * Tests the <code>put</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testPut() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        assertNull("Invalid SCXML context",      context.get ("bear"));
        assertNull("Invalid Javascript bindings",bindings.get("bear"));
        assertNull("Invalid JSbindings",         jsx.get     ("bear"));

        jsx.put       ("bear","koala");
        assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        assertEquals  ("Invalid SCXML context","koala",context.get("bear"));
        assertNotNull ("Invalid JSbindings",           jsx.get ("bear"));
        assertNull    ("Invalid Javascript bindings",  bindings.get("bear"));
    }

    /**
     * Tests the <code>putAll</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testPutAll() {
        Context            context  = new JSContext     ();
        Bindings           bindings = new SimpleBindings();
        JSBindings         jsx      = new JSBindings    (context,bindings);
        Map<String,Object> vars     = new HashMap<String, Object>();

        vars.put("bear","grizzly");
        vars.put("cat","felix");
        vars.put("dog","rover");

        assertNull("Invalid SCXML context",      context.get ("bear"));
        assertNull("Invalid SCXML context",      context.get ("cat"));
        assertNull("Invalid SCXML context",      context.get ("dog"));

        assertNull("Invalid Javascript bindings",bindings.get("bear"));
        assertNull("Invalid Javascript bindings",bindings.get("cat"));
        assertNull("Invalid Javascript bindings",bindings.get("dog"));

        assertNull("Invalid JSbindings",         jsx.get     ("bear"));
        assertNull("Invalid JSbindings",         jsx.get     ("cat"));
        assertNull("Invalid JSbindings",         jsx.get     ("dog"));

        context.set("bear","koala");
        jsx.putAll (vars);

        assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        assertNull    ("Invalid SCXML context",        context.get ("cat"));
        assertNull    ("Invalid SCXML context",        context.get ("dog"));
        assertEquals  ("Invalid SCXML context","koala",context.get ("bear"));
        assertEquals  ("Invalid SCXML context",1,      context.getVars().size());

        assertNotNull ("Invalid Javascript bindings",          bindings.get("bear"));
        assertNotNull ("Invalid Javascript bindings",          bindings.get("cat"));
        assertNotNull ("Invalid Javascript bindings",          bindings.get("dog"));
        assertEquals  ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertEquals  ("Invalid Javascript bindings","felix",  bindings.get("cat"));
        assertEquals  ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        assertEquals  ("Invalid Javascript bindings",3,        bindings.size());
    }

    /**
     * Tests the <code>remove</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testRemove() {
        Context            context  = new JSContext     ();
        Bindings           bindings = new SimpleBindings();
        JSBindings         jsx      = new JSBindings    (context,bindings);

        context.set ("bear","koala");
        bindings.put("bear","grizzly");
        bindings.put("cat", "felix");
        bindings.put("dog", "rover");

        assertNotNull("Invalid SCXML context",        context.get("bear"));
        assertEquals ("Invalid SCXML context","koala",context.get("bear"));
        assertEquals ("Invalid SCXML context",1,      context.getVars().size());

        assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        assertNotNull("Invalid Javascript bindings",          bindings.get("cat"));
        assertNotNull("Invalid Javascript bindings",          bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertEquals ("Invalid Javascript bindings","felix",  bindings.get("cat"));
        assertEquals ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings",3,        bindings.size());

        jsx.remove("cat");

        assertNotNull("Invalid SCXML context",                context.get("bear"));
        assertEquals ("Invalid SCXML context","koala",        context.get("bear"));
        assertEquals ("Invalid SCXML context",1,              context.getVars().size());
        assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        assertNull   ("Invalid Javascript bindings",          bindings.get("cat"));
        assertNotNull("Invalid Javascript bindings",          bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertEquals ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings",2,        bindings.size());

        jsx.remove("dog");

        assertNotNull("Invalid SCXML context",               context.get("bear"));
        assertEquals ("Invalid SCXML context","koala",        context.get("bear"));
        assertEquals ("Invalid SCXML context",1,              context.getVars().size());
        assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        assertNull   ("Invalid Javascript bindings",          bindings.get("cat"));
        assertNull   ("Invalid Javascript bindings",          bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertEquals ("Invalid Javascript bindings",1,        bindings.size());

        jsx.remove("bear");

        assertNotNull("Invalid SCXML context",       context.get("bear"));
        assertEquals("Invalid SCXML context","koala",context.get("bear"));
        assertEquals("Invalid SCXML context",1,      context.getVars().size());
        assertNull  ("Invalid Javascript bindings",  bindings.get("bear"));
        assertNull  ("Invalid Javascript bindings",  bindings.get("cat"));
        assertNull  ("Invalid Javascript bindings",  bindings.get("dog"));
        assertEquals("Invalid Javascript bindings",0,bindings.size());

        jsx.remove("bear");

        assertNull  ("Invalid SCXML context",        context.get("bear"));
        assertEquals("Invalid SCXML context",0,      context.getVars().size());
        assertNull  ("Invalid Javascript bindings",  bindings.get("bear"));
        assertNull  ("Invalid Javascript bindings",  bindings.get("cat"));
        assertNull  ("Invalid Javascript bindings",  bindings.get("dog"));
        assertEquals("Invalid Javascript bindings",0,bindings.size());
    }

    /**
     * Tests the <code>clear</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
    public void testClear() {
        Context            context  = new JSContext     ();
        Bindings           bindings = new SimpleBindings();
        JSBindings         jsx      = new JSBindings    (context,bindings);

        context.set ("bear","koala");
        bindings.put("bear","grizzly");
        bindings.put("cat", "felix");
        bindings.put("dog", "rover");

        assertNotNull("Invalid SCXML context",        context.get("bear"));
        assertEquals ("Invalid SCXML context","koala",context.get("bear"));
        assertEquals ("Invalid SCXML context",1,      context.getVars().size());

        assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        assertNotNull("Invalid Javascript bindings",          bindings.get("cat"));
        assertNotNull("Invalid Javascript bindings",          bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        assertEquals ("Invalid Javascript bindings","felix",  bindings.get("cat"));
        assertEquals ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        assertEquals ("Invalid Javascript bindings",3,        bindings.size());

        jsx.clear();

        assertNotNull("Invalid SCXML context",       context.get("bear"));
        assertEquals("Invalid SCXML context","koala",context.get("bear"));
        assertEquals("Invalid SCXML context",1,      context.getVars().size());
        assertNull  ("Invalid Javascript bindings",  bindings.get("bear"));
        assertNull  ("Invalid Javascript bindings",  bindings.get("cat"));
        assertNull  ("Invalid Javascript bindings",  bindings.get("dog"));
        assertEquals("Invalid Javascript bindings",0,bindings.size());
    }

}

