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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.scxml2.Evaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A <code>PayloadProvider</code> is an element in the SCXML document
 * that can provide payload data for an event or an external process.
 */
public abstract class PayloadProvider extends Action {

    /**
     * Payload data values wrapper list needed when multiple variable entries use the same names.
     * The multiple values are then wrapped in a list. The PayloadBuilder uses this 'marker' list
     * to distinguish between entry values which are a list themselves and the wrapper list.
     */
    private static class DataValueList extends ArrayList {
    }

    /**
     * Adds an attribute and value to a payload data map.
     * <p>
     * As the SCXML specification allows for multiple payload attributes with the same name, this
     * method takes care of merging multiple values for the same attribute in a list of values.
     * </p>
     * <p>
     * Furthermore, as modifications of payload data on either the sender or receiver side should affect the
     * the other side, attribute values (notably: {@link Node} value only for now) is cloned first before being added
     * to the payload data map. This includes 'nested' values within a {@link NodeList}, {@link List} or {@link Map}.
     * </p>
     * @param attrName the name of the attribute to add
     * @param attrValue the value of the attribute to add
     * @param payload the payload data map to be updated
     */
    @SuppressWarnings("unchecked")
    protected void addToPayload(final String attrName, final Object attrValue, Map<String, Object> payload) {
        DataValueList valueList = null;
        Object value = payload.get(attrName);
        if (value != null) {
            if (value instanceof DataValueList) {
                valueList = (DataValueList)value;
            }
            else {
                valueList = new DataValueList();
                valueList.add(value);
                payload.put(attrName, valueList);
            }
        }
        value = clonePayloadValue(attrValue);
        if (value instanceof List) {
            if (valueList == null) {
                valueList = new DataValueList();
                payload.put(attrName, valueList);
            }
            valueList.addAll((List)value);
        }
        else if (valueList != null) {
            valueList.add(value);
        }
        else {
            payload.put(attrName, value);
        }
    }

    /**
     * Clones a value object for adding to a payload data map.
     * <p>
     * Currently only clones {@link Node} values.
     * </p>
     * <p>
     * If the value object is an instanceof {@link NodeList}, {@link List} or {@link Map}, its elements
     * are also cloned (if possible) through recursive invocation of this same method, and put in
     * a new {@link List} or {@link Map} before returning.
     * </p>
     * @param value the value to be cloned
     * @return the cloned value if it could be cloned or otherwise the unmodified value parameter
     */
    @SuppressWarnings("unchecked")
    protected Object clonePayloadValue(final Object value) {
        if (value != null) {
            if (value instanceof Node) {
                return ((Node)value).cloneNode(true);
            }
            else if (value instanceof NodeList) {
                NodeList nodeList = (NodeList)value;
                ArrayList<Node> list = new ArrayList<Node>();
                for (int i = 0, size = nodeList.getLength(); i < size; i++) {
                    list.add(nodeList.item(i).cloneNode(true));
                }
                return list;
            }
            else if (value instanceof List) {
                ArrayList<Object> list = new ArrayList<Object>();
                for (Object v : (List)value) {
                    list.add(clonePayloadValue(v));
                }
                return list;
            }
            else if (value instanceof Map) {
                HashMap<Object, Object> map = new HashMap<Object, Object>();
                for (Map.Entry<Object,Object> entry : ((Map<Object,Object>)value).entrySet()) {
                    map.put(entry.getKey(), clonePayloadValue(entry.getValue()));
                }
                return map;
            }
            // TODO: cloning other type of data?
        }
        return value;
    }

    /**
     * Converts a payload data map to be used for an event payload.
     * <p>
     * Event payload involving key-value pair attributes for an xpath datamodel requires special handling as the
     * attributes needs to be contained and put in a "data" element under a 'root' Event payload element.
     * </p>
     * <p>
     * For non-xpath datamodels this method simply returns the original payload parameter unmodified.
     * </p>
     * @param evaluator the evaluator to test for which datamodel type this event payload is intended
     * @param payload the payload data map
     * @return payload for an event
     * @throws ModelException
     */
    protected Object makeEventPayload(final Evaluator evaluator, final Map<String, Object> payload)
            throws ModelException {
        if (payload != null && !payload.isEmpty() && Evaluator.XPATH_DATA_MODEL.equals(evaluator.getSupportedDatamodel())) {

            try {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element payloadNode = document.createElement("payload");
                for (Map.Entry<String, Object> entry : payload.entrySet()) {
                    Element dataNode = document.createElement("data");
                    payloadNode.appendChild(dataNode);
                    dataNode.setAttribute("id", entry.getKey());
                    if (entry.getValue() instanceof Node) {
                        dataNode.appendChild(document.importNode((Node)entry.getValue(), true));
                    }
                    else if (entry.getValue() instanceof DataValueList) {
                        for (Object value : ((DataValueList)entry.getValue())) {
                            if (value instanceof Node) {
                                dataNode.appendChild(document.importNode((Node)entry.getValue(), true));
                            }
                            else {
                                dataNode.setTextContent(String.valueOf(value));
                            }
                        }
                    }
                    else if (entry.getValue() != null) {
                        dataNode.setTextContent(String.valueOf(entry.getValue()));
                    }
                }
                return payloadNode;
            }
            catch (ParserConfigurationException pce) {
                throw new ModelException("Cannot instantiate a DocumentBuilder", pce);
            }
        }
        return payload;
    }
}
