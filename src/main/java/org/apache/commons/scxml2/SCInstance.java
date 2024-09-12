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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.scxml2.env.SimpleContext;
import org.apache.commons.scxml2.env.javascript.JSEvaluator;
import org.apache.commons.scxml2.io.ContentParser;
import org.apache.commons.scxml2.model.Data;
import org.apache.commons.scxml2.model.Datamodel;
import org.apache.commons.scxml2.model.EnterableState;
import org.apache.commons.scxml2.model.History;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.commons.scxml2.model.TransitionalState;
import org.apache.commons.scxml2.semantics.ErrorConstants;

/**
 * The <code>SCInstance</code> performs book-keeping functions for
 * a particular execution of a state chart represented by a
 * <code>SCXML</code> object.
 */
public class SCInstance implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * SCInstance cannot be initialized without setting a state machine.
     */
    private static final String ERR_NO_STATE_MACHINE = "SCInstance: State machine not set";

    /**
     * SCInstance cannot be initialized without setting an error reporter.
     */
    private static final String ERR_NO_ERROR_REPORTER = "SCInstance: ErrorReporter not set";

    /**
     * Flag indicating the state machine instance has been initialized (before).
     */
    private boolean initialized;

    /**
     * The stateMachine being executed.
     */
    private SCXML stateMachine;

    /**
     * The current state configuration of the state machine
     */
    private final StateConfiguration stateConfiguration;

    /**
     * The current status of the stateMachine.
     */
    private final Status currentStatus;

    /**
     * Running status for this state machine
     */
    private boolean running;

    /**
     * The SCXML I/O Processor for the internal event queue
     */
    private transient SCXMLIOProcessor internalIOProcessor;

    /**
     * The Evaluator used for this state machine instance.
     */
    private transient Evaluator evaluator;

    /**
     * The error reporter.
     */
    private transient ErrorReporter errorReporter = null;

    /**
     * The map of contexts per EnterableState.
     */
    private final Map<EnterableState, Context> contexts = new HashMap<>();

    /**
     * The map of last known configurations per History.
     */
    private final Map<History, Set<EnterableState>> histories = new HashMap<>();

    /**
     * The root context.
     */
    private Context rootContext;

    /**
     * The wrapped system context.
     */
    private SCXMLSystemContext systemContext;

    /**
     * The global context
     */
    private Context globalContext;

    /**
     * Flag indicating if the globalContext is shared between all states (a single flat context, default false)
     */
    private boolean singleContext;

    /**
     * Constructor
     * @param internalIOProcessor The I/O Processor for the internal event queue
     * @param evaluator The evaluator
     * @param errorReporter The error reporter
     */
    protected SCInstance(final SCXMLIOProcessor internalIOProcessor, final Evaluator evaluator,
                         final ErrorReporter errorReporter) {
        this.internalIOProcessor = internalIOProcessor;
        this.evaluator = evaluator;
        this.errorReporter = errorReporter;
        this.stateConfiguration = new StateConfiguration();
        this.currentStatus = new Status(stateConfiguration);
    }

    /**
     * Clone data model.
     *
     * @param ctx The context to clone to.
     * @param datamodel The datamodel to clone.
     * @param evaluator The expression evaluator.
     * @param errorReporter The error reporter
     */
    protected void cloneDatamodel(final Datamodel datamodel, final Context ctx, final Evaluator evaluator,
                                      final ErrorReporter errorReporter) {
        if (datamodel == null || Evaluator.NULL_DATA_MODEL.equals(evaluator.getSupportedDatamodel())) {
            return;
        }
        final List<Data> data = datamodel.getData();
        if (data == null) {
            return;
        }
        for (final Data datum : data) {
            if (getGlobalContext() == ctx && ctx.has(datum.getId())) {
                // earlier/externally defined 'initial' value found: do not overwrite
                continue;
            }
            Object value = null;
            boolean setValue = false;
            // prefer "src" over "expr" over "inline"
            if (datum.getSrc() != null) {
                String resolvedSrc = datum.getSrc();
                final PathResolver pr = getStateMachine().getPathResolver();
                if (pr != null) {
                    resolvedSrc = pr.resolvePath(resolvedSrc);
                }
                try {
                    datum.setParsedValue(ContentParser.DEFAULT_PARSER.parseResource(resolvedSrc));
                    value = evaluator.cloneData(datum.getParsedValue().getValue());
                    setValue = true;
                } catch (final IOException e) {
                    if (internalIOProcessor != null) {
                        internalIOProcessor.addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
                    }
                    errorReporter.onError(ErrorConstants.EXECUTION_ERROR, e.getMessage(), datum);
                }
            }
            else if (datum.getExpr() != null) {
                try {
                    value = evaluator.eval(ctx, datum.getExpr());
                    setValue = true;
                } catch (final SCXMLExpressionException see) {
                    if (internalIOProcessor != null) {
                        internalIOProcessor.addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
                    }
                    errorReporter.onError(ErrorConstants.EXPRESSION_ERROR, see.getMessage(), datum);
                }
            } else {
                if (datum.getParsedValue() != null) {
                    value = evaluator.cloneData(datum.getParsedValue().getValue());
                }
                setValue = true;
            }
            if (setValue) {
                if (evaluator instanceof JSEvaluator) {
                    // the Javascript engine (Nashorn) may require special handling/wrapping of data objects which
                    // directly injecting them in the context.
                    try {
                        ((JSEvaluator)evaluator).injectData(ctx, datum.getId(), value);
                    } catch (final SCXMLExpressionException e) {
                        if (internalIOProcessor != null) {
                            internalIOProcessor.addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
                        }
                        errorReporter.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), datum);
                    }
                } else {
                    ctx.setLocal(datum.getId(), value);
                }
            }
        }
    }

    /**
     * Detach this state machine instance to allow external serialization.
     * <p>
     * This clears the internal I/O processor, evaluator and errorReporter members.
     * </p>
     */
    protected void detach() {
        this.internalIOProcessor = null;
        this.evaluator = null;
        this.errorReporter = null;
    }

    /**
     * Gets the context for an EnterableState or create one if not created before.
     *
     * @param state The EnterableState.
     * @return The context.
     */
    public Context getContext(final EnterableState state) {
        Context context = contexts.get(state);
        if (context == null) {
            if (singleContext) {
                context = getGlobalContext();
            }
            else {
                final EnterableState parent = state.getParent();
                if (parent == null) {
                    // docroot
                    context = evaluator.newContext(getGlobalContext());
                } else {
                    context = evaluator.newContext(getContext(parent));
                }
            }
            if (state instanceof TransitionalState) {
                final Datamodel datamodel = ((TransitionalState)state).getDatamodel();
                cloneDatamodel(datamodel, context, evaluator, errorReporter);
            }
            contexts.put(state, context);
        }
        return context;
    }

    /**
     * @return Returns the current status for this instance
     */
    public Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * @return Return the current evaluator
     */
    protected Evaluator getEvaluator() {
        return evaluator;
    }

    /**
     * @return Returns the global context, which is the top context <em>within</em> the state machine.
     */
    public Context getGlobalContext() {
        if (globalContext == null) {
            // force initialization of systemContext
            getSystemContext();
            if (systemContext != null) {
                globalContext = evaluator.newContext(systemContext);
            }
        }
        return globalContext;
    }

    /**
     * Gets the last configuration for this history.
     *
     * @param history The history.
     * @return Returns the lastConfiguration.
     */
    public Set<EnterableState> getLastConfiguration(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        if (lastConfiguration == null) {
            lastConfiguration = Collections.emptySet();
        }
        return lastConfiguration;
    }

    /**
     * Gets the root context.
     *
     * @return The root context.
     */
    public Context getRootContext() {
        if (rootContext == null) {
            rootContext = new SimpleContext();
        }
        return rootContext;
    }

    /**
     * @return Returns the state configuration for this instance
     */
    public StateConfiguration getStateConfiguration() {
        return stateConfiguration;
    }

    /**
     * @return Return the state machine for this instance
     */
    public SCXML getStateMachine() {
        return stateMachine;
    }

    /**
     * Gets the unwrapped (modifiable) system context.
     *
     * @return The unwrapped system context.
     */
    public Context getSystemContext() {
        if (systemContext == null) {
            // force initialization of rootContext
            getRootContext();
            if (rootContext != null) {
                final Context internalContext = new SimpleContext(rootContext);
                systemContext = new SCXMLSystemContext(internalContext);
                systemContext.getContext().set(SCXMLSystemContext.SESSIONID_KEY, UUID.randomUUID().toString());
                final String name = stateMachine != null && stateMachine.getName() != null ? stateMachine.getName() : "";
                systemContext.getContext().set(SCXMLSystemContext.SCXML_NAME_KEY, name);
                systemContext.getPlatformVariables().put(SCXMLSystemContext.STATUS_KEY, currentStatus);
            }
        }
        return systemContext != null ? systemContext.getContext() : null;
    }

    /**
     * (re)Initializes the state machine instance, clearing all variable contexts, histories and current status,
     * and clones the SCXML root datamodel into the root context.
     * @throws ModelException if the state machine hasn't been setup for this instance
     */
    protected void initialize() throws ModelException {
        running = false;
        if (stateMachine == null) {
            throw new ModelException(ERR_NO_STATE_MACHINE);
        }
        if (evaluator == null) {
            evaluator = EvaluatorFactory.getEvaluator(stateMachine);
        }
        if (evaluator.requiresGlobalContext()) {
            singleContext = true;
        }
        if (stateMachine.getDatamodelName() != null && !stateMachine.getDatamodelName().equals(evaluator.getSupportedDatamodel())) {
            throw new ModelException("Incompatible SCXML document datamodel \""+stateMachine.getDatamodelName()+"\""
                    + " for evaluator "+evaluator.getClass().getName()+" supported datamodel \""+evaluator.getSupportedDatamodel()+"\"");
        }
        if (errorReporter == null) {
            throw new ModelException(ERR_NO_ERROR_REPORTER);
        }
        systemContext = null;
        globalContext = null;
        contexts.clear();
        histories.clear();
        stateConfiguration.clear();

        initialized = true;
    }

    protected void initializeDatamodel(final Map<String, Object> data) {
        if (globalContext == null) {
            // Clone root datamodel
            final Datamodel rootdm = stateMachine.getDatamodel();
            cloneDatamodel(rootdm, getGlobalContext(), evaluator, errorReporter);

            // initialize/override global context data
            if (data != null) {
                for (final String key : data.keySet()) {
                    if (globalContext.has(key)) {
                        globalContext.set(key, data.get(key));
                    }
                }
            }
            if (stateMachine.isLateBinding() == null || Boolean.FALSE.equals(stateMachine.isLateBinding())) {
                // early binding
                for (final EnterableState es : stateMachine.getChildren()) {
                    getContext(es);
                }
            }
        }
    }

    /**
     * @return Returns if the state machine is running
     */
    public boolean isRunning() {
        return running;
    }

    public boolean isSingleContext() {
        return singleContext;
    }

    /**
     * Gets the context for an EnterableState if available.
     *
     * <p>Note: used for testing purposes only</p>
     *
     * @param state The EnterableState
     * @return The context or null if not created yet.
     */
    Context lookupContext(final EnterableState state) {
        return contexts.get(state);
    }

    /**
     * Resets the history state.
     *
     * <p>Note: used for testing purposes only</p>
     *
     * @param history The history.
     */
    public void resetConfiguration(final History history) {
        histories.remove(history);
    }

    /**
     * Sets the context for an EnterableState
     *
     * <p>Note: used for testing purposes only</p>
     *
     * @param state The EnterableState.
     * @param context The context.
     */
    void setContext(final EnterableState state,
            final Context context) {
        contexts.put(state, context);
    }

    /**
     * Sets or re-attach the error reporter
     * @param errorReporter The error reporter for this state machine instance.
     * @throws ModelException if an attempt is made to set a null value for the error reporter
     */
    protected void setErrorReporter(final ErrorReporter errorReporter) throws ModelException {
        if (errorReporter == null) {
            throw new ModelException(ERR_NO_ERROR_REPORTER);
        }
        this.errorReporter = errorReporter;
    }

    /**
     * Sets or re-attach the evaluator
     * <p>
     * If not re-attaching and this state machine instance has been initialized before,
     * it will be initialized again, destroying all existing state!
     * </p>
     * @param evaluator The evaluator for this state machine instance
     * @param reAttach Flag whether or not re-attaching it
     * @throws ModelException if {@code evaluator} is null
     */
    protected void setEvaluator(final Evaluator evaluator, final boolean reAttach) throws ModelException {
        this.evaluator = evaluator;
        if (initialized) {
            if (!reAttach) {
                // change of evaluator after initialization: re-initialize
                initialize();
            }
            else if (evaluator == null) {
                throw new ModelException("SCInstance: re-attached without Evaluator");
            }
        }
    }

    /**
     * Sets the I/O Processor for the internal event queue
     * @param internalIOProcessor the I/O Processor
     */
    protected void setInternalIOProcessor(final SCXMLIOProcessor internalIOProcessor) {
        this.internalIOProcessor = internalIOProcessor;
    }

    /**
     * Sets the last configuration for this history.
     *
     * @param history The history.
     * @param lc The lastConfiguration to set.
     */
    public void setLastConfiguration(final History history,
            final Set<EnterableState> lc) {
        histories.put(history, new HashSet<>(lc));
    }

    /**
     * Sets or replace the root context.
     * @param context The new root context.
     */
    protected void setRootContext(final Context context) {
        this.rootContext = context;
        // force initialization of rootContext
        getRootContext();
        if (systemContext != null) {
            // re-parent the system context
            systemContext.setSystemContext(new SimpleContext(rootContext));
        }
    }

    public void setSingleContext(final boolean singleContext) throws ModelException {
        if (initialized) {
            throw new ModelException("SCInstance: already initialized");
        }
        this.singleContext = singleContext;
    }

    /**
     * Sets the state machine for this instance.
     * <p>
     * If this state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     * @param stateMachine The state machine for this instance
     * @throws ModelException if an attempt is made to set a null value for the state machine
     */
    protected void setStateMachine(final SCXML stateMachine) throws ModelException {
        if (stateMachine == null) {
            throw new ModelException(ERR_NO_STATE_MACHINE);
        }
        this.stateMachine = stateMachine;
        initialize();
    }

    /**
     * Starts the state machine, {@link #isRunning()} hereafter will return true
     * @throws IllegalStateException Exception thrown if trying to start the state machine when in a Final state
     */
    public void start() throws IllegalStateException {
        if (!this.running &&  currentStatus.isFinal()) {
            throw new IllegalStateException("The state machine is in a Final state and cannot be set running again");
        }
        this.running = true;
    }

    /**
     * Stops the state machine, {@link #isRunning()} hereafter will return false
     */
    public void stop() {
        this.running = false;
    }
}

