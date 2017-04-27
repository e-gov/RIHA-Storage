package ee.eesti.riha.rest.dao;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import ee.eesti.riha.rest.model.readonly.Role_right;

public interface Role_rightRepository {

  @Cacheable(cacheNames = "role_rights", key = "#name")
  List<Role_right> getByName(String name);

  @Cacheable(cacheNames = "role_rights", key = "#role_right_id")
  Role_right getById(Integer role_right_id);
}
