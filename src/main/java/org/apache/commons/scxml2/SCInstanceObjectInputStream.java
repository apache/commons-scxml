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
package org.apache.commons.scxml2;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Extended ObjectInputStream to be used for de-serializing SCInstance.
 * <p>
 * This class allows configuring a custom {@link ClassResolver} callback to dynamically resolve a deserialization class,
 * which is needed for SCXML languages like Groovy which need to dynamically lookup custom Groovy classes.
 * </p>
 */
public class SCInstanceObjectInputStream extends ObjectInputStream {

    /** ClassResolver Callback interface */
    public interface ClassResolver {

        /**
         * Callback method invoked from {@link SCInstanceObjectInputStream#resolveClass(ObjectStreamClass)}
         * @param osc an instance of class ObjectStreamClass
         * @return a Class object corresponding to osc
         * @throws IOException if an I/O error occurs while reading the instance of class ObjectStreamClass
         * @throws ClassNotFoundException if the class is not found while reading the instance of class ObjectStreamClass
         */
        Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException;
    }

    /**
     * current classresolver callback
     */
    private ClassResolver classResolver;

    /**
     * Default constructor
     * @param in Inputstream to use
     * @throws IOException if an I/O error occurs while reading the stream
     */
    public SCInstanceObjectInputStream(final InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class resolveClass(final ObjectStreamClass osc) throws IOException, ClassNotFoundException {
        if (classResolver != null) {
            return classResolver.resolveClass(osc);
        }
        return super.resolveClass(osc);
    }

    /**
     * Sets custom class resolver callback, or null when no longer needed.
     * <p>
     * Typically usage:
     * <pre><code>
     * private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
     *     ClassResolver currentClassResolver = null;
     *     try {
     *         if (in instanceof SCInstanceObjectInputStream) {
     *             currentClassResolver = ((SCInstanceObjectInputStream)in).setClassResolver(customClassResolver);
     *         }
     *         ... // read Object(s)
     *     }
     *     finally {
     *         if (in instanceof SCInstanceObjectInputStream) {
     *             ((SCInstanceObjectInputStream)in).setClassResolver(currentClassResolver);
     *         }
     *     }
     * }
     * </code></pre>
     * @see org.apache.commons.scxml2.env.groovy.GroovyContext#readObject(ObjectInputStream)
     * @param classResolver custom class resolver
     * @return the old classResolver replaced by the new {@code classResolver}
     */
    public ClassResolver setClassResolver(final ClassResolver classResolver) {
        final ClassResolver old = this.classResolver;
        this.classResolver = classResolver;
        return old;
    }
}
