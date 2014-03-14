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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.ErrorReporter;
import org.apache.commons.scxml2.model.Executable;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.State;
import org.apache.commons.scxml2.model.TransitionTarget;
import org.apache.commons.scxml2.semantics.ErrorConstants;

/**
 * Custom error reporter that log execution errors.
 */
public class SimpleErrorReporter implements ErrorReporter, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Log. */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Constructor.
     */
    public SimpleErrorReporter() {
        super();
    }

    /**
     * @see ErrorReporter#onError(String, String, Object)
     */
    @SuppressWarnings("unchecked")
    public void onError(final String errorCode, final String errDetail,
            final Object errCtx) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes
        String errCode = errorCode.intern();
        StringBuffer msg = new StringBuffer();
        msg.append(errCode).append(" (");
        msg.append(errDetail).append("): ");
        if (errCode == ErrorConstants.NO_INITIAL) {
            if (errCtx instanceof SCXML) {
                //determineInitialStates
                msg.append("<SCXML>");
            } else if (errCtx instanceof State) {
                //determineInitialStates
                //determineTargetStates
                msg.append("State " + LogUtils.getTTPath((State) errCtx));
            }
        } else if (errCode == ErrorConstants.UNKNOWN_ACTION) {
            //executeActionList
            msg.append("Action: " + errCtx.getClass().getName());
        } else if (errCode == ErrorConstants.ILLEGAL_CONFIG) {
            //isLegalConfig
            if (errCtx instanceof Map.Entry) { //unchecked cast below
                Map.Entry<TransitionTarget, Set<TransitionTarget>> badConfigMap =
                    (Map.Entry<TransitionTarget, Set<TransitionTarget>>) errCtx;
                TransitionTarget tt = badConfigMap.getKey();
                Set<TransitionTarget> vals = badConfigMap.getValue();
                msg.append(LogUtils.getTTPath(tt) + " : [");
                for (Iterator<TransitionTarget> i = vals.iterator();
                        i.hasNext();) {
                    TransitionTarget tx = i.next();
                    msg.append(LogUtils.getTTPath(tx));
                    if (i.hasNext()) { // reason for iterator usage
                        msg.append(", ");
                    }
                }
                msg.append(']');
            } else if (errCtx instanceof Set) { //unchecked cast below
                Set<TransitionTarget> vals = (Set<TransitionTarget>) errCtx;
                msg.append("<SCXML> : [");
                for (Iterator<TransitionTarget> i = vals.iterator(); i.hasNext();) {
                    TransitionTarget tx = i.next();
                    msg.append(LogUtils.getTTPath(tx));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            }
        } else if (errCode == ErrorConstants.EXPRESSION_ERROR) {
            if (errCtx instanceof Executable) {
                TransitionTarget parent = ((Executable) errCtx).getParent();
                msg.append("Expression error inside " + LogUtils.getTTPath(parent));
            }
            else if (errCtx instanceof SCXML) {
                // Global Script
                msg.append("Expression error inside the global script");
            }
        }
        handleErrorMessage(errorCode, errDetail, errCtx, msg);
    }

    /**
     * Final handling of the resulting errorMessage build by {@link #onError(String, String, Object)} onError}.
     * <p>The default implementation write the errorMessage as a warning to the log.</p>
     */
    protected void handleErrorMessage(final String errorCode, final String errDetail,
                               final Object errCtx, final CharSequence errorMessage) {

        if (log.isWarnEnabled()) {
            log.warn(errorMessage.toString());
        }
    }
}

