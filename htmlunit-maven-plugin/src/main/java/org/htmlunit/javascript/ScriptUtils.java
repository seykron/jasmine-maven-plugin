package org.htmlunit.javascript;

import java.net.URL;

import net.sourceforge.htmlunit.corejs.javascript.Function;

import com.gargoylesoftware.htmlunit.javascript.host.Node;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

/** Utility methods for JavaScript scripts.
 */
public final class ScriptUtils {

  /** Cannot be instantiated. */
  private ScriptUtils() {
  }

  /** Adds a DOM event listener to the specified node.
   *
   * @param target Object to add event listener. Cannot be null.
   * @param eventType The event type to listen (like "click"). Cannot be null
   *    or empty.
   * @param handler The event listener. Cannot be null.
   * @param useCapture If <code>true</code>, indicates that the user wishes to
   *    initiate capture.
   */
  public static void addEventListener(final Node target,
      final String eventType, final Function handler,
      final boolean useCapture) {
    target.jsxFunction_addEventListener(eventType, handler, useCapture);
  }


  /** Adds a DOM event listener to the specified node.
   *
   * @param target Object to add event listener. Cannot be null.
   * @param eventType The event type to listen (like "click"). Cannot be null
   *    or empty.
   * @param handler The event listener. Cannot be null.
   * @param useCapture If <code>true</code>, indicates that the user wishes to
   *    initiate capture.
   */
  public static void addEventListener(final Window target,
      final String eventType, final Function handler,
      final boolean useCapture) {
    target.jsxFunction_addEventListener(eventType, handler, useCapture);
  }

  /** Adds a script tag to the specified window.
   * @param window Window to add script. Cannot be null.
   * @param source Source to add. Cannot be null.
   */
  public static void addScript(final Window window, final URL source) {
    StringBuilder script = new StringBuilder();
    script.append("var script = document.createElement('script');");
    script.append("script.setAttribute('type', 'text/javascript');");
    script.append("script.src = '" + source.toString() + "';");
    script.append("document.getElementsByTagName('head')[0]"
        + ".appendChild(script);");
    window.custom_eval(script.toString());
  }
}
