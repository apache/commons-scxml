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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.scxml2.PathResolver;
import org.apache.commons.scxml2.SCXMLExecutor;
import org.apache.commons.scxml2.env.Tracer;
import org.apache.commons.scxml2.env.URLResolver;
import org.apache.commons.scxml2.invoke.SimpleSCXMLInvoker;
import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.Final;
import org.apache.commons.scxml2.model.SCXML;

/**
 * W3C SCXML 1.0 IRP tests: <a href="http://www.w3.org/Voice/2013/scxml-irp/">http://www.w3.org/Voice/2013/scxml-irp/</a>.
 * <p>
 * The <strong>W3CTests</strong> class is standalone and can download and transform the IRP tests locally using respectively
 * command-line parameter <strong>get</strong> or <strong>make</strong>.
 * </p>
 * <p>
 * To execute one or multiple IRP tests the command-line parameter <strong>run</strong> must be specified.
 * </p>
 * <p>
 * Optional environment parameter <strong>-Ddatamodel=&lt;minimal|ecma|jexl|groovy&gt;</strong> can be specified to limit the
 * execution of the tests for and using only the specified datamodel language.
 * </p>
 * <p>
 * Optional environment parameter <strong>-Dtest=&lt;testId&gt;</strong> can be specified to only execute a single test, which
 * also can be combined with the <strong>-Ddatamodel</strong> parameter.
 * </p>
 * <p>
 * The W3CTests also uses a separate <strong><code>tests.xml</code></strong> configuration file, located in the
 * <strong><code>src/test/resources/w3c</code></strong> directory, which is manually maintained to enable|disable execution
 * of tests (when <em>not</em> using the <strong>-Dtest</strong> parameter, which will always execute the specified test).<br/>
 * Furthermore, in this configuration file the current <em>success</em> or <em>failure</em> status, and even more
 * meta data per test is maintained.
 * </p>
 */
@SuppressWarnings("unused")
public class W3CTests {

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

            public Datamodel getDatamodel() {
                if ("#minimal-profile".equals(specid)) {
                    return Datamodel.MINIMAL;
                }
                if ("#ecma-profile".equals(specid)) {
                    return Datamodel.ECMA;
                }
                return null;
            }

            public String getId() {
                return id;
            }

            public String getSpecId() {
                return specid;
            }

            public String getSpecNum() {
                return specnum;
            }

            public List<TestCase> getTestCases() {
                return testCases != null ? testCases : Collections.emptyList();
            }

