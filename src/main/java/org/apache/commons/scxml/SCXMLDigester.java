/*
 *
 *   Copyright 2005 The Apache Software Foundation.
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
package org.apache.commons.scxml;

//import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.NodeCreateRule;
import org.apache.commons.digester.ObjectCreateRule;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.SetNextRule;
import org.apache.commons.digester.SetPropertiesRule;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.scxml.env.URLResolver;
import org.apache.commons.scxml.model.Action;
import org.apache.commons.scxml.model.Assign;
import org.apache.commons.scxml.model.Cancel;
import org.apache.commons.scxml.model.Else;
import org.apache.commons.scxml.model.ElseIf;
import org.apache.commons.scxml.model.Executable;
import org.apache.commons.scxml.model.Exit;
import org.apache.commons.scxml.model.History;
import org.apache.commons.scxml.model.If;
import org.apache.commons.scxml.model.Initial;
import org.apache.commons.scxml.model.Log;
import org.apache.commons.scxml.model.OnEntry;
import org.apache.commons.scxml.model.OnExit;
import org.apache.commons.scxml.model.Parallel;
import org.apache.commons.scxml.model.SCXML;
import org.apache.commons.scxml.model.Send;
import org.apache.commons.scxml.model.State;
import org.apache.commons.scxml.model.Transition;
import org.apache.commons.scxml.model.TransitionTarget;
import org.apache.commons.scxml.model.Var;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 * <p>The SCXMLDigester provides the ability to digest a SCXML document into
 * the Java object model provided in the model package.</p>
 * <p>The SCXMLDigester can be used for:</p>
 * <ol>
 *  <li>Digest a SCXML file into the Commons SCXML Java object model.</li>
 *  <li>Serialize an SCXML object (primarily for debugging).</li>
 * </ol>
 */
public final class SCXMLDigester {

    //---------------------- PUBLIC METHODS ----------------------//
    /**
     * <p>API for standalone usage where the SCXML document is a URL.</p>
     *
     * @param scxmlURL
     *            a canonical absolute URL to parse (relative URLs within the
     *            top level document are to be resovled against this URL).
     * @param errHandler
     *            The SAX ErrorHandler
     * @param evalCtx
     *            the document-level variable context for guard condition
     *            evaluation
     * @param evalEngine
     *            the scripting/expression language engine for creating local
     *            state-level variable contexts (if supported by a given
     *            scripting engine)
     *
     * @return SCXML The SCXML object corresponding to the file argument
     *
     * @see Context
     * @see ErrorHandler
     * @see Evaluator
     * @see PathResolver
     */
    public static SCXML digest(final URL scxmlURL,
            final ErrorHandler errHandler, final Context evalCtx,
            final Evaluator evalEngine) {

        SCXML scxml = null;
        Digester scxmlDigester = SCXMLDigester
                .newInstance(null, new URLResolver(scxmlURL));
        scxmlDigester.setErrorHandler(errHandler);

        try {
            scxml = (SCXML) scxmlDigester.parse(scxmlURL.toString());
        } catch (Exception e) {
            MessageFormat msgFormat = new MessageFormat(ERR_DOC_PARSE_FAIL);
            String errMsg = msgFormat.format(new Object[] {
                scxmlURL.toString(), e.getMessage()
            });
            log.error(errMsg, e);
        }

        if (scxml != null) {
            updateSCXML(scxml, evalCtx, evalEngine);
        }

        return scxml;

    }

    /**
     * <p>API for standalone usage where the SCXML document is a URI.
     * A PathResolver must be provided.</p>
     *
     * @param pathResolver
     *            The PathResolver for this context
     * @param documentRealPath
     *            The String pointing to the absolute (real) path of the
     *            SCXML document
     * @param errHandler
     *            The SAX ErrorHandler
     * @param evalCtx
     *            the document-level variable context for guard condition
     *            evaluation
     * @param evalEngine
     *            the scripting/expression language engine for creating local
     *            state-level variable contexts (if supported by a given
     *            scripting engine)
     *
     * @return SCXML The SCXML object corresponding to the file argument
     *
     * @see Context
     * @see ErrorHandler
     * @see Evaluator
     * @see PathResolver
     */
    public static SCXML digest(final String documentRealPath,
            final ErrorHandler errHandler, final Context evalCtx,
            final Evaluator evalEngine, final PathResolver pathResolver) {

        SCXML scxml = null;
        Digester scxmlDigester = SCXMLDigester.newInstance(null, pathResolver);
        scxmlDigester.setErrorHandler(errHandler);

        try {
            scxml = (SCXML) scxmlDigester.parse(documentRealPath);
        } catch (Exception e) {
            MessageFormat msgFormat = new MessageFormat(ERR_DOC_PARSE_FAIL);
            String errMsg = msgFormat.format(new Object[] {
                documentRealPath, e.getMessage()
            });
            log.error(errMsg, e);
        }

        if (scxml != null) {
            updateSCXML(scxml, evalCtx, evalEngine);
        }

        return scxml;

    }

