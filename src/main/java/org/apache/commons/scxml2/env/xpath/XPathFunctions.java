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
package org.apache.commons.scxml2.env.xpath;

import java.util.Map;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.scxml2.SCXMLSystemContext;
import org.apache.commons.scxml2.Status;

/**
 * Commons JXPath custom extension function providing the SCXML In() predicate
 */
public class XPathFunctions {

    /**
     * Provides the SCXML standard In() predicate for SCXML documents.
     * @param expressionContext The context currently in use for evaluation
     * @param state The State ID to compare with
     * @return true if this state is currently active
     */
    @SuppressWarnings("unchecked")
    public static boolean In(ExpressionContext expressionContext, String state) {
        Variables variables = expressionContext.getJXPathContext().getVariables();
        Map<String,Object> platformVariables = (Map<String, Object>)variables.getVariable(SCXMLSystemContext.X_KEY);
        return ((Status)platformVariables.get(SCXMLSystemContext.STATUS_KEY)).isInState(state);
    }
}
