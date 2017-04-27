package ee.eesti.riha.rest.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.eesti.riha.rest.model.readonly.Kind;
import ee.eesti.riha.rest.model.readonly.Role_right;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestRole_rightRepository {

  @Autowired
  GenericDAO<Kind> noLogicDAO;

  @Autowired
  CacheManager cacheManager;

  @Autowired
  Role_rightRepository roleRepository;

  Cache roleRightsCache;

  private static final String AUTHENTICATED = "AUTHENTICATED";


  @Before
  public void beforeTest() {
    roleRightsCache = cacheManager.getCache("role_rights");
    // for some reason cache is not empty before test
    roleRightsCache.clear();
//    roleRepository.getByName(AUTHENTICATED);
  }

  @After
  public void afterTest() {
    roleRightsCache.clear();
  }


  @Test
  public void testCacheGetByWrongName() throws Exception {

    String name = "wrongNameHere123";

//    assertNull(roleRightsCache.get(name, Role_right.class));
    assertNull(roleRightsCache.get(name, List.class));
    assertEquals("{}", roleRightsCache.getNativeCache().toString());
    List<Role_right> roleRights = roleRepository.getByName(name);
    assertNotNull(roleRights);
    assertTrue(roleRights.isEmpty());
    // expect cache to contain spring.cache.support.NullValue
    System.out.println(roleRightsCache.getNativeCache());
    assertNotEquals("{}", roleRightsCache.getNativeCache().toString());
    assertTrue(roleRightsCache.getNativeCache().toString().contains("[]"));

  }

  @Test
  public void testCacheGetByName() throws Exception {

    String name = AUTHENTICATED;

//    assertNull(roleRightsCache.get(name, Role_right.class));
    assertNull(roleRightsCache.get(name, List.class));
    List<Role_right> roles = roleRepository.getByName(name);

    List<Role_right> roleFromCache = roleRightsCache.get(name, List.class);
    assertNotNull(roles);
    assertNotNull(roleFromCache);
    
    assertEquals(roles.get(0).getRole_right_id(), roleFromCache.get(0).getRole_right_id());
    assertEquals(roles.get(0).getKind_id(), roleFromCache.get(0).getKind_id());
 

  }

}
