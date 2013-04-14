package org.htmlunit.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.lang.Validate;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.htmlunit.javascript.ScriptUtils;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebConnection;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

/** Support for runner. It initializes configuration and provides utility
 * methods.
 */
public abstract class AbstractRunner implements WebDriverRunner {

  /** Class logger. */
  private static final Logger LOG = LoggerFactory
      .getLogger(AbstractRunner.class);

  /** Runner configuration; it's valid only after initialize(). */
  private RunnerContext context;

  /** Web driver to load pages; it's never null after initialize(). */
  private HtmlUnitDriver driver;

  /** Current client; it's never null after initialize(). */
  private WebClient client;

  /** List of registered events. */
  private List<EventDefinition> eventDefinitions =
      new ArrayList<EventDefinition>();

  /** Object to wait for web driver processing; it's never null after
   * initialize(). */
  private WebClientWait wait;

  /** Configures the runner. It's invoked during the runner initialization.
   * @param context Context to configure the runner. Cannot be null.
   */
  protected abstract void configure(final RunnerContext context);

  /** {@inheritDoc}
   */
  public void initialize(final RunnerContext theContext) {
    Validate.notNull(theContext, "The context cannot be null.");

    context = theContext;
    driver = new HtmlUnitDriver(context.getBrowserVersion()) {
      @Override
      protected WebClient newWebClient(final BrowserVersion version) {
        client = new WebClient(version);
        return client;
      }

      @Override
      protected WebClient modifyWebClient(final WebClient theClient) {
        client.setWebConnection(createConnectionWrapper(client));
        configureWebClient(client);
        return client;
      };
    };
    int timeout = context.getTimeout();
    boolean throwException = client.isThrowExceptionOnScriptError();
    wait = new WebClientWait(client);
    wait.setThrowJavaScriptException(throwException)
      .pollingEvery(1000, TimeUnit.MILLISECONDS);
    if (timeout > -1) {
      // -1 means INFINITE, no timeout.
      wait.withTimeout(timeout, TimeUnit.SECONDS);
    }
    configure(context);
  }

  /** Adds an event listener to the current window, if any. The event will be
   * added to every new window.
   * <p>
   * Throws an exception if the runner isn't initialized.
   * </p>
   * @param eventType Event to listen (like "click").
   * @param handler Event listener. Cannot be null.
   * @param useCapture If true, uses event capture phase.
   */
  public void addEventListener(final String eventType,
      final Function handler, final boolean useCapture) {
    Validate.notNull(client, "The web client is not initialized.");

    if (client.getCurrentWindow() != null
        && client.getCurrentWindow().getScriptObject() != null) {
      Window window = (Window) client.getCurrentWindow().getScriptObject();
      ScriptUtils.addEventListener(window, eventType, handler, useCapture);
    }
    eventDefinitions.add(new EventDefinition(eventType, handler, useCapture));
  }

  /** Returns the runner configuration.
   * @return Returns the current configuration, or null if this runner isn't
   *    yet initialized.
   */
  public RunnerContext getContext() {
    return context;
  }

  /** Returns the htmlunit web driver.
   * @return Returns the driver, or null if the runner isn't yet initialized.
   */
  public HtmlUnitDriver getDriver() {
    return driver;
  }

  /** Returns the configured web client.
   * @return A valid web client, or null if initialize() was not executed.
   */
  public WebClient getWebClient() {
    if (client != null) {
      return client;
    }
    return null;
  }

  /** {@inheritDoc}
   */
  public String getName() {
    return getClass().getName();
  }

  /** Allows to modify web client just after creation. By default it applies
   * all properties specified in the runner configuration.
   *
   * @param client Client to modify. Cannot be null.
   */
  protected void configureWebClient(final WebClient client) {
    WebClientConfigurer configurer = new WebClientConfigurer(client);
    configurer.configure(context.getWebClientConfiguration());
    client.setAjaxController(new NicelyResynchronizingAjaxController());
    client.setIncorrectnessListener(new IncorrectnessListener() {
      public void notify(final String message, final Object origin) {
        LOG.trace(message, origin);
      }
    });
    client.setJavaScriptEnabled(true);
    client.addWebWindowListener(new WebWindowListener() {

      /** {@inheritDoc}
       */
      public void webWindowOpened(final WebWindowEvent event) {
      }

      /** Adds registered event listeners to the window.
       * {@inheritDoc}
       */
      public void webWindowContentChanged(final WebWindowEvent event) {
        Window window = (Window) event.getWebWindow().getScriptObject();
        registerEventListeners(window);
        publishConfiguration(window);
      }

      /**
       * {@inheritDoc}
       */
      public void webWindowClosed(final WebWindowEvent event) {
      }
    });
  }

  /** Waits until all windows, even those opened in JavaScript, are closed.
   */
  protected void waitCompletion() {
    wait.start();
  }

  /** Creates a web connection that supports to load resources from the
   * classpath.
   *
   * @param client Client to wrap connection. Cannot be null.
   * @return A wrapped web connection, never returns null.
   */
  private WebConnection createConnectionWrapper(final WebClient client) {
    return new WebConnectionWrapper(client) {
      @Override
      public WebResponse getResponse(final WebRequest request)
          throws IOException {
        String protocol = request.getUrl().getProtocol();

        // Default web response is retrieved using commons HttpClient.
        // It assumes HttpClient created internally by HtmlUnit is using the
        // default registry. We check for supported schemes by the default
        // registry.
        boolean canHandle = SchemeRegistryFactory.createDefault()
            .get(protocol) != null;
        if (!canHandle) {
          // For unsupported schemes, it tries to read the response using
          // native URL connection.
          String data = ResourceUtils.readAsText(request.getUrl());
          return new StringWebResponse(data, request.getUrl());
        }
        return super.getResponse(request);
      }
    };
  }

  /** Adds all registered event listeners to the specified window.
   * @param window Window to add event listeners. Cannot be null.
   */
  private void registerEventListeners(final Window window) {
    for (EventDefinition definition : eventDefinitions) {
      ScriptUtils.addEventListener(window, definition.getEventType(),
          definition.getHandler(), definition.isUseCapture());
    }
  }

  /** Publishes runner configuration properties to JavaScript's global scope.
   *
   * @param window Window to expose configuration. Cannot be null.
   */
  private void publishConfiguration(final Window window) {
    Properties config = getContext().getRunnerConfiguration();

    for (Object key : config.keySet()) {
      window.defineProperty((String) key, config.get(key),
          ScriptableObject.READONLY);
    }
  }

  /** Event definition to allow event enqueue.
   */
  private static class EventDefinition {
    /** Event to listen; it's never null. */
    private final String eventType;

    /** Event listener; it's never null. */
    private final Function handler;

    /** True to use capture event phase. */
    private final boolean useCapture;

    /** Creates a new event definition.
     *
     * @param eventType Event to listen (like "click").
     * @param handler Event listener. Cannot be null.
     * @param useCapture If true, uses event capture phase.
     */
    public EventDefinition(final String theEventType, final Function theHandler,
        final boolean isUseCapture) {
      eventType = theEventType;
      handler = theHandler;
      useCapture = isUseCapture;
    }

    /** Returns the event type.
     * @return A valid DOM event type. Never returns null.
     */
    public String getEventType() {
      return eventType;
    }

    /** Returns the event listener.
     * @return A valid JavaScript function. Never returns null.
     */
    public Function getHandler() {
      return handler;
    }

    /** Determines whether to use the capture phase or not.
     * @return true to use the capture phase, false otherwise.
     */
    public boolean isUseCapture() {
      return useCapture;
    }
  }
}
