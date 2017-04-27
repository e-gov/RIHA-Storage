package ee.eesti.riha.rest.logic.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.util.DisallowUseMethodForUpdate;

// TODO: Auto-generated Javadoc
/**
 * The Class JsonFieldHelper.
 */
public final class JsonFieldHelper {

  // fields by class, that must not be updated
  static Map<Class<?>, Map<String, Object>> nonUpdatableFieldsMap = new HashMap<>();

  private static final String SET = "set";
  private static final int SET_LENGTH = SET.length();

  private static final Logger LOG = LoggerFactory.getLogger(JsonFieldHelper.class);

  /**
   * Instantiates a new json field helper.
   */
  private JsonFieldHelper() {

  }

  static {
    for (Map.Entry<String, Class<?>> entry : Finals.getTableClassMap().entrySet()) {
      findNonUpdatableFields(entry.getValue());
    }
    if (nonUpdatableFieldsMap.size() != Finals.getTableClassMap().size()) {
      throw new RuntimeException("MAP SIZES MUST BE EQUAL");
    }
  }

  // public static Map<String, Object> getNonUpdatableFieldsByTable(String tableName) {
  // Class<?> clazz = Finals.getClassRepresentingTable(tableName);
  //
  // return nonUpdatableFieldsMap.get(clazz);
  // }

  private static <T> void findNonUpdatableFields(Class<T> clazz) {
    nonUpdatableFieldsMap.put(clazz, new HashMap<String, Object>());
    Method[] methods = clazz.getDeclaredMethods();
    for (Method m : methods) {
      String fieldName = fieldNameFromSetter(m.getName());

      if (m.getAnnotation(DisallowUseMethodForUpdate.class) != null && fieldName != null) {
        nonUpdatableFieldsMap.get(clazz).put(fieldName, fieldName);
      }
    }
    LOG.info("" + nonUpdatableFieldsMap);
  }

  /**
   * Remove "set" and decapitalize.
   *
   * @param methodName the method name
   * @return field name
   */
  public static String fieldNameFromSetter(String methodName) {
    String fieldName = null;
    if (methodName.startsWith(SET)) {
      fieldName = methodName.substring(SET_LENGTH, SET_LENGTH + 1).toLowerCase() + methodName.substring(SET_LENGTH + 1);
    }
    return fieldName;
  }

  // public static void main(String[] args) {
  //
  // }
}
