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

import org.apache.commons.scxml2.invoke.Invoker;
import org.apache.commons.scxml2.invoke.InvokerException;
import org.apache.commons.scxml2.model.Invoke;

/**
 * InvokerManager provides the ability to an Invoke action to
 * create and register an active Invoker instance
 */
public interface InvokerManager {

    /**
     * Create a new {@link Invoker}
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     *         invoker class is registered against that type,
     *         <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot be instantiated.
     */
    Invoker newInvoker(final String type) throws InvokerException;

    /**
     * Registers the active {@link Invoker} for an {@link Invoke}
     *
     * @param invoke The Invoke.
     * @param invoker The Invoker.
     * @throws InvokerException when the Invoker doesn't have an invokerId
     */
    void registerInvoker(final Invoke invoke, final Invoker invoker) throws InvokerException;
}
