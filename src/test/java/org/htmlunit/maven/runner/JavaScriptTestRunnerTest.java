package org.htmlunit.maven.runner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.ReflectionUtils;
import org.htmlunit.maven.RunnerContext;
import org.htmlunit.maven.runner.JavaScriptTestRunner;
import org.htmlunit.maven.runner.JavaScriptTestRunner.DefaultAttributes;
import org.junit.Before;
import org.junit.Test;

/** Tests the {@link JavaScriptTestRunner} class.
 */
public class JavaScriptTestRunnerTest {

  private RunnerContext context;
  private JavaScriptTestRunner runner;

  @Before
  public void setUp() {
    context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put(DefaultAttributes.OUTPUT_DIR.getKey(),
        System.getProperty("java.io.tmpdir"));
    runnerConfig.put(DefaultAttributes.TEST_RUNNER_TEMPLATE.getKey(),
        "classpath:org/htmlunit/maven/TestRunner.html");
    runnerConfig.put(DefaultAttributes.TEST_RUNNER_SCRIPT.getKey(),
        "classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put(DefaultAttributes.BOOTSTRAP_SCRIPTS.getKey(),
        "classpath:org/htmlunit/maven/Bootstrap.js;"
        + "classpath:/rhinoDiff.txt");
    runnerConfig.put(DefaultAttributes.SOURCE_SCRIPTS.getKey(),
        "classpath:org/htmlunit/maven/*.js;"
        + "~classpath:org/htmlunit/maven/*Test.js;"
        + "~classpath:org/htmlunit/maven/Bootstrap.js;"
        + "~classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put(DefaultAttributes.TEST_SCRIPTS.getKey(),
        "classpath:org/htmlunit/maven/*Test.js");
    context.setRunnerConfiguration(runnerConfig);
    context.setTimeout(10);
    context.getWebClientConfiguration().setProperty("javaScriptEnabled",
        String.valueOf(true));
    runner = new JavaScriptTestRunner();
  }

  @Test
  @SuppressWarnings("rawtypes")
  public void configure() {
    assertThat((String) getValue("testRunnerTemplate"),
        is("classpath:/org/htmlunit/maven/DefaultTestRunner.html"));
    assertThat(getValue("testRunnerScript"), is(nullValue()));
    assertThat(((List) getValue("bootstrapScripts")).size(), is(0));
    assertThat(((List) getValue("sourceScripts")).size(), is(0));
    assertThat(((List) getValue("testScripts")).size(), is(0));
    assertThat(getValue("outputDirectory"), is(nullValue()));

    runner.configure(context);

    assertThat((String) getValue("testRunnerTemplate"),
        is("classpath:org/htmlunit/maven/TestRunner.html"));
    assertThat(((URL) getValue("testRunnerScript")).toString()
        .endsWith("org/htmlunit/maven/TestRunner.js"), is(true));
    assertThat(((List) getValue("bootstrapScripts")).size(), is(1));
    assertThat(((URL) ((List) getValue("bootstrapScripts")).get(0))
        .toString().endsWith("org/htmlunit/maven/Bootstrap.js"), is(true));
    assertThat(((List) getValue("sourceScripts")).size(), is(1));
    assertThat(((URL) ((List) getValue("sourceScripts")).get(0))
        .toString().endsWith("org/htmlunit/maven/Widget.js"), is(true));
    assertThat(((List) getValue("testScripts")).size(), is(1));
    assertThat(((URL) ((List) getValue("testScripts")).get(0))
        .toString().endsWith("org/htmlunit/maven/WidgetTest.js"), is(true));
    assertThat((File) getValue("outputDirectory"),
        is(new File(System.getProperty("java.io.tmpdir"))));
  }

  @Test
  public void run() {
    runner.initialize(context);
    runner.run();
    assertThat(runner.getDriver().findElementById("result").getText(),
        is("OK"));
  }

  private Object getValue(final String property) {
    try {
      return ReflectionUtils.getValueIncludingSuperclasses(property, runner);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
