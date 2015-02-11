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
package org.apache.commons.scxml2.w3c;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.invoke.SimpleSCXMLInvoker;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.SCXML;

/**
 * W3C SCXML 1.0 IRP tests: <a href="http://www.w3.org/Voice/2013/scxml-irp/">http://www.w3.org/Voice/2013/scxml-irp/</a>.
 * <p>
 * The <b>W3CTests</b> class is standalone and can download and transform the IRP tests locally using respectively
 * commandline parameter <b>get</b> or <b>make</b>.
 * </p>
 * <p>
 * To execute one or multiple IRP tests the commandline parameter <b>run</b> must be specified.
 * </p>
 * <p>
 * Optional environment parameter <b>-Ddatamodel=&lt;minimal|ecma|xpath&gt;</b> can be specified to limit the
 * execution of the tests for and using only the specified datamodel language.
 * </p>
 * <p>
 * Optional environment parameter <b>-Dtest=&lt;testId&gt;</b> can be specified to only execute a single test, which
 * also can be combined with the <b>-Ddatamodel</b> parameter.
 * </p>
 * <p>
 * The W3CTests also uses a separate <b><code>tests.xml</code></b> configuration file, located in the
 * <b><code>src/test/resources/w3c</code></b> directory, which is manually maintained to enable|disable execution
 * of tests (when <em>not</em> using the <b>-Dtest</b> parameter, which will always execute the specified test).<br/>
 * Furthermore, in this configuration file the current <em>success</em> or <em>failure</em> status, and even more
 * meta data per test is maintained.
 * </p>
 */
@SuppressWarnings("unused")
public class W3CTests {

    private static final String SCXML_IRP_BASE_URL = "http://www.w3.org/Voice/2013/scxml-irp/";
    private static final String SCXML_IRP_MANIFEST_URI = "manifest.xml";
    private static final String SCXML_IRP_ECMA_XSL_URI = "confEcma.xsl";
    private static final String SCXML_IRP_XPATH_XSL_URI = "confXpath.xsl";

    private static final String TESTS_SRC_DIR = "src/w3c/scxml-irp/";
    private static final String TXML_TESTS_DIR = TESTS_SRC_DIR + "txml/";
    private static final String MINIMAL_TESTS_DIR = TESTS_SRC_DIR + "minimal/";
    private static final String ECMA_TESTS_DIR = TESTS_SRC_DIR + "ecma/";
    private static final String XPATH_TESTS_DIR = TESTS_SRC_DIR + "xpath/";
    private static final String PACKAGE_PATH = "/"+W3CTests.class.getPackage().getName().replace('.','/');
    private static final String TESTS_FILENAME = PACKAGE_PATH + "/tests.xml";
    private static final String SCXML_IRP_MINIMAL_XSL_FILENAME = PACKAGE_PATH + "/confMinimal.xsl";

    /**
     * Tests model class used for loading the <b>tests.xml</b> configuration file
     */
    @XmlRootElement(name="tests")
    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class Tests {

        @XmlAccessorType(XmlAccessType.FIELD)
        protected static class Test {

            @XmlAttribute(required=true)
            private String id;
            @XmlAttribute(required=true)
            private Boolean mandatory;
            @XmlAttribute(required=true)
            private Boolean manual;
            @XmlAttribute(required=true)
            private boolean enabled;
            @XmlAttribute
            private String finalId;
            @XmlAttribute
            private Boolean implemented;
            @XmlAttribute(name="minimal")
            String minimalStatus;
            @XmlAttribute(name="ecma")
            String ecmaStatus;
            @XmlAttribute(name="xpath")
            String xpathStatus;
            @XmlAttribute
            Boolean xpathEnabled;
            @XmlValue
            private String comment;

            public String getId() {
                return id;
            }

            public boolean isMandatory() {
                return mandatory;
            }

            public boolean isManual() {
                return manual == null || manual;
            }

            public boolean isEnabled() {
                return enabled;
            }

            public String getFinalState() {
                return finalId;
            }

            public boolean isImplemented() {
                return implemented == null || implemented;
            }

            public String getMinimalStatus() {
                return minimalStatus;
            }

            public String getEcmaStatus() {
                return ecmaStatus;
            }

            public String getXpathStatus() {
                return xpathStatus;
            }

            public boolean isXPathEnabled() {
                return xpathEnabled == null || xpathEnabled;
            }

            public String getComment() {
                return comment;
            }

            public String toString() {
                return id;
            }
        }

        @XmlElement(name="test")
        private ArrayList<Test> tests;

        private LinkedHashMap<String, Test> testsMap;

        public LinkedHashMap<String, Test> getTests() {
            if (testsMap == null) {
                testsMap = new LinkedHashMap<String, Test>();
                if (tests != null) {
                    for (Test t : tests) {
                        testsMap.put(t.getId(), t);
                    }
                }
            }
            return testsMap;
        }
    }

