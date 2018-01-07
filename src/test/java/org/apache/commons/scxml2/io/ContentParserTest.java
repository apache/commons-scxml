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
package org.apache.commons.scxml2.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Assert;
import org.junit.Test;

public class ContentParserTest {

    @Test
    public void testTrimContent() throws Exception {
        Assert.assertEquals(null, ContentParser.trimContent(null));
        Assert.assertEquals("", ContentParser.trimContent(""));
        Assert.assertEquals("", ContentParser.trimContent(" "));
        Assert.assertEquals("", ContentParser.trimContent("  "));
        Assert.assertEquals("", ContentParser.trimContent("   "));
        Assert.assertEquals("", ContentParser.trimContent("\t\n\r"));
        Assert.assertEquals("a", ContentParser.trimContent("a"));
        Assert.assertEquals("a", ContentParser.trimContent(" a"));
        Assert.assertEquals("a", ContentParser.trimContent("a "));
        Assert.assertEquals("a", ContentParser.trimContent(" a "));
    }

    @Test
    public void testSpaceNormalizeContent() throws Exception {
        Assert.assertEquals(null, ContentParser.spaceNormalizeContent(null));
        Assert.assertEquals("", ContentParser.spaceNormalizeContent(""));
        Assert.assertEquals("a", ContentParser.spaceNormalizeContent("a"));
        Assert.assertEquals("a", ContentParser.spaceNormalizeContent(" a"));
        Assert.assertEquals("a", ContentParser.spaceNormalizeContent("a "));
        Assert.assertEquals("a", ContentParser.spaceNormalizeContent(" a "));
        Assert.assertEquals("a b c", ContentParser.spaceNormalizeContent("  a\tb \n \r c  "));
    }

    @Test
    public void testParseJson() throws Exception {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        // not by default configured, but much easier for unit-testing Java embedded JSON Strings
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        ContentParser contentParser = new ContentParser(jsonObjectMapper);

        String jsonObjectString = "{ /*comment*/ 'string' : 'foobar', 'int' : 1, 'boolean' : false, 'null' : null }";
        LinkedHashMap<String, Object> jsonObject = new LinkedHashMap<>();
        jsonObject.put("string", "foobar");
        jsonObject.put("int", 1);
        jsonObject.put("boolean", Boolean.FALSE);
        jsonObject.put("null", null);
        Assert.assertEquals(jsonObject, contentParser.parseJson(jsonObjectString));

        String jsonArrayString = "[" + jsonObjectString + "," + "# yaml comment\n" + jsonObjectString+"]";
        ArrayList<Object> jsonArray = new ArrayList<>(2);
        jsonArray.add(jsonObject);
        jsonArray.add(jsonObject);
        Assert.assertEquals(jsonArray, contentParser.parseJson(jsonArrayString));
    }
}
