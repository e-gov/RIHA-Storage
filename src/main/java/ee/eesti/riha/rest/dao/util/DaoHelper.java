package ee.eesti.riha.rest.dao.util;

import java.lang.reflect.Field;
import java.util.List;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.model.util.FieldIsPK;

/**
 * The Class DaoHelper.
 */
public final class DaoHelper {

  private DaoHelper() {
  }

  /**
   * Gets field of model class that represents primary key in database. To use this method, one of the field in data
   * model passed to method by class parameter must have field marked with annotation <code>({@link FieldIsPK})</code>.
   *
   * @param clazz the clazz
   * @return the primary key of data model
   */
  public static Field getPrimaryKeyOfDataModel(Class clazz) {

    Field pkField = null;
    for (Field field : clazz.getDeclaredFields()) {
      if (field.getAnnotation(FieldIsPK.class) != null) {
        pkField = field;
        break;
      }
    }

    if (pkField == null) {
      throw new IllegalStateException("data class must have primary key marked!");
    }

    return pkField;

  }

  /**
   * Checks if is field part of model.
   *
   * @param field the field
   * @param clazzRepresentingModel the clazz representing model
   * @return true, if is field part of model
   */
  public static boolean isFieldPartOfModel(String field, Class clazzRepresentingModel) {
    try {
      clazzRepresentingModel.getDeclaredField(field);
      return true;
    } catch (NoSuchFieldException e) {
      return false;
    } catch (SecurityException e) {
      throw e;
    }
  }

  /**
   * Hibernate converts "id" to @Id in HQL, so it is part of model as well.
   *
   * @param field the field
   * @param clazzRepresentingModel the clazz representing model
   * @return true, if is field part of hibernate model
   */
  public static boolean isFieldPartOfHibernateModel(String field, Class clazzRepresentingModel) {
    return DaoHelper.isFieldPartOfModel(field, clazzRepresentingModel) || StringHelper.areEqual(field, Finals.ID);
  }

  /**
   * All fields in filter appear in model.
   *
   * @param filter the filter
   * @param clazz the clazz
   * @return true, if successful
   */
  public static boolean allFieldsInFilterAppearInModel(List<FilterComponent> filter, Class clazz) {
    boolean allFieldsExistInModel = false;
    for (FilterComponent fc : filter) {
      String field = fc.getOperandLeft();
      // is field part of model? exclude json_content
      // if (!field.equals(Finals.JSON_CONTENT)) {
      if (DaoHelper.isFieldPartOfModel(field, clazz)) {
        allFieldsExistInModel = true;
      } else {
        allFieldsExistInModel = false;
        break;
      }
      // }
    }
    return allFieldsExistInModel;
  }

}
