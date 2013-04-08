package org.htmlunit.javascript;

import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;

import org.apache.commons.lang.Validate;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import com.gargoylesoftware.htmlunit.javascript.host.Node;
import com.gargoylesoftware.htmlunit.javascript.host.Window;

/** Adapts HtmlUnit elements to support DOM native events.
 */
public class EventTargetAdapter extends ScriptableObject
  implements EventTarget {

  /** Id for serialization. */
  private static final long serialVersionUID = -4697753799242020763L;

  /** HtmlUnit target object; it's never null. */
  private final Scriptable target;

  /** Creates a new HtmlUnit element adapter.
   * @param theTarget Original HtmlUnit element. Cannot be null.
   */
  public EventTargetAdapter(final Scriptable theTarget) {
    Validate.notNull(theTarget, "The target object cannot be null.");
    target = theTarget;
  }

  /** Returns the original target.
   * @return A valid scriptable; never returns null.
   */
  public Scriptable getOriginalTarget() {
    return target;
  }

  /** {@inheritDoc}
   */
  @SuppressWarnings("serial")
  public void addEventListener(final String eventType,
      final EventListener handler, final boolean useCapture) {

    EventHandler handlerAdapter = new EventHandler() {
      @Override
      public void handle(final Event event) {
        handler.handleEvent(event);
      }
    };

    if (Node.class.isInstance(target)) {
      ScriptUtils.addEventListener((Node) target, eventType, handlerAdapter,
          useCapture);
    }
    if (Window.class.isInstance(target)) {
      ScriptUtils.addEventListener((Window) target, eventType, handlerAdapter,
          useCapture);
    }

    throw new UnsupportedOperationException(
        "Target object doesn't support events.");
  }

  /** {@inheritDoc}
   */
  public boolean dispatchEvent(final Event event) throws EventException {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  /** {@inheritDoc}
   */
  public void removeEventListener(final String eventType,
      final EventListener handler, final boolean useCapture) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  /** {@inheritDoc}
   */
  @Override
  public String getClassName() {
    return target.getClassName();
  }

  /** {@inheritDoc}
   */
  @Override
  public Object get(final int index, final Scriptable start) {
    return target.get(index, start);
  }

  /** {@inheritDoc}
   */
  @Override
  public void put(final int index, final Scriptable start, final Object value) {
    target.put(index, start, value);
  }


  /** {@inheritDoc}
   */
  @Override
  public Object get(final String name, final Scriptable start) {
    return target.get(name, start);
  }

  /** {@inheritDoc}
   */
  @Override
  public boolean has(final String name, final Scriptable start) {
    return target.has(name, start);
  }

  /** {@inheritDoc}
   */
  @Override
  public boolean has(final int index, final Scriptable start) {
    return target.has(index, start);
  }

  /** {@inheritDoc}
   */
  @Override
  public void put(final String name, final Scriptable start, final Object value) {
    target.put(name, start, value);
  }

  /** {@inheritDoc}
   */
  @Override
  public void delete(final String name) {
    target.delete(name);
  }

  /** {@inheritDoc}
   */
  @Override
  public void delete(final int index) {
    target.delete(index);
  }

  /** {@inheritDoc}
   */
  @Override
  public Scriptable getPrototype() {
    return target.getPrototype();
  }

  /** {@inheritDoc}
   */
  @Override
  public void setPrototype(final Scriptable prototype) {
    target.setPrototype(prototype);
  }

  /** {@inheritDoc}
   */
  @Override
  public Scriptable getParentScope() {
    return target.getParentScope();
  }

  /** {@inheritDoc}
   */
  @Override
  public void setParentScope(final Scriptable parent) {
    target.setParentScope(parent);
  }

  /** {@inheritDoc}
   */
  @Override
  public Object[] getIds() {
    return target.getIds();
  }

  /** {@inheritDoc}
   */
  @Override
  public Object getDefaultValue(final Class<?> hint) {
    return target.getDefaultValue(hint);
  }

  /** {@inheritDoc}
   */
  @Override
  public boolean hasInstance(final Scriptable instance) {
    return target.hasInstance(instance);
  }
}
