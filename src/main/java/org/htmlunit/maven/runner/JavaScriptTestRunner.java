package org.htmlunit.maven.runner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.ResourceUtils;
import org.htmlunit.maven.RunnerContext;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Runner to load JavaScript unit test environment into htmlunit.
 */
public class JavaScriptTestRunner extends AbstractRunner {

  /** Default test runner template. */
  private static final String DEFAULT_TEMPLATE =
      "classpath:/org/htmlunit/maven/DefaultTestRunner.html";

  /** Test runner file name. */
  private static final String TEST_RUNNER_FILE = "TestRunner.html";

  /** Path to the test runner template. */
  private String testRunnerTemplate = DEFAULT_TEMPLATE;

  /** Script included after page load in order to run the tests; can be
   * null. */
  private URL testRunnerScript;

  /** List of scripts loaded before test scripts; valid after initialize(). */
  private List<URL> bootstrapScripts = new ArrayList<URL>();

  /** List of scripts loaded before test scripts and after bootstrap
   * scripts; valid after initialize(). */
  private List<URL> sourceScripts = new ArrayList<URL>();

  /** List of scripts containing test code; valid after initialize(). */
  private List<URL> testScripts = new ArrayList<URL>();

  /** Test runner and test cases output directory; it's never null
   * after initialize();
   */
  private File outputDirectory;

  /** Test runner output file; it's never null after initialize();
   */
  private File testRunnerFile;

  /** {@inheritDoc}
   */
  @Override
  protected void configure(final RunnerContext theConfiguration) {
    Properties runnerConfig = theConfiguration.getRunnerConfiguration();

    if (DefaultAttributes.TEST_RUNNER_TEMPLATE.in(runnerConfig)) {
      testRunnerTemplate = (String) runnerConfig
          .get(DefaultAttributes.TEST_RUNNER_TEMPLATE.getKey());
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
    if (DefaultAttributes.TEST_SCRIPTS.in(runnerConfig)) {
      testScripts = expand((String) runnerConfig
          .get(DefaultAttributes.TEST_SCRIPTS.getKey()));
    }
    Validate.isTrue(DefaultAttributes.OUTPUT_DIR.in(runnerConfig),
        "Output directory must be specified.");
    outputDirectory = new File((String) runnerConfig
        .get(DefaultAttributes.OUTPUT_DIR.getKey()));
    testRunnerFile = new File(outputDirectory, TEST_RUNNER_FILE);
  }

  /** {@inheritDoc}
   */
  public void run() {
    StringTemplate template = createTestRunnerTemplate();
    doPrepare(template);
    try {
      FileUtils.writeStringToFile(testRunnerFile, template.toString());
      getDriver().get(testRunnerFile.toURI().toURL().toString());
      new WebDriverWait(getDriver(), getContext().getTimeout(), 1000);
    } catch (IOException cause) {
      throw new RuntimeException("Cannot write test runner file.", cause);
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
  protected void doPrepare(final StringTemplate testRunner) {
    testRunner.setAttribute(DefaultAttributes.TEST_RUNNER_SCRIPT.getKey(),
        generateScriptTags(Arrays.asList(testRunnerScript)));
    testRunner.setAttribute(DefaultAttributes.BOOTSTRAP_SCRIPTS.getKey(),
        generateScriptTags(bootstrapScripts));
    testRunner.setAttribute(DefaultAttributes.SOURCE_SCRIPTS.getKey(),
        generateScriptTags(sourceScripts));
    testRunner.setAttribute(DefaultAttributes.TEST_SCRIPTS.getKey(),
        generateScriptTags(testScripts));
  }

  /** Expands the specified resource matching expression into real
   * resources.
   * @param expression Expression to expand. Cannot be null.
   * @return A valid list of resources. Never returns null.
   */
  private List<URL> expand(final String expression) {
    String[] resources = expression.split(";");
    return ResourceUtils.expand(Arrays.asList(resources));
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

  /** Creates the test runner template.
   * @return A valid template. Never returns null.
   */
  private StringTemplate createTestRunnerTemplate() {
    try {
      URL url = URI.create(testRunnerTemplate).toURL();
      String htmlTemplate = ResourceUtils.readAsText(url);

      return new StringTemplate(htmlTemplate, DefaultTemplateLexer.class);
    } catch (MalformedURLException cause) {
      throw new RuntimeException("Test runner template URL isn't valid.",
          cause);
    }
  }

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

    /** Test scripts. They must register tests.
     */
    TEST_SCRIPTS("testScripts");

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
