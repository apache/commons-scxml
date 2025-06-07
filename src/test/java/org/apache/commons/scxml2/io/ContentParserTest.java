/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.scxml2.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContentParserTest {

    @Test
    void testParseJson() throws Exception {
        final ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        // not by default configured, but much easier for unit-testing Java embedded JSON Strings
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        final ContentParser contentParser = new ContentParser(jsonObjectMapper);

        final String jsonObjectString = "{ /*comment*/ 'string' : 'foobar', 'int' : 1, 'boolean' : false, 'null' : null }";
        final LinkedHashMap<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("string", "foobar");
        jsonObject.put("int", 1);
        jsonObject.put("boolean", Boolean.FALSE);
        jsonObject.put("null", null);
        Assertions.assertEquals(jsonObject, contentParser.parseJson(jsonObjectString));

        final String jsonArrayString = "[" + jsonObjectString + "," + "# yaml comment\n" + jsonObjectString+"]";
        final ArrayList<Object> jsonArray = new ArrayList<>(2);
        jsonArray.add(jsonObject);
        jsonArray.add(jsonObject);
        Assertions.assertEquals(jsonArray, contentParser.parseJson(jsonArrayString));
    }

    @Test
    void testSpaceNormalizeContent() {
        Assertions.assertNull(ContentParser.spaceNormalizeContent(null));
        Assertions.assertEquals("", ContentParser.spaceNormalizeContent(""));
        Assertions.assertEquals("a", ContentParser.spaceNormalizeContent("a"));
        Assertions.assertEquals("a", ContentParser.spaceNormalizeContent(" a"));
        Assertions.assertEquals("a", ContentParser.spaceNormalizeContent("a "));
        Assertions.assertEquals("a", ContentParser.spaceNormalizeContent(" a "));
        Assertions.assertEquals("a b c", ContentParser.spaceNormalizeContent("  a\tb \n \r c  "));
    }

    @Test
    void testTrimContent() {
        Assertions.assertNull(ContentParser.trimContent(null));
        Assertions.assertEquals("", ContentParser.trimContent(""));
        Assertions.assertEquals("", ContentParser.trimContent(" "));
        Assertions.assertEquals("", ContentParser.trimContent("  "));
        Assertions.assertEquals("", ContentParser.trimContent("   "));
        Assertions.assertEquals("", ContentParser.trimContent("\t\n\r"));
        Assertions.assertEquals("a", ContentParser.trimContent("a"));
        Assertions.assertEquals("a", ContentParser.trimContent(" a"));
        Assertions.assertEquals("a", ContentParser.trimContent("a "));
        Assertions.assertEquals("a", ContentParser.trimContent(" a "));
    }
}
