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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashMap;

import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Script;

/**
 * GroovyExtendableScriptCache is a general purpose and <em>{@link Serializable}</em> Groovy Script cache.
 * <p>
 * It provides automatic compilation of scripts and caches the resulting class(es) internally, and after de-serialization
 * re-compiles the cached scripts automatically.
 * </p>
 * <p>
 * It also provides easy support for (and scoped) script compilation with a specific {@link Script} base class.
 * </p>
 * <p>
 * Internally it uses a non-serializable and thus transient {@link GroovyClassLoader}, {@link CompilerConfiguration} and
 * the parent classloader to use.<br/>
 * To be able to be serializable, the {@link GroovyClassLoader} is automatically (re)created if not defined yet, and for
 * the  {@link CompilerConfiguration} and parent classloader it uses serializable instances of
 * {@link CompilerConfigurationFactory} and {@link ParentClassLoaderFactory} interfaces which either can be configured
 * or have defaults otherwise.
 * </p>
 * <p>
 * The underlying {@link GroovyClassLoader} can be accessed through {@link #getGroovyClassLoader()}, which might be needed
 * to de-serialize previously defined/created classes and objects through this class, from within a containing object
 * readObject(ObjectInputStream in) method.<br/>
 * For more information how this works and should be done, see:
 * <a href="http://jira.codehaus.org/browse/GROOVY-1627">Groovy-1627: Deserialization fails to work</a>
 * </p>
 * <p>
 * One other optional feature is script pre-processing which can be configured through an instance of the
 * {@link ScriptPreProcessor} interface (also {@link Serializable} of course).<br/>
 * When configured, the script source will be passed through the {@link ScriptPreProcessor#preProcess(String)} method
 * before being compiled.
 * </p>
 * <p>
 * The cache itself as well as the underlying GroovyClassLoader caches can be cleared through {@link #clearCache()}.
 * </p>
 * <p>
 * The GroovyExtendableScriptCache has no other external dependencies other than Groovy itself,
 * so can be used independent of Commons SCXML.
 * </p>
 */
