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
package org.apache.commons.scxml2.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.invoke.SimpleSCXMLInvoker;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.io.SCXMLWriter;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;

/**
 * Utility methods used by command line SCXML execution, useful for
 * debugging.
 *
 * The following expression languages are supported in SCXML documents:
 * <ol>
 *  <li>JEXL - Using Commons JEXL</li>
 * </ol>
 *
 * @see org.apache.commons.scxml2.env.jexl
 */
public final class StandaloneUtils {

    /**
     * Command line utility method for executing the state machine defined
     * using the SCXML document described by the specified URI and using
     * the specified expression evaluator.
     *
     * @param uri The URI or filename of the SCXML document
     * @param evaluator The expression evaluator for the expression language
     *                  used in the specified SCXML document
     *
     * <p>RUNNING:</p>
     * <ul>
     *  <li>Enter a space-separated list of "events"</li>
     *  <li>To quit, enter "quit"</li>
     *  <li>To populate a variable in the current context,
     *      type "name=value"</li>
     *  <li>To reset state machine, enter "reset"</li>
     * </ul>
     */
    public static void execute(final String uri, final Evaluator evaluator) {
        try {
            String documentURI = getCanonicalURI(uri);
            Context rootCtx = evaluator.newContext(null);
            Tracer trc = new Tracer();
            SCXML doc = SCXMLReader.read(new URL(documentURI));
            if (doc == null) {
                System.err.println("The SCXML document " + uri
                        + " can not be parsed!");
                System.exit(-1);
            }
            System.out.println(SCXMLWriter.write(doc));
            SCXMLExecutor exec = new SCXMLExecutor(evaluator, null, trc);
            exec.setStateMachine(doc);
            exec.addListener(doc, trc);
            exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
            exec.setRootContext(rootCtx);
            exec.go();
            BufferedReader br = new BufferedReader(new
                InputStreamReader(System.in));
            String event;
            while ((event = br.readLine()) != null) {
                event = event.trim();
                if (event.equalsIgnoreCase("help") || event.equals("?")) {
                    System.out.println("Enter a space-separated list of "
                        + "events");
                    System.out.println("To populate a variable in the "
                        + "current context, type \"name=value\"");
                    System.out.println("To quit, enter \"quit\"");
                    System.out.println("To reset state machine, enter "
                        + "\"reset\"");
                } else if (event.equalsIgnoreCase("quit")) {
                    break;
                } else if (event.equalsIgnoreCase("reset")) {
                    exec.reset();
                } else if (event.indexOf('=') != -1) {
                    int marker = event.indexOf('=');
                    String name = event.substring(0, marker);
                    String value = event.substring(marker + 1);
                    rootCtx.setLocal(name, value);
                    System.out.println("Set variable " + name + " to "
                        + value);
                } else if (event.trim().length() == 0
                           || event.equalsIgnoreCase("null")) {
                    TriggerEvent[] evts = {new TriggerEvent(null,
                        TriggerEvent.SIGNAL_EVENT, null)};
                    exec.triggerEvents(evts);
                    if (exec.getCurrentStatus().isFinal()) {
                        System.out.println("A final configuration reached.");
                    }
                } else {
                    StringTokenizer st = new StringTokenizer(event);
                    int tkns = st.countTokens();
                    TriggerEvent[] evts = new TriggerEvent[tkns];
                    for (int i = 0; i < tkns; i++) {
                        evts[i] = new TriggerEvent(st.nextToken(),
                                TriggerEvent.SIGNAL_EVENT, null);
                    }
                    exec.triggerEvents(evts);
                    if (exec.getCurrentStatus().isFinal()) {
                        System.out.println("A final configuration reached.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ModelException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
        	e.printStackTrace();
        }
    }

    /**
     * @param uri an absolute or relative URL
     * @return java.lang.String canonical URL (absolute)
     * @throws java.io.IOException if a relative URL can not be resolved
     *         to a local file
     */
    private static String getCanonicalURI(final String uri)
    throws IOException {
        if (uri.toLowerCase().startsWith("http://")
            || uri.toLowerCase().startsWith("file://")) {
                return uri;
        }
        File in = new File(uri);
        return "file:///" + in.getCanonicalPath();
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private StandaloneUtils() {
        super();
    }

}

