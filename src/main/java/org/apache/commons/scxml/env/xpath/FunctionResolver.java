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
package org.apache.commons.scxml.env.xpath;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;

import org.apache.commons.scxml.Builtin;
import org.apache.commons.scxml.Context;
import org.apache.commons.scxml.model.TransitionTarget;

/**
 * A {@link XPathFunctionResolver} for the Commons SCXML environment.
 *
 */
public class FunctionResolver implements XPathFunctionResolver, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = -1735881753812903834L;

    /** The Commons SCXML namespace. */
    private static final String NAMESPACE_COMMONS_SCXML =
        "http://commons.apache.org/scxml";
    /** The {@link Context} key to retrieve all the current states. */
    private static final String STATES = "_ALL_STATES";
    /** The {@link Context} key to retrieve all the current namespaces. */
    private static final String NAMESPACES = "_ALL_NAMESPACES";

    /** Functions map. */
    private final Map<FunctionKey, XPathFunction> functions =
        new HashMap<FunctionKey, XPathFunction>();
    /** In() function. */
    private final XPathFunction inFct = new InFunction();
    /** Data() function. */
    private final XPathFunction dataFct = new DataFunction();
    /** DataNode() function. */
    private final XPathFunction dataNodeFct = new DataNodeFunction();
    /** The execution context for the functions. */
    private XPathContext xctx;

    /**
     * Constructor.
     */
    public FunctionResolver() {
        functions.put(new FunctionKey(new QName(NAMESPACE_COMMONS_SCXML,
                "In"), 1), inFct);
        functions.put(new FunctionKey(new QName(NAMESPACE_COMMONS_SCXML,
                "Data"), 2), dataFct);
        functions.put(new FunctionKey(new QName(NAMESPACE_COMMONS_SCXML,
                "DataNode"), 1), dataNodeFct);
    }

    /** 
     * @see XPathFunctionResolver#resolveFunction(QName, int)
     */
    @Override
    public XPathFunction resolveFunction(final QName functionName,
            final int arity) {
        return functions.get(new FunctionKey(functionName, arity));
    }

    /**
     * Set the execution context for the subsequent functions.
     */
    void setContext(final XPathContext xctx) {
        this.xctx = xctx;
    }

    /**
     * Add these user defined XPath functions.
     */
    void addFunctions(final Map<FunctionKey, XPathFunction> fns) {
        for (Map.Entry<FunctionKey, XPathFunction> entry : fns.entrySet()) {
            functions.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * An {@link XPathFunction} for the SCXML In() function.
     *
     */
    private final class InFunction implements XPathFunction, Serializable {

        /** Serial version UID. */
        private static final long serialVersionUID = 6580800854875437013L;

        /**
         * @see XPathFunction#evaluate(List)
         * @see Builtin#isMember(Set, String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(final List args) throws XPathFunctionException {
            Set<TransitionTarget> allStates =
                (Set<TransitionTarget>) xctx.get(STATES);
            return Builtin.isMember(allStates, (String) args.get(0));
        }

    }

    /**
     * An {@link XPathFunction} for the Commons SCXML Data() function.
     *
     */
    private final class DataFunction implements XPathFunction, Serializable {

        /** Serial version UID. */
        private static final long serialVersionUID = 6812793739418439523L;

        /**
         * @see XPathFunction#evaluate(List)
         * @see Builtin#data(Map, Object, String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(final List args) throws XPathFunctionException {
            Map<String, String> namespaces =
                (Map<String, String>) xctx.get(NAMESPACES);
            Object node = xctx.get((String) args.get(0));
            return Builtin.data(namespaces, node, (String) args.get(1));
        }

    }


    /**
     * An {@link XPathFunction} for the Commons SCXML LData() function.
     *
     */
    private final class DataNodeFunction implements XPathFunction, Serializable {

        /** Serial version UID. */
        private static final long serialVersionUID = 2407212352705223138L;

        /**
         * @see XPathFunction#evaluate(List)
         * @see Builtin#dataNode(Map, Object, String)
         */
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(final List args) throws XPathFunctionException {
            Map<String, String> namespaces =
                (Map<String, String>) xctx.get(NAMESPACES);
            Object node = xctx.get((String) args.get(0));
            return Builtin.dataNode(namespaces, node, (String) args.get(1));
        }

    }

    /**
     * The keys used by the {@link FunctionResolver} to store XPath function
     * definitions.
     *
     */
    public static final class FunctionKey implements Serializable {
        
        /** Serial version UID. */
        private static final long serialVersionUID = 5977444894712979556L;
        /** The constant for manipulating key hashes. */
        private static final int HASH_CONSTANT = 37;

        /** The QName for the XPath function. */
        private final QName name;
        /** The number of arguments for the XPath function. */
        private final int arity;

        /**
         * Constructor.
         *
         * @param name The QName used to identify the XPath function.
         * @param arity The number of arguments accepted by the XPath
         *              function.
         */
        public FunctionKey(final QName name, final int arity) {
            if (name == null || name.getLocalPart() == null) {
                throw new IllegalArgumentException("QName or its" +
                        " local part cannot be null");
            }
            this.name = name;
            this.arity = arity;
        }

        /**
         * Get the {@link QName} associated with this function key.
         *
         * @return The QName associated with this key
         */
        public QName getQName() {
            return name;
        }

        /**
         * Get the arity associated with this function key.
         *
         * @return The arity associated with this key
         */
        public int getArity() {
            return arity;
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof FunctionKey)) {
                return false;
            }
            FunctionKey key = (FunctionKey) other;
            if (name.toString().equals(key.getQName().toString()) &&
                    arity == key.getArity()) {
                return true;
            }
            return false;
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return name.toString().hashCode() + HASH_CONSTANT * arity;
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return "FunctionKey[ qName=" + name.toString() +
                ", arity=" + arity + " ]";
        }

    }

}
