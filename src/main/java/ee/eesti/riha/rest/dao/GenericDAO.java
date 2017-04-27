package ee.eesti.riha.rest.dao;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface GenericDAO.
 *
 * @param <T> the generic type
 */
public interface GenericDAO<T> {

  /**
   * Find all.
   *
   * @param clazz the clazz
   * @return the list
   */
  List<T> findAll(Class<T> clazz);

  /**
   * Find by id.
   *
   * @param clazz the clazz
   * @param id the id
   * @return the t
   */
  T findById(Class<T> clazz, Integer id);

  /**
   * Creates the.
   *
   * @param object the object
   * @return the int
   */
  int create(T object);

  /**
   * Update.
   *
   * @param object the object
   * @return the int
   */
  int update(T object);

  /**
   * Delete.
   *
   * @param object the object
   */
  void delete(T object);

}