    /**
     * <p>API for standalone usage where the SCXML document is an
     * InputSource. This method may be used when the SCXML document is
     * packaged in a Java archive, or part of a compound document
     * where the SCXML root is available as a
     * <code>org.w3c.dom.Element</code> or via a <code>java.io.Reader</code>.
     * </p>
     *
     * <p><em>Note:</em> Since there is no path resolution, the SCXML document
     * must not have external state sources.</p>
     *
     * @param documentInputSource
     *            The InputSource for the SCXML document
     * @param errHandler
     *            The SAX ErrorHandler
     * @param evalCtx
     *            the document-level variable context for guard condition
     *            evaluation
     * @param evalEngine
     *            the scripting/expression language engine for creating local
     *            state-level variable contexts (if supported by a given
     *            scripting engine)
     *
     * @return SCXML The SCXML object corresponding to the file argument
     *
     * @see Context
     * @see ErrorHandler
     * @see Evaluator
     */
    public static SCXML digest(final InputSource documentInputSource,
            final ErrorHandler errHandler, final Context evalCtx,
            final Evaluator evalEngine) {

        Digester scxmlDigester = SCXMLDigester.newInstance(null, null);
        scxmlDigester.setErrorHandler(errHandler);

        SCXML scxml = null;
        try {
            scxml = (SCXML) scxmlDigester.parse(documentInputSource);
        } catch (Exception e) {
            log.error("Could not parse SCXML", e);
        }

        if (scxml != null) {
            updateSCXML(scxml, evalCtx, evalEngine);
        }

        return scxml;

    }

    /**
     * Serialize this SCXML object (primarily for debugging).
     *
     * @param scxml
     *            The SCXML to be serialized
     * @return String The serialized SCXML
     */
    public static String serializeSCXML(final SCXML scxml) {
        StringBuffer b = new StringBuffer("<scxml xmlns=\"").append(
                scxml.getXmlns()).append("\" version=\"").append(
                scxml.getVersion()).append("\" initialstate=\"").append(
                scxml.getInitialstate()).append("\">\n");
        Map s = scxml.getStates();
        Iterator i = s.keySet().iterator();
        while (i.hasNext()) {
            serializeState(b, (State) s.get(i.next()), INDENT);
        }
        b.append("</scxml>\n");
        return b.toString();
    }

    //---------------------- PRIVATE CONSTANTS ----------------------//
    //// Patterns to get the digestion going
    /** Root &lt;scxml&gt; element. */
    private static final String XP_SM = "scxml";

    /** &lt;state&gt; children of root &lt;scxml&gt; element. */
    private static final String XP_SM_ST = "scxml/state";

    //// Universal matches
    // State
    /** &lt;state&gt; children of &lt;state&gt; elements. */
    private static final String XP_ST_ST = "!*/state/state";

    /** &lt;state&gt; children of &lt;parallel&gt; elements. */
    private static final String XP_PAR_ST = "!*/parallel/state";

    /** &lt;state&gt; children of transition &lt;target&gt; elements. */
    private static final String XP_TR_TAR_ST = "!*/transition/target/state";

    //private static final String XP_ST_TAR_ST = "!*/state/target/state";

    // Parallel
    /** &lt;parallel&gt; child of &lt;state&gt; elements. */
    private static final String XP_ST_PAR = "!*/state/parallel";

    // If
    /** &lt;if&gt; element. */
    private static final String XP_IF = "!*/if";

    //// Path Fragments
    // Onentries and Onexits
    /** &lt;onentry&gt; child element. */
    private static final String XP_ONEN = "/onentry";

    /** &lt;onexit&gt; child element. */
    private static final String XP_ONEX = "/onexit";

    // Initial
    /** &lt;initial&gt; child element. */
    private static final String XP_INI = "/initial";

    // History
    /** &lt;history&gt; child element. */
    private static final String XP_HIST = "/history";

    // Transition, target and exit
    /** &lt;transition&gt; child element. */
    private static final String XP_TR = "/transition";

    /** &lt;target&gt; child element. */
    private static final String XP_TAR = "/target";

    /** &lt;state&gt; child element. */
    private static final String XP_ST = "/state";

    /** &lt;exit&gt; child element. */
    private static final String XP_EXT = "/exit";

    // Actions
    /** &lt;var&gt; child element. */
    private static final String XP_VAR = "/var";

    /** &lt;assign&gt; child element. */
    private static final String XP_ASN = "/assign";

    /** &lt;log&gt; child element. */
    private static final String XP_LOG = "/log";

    /** &lt;send&gt; child element. */
    private static final String XP_SND = "/send";

    /** &lt;cancel&gt; child element. */
    private static final String XP_CAN = "/cancel";

    /** &lt;elseif&gt; child element. */
    private static final String XP_EIF = "/elseif";

    /** &lt;else&gt; child element. */
    private static final String XP_ELS = "/else";

    //// Other constants
    /** The indent to be used while serializing an SCXML object. */
    private static final String INDENT = " ";

    /**
     * Logger for SCXMLDigester.
     */
    private static org.apache.commons.logging.Log log = LogFactory
            .getLog(SCXMLDigester.class);

    // Error messages
    /**
     * Parsing SCXML document has failed.
     * This message may be rendered hence wrapped in a comment.
     */
    private static final String ERR_DOC_PARSE_FAIL = "<!-- Error parsing "
        + "SCXML document: \"{0}\", with message: \"{1}\" -->\n";

    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_SCXML_NO_INIT = "No SCXML child state "
        + "with ID \"{0}\" found; illegal initialstate for SCXML document";

    /**
     * Error message when a state element specifies an initial state which
     * cannot be found.
     */
    private static final String ERR_STATE_NO_INIT = "No initial element "
        + "available for \"{0}\"";

