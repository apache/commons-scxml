/*
 *
 *   Copyright 2006 The Apache Software Foundation.
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
package org.apache.commons.scxml;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.scxml.model.TransitionTarget;

/**
 * Implementations of builtin functions defined by the SCXML
 * specification.
 *
 * The current version of the specification defines one builtin
 * predicate In()
 */
public class Builtin {

    /**
     * Implements the In() predicate for SCXML documents. The method
     * name chosen is different since &quot;in&quot; is a reserved token
     * in some expression languages.
     *
     * Does this state belong to the given Set of States.
     * Simple ID based comparator, assumes IDs are unique.
     *
     * @param allStates The Set of State objects to look in
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public static boolean isMember(final Set allStates,
            final String state) {
        for (Iterator i = allStates.iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            if (state.equals(tt.getId())) {
                return true;
            }
        }
        return false;
    }

}

