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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The class in this SCXML object model that corresponds to the SCXML &lt;donedata&gt; element.
 */
public class DoneData implements ContentContainer, ParamsContainer, Serializable {

    /**
     * The &lt;content/&gt; of this send
     */
    private Content content;

    /**
     * The List of the params to be sent
     */
    private final List<Param> paramsList = new ArrayList<>();

    /**
     * Returns the content
     *
     * @return the content
     */
    public Content getContent() {
        return content;
    }

    /**
     * Sets the content
     *
     * @param content the content to set
     */
    public void setContent(final Content content) {
        this.content = content;
    }

    /**
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    public List<Param> getParams() {
        return paramsList;
    }

}