    /**
     * Datamodel enum representing the minimal, ecma and xpath datamodel types used and tested by the W3C IRP tests.
     */
    protected enum Datamodel {

        MINIMAL("minimal"),
        ECMA("ecma"),
        XPATH("xpath");

        private final String value;

        private Datamodel(final String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        public static Datamodel fromValue(final String value) {
            for (Datamodel datamodel : Datamodel.values()) {
                if (datamodel.value().equals(value)) {
                    return datamodel;
                }
            }
            return null;
        }
    }

    /**
     * Assertions model class used for loading the W3C IRP tests manifest.xml file, defining the meta data and
     * source URIs for all the W3C IRP tests.
     */
    @XmlRootElement(name="assertions")
    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class Assertions {

        @XmlAccessorType(XmlAccessType.FIELD)
        protected static class Assertion {

            @XmlAttribute
            private String id;
            @XmlAttribute(name="specnum")
            private String specnum;
            @XmlAttribute(name="specid")
            private String specid;
            @XmlElement(name="test")
            private ArrayList<TestCase> testCases;

            public String getId() {
                return id;
            }

            public String getSpecNum() {
                return specnum;
            }

            public String getSpecId() {
                return specid;
            }

            public List<TestCase> getTestCases() {
                return testCases != null ? testCases : Collections.<TestCase>emptyList();
            }

            public Datamodel getDatamodel() {
                if ("#minimal-profile".equals(specid)) {
                    return Datamodel.MINIMAL;
                }
                else if ("#ecma-profile".equals(specid)) {
                    return Datamodel.ECMA;
                }
                else if ("#xpath-profile".equals(specid)) {
                    return Datamodel.XPATH;
                }
                return null;
            }

            public String toString() {
                return id;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        protected static class TestCase {

            @XmlAttribute
            private String id;
            @XmlAttribute
            private String manual;
            @XmlAttribute
            private String conformance;
            @XmlElement(name="start")
            private ArrayList<Resource> scxmlResources;
            @XmlElement(name="dep")
            private ArrayList<Resource> depResources;

            private ArrayList<Resource> resources;

            public String getId() {
                return id;
            }

            public boolean isManual() {
                return Boolean.parseBoolean(manual);
            }

            public boolean isOptional() {
                return "mandatory".equals(conformance);
            }

            public List<Resource> getScxmlResources() {
                return scxmlResources != null ? scxmlResources : Collections.<Resource>emptyList();
            }

            public List<Resource> getResources() {
                if (resources == null) {
                    resources = new ArrayList<Resource>();
                    if (scxmlResources != null) {
                        resources.addAll(scxmlResources);
                    }
                    if (depResources != null) {
                        resources.addAll(depResources);
                        // no longer needed
                        depResources = null;
                    }
                }
                return resources;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        protected static class Resource {

            @XmlAttribute
            private String uri;

            public String getUri() {
                return uri;
            }

            public String getName() {
                return uri.substring(uri.indexOf("/")+1, uri.indexOf("."));
            }

            public String getFilename() {
                return uri.substring(uri.indexOf("/")+1);
            }
        }

        @XmlElement(name="assert")
        private ArrayList<Assertion> assertions;

        private LinkedHashMap<String, Assertion> assertionsMap;

        public LinkedHashMap<String, Assertion> getAssertions() {
            if (assertionsMap == null) {
                assertionsMap = new LinkedHashMap<String, Assertion>();
                if (assertions != null) {
                    for (Assertion a : assertions) {
                        assertionsMap.put(a.getId(), a);
                    }
                }
            }
            return assertionsMap;
        }
    }

    /**
     * Simple TestResult data struct for tracking test results
     */
    protected static class TestResults {
        int testsSkipped;
        int testsPassed;
        int testsFailed;
        int minimalPassed;
        int minimalFailed;
        int ecmaPassed;
        int ecmaFailed;
        int xpathPassed;
        int xpathFailed;
        ArrayList<String> failedTests = new ArrayList<String>();
    }

    /**
     * W3CTests main function, see {@link #usage()} how to use.
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        if (args.length > 0) {
            if ("get".equals(args[0])) {
                new W3CTests().getTests();
                return;
            }
            else if ("make".equals(args[0])) {
                new W3CTests().makeTests();
                return;
            }
            else if ("run".equals(args[0])) {
                Datamodel datamodel = Datamodel.fromValue(System.getProperty("datamodel"));
                String testId = System.getProperty("test");
                new W3CTests().runTests(testId, datamodel);
                return;
            }
        }
        usage();
    }

    /**
     * Usage prints the 'commandline' usage options.
     */
    protected static void usage() {
        System.out.println("Usage: W3CTests <get|run>\n" +
                "  get  - downloads the W3C IRP tests\n" +
                "  make - make previously downloaded  W3C IRP tests by transforming the .txml templates\n" +
                "  run  - runs test(s), optionally only for a specific datamodel (default: all)\n\n" +
                "To run a single test, specify -Dtest=<testId>, otherwise all enabled tests will be run.\n" +
                "To only run test(s) for a specific datamodel, specify -Ddatamodel=<minimal|ecma|xpath>.\n");
    }

    /**
     * Downloads the W3C IRP manifest.xml, the IRP ecma and xpath stylesheets to transform the tests, and the
     * actual test templates (.txml) as defined in the manifest.xml
     * @throws Exception
     */
    protected void getTests() throws Exception {
        final File testsSrcDir = new File(TESTS_SRC_DIR);
        if (!testsSrcDir.mkdirs()) {
            FileUtils.cleanDirectory(testsSrcDir);
        }
        new File(TXML_TESTS_DIR).mkdirs();
        new File(MINIMAL_TESTS_DIR).mkdirs();
        new File(ECMA_TESTS_DIR).mkdirs();
        new File(XPATH_TESTS_DIR).mkdirs();
        System.out.println("Downloading IRP manifest: " + SCXML_IRP_BASE_URL + SCXML_IRP_MANIFEST_URI);
        FileUtils.copyURLToFile(new URL(SCXML_IRP_BASE_URL + SCXML_IRP_MANIFEST_URI), new File(testsSrcDir, SCXML_IRP_MANIFEST_URI));
        System.out.println("Downloading ecma stylesheet: " + SCXML_IRP_BASE_URL + SCXML_IRP_ECMA_XSL_URI);
        FileUtils.copyURLToFile(new URL(SCXML_IRP_BASE_URL + SCXML_IRP_ECMA_XSL_URI), new File(testsSrcDir, SCXML_IRP_ECMA_XSL_URI));
        System.out.println("Downloading xpath stylesheet: " + SCXML_IRP_BASE_URL + SCXML_IRP_XPATH_XSL_URI);
        FileUtils.copyURLToFile(new URL(SCXML_IRP_BASE_URL + SCXML_IRP_XPATH_XSL_URI), new File(testsSrcDir, SCXML_IRP_XPATH_XSL_URI));
        Assertions assertions = loadAssertions();
        for (Assertions.Assertion entry : assertions.getAssertions().values()) {
            for (Assertions.TestCase test : entry.getTestCases()) {
                for (Assertions.Resource resource : test.getResources()) {
                    System.out.println("Downloading IRP test file: " + SCXML_IRP_BASE_URL + resource.getUri());
                    FileUtils.copyURLToFile(new URL(SCXML_IRP_BASE_URL + resource.getUri()), new File(TXML_TESTS_DIR + resource.getFilename()));
                }
            }
        }
    }

    /**
     * Transforms the W3C IRP tests.
     * <p>
     * Note: for transforming the IRP .txml test files XPath 2.0 is required, for which the Saxon library is used.
     * </p>
     * @throws Exception
     */
    protected void makeTests() throws Exception {
        final File testsSrcDir = new File(TESTS_SRC_DIR);

        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",null);
        factory.setFeature("http://saxon.sf.net/feature/suppressXsltNamespaceCheck", true);
        Transformer ecmaTransformer = factory.newTransformer(new StreamSource(new FileInputStream(new File(testsSrcDir, SCXML_IRP_ECMA_XSL_URI))));
        Transformer xpathTransformer = factory.newTransformer(new StreamSource(new FileInputStream(new File(testsSrcDir, SCXML_IRP_XPATH_XSL_URI))));
        Transformer minimalTransformer = factory.newTransformer(new StreamSource(getClass().getResourceAsStream(SCXML_IRP_MINIMAL_XSL_FILENAME)));
        Assertions assertions = loadAssertions();
        for (Assertions.Assertion entry : assertions.getAssertions().values()) {
            for (Assertions.TestCase test : entry.getTestCases()) {
                for (Assertions.Resource resource : test.getResources()) {
                    processResource(entry.getSpecId(), resource, minimalTransformer, ecmaTransformer, xpathTransformer);
                }
            }
        }
    }

    /**
     * Unmarshall and return the W3C IRP tests manifest.xml
     * @return an Assertions instance reprenting the W3C IRP tests manifest.xml
     * @throws Exception
     */
    protected Assertions loadAssertions() throws Exception {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Assertions.class);
        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Assertions)jaxbUnmarshaller.unmarshal(new File(TESTS_SRC_DIR, SCXML_IRP_MANIFEST_URI));
    }

    /**
     * Download and transform a W3C IRP test resource file
     * @param specid the SCXML 1.0 spec id (anchor) for the current assertion,
     *               which is used to determine if, how and where the resource should be transformed.
     * @param resource The test resource definition
     * @param minimalTransformer transformer to produce an minimal datamodel SCXML document from the txml resource
     * @param ecmaTransformer transformer to produce an ecmascript datamodel SCXML document from the txml resource
     * @param xpathTransformer transformer to produce a xpath datamodel based SCXML document from the txml resource
     * @throws Exception
     */
    protected void processResource(final String specid, final Assertions.Resource resource,
                                   final Transformer minimalTransformer, final Transformer ecmaTransformer,
                                   final Transformer xpathTransformer)
            throws Exception {
        System.out.println("processing IRP test file " + resource.getFilename());
        FileUtils.copyURLToFile(new URL(SCXML_IRP_BASE_URL + resource.getUri()), new File(TXML_TESTS_DIR + resource.getFilename()));
        if (specid.equals("#minimal-profile")) {
            transformResource(resource, minimalTransformer, MINIMAL_TESTS_DIR);
        }
        else if (specid.equals("#ecma-profile")) {
            transformResource(resource, ecmaTransformer, ECMA_TESTS_DIR);
        }
        else if (specid.equals("#xpath-profile")) {
            transformResource(resource, xpathTransformer, XPATH_TESTS_DIR);
        }
        else {
            transformResource(resource, ecmaTransformer, ECMA_TESTS_DIR);
            transformResource(resource, xpathTransformer, XPATH_TESTS_DIR);
        }
    }

    /**
     * XSL transform a W3C IRP test SCXML resource to a datamodel specific location and format,
     * or simply copy a non SCXML resource to that location.
     * @param resource the test resource definition
     * @param transformer the XSL transformer to use
     * @param targetDir the target location for the transformed SCXML document, or the non-SCXML resource
     * @throws Exception
     */
    protected void transformResource(final Assertions.Resource resource, final Transformer transformer,
                                     final String targetDir) throws Exception {
        if (resource.getFilename().endsWith(".txml")) {
            StreamSource txmlSource = new StreamSource(new FileInputStream(new File(TXML_TESTS_DIR, resource.getFilename())));
            transformer.transform(txmlSource, new StreamResult(new FileOutputStream(new File(targetDir, resource.getName() + ".scxml"))));
        }
        else {
            FileUtils.copyFile(new File(TXML_TESTS_DIR, resource.getFilename()), new File(targetDir, resource.getFilename()));
        }
    }

    protected void createCleanDirectory(final String path) throws Exception {
        final File dir = new File(path);
        if (!dir.mkdirs()) {
            FileUtils.cleanDirectory(dir);
        }
    }

    /**
     * Run one or multiple W3C IRP tests
     * @param testId a W3C IRP test id, or null to specify all tests to run
     * @param datamodel only tests available for or executable with the specified datamodel will be run (or all if null)
     * @throws Exception
     */
    protected void runTests(final String testId, final Datamodel datamodel) throws Exception {
        final Assertions assertions = loadAssertions();
        final Tests tests = loadTests();
        final TestResults results = new TestResults();
        if (testId != null) {
            final Assertions.Assertion assertion = assertions.getAssertions().get(testId);
            if (assertion != null) {
                runTest(assertion, tests, datamodel, true, results);
            }
            else {
                throw new IllegalArgumentException("Unknown test with id: "+testId);
            }
        }
        else {
            for (Assertions.Assertion entry : assertions.getAssertions().values()) {
                runTest(entry, tests, datamodel, false, results);
            }
        }
        System.out.println(
                "\nTest results running " +
                (testId == null ? "all enabled tests" : "test "+testId) +
                (datamodel != null ? " for the "+datamodel.value+" datamodel" : "") +
                ":\n" +
                "  number of tests    : "+(results.testsSkipped+results.testsPassed+results.testsFailed) +
                   " ("+results.testsPassed+" passed,  "+results.testsFailed +" failed,  "+results.testsSkipped+" skipped)");
        if (results.minimalPassed+results.minimalFailed > 0) {
            System.out.println(
                    "    mimimal datamodel: "+results.minimalPassed+" passed,  "+results.minimalFailed+" failed");
        }
        if (results.ecmaPassed+results.ecmaFailed > 0) {
            System.out.println(
                    "    ecma    datamodel: "+results.ecmaPassed+" passed,  "+results.ecmaFailed+" failed");
        }
        if (results.xpathPassed+results.xpathFailed > 0) {
            System.out.println(
                    "    xpath   datamodel: "+results.xpathPassed+" passed,  "+results.xpathFailed+" failed");
        }
        System.out.print("\n");
        if (!results.failedTests.isEmpty()) {
            System.out.println("  failed tests: ");
            for (String filename : results.failedTests) {
                System.out.println("    "+filename);
            }
            System.out.print("\n");
        }
    }

    /**
     * Loads the tests.xml configuration file into a Tests class configuration model instance.
     * @return a Tests instance for the tests.xml configuration file.
     * @throws Exception
     */
    protected Tests loadTests() throws Exception {
        final JAXBContext jaxbContext = JAXBContext.newInstance(Tests.class);
        final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (Tests)jaxbUnmarshaller.unmarshal(getClass().getResource(TESTS_FILENAME));
    }

    /**
     * Run a single W3C IRP test (assert)
     * @param assertion The W3C IRP assert, defining one or more {@link Assertions.TestCase}s
     * @param tests the tests configurations
     * @param datamodel the datamodel to limit and restrict the execution of the test
     * @param singleTest if true a single test id was specified which will be executed even if disabled in the configuration.
     * @throws Exception
     */
    protected void runTest(final Assertions.Assertion assertion, final Tests tests, final Datamodel datamodel,
                           final boolean singleTest, TestResults results) throws Exception {
        final Tests.Test test = tests.getTests().get(assertion.getId());
        if (test == null) {
            throw new IllegalStateException("No test configuration found for W3C IRP test with id: "+assertion.getId());
        }
        boolean skipped = true;
        boolean passed = true;
        if (singleTest || test.isEnabled()) {
            if (datamodel != Datamodel.MINIMAL || datamodel.equals(assertion.getDatamodel())) {
                if (datamodel == null || assertion.getDatamodel() == null || datamodel.equals(assertion.getDatamodel())) {
                    final Datamodel effectiveDM = datamodel != null ? datamodel : assertion.getDatamodel();
                    for (Assertions.TestCase testCase : assertion.getTestCases()) {
                        if (effectiveDM != null) {
                            switch (effectiveDM) {
                                case MINIMAL:
                                    skipped = false;
                                    if (runTests(assertion, testCase, test, MINIMAL_TESTS_DIR, results.failedTests)) {
                                        results.minimalPassed++;
                                    }
                                    else {
                                        passed = false;
                                        results.minimalFailed++;
                                    }
                                    break;
                                case ECMA:
                                    skipped = false;
                                    if (runTests(assertion, testCase, test, ECMA_TESTS_DIR, results.failedTests)) {
                                        results.ecmaPassed++;
                                    }
                                    else {
                                        passed = false;
                                        results.ecmaFailed++;
                                    }
                                    break;
                                case XPATH:
                                    if (test.isXPathEnabled()) {
                                        skipped = false;
                                        if (runTests(assertion, testCase, test, XPATH_TESTS_DIR, results.failedTests)) {
                                            results.xpathPassed++;
                                        }
                                        else {
                                            passed = false;
                                            results.xpathFailed++;
                                        }
                                    }
                                    break;
                            }
                        }
                        else {
                            skipped = false;
                            if (runTests(assertion, testCase, test, ECMA_TESTS_DIR, results.failedTests)) {
                                results.ecmaPassed++;
                            }
                            else {
                                passed = false;
                                results.ecmaFailed++;
                            }
                            if (test.isXPathEnabled()) {
                                if (runTests(assertion, testCase, test, XPATH_TESTS_DIR, results.failedTests)) {
                                    results.xpathPassed++;
                                }
                                else {
                                    passed = false;
                                    results.xpathFailed++;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (skipped) {
            results.testsSkipped++;
        }
        else if (passed) {
            results.testsPassed++;
        }
        else {
            results.testsFailed++;
        }
    }

    /**
     * Execute all W3C IRP SCXML tests for a specific {@link Assertions.TestCase}
     * @param assertion the W3C IRP test assert definition
     * @param testCase the W3C IRP test definition
     * @param test the test configuration
     * @param scxmlDir the datamodel specific directory path containing the SCXML document(s)
     * @throws Exception
     */
    protected boolean runTests(final Assertions.Assertion assertion, final Assertions.TestCase testCase,
                               final Tests.Test test, final String scxmlDir, ArrayList<String> failedTests)
            throws Exception {
        boolean passed = true;
        for (Assertions.Resource scxmlResource : testCase.getScxmlResources()) {
            File scxmlFile = new File(scxmlDir, scxmlResource.getName()+".scxml");
            if (!runTest(testCase, test, scxmlFile)) {
                passed = false;
                failedTests.add(scxmlFile.getParentFile().getName()+"/"+scxmlFile.getName());
            }
        }
        return passed;
    }

    /**
     * Run a single W3C IRP SCXML test
     * @param testCase the W3C IRP test definition
     * @param test the test configuration
     * @param scxmlFile the file handle for the SCXML document
     */
    protected boolean runTest(final Assertions.TestCase testCase, final Tests.Test test, final File scxmlFile) {
        try {
            System.out.println("Executing test: "+scxmlFile.getParentFile().getName()+"/"+scxmlFile.getName());
            final Tracer trc = new Tracer();
            final SCXML doc = SCXMLReader.read(new FileReader(scxmlFile));
            if (doc == null) {
                System.out.println("                FAIL: the SCXML file " +
                        scxmlFile.getCanonicalPath() + " can not be parsed!");
                return false;
            }
            final SCXMLExecutor exec = new SCXMLExecutor(null, null, trc);
            exec.setSingleContext(true);
            exec.setStateMachine(doc);
            exec.addListener(doc, trc);
            exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
            exec.registerInvokerClass("http://www.w3.org/TR/scxml/", SimpleSCXMLInvoker.class);
            exec.go();
            Final end;
            while ((end = exec.getStatus().getFinalState()) == null) {
                Thread.sleep(100);
                exec.triggerEvents();
            }
            System.out.println("                final state: "+end.getId());
            if (!testCase.isManual()) {
                return end.getId().equals("pass");
            }
            else if (test.getFinalState() != null) {
                return end.getId().equals(test.getFinalState());
            }
            else {
                // todo: manual verification for specific tests
                return false;
            }
        }
        catch (Exception e) {
            System.out.println("                FAIL: "+e.getMessage());
            return false;
        }
    }
}
