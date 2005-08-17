/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.taglibs.rdc.scxml.env;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.rdc.scxml.EventDispatcher;

/**
 * Trivial EventDispatcher implementation. No remote eventing.
 * 
 * @author Jaroslav Gergic
 */
public class SimpleDispatcher implements EventDispatcher {
    
     private static Log log = LogFactory.getLog(EventDispatcher.class);

    /**
     *  Constructor
     */
    public SimpleDispatcher() {
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.EventDispatcher#cancel(java.lang.String)
     */
    public void cancel(String sendId) {
        if(log.isInfoEnabled()) {
            log.info("cancel( sendId: " + sendId + ")");
        }
    }

    /**
     * @see org.apache.taglibs.rdc.scxml.EventDispatcher#send(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.lang.Object, long)
     */
    public void send(String sendId, String target, String targetType,
            String event, Map params, Object hints, long delay) {
        if(log.isInfoEnabled()) {
            StringBuffer buf = new StringBuffer(32);
            buf.append("send ( sendId: ").append(sendId);
            buf.append(", target: ").append(target);
            buf.append(", targetType: ").append(targetType);
            buf.append(", event: ").append(event);
            buf.append(", params: ").append(String.valueOf(params.toString()));
            buf.append(", hints: ").append(String.valueOf(hints.toString()));
            buf.append(", delay: ").append(delay);
            buf.append(')');
            log.info(buf.toString());
        }

    }

}
