package org.htmlunit.maven;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.Validate;

/** Matches a set of resources described by an {@link AntExpression}.
 */
public abstract class ResourceScanner {

  /** Convenience factory method to create a suitable resource scanner for the
   * specified expression. The resource must exist.
   *
   * @param resourcePattern Resource expression to scan. Cannot be null or
   *    empty.
   * @return Returns a valid scanner. Never returns null.
   */
  public static ResourceScanner create(final AntExpression expression) {

    if ("classpath".equals(expression.getProtocol())
        && ResourceUtils.isJarResource(expression.getRootDir())) {
      // Classpath resource located into a JAR file.
      return new ClassPathScanner(expression);
    } else {
      if ("classpath".equals(expression.getProtocol())) {
        // Classpath resource located in the file system.
        String rootDir = expression.getRootDir();
        if (rootDir.startsWith("/")) {
          rootDir = rootDir.substring(1);
        }

        URL url = Thread.currentThread().getContextClassLoader()
            .getResource(rootDir);

        Validate.notNull(url, "Resource not found.");

        return new FileSystemScanner(new File(url.getFile()), expression);
      } else {
        // Regular file system resource.
        return new FileSystemScanner(expression);
      }
    }
  }

  /** List resources matching the current expression.
   * @return Returns a list of valid resources. Never returns null.
   */
  public abstract List<URL> list();

  /** Expression to match; it's never null.  */
  private final AntExpression expression;

  /** Creates a new resource scanner to match the specified expression.
   *
   * @param theExpression Expression to match. Cannot be null.
   */
  public ResourceScanner(final AntExpression theExpression) {
    Validate.notNull(theExpression, "The expression cannot be null.");
    expression = theExpression;
  }

  /** Returns the expression to match.
   * @return Returns a valid expression. Never returns null.
   */
  public AntExpression getExpression() {
    return expression;
  }
}
