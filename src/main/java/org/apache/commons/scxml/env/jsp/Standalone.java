/*
 *
 *   Copyright 2005 The Apache Software Foundation.
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
package org.apache.commons.scxml.env.jsp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.Evaluator;
import org.apache.commons.scxml.EventDispatcher;
import org.apache.commons.scxml.SCXMLDigester;
import org.apache.commons.scxml.SCXMLExecutor;
import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.TriggerEvent;
import org.apache.commons.scxml.env.SimpleDispatcher;
import org.apache.commons.scxml.env.Tracer;
import org.apache.commons.scxml.model.ModelException;
import org.apache.commons.scxml.model.SCXML;

/**
 * Standalone SCXML interpreter, useful for command-line testing and
 * debugging.
 *
 * <p><b>USAGE:</b></p>
 * <p><code>java org.apache.commons.scxml.env.jsp.Standalone url</code></p>
 * <p>or</p>
 * <p><code>java org.apache.commons.scxml.env.jsp.Standalone filename</code>
 * </p>
 *
 * <p><b>RUNNING:</b></p>
 * <p>Enter a space-separated list of "events"</p>
 * <p>To quit, enter "quit"</p>
 * <p>To populate a variable in the current context, type "name=value"</p>
 * <p>To reset state machine, enter "reset"</p>
 *
 */
public final class Standalone {

    /**
     * Launcher.
     * @param args The arguments, one expected, the URI or filename of the
     *             SCXML document
     */
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: java " + Standalone.class.getName()
                    + "<url|filename>");
            System.exit(-1);
        }
        try {
            String uri = getCanonicalURI(args[0]);
            Evaluator engine = new ELEvaluator();
            Context rootCtx = engine.newContext(null);
            EventDispatcher ed = new SimpleDispatcher();
            Tracer trc = new Tracer();
            SCXML doc = SCXMLDigester.digest(new URL(uri), trc, rootCtx,
                engine);
            if (doc == null) {
                System.err.println("The SCXML document " + uri
                        + " can not be parsed!");
                System.exit(-1);
            }
            System.err.println(SCXMLDigester.serializeSCXML(doc));
            SCXMLExecutor exec = new SCXMLExecutor(engine, ed, trc);
            doc.addListener(trc);
            exec.setSuperStep(true);
            exec.setStateMachine(doc);
            BufferedReader br = new BufferedReader(new
                InputStreamReader(System.in));
            String event = null;
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
                    System.out.println("Set variable " + name + " to " +
                        value);
                } else if (SCXMLHelper.isStringEmpty(event)
                           || event.equalsIgnoreCase("null")) {
                    TriggerEvent[] evts = { new TriggerEvent(null,
                    TriggerEvent.SIGNAL_EVENT, null) };
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
    private Standalone() {
        super();
    }

}