public class GroovyExtendableScriptCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Serializable factory interface providing the Groovy parent ClassLoader,
     * needed to restore the specific ClassLoader after de-serialization
     */
    public interface ParentClassLoaderFactory extends Serializable {
        ClassLoader getClassLoader();
    }

    /**
     * Serializable factory interface providing the Groovy CompilerConfiguration,
     * needed to restore the specific CompilerConfiguration after de-serialization
     */
    public interface CompilerConfigurationFactory extends Serializable {
        CompilerConfiguration getCompilerConfiguration();
    }

    public interface ScriptPreProcessor extends Serializable {
        String preProcess(String script);
    }

    /** Default CodeSource code base for the compiled Groovy scripts */
    public static final String DEFAULT_SCRIPT_CODE_BASE = "/groovy/scxml/script";

    /** Default factory for the Groovy parent ClassLoader, returning this class its ClassLoader */
    public static final ParentClassLoaderFactory DEFAULT_PARENT_CLASS_LOADER_FACTORY = new ParentClassLoaderFactory() {
        public ClassLoader getClassLoader() {
            return GroovyExtendableScriptCache.class.getClassLoader();
        }
    };

    /** Default factory for the Groovy CompilerConfiguration, returning a new and unmodified CompilerConfiguration instance */
    public static final CompilerConfigurationFactory DEFAULT_COMPILER_CONFIGURATION_FACTORY = new CompilerConfigurationFactory() {
        public CompilerConfiguration getCompilerConfiguration() {
            return new CompilerConfiguration();
        }
    };

    protected static class ScriptCacheElement implements Serializable {
        private static final long serialVersionUID = 1L;

        protected final String baseClass;
        protected final String scriptSource;
        protected String scriptName;
        protected transient Class<? extends Script> scriptClass;

        public ScriptCacheElement(String baseClass, String scriptSource) {
            this.baseClass = baseClass;
            this.scriptSource = scriptSource;
        }

        public String getBaseClass() {
            return baseClass;
        }

        public String getScriptSource() {
            return scriptSource;
        }

        public String getScriptName() {
            return scriptName;
        }

        public void setScriptName(String scriptName) {
            this.scriptName = scriptName;
        }

        public Class<? extends Script> getScriptClass() {
            return scriptClass;
        }

        public void setScriptClass(Class<? extends Script> scriptClass) {
            this.scriptClass = scriptClass;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final ScriptCacheElement that = (ScriptCacheElement) o;

            return !(baseClass != null ? !baseClass.equals(that.baseClass) : that.baseClass != null) &&
                    scriptSource.equals(that.scriptSource);

        }

        @Override
        public int hashCode() {
            int result = baseClass != null ? baseClass.hashCode() : 0;
            result = 31 * result + scriptSource.hashCode();
            return result;
        }
    }

    private final LinkedHashMap<ScriptCacheElement, ScriptCacheElement> scriptCache = new LinkedHashMap<ScriptCacheElement, ScriptCacheElement>();

    private String scriptCodeBase = DEFAULT_SCRIPT_CODE_BASE;
    private String scriptBaseClass;
    private ParentClassLoaderFactory parentClassLoaderFactory = DEFAULT_PARENT_CLASS_LOADER_FACTORY;
    private CompilerConfigurationFactory compilerConfigurationFactory = DEFAULT_COMPILER_CONFIGURATION_FACTORY;
    private ScriptPreProcessor scriptPreProcessor;

    /* non-serializable thus transient GroovyClassLoader and CompilerConfiguration */
    private transient GroovyClassLoader groovyClassLoader;
    private transient CompilerConfiguration compilerConfiguration;

    public GroovyExtendableScriptCache() {
    }

    /**
     * Hook into the de-serialization process, reloading the transient GroovyClassLoader, CompilerConfiguration and
     * re-generate Script classes through {@link #ensureInitializedOrReloaded()}
     */
    private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        in.defaultReadObject();
        ensureInitializedOrReloaded();
    }

    public ClassLoader getGroovyClassLoader() {
        return groovyClassLoader;
    }

    /**
     * @param scriptSource The script source, which will optionally be first preprocessed through {@link #preProcessScript(String)}
     *                     using the configured {@link #getScriptPreProcessor}
     * @return A new Script instance from a compiled (or cached) Groovy class parsed from the provided
     * scriptSource
     */
    public Script getScript(String scriptSource) {
        return getScript(null, scriptSource);
    }

    public Script getScript(String scriptBaseClass, String scriptSource) {
        Class<? extends Script> scriptClass;
        synchronized (scriptCache) {
            ensureInitializedOrReloaded();
            ScriptCacheElement cacheKey = new ScriptCacheElement(scriptBaseClass, scriptSource);
            ScriptCacheElement cacheElement = scriptCache.get(cacheKey);
            if (cacheElement != null) {
                scriptClass = cacheElement.getScriptClass();
            }
            else {
                String scriptName = generatedScriptName(scriptSource, scriptCache.size());
                scriptClass = compileScript(scriptBaseClass, scriptSource, scriptName);
                cacheKey.setScriptName(scriptName);
                cacheKey.setScriptClass(scriptClass);
                scriptCache.put(cacheKey, cacheKey);
            }
        }
        try {
            return scriptClass.newInstance();
        } catch (Exception e) {
            throw new GroovyRuntimeException("Failed to create Script instance for class: "+ scriptClass + ". Reason: " + e, e);
        }
    }

    protected void ensureInitializedOrReloaded() {
        if (groovyClassLoader == null) {
            compilerConfiguration = new CompilerConfiguration(getCompilerConfigurationFactory().getCompilerConfiguration());
            if (getScriptBaseClass() != null) {
                compilerConfiguration.setScriptBaseClass(getScriptBaseClass());
            }

            groovyClassLoader = AccessController.doPrivileged(new PrivilegedAction<GroovyClassLoader>() {
                public GroovyClassLoader run() {
                    return new GroovyClassLoader(getParentClassLoaderFactory().getClassLoader(), compilerConfiguration);
                }
            });
            if (!scriptCache.isEmpty()) {
                // de-serialized: need to re-generate all previously compiled scripts (this can cause a hick-up...):
                for (ScriptCacheElement element : scriptCache.keySet()) {
                    element.setScriptClass(compileScript(element.getBaseClass(), element.getScriptSource(), element.getScriptName()));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<Script> compileScript(final String scriptBaseClass, String scriptSource, final String scriptName) {
        final String script = preProcessScript(scriptSource);

        GroovyCodeSource codeSource = AccessController.doPrivileged(new PrivilegedAction<GroovyCodeSource>() {
            public GroovyCodeSource run() {
                return new GroovyCodeSource(script, scriptName, getScriptCodeBase());
            }
        });

        String currentScriptBaseClass = compilerConfiguration.getScriptBaseClass();
        try {
            if (scriptBaseClass != null) {
                compilerConfiguration.setScriptBaseClass(scriptBaseClass);
            }
            return groovyClassLoader.parseClass(codeSource, false);
        }
        finally {
            compilerConfiguration.setScriptBaseClass(currentScriptBaseClass);
        }
    }

    protected String preProcessScript(String scriptSource) {
        return getScriptPreProcessor() != null ? getScriptPreProcessor().preProcess(scriptSource) : scriptSource;
    }

    protected String generatedScriptName(String scriptSource, int seed) {
        return "script"+seed+"_"+Math.abs(scriptSource.hashCode())+".groovy";
    }

    /** @return The current configured CodeSource code base used for the compilation of the Groovy scripts */
    public String getScriptCodeBase() {
        return scriptCodeBase;
    }

    /**
     * @param scriptCodeBase The CodeSource code base to be used for the compilation of the Groovy scripts.<br/>
     *                             When null, of zero length or not (at least) starting with a '/',
     *                             the {@link #DEFAULT_SCRIPT_CODE_BASE} will be set instead.
     */
    @SuppressWarnings("unused")
    public void setScriptCodeBase(String scriptCodeBase) {
        if (scriptCodeBase != null && scriptCodeBase.length() > 0 && scriptCodeBase.charAt(0) == '/') {
            this.scriptCodeBase = scriptCodeBase;
        }
        else {
            this.scriptCodeBase = DEFAULT_SCRIPT_CODE_BASE;
        }
    }

    public String getScriptBaseClass() {
        return scriptBaseClass;
    }

    public void setScriptBaseClass(String scriptBaseClass) {
        this.scriptBaseClass = scriptBaseClass;
    }

    public ParentClassLoaderFactory getParentClassLoaderFactory() {
        return parentClassLoaderFactory;
    }

    @SuppressWarnings("unused")
    public void setParentClassLoaderFactory(ParentClassLoaderFactory parentClassLoaderFactory) {
        this.parentClassLoaderFactory = parentClassLoaderFactory != null ? parentClassLoaderFactory : DEFAULT_PARENT_CLASS_LOADER_FACTORY;
    }

    public CompilerConfigurationFactory getCompilerConfigurationFactory() {
        return compilerConfigurationFactory;
    }

    @SuppressWarnings("unused")
    public void setCompilerConfigurationFactory(CompilerConfigurationFactory compilerConfigurationFactory) {
        this.compilerConfigurationFactory = compilerConfigurationFactory != null ? compilerConfigurationFactory : DEFAULT_COMPILER_CONFIGURATION_FACTORY;
    }

    public ScriptPreProcessor getScriptPreProcessor() {
        return scriptPreProcessor;
    }

    public void setScriptPreProcessor(final ScriptPreProcessor scriptPreProcessor) {
        this.scriptPreProcessor = scriptPreProcessor;
    }

    public boolean isEmpty() {
        synchronized (scriptCache) {
            return scriptCache.isEmpty();
        }
    }
    public void clearCache() {
        synchronized (scriptCache) {
            scriptCache.clear();
            if (groovyClassLoader != null) {
                groovyClassLoader.clearCache();
            }
        }
    }
}
