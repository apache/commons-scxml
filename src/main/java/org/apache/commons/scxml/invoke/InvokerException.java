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
package org.apache.commons.scxml.invoke;

/**
 * Exception thrown when a process specified by an &lt;invoke&gt;
 * cannot be initiated.
 *
 */
public class InvokerException extends Exception {

    /**
     * @see java.lang.Exception#Exception()
     */
    public InvokerException() {
        super();
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.String)
     * @param message The error message
     */
    public InvokerException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.Throwable)
     * @param cause The cause
     */
    public InvokerException(final Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     * @param message The error message
     * @param cause The cause
     */
    public InvokerException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

}

