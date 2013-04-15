package org.htmlunit.maven;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/** Tests the {@link RunnerContext} class.
 */
public class RunnerContextTest {

  @Test
  public void construct() {
    RunnerContext context = new RunnerContext();
    assertThat(context.getBrowserVersion(), is(BrowserVersion.FIREFOX_17));
    assertThat(context.getWebClientConfiguration(), is(notNullValue()));
    assertThat(context.getRunnerConfiguration(), is(notNullValue()));
    assertThat(context.getTimeout(), is(-1));
    assertThat(context.getLog(), is(nullValue()));
  }

  @Test
  public void configure() {
    RunnerContext context = new RunnerContext();
    Properties runnerConfig = new Properties();
    Properties webClientConfig = new Properties();
    Log log = new SystemStreamLog();

    context.setBrowserVersion(BrowserVersion.INTERNET_EXPLORER_8);
    context.setRunnerConfiguration(runnerConfig);
    context.setWebClientConfiguration(webClientConfig);
    context.setTimeout(60);
    context.setLog(log);

    assertThat(context.getBrowserVersion(),
        is(BrowserVersion.INTERNET_EXPLORER_8));
    assertThat(context.getWebClientConfiguration(), is(webClientConfig));
    assertThat(context.getRunnerConfiguration(), is(runnerConfig));
    assertThat(context.getTimeout(), is(60));
    assertThat(context.getLog(), is(log));
  }
}
