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
package org.apache.taglibs.rdc.scxml;

/**
 * Interface that allows an entity within an SCXML document to have
 * listeners attached to itself so they may be informed of events within
 * this entity's realm.   
 * 
 * @author Jaroslav Gergic
 * @author Rahul Akolkar
 */
public interface Observable {
    
    /**
     * Add this SCXMLListener to the list of listeners associated
     * with this SCXML model entity
     * 
     * @param lst The listener to be added
     */
    public void addListener(SCXMLListener lst);

    /**
     * Remove this SCXMLListener from the list of listeners associated
     * with this SCXML model entity.
     * 
     * @param lst The listener to be removed
     */
    public void removeListener(SCXMLListener lst);

}
