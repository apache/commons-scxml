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
package org.apache.commons.scxml2;

import java.io.Serializable;
import java.util.Objects;

/**
 * A class representing an event. Specific event types have been
 * defined in reference to SCXML.
 *
 * <strong>NOTE:</strong> Instances are {@link Serializable} as long as the associated
 * data, if any, is {@link Serializable}.
 */
public class TriggerEvent implements Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * <code>CALL_EVENT</code>.
     */
    public static final int CALL_EVENT = 1;

    /**
     * <code>CHANGE_EVENT</code>.
     */
    public static final int CHANGE_EVENT = 2;

    /**
     * <code>SIGNAL_EVENT</code>.
     */
    public static final int SIGNAL_EVENT = 3;

    /**
     * <code>TIME_EVENT</code>.
     */
    public static final int TIME_EVENT = 4;

    /**
     * <code>ERROR_EVENT</code>.
     */
    public static final int ERROR_EVENT = 5;

    /**
     * <code>CANCEL_EVENT</code>.
     */
    public static final int CANCEL_EVENT = 6;

    /**
     * The predefined SCXML 'error.execution' Event name
     * <p>
     * Indicates that an error internal to the execution of the document has occurred, such as one arising from
     * expression evaluation.
     * </p>
     * @see <a href="http://www.w3.org/TR/scxml/#errorsAndEvents">
     *      http://www.w3.org/TR/scxml/#errorsAndEvents</a>
     */
    public static final String ERROR_EXECUTION = "error.execution";

    /**
     * The predefined SCXML 'error.communication' Event name
     * <p>
     * Indicates that an error has occurred while trying to communicate with an external entity.
     * </p>
     * @see <a href="http://www.w3.org/TR/scxml/#errorsAndEvents">
     *      http://www.w3.org/TR/scxml/#errorsAndEvents</a>
     */
    public static final String ERROR_COMMUNICATION = "error.communication";

    /**
     * The predefined SCXML 'error.platform' Event name
     * <p>
     * Indicates that a platform- or application-specific error has occurred.
     * </p>
     * @see <a href="http://www.w3.org/TR/scxml/#errorsAndEvents">
     *      http://www.w3.org/TR/scxml/#errorsAndEvents</a>
     */
    public static final String ERROR_PLATFORM = "error.platform";

    private final String name;

    private final int type;

    private final String sendId;
    private final String origin;
    private final String originType;
    private final String invokeId;
    private final Object data;
    /**
     * Constructs a new instance.
     *
     * @param name The event name
     * @param type The event type
     * @deprecated use {@link EventBuilder instead}
     */
    @Deprecated
    public TriggerEvent(final String name, final int type) {
        this(name, type, null, null, null, null, null);
    }
    TriggerEvent(final String name, final int type, final String sendId, final String origin,
                        final String originType, final String invokeId, final Object data) {
        this.name = name != null ? name.trim() : "";
        this.type = type;
        this.sendId = sendId;
        this.origin = origin;
        this.originType = originType;
        this.invokeId = invokeId;
        this.data = data;
    }

    /**
     * Define an equals operator for TriggerEvent.
     *
     * @see Object#equals(Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TriggerEvent) {
            final TriggerEvent te2 = (TriggerEvent) obj;
            if (type == te2.type && name.equals(te2.name) &&
                    Objects.equals(sendId, te2.sendId) &&
                    Objects.equals(origin, te2.origin) &&
                    Objects.equals(originType, te2.originType) &&
                    Objects.equals(invokeId, te2.invokeId) &&
                    Objects.equals(data, te2.data)) {
                return true;
            }
        }
        return false;
    }

    public Object getData() {
        return data;
    }

    public String getInvokeId() {
        return invokeId;
    }

    public String getName() {
        return name;
    }

    public String getOrigin() {
        return origin;
    }

    public String getOriginType() {
        return originType;
    }

    public String getSendId() {
        return sendId;
    }

    public int getType() {
        return type;
    }

    /**
     * Returns the hash code for this TriggerEvent object.
     *
     * @see Object#hashCode()
     */
    @Override
    public int hashCode() {
        return String.valueOf(this).hashCode();
    }

    /**
     * Returns a string representation of this TriggerEvent object.
     *
     * @see Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("TriggerEvent{name=");
        buf.append(name).append(", type=").append(type);
        if (sendId != null) {
            buf.append(", sendid=").append(invokeId);
        }
        if (origin != null) {
            buf.append(", origin=").append(invokeId);
        }
        if (originType != null) {
            buf.append(", origintype=").append(invokeId);
        }
        if (invokeId != null) {
            buf.append(", invokeid=").append(invokeId);
        }
        if (data != null) {
            buf.append(", data=").append(data.toString());
        }
        buf.append("}");
        return String.valueOf(buf);
    }

}

