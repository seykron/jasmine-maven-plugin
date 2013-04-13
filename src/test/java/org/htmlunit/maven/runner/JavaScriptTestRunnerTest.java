package org.htmlunit.maven.runner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.codehaus.plexus.util.ReflectionUtils;
import org.htmlunit.javascript.EventHandler;
import org.htmlunit.maven.RunnerContext;
import org.htmlunit.maven.runner.JavaScriptTestRunner;
import org.htmlunit.maven.runner.JavaScriptTestRunner.DefaultAttributes;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.javascript.host.Event;

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
    runnerConfig.put(DefaultAttributes.TEST_FILES.getKey(),
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
    assertThat(((List) getValue("testFiles")).size(), is(0));
    assertThat(getValue("outputDirectory"), is(nullValue()));
    context.getRunnerConfiguration().put(
        DefaultAttributes.TEST_RUNNER_TEMPLATE.getKey(),
        "classpath:org/htmlunit/maven/TestRunner.html");

    runner.configure(context);

    assertThat((String) getValue("testRunnerTemplate"),
        is("classpath:org/htmlunit/maven/TestRunner.html"));
    assertThat(((URL) getValue("testRunnerScript")).toString()
        .endsWith("org/htmlunit/maven/TestRunner.js"), is(true));
    assertThat(((List) getValue("bootstrapScripts")).size(), is(1));
    assertThat(((URL) ((List) getValue("bootstrapScripts")).get(0))
        .toString().endsWith("org/htmlunit/maven/Bootstrap.js"), is(true));
    assertThat(((List) getValue("sourceScripts")).size(), is(2));
    assertThat(((URL) ((List) getValue("sourceScripts")).get(0))
        .toString().endsWith("Widget.js"), is(true));
    assertThat(((List) getValue("testFiles")).size(), is(2));
    assertThat(((URL) ((List) getValue("testFiles")).get(0))
        .toString().endsWith("WidgetTest.js"), is(true));
    assertThat((File) getValue("outputDirectory"),
        is(new File(System.getProperty("java.io.tmpdir"))));
  }

  @Test
  @SuppressWarnings("serial")
  public void run() {
    runner.initialize(context);
    runner.addEventListener(Event.TYPE_BEFORE_UNLOAD, new EventHandler() {
      private boolean aboutBlank = true;

      public void handleEvent(final org.w3c.dom.events.Event event) {
        if (!aboutBlank) {
          String result = runner.getDriver().findElementById("result")
              .getText();
          assertThat(result.equals("FOO") || result.equals("BAR"), is(true));
        }
        aboutBlank = false;
      }
    }, false);
    runner.run();
  }

  private Object getValue(final String property) {
    try {
      return ReflectionUtils.getValueIncludingSuperclasses(property, runner);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}