    /**
     * Error message when a state element specifies an initial state which
     * is not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT = "Initial state "
        + "null or not a descendant of \"{0}\"";

    /**
     * Error message when a referenced history state cannot be found.
     */
    private static final String ERR_STATE_NO_HIST = "Referenced history state "
        + "null for \"{0}\"";

    /**
     * Error message when a shallow history state is not a child state.
     */
    private static final String ERR_STATE_BAD_SHALLOW_HIST = "History state"
        + " for shallow history is not child for \"{0}\"";

    /**
     * Error message when a deep history state is not a descendent state.
     */
    private static final String ERR_STATE_BAD_DEEP_HIST = "History state"
        + " for deep history is not descendant for \"{0}\"";

    //---------------------- PRIVATE UTILITY METHODS ----------------------//
    /**
     * Get a SCXML digester instance.
     *
     * @param scxml The parent SCXML document if there is one (in case of
     *              state templates for examples), null otherwise
     * @param pr The PathResolver
     * @return Digester A newly configured SCXML digester instance
     */
    private static Digester newInstance(final SCXML scxml,
            final PathResolver pr) {

        Digester digester = new Digester();
        digester.setNamespaceAware(true);
        //Uncomment next line after SCXML DTD is available
        //digester.setValidating(true);
        digester.setRules(initRules(scxml, pr));
        return digester;
    }

    /*
     * Private utility functions for configuring digester rule base for SCXML.
     */
    /**
     * Initialize the Digester rules for the current document.
     *
     * @param scxml The parent SCXML document (or null)
     * @param pr The PathResolver
     * @return ExtendedBaseRules The Digester rules configured for the
     *                           current document
     */
    private static ExtendedBaseRules initRules(final SCXML scxml,
            final PathResolver pr) {

        ExtendedBaseRules scxmlRules = new ExtendedBaseRules();

        //// SCXML
        scxmlRules.add(XP_SM, new ObjectCreateRule(SCXML.class));
        scxmlRules.add(XP_SM, new SetPropertiesRule());

        //// States
        // Level one states
        addStateRules(XP_SM_ST, scxmlRules, scxml, pr, 0);
        scxmlRules.add(XP_SM_ST, new SetNextRule("addState"));
        // Nested states
        addStateRules(XP_ST_ST, scxmlRules, scxml, pr, 1);
        scxmlRules.add(XP_ST_ST, new SetNextRule("addChild"));

        // Parallel states
        addStateRules(XP_PAR_ST, scxmlRules, scxml, pr, 1);
        scxmlRules.add(XP_PAR_ST, new SetNextRule("addState"));
        // Target states
        addStateRules(XP_TR_TAR_ST, scxmlRules, scxml, pr, 2);
        scxmlRules.add(XP_TR_TAR_ST, new SetNextRule("setTarget"));

        //// Parallels
        addParallelRules(XP_ST_PAR, scxmlRules, scxml);

        //// Ifs
        addIfRules(XP_IF, scxmlRules);

        return scxmlRules;

    }

    /**
     * Add Digester rules for all &lt;state&gt; elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param scxml The parent SCXML document (or null)
     * @param pr The PathResolver
     * @param parent The distance between this state and its parent
     *               state on the Digester stack
     */
    private static void addStateRules(final String xp,
            final ExtendedBaseRules scxmlRules, final SCXML scxml,
            final PathResolver pr, final int parent) {
        scxmlRules.add(xp, new ObjectCreateRule(State.class));
        addStatePropertiesRules(xp, scxmlRules, pr);
        addInitialRule(xp + XP_INI, scxmlRules, pr, scxml);
        addHistoryRules(xp + XP_HIST, scxmlRules, pr, scxml);
        addParentRule(xp, scxmlRules, parent);
        addTransitionRules(xp + XP_TR, scxmlRules, "addTransition");
        addHandlerRules(xp, scxmlRules);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
    }

    /**
     * Add Digester rules for all &lt;parallel&gt; elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param scxml The parent SCXML document (or null)
     */
    private static void addParallelRules(final String xp,
            final ExtendedBaseRules scxmlRules, final SCXML scxml) {
        addSimpleRulesTuple(xp, scxmlRules, Parallel.class, null, null,
                "setParallel");
        addHandlerRules(xp, scxmlRules);
        addParentRule(xp, scxmlRules, 1);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
    }

    /**
     * Add Digester rules for all &lt;state&gt; element attributes.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param pr The PathResolver
     */
    private static void addStatePropertiesRules(final String xp,
            final ExtendedBaseRules scxmlRules, final PathResolver pr) {
        scxmlRules.add(xp, new SetPropertiesRule(new String[] {"id", "final"},
            new String[] {"id", "isFinal"}));
        scxmlRules.add(xp, new DigestSrcAttributeRule(pr));
    }

    /**
     * Add Digester rules for all &lt;initial&gt; elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param pr The PathResolver
     * @param scxml The parent SCXML document (or null)
     */
    private static void addInitialRule(final String xp,
            final ExtendedBaseRules scxmlRules, final PathResolver pr,
            final SCXML scxml) {
        scxmlRules.add(xp, new ObjectCreateRule(Initial.class));
        addPseudoStatePropertiesRules(xp, scxmlRules, pr);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
        addTransitionRules(xp + XP_TR, scxmlRules, "setTransition");
        scxmlRules.add(xp, new SetNextRule("setInitial"));
    }

