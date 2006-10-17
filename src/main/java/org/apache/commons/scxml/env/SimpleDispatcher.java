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
package org.apache.commons.scxml.env;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml.EventDispatcher;

/**
 * Trivial EventDispatcher implementation.
 * No remote eventing.
 *
 */
public final class SimpleDispatcher implements EventDispatcher, Serializable {

     /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Implementation independent log category. */
     private Log log = LogFactory.getLog(EventDispatcher.class);

    /**
     *  Constructor.
     */
    public SimpleDispatcher() {
        super();
    }

    /**
     * @see EventDispatcher#cancel(String)
     */
    public void cancel(final String sendId) {
        if (log.isInfoEnabled()) {
            log.info("cancel( sendId: " + sendId + ")");
        }
    }

    /**
    @see EventDispatcher#send(String,String,String,String,Map,Object,long,List)
     */
    public void send(final String sendId, final String target,
            final String targetType, final String event, final Map params,
            final Object hints, final long delay, final List externalNodes) {
        if (log.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("send ( sendId: ").append(sendId);
            buf.append(", target: ").append(target);
            buf.append(", targetType: ").append(targetType);
            buf.append(", event: ").append(event);
            buf.append(", params: ").append(String.valueOf(params));
            buf.append(", hints: ").append(String.valueOf(hints));
            buf.append(", delay: ").append(delay);
            buf.append(')');
            log.info(buf.toString());
        }

    }

}

