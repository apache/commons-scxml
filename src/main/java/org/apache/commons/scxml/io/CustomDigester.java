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
package org.apache.commons.scxml.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.digester.Digester;

/**
 * <p><code>Digester</code> that exposes a <code>getCurrentNamespaces()</code>
 * method, for capturing namespace snapshots.</p>
 *
 * @deprecated Will be removed after the 1.8 release of Commons Digester.
 */
final class CustomDigester extends Digester {

    /**
     * Get the most current namespaces for all prefixes.
     *
     * @return Map A map with namespace prefixes as keys and most current
     *             namespace URIs for the corresponding prefixes as values
     *
     */
    public Map getCurrentNamespaces() {
        if (!namespaceAware) {
            log.warn("Digester is not namespace aware");
        }
        Map currentNamespaces = new HashMap();
        Iterator nsIterator = namespaces.entrySet().iterator();
        while (nsIterator.hasNext()) {
            Map.Entry nsEntry = (Map.Entry) nsIterator.next();
            try {
                currentNamespaces.put(nsEntry.getKey(),
                    ((ArrayStack) nsEntry.getValue()).peek());
            } catch (RuntimeException e) {
                // rethrow, after logging
                log.error(e.getMessage(), e);
                throw e;
            }
        }
        return currentNamespaces;
    }

}

