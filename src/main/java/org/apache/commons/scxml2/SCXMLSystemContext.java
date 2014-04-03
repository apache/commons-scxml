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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The SCXMLSystemContext is used as a read only Context wrapper
 * and provides the SCXML (read only) system variables which are injected via the unwrapped {@link #getContext()}.
 *
 * @see <a href="http://www.w3.org/TR/scxml/#SystemVariables">http://www.w3.org/TR/scxml/#SystemVariables</a>
 */
public class SCXMLSystemContext implements Context, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The protected system variables names as defined in the SCXML specification
     * @see <a href="http://www.w3.org/TR/scxml/#SystemVariables">http://www.w3.org/TR/scxml/#SystemVariables</a>
     */
    public static final String EVENT_KEY = "_event";
    public static final String SESSIONID_KEY = "_sessionid";
    public static final String SCXML_NAME_KEY = "_name";
    public static final String IOPROCESSORS_KEY = "_ioprocessors";
    public static final String X_KEY = "_x";

    /**
     * Commons SCXML internal system variable holding the current SCXML configuration of all (including ancestors)
     * active states.
     */
    public static final String ALL_STATES_KEY = "_ALL_STATES";

    /**
     * The set of protected system variables names
     */
    private static final Set<String> PROTECTED_NAMES = new HashSet<String>(Arrays.asList(
            new String[] {EVENT_KEY, SESSIONID_KEY, SCXML_NAME_KEY, IOPROCESSORS_KEY, X_KEY, ALL_STATES_KEY}
    ));

    /**
     * The wrapped system context
     */

    private Context systemContext;

    /**
     * Initialize or replace systemContext
     * @param systemContext the system context to set
     */
    void setSystemContext(Context systemContext) {
        if (this.systemContext != null) {
            // replace systemContext
            systemContext.getVars().putAll(this.systemContext.getVars());
        }
        this.systemContext = systemContext;
        this.protectedVars = Collections.unmodifiableMap(systemContext.getVars());
    }

    /**
     * The unmodifiable wrapped variables map from the wrapped system context
     */
    private Map<String, Object> protectedVars;

    public SCXMLSystemContext(Context systemContext) {
        setSystemContext(systemContext);
    }

    @Override
    public void set(final String name, final Object value) {
        if (PROTECTED_NAMES.contains(name)) {
            throw new UnsupportedOperationException();
        }
        // non-protected variables are set on the parent of the system context (e.g. root context)
        systemContext.getParent().set(name, value);
    }

    @Override
    public void setLocal(final String name, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(final String name) {
        return systemContext.get(name);
    }

    @Override
    public boolean has(final String name) {
        return systemContext.has(name);
    }

    @Override
    public boolean hasLocal(final String name) {
        return systemContext.hasLocal(name);
    }

    @Override
    public Map<String, Object> getVars() {
        return protectedVars;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getParent() {
        return systemContext.getParent();
    }

    /**
     * @return Returns the wrapped (modifiable) system context
     */
    Context getContext() {
        return systemContext;
    }
}