    /**
     * Add Digester rules for all &lt;history&gt; elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param pr The PathResolver
     * @param scxml The parent SCXML document (or null)
     */
    private static void addHistoryRules(final String xp,
            final ExtendedBaseRules scxmlRules, final PathResolver pr,
            final SCXML scxml) {
        scxmlRules.add(xp, new ObjectCreateRule(History.class));
        addPseudoStatePropertiesRules(xp, scxmlRules, pr);
        scxmlRules.add(xp, new UpdateModelRule(scxml));
        scxmlRules.add(xp, new SetPropertiesRule(new String[] {"type"},
            new String[] {"type"}));
        addTransitionRules(xp + XP_TR, scxmlRules, "setTransition");
        scxmlRules.add(xp, new SetNextRule("addHistory"));
    }

    /**
     * Add Digester rules for all pseudo state (initial, history) element
     * attributes.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param pr The PathResolver
     */
    private static void addPseudoStatePropertiesRules(final String xp,
            final ExtendedBaseRules scxmlRules, final PathResolver pr) {
        scxmlRules.add(xp, new SetPropertiesRule(new String[] {"id"},
            new String[] {"id"}));
        scxmlRules.add(xp, new DigestSrcAttributeRule(pr));
        addParentRule(xp, scxmlRules, 1);
    }

    /**
     * Add Digester rule for all setting parent state.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param parent The distance between this state and its parent
     *               state on the Digester stack
     */
    private static void addParentRule(final String xp,
            final ExtendedBaseRules scxmlRules, final int parent) {
        if (parent < 1) {
            return;
        }
        scxmlRules.add(xp, new Rule() {
            // A generic version of setTopRule
            public void body(final String namespace, final String name,
                    final String text) throws Exception {
                TransitionTarget t = (TransitionTarget) getDigester().peek();
                TransitionTarget p = (TransitionTarget) getDigester().peek(
                        parent);
                // CHANGE - Moved parent property to TransitionTarget
                t.setParent(p);
            }
        });
    }

    /**
     * Add Digester rules for all &lt;transition&gt; elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param setNextMethod The method name for adding this transition
     *             to its parent (defined by the SCXML Java object model).
     */
    private static void addTransitionRules(final String xp,
            final ExtendedBaseRules scxmlRules, final String setNextMethod) {
        scxmlRules.add(xp, new ObjectCreateRule(Transition.class));
        scxmlRules.add(xp, new SetPropertiesRule());
        scxmlRules.add(xp + XP_TAR, new SetPropertiesRule());
        addActionRules(xp, scxmlRules);
        scxmlRules.add(xp + XP_EXT, new Rule() {
            public void end(final String namespace, final String name) {
                Transition t = (Transition) getDigester().peek(1);
                State exitState = new State();
                exitState.setIsFinal(true);
                t.setTarget(exitState);
            }
        });
        scxmlRules.add(xp, new SetNextRule(setNextMethod));
    }

    /**
     * Add Digester rules for all &lt;onentry&gt; and &lt;onexit&gt;
     * elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     */
    private static void addHandlerRules(final String xp,
            final ExtendedBaseRules scxmlRules) {
        scxmlRules.add(xp + XP_ONEN, new ObjectCreateRule(OnEntry.class));
        addActionRules(xp + XP_ONEN, scxmlRules);
        scxmlRules.add(xp + XP_ONEN, new SetNextRule("setOnEntry"));
        scxmlRules.add(xp + XP_ONEX, new ObjectCreateRule(OnExit.class));
        addActionRules(xp + XP_ONEX, scxmlRules);
        scxmlRules.add(xp + XP_ONEX, new SetNextRule("setOnExit"));
    }

    /**
     * Add Digester rules for all actions (&quot;executable&quot; elements).
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     */
    private static void addActionRules(final String xp,
            final ExtendedBaseRules scxmlRules) {
        addActionRulesTuple(xp + XP_ASN, scxmlRules, Assign.class);
        addActionRulesTuple(xp + XP_VAR, scxmlRules, Var.class);
        addActionRulesTuple(xp + XP_LOG, scxmlRules, Log.class);
        addSendRulesTuple(xp + XP_SND, scxmlRules);
        addActionRulesTuple(xp + XP_CAN, scxmlRules, Cancel.class);
        addActionRulesTuple(xp + XP_EXT, scxmlRules, Exit.class);
    }

    /**
     * Add Digester rules that are specific to the &lt;send&gt; action
     * element.
     *
     * @param xp The Digester style XPath expression of &lt;send&gt; element
     * @param scxmlRules The rule set to be used for digestion
     */
    private static void addSendRulesTuple(final String xp,
            final ExtendedBaseRules scxmlRules) {
        addActionRulesTuple(xp, scxmlRules, Send.class);
        try {
            scxmlRules.add(xp, new ParseSendRule());
        } catch (ParserConfigurationException pce) {
            log.error("Error parsing <send> element content",
                pce);
        }
    }

    /**
     * Add Digester rules for all &lt;if&gt; elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     */
    private static void addIfRules(final String xp,
            final ExtendedBaseRules scxmlRules) {
        addActionRulesTuple(xp, scxmlRules, If.class);
        addActionRules(xp, scxmlRules);
        addActionRulesTuple(xp + XP_EIF, scxmlRules, ElseIf.class);
        addActionRulesTuple(xp + XP_ELS, scxmlRules, Else.class);
    }

