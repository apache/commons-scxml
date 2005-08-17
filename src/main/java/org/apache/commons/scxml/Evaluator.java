/*
 *    
 *   Copyright 2004 The Apache Software Foundation.
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
package org.apache.taglibs.rdc.scxml;

/**
 * Interface for a component that may be used by the SCXML engines to
 * evaluate the expressions within the SCXML document. 
 * 
 * @author Jaroslav Gergic
 * @author Rahul Akolkar
 */
public interface Evaluator {
    
    /**
     * Evaluate an expression
     * 
     * @param ctx variable context
     * @param expr expression
     * @return a result of the evaluation
     * @throws SCXMLExpressionException
     */
    public Object eval(Context ctx, String exp) throws SCXMLExpressionException;

    /**
     * Create a new child context.
     * 
     * @param parent parent context
     * @return new child context
     */
    public Context newContext(Context parent);

    /**
     * Evaluate a condition.
     * 
     * @param ctx variable context
     * @param expr expression
     * @return true/false
     * @throws SCXMLExpressionException
     */
    public Boolean evalCond(Context ctx, String expr) throws SCXMLExpressionException;
    
}