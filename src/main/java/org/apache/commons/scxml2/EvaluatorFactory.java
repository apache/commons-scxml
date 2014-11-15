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
package org.apache.commons.scxml2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.scxml2.env.groovy.GroovyEvaluator;
import org.apache.commons.scxml2.env.javascript.JSEvaluator;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.env.minimal.MinimalEvaluator;
import org.apache.commons.scxml2.env.xpath.XPathEvaluator;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import static org.apache.commons.scxml2.Evaluator.DEFAULT_DATA_MODEL;

/**
 * A static singleton factory for {@link EvaluatorProvider}s by supported SCXML datamodel type.
 * <p>
 *  The EvaluatorFactory is used to automatically create an {@link Evaluator} instance for an SCXML
 *  statemachine when none has been pre-defined and configured for the {@link SCXMLExecutor}.
 * </p>
 * <p>
 *  The builtin supported providers are:
 *  <ul>
 *      <li>no or empty datamodel (default) or datamodel="jexl": {@link JexlEvaluator.JexlEvaluatorProvider}</li>
 *      <li>datamodel="ecmascript": {@link JSEvaluator.JSEvaluatorProvider}</li>
 *      <li>datamodel="groovy": {@link GroovyEvaluator.GroovyEvaluatorProvider}</li>
 *      <li>datamodel="xpath": {@link XPathEvaluator.XPathEvaluatorProvider}</li>
 *      <li>datamodel="null": {@link MinimalEvaluator.MinimalEvaluatorProvider}</li>
 *  </ul>
 *  </p>
 *  <p>
 *  For adding additional or overriding the builtin Evaluator implementations use
 *  {@link #registerEvaluatorProvider(EvaluatorProvider)} or {@link #unregisterEvaluatorProvider(String)}.
 *  </p>
 *  <p>
 *  The default provider can be overridden using the {@link #setDefaultProvider(EvaluatorProvider)} which will
 *  register the provider under the {@link Evaluator#DEFAULT_DATA_MODEL} ("") value for the datamodel.<br/>
 *  Note: this is <em>not</em> the same as datamodel="null"!
 * </p>
 */
public class EvaluatorFactory {

    private static EvaluatorFactory INSTANCE = new EvaluatorFactory();

    private final Map<String, EvaluatorProvider> providers = new ConcurrentHashMap<String, EvaluatorProvider>();

    private EvaluatorFactory() {
        providers.put(XPathEvaluator.SUPPORTED_DATA_MODEL, new XPathEvaluator.XPathEvaluatorProvider());
        providers.put(JSEvaluator.SUPPORTED_DATA_MODEL, new JSEvaluator.JSEvaluatorProvider());
        providers.put(GroovyEvaluator.SUPPORTED_DATA_MODEL, new GroovyEvaluator.GroovyEvaluatorProvider());
        providers.put(JexlEvaluator.SUPPORTED_DATA_MODEL, new JexlEvaluator.JexlEvaluatorProvider());
        providers.put(MinimalEvaluator.SUPPORTED_DATA_MODEL, new MinimalEvaluator.MinimalEvaluatorProvider());
        providers.put(DEFAULT_DATA_MODEL, providers.get(JexlEvaluator.SUPPORTED_DATA_MODEL));
    }

    public static void setDefaultProvider(EvaluatorProvider defaultProvider) {
        INSTANCE.providers.put(DEFAULT_DATA_MODEL, defaultProvider);
    }

    @SuppressWarnings("unused")
    public static EvaluatorProvider getDefaultProvider() {
        return INSTANCE.providers.get(DEFAULT_DATA_MODEL);
    }

    @SuppressWarnings("unused")
    public static EvaluatorProvider getEvaluatorProvider(String datamodelName) {
        return INSTANCE.providers.get(datamodelName == null ? DEFAULT_DATA_MODEL : datamodelName);
    }

    @SuppressWarnings("unused")
    public static void registerEvaluatorProvider(EvaluatorProvider provider) {
        INSTANCE.providers.put(provider.getSupportedDatamodel(), provider);
    }

    @SuppressWarnings("unused")
    public static void unregisterEvaluatorProvider(String datamodelName) {
        INSTANCE.providers.remove(datamodelName == null ? DEFAULT_DATA_MODEL : datamodelName);
    }

    /**
     * Returns a dedicated Evaluator instance for a specific SCXML document its documentmodel.
     * <p>If no SCXML document is provided a default Evaluator will be returned.</p>
     * @param document The document to return a dedicated Evaluator for. May be null to retrieve the default Evaluator.
     * @return a new and not sharable Evaluator instance for the provided document, or a default Evaluator otherwise
     * @throws ModelException If the SCXML document datamodel is not supported.
     */
    public static Evaluator getEvaluator(SCXML document) throws ModelException {
        String datamodelName = document != null ? document.getDatamodelName() : null;
        EvaluatorProvider provider = INSTANCE.providers.get(datamodelName == null ? DEFAULT_DATA_MODEL : datamodelName);
        if (provider == null) {
            throw new ModelException("Unsupported SCXML document datamodel \""+(datamodelName)+"\"");
        }
        return document != null ? provider.getEvaluator(document) : provider.getEvaluator();
    }
}
