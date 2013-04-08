package org.htmlunit.protocol.classpath;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.htmlunit.protocol.classpath.Handler;
import org.junit.Test;


/** Tests the {@link Handler} class.
 */
public class HandlerTest {

  @Test
  public void readUrl() throws Exception {
    String classPathHandler;
    classPathHandler = StringUtils.substringBeforeLast(
        Handler.class.getPackage().getName(), ".");

    System.setProperty("java.protocol.handler.pkgs", classPathHandler);
    URL url = new URL("classpath:/org/htmlunit/maven/TestRunner.html");
    InputStream input = url.openStream();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    IOUtils.copy(input, output);
    assertThat(output.toString().contains("$testRunnerScript$"), is(true));
  }
}
