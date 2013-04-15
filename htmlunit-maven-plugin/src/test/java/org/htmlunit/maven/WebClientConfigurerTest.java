package org.htmlunit.maven;

import static org.easymock.EasyMock.*;

import java.util.Properties;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;

/** Tests the {@link WebClientConfigurer} class.
 */
public class WebClientConfigurerTest {

  @Test
  public void configure() {
    Properties config = new Properties();
    config.put("javaScriptEnabled", true);
    config.put("homePage", "http://www.foo.bar");

    WebClientOptions options = createMock(WebClientOptions.class);
    options.setJavaScriptEnabled(true);
    options.setHomePage("http://www.foo.bar");

    WebClient client = createMock(WebClient.class);
    expect(client.getOptions()).andReturn(options).anyTimes();
    replay(client, options);

    WebClientConfigurer configurer = new WebClientConfigurer(client);
    configurer.configure(config);

    verify(client);
  }
}
