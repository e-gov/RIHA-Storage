package ee.eesti.riha.rest.integration.document;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opPut_Document<T> {

  // general info here
  @Autowired
  WebClient webClient;
  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiCGIService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.DOCUMENT;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_DOCUMENT_CORRECT_SAMPLE_AS_JSON_STRING;

  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_DOCUMENT;
  
  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;

  private static Integer main_resourceId;
  
  @Resource(name = "apiGenericDAOImpl")
  ApiGenericDAO<Document, Integer> genericDAO;

  @Before
  public void beforeTest() {
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
    
    // create test Main_resource
    if (main_resourceId == null) {
      main_resourceId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData,
          TestFinals.MAIN_RESOURCE, jsonToUseForCreateMain_resource);
    }
    // add created Main_resource id to json
    JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
    jsonObject.addProperty("main_resource_id", main_resourceId);
    jsonToUseForCreate = jsonObject.toString();
    
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
  }

  @After
  public void afterTest() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }
  
  @AfterClass
  public static void afterAllTests() {
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, TestFinals.MAIN_RESOURCE,
        main_resourceId);
    main_resourceId = null;
  }

  @Test
  public void testUpdate_jsonContentAndMainResourceWereBothUpdated() throws Exception {
    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_DOCUMENT_AS_JSON_STRING_COMPLEX + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in document
    Document updated = genericDAO.find(Document.class, idUnderTestList.get(0));
    // kind must not be modified
    // assertFalse(updated.getKind().equals("A"));
    System.out.println(updated.getName());
    assertTrue(updated.getName().equals("TEST22"));
    // [OLD] should not have updated null value
    // now should set to null, if field is explicitly set to null
    assertNull(updated.getMime());
    assertEquals(2111111, updated.getOld_id().intValue());

    JsonObject json_contentStored = updated.getJson_content();

    // these fields should be updated in json_content
    // check strings ok
    // kind must not be modified
    // assertFalse(json_contentStored.get("kind").getAsString().equals("A"));
    assertTrue(json_contentStored.get("name").getAsString().equals("TEST22"));

    assertTrue(json_contentStored.get("mime").isJsonNull());
    // check jsonarray ok
    assertEquals(1, json_contentStored.get("organizations").getAsJsonArray().size());
    assertTrue(json_contentStored.get("organizations").getAsJsonArray().get(0).getAsJsonObject().get("test_field")
        .getAsBoolean());
    assertEquals(2, json_contentStored.get("groups").getAsJsonArray().size());
    assertTrue(json_contentStored.get("groups").getAsJsonArray().get(1).getAsString().equals("G_TEST2"));
    // check boolean ok
    assertFalse(json_contentStored.get("uses_ads").getAsBoolean());
    // check number ok
    assertEquals(2111111, json_contentStored.get("old_id").getAsInt());
    assertTrue(json_contentStored.get("description").getAsString().equals("TESTdesc"));
    // check jsonobject ok
    assertTrue(json_contentStored.get("another_object").getAsJsonObject().get("field1").getAsString().equals("val1"));

  }

  @Test
  public void testUpdateDocument() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_DOCUMENT_FILE + "}";

    // String testContentBeginning = "PD94b";
    String testContentBeginning = "<?xml version=\"1.0\"";
    String updatedContentBeginning = "TEST UPDATE";

    // assert file exists
    String filePath = FileHelper.PATH_ROOT + FileHelper.createDocumentFilePath(idUnderTestList.get(0));
    List<String> lines = FileHelper.readFile(filePath);
    assertNotNull(lines);
    assertTrue(!lines.isEmpty());
    assertTrue(lines.get(0).startsWith(testContentBeginning));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in document
    Document updated = genericDAO.find(Document.class, idUnderTestList.get(0));
    JsonObject json_contentStored = updated.getJson_content();

    assertEquals(FileHelper.createDocumentFilePath(idUnderTestList.get(0)), json_contentStored.get("content")
        .getAsString());

    // assert file updated
    lines = FileHelper.readFile(filePath);
    assertNotNull(lines);
    assertTrue(!lines.isEmpty());
    System.out.println(lines);
    assertTrue(lines.get(0).startsWith(updatedContentBeginning));

  }

  @Test
  public void testUpdateDocumentURL() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": {\"url\":\"url_updated\"} }";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in document
    Document updated = genericDAO.find(Document.class, idUnderTestList.get(0));
    JsonObject json_contentStored = updated.getJson_content();

    assertEquals("url_updated", updated.getUrl());
    assertEquals("url_updated", json_contentStored.get("url").getAsString());
  }

  @Test
  public void testUpdate_whenParameterPathIsNotValid_thenError() throws Exception {

    String path = pathToUse + "/test/test///";
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_DOCUMENT_AS_JSON_STRING_COMPLEX + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testUpdate_whenParameterPathValueIsNotValid_whenIdIsNotNumber_thenError() throws Exception {

    String path = pathToUse + "abc/";
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_DOCUMENT_AS_JSON_STRING_COMPLEX + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testUpdateDocument_whenNonExistentKind_thenError() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\"op\":\"put\"," + "\"path\": \"" + path + "\"," + "\"token\":\"testToken\",\"data\": "
        + "{\"kind\": \"infosystemTEST\", \"name\": \"TEST22\", \"mime\": null, \"content\":\"VEVA==\"}" + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.KIND_NOT_FOUND, error.getErrcode());
    assertEquals(ErrorCodes.KIND_NOT_FOUND_MSG, error.getErrmsg());
  }
  
  @Test
  public void testUpdateDocument_whenAechived_thenError() throws Exception {

    // prepare for test, set value to end_date -> archived
    int documentId = idUnderTestList.get(0);
    Document document = genericDAO.find(Document.class, documentId);
    document.setEnd_date(new Date());
    genericDAO.update(document, documentId);
        
    
    String path = pathToUse + documentId;
    String json = "{\"op\":\"put\"," + "\"path\": \"" + path + "\"," + "\"token\":\"testToken\",\"data\": "
        + "{\"name\": \"TEST22\", \"mime\": null}" + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.CANT_UPDATE_ARCHIVED, error.getErrcode());
    assertEquals(ErrorCodes.CANT_UPDATE_ARCHIVED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().contains("" + documentId));
  }

  @Test
  public void testUpdateDocumentList() throws Exception {

    String path = pathToUse;
    // bad idea to update by kind, could update a lot of rows
//    String kind = "infosystemTEST";
    String fileName = "Liitumise_taotlus_70006317TEST.ddoc";
    JsonObject jsonUpdate = JsonHelper.getFromJson(TestFinals.JSON_TO_UPDATE_DOCUMENT_FILE);
    jsonUpdate.addProperty("filename", fileName);
    String json = "{\r\n" + "	\"op\":\"put\", \"key\":\"filename\",\r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": [" + jsonUpdate.toString() + "]}";

    // String testContentBeginning = "PD94b";
    String testContentBeginning = "<?xml version=\"1.0\"";
    String updatedContentBeginning = "TEST UPDATE";

    // assert file exists
    String filePath = FileHelper.PATH_ROOT + FileHelper.createDocumentFilePath(idUnderTestList.get(0));
    List<String> lines = FileHelper.readFile(filePath);
    assertNotNull(lines);
    assertTrue(!lines.isEmpty());
    assertTrue(lines.get(0).startsWith(testContentBeginning));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    Document updated = genericDAO.find(Document.class, idUnderTestList.get(0));
    JsonObject json_contentStored = updated.getJson_content();

    assertEquals(FileHelper.createDocumentFilePath(idUnderTestList.get(0)), json_contentStored.get("content")
        .getAsString());

    // assert file updated
    lines = FileHelper.readFile(filePath);
    assertNotNull(lines);
    assertTrue(!lines.isEmpty());
    System.out.println(lines);
    assertTrue(lines.get(0).startsWith(updatedContentBeginning));

  }

  // @Ignore("unfinished")
  // @Ignore("max connetions reached DB error; need find solution, then enable
  // again this test") @Test
  // public void testUpdateList() throws Exception {
  //
  // String path = pathToUse;
  // String data = "[{\"main_resource_id\":922,
  // \"short_name\":\"xLL\",\"name\":\"yLL\"},{\"main_resource_id\":923,
  // \"short_name\":\"zLL\",\"name\":\"aLL\"}]";
  // String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" + path
  // + "\", \r\n"
  // + "\"key\": \"main_resource_id\", \"data\": " + data + "}";
  //
  // Response response = client.postCGI(json);
  //
  // assertNotNull(response.getEntity());
  //
  // Map<String, Integer> result = TestHelper.getResultMap(response);
  // Integer numOfChanged = 2;
  // assertNotNull(result);
  // assertEquals(numOfChanged, result.get(Finals.OK));
  //
  // // TODO
  // // test thad properties were actually updated
  //
  // }
  //
  // @Ignore("TODO needs to be rewritten")
  // @Ignore("max connetions reached DB error; need find solution, then enable
  // again this test") @Test
  // public void testUpdateListByOwner() throws Exception {
  //
  // // Integer id = 10015;
  // // List<Main_resource> main_resources = new ArrayList<>();
  // // String ownerOrganization = "TEST_ORG_123XX";
  // // int count = 2;
  // // for (int i = 0; i < count; i++) {
  // // Main_resource main_resource =
  // // TestApiGenericDAOMain_resource.createTestMain_resource(id + i);
  // // main_resource.setOwner(ownerOrganization);
  // // main_resources.add(main_resource);
  // // }
  // //
  // // client.create(GsonHelper.GSON.toJson(main_resources), tableName);
  // //
  // // Main_resource main_resource = new Main_resource();
  // // main_resource.setOwner(ownerOrganization);
  // // main_resource.setName("TEST_UPDATE_NAME");
  // // main_resource.setKind("XXXXXXXXXXX");
  // //
  // // String path = pathToUse;
  // // String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" +
  // // path + "\", \r\n" + " \"key\": \"owner\"," + " \"data\": ["
  // // + GsonHelper.GSON.toJson(main_resource) + "]}";
  // // System.out.println(json);
  // // Response response = client.postCGI(json);
  // // System.out.println(response);
  // // assertNotNull(response.getEntity());
  // //
  // // Map<String, Integer> result = TestHelper.getResultMap(response);
  // // Integer numOfChanged = 2;
  // // assertNotNull(result);
  // // assertEquals(numOfChanged, result.get(Finals.OK));
  // //
  // // for (int i = 0; i < count; i++) {
  // // client.delete(tableName, id + i);
  // // }
  //
  // }

}
