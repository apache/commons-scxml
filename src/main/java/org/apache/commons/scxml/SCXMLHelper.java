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
package org.apache.taglibs.rdc.scxml;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.taglibs.rdc.scxml.model.Parallel;
import org.apache.taglibs.rdc.scxml.model.Path;
import org.apache.taglibs.rdc.scxml.model.State;
import org.apache.taglibs.rdc.scxml.model.Transition;
import org.apache.taglibs.rdc.scxml.model.TransitionTarget;

/**
 * Helper class, all methods static final.
 *
 */
public class SCXMLHelper {

    /**
     * Return true if the string is empty
     * 
     * @param attr The String to test
     * @return Is string empty
     */
    public static final boolean isStringEmpty(String attr) {
        return (attr == null || attr.trim().length() == 0) ? true : false;
    }

    /**
     * Checks whether a transition target tt (State or Parallel) is a descendant
     * of the transition target ctx.
     * 
     * @param tt
     *            TransitionTarget to check - a potential descendant
     * @param ctx
     *            TransitionTarget context - a potential ancestor
     * @return true iff tt is a descendant of ctx, false otherwise
     */
    public static final boolean isDescendant(TransitionTarget tt,
            TransitionTarget ctx) {
        while ((tt = tt.getParent()) != null) {
            if (tt == ctx) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a set which contains given states and all their ancestors
     * recursively up to the upper bound. Null upperBound means root 
     * of the state machine.
     *   
     * @param upperBounds 
     * @param transTargets
     *            [in]
     * 
     * @return transitive closure of a given state set
     */
    public static final Set getAncestorClosure(Set states, Set upperBounds) {
        Set closure = new HashSet((int) (states.size() * 2));
        for (Iterator i = states.iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            closure.add(tt);
            while ((tt = tt.getParent()) != null) {
                if (upperBounds != null && upperBounds.contains(tt)) {
                    break;
                }
                if (!closure.add(tt)) {
                    //parent is already a part of the closure
                    break;
                }
            }
        }
        return closure;
    }

    /**
     * Checks whether a given set of states is a legal Harel State Table
     * configuration (with the respect to the definition of the OR and AND
     * states).
     * 
     * @param states
     *            a set of states
     * @param errRep
     *            ErrorReporter to report detailed error info if needed
     * @return true if a given state configuration is legal, false otherwise
     */
    public static final boolean isLegalConfig(Set states, ErrorReporter errRep) {
        /*
         * For every active state we add 1 to the count of its parent. Each
         * Parallel should reach count equal to the number of its children and
         * contribute by 1 to its parent. Each State should reach count exactly
         * 1. SCXML elemnt (top) should reach count exactly 1. We essentially
         * summarize up the hierarchy tree starting with a given set of states =
         * active configuration.
         */
        boolean legalConfig = true; // let's be optimists
        Map counts = new IdentityHashMap();
        Set scxmlCount = new HashSet();
        for (Iterator i = states.iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            TransitionTarget parent = null;
            while ((parent = tt.getParent()) != null) {
                HashSet cnt = (HashSet) counts.get(parent);
                if (cnt == null) {
                    cnt = new HashSet();
                    counts.put(parent, cnt);
                }
                cnt.add(tt);
                tt = parent;
            }
            //top-level contribution
            scxmlCount.add(tt);
        }
        //Validate counts:
        for (Iterator i = counts.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            TransitionTarget tt = (TransitionTarget) entry.getKey();
            Set count = (Set) entry.getValue();
            if (tt instanceof Parallel) {
                Parallel p = (Parallel) tt;
                if (count.size() < p.getStates().size()) {
                    errRep.onError(ErrorReporter.ILLEGAL_CONFIG,
                        "Not all AND states active for parallel "
                        + p.getId(), entry);
                    legalConfig = false;
                }
            } else {
                if (count.size() > 1) {
                    errRep.onError(ErrorReporter.ILLEGAL_CONFIG,
                        "Multiple OR states active for state "
                        + tt.getId(), entry);
                    legalConfig = false;
                }
            }
            count.clear(); //cleanup
        }
        if (scxmlCount.size() > 1) {
            errRep.onError(ErrorReporter.ILLEGAL_CONFIG,
                    "Multiple top-level OR states active!", scxmlCount);
        }
        //cleanup
        scxmlCount.clear();
        counts.clear();
        return legalConfig;
    }

    /**
     * finds the least common ancestor of transition targets tt1 and tt2 if one
     * exists.
     * 
     * @param tt1
     * @param tt2
     * @return closest common ancestor of tt1 and tt2 or null
     */
    public static final TransitionTarget getLCA(TransitionTarget tt1,
            TransitionTarget tt2) {
        if (tt1 == tt2) {
            return tt1; //self-transition
        } else if (isDescendant(tt1, tt2)) {
            return tt2;
        } else if (isDescendant(tt2, tt1)) {
            return tt1;
        }
        Set parents = new HashSet();
        TransitionTarget tmp = tt1;
        while ((tmp = tmp.getParent()) != null) {
            if (tmp instanceof State) {
                parents.add(tmp);
            }
        }
        tmp = tt2;
        while ((tmp = tmp.getParent()) != null) {
            if (tmp instanceof State) {
                //test redundant add = common ancestor
                if (!parents.add(tmp)) {
                    parents.clear();
                    return tmp;
                }
            }
        }
        return null;
    }

    /**
     * Returns the set of all states (and parallels) which are exited if a given transition t is
     * going to be taken. Current states are necessary to be taken into account
     * due to orthogonal states and cross-region transitions - see UML specs.
     * for more details.
     * 
     * @param t
     *            transition to be taken
     * @param currentStates
     *            the set of current states (simple states only)
     * @return a set of all states (including composite) which are exited if a
     *         given transition is taken
     */
    public static final Set getStatesExited(Transition t, Set currentStates) {
        Set allStates = new HashSet();
        Path p = t.getPath();
        //the easy part
        allStates.addAll(p.getUpwardSegment());
        if (p.isCrossRegion()) {
            for (Iterator regions = p.getRegionsExited().iterator(); 
                    regions.hasNext(); ) {
                Parallel par = ((Parallel) ((State) regions.next()).getParent());
                //let's find affected states in sibling regions            
                for (Iterator siblings = par.getStates().iterator(); 
                        siblings.hasNext(); ) {
                    State s = (State) siblings.next();
                    for (Iterator act = currentStates.iterator(); 
                            act.hasNext(); ) {
                        TransitionTarget a = (TransitionTarget) act.next();
                        if (isDescendant(a, s)) {
                            //a is affected
                            boolean added = false;
                            added = allStates.add(a);
                            while (added && a != s) {
                                a = a.getParent();
                                added = allStates.add(a);
                            }
                        }
                    }
                }
            }
        }
        return allStates;
    }

    /**
     * According to the UML definition, two transitions
     * are conflicting if the sets of states they exit overlap.
     * 
     * @param t1 a transition to check against t2
     * @param t2 a transition to check against t1
     * @param currentStates the set of current states (simple states only)
     * @return true if the t1 and t2 are conflicting transitions
     * @see #getStatesExited(Transition, Set)
     */
    public static final boolean inConflict(Transition t1, Transition t2,
            Set currentStates) {
        Set ts1 = getStatesExited(t1, currentStates);
        Set ts2 = getStatesExited(t2, currentStates);
        ts1.retainAll(ts2);
        return (ts1.isEmpty()) ? false : true;
    }
}
