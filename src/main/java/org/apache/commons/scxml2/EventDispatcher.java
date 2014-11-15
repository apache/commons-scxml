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

import java.util.Map;

/**
 * The event controller interface used to send messages containing
 * events or other information directly to another SCXML Interpreter,
 * other external systems using an Event I/O Processor or to raise
 * events in the current SCXML session.
 *
 */
public interface EventDispatcher {

    /**
     * Cancel the specified send message.
     *
     * @param sendId The ID of the send message to cancel
     */
    void cancel(String sendId);

    /**
     * Send this message to the target.
     *
     * @param ioProcessors the available SCXMLIOProcessors, the same map as the SCXML system variable _ioprocessors
     * @param id The ID of the send message
     * @param target An expression returning the target location of the event
     * @param type The type of the Event I/O Processor that the event should
     *  be dispatched to
     * @param event The type of event being generated.
     * @param data The event payload
     * @param hints The data containing information which may be
     *  used by the implementing platform to configure the event processor
     * @param delay The event is dispatched after the delay interval elapses
     */
    void send(Map<String, SCXMLIOProcessor> ioProcessors, String id, String target, String type, String event,
              Object data, Object hints, long delay);

}

