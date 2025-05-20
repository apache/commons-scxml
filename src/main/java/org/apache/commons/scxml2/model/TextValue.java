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
 * Plain text {@link ParsedValue} implementation.
 * <p>
 * For serialization back to XML flag {@link #isCDATA()} is recorded to wrap the text in a CDATA section if needed.
 * </p>
 */
public class TextValue implements ParsedValue {

    /**
     * The text value
     */
    private final String text;

    /**
     * The flag indicating if the text requires wrapping in a CDATA section when writing out to XML
     */
    private final boolean cdata;

    public TextValue(final String text, final boolean cdata) {
        this.text = text;
        this.cdata = cdata;
    }

    @Override
    public final ValueType getType() {
        return ValueType.TEXT;
    }

    @Override
    public final String getValue() {
        return text;
    }

    /**
     * @return true if the text requires wrapping in a CDATA section when writing out to XML
     */
    public final boolean isCDATA() {
        return cdata;
    }
}
