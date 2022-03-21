package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.model.readonly.Kind;
import org.springframework.cache.annotation.Cacheable;

/**
 * Cache repository of {@link ee.eesti.riha.rest.model.readonly.Kind Kind} objects
 *
 */
public interface KindRepository extends ClassifierRepository<Kind> {

  @Cacheable(cacheNames = "kinds", key = "#name")
  @Override
  Kind getByName(String name);

  @Cacheable(cacheNames = "kinds", key = "#kind_id")
  @Override
  Kind getById(Integer kind_id);
}
