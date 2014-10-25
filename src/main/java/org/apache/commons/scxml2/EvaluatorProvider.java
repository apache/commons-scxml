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

import org.apache.commons.scxml2.model.SCXML;

/**
 * An EvaluatorProvider provides an {@link Evaluator} instance for a specific SCXML datamodel type.
 */
public interface EvaluatorProvider {

    /**
     * @return The SCXML datamodel type this provider supports
     */
    String getSupportedDatamodel();

    /**
     * @return a default or generic {@link Evaluator} supporting the {@link #getSupportedDatamodel()}
     */
    Evaluator getEvaluator();

    /**
     * Factory method to return a dedicated and optimized {@link Evaluator} instance for a specific SCXML document.
     * <p>
     * As the returned Evaluator <em>might</em> be optimized and dedicated for the SCXML document instance, the
     * Evaluator may not be shareable and reusable for other SCXML documents.
     * </p>
     * @param document the SCXML document
     * @return a new and not sharable Evaluator instance
     */
    Evaluator getEvaluator(SCXML document);
}
