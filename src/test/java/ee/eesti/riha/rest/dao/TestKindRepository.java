package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.model.readonly.Kind;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestKindRepository extends AbstractGenericDaoTest {
  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private KindRepository kindRepository;

  private Cache kindsCache;

  @Before
  public void beforeTest() {
    super.beforeTest();
    kindsCache = cacheManager.getCache("kinds");
  }

  @After
  public void afterTest() {
    super.afterTest();
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
    assertNull(kindsCache.get(INFOSYSTEM_KIND_ID, Kind.class));
    Kind kind = kindRepository.getById(INFOSYSTEM_KIND_ID);

    Kind kindFromCache = kindsCache.get(INFOSYSTEM_KIND_ID, Kind.class);
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
    String name = INFOSYSTEM_KIND_NAME;

    assertNull(kindsCache.get(name, Kind.class));
    Kind kind = kindRepository.getByName(name);

    Kind kindFromCache = kindsCache.get(name, Kind.class);
    assertNotNull(kind);
    assertNotNull(kindFromCache);
    assertEquals(kind.getKind_id(), kindFromCache.getKind_id());
    assertEquals(kind.getName(), kindFromCache.getName());
  }
}
