package org.htmlunit.maven;

import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.apache.maven.plugin.logging.Log;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/** Contains common {@link WebDriverRunner} configuration. By default
 * target browser version is {@link BrowserVersion#FIREFOX_3_6}.
 */
public class RunnerContext {

  /** Htmlunit browser version; it's never null. */
  private BrowserVersion browserVersion = BrowserVersion.FIREFOX_3_6;

  /** Properties to configure htmlunit web client; it's never null. */
  private Properties webClientConfiguration = new Properties();

  /** Properties to configure the runner; it's never null. */
  private Properties runnerConfiguration = new Properties();

  /** Timeout to wait for page loads. */
  private int timeout = -1;

  /** Maven logger. */
  private Log log;

  /** Returns the htmlunit browser version.
   * @return A valid browser version, never returns null.
   */
  public BrowserVersion getBrowserVersion() {
    return browserVersion;
  }

  /** Sets the htmlunit browser version.
   *
   * @param theBrowserVersion Browser version. Cannot be null.
   */
  public void setBrowserVersion(final BrowserVersion theBrowserVersion) {
    Validate.notNull(theBrowserVersion, "The browser version cannot be null.");
    browserVersion = theBrowserVersion;
  }

  /** Returns the HTMLUnit web client configuration.
   * @return A valid set of properties. Never returns null.
   */
  public Properties getWebClientConfiguration() {
    return webClientConfiguration;
  }

  /** Sets the HTMLUnit web client configuration.
   *
   * @param theWebClientConfiguration Web client configuration. Cannot be null.
   */
  public void setWebClientConfiguration(
      final Properties theWebClientConfiguration) {
    Validate.notNull(theWebClientConfiguration,
        "The web client configuration cannot be null.");
    webClientConfiguration = theWebClientConfiguration;
  }

  /** Returns the runner's specific configuration.
   * @return A valid set of properties. Never returns null.
   */
  public Properties getRunnerConfiguration() {
    return runnerConfiguration;
  }

  /** Sets the runner's specific configuration.
   *
   * @param theRunnerConfiguration Runner configuration. Cannot be null.
   */
  public void setRunnerConfiguration(
      final Properties theRunnerConfiguration) {
    Validate.notNull(theRunnerConfiguration,
        "The runner configuration cannot be null.");
    runnerConfiguration = theRunnerConfiguration;
  }

  /** Returns the timeout, in seconds, to wait for page load.
   * @return A number greater than 0, or -1 to wait infinite.
   */
  public int getTimeout() {
    return timeout;
  }

  /** Sets the timeout, in seconds, to wait for page load.
   *
   * @param theTimeout A number greater than 0, or -1 to wait infinite.
   */
  public void setTimeout(final int theTimeout) {
    timeout = theTimeout;
  }

  /** Returns the maven root logger.
   * @return A valid logger, or null if the logger isn't set.
   */
  public Log getLog() {
    return log;
  }

  /** Sets the maven root logger.
   *
   * @param theLog Maven's root logger. Cannot be null.
   */
  public void setLog(final Log theLog) {
    Validate.notNull(theLog, "The logger cannot be null.");
    log = theLog;
  }
}
