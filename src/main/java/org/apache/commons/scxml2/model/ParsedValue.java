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
package org.apache.commons.scxml2.model;

import java.io.Serializable;

/**
 * A {@code ParsedValue} holds the parsed content of the body of a {@link ParsedValueContainer},
 * or from an external src for &lt;data&gt; or &lt;assign&gt;.
 * <p>Supported types are defined by enum {@link ValueType}, each of which have a specific implementation:
 * <ul>
 *   <li>{@link TextValue}</li>
 *   <li>{@link JsonValue}</li>
 *   <li>{@link NodeValue}</li>
 *   <li>{@link NodeListValue}</li>
 *   <li>{@link NodeTextValue}</li>
 * </ul>
 * For a &lt;invoke&gt; &lt;content&gt; body the special {@link NodeTextValue} implementation is used,
 * which stored the (only supported) embedded &lt;scxml&gt; document as plain XML text for the &lt;invoke&gt;
 * execution to parse (again) at runtime.
 */
public interface ParsedValue extends Serializable {

    /**
     * The supported body value types
     */
    enum ValueType {
        TEXT,
        JSON,
        NODE,
        NODE_LIST,
        NODE_TEXT
    }

    /**
     * @return the parsed value type
     */
    ValueType getType();

    /**
     * @return the parsed value
     */
    Object getValue();
}
