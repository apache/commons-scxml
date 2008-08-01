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
package org.apache.commons.scxml.io;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Assign;
import org.apache.commons.scxml.model.Cancel;
import org.apache.commons.scxml.model.Else;
import org.apache.commons.scxml.model.ElseIf;
import org.apache.commons.scxml.model.Exit;
import org.apache.commons.scxml.model.If;
import org.apache.commons.scxml.model.Log;
import org.apache.commons.scxml.model.OnEntry;
import org.apache.commons.scxml.model.OnExit;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.model.Var;

public class SCXMLSerializerTest extends TestCase {

    public SCXMLSerializerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(SCXMLSerializerTest.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { SCXMLSerializerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }
    
    public void testSerializeSCXMLNoStates() {
        SCXML scxml = new SCXML();
        scxml.setVersion("version1");
        scxml.setInitial("off");
        scxml.addChild(new State());
        
        String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"http://commons.apache.org/scxml\" "
            + "version=\"version1\" initial=\"off\">\n <state>\n </state>\n</scxml>\n";
        
        assertEquals(assertValue, SCXMLSerializer.serialize(scxml));
    }
    
    public void testSerializeSend() {
        Send send = new Send();
        send.setSendid("1");
        send.setTarget("newTarget");
        send.setTargettype("newTargetType");
        send.setNamelist("names");
        send.setDelay("4s");
        send.setEvent("turnoff");
        send.setHints("guess");
        
        String assertValue = " <send sendid=\"1\" " +
                "target=\"newTarget\" " +
                "targetType=\"newTargetType\" " +
                "namelist=\"names\" " +
                "delay=\"4s\" " +
                "event=\"turnoff\" " +
                "hints=\"guess\">\n </send>\n";
        
        StringBuffer returnValue = new StringBuffer(); 
        SCXMLSerializer.serializeSend(returnValue, send, " "); 
        
        assertEquals(assertValue.toString(), returnValue.toString());
    }

    public void testSerializeActionsListNull() {
        TransitionTarget target = new State();
        target.setId("1");
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, null, " ");
        
        assertFalse(returnBoolean);
        assertEquals(0, returnValue.length());
    }
    
