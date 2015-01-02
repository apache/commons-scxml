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

package org.apache.commons.scxml2.env.javascript;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.SCXMLTestHelper;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.model.Action;
import org.apache.commons.scxml2.ActionExecutionContext;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

import org.junit.Assert;
import org.junit.Test;

/**
 * SCXML application for the example JavaScript scripts.
 *
 */
public class JSExampleTest {

    // TEST METHODS
    @Test
    public void testExample01Sample() throws Exception {

        List<CustomAction> actions  = new ArrayList<CustomAction>();        
        actions.add(new CustomAction("http://my.custom-actions.domain", "eventdatamaptest", EventDataMapTest.class));

        SCXML scxml = SCXMLTestHelper.parse("org/apache/commons/scxml2/env/javascript/example-01.xml", actions);
        SCXMLExecutor exec = SCXMLTestHelper.getExecutor(scxml);
        exec.go();
        Set<EnterableState> currentStates = exec.getStatus().getStates();
        Assert.assertEquals(1, currentStates.size());
        Assert.assertEquals("end", currentStates.iterator().next().getId());
    }

    // INNER CLASSES
    
    public static class EventDataMapTest extends Action {
        private static final long serialVersionUID = 1L;

        @Override
        public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
            exctx.getInternalIOProcessor().addEvent(new TriggerEvent("ok",TriggerEvent.SIGNAL_EVENT,"and its ok with me to"));
        }
    }

}

