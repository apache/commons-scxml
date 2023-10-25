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
package org.apache.commons.scxml2.env.jexl;

import java.util.ArrayList;
import java.util.List;

public class JexlEvaluatorBuilder {

    private final List<String> allowedPackages = new ArrayList<>();
    private final List<String> deniedClasses = new ArrayList<>();

    /**
     * Adds a whole package to the JEXL permissions and allows to use this classes in JEXL expressions.
     *
     * @param fullQualifiedPackageName expects the complete path f.e. "org.apache.commons.logging"
     */
    public JexlEvaluatorBuilder addAllowedPackage(String fullQualifiedPackageName) {
        allowedPackages.add(fullQualifiedPackageName);
        return this;
    }

    /**
     * Adds a specific class, which is not allowed to be used in JEXL expressions.
     *
     * @param fullQualifiedClassName expects a complete path f.e. "org.apache.commons.logging.Logger"
     */
    public JexlEvaluatorBuilder addDeniedClass(String fullQualifiedClassName) {
        deniedClasses.add(fullQualifiedClassName);
        return this;
    }

    /**
     * Creates a JexlEvaluator by the defined options.
     */
    public JexlEvaluator build() {
        return new JexlEvaluator(this);
    }

    /**
     * Package-private: converts the user given information to JEXL understandable src-list
     */
    String[] getJexlPermissions() {
        List<String> permissions = new ArrayList<>();

        for (String allowedPackage : allowedPackages) {
            permissions.add(allowedPackage.replace(".*", "") + ".*");
        }

        for (String deniedClass : deniedClasses) {
            final int lastDot = deniedClass.lastIndexOf('.');
            String packageName = deniedClass.substring(0, lastDot);
            String className = deniedClass.substring(lastDot + 1);
            permissions.add(packageName + " { " + className + " {} }");
        }

        return permissions.toArray(new String[]{});
    }
}

