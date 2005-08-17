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
package org.apache.commons.scxml.model;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;cancel&gt; SCXML element.
 * 
 */
public class Cancel extends Action {
    
    /**
     * Constructor
     */
    public Cancel() {
        super();
    }
    
    /**
     * The ID of the send message that should be cancelled.
     */
    private String sendId;

    /**
     * Get the ID of the send message that should be cancelled.
     * 
     * @return Returns the sendId.
     */
    public String getSendId() {
        return sendId;
    }
    
    /**
     * Set the ID of the send message that should be cancelled.
     * 
     * @param sendId The sendId to set.
     */
    public void setSendId(String sendId) {
        this.sendId = sendId;
    }    
    
}
