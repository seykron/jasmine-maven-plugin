package org.htmlunit.maven;

import static org.easymock.EasyMock.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.htmlunit.javascript.EventHandler;
import org.htmlunit.maven.AbstractRunner;
import org.htmlunit.maven.RunnerContext;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

/** Tests the {@link AbstractRunner} class.
 * TODO (matias.mirabelli): test connection and web window listeners.
 */
public class AbstractRunnerTest {

  @Test
  public void configuration() {
    final RunnerContext context = createMock(RunnerContext.class);
    AbstractRunner runner = new AbstractRunner() {
      @Override
      protected void configure(final RunnerContext theContext) {
        assertThat(theContext, is(context));
      }
      public void run() {
      }
    };
    expect(context.getBrowserVersion()).andReturn(BrowserVersion.FIREFOX_17);

    Properties clientProps = new Properties();
    clientProps.put("homePage", "http://foo.bar");
    expect(context.getWebClientConfiguration()).andReturn(clientProps);
    expect(context.getTimeout()).andReturn(60);
    replay(context);

    assertThat(runner.getWebClient(), is(nullValue()));
    assertThat(runner.getContext(), is(nullValue()));
    assertThat(runner.getDriver(), is(nullValue()));

    runner.initialize(context);
    assertThat(runner.getWebClient().getOptions().getHomePage(),
        is("http://foo.bar"));
    assertThat(runner.getWebClient().getOptions().isJavaScriptEnabled(),
        is(true));
    assertThat(runner.getWebClient().getAjaxController(),
        instanceOf(NicelyResynchronizingAjaxController.class));
    assertThat(runner.getContext(), is(context));
    assertThat(runner.getDriver(), is(notNullValue()));
    verify(context);
  }

  @SuppressWarnings("serial")
  @Test
  public void addEventListener() {
    AbstractRunner runner = new AbstractRunner() {
      @Override
      protected void configure(final RunnerContext theContext) {
      }
      public void run() {
        getDriver().get("classpath:org/htmlunit/maven/TestRunner.js");
      }
    };
    runner.initialize(new RunnerContext());
    runner.addEventListener(Event.TYPE_DOM_DOCUMENT_LOADED, new EventHandler() {
      @Override
      public void handleEvent(final Event event) {
        assertThat(event.getCurrentTarget(), is(notNullValue()));
        assertThat(event.getCurrentTarget(), is(notNullValue()));
        assertThat(event.getCurrentTarget(), instanceOf(Window.class));
      }
    }, false);
    runner.run();
  }
}
