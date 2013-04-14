package org.htmlunit.javascript;

import org.w3c.dom.events.EventListener;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Function;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

/** Represents a JavaScript event handler. It can be used to pass around as
 * event listener.
 */
public abstract class EventHandler extends ScriptableObject
    implements Function, EventListener {

  /** Default id for serialization.
   */
  private static final long serialVersionUID = 1L;

  /** Delegates the JavaScript function call to the internal callback.
   *
   * <p>{@inheritDoc}</p>
   */
  public Object call(final Context cx, final Scriptable scope,
      final Scriptable thisObj, final Object[] args) {
    if (args.length == 1
        && com.gargoylesoftware.htmlunit.javascript.host
          .Event.class.isInstance(args[0])) {
      handleEvent(new EventAdapter(
          (com.gargoylesoftware.htmlunit.javascript.host.Event) args[0]));
    }
    return null;
  }

  /** {@inheritDoc}
   */
  public Scriptable construct(final Context cx, final Scriptable scope,
      final Object[] args) {
    throw new UnsupportedOperationException(
        "Cannot instantiate event handlers.");
  }

  /** {@inheritDoc}
   */
  @Override
  public String getClassName() {
    return getClass().getName();
  }
}
