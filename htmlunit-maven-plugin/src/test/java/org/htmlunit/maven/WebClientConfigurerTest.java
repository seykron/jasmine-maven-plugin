package org.htmlunit.maven;

import static org.easymock.EasyMock.*;

import java.util.Properties;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;

/** Tests the {@link WebClientConfigurer} class.
 */
public class WebClientConfigurerTest {

  @Test
  public void configure() {
    Properties config = new Properties();
    config.put("javaScriptEnabled", true);
    config.put("homePage", "http://www.foo.bar");

    WebClient client = createMock(WebClient.class);
    client.setJavaScriptEnabled(true);
    client.setHomePage("http://www.foo.bar");
    replay(client);

    WebClientConfigurer configurer = new WebClientConfigurer(client);
    configurer.configure(config);

    verify(client);
  }
}
