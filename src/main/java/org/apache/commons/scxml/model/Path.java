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
package org.apache.taglibs.rdc.scxml.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.taglibs.rdc.scxml.SCXMLHelper;

/**
 * A helper class for this SCXML implementation that represents the
 * location of an entity in the SCXML document.
 * 
 * @author Jaroslav Gergic 
 */
public class Path {

    private List upSeg = new ArrayList();

    private List downSeg = new ArrayList();

    private State scope = null;

    private boolean crossRegion = false;

    Path(TransitionTarget source, TransitionTarget target) {
        if (target == null) {
            //a local "stay" transition
            scope = (State) source;
            //all segments remain empty
        } else {
            TransitionTarget tt = SCXMLHelper.getLCA(source, target);
            if (tt != null) {
                if (tt instanceof State) {
                    scope = (State) tt;
                } else {
                    scope = tt.getParentState();
                }
                if (scope == source || scope == target) {
                    scope = scope.getParentState();
                }
            }
            tt = source;
            while (tt != scope) {
                upSeg.add(tt);
                if (tt instanceof State) {
                    State st = (State) tt;
                    if (st.isRegion()) {
                        crossRegion = true;
                    }
                }
                tt = tt.getParent();
            }
            tt = target;
            while (tt != scope) {
                downSeg.add(0, tt);
                if (tt instanceof State) {
                    State st = (State) tt;
                    if (st.isRegion()) {
                        crossRegion = true;
                    }
                }
                tt = tt.getParent();
            }
        }
    }

    /**
     * @return true when the path crosses a region border(s)
     * @see State#isRegion()
     */
    public boolean isCrossRegion() {
        return crossRegion;
    }

    /**
     * @return a list of exited regions sorted bottom-up; no order defined for
     *         siblings
     */
    public List getRegionsExited() {
        LinkedList ll = new LinkedList();
        for (Iterator i = upSeg.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof State) {
                State st = (State) o;
                if (st.isRegion()) {
                    ll.add(st);
                }
            }
        }
        return ll;
    }

    /**
     * @return a list of entered regions sorted top-down; no order defined for
     *         siblings
     */
    public List getRegionsEntered() {
        LinkedList ll = new LinkedList();
        for (Iterator i = downSeg.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof State) {
                State st = (State) o;
                if (st.isRegion()) {
                    ll.add(st);
                }
            }
        }
        return ll;
    }

    /**
     * @return scope of the transition path, null means global transition (SCXML
     *         document level) Scope is the least state which is not being
     *         exited nor entered by the transition.
     */
    public State getScope() {
        return scope;
    }

    /**
     * @return upward segment of the path up to the scope
     */
    public List getUpwardSegment() {
        return upSeg;
    }

    /**
     * @return downward segment from the scope to the target
     */
    public List getDownwardSegment() {
        return downSeg;
    }
}

