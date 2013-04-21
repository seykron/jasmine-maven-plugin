package org.htmlunit.maven;

import java.beans.Statement;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;

/** Configures {@link WebClient} from properties.
 */
public class WebClientConfigurer {

  /** Class logger. */
  private static final Logger LOG = LoggerFactory.getLogger(
      WebClientConfigurer.class);

  /** Web client to configure; it's never null.
   */
  private final WebClient client;

  /** Creates a configurer and sets the client to configure.
   *
   * @param theClient Web client to configure. Cannot be null.
   */
  public WebClientConfigurer(final WebClient theClient) {
    Validate.notNull(theClient, "The web client cannot be null.");
    client = theClient;
  }

  /** Configures the web client applying the specified properties.
   *
   * @param configuration Client configuration. Cannot be null.
   */
  @SuppressWarnings("rawtypes")
  public void configure(final Map configuration) {
    for (Object property : configuration.keySet()) {
      try {
        String methodName = "set" + StringUtils.capitalize((String) property);
        Statement stmt = new Statement(getWebClient().getOptions(), methodName,
            new Object[] { configuration.get(property)});
        stmt.execute();
      } catch (Exception cause) {
        LOG.debug("Property " + property + " not found in web client.", cause);
      }
    }
  }

  /** Returns the web client.
   * @return A valid web client. Never returns null.
   */
  public WebClient getWebClient() {
    return client;
  }
}
