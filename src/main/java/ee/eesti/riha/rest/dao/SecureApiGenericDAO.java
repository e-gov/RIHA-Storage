package ee.eesti.riha.rest.dao;

import java.util.List;

import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.RihaRestException;

/**
 * This interface is a layer before ApiGenericDAO to check whether user has permission to perform operations
 *
 * @param <T>
 * @param <K>
 */
public interface SecureApiGenericDAO<T, K> {

  List<T> find(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents, String sort)
      throws RihaRestException;

  T find(Class<T> clazz, Integer id) throws RihaRestException;

  List<T> findByMainResourceId(Class<T> clazz, Integer id) throws RihaRestException;

  Integer findCount(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents, String sort)
      throws RihaRestException;

  Integer findCount(Class<T> clazz) throws RihaRestException;

  List<K> create(T object) throws RihaRestException;

  List<K> create(List<T> object) throws RihaRestException;

  int update(T object, Integer id) throws RihaRestException;

  int update(List<T> object, String idFieldName) throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException, RihaRestException;

  int delete(Class<T> clazz, Integer id) throws RihaRestException;

  int delete(String tableName, String key, Object[] values) throws RihaRestException;

}
