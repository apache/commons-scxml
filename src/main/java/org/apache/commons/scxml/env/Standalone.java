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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.taglibs.rdc.scxml.Context;
import org.apache.taglibs.rdc.scxml.Evaluator;
import org.apache.taglibs.rdc.scxml.EventDispatcher;
import org.apache.taglibs.rdc.scxml.SCXMLDigester;
import org.apache.taglibs.rdc.scxml.SCXMLExecutor;
import org.apache.taglibs.rdc.scxml.TriggerEvent;
import org.apache.taglibs.rdc.scxml.model.ModelException;
import org.apache.taglibs.rdc.scxml.model.SCXML;

/**
 * Standalone SCXML Interpreter.
 * Useful for command-line testing and debugging.
 * 
 * @author Jaroslav Gergic
 */
public class Standalone {

    /**
     * Launcher
     */
    public static void main(String[] args) {
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
            SCXML doc = SCXMLDigester.digest(new URL(uri), trc, rootCtx, engine);
            if(doc == null) {
                System.err.println("The SCXML document " + uri
                        + " can not be parsed!");
                System.exit(-1);
            }
            System.err.println(SCXMLDigester.serializeSCXML(doc));
            SCXMLExecutor exec = new SCXMLExecutor(engine, ed, trc);
            doc.addListener(trc);
            exec.setSuperStep(true);
            exec.setStateMachine(doc);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String event = null;
            while((event = br.readLine()) != null) {
                event = event.trim();
                if(event.equals("help") || event.equals("?")) {
                    System.out.println("enter a space-separated list of events");
                    System.out.println("to quit, enter \"quit\"");
                    System.out.println("to reset state machine, enter \"reset\"");
                } else if(event.equals("quit")) {
                    break;
                } else if(event.equals("reset")) {
                    exec.reset();
                } else {
                    StringTokenizer st = new StringTokenizer(event);
                    int tkns = st.countTokens();
                    TriggerEvent evts[] = new TriggerEvent[tkns];
                    for(int i=0; i<tkns; i++) {
                        evts[i] = new TriggerEvent(st.nextToken(),
                                TriggerEvent.SIGNAL_EVENT, null);
                    }
                    exec.triggerEvents(evts);
                    if(exec.getCurrentStatus().isFinal()) {
                        System.out.println("A final configuration reached.");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ModelException e){
            e.printStackTrace();
        }
        
    }
    
      /**
       * @param uri an absolute or relative URL
       * @return java.lang.String canonical URL (absolute)
       * @throws java.io.IOException if a relative URL can not be resolved
       * to a local file
       * @author Lubos Ures
       */
      private static String getCanonicalURI(String uri) throws IOException {
        if (uri.toLowerCase().startsWith("http://")
                || uri.toLowerCase().startsWith("file://")) {
        } else {
            File in = new File(uri);
            uri = "file:///" + in.getCanonicalPath();
        }
        return uri;
    }
        
}
