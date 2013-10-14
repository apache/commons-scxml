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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.commons.scxml.SCXMLTestHelper;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.State;

public class SCXMLWriterTest extends TestCase {

    public SCXMLWriterTest(String testName) {
        super(testName);
    }

    public void testSerializeSCXMLNoStates() throws IOException, XMLStreamException {
        SCXML scxml = new SCXML();
        Map<String, String> namespaces = new LinkedHashMap<String, String>();
        namespaces.put("", "http://www.w3.org/2005/07/scxml");
        namespaces.put("cs", "http://commons.apache.org/scxml");
        namespaces.put("foo", "http://f.o.o");
        namespaces.put("bar", "http://b.a.r");
        scxml.setNamespaces(namespaces);
        scxml.setVersion("version1");
        scxml.setInitial("off");
        scxml.addChild(new State());
        
        String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"http://commons.apache.org/scxml\" "
            + "xmlns:foo=\"http://f.o.o\" xmlns:bar=\"http://b.a.r\" "
            + "version=\"version1\" initial=\"off\">\n<!--http://commons.apache.org/scxml-->\n<state/>\n"
            + "</scxml>\n";

        assertEquals(assertValue, SCXMLTestHelper.removeCarriageReturns(SCXMLWriter.write(scxml)));
    }

    public void testSerializeSCXMLState() throws IOException, XMLStreamException {
        SCXML scxml = new SCXML();
        Map<String, String> namespaces = new LinkedHashMap<String, String>();
        scxml.setNamespaces(namespaces);
        scxml.setVersion("1.0");
        scxml.setInitial("S1");

        State s1 = new State();
        s1.setId("S1");

        scxml.addChild(s1);

        String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" "
            + "xmlns:cs=\"http://commons.apache.org/scxml\" version=\"1.0\" initial=\"S1\">\n"
            + "<!--http://commons.apache.org/scxml-->\n<state id=\"S1\"/>\n</scxml>\n";

        assertEquals(assertValue, SCXMLTestHelper.removeCarriageReturns(SCXMLWriter.write(scxml)));
    }

    public void testSerializeParallel() throws IOException, XMLStreamException {

        SCXML scxml = new SCXML();
        Map<String, String> namespaces = new LinkedHashMap<String, String>();
        scxml.setNamespaces(namespaces);
        scxml.setVersion("1.0");
        scxml.setInitial("par");

        Parallel par = new Parallel();
        par.setId("par");

        State s1 = new State();
        s1.setId("S1");

        State s11 = new State();
        s11.setId("S11");

        s1.addChild(s11);

        State s2 = new State();
        s2.setId("S2");

        State s21 = new State();
        s21.setId("S21");

        s2.addChild(s21);

        par.addChild(s1);
        par.addChild(s2);

        scxml.addChild(par);

        String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"http://commons.apache.org/scxml\" "
            + "version=\"1.0\" initial=\"par\">\n"
            + "<!--http://commons.apache.org/scxml-->\n"
            + "<parallel id=\"par\">\n"
            + "<state id=\"S1\">\n"
            + "<state id=\"S11\"/>\n"
            + "</state>\n"
            + "<state id=\"S2\">\n"
            + "<state id=\"S21\"/>\n"
            + "</state>\n"
            + "</parallel>\n"
            + "</scxml>\n";

        assertEquals(assertValue, SCXMLTestHelper.removeCarriageReturns(SCXMLWriter.write(scxml)));
     }

}