    /**
     * Add Digester rules that are common across all actions elements.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param klass The class in the Java object model to be instantiated
     *              in the ObjectCreateRule for this action
     */
    private static void addActionRulesTuple(final String xp,
            final ExtendedBaseRules scxmlRules, final Class klass) {
        addSimpleRulesTuple(xp, scxmlRules, klass, null, null, "addAction");
        scxmlRules.add(xp, new SetExecutableParentRule());
    }

    /**
     * Add the run of the mill Digester rules for any element.
     *
     * @param xp The Digester style XPath expression of the parent
     *           XML element
     * @param scxmlRules The rule set to be used for digestion
     * @param klass The class in the Java object model to be instantiated
     *              in the ObjectCreateRule for this action
     * @param args The attributes to be mapped into the object model
     * @param props The properties that args get mapped to
     * @param addMethod The method that the SetNextRule should call
     */
    private static void addSimpleRulesTuple(final String xp,
            final ExtendedBaseRules scxmlRules, final Class klass,
            final String[] args, final String[] props,
            final String addMethod) {
        scxmlRules.add(xp, new ObjectCreateRule(klass));
        if (args == null) {
            scxmlRules.add(xp, new SetPropertiesRule());
        } else {
            scxmlRules.add(xp, new SetPropertiesRule(args, props));
        }
        scxmlRules.add(xp, new SetNextRule(addMethod));
    }

    /*
     * Post-processing methods to make the SCXML object SCXMLExecutor ready.
     */
     /**
      * Update the SCXML object model (part of post-digestion processing).
      *
      * @param scxml The SCXML object (output from Digester)
      * @param evalCtx The root evaluation context (from the host environment
      *                of the SCXML document)
      * @param evalEngine The expression evaluator
      */
    private static void updateSCXML(final SCXML scxml, final Context evalCtx,
            final Evaluator evalEngine) {
        // Watch case, slightly unfortunate naming ;-)
        String initialstate = scxml.getInitialstate();
        //we have to use getTargets() here since the initialState can be
        //an indirect descendant
        // Concern marked by one of the code reviewers: better type check,
        //            now ClassCastException happens for Parallel
        // Response: initial should be a State, for Parallel, it is implicit
        State initialState = (State) scxml.getTargets().get(initialstate);
        if (initialState == null) {
            // Where do we, where do we go?
            logModelError(ERR_SCXML_NO_INIT, new Object[] {initialstate});
        }
        scxml.setInitialState(initialState);
        scxml.setRootContext(evalCtx);
        Map targets = scxml.getTargets();
        Map states = scxml.getStates();
        Iterator i = states.keySet().iterator();
        while (i.hasNext()) {
            updateState((State) states.get(i.next()), targets, evalCtx,
                    evalEngine);
        }
    }

