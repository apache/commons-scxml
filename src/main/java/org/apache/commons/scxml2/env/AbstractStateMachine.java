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
package org.apache.commons.scxml2.env;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.scxml2.Context;
import org.apache.commons.scxml2.Evaluator;
import org.apache.commons.scxml2.EventBuilder;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.SCXMLListener;
import org.apache.commons.scxml2.TriggerEvent;
import org.apache.commons.scxml2.env.jexl.JexlContext;
import org.apache.commons.scxml2.env.jexl.JexlEvaluator;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.Transition;
import org.apache.commons.scxml2.model.TransitionTarget;

/**
 * <p>This class demonstrates one approach for providing the base
 * functionality needed by classes representing stateful entities,
 * whose behaviors are defined via SCXML documents.</p>
 *
 * <p>SCXML documents (more generically, UML state chart diagrams) can be
 * used to define stateful behavior of objects, and Commons SCXML enables
 * developers to use this model directly into the corresponding code
 * artifacts. The resulting artifacts tend to be much simpler, embody
 * a useful separation of concerns and are easier to understand and
 * maintain. As the size of the modeled entity grows, these benefits
 * become more apparent.</p>
 *
 * <p>This approach functions by registering an SCXMLListener that gets
 * notified onentry, and calls the namesake method for each state that
 * has been entered.</p>
 *
 * <p>This class swallows all exceptions only to log them. Developers of
 * subclasses should think of themselves as &quot;component developers&quot;
 * catering to other end users, and therefore ensure that the subclasses
 * are free of <code>ModelException</code>s and the like. Most methods
 * are <code>protected</code> for ease of subclassing.</p>
 *
 */
public abstract class AbstractStateMachine {

    /**
     * The state machine that will drive the instances of this class.
     */
    private SCXML stateMachine;

    /**
     * The instance specific SCXML engine.
     */
    private SCXMLExecutor engine;

    /**
     * The log.
     */
    private Log log;

    /**
     * The method signature for the activities corresponding to each
     * state in the SCXML document.
     */
    private static final Class<?>[] SIGNATURE = new Class[0];

    /**
     * The method parameters for the activities corresponding to each
     * state in the SCXML document.
     */
    private static final Object[] PARAMETERS = new Object[0];

    /**
     * Convenience constructor, object instantiation incurs parsing cost.
     *
     * @param scxmlDocument The URL pointing to the SCXML document that
     *                      describes the &quot;lifecycle&quot; of the
     *                      instances of this class.
     * @throws ModelException in case there is a fatal SCXML object model problem
     */
    public AbstractStateMachine(final URL scxmlDocument) throws ModelException {
        // default is JEXL
        this(scxmlDocument, new JexlContext(), new JexlEvaluator());
    }

    /**
     * Primary constructor, object instantiation incurs parsing cost.
     *
     * @param scxmlDocument The URL pointing to the SCXML document that
     *                      describes the &quot;lifecycle&quot; of the
     *                      instances of this class.
     * @param rootCtx The root context for this instance.
     * @param evaluator The expression evaluator for this instance.
     * @throws ModelException in case there is a fatal SCXML object model problem
     *
     * @see Context
     * @see Evaluator
     */
    public AbstractStateMachine(final URL scxmlDocument,
            final Context rootCtx, final Evaluator evaluator) throws ModelException {
        log = LogFactory.getLog(this.getClass());
        try {
            stateMachine = SCXMLReader.read(scxmlDocument);
        } catch (final IOException | XMLStreamException | ModelException ioe) {
            logError(ioe);
        }
        initialize(stateMachine, rootCtx, evaluator);
    }

    /**
     * Convenience constructor.
     *
     * @param stateMachine The parsed SCXML instance that
     *                     describes the &quot;lifecycle&quot; of the
     *                     instances of this class.
     * @throws ModelException in case there is a fatal SCXML object model problem
     * @since 0.7
     */
    public AbstractStateMachine(final SCXML stateMachine) throws ModelException {
        // default is JEXL
        this(stateMachine, new JexlContext(), new JexlEvaluator());
    }

