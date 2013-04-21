package org.htmlunit.maven.runner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.TestDebugServer;
import org.htmlunit.maven.ResourceUtils;
import org.htmlunit.maven.RunnerContext;

/** Runner to load JavaScript unit test environment into htmlunit.
 */
public class JavaScriptTestRunner extends AbstractRunner {

  /** Default test runner template. */
  private static final String DEFAULT_TEMPLATE =
      "classpath:/org/htmlunit/maven/DefaultTestRunner.html";

  /** Test runner file name. */
  private static final String TEST_RUNNER_SUFFIX = "Runner.html";

  /** Path to the test runner template. */
  private URL testRunnerTemplate;

  /** Script included after page load in order to run the tests; can be
   * null. */
  private URL testRunnerScript;

  /** List of scripts loaded before test scripts; valid after initialize(). */
  private List<URL> bootstrapScripts = new ArrayList<URL>();

  /** List of scripts loaded before test scripts and after bootstrap
   * scripts; valid after initialize(). */
  private List<URL> sourceScripts = new ArrayList<URL>();

  /** Test files. Can be any resource identified as test by the current
   * runner. It's never null after initialize().
   */
  private List<URL> testFiles = new ArrayList<URL>();

  /** Test runner and test cases output directory; it's never null
   * after initialize();
   */
  private File outputDirectory;

  /** Indicates whether to run tests in debug mode or not. When it's in
   * debug mode, tests can be opened in the browser.
   */
  private boolean debugMode;

  /** Port to start debug server. Default is 8000. */
  private int debugPort = 8000;

  /** {@inheritDoc}
   */
  @Override
  public void run() {
    if (debugMode) {
      runServer();
    } else {
      runDriver();
    }
  }

  /** {@inheritDoc}
   */
  @Override
  protected void configure(final RunnerContext theConfiguration) {
    Properties runnerConfig = theConfiguration.getRunnerConfiguration();

    try {
      if (DefaultAttributes.DEBUG_MODE.in(runnerConfig)) {
        debugMode = (Boolean) runnerConfig.get(
            DefaultAttributes.DEBUG_MODE.getKey());
      }

      if (DefaultAttributes.DEBUG_PORT.in(runnerConfig)) {
        debugPort = (Integer) runnerConfig.get(
            DefaultAttributes.DEBUG_PORT.getKey());
      }

      if (DefaultAttributes.TEST_RUNNER_TEMPLATE.in(runnerConfig)) {
        String template = (String) runnerConfig
            .get(DefaultAttributes.TEST_RUNNER_TEMPLATE.getKey());
        List<URL> urls = ResourceUtils.expand(template);

        if (urls.size() > 0) {
          testRunnerTemplate = urls.get(0);
        } else {
          testRunnerTemplate = new URL(template);
        }
      } else {
        testRunnerTemplate = new URL(DEFAULT_TEMPLATE);
      }
    } catch (MalformedURLException cause) {
      throw new RuntimeException("Cannot resolve runner template.");
    }

    if (DefaultAttributes.TEST_RUNNER_SCRIPT.in(runnerConfig)) {
      testRunnerScript = expand((String) runnerConfig
          .get(DefaultAttributes.TEST_RUNNER_SCRIPT.getKey())).get(0);
    }
    if (DefaultAttributes.BOOTSTRAP_SCRIPTS.in(runnerConfig)) {
      bootstrapScripts = expand((String) runnerConfig
          .get(DefaultAttributes.BOOTSTRAP_SCRIPTS.getKey()));
    }
    if (DefaultAttributes.SOURCE_SCRIPTS.in(runnerConfig)) {
      sourceScripts = expand((String) runnerConfig
          .get(DefaultAttributes.SOURCE_SCRIPTS.getKey()));
    }
    if (DefaultAttributes.TEST_FILES.in(runnerConfig)) {
      testFiles = expand((String) runnerConfig
          .get(DefaultAttributes.TEST_FILES.getKey()));
    }
    Validate.isTrue(DefaultAttributes.OUTPUT_DIR.in(runnerConfig),
        "Output directory must be specified.");
    outputDirectory = new File((String) runnerConfig
        .get(DefaultAttributes.OUTPUT_DIR.getKey()));
  }

