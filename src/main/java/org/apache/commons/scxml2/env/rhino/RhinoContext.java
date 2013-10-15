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
package org.apache.commons.scxml2.env.rhino;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.scxml2.env.SimpleContext;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Node;

/**
 * @see org.apache.commons.scxml2.Context
 */
public class RhinoContext extends SimpleContext {

    /** Serial Version UID. */
    private static final long serialVersionUID = 1L;

    /** Rhino scope for saving variables. */
    private Scriptable scope;

    //// Constants
    private static final String SCOPE_LOCAL = "local";
    private static final String SCOPE_PARENT = "parent";
    private static final String SCOPE_GLOBAL = "global";

    private static final String _ALL_NAMESPACES = "_ALL_NAMESPACES";

    /**
     * Default constructor.
     */
    public RhinoContext() {
        this(null);
    }

    /**
     * Creates new rhino scope. If parent is not null it will be set to new scope's parent.
     *
     * @param parent Parent context.
     */
    public RhinoContext(org.apache.commons.scxml2.Context parent) {
        super(parent);
        initContext();
    }

    /**
     * @see org.apache.commons.scxml2.Context#set(String, Object)
     */
    public void set(String name, Object value) {
        org.apache.commons.scxml2.Context parent = getParent();
        // if variable is local => override it
        if (scope.has(name, scope)) {
            setLocal(name, value);
        }
        // iterate over all parents until variable is found
        else if (parent != null && parent.has(name)) {
            parent.set(name, value);
        }
        // no parent contains that variable => create it in local scope
        else {
            setLocal(name, value);
        }
    }

    /**
     * @see org.apache.commons.scxml2.Context#setLocal(String, Object)
     */
    @SuppressWarnings("unchecked")
    public void setLocal(String name, Object value) {
        Context cx = Context.enter();
        try {
            if (_ALL_NAMESPACES.equals(name)) {
                Map<String, String> namespaceMap = (Map<String, String>) value;
                setNamespaces(cx, namespaceMap);
                if (value != null) {
                    scope.put(_ALL_NAMESPACES, scope, value);
                }
            } else {
                addVarToScope(cx, scope, name, value);
            }
            // output for set variables
            if (!name.startsWith("_") && !String.valueOf(value).startsWith("<")) {
                if (value != null) {
                    getLog().debug(name + " = " + String.valueOf(value) + " (" + value.getClass() + ")");
                } else {
                    getLog().debug(name + " = (NULL)");
                }
            } else if (!name.startsWith("_") && String.valueOf(value).startsWith("<")) {
                getLog().info(name + " =\n" + String.valueOf(value));
            }
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        } finally {
            Context.exit();
        }
    }