    /**
     * Primary constructor.
     *
     * @param stateMachine The parsed SCXML instance that
     *                     describes the &quot;lifecycle&quot; of the
     *                     instances of this class.
     * @param rootCtx The root context for this instance.
     * @param evaluator The expression evaluator for this instance.
     * @throws ModelException in case there is a fatal SCXML object model problem
     * @see Context
     * @see Evaluator
     *
     * @since 0.7
     */
    public AbstractStateMachine(final SCXML stateMachine,
            final Context rootCtx, final Evaluator evaluator) throws ModelException {
        log = LogFactory.getLog(this.getClass());
        initialize(stateMachine, rootCtx, evaluator);
    }

    /**
     * Instantiate and initialize the underlying executor instance.
     *
     * @param stateMachine The state machine
     * @param rootCtx The root context
     * @param evaluator The expression evaluator
     * @throws ModelException in case there is a fatal SCXML object model problem
     */
    private void initialize(final SCXML stateMachine,
            final Context rootCtx, final Evaluator evaluator) throws ModelException {
        engine = new SCXMLExecutor(evaluator, new SimpleDispatcher(),
            new SimpleErrorReporter());
        engine.setStateMachine(stateMachine);
        engine.setRootContext(rootCtx);
        engine.addListener(stateMachine, new EntryListener());
        try {
            engine.go();
        } catch (final ModelException me) {
            logError(me);
        }
    }

    /**
     * Fire an event on the SCXML engine.
     *
     * @param event The event name.
     * @return Whether the state machine has reached a &quot;final&quot;
     *         configuration.
     */
    public boolean fireEvent(final String event) {
        final TriggerEvent[] evts = {new EventBuilder(event, TriggerEvent.SIGNAL_EVENT).build()};
        try {
            engine.triggerEvents(evts);
        } catch (final ModelException me) {
            logError(me);
        }
        return engine.getStatus().isFinal();
    }

    /**
     * Get the SCXML engine driving the &quot;lifecycle&quot; of the
     * instances of this class.
     *
     * @return Returns the engine.
     */
    public SCXMLExecutor getEngine() {
        return engine;
    }

    /**
     * Get the log for this class.
     *
     * @return Returns the log.
     */
    public Log getLog() {
        return log;
    }

    /**
     * Set the log for this class.
     *
     * @param log The log to set.
     */
    public void setLog(final Log log) {
        this.log = log;
    }

    /**
     * Invoke the no argument method with the following name.
     *
     * @param methodName The method to invoke.
     * @return Whether the invoke was successful.
     */
    public boolean invoke(final String methodName) {
        final Class<?> clas = this.getClass();
        try {
            final Method method = clas.getDeclaredMethod(methodName, SIGNATURE);
            method.invoke(this, PARAMETERS);
        } catch (final SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                InvocationTargetException se) {
            logError(se);
            return false;
        }
        return true;
    }

    /**
     * Reset the state machine.
     *
     * @return Whether the reset was successful.
     */
    public boolean resetMachine() {
        try {
            engine.reset();
        } catch (final ModelException me) {
            logError(me);
            return false;
        }
        return true;
    }

    /**
     * Utility method for logging error.
     *
     * @param exception The exception leading to this error condition.
     */
    protected void logError(final Exception exception) {
        if (log.isErrorEnabled()) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * A SCXMLListener that is only concerned about &quot;onentry&quot;
     * notifications.
     */
    protected class EntryListener implements SCXMLListener {

        /**
         * {@inheritDoc}
         */
        public void onEntry(final EnterableState entered) {
            invoke(entered.getId());
        }

        /**
         * No-op.
         *
         * @param from The &quot;source&quot; transition target.
         * @param to The &quot;destination&quot; transition target.
         * @param transition The transition being followed.
         * @param event The event triggering the transition
         */
        public void onTransition(final TransitionTarget from,
                final TransitionTarget to, final Transition transition, final String event) {
            // nothing to do
        }

        /**
         * No-op.
         *
         * @param exited The state being exited.
         */
        public void onExit(final EnterableState exited) {
            // nothing to do
        }

    }

}

