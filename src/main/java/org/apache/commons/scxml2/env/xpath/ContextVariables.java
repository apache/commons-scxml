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
package org.apache.commons.scxml2.env.xpath;

import org.apache.commons.jxpath.Variables;
import org.apache.commons.scxml2.Context;

/**
 * JXPath Variables mapping for SCXML Context
 */
public class ContextVariables implements Variables {

    private final Context ctx;

    public ContextVariables(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean isDeclaredVariable(final String varName) {
        return ctx.has(varName);
    }

    @Override
    public Object getVariable(final String varName) {
        return ctx.get(varName);
    }

    @Override
    public void declareVariable(final String varName, final Object value) {
        ctx.set(varName, value);
    }

    @Override
    public void undeclareVariable(final String varName) {
        if (ctx.has(varName)) {
            Context cctx = ctx;
            while (!cctx.hasLocal(varName)) {
                cctx = cctx.getParent();
                if (cctx == null) {
                    return;
                }
            }
            cctx.getVars().remove(varName);
        }
    }
}