    /**
     * @see org.apache.commons.scxml2.Context#has(String)
     */
    public boolean has(String name) {
        org.apache.commons.scxml2.Context parent = getParent();
        Context.enter();
        try {
            // search local
            if (scope.has(name, null)) {
                return true;
            }
            // search in parent
            else if (parent != null && parent.has(name)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            return false;
        } finally {
            Context.exit();
        }
    }

    /**
     * @see org.apache.commons.scxml2.Context#get(String)
     */
    public Object get(String name) {
        org.apache.commons.scxml2.Context parent = getParent();
        Context.enter();
        try {
            // get local
            Object var = scope.get(name, null);

            if (var != Scriptable.NOT_FOUND) {
                return var;
            }
            // get from parent
            else if (parent != null) {
                var = parent.get(name);
                if (var != Scriptable.NOT_FOUND) {
                    return var;
                }
            }
            return null;
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
            return null;
        } finally {
            Context.exit();
        }
    }

    /**
     * Get the variable map for this Rhino context's scope.
     *
     * @see org.apache.commons.scxml2.Context#getVars()
     */
    public Map<String, Object> getVars() {
        Map<String, Object> vars = new HashMap<String, Object>();
        Object[] ids = scope.getIds();
        for (int i = ids.length - 1; i >= 0; i--) {
            String name = ids[i].toString();
            Object value = scope.get(name, null);
            // Put all "user defined" objects in the map
            if (value instanceof String) {
                vars.put(name, value);
            }
        }
        getLog().warn("RhinoContext::getVars() was called, which returns Strings only.");
        return vars;
    }

    /**
     * @see org.apache.commons.scxml2.Context#reset()
     */
    public void reset() {
        initContext();
    }

    /**
     * Get the Rhino scope for executing expressions.
     *
     * @return Scriptable Rhino scope.
     */
    public Scriptable getScope() {
        return scope;
    }

    /**
     * Initializes the Rhino context.
     */
    private void initContext() {
        // create new rhino scope (necessary for evaluator)
        Context cx = Context.enter();
        if (getParent() == null) {
            // the root scope gets the standard objects and constructors
            scope = cx.initStandardObjects();
        } else {
            // get the root scope...
            org.apache.commons.scxml2.Context rootContext = getParent();
            while (rootContext.getParent() != null) {
                rootContext = rootContext.getParent();
            }
            // ...to search for the constructor to evaluate this scope against
            scope = cx.newObject(((RhinoContext) rootContext).getScope());
        }
        setScopeAttributes();
        Context.exit();
    }

    /**
     * Puts scopes as variables into the scope of this context according to the scope chain.
     */
    private void setScopeAttributes() {
        org.apache.commons.scxml2.Context parent = getParent();

        // set itself as "local"
        scope.put(SCOPE_LOCAL, scope, scope);

        // set parent scope as "parent"
        if (parent != null) {
            Scriptable parentscope = ((RhinoContext) parent).getScope();
            scope.setParentScope(parentscope);
            scope.put(SCOPE_PARENT, scope, parentscope);
        } else {
            scope.setParentScope(null);
            scope.put(SCOPE_PARENT, scope, null);
        }

        // set root scope as "global"
        Scriptable rootscope = scope;
        while (rootscope.getParentScope() != null) {
            rootscope = rootscope.getParentScope();
        }
        scope.put(SCOPE_GLOBAL, scope, rootscope);
    }

    /**
     * Adds the given variable into the scope.
     *
     * @param context
     * @param scope
     * @param name
     * @param value
     */
    @SuppressWarnings("unchecked")
    private void addVarToScope(Context context, Scriptable scope, String name, Object value) {

        if (value == null) {

            scope.delete(name);

        } else if (value instanceof Map) {

            Map<String, Object> mapValue = (Map<String, Object>) value;
            Scriptable mapJSObject = convertMapToJS(context, scope, mapValue);
            scope.put(name, scope, mapJSObject);

        } else if (value instanceof String) {

            String stringValue = (String) value;
            // check for a XML string
            if (stringValue.startsWith("<")) {
                XmlObject xmlObject = null;
                try {
                    XmlOptions options = new XmlOptions();
                    options.setLoadStripWhitespace();
                    options.setLoadStripComments();
                    xmlObject = XmlObject.Factory.parse(stringValue, options);
                } catch (XmlException e) {
                    getLog().error(e.getMessage(), e);
                    return;
                }
                Scriptable xmlJSObject = context.newObject(scope, "XML",
                    new Object[] { Context.javaToJS(xmlObject, scope) });
                scope.put(name, scope, xmlJSObject);
            } else {
                scope.put(name, scope, stringValue);
            }

        } else if (value instanceof Node) {

            Node nodeValue = (Node) value;
            XmlObject xml = null;
            try {
                XmlOptions options = new XmlOptions();
                options.setLoadStripWhitespace();
                options.setLoadStripComments();
                xml = XmlObject.Factory.parse(nodeValue, options);
            } catch (XmlException e) {
                getLog().error(e.getMessage(), e);
                return;
            }
            Scriptable xmlObject = context.newObject(scope, "XML", new Object[] { Context.javaToJS(xml, scope) });
            scope.put(name, scope, xmlObject);

        } else {

            scope.put(name, scope, value);

        }
    }

    /**
     * Converts a {@link Map} to an JavaScript object.
     *
     * @param cx
     *            Context to evaluate new XMLObject
     * @param scope
     *            Rhino scope to evaluate new XMLObject in
     * @param map
     *            Map to convert into an XMLObject
     * @return Returns the converted Map.
     *
     */
    private Scriptable convertMapToJS(Context cx, Scriptable scope, Map<String, Object> map) {
        Scriptable newJSObject = cx.newObject(scope);
        for (String key : map.keySet()) {
            Object value = map.get(key);
            addVarToScope(cx, newJSObject, key, value);
        }
        return newJSObject;
    }

    /**
     * Sets a specified map of namespaces in RhinoContext.
     *
     * @param cx
     *            Context to evaluate namespaces
     * @param value
     *            Map of namespaces
     */
    private void setNamespaces(Context cx, Map<String, String> namespaceMap) {
        if (namespaceMap == null) {
            return;
        }
        for (String nsPrefix : namespaceMap.keySet()) {
            String nsURI = namespaceMap.get(nsPrefix);
            // ignore the default namespace
            if (!"".equals(nsPrefix)) {
                Scriptable nsObject = cx.newObject(scope, "Namespace", new Object[] { nsPrefix, nsURI });
                scope.put(nsPrefix, scope, nsObject);
            }
        }
    }

}

