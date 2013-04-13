package org.htmlunit.maven.runner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.net.URL;
import java.util.Properties;

import org.htmlunit.maven.RunnerContext;
import org.htmlunit.maven.runner.JavaScriptTestRunner.DefaultAttributes;
import org.junit.Before;
import org.junit.Test;

/** Tests the {@link HtmlTestRunner} class.
 */
public class HtmlTestRunnerTest {
  private RunnerContext context;
  private HtmlTestRunner runner;

  @Before
  public void setUp() {
    context = new RunnerContext();
    Properties runnerConfig = new Properties();
    runnerConfig.put(DefaultAttributes.OUTPUT_DIR.getKey(),
        System.getProperty("java.io.tmpdir"));
    runnerConfig.put(DefaultAttributes.TEST_FILES.getKey(),
        "classpath:org/htmlunit/maven/*Test.html");

    context.setRunnerConfiguration(runnerConfig);
    context.setTimeout(10);
    context.getWebClientConfiguration().setProperty("javaScriptEnabled",
        String.valueOf(true));
  }

  @Test
  public void run() {
    runner = new HtmlTestRunner() {
      @Override
      protected void testFinished(final URL test) {
        String result = runner.getDriver().findElementById("main").getText();

        if (test.getFile().endsWith("FirstTest.html")) {
          assertThat(result, is("Joe"));
        } else if (test.getFile().endsWith("SecondTest.html")) {
          assertThat(result, is("Moe"));
        }
      }
    };
    runner.initialize(context);
    runner.run();
  }
}
