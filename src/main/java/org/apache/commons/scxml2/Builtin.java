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

import java.io.Serializable;

/**
 * Implementation of the SCXML specification required In() builtin predicate.
 */
public class Builtin implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Implements the In() predicate for SCXML documents. The method
     * name chosen is different since &quot;in&quot; is a reserved token
     * in some expression languages.
     *
     * Simple ID based comparator, assumes IDs are unique.
     *
     * @param ctx variable context
     * @param state The State ID to compare with
     * @return Whether this State is current active
     */
    @SuppressWarnings("unchecked")
    public static boolean isMember(final Context ctx, final String state) {
        return ((Status)ctx.getSystemContext().getPlatformVariables().get(SCXMLSystemContext.STATUS_KEY)).isInState(state);
    }
}

