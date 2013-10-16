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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.scxml2.Context;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.SimpleBindings;

/**
 * JUnit 3 test case for the JSBinding implementation that imports
 * SCXML context variables into a JavaScript bindings. Includes tests
 * for:
 * <ul>
 * <li> constructor
 * </ul>
 */
public class JSBindingsTest {
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
    @Before
    public void setUp() throws Exception {
    }

    // CLASS METHODS

    /**
     * Stand-alone test runtime.
     */
    public static void main(String args[]) {
        String[] testCaseName = {JSBindingsTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // INSTANCE METHOD TESTS

    /**
     * Tests implementation of JSBindings constructor.
     */    
    @Test
    public void testConstructor() {
        Assert.assertNotNull(new JSBindings(new JSContext(),new SimpleBindings()));
    }

    /**
     * Test implementation of JSBindings constructor with invalid SCXML context.
     */    
    @Test
    public void testInvalidContextConstructor() {
        try {
             Assert.assertNotNull(new JSBindings(null,new SimpleBindings()));
             Assert.fail("JSBindings constructor accepted invalid SCXML context");

        } catch (IllegalArgumentException x) {
             // expected, ignore
        }
    }

    /**
     * Test implementation of JSBindings constructor with invalid Javascript bindings.
     */    
    @Test
    public void testInvalidBindingsConstructor() {
        try {
             Assert.assertNotNull(new JSBindings(new JSContext(),null));
             Assert.fail("JSBindings constructor accepted invalid Javascript bindings");

        } catch (IllegalArgumentException x) {
             // expected, ignore
        }
    }

    /**
     * Tests the <code>containsKey</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testContainsKey() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertFalse("Invalid SCXML context",      context.has         ("bear"));
        Assert.assertFalse("Invalid Javascript bindings",bindings.containsKey("bear"));
        Assert.assertFalse("Invalid JSbindings",         jsx.containsKey     ("bear"));

        context.set("bear","koala");
        Assert.assertTrue ("Invalid SCXML context",      context.has         ("bear"));
        Assert.assertFalse("Invalid Javascript bindings",bindings.containsKey("bear"));
        Assert.assertTrue ("Invalid JSbindings",         jsx.containsKey     ("bear"));

        context.reset();
        bindings.put ("bear","grizzly");
        Assert.assertFalse  ("Invalid SCXML context",      context.has         ("bear"));
        Assert.assertTrue   ("Invalid Javascript bindings",bindings.containsKey("bear"));
        Assert.assertTrue   ("Invalid JSbindings",         jsx.containsKey     ("bear"));

        context.set ("bear","koala");
        bindings.put("bear","grizzly");
        Assert.assertTrue  ("Invalid SCXML context",      context.has         ("bear"));
        Assert.assertTrue  ("Invalid Javascript bindings",bindings.containsKey("bear"));
        Assert.assertTrue  ("Invalid JSbindings",         jsx.containsKey     ("bear"));
    }

    /**
     * Tests the <code>keySet</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testKeySet() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertFalse ("Invalid SCXML context",      context.has          ("bear"));
        Assert.assertFalse ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        Assert.assertFalse ("Invalid JSbindings",         jsx.keySet().contains("bear"));

        context.set   ("bear","koala");
        bindings.clear();
        Assert.assertTrue    ("Invalid SCXML context",      context.has          ("bear"));
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.keySet().contains("bear"));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        Assert.assertFalse   ("Invalid SCXML context",      context.has          ("bear"));
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.keySet().contains("bear"));

        context.reset ();
        bindings.clear();
        context.set  ("cat","felix");
        bindings.put ("dog","rover");
        Assert.assertFalse  ("Invalid SCXML context",      context.has          ("bear"));
        Assert.assertFalse  ("Invalid Javascript bindings",bindings.containsKey ("bear"));
        Assert.assertTrue   ("Invalid SCXML context",      context.has          ("cat"));
        Assert.assertTrue   ("Invalid Javascript bindings",bindings.containsKey ("dog"));
        Assert.assertTrue   ("Invalid JSbindings",         jsx.keySet().contains("cat"));
        Assert.assertTrue   ("Invalid JSbindings",         jsx.keySet().contains("dog"));
    }

    /**
     * Tests the <code>size</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */
   
    @Test
    public void testSize() {
       Context    context  = new JSContext     ();
       Bindings   bindings = new SimpleBindings();
       JSBindings jsx      = new JSBindings    (context,bindings);

       Assert.assertFalse ("Invalid SCXML context",      context.has          ("bear"));
       Assert.assertFalse ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       Assert.assertEquals("Invalid JSbindings",0,jsx.size());

       context.set   ("bear","koala");
       bindings.clear();
       Assert.assertTrue    ("Invalid SCXML context",      context.has          ("bear"));
       Assert.assertFalse   ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       Assert.assertEquals  ("Invalid JSbindings",1,jsx.size());

       context.reset ();
       bindings.clear();
       bindings.put  ("bear","grizzly");
       Assert.assertFalse   ("Invalid SCXML context",      context.has          ("bear"));
       Assert.assertTrue    ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       Assert.assertEquals  ("Invalid JSbindings",1,jsx.size());

       context.reset ();
       bindings.clear();
       context.set   ("bear","koala");
       bindings.put  ("bear","grizzly");
       Assert.assertTrue    ("Invalid SCXML context",      context.has          ("bear"));
       Assert.assertTrue    ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       Assert.assertEquals  ("Invalid JSbindings",1,jsx.size());

       context.reset ();
       bindings.clear();
       context.set  ("cat","felix");
       bindings.put ("dog","rover");
       Assert.assertFalse  ("Invalid SCXML context",      context.has          ("bear"));
       Assert.assertFalse  ("Invalid Javascript bindings",bindings.containsKey ("bear"));
       Assert.assertTrue   ("Invalid SCXML context",      context.has          ("cat"));
       Assert.assertTrue   ("Invalid Javascript bindings",bindings.containsKey ("dog"));
       Assert.assertEquals ("Invalid JSbindings",2,jsx.size());
    }

    /**
     * Tests the <code>containsValue</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testContainsValue() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertFalse("Invalid SCXML context",      context.getVars().containsValue("koala"));
        Assert.assertFalse("Invalid Javascript bindings",bindings.containsValue("koala"));
        Assert.assertFalse("Invalid JSbindings",         jsx.containsValue     ("koala"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().containsValue("koala"));
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.containsValue("koala"));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("koala"));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        Assert.assertFalse   ("Invalid SCXML context",      context.getVars().containsValue("grizzly"));
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.containsValue("grizzly"));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("grizzly"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().containsValue("koala"));
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.containsValue("grizzly"));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("koala"));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.containsValue     ("grizzly"));
    }

    /**
     * Tests the <code>entrySet</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testEntrySet() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertEquals("Invalid SCXML context",      0,context.getVars().entrySet().size());
        Assert.assertEquals("Invalid Javascript bindings",0,bindings.entrySet().size());
        Assert.assertEquals("Invalid JSbindings",         0,jsx.entrySet().size());

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        Assert.assertEquals  ("Invalid SCXML context",      1,context.getVars().entrySet().size());
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().entrySet().contains(KOALA));
        Assert.assertEquals  ("Invalid Javascript bindings",0,bindings.entrySet().size());
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.entrySet().contains(KOALA));
        Assert.assertEquals  ("Invalid JSBindings",         1,jsx.entrySet().size());
        Assert.assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(KOALA));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        Assert.assertEquals  ("Invalid SCXML context",      0,context.getVars().entrySet().size());
        Assert.assertFalse   ("Invalid SCXML context",      context.getVars().entrySet().contains(GRIZZLY));
        Assert.assertEquals  ("Invalid Javascript bindings",1,bindings.entrySet().size());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.entrySet().contains(GRIZZLY));
        Assert.assertEquals  ("Invalid JSBindings",         1,jsx.entrySet().size());
        Assert.assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(GRIZZLY));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        Assert.assertEquals  ("Invalid SCXML context",      1,context.getVars().entrySet().size());
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().entrySet().contains(KOALA));
        Assert.assertEquals  ("Invalid Javascript bindings",1,bindings.entrySet().size());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.entrySet().contains(GRIZZLY));
        Assert.assertEquals  ("Invalid JSBindings",         1,jsx.entrySet().size());
        Assert.assertFalse   ("Invalid JSbindings",         jsx.entrySet().contains(KOALA));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(GRIZZLY));

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        Assert.assertEquals  ("Invalid SCXML context",      1,context.getVars().entrySet().size());
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().entrySet().contains(FELIX));
        Assert.assertEquals  ("Invalid Javascript bindings",1,bindings.entrySet().size());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.entrySet().contains(ROVER));
        Assert.assertEquals  ("Invalid JSBindings",         2,jsx.entrySet().size());
        Assert.assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(FELIX));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.entrySet().contains(ROVER));
    }

    /**
     * Tests the <code>values</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testValues() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertEquals("Invalid SCXML context",      0,context.getVars().values().size());
        Assert.assertEquals("Invalid Javascript bindings",0,bindings.values().size());
        Assert.assertEquals("Invalid JSbindings",         0,jsx.values().size());

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        Assert.assertEquals  ("Invalid SCXML context",      1,context.getVars().values().size());
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().values().contains(KOALA.getValue()));
        Assert.assertEquals  ("Invalid Javascript bindings",0,bindings.values().size());
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.values().contains(KOALA.getValue()));
        Assert.assertEquals  ("Invalid JSBindings",         1,jsx.values().size());
        Assert.assertTrue    ("Invalid JSbindings",         jsx.values().contains(KOALA.getValue()));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        Assert.assertEquals  ("Invalid SCXML context",      0,context.getVars().values().size());
        Assert.assertFalse   ("Invalid SCXML context",      context.getVars().values().contains(GRIZZLY.getValue()));
        Assert.assertEquals  ("Invalid Javascript bindings",1,bindings.values().size());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.values().contains(GRIZZLY.getValue()));
        Assert.assertEquals  ("Invalid JSBindings",         1,jsx.values().size());
        Assert.assertTrue    ("Invalid JSbindings",         jsx.values().contains(GRIZZLY.getValue()));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        Assert.assertEquals  ("Invalid SCXML context",      1,context.getVars().values().size());
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().values().contains(KOALA.getValue()));
        Assert.assertEquals  ("Invalid Javascript bindings",1,bindings.values().size());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.values().contains(GRIZZLY.getValue()));
        Assert.assertEquals  ("Invalid JSBindings",         1,jsx.values().size());
        Assert.assertFalse   ("Invalid JSbindings",         jsx.values().contains(KOALA.getValue()));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.values().contains(GRIZZLY.getValue()));

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        Assert.assertEquals  ("Invalid SCXML context",      1,context.getVars().values().size());
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().values().contains(FELIX.getValue()));
        Assert.assertEquals  ("Invalid Javascript bindings",1,bindings.values().size());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.values().contains(ROVER.getValue()));
        Assert.assertEquals  ("Invalid JSBindings",         2,jsx.values().size());
        Assert.assertTrue    ("Invalid JSbindings",         jsx.values().contains(FELIX.getValue()));
        Assert.assertTrue    ("Invalid JSbindings",         jsx.values().contains(ROVER.getValue()));
    }

    /**
     * Tests the <code>isEmpty</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testIsEmpty() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertTrue("Invalid SCXML context",      context.getVars().isEmpty());
        Assert.assertTrue("Invalid Javascript bindings",bindings.isEmpty());
        Assert.assertTrue("Invalid JSbindings",         jsx.isEmpty());

        context.set   ("bear","koala");
        bindings.clear();
        Assert.assertFalse   ("Invalid SCXML context",      context.getVars().isEmpty());
        Assert.assertTrue    ("Invalid Javascript bindings",bindings.isEmpty());
        Assert.assertFalse   ("Invalid JSbindings",         jsx.isEmpty());

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        Assert.assertTrue    ("Invalid SCXML context",      context.getVars().isEmpty());
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.isEmpty());
        Assert.assertFalse   ("Invalid JSbindings",         jsx.isEmpty());

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        Assert.assertFalse   ("Invalid SCXML context",      context.getVars().isEmpty());
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.isEmpty());
        Assert.assertFalse   ("Invalid JSbindings",         jsx.isEmpty());

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        Assert.assertFalse   ("Invalid SCXML context",      context.getVars().isEmpty());
        Assert.assertFalse   ("Invalid Javascript bindings",bindings.isEmpty());
        Assert.assertFalse   ("Invalid JSbindings",         jsx.isEmpty());
    }

    /**
     * Tests the <code>get</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testGet() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertNull("Invalid SCXML context",      context.get ("bear"));
        Assert.assertNull("Invalid Javascript bindings",bindings.get("bear"));
        Assert.assertNull("Invalid JSbindings",         jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        Assert.assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        Assert.assertEquals  ("Invalid SCXML context","koala",context.get ("bear"));
        Assert.assertNull    ("Invalid Javascript bindings",  bindings.get("bear"));
        Assert.assertNotNull ("Invalid JSbindings",           jsx.get     ("bear"));
        Assert.assertEquals  ("Invalid JSbindings","koala",   jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        bindings.put  ("bear","grizzly");
        Assert.assertNull    ("Invalid SCXML context",                context.get ("bear"));
        Assert.assertNotNull ("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertEquals  ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertNotNull ("Invalid JSbindings",                   jsx.get     ("bear"));
        Assert.assertEquals  ("Invalid JSbindings","grizzly",         jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        context.set   ("bear","koala");
        bindings.put  ("bear","grizzly");
        Assert.assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        Assert.assertEquals  ("Invalid SCXML context","koala",context.get ("bear"));
        Assert.assertNotNull ("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertEquals  ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertNotNull ("Invalid JSbindings",                   jsx.get     ("bear"));
        Assert.assertEquals  ("Invalid JSbindings","grizzly",         jsx.get     ("bear"));

        context.reset ();
        bindings.clear();
        context.set   ("cat","felix");
        bindings.put  ("dog","rover");
        Assert.assertNotNull ("Invalid SCXML context",              context.get ("cat"));
        Assert.assertEquals  ("Invalid SCXML context","felix",      context.get ("cat"));
        Assert.assertNotNull ("Invalid Javascript bindings",        bindings.get("dog"));
        Assert.assertEquals  ("Invalid Javascript bindings","rover",bindings.get("dog"));
        Assert.assertNotNull ("Invalid JSbindings",                 jsx.get     ("cat"));
        Assert.assertEquals  ("Invalid JSbindings","felix",         jsx.get     ("cat"));
        Assert.assertNotNull ("Invalid JSbindings",                 jsx.get     ("dog"));
        Assert.assertEquals  ("Invalid JSbindings","rover",         jsx.get     ("dog"));
    }

    /**
     * Tests the <code>put</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testPut() {
        Context    context  = new JSContext     ();
        Bindings   bindings = new SimpleBindings();
        JSBindings jsx      = new JSBindings    (context,bindings);

        Assert.assertNull("Invalid SCXML context",      context.get ("bear"));
        Assert.assertNull("Invalid Javascript bindings",bindings.get("bear"));
        Assert.assertNull("Invalid JSbindings",         jsx.get     ("bear"));

        jsx.put       ("bear","koala");
        Assert.assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        Assert.assertEquals  ("Invalid SCXML context","koala",context.get("bear"));
        Assert.assertNotNull ("Invalid JSbindings",           jsx.get ("bear"));
        Assert.assertNull    ("Invalid Javascript bindings",  bindings.get("bear"));
    }

    /**
     * Tests the <code>putAll</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testPutAll() {
        Context            context  = new JSContext     ();
        Bindings           bindings = new SimpleBindings();
        JSBindings         jsx      = new JSBindings    (context,bindings);
        Map<String,Object> vars     = new HashMap<String, Object>();

        vars.put("bear","grizzly");
        vars.put("cat","felix");
        vars.put("dog","rover");

        Assert.assertNull("Invalid SCXML context",      context.get ("bear"));
        Assert.assertNull("Invalid SCXML context",      context.get ("cat"));
        Assert.assertNull("Invalid SCXML context",      context.get ("dog"));

        Assert.assertNull("Invalid Javascript bindings",bindings.get("bear"));
        Assert.assertNull("Invalid Javascript bindings",bindings.get("cat"));
        Assert.assertNull("Invalid Javascript bindings",bindings.get("dog"));

        Assert.assertNull("Invalid JSbindings",         jsx.get     ("bear"));
        Assert.assertNull("Invalid JSbindings",         jsx.get     ("cat"));
        Assert.assertNull("Invalid JSbindings",         jsx.get     ("dog"));

        context.set("bear","koala");
        jsx.putAll (vars);

        Assert.assertNotNull ("Invalid SCXML context",        context.get ("bear"));
        Assert.assertNull    ("Invalid SCXML context",        context.get ("cat"));
        Assert.assertNull    ("Invalid SCXML context",        context.get ("dog"));
        Assert.assertEquals  ("Invalid SCXML context","koala",context.get ("bear"));
        Assert.assertEquals  ("Invalid SCXML context",1,      context.getVars().size());

        Assert.assertNotNull ("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertNotNull ("Invalid Javascript bindings",          bindings.get("cat"));
        Assert.assertNotNull ("Invalid Javascript bindings",          bindings.get("dog"));
        Assert.assertEquals  ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertEquals  ("Invalid Javascript bindings","felix",  bindings.get("cat"));
        Assert.assertEquals  ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        Assert.assertEquals  ("Invalid Javascript bindings",3,        bindings.size());
    }

    /**
     * Tests the <code>remove</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testRemove() {
        Context            context  = new JSContext     ();
        Bindings           bindings = new SimpleBindings();
        JSBindings         jsx      = new JSBindings    (context,bindings);

        context.set ("bear","koala");
        bindings.put("bear","grizzly");
        bindings.put("cat", "felix");
        bindings.put("dog", "rover");

        Assert.assertNotNull("Invalid SCXML context",        context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context","koala",context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context",1,      context.getVars().size());

        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("cat"));
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertEquals ("Invalid Javascript bindings","felix",  bindings.get("cat"));
        Assert.assertEquals ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings",3,        bindings.size());

        jsx.remove("cat");

        Assert.assertNotNull("Invalid SCXML context",                context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context","koala",        context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context",1,              context.getVars().size());
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertNull   ("Invalid Javascript bindings",          bindings.get("cat"));
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertEquals ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings",2,        bindings.size());

        jsx.remove("dog");

        Assert.assertNotNull("Invalid SCXML context",               context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context","koala",        context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context",1,              context.getVars().size());
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertNull   ("Invalid Javascript bindings",          bindings.get("cat"));
        Assert.assertNull   ("Invalid Javascript bindings",          bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertEquals ("Invalid Javascript bindings",1,        bindings.size());

        jsx.remove("bear");

        Assert.assertNotNull("Invalid SCXML context",       context.get("bear"));
        Assert.assertEquals("Invalid SCXML context","koala",context.get("bear"));
        Assert.assertEquals("Invalid SCXML context",1,      context.getVars().size());
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("bear"));
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("cat"));
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("dog"));
        Assert.assertEquals("Invalid Javascript bindings",0,bindings.size());

        jsx.remove("bear");

        Assert.assertNull  ("Invalid SCXML context",        context.get("bear"));
        Assert.assertEquals("Invalid SCXML context",0,      context.getVars().size());
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("bear"));
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("cat"));
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("dog"));
        Assert.assertEquals("Invalid Javascript bindings",0,bindings.size());
    }

    /**
     * Tests the <code>clear</code> method with items in the SCXML context as well as the
     * JavaScript Bindings.
     *
     */    
    @Test
    public void testClear() {
        Context            context  = new JSContext     ();
        Bindings           bindings = new SimpleBindings();
        JSBindings         jsx      = new JSBindings    (context,bindings);

        context.set ("bear","koala");
        bindings.put("bear","grizzly");
        bindings.put("cat", "felix");
        bindings.put("dog", "rover");

        Assert.assertNotNull("Invalid SCXML context",        context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context","koala",context.get("bear"));
        Assert.assertEquals ("Invalid SCXML context",1,      context.getVars().size());

        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("bear"));
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("cat"));
        Assert.assertNotNull("Invalid Javascript bindings",          bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings","grizzly",bindings.get("bear"));
        Assert.assertEquals ("Invalid Javascript bindings","felix",  bindings.get("cat"));
        Assert.assertEquals ("Invalid Javascript bindings","rover",  bindings.get("dog"));
        Assert.assertEquals ("Invalid Javascript bindings",3,        bindings.size());

        jsx.clear();

        Assert.assertNotNull("Invalid SCXML context",       context.get("bear"));
        Assert.assertEquals("Invalid SCXML context","koala",context.get("bear"));
        Assert.assertEquals("Invalid SCXML context",1,      context.getVars().size());
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("bear"));
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("cat"));
        Assert.assertNull  ("Invalid Javascript bindings",  bindings.get("dog"));
        Assert.assertEquals("Invalid Javascript bindings",0,bindings.size());
    }

}

