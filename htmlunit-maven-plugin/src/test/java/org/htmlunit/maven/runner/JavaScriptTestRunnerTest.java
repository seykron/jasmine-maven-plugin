package org.htmlunit.maven.runner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.net.URL;
import java.util.Properties;

import org.htmlunit.maven.RunnerContext;
import org.htmlunit.maven.runner.JavaScriptTestRunner;
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
    runnerConfig.put("outputDirectory", System.getProperty("java.io.tmpdir"));
    runnerConfig.put("testRunnerScript",
        "classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put("bootstrapScripts",
        "classpath:org/htmlunit/maven/Bootstrap.js;"
        + "http://code.jquery.com/jquery-1.9.1.js");
    runnerConfig.put("sourceScripts",
        "classpath:org/htmlunit/maven/*.js;"
        + "~classpath:org/htmlunit/maven/*Test.js;"
        + "~classpath:org/htmlunit/maven/Bootstrap.js;"
        + "~classpath:org/htmlunit/maven/TestRunner.js");
    runnerConfig.put("testFiles",
        "classpath:org/htmlunit/maven/*Test.js");
    runnerConfig.put("PROP_FOO", "FOO");
    runnerConfig.put("PROP_BAR", "BAR");

    context.setRunnerConfiguration(runnerConfig);
    context.setTimeout(10);
    context.getWebClientConfiguration().setProperty("javaScriptEnabled",
        String.valueOf(true));
    context.getWebClientConfiguration()
      .setProperty("throwExceptionOnScriptError", String.valueOf(true));
    runner = new JavaScriptTestRunner();
  }

  @Test
  public void run() {
    runner = new JavaScriptTestRunner() {
      @Override
      protected void testFinished(final URL test) {
        String result = runner.getDriver().findElementById("main").getText();

        if (test.getFile().endsWith("FooWidgetTest.js")) {
          assertThat(result, is("FOO"));
        } else if (test.getFile().endsWith("BarWidgetTest.js")) {
          assertThat(result, is("BAR"));
        }
      }
    };
    runner.initialize(context);
    runner.run();
  }
}
