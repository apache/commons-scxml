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

import java.util.Comparator;

/**
 * DocumentOrder is implemented by {@link EnterableState} and {@link Transition} elements in the SCXML document
 * representing their document order,
 * <p>
 *   They are ordered with ancestor states before their descendant states,
 *   and the transitions within a state in document order before any descendant states.
 * </p>
 * <p>Note: it is assumed there will be no more than Integer.MAX_VALUE of such elements in a single SCXML document</p>
 */
public interface DocumentOrder {

    Comparator<DocumentOrder> documentOrderComparator = new Comparator<DocumentOrder>() {
        @Override
        public int compare(final DocumentOrder o1, final DocumentOrder o2) {
            return o1.getOrder() - o2.getOrder();
        }
    };

    Comparator<DocumentOrder> reverseDocumentOrderComparator = new Comparator<DocumentOrder>() {
        @Override
        public int compare(final DocumentOrder o1, final DocumentOrder o2) {
            return o2.getOrder() - o1.getOrder();
        }
    };

    /**
     * @return the relative document order within the SCXML document of this element
     */
    int getOrder();
}
