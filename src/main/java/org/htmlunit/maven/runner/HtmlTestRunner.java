package org.htmlunit.maven.runner;

import java.net.URL;

import org.antlr.stringtemplate.StringTemplate;
import org.htmlunit.maven.ResourceUtils;

/** Runs tests from HTML files.
 */
public class HtmlTestRunner extends JavaScriptTestRunner {

  /** Considers the test file as HTML resource and loads the mark-up into the
   * template.
   * <p>{@inheritDoc}</p>
   */
  @Override
  protected void loadTest(final StringTemplate runnerTemplate,
      final URL test) {
    String htmlTest = ResourceUtils.readAsText(test);
    runnerTemplate.setAttribute(DefaultAttributes.TEST_FILES.getKey(),
        htmlTest);
  }
}