            @Override
            public String toString() {
                return id;
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        protected static class Resource {

            @XmlAttribute
            private String uri;

            public String getFileName() {
                return uri.substring(uri.indexOf("/")+1);
            }

            public String getName() {
                return uri.substring(uri.indexOf("/")+1, uri.indexOf("."));
            }

            public String getUri() {
                return uri;
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

            public List<Resource> getResources() {
                if (resources == null) {
                    resources = new ArrayList<>();
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

            public List<Resource> getScxmlResources() {
                return scxmlResources != null ? scxmlResources : Collections.emptyList();
            }

            public boolean isManual() {
                return Boolean.parseBoolean(manual);
            }

            public boolean isOptional() {
                return "mandatory".equals(conformance);
            }
        }

        @XmlElement(name="assert")
        private ArrayList<Assertion> assertions;

        private LinkedHashMap<String, Assertion> assertionsMap;

        public LinkedHashMap<String, Assertion> getAssertions() {
            if (assertionsMap == null) {
                assertionsMap = new LinkedHashMap<>();
                if (assertions != null) {
                    for (final Assertion a : assertions) {
                        assertionsMap.put(a.getId(), a);
                    }
                }
            }
            return assertionsMap;
        }
    }
    /**
     * Datamodel enum representing the datamodel types used and tested with the W3C IRP tests.
     */
    protected enum Datamodel {

        MINIMAL("minimal", "minimal"),
        ECMA("ecma",       "ecma   "),
        JEXL("jexl",       "jexl   "),
        GROOVY("groovy",   "groovy ");

        public static Datamodel fromValue(final String value) {
            for (final Datamodel datamodel : Datamodel.values()) {
                if (datamodel.value().equals(value)) {
                    return datamodel;
                }
            }
            return null;
        }
        private final String value;
        private final String label;

        private final String testDir;

        Datamodel(final String value, final String label) {
            this.value = value;
            this.label = label;
            this.testDir = TESTS_SRC_DIR_STR + value + "/";
        }

        public String label() {
            return label;
        }

        public String testDir() {
            return testDir;
        }

        public String value() {
            return value;
        }
    }
    /**
     * Simple TestResult data struct for tracking test results
     */
    protected static class TestResults {
        final Map<Datamodel, Integer> passed = new HashMap<>();
        final Map<Datamodel, Integer> failed = new HashMap<>();
        final Map<Datamodel, Integer> skipped = new HashMap<>();
        final ArrayList<String> changedStatusTests = new ArrayList<>();

        public int failed(final Datamodel dm) {
            return failed.get(dm) != null ? failed.get(dm) : 0;
        }

        public int passed(final Datamodel dm) {
            return passed.get(dm) != null ? passed.get(dm) : 0;
        }

        public int skipped(final Datamodel dm) {
            return skipped.get(dm) != null ? skipped.get(dm) : 0;
        }
    }

    /**
     * Tests model class used for loading the <strong>tests.xml</strong> configuration file
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
            @XmlAttribute
            private String finalState;
            @XmlAttribute
            private Boolean implemented;
            @XmlAttribute(name="minimal")
            private Boolean minimalStatus;
            @XmlAttribute(name="ecma")
            private Boolean ecmaStatus;
            @XmlAttribute(name="jexl")
            private boolean jexlStatus;
            @XmlAttribute(name="groovy")
            private boolean groovyStatus;
            @XmlValue
            private String comment;

            public String getComment() {
                return comment;
            }

            public String getFinalState() {
                return finalState;
            }

            public String getId() {
                return id;
            }

            public Boolean getStatus(final Datamodel dm) {
                switch (dm) {
                    case ECMA:
                        return ecmaStatus;
                    case JEXL:
                        return jexlStatus;
                    case GROOVY:
                        return groovyStatus;
                    default:
                        return minimalStatus;
                }
            }

            public boolean isImplemented() {
                return implemented == null || implemented;
            }

            public boolean isMandatory() {
                return mandatory;
            }
            public boolean isManual() {
                return manual == null || manual;
            }

            @Override
            public String toString() {
                return id;
            }
        }

        @XmlElement(name="test")
        private ArrayList<Test> tests;

        private LinkedHashMap<String, Test> testsMap;

        public LinkedHashMap<String, Test> getTests() {
            if (testsMap == null) {
                testsMap = new LinkedHashMap<>();
                if (tests != null) {
                    for (final Test t : tests) {
                        testsMap.put(t.getId(), t);
                    }
                }
            }
            return testsMap;
        }
    }
    private static final String SCXML_IRP_BASE_URL = "http://www.w3.org/Voice/2013/scxml-irp/";
    private static final String SCXML_IRP_MANIFEST_URI = "manifest.xml";
    private static final String SCXML_IRP_ECMA_XSL_URI = "confEcma.xsl";
    private static final String TESTS_SRC_DIR_STR = "src/w3c/scxml-irp/";
    private static final Path TESTS_SRC_DIR = Paths.get(TESTS_SRC_DIR_STR);
    private static final Path TXML_TESTS_DIR = TESTS_SRC_DIR.resolve("txml/");
    private static final String PACKAGE_PATH = "/" + W3CTests.class.getPackage().getName().replace('.', '/');

    private static final String TESTS_FILENAME = PACKAGE_PATH + "/tests.xml";

    private static final String SCXML_IRP_MINIMAL_XSL_FILENAME = PACKAGE_PATH + "/confMinimal.xsl";

    private static final String SCXML_IRP_JEXL_XSL_FILENAME = PACKAGE_PATH + "/confJexl.xsl";

    private static final String SCXML_IRP_GROOVY_XSL_FILENAME = PACKAGE_PATH + "/confGroovy.xsl";

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
            if ("make".equals(args[0])) {
                new W3CTests().makeTests();
                return;
            }
            if ("run".equals(args[0])) {
                final Datamodel datamodel = Datamodel.fromValue(System.getProperty("datamodel"));
                final String testId = System.getProperty("test");
                new W3CTests().runTests(testId, datamodel);
                return;
            }
        }
        usage();
    }

    /**
     * Usage prints the 'command-line' usage options.
     */
    protected static void usage() {
        System.out.println("Usage: W3CTests <get|run>\n" +
                "  get  - downloads the W3C IRP tests\n" +
                "  make - make previously downloaded  W3C IRP tests by transforming the .txml templates\n" +
                "  run  - runs test(s), optionally only for a specific datamodel (default: all)\n" +
                "         To run a single test, specify -Dtest=<testId>, otherwise all tests will be run.\n" +
                "         To only run test(s) for a specific datamodel, specify -Ddatamodel=<minimal|ecma|jexl|groovy>.\n" +
                "         By default only enabled tests (for the specified datamodel, or all) are run,\n" +
                "         specify -Denabled=false to only run disabled tests.\n");
    }

    protected void createCleanDirectory(final String path) throws Exception {
        final File dir = new File(path);
        if (!dir.mkdirs()) {
            FileUtils.cleanDirectory(dir);
        }
    }

    /**
     * Downloads the W3C IRP manifest.xml, the IRP ecma stylesheet to transform the tests, and the actual test templates (.txml) as defined in the manifest.xml
     * 
     * @throws Exception
     */
    protected void getTests() throws Exception {
        final File testsSrcDir = new File(TESTS_SRC_DIR_STR);
        if (!testsSrcDir.mkdirs()) {
            FileUtils.cleanDirectory(testsSrcDir);
        }
        Files.createDirectories(TESTS_SRC_DIR);
        for (final Datamodel dm : Datamodel.values()) {
            new File(dm.testDir()).mkdirs();
        }
        System.out.println("Downloading IRP manifest: " + SCXML_IRP_BASE_URL + SCXML_IRP_MANIFEST_URI);
        PathUtils.copyFile(new URL(SCXML_IRP_BASE_URL + SCXML_IRP_MANIFEST_URI), TESTS_SRC_DIR.resolve(SCXML_IRP_MANIFEST_URI));
        System.out.println("Downloading ecma stylesheet: " + SCXML_IRP_BASE_URL + SCXML_IRP_ECMA_XSL_URI);
        PathUtils.copyFile(new URL(SCXML_IRP_BASE_URL + SCXML_IRP_ECMA_XSL_URI), TESTS_SRC_DIR.resolve(SCXML_IRP_ECMA_XSL_URI));
        final Assertions assertions = loadAssertions();
        for (final Assertions.Assertion entry : assertions.getAssertions().values()) {
            for (final Assertions.TestCase test : entry.getTestCases()) {
                for (final Assertions.Resource resource : test.getResources()) {
                    System.out.println("Downloading IRP test file: " + SCXML_IRP_BASE_URL + resource.getUri());
                    PathUtils.copyFile(new URL(SCXML_IRP_BASE_URL + resource.getUri()), TXML_TESTS_DIR.resolve(resource.getFileName()));
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
        return (Assertions)jaxbUnmarshaller.unmarshal(TESTS_SRC_DIR.resolve(SCXML_IRP_MANIFEST_URI).toFile());
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
     * Transforms the W3C IRP tests.
     * <p>
     * Note: for transforming the IRP .txml test files XPath 2.0 is required, for which the Saxon library is used.
     * </p>
     * @throws Exception
     */
    protected void makeTests() throws Exception {
        final TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",null);
        factory.setFeature("http://saxon.sf.net/feature/suppressXsltNamespaceCheck", true);
        final Map<Datamodel, Transformer> transformers = new HashMap<>();
        transformers.put(Datamodel.ECMA, factory.newTransformer(new StreamSource(Files.newInputStream(TESTS_SRC_DIR.resolve(SCXML_IRP_ECMA_XSL_URI)))));
        transformers.put(Datamodel.MINIMAL, factory.newTransformer(new StreamSource(getClass().getResourceAsStream(SCXML_IRP_MINIMAL_XSL_FILENAME))));
        transformers.put(Datamodel.JEXL, factory.newTransformer(new StreamSource(getClass().getResourceAsStream(SCXML_IRP_JEXL_XSL_FILENAME))));
        transformers.put(Datamodel.GROOVY, factory.newTransformer(new StreamSource(getClass().getResourceAsStream(SCXML_IRP_GROOVY_XSL_FILENAME))));
        final Assertions assertions = loadAssertions();
        for (final Assertions.Assertion entry : assertions.getAssertions().values()) {
            for (final Assertions.TestCase test : entry.getTestCases()) {
                for (final Assertions.Resource resource : test.getResources()) {
                    processResource(entry.getSpecId(), resource, transformers);
                }
            }
        }
    }

    /**
     * Download and transform a W3C IRP test resource file
     * 
     * @param specid       the SCXML 1.0 spec id (anchor) for the current assertion, which is used to determine if, how and where the resource should be
     *                     transformed.
     * @param resource     The test resource definition
     * @param transformers map of datamodel transformers to produce a datamodel specific SCXML document from the txml resource
     * @throws Exception
     */
    protected void processResource(final String specid, final Assertions.Resource resource, final Map<Datamodel, Transformer> transformers) throws Exception {
        System.out.println("processing IRP test file " + resource.getFileName());
        PathUtils.copyFile(new URL(SCXML_IRP_BASE_URL + resource.getUri()), TXML_TESTS_DIR.resolve(resource.getFileName()));
        switch (specid) {
        case "#minimal-profile":
            transformResource(resource, transformers.get(Datamodel.MINIMAL), Datamodel.MINIMAL.testDir());
            break;
        case "#ecma-profile":
            transformResource(resource, transformers.get(Datamodel.ECMA), Datamodel.ECMA.testDir());
            break;
        default:
            for (final Datamodel dm : transformers.keySet()) {
                if (dm != Datamodel.MINIMAL) {
                    transformResource(resource, transformers.get(dm), dm.testDir());
                }
            }
            break;
        }
    }

    /**
     * Run a single W3C IRP assert test
     * @param assertion The W3C IRP assert, defining one or more {@link Assertions.TestCase}s
     * @param tests the tests configurations
     * @param datamodel the datamodel to limit and restrict the execution of the test
     * @param status true to run the test with status true for the (or any) datamodel, false to do so for status false
     * @param singleTest if true a single test id was specified which will be executed even if disabled in the configuration.
     * @throws Exception
     */
    protected void runAssert(final Assertions.Assertion assertion, final Tests tests, final Datamodel datamodel,
                             final boolean status, final boolean singleTest, final TestResults results) {
        final Tests.Test test = tests.getTests().get(assertion.getId());
        if (test == null) {
            throw new IllegalStateException("No test configuration found for W3C IRP test with id: "+assertion.getId());
        }
        if (test.isImplemented()) {
            for (final Assertions.TestCase testCase : assertion.getTestCases()) {
                for (final Datamodel dm : Datamodel.values()) {
                    if (assertion.getDatamodel() == null && dm != Datamodel.MINIMAL || dm == assertion.getDatamodel()) {
                        boolean skipped = true;
                        if (datamodel == null || datamodel == dm) {
                            if (singleTest || test.getStatus(dm) == null || status == test.getStatus(dm)) {
                                for (final Assertions.Resource scxmlResource : testCase.getScxmlResources()) {
                                    final File scxmlFile = new File(dm.testDir(), scxmlResource.getName()+".scxml");
                                    skipped = false;
                                    final boolean success = runTest(testCase, test, scxmlFile);
                                    if (!success) {
                                        results.failed.put(dm, results.failed(dm)+1);
                                    } else {
                                        results.passed.put(dm, results.passed(dm)+1);
                                    }
                                    if (success != status) {
                                        results.changedStatusTests.add(scxmlFile.getParentFile().getName()+"/"+scxmlFile.getName());
                                    }
                                }
                            }
                        }
                        if (skipped) {
                            results.skipped.put(dm, results.skipped(dm)+1);
                        }
                    }
                }
            }
        }
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
            final PathResolver pathResolver = new URLResolver(scxmlFile.getParentFile().toURI().toURL());
            final SCXMLReader.Configuration configuration = new SCXMLReader.Configuration(null, pathResolver);
            final SCXML doc = SCXMLReader.read(new FileReader(scxmlFile), configuration);
            if (doc == null) {
                System.out.println("                FAIL: the SCXML file " +
                        scxmlFile.getCanonicalPath() + " cannot be parsed!");
                return false;
            }
            final SCXMLExecutor exec = new SCXMLExecutor(null, null, trc);
            exec.setSingleContext(true);
            exec.setStateMachine(doc);
            exec.addListener(doc, trc);
            exec.registerInvokerClass("scxml", SimpleSCXMLInvoker.class);
            exec.registerInvokerClass("http://www.w3.org/TR/scxml/", SimpleSCXMLInvoker.class);
            exec.run().join();
            final Final end = exec.getStatus().getFinalState();
            System.out.println("                final state: "+end.getId());
            if (!testCase.isManual()) {
                return end.getId().equals("pass");
            }
            if (test.getFinalState() != null) {
                return end.getId().equals(test.getFinalState());
            }
            // todo: manual verification for specific tests
            return false;
        }
        catch (final Exception e) {
            if (test.isManual() && e.getMessage() != null && e.getMessage().equals(test.getFinalState())) {
                System.out.println("                PASS: "+e.getMessage());
                return true;
            }
            System.out.println("                FAIL: "+e.getMessage());
            return false;
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
        final boolean enabled = Boolean.parseBoolean(System.getProperty("enabled", "true"));
        if (testId != null) {
            final Assertions.Assertion assertion = assertions.getAssertions().get(testId);
            if (assertion == null) {
                throw new IllegalArgumentException("Unknown test with id: "+testId);
            }
            runAssert(assertion, tests, datamodel, enabled, true, results);
        }
        else {
            for (final Assertions.Assertion entry : assertions.getAssertions().values()) {
                runAssert(entry, tests, datamodel, enabled, false, results);
            }
        }
        System.out.println(
                "\nTest results running " +
                (testId == null ? "all tests" : "test "+testId) +
                (datamodel != null ? " for the "+datamodel.value+" datamodel" : "") + (enabled ? " enabled" : " disabled"));
        for (final Datamodel dm : Datamodel.values()) {
            if (datamodel == null || datamodel == dm) {
                System.out.println(
                        "    "+dm.label()+" datamodel: "+results.passed(dm)+" passed,  " +
                                results.failed(dm)+" failed, " +
                                results.skipped(dm)+" skipped ("+
                                (results.passed(dm)+results.failed(dm)+results.skipped(dm))+" total)");
            }
        }
        System.out.print("\n");
        if (testId == null && !results.changedStatusTests.isEmpty()) {
            System.out.println("  "+(enabled? "failed" : "passed")+" tests: ");
            for (final String filename : results.changedStatusTests) {
                System.out.println("    "+filename);
            }
            System.out.print("\n");
        }
    }

    /**
     * XSL transform a W3C IRP test SCXML resource to a datamodel specific location and format, or simply copy a non SCXML resource to that location.
     *
     * @param resource    the test resource definition
     * @param transformer the XSL transformer to use
     * @param targetDir   the target location for the transformed SCXML document, or the non-SCXML resource
     * @throws Exception
     */
    protected void transformResource(final Assertions.Resource resource, final Transformer transformer, final String targetDir) throws Exception {
        if (resource.getFileName().endsWith(".txml")) {
            try (InputStream source = Files.newInputStream(TXML_TESTS_DIR.resolve(resource.getFileName()));
                    FileOutputStream result = new FileOutputStream(new File(targetDir, resource.getName() + ".scxml"))) {
                transformer.transform(new StreamSource(source), new StreamResult(result));
            }
        } else {
            Files.copy(TXML_TESTS_DIR.resolve(resource.getFileName()), Paths.get(targetDir, resource.getFileName()));
        }
    }
}