    /**
      * Update this State object (part of post-digestion processing).
      * Also checks for any errors in the document.
      *
      * @param s The State object
      * @param targets The global Map of all transition targets
      * @param evalCtx The evaluation context for this State
      * @param evalEngine The expression evaluator
      */
    private static void updateState(final State s, final Map targets,
            final Context evalCtx, final Evaluator evalEngine) {
        //setup local variable context
        Context localCtx = null;
        if (s.getParent() == null) {
            localCtx = evalEngine.newContext(evalCtx);
        } else {
            State parentState = null;
            if (s.getParent() instanceof Parallel) {
                parentState = (State) (s.getParent().getParent());
            } else {
                parentState = (State) (s.getParent());
            }
            localCtx = evalEngine.newContext(parentState.getContext());
        }
        s.setContext(localCtx);
        //ensure both onEntry and onExit have parent
        //could add next two lines as a Digester rule for OnEntry/OnExit
        s.getOnEntry().setParent(s);
        s.getOnExit().setParent(s);
        //initialize next / inital
        Initial ini = s.getInitial();
        Map c = s.getChildren();
        String badState = "anonymous state";
        if (!SCXMLHelper.isStringEmpty(s.getId())) {
            badState = "state with ID " + s.getId();
        }
        if (!c.isEmpty()) {
            if (ini == null) {
                logModelError(ERR_STATE_NO_INIT, new Object[] {badState});
            }
            Transition initialTransition = ini.getTransition();
            updateTransition(initialTransition, targets);
            TransitionTarget initialState = initialTransition.getTarget();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialState == null
                    || !SCXMLHelper.isDescendant(initialState, s)) {
                logModelError(ERR_STATE_BAD_INIT, new Object[] {badState});
            }
        }
        List histories = s.getHistory();
        Iterator histIter = histories.iterator();
        while (histIter.hasNext()) {
            History h = (History) histIter.next();
            Transition historyTransition = h.getTransition();
            updateTransition(historyTransition, targets);
            State historyState = (State) historyTransition.getTarget();
            if (historyState == null) {
                logModelError(ERR_STATE_NO_HIST, new Object[] {badState});
            }
            if (!h.isDeep()) {
                if (!c.containsValue(historyState)) {
                    logModelError(ERR_STATE_BAD_SHALLOW_HIST, new Object[] {
                        badState });
                }
            } else {
                if (!SCXMLHelper.isDescendant(historyState, s)) {
                    logModelError(ERR_STATE_BAD_DEEP_HIST, new Object[] {
                        badState });
                }
            }
        }
        Map t = s.getTransitions();
        Iterator i = t.keySet().iterator();
        while (i.hasNext()) {
            Iterator j = ((List) t.get(i.next())).iterator();
            while (j.hasNext()) {
                Transition trn = (Transition) j.next();
                //could add next two lines as a Digester rule for Transition
                trn.setNotificationRegistry(s.getNotificationRegistry());
                trn.setParent(s);
                updateTransition(trn, targets);
            }
        }
        Parallel p = s.getParallel();
        if (p != null) {
            updateParallel(p, targets, evalCtx, evalEngine);
        } else {
            Iterator j = c.keySet().iterator();
            while (j.hasNext()) {
                updateState((State) c.get(j.next()), targets, evalCtx,
                        evalEngine);
            }
        }
    }

    /**
      * Update this Parallel object (part of post-digestion processing).
      *
      * @param p The Parallel object
      * @param targets The global Map of all transition targets
      * @param evalCtx The evaluation context for this State
      * @param evalEngine The expression evaluator
      */
    private static void updateParallel(final Parallel p, final Map targets,
            final Context evalCtx, final Evaluator evalEngine) {
        Iterator i = p.getStates().iterator();
        while (i.hasNext()) {
            updateState((State) i.next(), targets, evalCtx, evalEngine);
        }
    }

    /**
      * Update this Transition object (part of post-digestion processing).
      *
      * @param t The Transition object
      * @param targets The global Map of all transition targets
      */
    private static void updateTransition(final Transition t,
            final Map targets) {
        String next = t.getNext();
        TransitionTarget tt = t.getTarget();
        if (tt == null) {
            tt = (TransitionTarget) targets.get(next);
            if (tt == null) {
                // Could move Digester warnings to errors
                log.warn("WARNING: SCXMLDigester - Transition "
                        + "target \"" + next + "\" not found");
            }
            t.setTarget(tt);
        }
    }

    /**
      * Log an error discovered in post-digestion processing.
      *
      * @param errType The type of error
      * @param msgArgs The arguments for formatting the error message
      */
    private static void logModelError(final String errType,
            final Object[] msgArgs) {
        MessageFormat msgFormat = new MessageFormat(errType);
        String errMsg = msgFormat.format(msgArgs);
        log.error(errMsg);
    }

    /*
     * Private SCXML object serialization utility functions.
     */
    /**
     * Serialize this State object.
     *
     * @param b The buffer to append the serialization to
     * @param s The State to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeState(final StringBuffer b,
            final State s, final String indent) {
        b.append(indent).append("<state");
        serializeTransitionTargetAttributes(b, s);
        boolean f = s.getIsFinal();
        if (f) {
            b.append(" final=\"true\"");
        }
        b.append(">\n");
        Initial ini = s.getInitial();
        if (ini != null) {
            serializeInitial(b, ini, indent + INDENT);
        }
        List h = s.getHistory();
        if (h != null) {
            serializeHistory(b, h, indent + INDENT);
        }
        serializeOnEntry(b, s, indent + INDENT);
        Map t = s.getTransitions();
        Iterator i = t.keySet().iterator();
        while (i.hasNext()) {
            List et = (List) t.get(i.next());
            for (int len = 0; len < et.size(); len++) {
                serializeTransition(b, (Transition) et.get(len), indent
                        + INDENT);
            }
        }
        Parallel p = s.getParallel();
        if (p != null) {
            serializeParallel(b, p, indent + INDENT);
        } else {
            Map c = s.getChildren();
            Iterator j = c.keySet().iterator();
            while (j.hasNext()) {
                State cs = (State) c.get(j.next());
                serializeState(b, cs, indent + INDENT);
            }
        }
        serializeOnExit(b, s, indent + INDENT);
        b.append(indent).append("</state>\n");
    }

    /**
     * Serialize this Parallel object.
     *
     * @param b The buffer to append the serialization to
     * @param p The Parallel to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeParallel(final StringBuffer b,
            final Parallel p, final String indent) {
        b.append(indent).append("<parallel");
        serializeTransitionTargetAttributes(b, p);
        b.append(">\n");
        serializeOnEntry(b, p, indent + INDENT);
        Set s = p.getStates();
        Iterator i = s.iterator();
        while (i.hasNext()) {
            serializeState(b, (State) i.next(), indent + INDENT);
        }
        serializeOnExit(b, p, indent + INDENT);
        b.append(indent).append("</parallel>\n");
    }

    /**
     * Serialize this Initial object.
     *
     * @param b The buffer to append the serialization to
     * @param i The Initial to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeInitial(final StringBuffer b, final Initial i,
            final String indent) {
        b.append(indent).append("<initial");
        serializeTransitionTargetAttributes(b, i);
        b.append(">\n");
        serializeTransition(b, i.getTransition(), indent + INDENT);
        b.append(indent).append("</initial>\n");
    }

    /**
     * Serialize the History.
     *
     * @param b The buffer to append the serialization to
     * @param l The List of History objects to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeHistory(final StringBuffer b, final List l,
            final String indent) {
        if (l.size() > 0) {
            for (int i = 0; i < l.size(); i++) {
                History h = (History) l.get(i);
                b.append(indent).append("<history");
                serializeTransitionTargetAttributes(b, h);
                 if (h.isDeep()) {
                     b.append(" type=\"deep\"");
                 } else {
                     b.append(" type=\"shallow\"");
                 }
                b.append(">\n");
                serializeTransition(b, h.getTransition(), indent + INDENT);
                b.append(indent).append("</history>\n");
            }
        }
    }

    /**
     * Serialize properties of TransitionTarget which are element attributes.
     *
     * @param b The buffer to append the serialization to
     * @param t The TransitionTarget
     */
    private static void serializeTransitionTargetAttributes(
            final StringBuffer b, final TransitionTarget t) {
        String id = t.getId();
        if (id != null) {
            b.append(" id=\"" + id + "\"");
        }
        TransitionTarget pt = t.getParent();
        if (pt != null) {
            String pid = pt.getId();
            if (pid != null) {
                b.append(" parentid=\"").append(pid).append("\"");
            }
        }
    }

    /**
     * Serialize this Transition object.
     *
     * @param b The buffer to append the serialization to
     * @param t The Transition to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeTransition(final StringBuffer b,
            final Transition t, final String indent) {
        b.append(indent).append("<transition event=\"").append(t.getEvent())
                .append("\" cond=\"").append(t.getCond()).append("\">\n");
        boolean exit = serializeActions(b, t.getActions(), indent + INDENT);
        if (!exit) {
            serializeTarget(b, t, indent + INDENT);
        }
        b.append(indent).append("</transition>\n");
    }

    /**
     * Serialize this Transition's Target.
     *
     *
     * @param b The buffer to append the serialization to
     * @param t The Transition whose Target needs to be serialized
     * @param indent The indent for this XML element
     */
    private static void serializeTarget(final StringBuffer b,
            final Transition t, final String indent) {
        b.append(indent).append("<target");
        String n = t.getNext();
        if (n != null) {
            b.append(" next=\"" + n + "\">\n");
        } else {
            b.append(">\n");
            if (t.getTarget() != null) {
                // The inline transition target can only be a state
                serializeState(b, (State) t.getTarget(), indent + INDENT);
            }
        }
        b.append(indent).append("</target>\n");
    }

    /**
     * Serialize this OnEntry object.
     *
     * @param b The buffer to append the serialization to
     * @param t The TransitionTarget whose OnEntry is to be serialized
     * @param indent The indent for this XML element
     */
    private static void serializeOnEntry(final StringBuffer b,
            final TransitionTarget t, final String indent) {
        OnEntry e = t.getOnEntry();
        if (e != null && e.getActions().size() > 0) {
            b.append(indent).append("<onentry>\n");
            serializeActions(b, e.getActions(), indent + INDENT);
            b.append(indent).append("</onentry>\n");
        }
    }

    /**
     * Serialize this OnExit object.
     *
     * @param b The buffer to append the serialization to
     * @param t The TransitionTarget whose OnExit is to be serialized
     * @param indent The indent for this XML element
     */
    private static void serializeOnExit(final StringBuffer b,
            final TransitionTarget t, final String indent) {
        OnExit x = t.getOnExit();
        if (x != null && x.getActions().size() > 0) {
            b.append(indent).append("<onexit>\n");
            serializeActions(b, x.getActions(), indent + INDENT);
            b.append(indent).append("</onexit>\n");
        }
    }

    /**
     * Serialize this List of actions.
     *
     * @param b The buffer to append the serialization to
     * @param l The List of actions to serialize
     * @param indent The indent for this XML element
     * @return boolean true if the list of actions contains an &lt;exit/&gt;
     */
    private static boolean serializeActions(final StringBuffer b, final List l,
            final String indent) {
        if (l == null) {
            return false;
        }
        boolean exit = false;
        Iterator i = l.iterator();
        while (i.hasNext()) {
            Action a = (Action) i.next();
            if (a instanceof Var) {
                Var v = (Var) a;
                b.append(indent).append("<var name=\"").append(v.getName())
                        .append("\" expr=\"").append(v.getExpr()).append(
                                "\"/>\n");
            } else if (a instanceof Assign) {
                Assign asn = (Assign) a;
                b.append(indent).append("<assign name=\"")
                        .append(asn.getName()).append("\" expr=\"")
                        .append(asn.getExpr()).append("\"/>\n");
            } else if (a instanceof Send) {
                serializeSend(b, (Send) a, indent);
            } else if (a instanceof Cancel) {
                Cancel c = (Cancel) a;
                b.append(indent).append("<cancel sendid=\"")
                    .append(c.getSendid()).append("\"/>\n");
            } else if (a instanceof Log) {
                Log lg = (Log) a;
                b.append(indent).append("<log expr=\"").append(lg.getExpr())
                        .append("\"/>\n");
            } else if (a instanceof Exit) {
                Exit e = (Exit) a;
                b.append(indent).append("<exit");
                String expr = e.getExpr();
                String nl = e.getNamelist();
                if (expr != null) {
                    b.append(" expr=\"" + expr + "\"");
                }
                if (nl != null) {
                    b.append(" namelist=\"" + nl + "\"");
                }
                b.append("/>\n");
                exit = true;
            } else if (a instanceof If) {
                If iff = (If) a;
                serializeIf(b, iff, indent);
            } else if (a instanceof Else) {
                b.append(indent).append("<else/>\n");
            } else if (a instanceof ElseIf) {
                ElseIf eif = (ElseIf) a;
                b.append(indent).append("<elseif cond=\"")
                        .append(eif.getCond()).append("\" />\n");
            }
        }
        return exit;
    }

    /**
     * Serialize this Send object.
     *
     * @param b The buffer to append the serialization to
     * @param send The Send object to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeSend(final StringBuffer b,
            final Send send, final String indent) {
        b.append(indent).append("<send sendid=\"")
            .append(send.getSendid()).append("\" target=\"")
            .append(send.getTarget()).append("\" targetType=\"")
            .append(send.getTargettype()).append("\" namelist=\"")
            .append(send.getNamelist()).append("\" delay=\"")
            .append(send.getDelay()).append("\" events=\"")
            .append(send.getEvent()).append("\" hints=\"")
            .append(send.getHints()).append("\">\n");
        /* TODO - Serialize body content
        try {
            b.append(send.getBodyContent());
        } catch (IOException ioe) {
            log.error("Failed to serialize external nodes for <send>", ioe);
        }
        */
        b.append(indent).append("</send>\n");
    }

    /**
     * Serialize this If object.
     *
     * @param b The buffer to append the serialization to
     * @param iff The If object to serialize
     * @param indent The indent for this XML element
     */
    private static void serializeIf(final StringBuffer b,
            final If iff, final String indent) {
        b.append(indent).append("<if cond=\"").append(iff.getCond()).append(
                "\">\n");
        serializeActions(b, iff.getActions(), indent + INDENT);
        b.append(indent).append("</if>\n");
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private SCXMLDigester() {
        super();
    }

    /**
     * Custom digestion rule for establishing necessary associations of this
     * TransitionTarget with the root SCXML object.
     * These include: <br>
     * 1) Updation of the SCXML object's global targets Map <br>
     * 2) Obtaining a handle to the SCXML object's NotificationRegistry <br>
     *
     */
    public static class UpdateModelRule extends Rule {

        /**
         * The root SCXML object.
         */
        private SCXML scxml;

        /**
         * Constructor.
         * @param scxml The root SCXML object
         */
        public UpdateModelRule(final SCXML scxml) {
            super();
            this.scxml = scxml;
        }

        /**
         * @see Rule#end(String, String)
         */
        public final void end(final String namespace, final String name) {
            if (scxml == null) {
                scxml = (SCXML) getDigester()
                        .peek(getDigester().getCount() - 1);
            }
            NotificationRegistry notifReg = scxml.getNotificationRegistry();
            TransitionTarget tt = (TransitionTarget) getDigester().peek();
            scxml.addTarget(tt);
            tt.setNotificationRegistry(notifReg);
        }
    }

    /**
     * Custom digestion rule for setting Executable parent of Action elements.
     *
     */
    public static class SetExecutableParentRule extends Rule {

        /**
         * Constructor.
         */
        public SetExecutableParentRule() {
            super();
        }

        /**
         * @see Rule#end(String, String)
         */
        public final void end(final String namespace, final String name) {
            Action child = (Action) getDigester().peek();
            for (int i = 1; i < getDigester().getCount() - 1; i++) {
                Object ancestor = getDigester().peek(i);
                if (ancestor instanceof Executable) {
                    child.setParent((Executable) ancestor);
                    return;
                }
            }
        }
    }

    /**
     * Custom digestion rule for setting Executable parent of Action elements.
     *
     */
    public static class ParseSendRule extends NodeCreateRule {
        /**
         * Constructor.
         * @throws ParserConfigurationException A JAXP configuration error
         */
        public ParseSendRule() throws ParserConfigurationException {
            super();
        }
        /**
         * @see Rule#end(String, String)
         */
        public final void end(final String namespace, final String name) {
            Element sendElement = (Element) getDigester().pop();
            NodeList childNodes = sendElement.getChildNodes();
            Send send = (Send) getDigester().peek();
            for (int i = 0; i < childNodes.getLength(); i++) {
                send.getExternalNodes().add(childNodes.item(i));
            }
        }
    }

    /**
     * Custom digestion rule for external sources, that is, the src attribute of
     * the &lt;state&gt; element.
     *
     */
    public static class DigestSrcAttributeRule extends Rule {

        /**
         * The PathResolver used to resolve the src attribute to the
         * SCXML document it points to.
         * @see PathResolver
         */
        private PathResolver pr;

        /**
         * Constructor.
         * @param pr The PathResolver
         * @see PathResolver
         */
        public DigestSrcAttributeRule(final PathResolver pr) {
            super();
            this.pr = pr;
        }

        /**
         * @see Rule#begin(String, String, Attributes)
         */
        public final void begin(final String namespace, final String name,
                final Attributes attributes) {
            String src = attributes.getValue("src");
            if (SCXMLHelper.isStringEmpty(src)) {
                return;
            }
            Digester digester = getDigester();
            SCXML scxml = (SCXML) digester.peek(digester.getCount() - 1);
            // 1) Digest the external SCXML file
            Digester externalSrcDigester = newInstance(scxml,
                pr.getResolver(src));
            SCXML externalSCXML = null;
            String path = null;
            if (pr == null) {
                path = src;
            } else {
                path = pr.resolvePath(src);
            }

            try {
                externalSCXML = (SCXML) externalSrcDigester.parse(path);
            } catch (Exception e) {
                log.error(null, e);
            }
            // 2) Adopt the children
            // TODO - Clarify spec; Priority: High
            if (externalSCXML == null) {
                return;
            }
            State s = (State) digester.peek();
            Transition t = new Transition();
            t.setNext(externalSCXML.getInitialstate());
            Initial ini = new Initial();
            ini.setTransition(t);
            s.setInitial(ini);
            Map children = externalSCXML.getStates();
            Object[] ids = children.keySet().toArray();
            for (int i = 0; i < ids.length; i++) {
                s.addChild((State) children.get(ids[i]));
            }
        }
    }
}

