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
package org.apache.commons.scxml.env;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Custom error handler that logs the parsing errors in the
 * SCXML document.
 */
public class SimpleErrorHandler implements ErrorHandler {

    /** Message prefix. */
    private static final String MSG_PREFIX = "SCXML SAX Parsing: ";
    /** Message postfix. */
    private static final String MSG_POSTFIX = " Correct the SCXML document.";

    /** Log. */
    private Log log = LogFactory.getLog(getClass());

    /**
     * Constructor.
     */
    public SimpleErrorHandler() {
        super();
    }

    /**
     * @see ErrorHandler#error(SAXParseException)
     */
    public void error(SAXParseException exception) {
        if (log.isErrorEnabled()) {
            log.error(MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
                exception);
        }
    }

    /**
     * @see ErrorHandler#fatalError(SAXParseException)
     */
    public void fatalError(SAXParseException exception) {
        if (log.isFatalEnabled()) {
            log.fatal(MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
                exception);
        }
    }

    /**
     * @see ErrorHandler#warning(SAXParseException)
     */
    public void warning(SAXParseException exception) {
        if (log.isWarnEnabled()) {
            log.warn(MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
                exception);
        }
    }
}

