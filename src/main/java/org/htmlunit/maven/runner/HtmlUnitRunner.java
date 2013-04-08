package org.htmlunit.maven.runner;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.RunnerContext;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.WebClient;

/** Runner that loads plain HTML files into a {@link WebClient}.
 * {@link WebClient} configured by this runner supports loading resources
 * from the classpath using "classpath:" as protocol.
 */
public class HtmlUnitRunner extends AbstractRunner {

  /** Property that contains the list of documents. */
  private static final String DOCUMENTS = "documents";

  /** List of documents to load; it's never empty after initialize(). */
  private List<String> documents;

  /** {@inheritDoc}
   */
  @Override
  protected void configure(final RunnerContext theConfiguration) {
    super.initialize(theConfiguration);

    Properties runnerConfig = theConfiguration.getRunnerConfiguration();

    Validate.isTrue(runnerConfig.contains(DOCUMENTS),
        "Documents property is required.");
    documents = Arrays.asList(((String) runnerConfig.get(DOCUMENTS))
        .split(";"));

    Validate.isTrue(documents.isEmpty(),
        "There're no documents to load in the web client.");
  }

  /** {@inheritDoc}
   */
  public void run() {
    Validate.notNull(getDriver(), "The web driver is not initialized.");

    for (String document : documents) {
      getDriver().get(document);
      new WebDriverWait(getDriver(), getContext().getTimeout(), 1000);
    }
  }
}
