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
 * The SCXML I/O Processor provides the interface for either an internal process or an external system or invoked child
 * SCXML process ({@link org.apache.commons.scxml2.invoke.Invoker}) to send events into the SCXML processor queue.
 */
public interface SCXMLIOProcessor {

    /**
     * The name of the default SCXML I/O Event Processor
     */
    String DEFAULT_EVENT_PROCESSOR = "http://www.w3.org/TR/scxml/#SCXMLEventProcessor";

    /**
     * Prefix for SCXML I/O Event Processor aliases
     */
    String EVENT_PROCESSOR_ALIAS_PREFIX = "#_";

    /**
     * Default SCXML I/O Event Processor alias
     */
    String SCXML_EVENT_PROCESSOR = "scxml";

    /**
     * The name of the internal Event Processor
     */
    String INTERNAL_EVENT_PROCESSOR = EVENT_PROCESSOR_ALIAS_PREFIX + "internal";

    /**
     * The name of the parent Event Processor
     */
    String PARENT_EVENT_PROCESSOR = EVENT_PROCESSOR_ALIAS_PREFIX + "parent";

    /**
     * Send an event into the SCXML processor queue
     * <p>
     * @param event the event to send
     */
    void addEvent(TriggerEvent event);
}
