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
package org.apache.commons.scxml.semantics;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.scxml.SCXMLHelper;
import org.apache.commons.scxml.model.TransitionTarget;


/**
 * A comparator for TransitionTarget instances.
 *
 */
final class TransitionTargetComparator implements Comparator, Serializable {

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
    public int compare(final Object o1, final Object o2) {
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

