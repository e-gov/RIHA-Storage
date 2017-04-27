package ee.eesti.riha.rest.dao.util;

import java.lang.reflect.Field;

// TODO: Auto-generated Javadoc
/**
 * Purpose of this class: to pass to DAO processing some info on the field, more precisely what type the field is (is it
 * string?, is it numeric?, etc). For example: when received fieldA=1 via url, we need to indicate that fieldA values
 * are numeric not string.
 *
 * @param <T> the generic type
 */
public final class FieldTypeHolder<T> {

  private final Object value;

  private final Class<?> type;

  /**
   * Instantiates a new field type holder.
   *
   * @param value the value
   * @param type the type
   */
  private FieldTypeHolder(Object value, Class<?> type) {
    this.value = value;
    this.type = type;
  }

  /**
   * Construct.
   *
   * @param obj the obj
   * @param fieldName the field name
   * @return the field type holder
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   */
  public static FieldTypeHolder construct(Object obj, String fieldName) throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException {

    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return new FieldTypeHolder(field.get(obj), field.getType());

  }

  /**
   * Construct.
   *
   * @param clazz the clazz
   * @param fieldName the field name
   * @return the field type holder
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   */
  public static FieldTypeHolder construct(Class clazz, String fieldName) throws NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException {

    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);
    return new FieldTypeHolder(new String(fieldName), field.getType());

  }

  /**
   * Gets the value.
   *
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public Class<?> getType() {
    return type;
  }

}
