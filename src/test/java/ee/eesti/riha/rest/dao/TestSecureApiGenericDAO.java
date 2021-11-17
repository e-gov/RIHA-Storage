package ee.eesti.riha.rest.dao;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/test-applicationContext.xml")
public class TestSecureApiGenericDAO extends AbstractGenericDaoTest {
  @Autowired
  private SecureApiGenericDAO<Main_resource, Integer> secureMainResourceDAO;

  @Autowired
  private SecureApiGenericDAO<Document, Integer> secureDocumentDAO;

  @Resource
  protected UtilitiesDAO<Document> documentUtilitiesDAO;

  @Test
  public void testFindAll() throws RihaRestException {
    List<Main_resource> main_resources = secureMainResourceDAO.find(Main_resource.class, null, null, null,null);

    assertNotNull(main_resources);
    assertFalse(main_resources.isEmpty());
    assertEquals(1, main_resources.size());
  }
  
  @Test
  public void testFindAllDoc() throws RihaRestException {
    Document document = new Document();
    document.setDocument_id(documentUtilitiesDAO.getNextSeqValForPKForTable(Document.class));
    document.setMain_resource_id(mrAsPrimeTestEntry.getMain_resource_id());
    document.setUri("uri");
    secureDocumentDAO.create(document);

    List<Document> documents;
    documents = secureDocumentDAO.find(Document.class, null, null, null, null);

    assertNotNull(documents);
    assertFalse(documents.isEmpty());
    assertEquals(1, documents.size());

    secureDocumentDAO.delete(Document.class, document.getDocument_id());
  }
  
  @Test
  public void testCreate() throws RihaRestException {
    Main_resource mr = createMain_resource();
    additionalMrTestEntries.add(mr);
    List<Integer> keys = secureMainResourceDAO.create(mr);
    System.out.println(keys);
    assertNotNull(keys);
    assertFalse(keys.isEmpty());
    assertEquals(1, keys.size());
  }
  
  @Test
  public void testCreateList() throws RihaRestException {
    Main_resource mr = createMain_resource();
    additionalMrTestEntries.add(mr);
    List<Integer> keys = secureMainResourceDAO.create(Collections.singletonList(mr));
    System.out.println(keys);
    assertNotNull(keys);
    assertFalse(keys.isEmpty());
    assertEquals(1, keys.size());
  }
  
  @Test
  public void testUpdate() throws RihaRestException {
    System.out.println(JsonHelper.GSON.toJson(mrAsPrimeTestEntry));
    
    Main_resource mr = createSimpleTestMain_resource(mrAsPrimeTestEntry);
    
    int updated = secureMainResourceDAO.update(mr, mrAsPrimeTestEntry.callGetId());
    System.out.println(updated);
    assertTrue(updated > 0);
    assertEquals(1, updated);
  }
  
  @Test
  public void testUpdateList() throws Exception {
    System.out.println(JsonHelper.GSON.toJson(mrAsPrimeTestEntry));
    
    Main_resource mr = createSimpleTestMain_resource(mrAsPrimeTestEntry);
    
    int updated = secureMainResourceDAO.update(Collections.singletonList(mr), "name");
    System.out.println(updated);
    assertTrue(updated > 0);
    assertEquals(1, updated);
  }
  
  @Test
  public void testDelete() throws RihaRestException {
    System.out.println(JsonHelper.GSON.toJson(mrAsPrimeTestEntry));
    
    int delete = secureMainResourceDAO.delete(Main_resource.class, mrAsPrimeTestEntry.callGetId());
    System.out.println(delete);
    assertTrue(delete > 0);
    assertEquals(1, delete);
  }
  
  @Test
  public void testDeleteList() throws RihaRestException {
    System.out.println(JsonHelper.GSON.toJson(mrAsPrimeTestEntry));
    
    int delete = secureMainResourceDAO.delete(Main_resource.class.getSimpleName(), "name", new String[]{EXAMPLE_NAME});
    System.out.println(delete);
    assertTrue(delete > 0);
    assertEquals(1, delete);
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
