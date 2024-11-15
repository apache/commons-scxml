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
 * The ExternalSCXMLIOProcessor is registered in the _ioprocessors system variable under the
 * {@link #DEFAULT_EVENT_PROCESSOR} key <strong>and</strong> maintains a {@link #getLocation() location} field
 * <pre>
 *     <em>whose value holds an address that external entities can use to communicate with this SCXML session using the SCXML Event I/O Processor.</em></pre>
 * @see <a href="https://www.w3.org/TR/scxml/#SCXMLEventProcessor">SCXML specification C.1.1 _ioprocessors Value</a>
 */
public class ExternalSCXMLIOProcessor implements SCXMLIOProcessor {

    private final SCXMLIOProcessor processor;

    public ExternalSCXMLIOProcessor(final SCXMLIOProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void addEvent(final TriggerEvent event) {
        processor.addEvent(event);
    }

    public String getLocation() {
        return SCXML_EVENT_PROCESSOR;
    }
}
