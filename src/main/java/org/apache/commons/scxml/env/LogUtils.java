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
package org.apache.commons.scxml.env;

import java.util.LinkedList;

import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * Helper methods for Commons SCXML logging.
 *
 */
public final class LogUtils {

    /**
     * Create a human readable log view of this transition.
     *
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that is taken
     * @return String The human readable log entry
     */
    public static String transToString(final TransitionTarget from,
            final TransitionTarget to, final Transition transition) {
        StringBuffer buf = new StringBuffer("transition (");
        buf.append("event = ").append(transition.getEvent());
        buf.append(", cond = ").append(transition.getCond());
        buf.append(", from = ").append(getTTPath(from));
        buf.append(", to = ").append(getTTPath(to));
        buf.append(')');
        return buf.toString();
    }

    /**
     * Write out this TransitionTarget location in a XPath style format.
     *
     * @param tt The TransitionTarget whose &quot;path&quot; is to needed
     * @return String The XPath style location of the TransitionTarget within
     *                the SCXML document
     */
    public static String getTTPath(final TransitionTarget tt) {
        TransitionTarget parent = tt.getParent();
        if (parent == null) {
            return "/" + tt.getId();
        } else {
            LinkedList<TransitionTarget> pathElements = new LinkedList<TransitionTarget>();
            pathElements.addFirst(tt);
            while (parent != null) {
                pathElements.addFirst(parent);
                parent = parent.getParent();
            }
            StringBuffer names = new StringBuffer();
            for (TransitionTarget pathElement : pathElements) {
                names.append('/').append(pathElement.getId());
            }
            return names.toString();
        }
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private LogUtils() {
        super();
    }

}
