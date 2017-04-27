package ee.eesti.riha.rest.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestKindRepository {

  @Autowired
  GenericDAO<Kind> noLogicDAO;

  @Autowired
  CacheManager cacheManager;

  @Autowired
  KindRepository kindRepository;

  Cache kindsCache;

  private static final String INFOSYSTEM = "infosystem";
  static Integer infosystemId = null;

  @Before
  public void beforeTest() {
    kindsCache = cacheManager.getCache("kinds");

    // find infosystemId
    if (infosystemId == null) {
      List<Kind> kinds = noLogicDAO.findAll(Kind.class);
      for (Kind kind : kinds) {
        if (kind.getName().equals(INFOSYSTEM)) {
          infosystemId = kind.getKind_id();
          break;
        }
      }
    }
  }

  @After
  public void afterTest() {
    kindsCache.clear();
  }

  @Test
  public void testCacheGetByWrongId() throws Exception {

    int id = 555;

    assertNull(kindsCache.get(id, Kind.class));
    assertEquals("{}", kindsCache.getNativeCache().toString());
    assertNull(kindRepository.getById(id));

    // expect cache to contain spring.cache.support.NullValue
    System.out.println(kindsCache.getNativeCache());
    assertNotEquals("{}", kindsCache.getNativeCache().toString());
    assertTrue(kindsCache.getNativeCache().toString().contains("NullValue"));

  }

  @Test
  public void testCacheGetByInfosystemId() throws Exception {

    int id = infosystemId;

    assertNull(kindsCache.get(id, Kind.class));
    Kind kind = kindRepository.getById(id);

    Kind kindFromCache = kindsCache.get(id, Kind.class);
    assertNotNull(kind);
    assertNotNull(kindFromCache);
    assertEquals(kind.getKind_id(), kindFromCache.getKind_id());
    assertEquals(kind.getName(), kindFromCache.getName());

  }

  @Test
  public void testCacheGetByWrongName() throws Exception {

    String name = "badNameForKind123";

    assertNull(kindsCache.get(name, Kind.class));
    assertEquals("{}", kindsCache.getNativeCache().toString());
    assertNull(kindRepository.getByName(name));

    // expect cache to contain spring.cache.support.NullValue
    System.out.println(kindsCache.getNativeCache());
    assertNotEquals("{}", kindsCache.getNativeCache().toString());
    assertTrue(kindsCache.getNativeCache().toString().contains("NullValue"));

  }

  @Test
  public void testCacheGetByName() throws Exception {

    String name = INFOSYSTEM;

    assertNull(kindsCache.get(name, Kind.class));
    Kind kind = kindRepository.getByName(name);

    Kind kindFromCache = kindsCache.get(name, Kind.class);
    assertNotNull(kind);
    assertNotNull(kindFromCache);
    assertEquals(kind.getKind_id(), kindFromCache.getKind_id());
    assertEquals(kind.getName(), kindFromCache.getName());

  }

}
