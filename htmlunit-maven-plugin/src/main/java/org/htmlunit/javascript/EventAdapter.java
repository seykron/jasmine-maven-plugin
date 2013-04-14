package org.htmlunit.javascript;

import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

import org.apache.commons.lang.Validate;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

/** Adapts HtmlUnit events to DOM events.
 * <p>
 * Current target and target are actually unsupported. In order to access the
 * target object use {@link EventAdapter#getOriginalCurrentTarget()} and
 * {@link EventAdapter#getOriginalTarget()} instead.
 * </p>
 */
public class EventAdapter implements Event {

  /** Original HtmlUnit event. */
  private final com.gargoylesoftware.htmlunit.javascript.host.Event original;

  /** Event timestamp; it's greater than 0. */
  private final long timestamp;

  /** Event target element; it's never null. */
  private final EventTargetAdapter target;

  /** Current target element; it's never null. */
  private final EventTargetAdapter currentTarget;

  /** Creates a DOM event and delegates to the specified HtmlUnit event.
   * @param theOriginal Event to delegate to. Cannot be null.
   */
  public EventAdapter(
      final com.gargoylesoftware.htmlunit.javascript.host.Event theOriginal) {
    Validate.notNull(theOriginal, "The event cannot be null.");

    original = theOriginal;
    timestamp = System.currentTimeMillis();
    target = new EventTargetAdapter((Scriptable) original.jsxGet_target());

    if (original.jsxGet_currentTarget() != null) {
      currentTarget = new EventTargetAdapter((Scriptable) original
          .jsxGet_currentTarget());
    } else {
      currentTarget = new EventTargetAdapter((Scriptable) original
          .jsxGet_srcElement());
    }
  }

  /** {@inheritDoc}
   */
  public boolean getBubbles() {
    return original.jsxGet_bubbles();
  }

  /** {@inheritDoc}
   */
  public boolean getCancelable() {
    return original.jsxGet_cancelable();
  }

  /** {@inheritDoc}
   */
  public EventTarget getCurrentTarget() {
    return currentTarget;
  }

  /** {@inheritDoc}
   */
  public short getEventPhase() {
    return (short) original.jsxGet_eventPhase();
  }

  /** {@inheritDoc}
   */
  public EventTarget getTarget() {
    return target;
  }


  /** {@inheritDoc}
   */
  public long getTimeStamp() {
    return timestamp;
  }

  /** {@inheritDoc}
   */
  public String getType() {
    return original.jsxGet_type();
  }

  /** {@inheritDoc}
   */
  public void initEvent(final String eventType, final boolean canBubble,
      final boolean cancelable) {
    original.jsxFunction_initEvent(eventType, canBubble, cancelable);
  }

  /** {@inheritDoc}
   */
  public void preventDefault() {
    original.jsxFunction_preventDefault();
  }

  /** {@inheritDoc}
   */
  public void stopPropagation() {
    original.jsxFunction_stopPropagation();
  }
}
