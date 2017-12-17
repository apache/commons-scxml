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

public final class SCXMLConstants {

    /**
     * The W3C SCXML namespace
     */
    public static final String XMLNS_SCXML = "http://www.w3.org/2005/07/scxml";

    /**
     * The Apache Commons SCXML namespace for custom actions defined by the Apache Commons SCXML implementation.
     * Any document that intends to use these custom actions needs to ensure that they are in the correct namespace.
     * Use of actions in this namespace makes the document non-portable across implementations.
     */
    public static final String XMLNS_COMMONS_SCXML = "http://commons.apache.org/scxml";

    /**
     * The default {@link #XMLNS_COMMONS_SCXML} prefix
     */
    public static final String XMLNS_COMMONS_SCXML_PREFIX = "cs";

    // W3C SCXML XML Element names
    public static final String ELEM_ASSIGN = "assign";
    public static final String ELEM_CANCEL = "cancel";
    public static final String ELEM_CONTENT = "content";
    public static final String ELEM_DATA = "data";
    public static final String ELEM_DATAMODEL = "datamodel";
    public static final String ELEM_DONEDATA = "donedata";
    public static final String ELEM_ELSE = "else";
    public static final String ELEM_ELSEIF = "elseif";
    public static final String ELEM_FINAL = "final";
    public static final String ELEM_FINALIZE = "finalize";
    public static final String ELEM_HISTORY = "history";
    public static final String ELEM_IF = "if";
    public static final String ELEM_INITIAL = "initial";
    public static final String ELEM_INVOKE = "invoke";
    public static final String ELEM_FOREACH = "foreach";
    public static final String ELEM_LOG = "log";
    public static final String ELEM_ONENTRY = "onentry";
    public static final String ELEM_ONEXIT = "onexit";
    public static final String ELEM_PARALLEL = "parallel";
    public static final String ELEM_PARAM = "param";
    public static final String ELEM_RAISE = "raise";
    public static final String ELEM_SCRIPT = "script";
    public static final String ELEM_SCXML = "scxml";
    public static final String ELEM_SEND = "send";
    public static final String ELEM_STATE = "state";
    public static final String ELEM_TRANSITION = "transition";

    // Commons SCXML XML Element names
    public static final String ELEM_VAR = "var";

    // W3C SCXML XML Attribute names
    public static final String ATTR_ARRAY = "array";
    public static final String ATTR_AUTOFORWARD = "autoforward";
    public static final String ATTR_BINDING = "binding";
    public static final String ATTR_BINDING_EARLY = "early";
    public static final String ATTR_BINDING_LATE = "late";
    public static final String ATTR_COND = "cond";
    public static final String ATTR_DATAMODEL = "datamodel";
    public static final String ATTR_DELAY = "delay";
    public static final String ATTR_DELAYEXPR = "delayexpr";
    public static final String ATTR_EVENT = "event";
    public static final String ATTR_EVENTEXPR = "eventexpr";
    public static final String ATTR_EXPR = "expr";
    public static final String ATTR_ID = "id";
    public static final String ATTR_IDLOCATION = "idlocation";
    public static final String ATTR_INDEX = "index";
    public static final String ATTR_INITIAL = "initial";
    public static final String ATTR_ITEM = "item";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_LOCATION = "location";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_NAMELIST = "namelist";
    public static final String ATTR_PROFILE = "profile";
    public static final String ATTR_SENDID = "sendid";
    public static final String ATTR_SENDIDEXPR = "sendidexpr";
    public static final String ATTR_SRC = "src";
    public static final String ATTR_SRCEXPR = "srcexpr";
    public static final String ATTR_TARGET = "target";
    public static final String ATTR_TARGETEXPR = "targetexpr";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_TYPEEXPR = "typeexpr";
    public static final String ATTR_VERSION = "version";

    // Commons SCXML XML Attribute names
    public static final String ATTR_EXMODE = "exmode";
    public static final String ATTR_HINTS = "hints";
}