    public void testSerializeActionsVar() {
        Var var = new Var();
        var.setName("newName");
        var.setExpr("newExpression");
        
        List<Action> values = new ArrayList<Action>();
        values.add(var);
        
        String actualValue = " <cs:var name=\"newName\" expr=\"newExpression\"/>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeActionsAssign() {
        Assign assign = new Assign();
        assign.setName("newName");
        assign.setExpr("newExpression");
        
        List<Action> values = new ArrayList<Action>();
        values.add(assign);
        
        String actualValue = " <assign name=\"newName\" expr=\"newExpression\"/>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeActionsCancel() {
        Cancel cancel = new Cancel();
        cancel.setSendid("1");
        
        List<Action> values = new ArrayList<Action>();
        values.add(cancel);
        
        String actualValue = " <cancel sendid=\"1\"/>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeActionsLog() {
        Log log = new Log();
        log.setExpr("newExpression");
        
        List<Action> values = new ArrayList<Action>();
        values.add(log);
        
        String actualValue = " <log expr=\"newExpression\"/>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeActionsExit() {
        Exit exit = new Exit();
        exit.setExpr("newExpression");
        exit.setNamelist("names");
        
        List<Action> values = new ArrayList<Action>();
        values.add(exit);
        
        String actualValue = " <cs:exit expr=\"newExpression\" namelist=\"names\"/>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertTrue(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeActionsElse() {
        Else elseValue = new Else();
        
        List<Action> values = new ArrayList<Action>();
        values.add(elseValue);
        
        String actualValue = " <else/>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeActionsElseIf() {
        ElseIf elseIf = new ElseIf();
        elseIf.setCond("newCondition");
        
        List<Action> values = new ArrayList<Action>();
        values.add(elseIf);
        
        String actualValue = " <elseif cond=\"newCondition\" />\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeIf() {
        If ifValue = new If();
        ifValue.setCond("newCondition");
        
        List<Action> values = new ArrayList<Action>();
        values.add(ifValue);
        
        String actualValue = " <if cond=\"newCondition\">\n </if>\n";
        
        StringBuffer returnValue = new StringBuffer();
        boolean returnBoolean = SCXMLSerializer.serializeActions(returnValue, values, " ");
        
        assertFalse(returnBoolean);
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeOnEntrySizeZero() {
        TransitionTarget target = new State();
        target.setOnEntry(new OnEntry());

        String actualValue = "";

        StringBuffer returnValue = new StringBuffer();
        SCXMLSerializer.serializeOnEntry(returnValue, target, " ");
        
        assertEquals(actualValue, returnValue.toString());
    }

    public void testSerializeOnEntry() {
        TransitionTarget target = new State();
        
        OnEntry onEntry = new OnEntry();
        onEntry.addAction(new Else());
        
        target.setOnEntry(onEntry);

        String actualValue = " <onentry>\n  <else/>\n </onentry>\n";

        StringBuffer returnValue = new StringBuffer();
        SCXMLSerializer.serializeOnEntry(returnValue, target, " ");
        
        assertEquals(actualValue, returnValue.toString());
    }
    
    public void testSerializeOnExitSizeZero() {
        TransitionTarget target = new State();
        target.setOnExit(new OnExit());

        String actualValue = "";

        StringBuffer returnValue = new StringBuffer();
        SCXMLSerializer.serializeOnExit(returnValue, target, " ");
        
        assertEquals(actualValue, returnValue.toString());
    }

    public void testSerializeOnExit() {
        TransitionTarget target = new State();
        
        OnExit onExit = new OnExit();
        onExit.addAction(new Else());
        
        target.setOnExit(onExit);

        String actualValue = " <onexit>\n  <else/>\n </onexit>\n";

        StringBuffer returnValue = new StringBuffer();
        SCXMLSerializer.serializeOnExit(returnValue, target, " ");
        
        assertEquals(actualValue, returnValue.toString());
    }

    public void testSerializeTransitionEscapeXML() {
        Transition t = new Transition();

        // note: the '<' char has to be escaped to "&lt;" to create valid XML
        t.setCond("i < 3");

        String actualValue = "<transition cond=\"i &lt; 3\">\n</transition>\n";

        StringBuffer returnValue = new StringBuffer();
        SCXMLSerializer.serializeTransition(returnValue, t, "");

        assertEquals(actualValue, returnValue.toString());
    }

    public void testSerializeSCXMLState() {
        SCXML scxml = new SCXML();
        scxml.setVersion("1.0");
        scxml.setInitial("S1");

        State s1 = new State();
        s1.setId("S1");

        scxml.addChild(s1);

        String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"http://commons.apache.org/scxml\" "
            + "version=\"1.0\" initial=\"S1\">\n <state id=\"S1\">\n </state>\n</scxml>\n";

        assertEquals(assertValue, SCXMLSerializer.serialize(scxml));
    }

    public void testSerializeParallel() {

        SCXML scxml = new SCXML();
        scxml.setVersion("1.0");
        scxml.setInitial("par");

        Parallel par = new Parallel();
        par.setId("par");

        State s1 = new State();
        s1.setId("S1");

        State s11 = new State();
        s11.setId("S11");

        s1.addChild((TransitionTarget)s11);

        State s2 = new State();
        s2.setId("S2");

        State s21 = new State();
        s21.setId("S21");

        s2.addChild((TransitionTarget)s21);

        par.addChild((TransitionTarget)s1);
        par.addChild((TransitionTarget)s2);

        scxml.addChild(par);

        String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"http://commons.apache.org/scxml\" "
            + "version=\"1.0\" initial=\"par\">\n"
            + " <parallel id=\"par\">\n"
            + "  <state id=\"S1\">\n"
            + "   <state id=\"S11\">\n"
            + "   </state>\n"
            + "  </state>\n"
            + "  <state id=\"S2\">\n"
            + "   <state id=\"S21\">\n"
            + "   </state>\n"
            + "  </state>\n"
            + " </parallel>\n"
            + "</scxml>\n";

        String s = SCXMLSerializer.serialize(scxml);

        assertEquals(assertValue, s);
     }

}
