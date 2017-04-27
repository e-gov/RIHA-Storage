package ee.eesti.riha.rest.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.model.readonly.Asutus;
import ee.eesti.riha.rest.model.readonly.Kind;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestSecureApiGenericDAO<T, K> {

  @Autowired
  SecureApiGenericDAO<T, K> secureGenericDAO;

  @Autowired
  GenericDAO<T> noLogicDAO;
  
  @Autowired
  ApiGenericDAO<T, K> genericDAO;
   
  @Autowired
  UtilitiesDAO<T> utilitiesDAO;
  
  AuthInfo authInfo = new AuthInfo("123", "ORG", "ADMIN", "asd123123");
  AuthInfo superAuthInfo = new AuthInfo("123", "ORG", "ROLL_RIHA_ADMINISTRAATOR", "asd123123");
  AuthInfo betterAuthInfo = new AuthInfo("123", "ORG", "ROLL_RIHA_HALDUR", "asd123123");
  
  private static final String INFOSYSTEM = "infosystem";
  static Integer infosystemId = null;
  
  public static final String EXAMPLE_NAME = "SECURE MAIN_RESOURCE 123TEST";
  public static final String EXAMPLE_SHORT_NAME = "smr55";
  public static final int EXAMPLE_OLD_ID = 222999;
  
  // before every test this item will be created in db and would be accessible
  // for test; after test it would be deleted
  Main_resource mrAsPrimeTestEntry;
  // some tests require more than one item, these can be placed here inside
  // the test; items in this list will be deleted from db after test
  List<Main_resource> additionalMrTestEntries = new ArrayList<Main_resource>();
  
  @Before
  public void beforeTest() {
    if (infosystemId == null) {
      List<Kind> kinds = (List<Kind>) noLogicDAO.findAll((Class<T>) Kind.class);
      for (Kind kind : kinds) {
        if (kind.getName().equals(INFOSYSTEM)) {
          infosystemId = kind.getKind_id();
          break;
        }
      }
    }
    
    mrAsPrimeTestEntry = createTestMain_resource();
    List<K> created = genericDAO.create((T)mrAsPrimeTestEntry);
    
    assertNotNull(created);
    assertEquals(1, created.size());
  }

  @After
  public void afterTest() {
//     clean up always
    genericDAO.delete((T)mrAsPrimeTestEntry);
    genericDAO.delete((List<T>)additionalMrTestEntries);
    additionalMrTestEntries.clear();
  }

  @Test
  public void testFindAll() throws RihaRestException {

    List<Main_resource> main_resources = null;
    int limit = 10;

    
    // use limit, otherwise too slow to show all elements ~200MB
    main_resources = (List<Main_resource>) secureGenericDAO.find(
        (Class<T>) Main_resource.class, limit, null, null,
        null, authInfo);

    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(limit, main_resources.size());

  }
  
  @Test
  public void testFindAllDoc() throws RihaRestException {
    
    List<Document> documents = null;
    int limit = 10;

    // use limit, otherwise too slow to show all elements ~200MB
    documents = (List<Document>) secureGenericDAO.find(
        (Class<T>) Document.class, limit, null, null,
        null, authInfo);

    assertNotNull(documents);
    assertFalse(documents.isEmpty());
    assertEquals(limit, documents.size());

  }
  
  @Test
  public void testCreate() throws RihaRestException {
    Main_resource mr = createTestMain_resource();
    mr.setOwner("ORG");
    additionalMrTestEntries.add(mr);
    List<K> keys = secureGenericDAO.create((T) mr, superAuthInfo);
    System.out.println(keys);
    assertNotNull(keys);
    assertFalse(keys.isEmpty());
    assertEquals(1, keys.size());
  }
  
  @Test
  public void testCreateList() throws RihaRestException {
    Main_resource mr = createTestMain_resource();
    mr.setOwner("ORG");
    additionalMrTestEntries.add(mr);
    List<K> keys = secureGenericDAO.create((List<T>)Arrays.asList(mr), superAuthInfo);
    System.out.println(keys);
    assertNotNull(keys);
    assertFalse(keys.isEmpty());
    assertEquals(1, keys.size());
  }
  
  @Test(expected=RihaRestException.class)
  public void testCreateListFail() throws RihaRestException {
    Main_resource mr = createTestMain_resource();
    mr.setOwner("ORG");
    additionalMrTestEntries.add(mr);
    List<K> keys = secureGenericDAO.create((List<T>)Arrays.asList(mr), authInfo);
    System.out.println(keys);

  }
  
  @Test
  public void testUpdate() throws RihaRestException {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    Main_resource mr = createSimpleTestMain_resource(old);
    
    int updated = secureGenericDAO.update((T) mr, old.callGetId(), superAuthInfo);
    System.out.println(updated);
    assertTrue(updated > 0);
    assertEquals(1, updated);
  }
  
  @Test
  public void testUpdateList() throws Exception {

    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    Main_resource mr = createSimpleTestMain_resource(old);
    
    int updated = secureGenericDAO.update((List<T>)Arrays.asList(mr), "name", superAuthInfo);
    System.out.println(updated);
    assertTrue(updated > 0);
    assertEquals(1, updated);
  }
  
  @Test(expected=RihaRestException.class)
  public void testUpdateListFail() throws Exception {
   
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    Main_resource mr = createSimpleTestMain_resource(old);
    
    int updated = secureGenericDAO.update((List<T>)Arrays.asList(mr), "name", authInfo);
    System.out.println(updated);

  }

  
  @Test
  public void testDelete() throws RihaRestException {
    
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    int delete = secureGenericDAO.delete((Class<T>) Main_resource.class, old.callGetId(), superAuthInfo);
    System.out.println(delete);
    assertTrue(delete > 0);
    assertEquals(1, delete);
  }
  
  @Test
  public void testDeleteList() throws RihaRestException {
    
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    int delete = secureGenericDAO.delete(Main_resource.class.getSimpleName(), "name", new String[]{EXAMPLE_NAME}, superAuthInfo);
    System.out.println(delete);
    assertTrue(delete > 0);
    assertEquals(1, delete);
  }
  
  @Test(expected=RihaRestException.class)
  public void testDeleteListFail() throws RihaRestException {

    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    int delete = secureGenericDAO.delete(Main_resource.class.getSimpleName(), "name", new String[]{EXAMPLE_NAME}, authInfo);
    System.out.println(delete);

  }
  
  public Main_resource createSimpleTestMain_resource(Main_resource old) {
    Main_resource mr = new Main_resource();
    mr.setOwner("ORG");
    mr.setName(EXAMPLE_NAME);
    mr.setKind_id(old.getKind_id());
    mr.setVersion(old.getVersion());
    mr.setShort_name(old.getShort_name());
    mr.setOld_id(old.getOld_id());
    JsonObject jsonContent = JsonHelper.getFromJson(JsonHelper.GSON.toJson(mr));
    mr.setJson_content(jsonContent);
    mr.getJson_content().addProperty("infosystem_status", TestFinals.INFOSYSTEM_STATUS);
    mr.getJson_content().addProperty("inapproval", true);
    return mr;
  }
  
  public Main_resource createTestMain_resource() {
    Main_resource main_resource = new Main_resource();
    // required fields
    main_resource.setMain_resource_id(utilitiesDAO.getNextSeqValForPKForTable((Class<T>) Main_resource.class));
    main_resource.setUri("uri");
    main_resource.setName(EXAMPLE_NAME);
    main_resource.setOwner("owner");
    main_resource.setVersion("1.1");
    main_resource.setKind("infosystem");
    // TODO change, currently infosystem kind_id = 389
    main_resource.setKind_id(infosystemId);

    System.out.println("SETTING KIND _ID " + infosystemId);
    
    main_resource.setCreator("test_creator");
    main_resource.setCreation_date(new Date());
    // not required fields
    main_resource.setShort_name(EXAMPLE_SHORT_NAME);
    main_resource.setOld_id(EXAMPLE_OLD_ID);
    main_resource.setField_name("testTEST01");

    // it is expected to have json_content
    main_resource.setJson_content(JsonHelper.getFromJson(JsonHelper.GSON.toJson(main_resource)));
    return main_resource;
  }
  
  
//  @Test
//  public void testFindAllAsutus() throws RihaRestException {
//    
//    List<Asutus> documents = null;
//    int limit = 10;
//
//    AuthInfo authInfo = new AuthInfo("123", "ORG", "ADMIN", "asd123123");
//    // use limit, otherwise too slow to show all elements ~200MB
//    documents = (List<Asutus>) secureGenericDAO.find(
//        (Class<T>) Asutus.class, limit, null, null,
//        null, authInfo);
//
//    assertNotNull(documents);
//    assertFalse(documents.isEmpty());
//    assertEquals(limit, documents.size());
//
//  }
  
//  @Test
//  public void testFindById() {
//
////    Main_resource found = secureGenericDAO.find(Main_resource.class, mrAsPrimeTestEntry.getMain_resource_id());
////    assertNotNull(found);
//
//  }

}
