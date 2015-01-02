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
package org.apache.commons.scxml2.env;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.scxml2.model.ModelException;

/**
 * A SCXML document driven stop watch.
 *
 * Using SCXML makes the StopWatch class simplistic; you are neither
 * managing the stopwatch "lifecycle" nor coding any "transitions",
 * that information is pulled in straight from the behavioral model
 * of the stop watch, which is encapsulated in the SCXML document
 * the constructor points to (which in turn may be generated straight
 * from the UML model).
 */
public class StopWatch extends AbstractStateMachine {

    /** The events for the stop watch. */
    public static final String EVENT_START = "watch.start",
        EVENT_STOP = "watch.stop", EVENT_SPLIT = "watch.split",
        EVENT_UNSPLIT = "watch.unsplit", EVENT_RESET = "watch.reset";

    /** The fragments of the elapsed time. */
    private int hr, min, sec, fract;
    /** The fragments of the display time. */
    private int dhr, dmin, dsec, dfract;
    /** The stopwatch "split" (display freeze). */
    private boolean split;
    /** The Timer to keep time. */
    private Timer timer;
    /** The display decorations. */
    private static final String DELIM = ":", DOT = ".", EMPTY = "", ZERO = "0";

    public StopWatch() throws ModelException {
        super(StopWatch.class.getClassLoader().
            getResource("org/apache/commons/scxml2/env/stopwatch.xml"));
    }

    // Each method below is the activity corresponding to a state in the
    // SCXML document (see class constructor for pointer to the document).
    public void reset() {
        hr = min = sec = fract = dhr = dmin = dsec = dfract = 0;
        split = false;
    }

    public void running() {
        split = false;
        if (timer == null) {
            timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    increment();
                }
            }, 100, 100);
        }
    }

    public void paused() {
        split = true;
    }

    public void stopped() {
        timer.cancel();
        timer = null;
    }

    public String getDisplay() {
        String padhr = dhr > 9 ? EMPTY : ZERO;
        String padmin = dmin > 9 ? EMPTY : ZERO;
        String padsec = dsec > 9 ? EMPTY : ZERO;
        return new StringBuffer().append(padhr).append(dhr).append(DELIM).
            append(padmin).append(dmin).append(DELIM).append(padsec).
            append(dsec).append(DOT).append(dfract).toString();
    }

    // used by the demonstration (see StopWatchDisplay usecase)
    public String getCurrentState() {
        return getEngine().getStatus().getStates().iterator().next().getId();
    }

    private void increment() {
        if (fract < 9) {
            fract++;
        } else {
            fract = 0;
            if (sec < 59) {
                sec++;
            } else {
                sec = 0;
                if (min < 59) {
                    min++;
                } else {
                    min = 0;
                    if (hr < 99) {
                        hr++;
                    } else {
                        hr = 0; //wrap
                    }
                }
            }
        }
        if (!split) {
            dhr = hr;
            dmin = min;
            dsec = sec;
            dfract = fract;
        }
    }

}

