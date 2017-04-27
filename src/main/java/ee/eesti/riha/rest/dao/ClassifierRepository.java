package ee.eesti.riha.rest.dao;

/**
 * Generic repository interface for caching.
 *
 * @param <T> the generic type
 */
public interface ClassifierRepository<T> {

  /**
   * Gets the by name.
   *
   * @param name the name
   * @return the by name
   */
  T getByName(String name);

  /**
   * Gets the by id.
   *
   * @param id the id
   * @return the by id
   */
  T getById(Integer id);
}
