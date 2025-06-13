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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.SCInstanceObjectInputStream;
import org.apache.commons.scxml2.env.SimpleContext;

import groovy.lang.Closure;

/**
 * Groovy Context implementation for Commons SCXML.
 */
public class GroovyContext extends SimpleContext {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(GroovyContext.class);

    private String scriptBaseClass;
    private GroovyEvaluator evaluator;
    private GroovyContextBinding binding;
    private Map<String, Object> vars;

    /**
     * Constructs a new instance.
     */
    public GroovyContext() {
    }

    /**
     * Constructor with parent context.
     *
     * @param parent The parent context.
     * @param evaluator The groovy evaluator
     */
    public GroovyContext(final Context parent, final GroovyEvaluator evaluator) {
        super(parent);
        this.evaluator = evaluator;
    }

    /**
     * Constructor with initial vars.
     *
     * @param parent The parent context.
     * @param initialVars The initial set of variables.
     * @param evaluator The groovy evaluator
     */
    public GroovyContext(final Context parent, final Map<String, Object> initialVars, final GroovyEvaluator evaluator) {
        super(parent, initialVars);
        this.evaluator = evaluator;
    }

    GroovyContextBinding getBinding() {
        if (binding == null) {
            binding = new GroovyContextBinding(this);
        }
        return  binding;
    }

    protected GroovyEvaluator getGroovyEvaluator() {
        return evaluator;
    }

    protected String getScriptBaseClass() {
        if (scriptBaseClass != null) {
            return scriptBaseClass;
        }
        if (getParent() instanceof GroovyContext) {
            return ((GroovyContext)getParent()).getScriptBaseClass();
        }
        return null;
    }

    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    @SuppressWarnings("unchecked")
    private void readObject(final ObjectInputStream in) throws IOException,ClassNotFoundException {
        this.scriptBaseClass = (String)in.readObject();
        this.evaluator = (GroovyEvaluator)in.readObject();
        this.binding = (GroovyContextBinding)in.readObject();
        SCInstanceObjectInputStream.ClassResolver currentResolver = null;
        try {
            if (evaluator != null && in instanceof SCInstanceObjectInputStream) {
                currentResolver = ((SCInstanceObjectInputStream)in).setClassResolver(osc -> Class.forName(osc.getName(), true, evaluator.getGroovyClassLoader()));
            }
            this.vars = (Map<String, Object>)in.readObject();
        }
        finally {
            if (in instanceof SCInstanceObjectInputStream) {
                ((SCInstanceObjectInputStream)in).setClassResolver(currentResolver);
            }
        }
    }

    protected void setGroovyEvaluator(final GroovyEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    protected void setScriptBaseClass(final String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    @Override
    protected void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        boolean closureErased = false;
        if (vars != null) {
            final Iterator<Map.Entry<String, Object>> iterator = getVars().entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry<String, Object> entry = iterator.next();
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
        out.writeObject(this.vars);
    }
}
