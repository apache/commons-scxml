/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class HistoryTest extends TestCase {

    public HistoryTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(HistoryTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { HistoryTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    private History history;
    
    public void setUp() {
        history = new History();
    }
    
    public void testSetTypeDeep() {
        history.setType("deep");
        
        assertTrue(history.isDeep());
    }
    
    public void testSetTypeNotDeep() {
        history.setType("shallow");
        
        assertFalse(history.isDeep());
    }
}
