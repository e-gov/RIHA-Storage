package ee.eesti.riha.rest.dao;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonObject;

import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*: **/test-applicationContext.xml")
public class TestSecureApiGenericDAO extends AbstractGenericDaoTest {

  @Autowired
  SecureApiGenericDAO<Main_resource, Integer> secureMainResourceDAO;

  @Autowired
  SecureApiGenericDAO<Document, Integer> secureDocumentDAO;

  @Autowired
  ApiGenericDAO<Main_resource, Integer> mainResourceDAO;
   
  @Autowired
  UtilitiesDAO<Main_resource> utilitiesDAO;
  
  private static final String EXAMPLE_NAME = "SECURE MAIN_RESOURCE 123TEST";
  private static final String EXAMPLE_SHORT_NAME = "smr55";
  private static final int EXAMPLE_OLD_ID = 222999;
  
  @Test
  public void testFindAll() throws RihaRestException {
    List<Main_resource> main_resources ;
    main_resources = secureMainResourceDAO.find(Main_resource.class, null, null, null,null);

    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
  }
  
  @Test
  public void testFindAllDoc() throws RihaRestException {
    Document document = new Document();
    secureDocumentDAO.create(document);

    List<Document> documents;
    int limit = 10;

    // use limit, otherwise too slow to show all elements ~200MB
    documents = secureDocumentDAO.find(Document.class, limit, null, null, null);

    assertNotNull(documents);
    assertFalse(documents.isEmpty());
    assertEquals(limit, documents.size());

    secureDocumentDAO.delete(Document.class, document.getDocument_id());
  }
  
  @Test
  public void testCreate() throws RihaRestException {
    Main_resource mr = createTestMain_resource();
    additionalMrTestEntries.add(mr);
    List<Integer> keys = secureMainResourceDAO.create(mr);
    System.out.println(keys);
    assertNotNull(keys);
    assertFalse(keys.isEmpty());
    assertEquals(1, keys.size());
  }
  
  @Test
  public void testCreateList() throws RihaRestException {
    Main_resource mr = createTestMain_resource();
    additionalMrTestEntries.add(mr);
    List<Integer> keys = secureMainResourceDAO.create(Collections.singletonList(mr));
    System.out.println(keys);
    assertNotNull(keys);
    assertFalse(keys.isEmpty());
    assertEquals(1, keys.size());
  }
  
  @Test(expected=RihaRestException.class)
  public void testCreateListFail() throws RihaRestException {
    Main_resource mr = createTestMain_resource();
    additionalMrTestEntries.add(mr);
    List<Integer> keys = secureMainResourceDAO.create(Collections.singletonList(mr));
    System.out.println(keys);
  }
  
  @Test
  public void testUpdate() throws RihaRestException {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    Main_resource mr = createSimpleTestMain_resource(old);
    
    int updated = secureMainResourceDAO.update(mr, old.callGetId());
    System.out.println(updated);
    assertTrue(updated > 0);
    assertEquals(1, updated);
  }
  
  @Test
  public void testUpdateList() throws Exception {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    Main_resource mr = createSimpleTestMain_resource(old);
    
    int updated = secureMainResourceDAO.update(Collections.singletonList(mr), "name");
    System.out.println(updated);
    assertTrue(updated > 0);
    assertEquals(1, updated);
  }
  
  @Test(expected=RihaRestException.class)
  public void testUpdateListFail() throws Exception {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    Main_resource mr = createSimpleTestMain_resource(old);
    
    int updated = secureMainResourceDAO.update(Collections.singletonList(mr), "name");
    System.out.println(updated);
  }

  
  @Test
  public void testDelete() throws RihaRestException {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    int delete = secureMainResourceDAO.delete(Main_resource.class, old.callGetId());
    System.out.println(delete);
    assertTrue(delete > 0);
    assertEquals(1, delete);
  }
  
  @Test
  public void testDeleteList() throws RihaRestException {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    int delete = secureMainResourceDAO.delete(Main_resource.class.getSimpleName(), "name", new String[]{EXAMPLE_NAME});
    System.out.println(delete);
    assertTrue(delete > 0);
    assertEquals(1, delete);
  }
  
  @Test(expected=RihaRestException.class)
  public void testDeleteListFail() throws RihaRestException {
    Main_resource old = mrAsPrimeTestEntry;
    System.out.println(JsonHelper.GSON.toJson(old));
    
    int delete = secureMainResourceDAO.delete(Main_resource.class.getSimpleName(), "name", new String[]{EXAMPLE_NAME});
    System.out.println(delete);
  }
  
  public Main_resource createSimpleTestMain_resource(Main_resource old) {
    Main_resource mr = new Main_resource();
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
    main_resource.setMain_resource_id(utilitiesDAO.getNextSeqValForPKForTable(Main_resource.class));
    main_resource.setUri("uri");
    main_resource.setName(EXAMPLE_NAME);
    main_resource.setVersion("1.1");
    main_resource.setKind(INFOSYSTEM_KIND_NAME);
    // TODO change, currently infosystem kind_id = 389
    main_resource.setKind_id(INFOSYSTEM_KIND_ID);

    System.out.println("SETTING KIND _ID " + INFOSYSTEM_KIND_ID);
    
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
//    documents = (List<Asutus>) secureMainResourceDAO.find(
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
////    Main_resource found = secureMainResourceDAO.find(Main_resource.class, mrAsPrimeTestEntry.getMain_resource_id());
////    assertNotNull(found);
//
//  }

}
