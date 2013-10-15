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

import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.env.rhino.RhinoEvaluator;

/**
 * Standalone SCXML interpreter, useful for command-line testing and
 * debugging, where expressions are ECMA script expressions.
 *
 * <p>USAGE:</p>
 * <p><code>java org.apache.commons.scxml2.test.StandaloneRhinoExpressions
 *          url</code></p>
 * <p>or</p>
 * <p><code>java org.apache.commons.scxml2.test.StandaloneRhinoExpressions
 *          filename</code>
 * </p>
 *
 * <p>RUNNING:</p>
 * <ul>
 *  <li>Enter a space-separated list of "events"</li>
 *  <li>To quit, enter "quit"</li>
 *  <li>To populate a variable in the current context,
 *      type "name=value"</li>
 *  <li>To reset state machine, enter "reset"</li>
 * </ul>
 *
 */
public final class StandaloneRhinoExpressions {

    /**
     * Launcher.
     * @param args The arguments, one expected, the URI or filename of the
     *             SCXML document
     */
    public static void main(final String[] args) {
        if (args.length < 1) {
            System.out.println("USAGE: java "
                    + StandaloneRhinoExpressions.class.getName()
                    + "<url|filename>");
            System.exit(-1);
        }
        Evaluator evaluator = new RhinoEvaluator();
        StandaloneUtils.execute(args[0], evaluator);
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private StandaloneRhinoExpressions() {
        super();
    }

}

