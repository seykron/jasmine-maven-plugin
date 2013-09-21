package org.htmlunit.maven;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.beans.Statement;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.gargoylesoftware.htmlunit.WebClient;

/** Configures {@link WebClient} from properties.
 */
public class WebClientConfigurer {

  /** Web client to configure; it's never null.
   */
  private final WebClient client;

  /** Creates a configurer and sets the client to configure.
   *
   * @param theClient Web client to configure. Cannot be null.
   */
  public WebClientConfigurer(final WebClient theClient) {
    Validate.notNull(theClient, "The web client cannot be null.");
    client = theClient;
  }

  /** Configures the web client applying the specified properties.
   *
   * @param configuration Client configuration. Cannot be null.
   */
  @SuppressWarnings("rawtypes")
  public void configure(final Map configuration) {
    for (Object property : configuration.keySet()) {
      try {
        String methodName = "set" + StringUtils.capitalize((String) property);
        Object value = convert((String) configuration.get(property));
        Statement stmt = new Statement(getWebClient().getOptions(), methodName,
            new Object[] { value });
        stmt.execute();
      } catch (Exception cause) {
        throw new IllegalArgumentException("Property " + property
            + " cannot be set in web client.", cause);
      }
    }
  }

  /** Returns the web client.
   * @return A valid web client. Never returns null.
   */
  public WebClient getWebClient() {
    return client;
  }

  /** Converts a value from String to its native type depending on the content.
   *
   * @param value Value to cast. Cannot be null.
   *
   * @return Returns the value converted to the proper type.
   */
  private Object convert(final String value) {
    ArrayList<PropertyEditor> types = new ArrayList<PropertyEditor>();
    types.add(new TypeEditor(Boolean.class) {
      @Override
      public boolean canEdit(final Object value) {
        return "true".equals(value) || "false".equals(value);
      }
    });
    types.add(new TypeEditor(Integer.class){
      @Override
      public boolean canEdit(final Object value) {
        return StringUtils.isNumeric(String.valueOf(value));
      }
    });
    types.add(new TypeEditor(Long.class) {
      @Override
      public boolean canEdit(final Object value) {
        return StringUtils.isNumeric(String.valueOf(value));
      }
    });

    for (PropertyEditor editor : types) {
      try {
        editor.setValue(value);
        return editor.getValue();
      } catch (IllegalArgumentException cause) {
        // Ignores exception
      }
    }

    return value;
  }

  /** Property editor to edit multiple typed values. Types must support a
   * <code>valueOf(String)</code> operation.
   */
  private abstract static class TypeEditor extends PropertyEditorSupport {

    /** Type to edit, it's never null. */
    private final Class<?> type;

    /** Creates a type editor and sets the editing type.
     * @param theType Type to edit. Cannot be null.
     */
    public TypeEditor(final Class<?> theType) {
      Validate.notNull(theType, "The type cannot be null.");
      type = theType;
    }

    /** Indicates whether this editor can edit the specified value or not.
     * @param value Value to edit. It's never null.
     * @return Returns true if this editor can edit the value, false otherwise.
     */
    public abstract boolean canEdit(final Object value);

    /** {@inheritDoc}
     */
    @Override
    public Object getValue() {
      Object value = super.getValue();
      try {
        return type.getMethod("valueOf", new Class[] { String.class })
            .invoke(null, new Object[] { value });
      } catch (Exception cause) {
        throw new IllegalArgumentException("Cannot convert value.", cause);
      }
    }

    /** It invokes {@link #canEdit(String)} to determine whether this value
     * can be edited or not.
     *
     * {@inheritDoc}
     */
    @Override
    public void setValue(final Object value) throws IllegalArgumentException {
      if (!canEdit(value)) {
        throw new IllegalArgumentException("Invalid integer value: " + value);
      }
      super.setValue(value);
    }
  }
}