  /** Invoked when a single test finished.
   * @param test Test that finished. It's never null.
   */
  protected void testFinished(final URL test) {
  }

  /** Creates the test runner for the specified test and writes the processed
   * template to the runner. By default, the test runner will be named as the
   * test file plus a constant suffix.
   *
   * @param testFile Test script to create runner file for. Cannot be null.
   * @return The generated runner URL. Never returns null.
   */
  protected URL createTestRunnerFile(final URL testFile) {
    String baseName = FilenameUtils.getBaseName(testFile.getFile());
    File runnerFile = new File(outputDirectory, baseName + TEST_RUNNER_SUFFIX);

    // Generates a new template and prepares the environment.
    String htmlTemplate = ResourceUtils.readAsText(testRunnerTemplate);
    StringTemplate template = new StringTemplate(htmlTemplate,
        DefaultTemplateLexer.class);
    loadResources(template);

    // Loads test file into template.
    loadTest(template, testFile);

    try {
      FileUtils.writeStringToFile(runnerFile, template.toString());
      return runnerFile.toURI().toURL();
    } catch (IOException cause) {
      throw new RuntimeException("Cannot write runner file", cause);
    }
  }

  /** Can be overridden in order to prepare the runner template before writing
   * to file and loading it into the web client.
   *
   * <p>
   * By default it performs replacement of {@link DefaultAttributes}.
   * </p>
   * @param testRunner Template already loaded. Cannot be null.
   */
  protected void loadResources(final StringTemplate testRunner) {
    if (testRunnerScript != null) {
      testRunner.setAttribute(DefaultAttributes.TEST_RUNNER_SCRIPT.getKey(),
          generateScriptTags(Arrays.asList(testRunnerScript)));
    }
    if (debugMode) {
      bootstrapScripts.addAll(TestDebugServer
          .getDebugBootstrapScripts("localhost", debugPort));
    }
    testRunner.setAttribute(DefaultAttributes.BOOTSTRAP_SCRIPTS.getKey(),
        generateScriptTags(bootstrapScripts));
    testRunner.setAttribute(DefaultAttributes.SOURCE_SCRIPTS.getKey(),
        generateScriptTags(sourceScripts));
  }

  /** Loads test file into test runner template. By default, it expands
   * the test file pattern and puts &lt;script&gt; tags for expanded files.
   *
   * Can be overridden to change the test load strategy.
   *
   * @param runnerTemplate Current runner template. Cannot be null.
   * @param test Test to load. Cannot be null.
   */
  protected void loadTest(final StringTemplate runnerTemplate,
      final URL test) {
    runnerTemplate.setAttribute(DefaultAttributes.TEST_FILES.getKey(),
        generateScriptTags(Arrays.asList(test)));
  }

  /** Expands the specified resource matching expression into real
   * resources.
   * @param expression Expression to expand. Cannot be null.
   * @return A valid list of resources. Never returns null.
   */
  private List<URL> expand(final String expression) {
    List<URL> resources = ResourceUtils.expand(Arrays
        .asList(expression.split(";")));

    if (debugMode) {
      // In debug mode, all resources are served by the debug server.
      // It transforms resource urls into debug urls.
      List<URL> serverUrls = new ArrayList<URL>();

      for (URL resource : resources) {
        serverUrls.add(TestDebugServer.getStaticContentUrl("localhost",
            debugPort, resource));
      }
      return serverUrls;
    } else {
      return resources;
    }
  }

  /** Generates a list of HTML script tags for the specified list of sources.
   * @param sources JavaScript source files. Cannot be null.
   * @return A valid HTML, never returns null.
   */
  private String generateScriptTags(final List<URL> sources) {
    StringBuilder scripts = new StringBuilder();
    for (URL source : sources) {
      scripts.append("<script type=\"text/javascript\" src=\"")
        .append(source).append("\"></script>");
    }
    return scripts.toString();
  }

