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
package org.apache.commons.scxml2.io;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.scxml2.model.CommonsSCXML;
import org.apache.commons.scxml2.model.Parallel;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Script;
import org.apache.commons.scxml2.model.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

public class SCXMLWriterTest {

    @Test
    public void testSerializeGlobalScript() throws IOException, XMLStreamException {
        final SCXML scxml = new CommonsSCXML();
        scxml.setVersion("1.0");
        scxml.setInitial("S1");

        final Script script = new Script();
        script.setGlobalScript(true);
        script.setScript("foo=\"abc\"");
        scxml.setGlobalScript(script);

        final State s1 = new State();
        s1.setId("S1");

        scxml.addChild(s1);

        final String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" "
                + "xmlns:cs=\"https://commons.apache.org/scxml\" version=\"1.0\" initial=\"S1\">"
                + "<!--https://commons.apache.org/scxml--><script><![CDATA[foo=\"abc\"]]></script><state id=\"S1\"></state></scxml>";

        Assertions.assertEquals(assertValue, SCXMLWriter.write(scxml, new SCXMLWriter.Configuration(true, false)));
    }

    @Test
    public void testSerializeParallel() throws IOException, XMLStreamException {

        final SCXML scxml = new CommonsSCXML();
        scxml.setVersion("1.0");
        scxml.setInitial("par");

        final Parallel par = new Parallel();
        par.setId("par");

        final State s1 = new State();
        s1.setId("S1");

        final State s11 = new State();
        s11.setId("S11");

        s1.addChild(s11);

        final State s2 = new State();
        s2.setId("S2");

        final State s21 = new State();
        s21.setId("S21");

        s2.addChild(s21);

        par.addChild(s1);
        par.addChild(s2);

        scxml.addChild(par);

        final String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"https://commons.apache.org/scxml\" "
            + "version=\"1.0\" initial=\"par\">"
            + "<!--https://commons.apache.org/scxml-->"
            + "<parallel id=\"par\">"
            + "<state id=\"S1\">"
            + "<state id=\"S11\"></state>"
            + "</state>"
            + "<state id=\"S2\">"
            + "<state id=\"S21\"></state>"
            + "</state>"
            + "</parallel>"
            + "</scxml>";

        Assertions.assertEquals(assertValue, SCXMLWriter.write(scxml, new SCXMLWriter.Configuration(true, false)));
     }

    @Test
    public void testSerializeSCXMLNoStates() throws IOException, XMLStreamException {
        final SCXML scxml = new CommonsSCXML();
        // ensure namespaces are stored in insertion order for write->read comparision below
        final LinkedHashMap<String, String> namespaces = new LinkedHashMap<>(scxml.getNamespaces());
        namespaces.put("foo", "http://f.o.o");
        namespaces.put("bar", "http://b.a.r");
        scxml.setNamespaces(namespaces);
        scxml.setVersion("version1");
        scxml.setInitial("off");
        final State s = new State();
        s.setId(scxml.generateTransitionTargetId());
        scxml.addChild(s);

        final String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" xmlns:cs=\"https://commons.apache.org/scxml\" "
            + "xmlns:foo=\"http://f.o.o\" xmlns:bar=\"http://b.a.r\" "
            + "version=\"version1\" initial=\"off\"><!--https://commons.apache.org/scxml--><state></state>"
            + "</scxml>";

        Assertions.assertEquals(assertValue, SCXMLWriter.write(scxml, new SCXMLWriter.Configuration(true, false)));
    }

    @Test
    public void testSerializeSCXMLState() throws IOException, XMLStreamException {
        final SCXML scxml = new CommonsSCXML();
        scxml.setVersion("1.0");
        scxml.setInitial("S1");

        final State s1 = new State();
        s1.setId("S1");

        scxml.addChild(s1);

        final String assertValue = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<scxml xmlns=\"http://www.w3.org/2005/07/scxml\" "
            + "xmlns:cs=\"https://commons.apache.org/scxml\" version=\"1.0\" initial=\"S1\">"
            + "<!--https://commons.apache.org/scxml--><state id=\"S1\"></state></scxml>";

        Assertions.assertEquals(assertValue, SCXMLWriter.write(scxml, new SCXMLWriter.Configuration(true, false)));
    }
}
