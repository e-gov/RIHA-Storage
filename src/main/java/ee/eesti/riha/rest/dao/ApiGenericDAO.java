package ee.eesti.riha.rest.dao;

import java.util.List;

import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.RihaRestException;

// TODO: Auto-generated Javadoc
/**
 * Collection of methods to satisfy all API CRUD needs.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
public interface ApiGenericDAO<T, K> {

  /**
   * Finds elements from table represented by model class with possiblity of using additional parameters.
   *
   * @param clazz the clazz
   * @param limit the limit
   * @param offset the offset
   * @param filterComponents the filter components
   * @param sort the sort
   * @return the list
   * @throws RihaRestException the riha rest exception
   */
  List<T> find(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents, String sort)
      throws RihaRestException;

  /**
   * Finds element by id.
   *
   * @param clazz the clazz
   * @param id the id
   * @return the t
   */
  T find(Class<T> clazz, Integer id);

  /**
   * Finds element by main_resource_id.
   *
   * @param clazz the clazz
   * @param id the id
   * @return the list
   */
  List<T> findByMainResourceId(Class<T> clazz, Integer id);

  /**
   * Finds count of results from query with filters.
   *
   * @param clazz the clazz
   * @param limit the limit
   * @param offset the offset
   * @param filterComponents the filter components
   * @param sort the sort
   * @return the integer
   * @throws RihaRestException the riha rest exception
   */
  Integer findCount(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents, String sort)
      throws RihaRestException;

  /**
   * Finds count of rows in table.
   *
   * @param clazz the clazz
   * @return the integer
   */
  Integer findCount(Class<T> clazz);

  /**
   * Inserts object to database.
   *
   * @param object the object
   * @return list of created primary keys
   */
  List<K> create(T object);

  /**
   * Inserts objects to database.
   *
   * @param object the object
   * @return list of created primary keys
   */
  List<K> create(List<T> object);

  /**
   * Updates object in database.
   *
   * @param object the object
   * @param id the id
   * @return number of rows changed
   * @throws RihaRestException the riha rest exception
   */
  int update(T object, Integer id) throws RihaRestException;

  /**
   * Updates objects in database identified by specified field name.
   *
   * @param object list of elements
   * @param idFieldName name of the field by which the update will be done, must not be null in object list elements
   * @return number of rows changed
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   * @throws RihaRestException the riha rest exception
   */
  int update(List<T> object, String idFieldName) throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException, RihaRestException;

  /**
   * Deletes object.
   *
   * @param object the object
   */
  void delete(T object);

  /**
   * Deletes object with id.
   * 
   * @param clazz model class
   * @param id primary key
   * @return number of deleted rows
   */
  int delete(Class<T> clazz, Integer id);

  /**
   * Deletes those entries that have field (specified) set to any value (specified).
   *
   * @param tableName the table name
   * @param key specified field (explain: when thinking of SQL, it is a in "WHERE a IN (B)")
   * @param values field values by which deletion must be done (explain: it is any b in "WHERE a IN (B)")
   * @return number of rows deleted
   */
  int delete(String tableName, String key, Object[] values);

  /**
   * Deletes objects.
   *
   * @param objects the objects
   */
  void delete(List<T> objects);

}
