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

/**
 * Plain Java Object {@link ParsedValue} implementation mapped from a json string
 * <p>
 * For serialization back to XML flag {@link #isCDATA()} is recorded to wrap the json string in a CDATA section if needed.
 * </p>
 */
public class JsonValue implements ParsedValue {

    /**
     * The Java Object mapped from a json string
     */
    private final Object jsonObject;

    /**
     * The flag indicating if the json string requires wrapping in a CDATA section when writing out to XML
     */
    private final boolean cdata;

    public JsonValue(final Object jsonObject, final boolean cdata) {
        this.jsonObject = jsonObject;
        this.cdata = cdata;
    }

    @Override
    public final ValueType getType() {
        return ValueType.JSON;
    }

    @Override
    public final Object getValue() {
        return jsonObject;
    }

    /**
     * @return true if the json string requires wrapping in a CDATA section when writing out to XML
     */
    public final boolean isCDATA() {
        return cdata;
    }
}
