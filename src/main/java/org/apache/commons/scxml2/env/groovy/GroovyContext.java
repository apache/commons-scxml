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
package org.apache.commons.scxml2.env.groovy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.env.SimpleContext;

import groovy.lang.Closure;

/**
 * Groovy Context implementation for Commons SCXML.
 */
public class GroovyContext extends SimpleContext {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(GroovyContext.class);

    /**
     * Internal flag to indicate whether it is to evaluate a location
     * that returns a Node within an XML data tree.
     */
    private boolean evaluatingLocation = false;

    private String scriptBaseClass;
    private GroovyEvaluator evaluator;
    private GroovyContextBinding binding;
    private Map<String, Object> vars;

    GroovyContextBinding getBinding() {
        if (binding == null) {
            binding = new GroovyContextBinding(this);
        }
        return  binding;
    }

    /**
     * Constructor.
     */
    public GroovyContext() {
        super();
    }

    /**
     * Constructor with initial vars.
     *
     * @param initialVars The initial set of variables.
     */
    public GroovyContext(final Map<String, Object> initialVars, GroovyEvaluator evaluator) {
        super(initialVars);
        this.evaluator = evaluator;
    }

    /**
     * Constructor with parent context.
     *
     * @param parent The parent context.
     */
    public GroovyContext(final Context parent, GroovyEvaluator evaluator) {
        super(parent);
        this.evaluator = evaluator;
    }

    protected GroovyEvaluator getGroovyEvaluator() {
        return evaluator;
    }

    protected void setGroovyEvaluator(GroovyEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Returns the internal flag to indicate whether it is to evaluate a location
     * that returns a Node within an XML data tree.
     */
    public boolean isEvaluatingLocation() {
        return evaluatingLocation;
    }

    /**
     * Sets the internal flag to indicate whether it is to evaluate a location
     * that returns a Node within an XML data tree.
     */
    public void setEvaluatingLocation(boolean evaluatingLocation) {
        this.evaluatingLocation = evaluatingLocation;
    }

    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    @Override
    protected void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

    protected void setScriptBaseClass(String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    protected String getScriptBaseClass() {
        if (scriptBaseClass != null) {
            return scriptBaseClass;
        }
        if (getParent() != null) {
            return ((GroovyContext)getParent()).getScriptBaseClass();
        }
        return null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        boolean closureErased = false;
        if (vars != null) {
            Iterator<Map.Entry<String, Object>> iterator = getVars().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                if (entry.getValue() != null && entry.getValue() instanceof Closure) {
                    iterator.remove();
                    closureErased = true;
                }
            }
            if (closureErased) {
                log.warn("Encountered and removed Groovy Closure(s) in the GroovyContext during serialization: these are not supported for (de)serialization");
            }
        }
        out.writeObject(this.scriptBaseClass);
        out.writeObject(this.evaluator);
        out.writeObject(this.binding);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new ObjectOutputStream(bout).writeObject(this.vars);
        out.writeObject(bout.toByteArray());
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        this.scriptBaseClass = (String)in.readObject();
        this.evaluator = (GroovyEvaluator)in.readObject();
        this.binding = (GroovyContextBinding)in.readObject();
        byte[] bytes  = (byte[])in.readObject();
        if (evaluator != null) {
            this.vars = (Map<String, Object>)
                    new ObjectInputStream(new ByteArrayInputStream(bytes)) {
                        protected Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
                            return Class.forName(osc.getName(), true, evaluator.getGroovyClassLoader());
                        }
                    }.readObject();
        }
        else {
            this.vars = (Map<String, Object>)new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        }
    }
}
