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
package org.apache.commons.scxml.semantics;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.TransitionTarget;


/**
 * A comparator for TransitionTarget instances.
 *
 */
final class TransitionTargetComparator<T> implements Comparator<T>, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    TransitionTargetComparator() {
        super();
    }

    /**
     * Compares two instances of TransitionTarget in terms of the
     * SCXML tree hierarchy.
     * <p>Important Remarks:</p> does not fullfill the Comparator contract,
     * since it returns 0 if o1 == o2 and also if they are not related to each
     * other and at the same time the chain-to-parent length for o1 is the
     * same length as for o2 (that is, they are equally deeply nested)
     *
     * @param o1 The first TransitionTarget object
     * @param o2 The second TransitionTarget object
     * @return int The comparation result
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * @see TransitionTarget
     */
    public int compare(final T o1, final T o2) {
        TransitionTarget tt1 = (TransitionTarget) o1;
        TransitionTarget tt2 = (TransitionTarget) o2;
        if (tt1 == tt2) {
            return 0;
        } else if (SCXMLHelper.isDescendant(tt1, tt2)) {
            return -1;
        } else if (SCXMLHelper.isDescendant(tt2, tt1)) {
            return 1;
        } else {
            //the tt1 and tt2 are parallel, now we have to count chain sizes
            int tc1 = countChainLength(tt1);
            int tc2 = countChainLength(tt2);
            if (tc2 == tc1) {
                // use document order as priority
                // - not a requirement
                // - though useful for an impl to have repeatable behavior
                // - downside is users may rely on this behavior
                Parallel lca = (Parallel) SCXMLHelper.getLCA(tt1, tt2);
                TransitionTarget parent1 = tt1;
                while (parent1.getParent() != lca) {
                    parent1 = parent1.getParent();
                }
                TransitionTarget parent2 = tt2;
                while (parent2.getParent() != lca) {
                    parent2 = parent2.getParent();
                }
                for (Iterator iter = lca.getChildren().iterator();
                        iter.hasNext();) {
                    State s = (State) iter.next();
                    if (s == parent1) {
                        return 1;
                    } else if (s == parent2) {
                        return -1;
                    }
                }
            }
            //longer the chain, deeper the node is
            return tc2 - tc1;
        }
    }

    /**
     * The &quot;depth&quot; at which this TransitionTarget exists in the
     * SCXML object model.
     *
     * @param tt The TransitionTarget
     * @return int The &quot;depth&quot;
     */
    private int countChainLength(final TransitionTarget tt) {
        int count = 0;
        TransitionTarget parent = tt.getParent();
        while (parent != null) {
            count++;
            parent = parent.getParent();
        }
        return count;
    }
}

