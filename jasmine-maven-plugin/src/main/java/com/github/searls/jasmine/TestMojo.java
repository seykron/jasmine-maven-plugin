package com.github.searls.jasmine;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;

import com.github.searls.jasmine.io.scripts.TargetDirScriptResolver;
import com.github.searls.jasmine.runner.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.htmlunit.maven.ClassLoaderBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.github.searls.jasmine.format.JasmineResultLogger;
import com.github.searls.jasmine.model.JasmineResult;


/**
 * @component
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 */
public class TestMojo extends AbstractJasmineMojo {
  /**
   * @component
   */
  private ArtifactResolver artifactResolver;

  /**
   * Used to build the list of artifacts from the project's dependencies.
   *
   * @component
   */
  private ArtifactFactory artifactFactory;

  /**
   * Provides some metadata operations, like querying the remote repository for
   * a list of versions available for an artifact.
   *
   * @component
   */
  private ArtifactMetadataSource metadataSource;

  /**
   * Specifies the repository used for artifact handling.
   *
   * @parameter expression="${localRepository}"
   */
  private ArtifactRepository localRepository;

  /** The Maven project object, used to generate a classloader to access the
   * classpath resources from the project.
   *
   * Injected by maven. This is never null.
   *
   * @parameter expression="${project}" @readonly
   */
  private MavenProject project;

  @Override
  public void run() throws Exception {
    if(!skipTests) {
      getLog().info("Executing Jasmine Specs");
      File runnerFile = writeSpecRunnerToOutputDirectory();
      JasmineResult result = executeSpecs(runnerFile);
      logResults(result);
      throwAnySpecFailures(result);
    } else {
      getLog().info("Skipping Jasmine Specs");
    }
  }

  private File writeSpecRunnerToOutputDirectory() throws IOException {

    SpecRunnerHtmlGenerator generator = new SpecRunnerHtmlGeneratorFactory().create(ReporterType.JsApiReporter, this, new TargetDirScriptResolver(this));

    String html = generator.generate();

    getLog().debug("Writing out Spec Runner HTML " + html + " to directory " + jasmineTargetDir);
    File runnerFile = new File(jasmineTargetDir,specRunnerHtmlFileName);
    FileUtils.writeStringToFile(runnerFile, html);
    return runnerFile;
  }

  private JasmineResult executeSpecs(final File runnerFile) throws MalformedURLException {
    WebDriver driver = createDriver();
    JasmineResult result = new SpecRunnerExecutor().execute(
      runnerFile.toURI().toURL(),
      new File(jasmineTargetDir,junitXmlReportFileName),
      driver,
      timeout, debug, getLog(), format);
    return result;
  }

  @SuppressWarnings("unchecked")
  private WebDriver createDriver() {
    if (!HtmlUnitDriver.class.getName().equals(webDriverClassName)) {
      try {
        ClassLoader classLoader = createDependenciesClassLoader();
        Class<? extends WebDriver> klass;
        klass = (Class<? extends WebDriver>) classLoader
            .loadClass(webDriverClassName);
        Constructor<? extends WebDriver> ctor = klass.getConstructor();
        Thread.currentThread().setContextClassLoader(classLoader);
        return ctor.newInstance();
      } catch (Exception e) {
        throw new RuntimeException("Couldn't instantiate webDriverClassName", e);
      }
    }

    // We have extra configuration to do to the HtmlUnitDriver
    BrowserVersion htmlUnitBrowserVersion;
    try {
      htmlUnitBrowserVersion = (BrowserVersion) BrowserVersion.class.getField(browserVersion).get(BrowserVersion.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    HtmlUnitDriver driver = new HtmlUnitDriver(htmlUnitBrowserVersion) {
      @Override
      protected WebClient modifyWebClient(final WebClient client) {
        client.setAjaxController(new NicelyResynchronizingAjaxController());

        //Disables stuff like this "com.gargoylesoftware.htmlunit.IncorrectnessListenerImpl notify WARNING: Obsolete content type encountered: 'text/javascript'."
        if (!debug)
          client.setIncorrectnessListener(new IncorrectnessListener() {
                public void notify(final String arg0, final Object arg1) {}
        });

        return client;
      };
    };
    driver.setJavascriptEnabled(true);
    return driver;
  }


  private void logResults(final JasmineResult result) {
    JasmineResultLogger resultLogger = new JasmineResultLogger();
    resultLogger.setLog(getLog());
    resultLogger.log(result);
  }

  private void throwAnySpecFailures(final JasmineResult result) throws MojoFailureException {
    if(haltOnFailure && !result.didPass()) {
      throw new MojoFailureException("There were Jasmine spec failures.");
    }
  }

  /**
   * Creates a {@link ClassLoader} which contains all the project's
   * dependencies.
   *
   * @return Returns the created {@link ClassLoader} containing all the
   *    project's dependencies.
   */
  private ClassLoader createDependenciesClassLoader() {

    ClassLoaderBuilder builder = new ClassLoaderBuilder(artifactResolver,
        metadataSource, localRepository, project);
    return builder.includeDependencies(true)
      .includeTestDependencies(false)
      .setParent(Thread.currentThread().getContextClassLoader())
      .create();
  }
}