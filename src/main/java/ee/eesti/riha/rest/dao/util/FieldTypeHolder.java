package ee.eesti.riha.rest.dao.util;

import javax.persistence.Column;
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

  private final String columnNameFromAnnotation;

  private final Class<?> type;

  /**
   * Instantiates a new field type holder.
   *
   * @param value the value
   * @param type the type
   */
  private FieldTypeHolder(Object value, Class<?> type, String columnNameFromAnnotation) {
    this.value = value;
    this.type = type;
    this.columnNameFromAnnotation = columnNameFromAnnotation;
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

    String columnNameFromAnnotation = null;
    if(field.isAnnotationPresent(Column.class)) {
      Column annotation = field.getAnnotation(Column.class);
      columnNameFromAnnotation = annotation.name();
    }
    return new FieldTypeHolder(field.get(obj), field.getType(), columnNameFromAnnotation);

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

    String columnNameFromAnnotation = null;
    if(field.isAnnotationPresent(Column.class)) {
      Column annotation = field.getAnnotation(Column.class);
      columnNameFromAnnotation = annotation.name();
    }
    return new FieldTypeHolder(fieldName, field.getType(), columnNameFromAnnotation);
  }

  public String getDatabaseColumnName() {

    return columnNameFromAnnotation != null
            ? columnNameFromAnnotation
            : (String) value;
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
