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
package org.apache.commons.scxml2.model;

/**
 * XML DOM Node stored as text {@link ParsedValue} implementation.
 */
public class NodeTextValue implements ParsedValue {

    /**
     * the XML node as text
     */
    private String nodeText;

    public NodeTextValue(final String nodeText) {
        this.nodeText = nodeText;
    }

    @Override
    public final ValueType getType() {
        return ValueType.NODE_TEXT;
    }

    @Override
    public String getValue() {
        return nodeText;
    }
}
