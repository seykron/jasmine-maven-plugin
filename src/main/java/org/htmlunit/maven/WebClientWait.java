package org.htmlunit.maven;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.openqa.selenium.support.ui.FluentWait;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.google.common.base.Predicate;

/** Waits until all windows in a {@link WebClient} are closed.
 */
public class WebClientWait extends FluentWait<WebClient> {

  /** Web client to wait for; it's never null. */
  private final WebClient client;

  /** Since HtmlUnitWebDriver doesn't implement window handles, there's no
   * way to map the window being closed with a window in web driver, so
   * this map keep track of windows opened in the web driver. */
  private List<WebWindow> windows = new ArrayList<WebWindow>();

  /** Creates a new wait object for the specified client.
   * @param theClient Client to wait for. Cannot be null.
   */
  public WebClientWait(final WebClient theClient) {
    super(theClient);

    Validate.notNull(theClient, "The web client cannot be null.");
    client = theClient;
    initialize(theClient);
  }

  /** Starts waiting until all windows in the web client are closed.
   */
  public void start() {
    until(new Predicate<WebClient>() {
      public boolean apply(final WebClient input) {
        return isDone();
      }
    });
  }

  /** Returns the web client to wait for.
   * @return A valid web client, never returns null.
   */
  public WebClient getWebClient() {
    return client;
  }

  /** Indicates whether this wait finished or not.
   * @return True if finished, false otherwise.
   */
  boolean isDone() {
    return windows.size() == 0;
  }

  /** Initializes the web client to wait for, registering opened windows and
   * listening for new windows.
   * @param theClient Web client to wait. Cannot be null.
   */
  private void initialize(final WebClient theClient) {
    theClient.addWebWindowListener(new WebWindowListener() {
      /** {@inheritDoc}
       */
      public void webWindowOpened(final WebWindowEvent event) {
      }

      /** {@inheritDoc}
       */
      public void webWindowContentChanged(final WebWindowEvent event) {
        WebWindow webWindow = event.getWebWindow();

        if (!windows.contains(webWindow)
            && webWindow.getScriptObject() != null) {
          windows.add(webWindow);
        }
      }

      public void webWindowClosed(final WebWindowEvent event) {
        windows.remove(event.getWebWindow());
      }
    });
    for (WebWindow webWindow : theClient.getWebWindows()) {
      if (!windows.contains(webWindow)
          && webWindow.getScriptObject() != null) {
        windows.add(webWindow);
      }
    }
  }
}
