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
package org.apache.commons.scxml2.model;

/**
 * Exception that is thrown when the SCXML model supplied to the
 * executor has a fatal flaw that prevents the executor from
 * further interpreting the the model.
 *
 */
public class ModelException extends Exception {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see java.lang.Exception#Exception()
     */
    public ModelException() {
        super();
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.String)
     * @param message
     */
    public ModelException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.Throwable)
     * @param cause
     */
    public ModelException(final Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception#Exception(String, java.lang.Throwable)
     * @param message
     * @param cause
     */
    public ModelException(final String message, final Throwable cause) {
        super(message, cause);
    }

}

