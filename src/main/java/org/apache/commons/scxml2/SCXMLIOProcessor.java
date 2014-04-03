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

/**
 * The SCXML I/O Processor provides the interface for an external system or invoked child SCXML process
 * ({@link org.apache.commons.scxml2.invoke.Invoker}) to asynchronously send events to the SCXMLExecutor.
 */
public interface SCXMLIOProcessor {

    /**
     * Send an asynchronous event to the SCXMLExecutor
     * <p>
     * @param event the event to send
     */
    void addEvent(TriggerEvent event);
}