  /** Runs tests using the web driver.
   *
   * @param runnerFile Runner file to load into the web driver. Cannot be null.
   * @param testFile Test file being executed. Cannot be null.
   */
  private void runDriver() {
    for (URL testFile : testFiles) {
      URL runner = createTestRunnerFile(testFile);

      // Executes the test and waits for completion.
      getDriver().get(runner.toString());

      waitCompletion();

      // Notifies test finish.
      testFinished(testFile);

      // WebDriver doesn't switch automatically.
      String windowHandle = (String) CollectionUtils
          .get(getDriver().getWindowHandles(), 0);
      getDriver().switchTo().window(windowHandle);
    }
  }

  /** Runs tests using the a web server to allow debugging from browsers.
   *
   * @param runnerFile Runner file to load into the web driver. Cannot be null.
   * @param testFile Test file being executed. Cannot be null.
   */
  private void runServer() {
    try {
      debugServer.setTestFiles(testFiles);
      debugServer.start();
    } catch (IOException cause) {
      throw new RuntimeException("Cannot start web server.", cause);
    }
  }

  /** HTTP server for debug mode.
   */
  private TestDebugServer debugServer = new TestDebugServer(debugPort) {
    /** {@inheritDoc}
     */
    @Override
    public URL getRunner(final URL testFile) {
      return createTestRunnerFile(testFile);
    }

    /** Adds the runner configuration to the window's global scope in order to
     * keep compatibility with non-debug tests.
     * <p>
     * {@inheritDoc}
     * </p>
     */
    @Override
    protected InputStream getDebugScript() {
      Set<Entry<Object, Object>> entries = getContext()
          .getRunnerConfiguration().entrySet();
      StringBuilder debugCode = new StringBuilder();

      for (Entry<Object, Object> entry : entries) {
        debugCode.append("window[\"" + entry.getKey() + "\"] = \""
            + entry.getValue() + "\";\n");
      }

      return new ByteArrayInputStream(debugCode.toString().getBytes());
    }
  };

  /** Enum of attributes available in the default test runner template.
   */
  public static enum DefaultAttributes {
    /** Test runner template configuration key. */
    TEST_RUNNER_TEMPLATE("testRunnerTemplate"),

    /** Script included at the end of page load to proceed running the tests.
     */
    TEST_RUNNER_SCRIPT("testRunnerScript"),

    /** Output directory configuration key. */
    OUTPUT_DIR("outputDirectory"),

    /** Bootstrap scripts configuration key.
     */
    BOOTSTRAP_SCRIPTS("bootstrapScripts"),

    /** Source scripts configuration key. They're included after bootstrap
     * scripts and before test scripts.
     */
    SOURCE_SCRIPTS("sourceScripts"),

    /** Test files. Can be any resource identified as test by the current
     * runner. Cannot be null.
     */
    TEST_FILES("testFiles"),

    /** Indicates whether to run tests in debug mode or not. When it's in
     * debug mode, tests can be opened in the browser.
     */
    DEBUG_MODE("debugMode"),

    /** Port to start debug server. Default is 8000.
     */
    DEBUG_PORT("debugPort");

    /** Attribute configuration key. */
    private String key;

    /** Sets the attribute name.
     * @param theKey Attribute name. Cannot be null or empty.
     */
    private DefaultAttributes(final String theKey) {
      Validate.notEmpty(theKey, "The attribute key cannot be null.");
      key = theKey;
    }

    /** Returns the attribute configuration key.
     * @return A valid string key, never returns null.
     */
    public String getKey() {
      return key;
    }

    /** Determines whether the attribute exist in the specified properties.
     * @param props Properties to check. Cannot be null.
     * @return true if the attribute exists in the properties, false otherwise.
     */
    public boolean in(final Properties props) {
      return props.containsKey(getKey());
    }
  }
}
