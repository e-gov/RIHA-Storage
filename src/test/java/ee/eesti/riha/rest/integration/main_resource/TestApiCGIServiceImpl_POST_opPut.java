package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opPut<T> {

  // general info here
  @Autowired
  WebClient webClient;
  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiCGIService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.MAIN_RESOURCE;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static String jsonToUseForCreateJsonField = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_WITH_TEST_FIELD;

  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_MAIN_RESOURCE;
  @Resource(name = "apiGenericDAOImpl")
  ApiGenericDAO<Main_resource, Integer> genericDAO;

  @Before
  public void beforeTest() {
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
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

  @Test
  public void testUpdate_jsonContentAndMainResourceWereBothUpdated() throws Exception {
    String testShortName = "TEST_SH_NM_1";
    JsonObject updateJson = JsonHelper.getFromJson(TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX);
    updateJson.addProperty("short_name", testShortName);
    
    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + updateJson.toString() + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in main_resource
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));
    // kind is final and must not be updatable
    // assertFalse(mrUpdated.getKind().equals("A"));
    assertTrue(mrUpdated.getName().equals("TEST1"));
    // [OLD] should not have updated null value
    // now should set to null, if field is explicitly set to null
    // infosystem short_name must not be null
    // assertNull(mrUpdated.getShort_name());
    assertEquals(testShortName, mrUpdated.getShort_name());
    assertEquals(2, mrUpdated.getOld_id().intValue());

    JsonObject json_contentStored = mrUpdated.getJson_content();

    // these fields should be updated in json_content
    // check strings ok
    // kind is final and must not be updatable
    // assertFalse(json_contentStored.get("kind").getAsString().equals("A"));
    assertTrue(json_contentStored.get("name").getAsString().equals("TEST1"));

    // infosystem short_name must not be null
    // assertTrue(json_contentStored.get("short_name").isJsonNull());
    assertEquals(testShortName, json_contentStored.get("short_name").getAsString());
    // check jsonarray ok
    assertEquals(1, json_contentStored.get("organizations").getAsJsonArray().size());
    assertTrue(json_contentStored.get("organizations").getAsJsonArray().get(0).getAsJsonObject().get("test_field")
        .getAsBoolean());
    assertEquals(2, json_contentStored.get("groups").getAsJsonArray().size());
    assertTrue(json_contentStored.get("groups").getAsJsonArray().get(1).getAsString().equals("G_TEST2"));
    // check boolean ok
    assertFalse(json_contentStored.get("uses_ads").getAsBoolean());
    // check number ok
    assertEquals(2, json_contentStored.get("old_id").getAsInt());
    assertTrue(json_contentStored.get("description").getAsString().equals("TESTdesc"));
    // check jsonobject ok
    assertTrue(json_contentStored.get("another_object").getAsJsonObject().get("field1").getAsString().equals("val1"));

  }

  @Test
  public void testUpdate_whenParameterPathIsNotValid_thenError() throws Exception {

    String path = pathToUse + "/test/test///";
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX + "}";
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
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testUpdate_whenArchived_thenError() throws Exception {
    //
    String dateJson = DateHelper.FORMATTER.format(new Date());
    JsonObject jsonObject = JsonHelper
        .getFromJson(TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING);
    jsonObject.addProperty("end_date", dateJson);

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonObject.toString()));

    // 2 elements created before already
    int id = idUnderTestList.get(2);
    String path = pathToUse + id;
    String json = "{\r\n" + "	\"op\":\"put\", \r\n" + "	\"path\": \"" + path + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\": " + TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    System.out.println(error);
    assertNotNull(error);
    assertEquals(ErrorCodes.CANT_UPDATE_ARCHIVED, error.getErrcode());
    assertEquals(ErrorCodes.CANT_UPDATE_ARCHIVED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().contains(id + ""));

  }

  @Test
  public void testUpdate_whenNestedObjectColonInString_thenOk() throws Exception {

    String jsonWithBug = "{\"op\":\"put\",\"path\":\"db/main_resource/"
        + idUnderTestList.get(0)
        + "\",\"token\":\"testToken\","
        + "\"key\":349790,\"data\":{\"reference_number\":\"zxczxc123\",\"name\":\"TEST:: muudetud klassifikaator\","
        + "\"short_name\":\"tka1\",\"classifier_status\":\"sisestamisel\",\"base_classifier\":null,\"owner\":\"21349\","
        + "\"access_restriction\":0,\"approved_by_law\":null,\"approval_required\":null,\"legal_basis\":null,\"state\":\"C\","
        + "\"start_date\":null,\"end_date\":null,\"update_frequency\":\"Nii kuidas vaja\","
        + "\"short_description\":\"TEST Klassifikaatori iseloomustus\",\"additional_information\":null,"
        + "\"main_resource_id\":349790,\"uri\":\"urn:fdc:riha.eesti.ee:2016:classifier:349790\","
        + "\"parent_uri\":null,\"template_version\":\"1.0.0\",\"creator\":\"-\",\"modifier\":\"35512121234\","
        + "\"creation_date\":\"2016-05-10T00:00:00\",\"modified_date\":\"2016-09-07T00:00:00\","
        + "\"finishing_description\":null,\"excellent\":false,"
        + "\"related_classifiers\":[{\"type\":\"preceding\",\"uri\":\"a:b\",\"description\":\"k1\"},"
        + "{\"type\":\"associated\",\"uri\":\"2\",\"description\":\"k2\"}],\"kind\":\"classifier\"}}";

    Response response = serviceUnderTest.postCGI(jsonWithBug);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));

    String uriWithColon = mrUpdated.getJson_content().get("related_classifiers").getAsJsonArray().get(0)
        .getAsJsonObject().get("uri").getAsString();

    assertEquals("a:b", uriWithColon);

  }

  @Test
  public void testUpdateDocument_whenNonExistentKind_thenError() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\"op\":\"put\"," + "\"path\": \"" + path + "\"," + "\"token\":\"testToken\",\"data\": "
        + "{\"kind\": \"infosystemTEST\", \"name\": \"TEST22\", \"short_name\":\"TEST_SHORT\"}" + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.KIND_NOT_FOUND, error.getErrcode());
    assertEquals(ErrorCodes.KIND_NOT_FOUND_MSG, error.getErrmsg());
  }

  @Ignore("trigger is modified, does not throw error anymore")
  @Test
  public void testUpdate_whenBadInfosystemState_thenTriggerError() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\"op\":\"put\"," + "\"path\": \"" + path + "\"," + "\"token\":\"testToken\",\"data\": "
        + "{\"infosystem_status\": \"nonExistentStatus\", \"inapproval\": true}" + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.SQL_TRIGGER_ERROR, error.getErrcode());
    assertTrue(error.getErrmsg().startsWith(ErrorCodes.SQL_TRIGGER_ERROR_MSG));
    // error from infosystem_trg() RAISE EXCEPTION
    assertTrue(error.getErrmsg().contains("ERROR: Invalid status"));
  }

  // @Test
  // public void testUpdateList() throws Exception {
  //
  // String testVersion = "xxxxxx123";
  // String path = pathToUse;
  // String data = "[{\"short_name\":\"" +
  // TestFinals.MAIN_RESOURCE_TEST_SHORT_NAME + "\", \"version\" : \"" + testVersion + "\"}]";
  // String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" + path
  // + "\",\"token\":\"testToken\", \r\n"
  // + "\"key\": \"short_name\", \"data\": " + data + "}";
  //
  // Response response = serviceUnderTest.postCGI(json);
  //
  // assertNotNull(response.getEntity());
  //
  // Map<String, Integer> result = TestHelper.getResultMap(response);
  // Integer numOfChanged = 2;
  // assertNotNull(result);
  // assertEquals(numOfChanged, result.get(Finals.OK));
  //
  // // test that properties were actually updated
  // Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));
  // Main_resource mrUpdated2 = genericDAO.find(Main_resource.class, idUnderTestList.get(1));
  //
  // assertEquals(testVersion, mrUpdated.getVersion());
  // assertEquals(testVersion, mrUpdated2.getVersion());
  //
  // JsonObject json_contentStored = mrUpdated.getJson_content();
  // JsonObject json_contentStored2 = mrUpdated2.getJson_content();
  //
  // assertEquals(testVersion, json_contentStored.get("version").getAsString());
  // assertEquals(testVersion, json_contentStored2.get("version").getAsString());
  //
  // assertEquals(mrUpdated.getModifier(), json_contentStored.get(Finals.MODIFIER).getAsString());
  // Date modifiedDateJson = DateHelper.FORMATTER.parse(
  // json_contentStored.get(Finals.MODIFIED_DATE).getAsString());
  // assertTrue("Dates aren't close enough to each other!",
  // Math.abs(mrUpdated.getModified_date().getTime() - modifiedDateJson.getTime()) < 1000);
  //
  // }

  @Test
  public void testUpdateList_cantUpdateVersionError() throws Exception {

    String testVersion = "xxxxxx123";
    String path = pathToUse;
    String data = "[{\"short_name\":\"" + TestFinals.MAIN_RESOURCE_TEST_SHORT_NAME + "\", \"version\" : \""
        + testVersion + "\"}]";
    String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" + path + "\",\"token\":\"testToken\", \r\n"
        + "\"key\": \"short_name\", \"data\": " + data + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.CAN_UPDATE_VERSION_HERE, error.getErrcode());
    assertEquals(ErrorCodes.CAN_UPDATE_VERSION_HERE_MSG, error.getErrmsg());
  }

  @Test
  public void testUpdateListByOwner() throws Exception {

    String testOwner = TestFinals.MAIN_RESOURCE_TEST_OWNER;
    String testShortName = "TEST_XZXC_123";
    String path = pathToUse;
    String data = "[{\"short_name\":\"" + testShortName + "\", \"owner\": \"" + testOwner + "\"}]";
    String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" + path + "\",\"token\":\"testToken\", \r\n"
        + "\"key\": \"owner\", \"data\": " + data + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 2;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // test that properties were actually updated
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));
    Main_resource mrUpdated2 = genericDAO.find(Main_resource.class, idUnderTestList.get(1));

    assertEquals(testShortName, mrUpdated.getShort_name());
    assertEquals(testShortName, mrUpdated2.getShort_name());

    JsonObject json_contentStored = mrUpdated.getJson_content();
    JsonObject json_contentStored2 = mrUpdated2.getJson_content();

    assertEquals(testShortName, json_contentStored.get("short_name").getAsString());
    assertEquals(testShortName, json_contentStored2.get("short_name").getAsString());

    assertEquals(mrUpdated.getModifier(), json_contentStored.get(Finals.MODIFIER).getAsString());

    Date modifiedDateJson = DateHelper.FORMATTER.parse(json_contentStored.get(Finals.MODIFIED_DATE).getAsString());

    assertEquals(mrUpdated.getModified_date().getTime(), modifiedDateJson.getTime());

  }

  @Test
  public void testUpdateListByFieldOnlyInJson_content() throws Exception {

    // test objects with test json fields
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreateJsonField));
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreateJsonField));

    // \"test_abc\":\"test_123\", \"test_num\":123

    // create test request

    String testJsonFieldName = "test_abc";
    String testJsonFieldValue = "test_123";

    String testOwner = "TEST_OWNER_ZXC";
    String testShortName = "TEST_XZXC_123";
    String path = pathToUse;
    String data = "[{\"" + testJsonFieldName + "\":\"" + testJsonFieldValue + "\", \"short_name\":\"" + testShortName
        + "\", \"ownerJsonTest\": \"" + testOwner + "\"}]";
    String json = "{\"op\":\"put\", \"path\": \"" + path + "\",\"token\":\"testToken\", \"key\": \""
        + testJsonFieldName + "\", \"data\": " + data + "}";

    // run test
    Response response = serviceUnderTest.postCGI(json);

    // assert results
    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 2;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // ignore first 2 elements created in beforeTest()
    // test that properties were actually updated
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(2));
    Main_resource mrUpdated2 = genericDAO.find(Main_resource.class, idUnderTestList.get(3));

    assertEquals(testShortName, mrUpdated.getShort_name());
    assertEquals(testShortName, mrUpdated2.getShort_name());

    JsonObject json_contentStored = mrUpdated.getJson_content();
    JsonObject json_contentStored2 = mrUpdated2.getJson_content();

    assertEquals(testShortName, json_contentStored.get("short_name").getAsString());
    assertEquals(testShortName, json_contentStored2.get("short_name").getAsString());

    assertEquals(testJsonFieldValue, json_contentStored.get(testJsonFieldName).getAsString());
    assertEquals(testJsonFieldValue, json_contentStored2.get(testJsonFieldName).getAsString());

    assertEquals(testOwner, json_contentStored.get("ownerJsonTest").getAsString());
    assertEquals(testOwner, json_contentStored2.get("ownerJsonTest").getAsString());

    assertEquals(mrUpdated.getModifier(), json_contentStored.get(Finals.MODIFIER).getAsString());

    Date modifiedDateJson = DateHelper.FORMATTER.parse(json_contentStored.get(Finals.MODIFIED_DATE).getAsString());

    assertEquals(mrUpdated.getModified_date().getTime(), modifiedDateJson.getTime());

  }

  @Test
  public void testUpdateListByFieldOnlyInJson_contentNumeric() throws Exception {

    // test objects with test json fields
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreateJsonField));
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreateJsonField));

    // \"test_abc\":\"test_123\", \"test_num\":123

    // create test request

    String testJsonFieldName = "test_num";
    int testJsonFieldValue = 123;

    String testOwner = "TEST_OWNER_ZXC";
    String testShortName = "TEST_XZXC_123";
    String path = pathToUse;
    String data = "[{\"" + testJsonFieldName + "\":" + testJsonFieldValue + ", \"short_name\":\"" + testShortName
        + "\", \"ownerJsonTest\": \"" + testOwner + "\"}]";
    String json = "{\"op\":\"put\", \"path\": \"" + path + "\",\"token\":\"testToken\", \"key\": \""
        + testJsonFieldName + "\", \"data\": " + data + "}";

    // run test
    Response response = serviceUnderTest.postCGI(json);

    // assert results
    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 2;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // ignore first 2 elements created in beforeTest()
    // test that properties were actually updated
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(2));
    Main_resource mrUpdated2 = genericDAO.find(Main_resource.class, idUnderTestList.get(3));

    assertEquals(testShortName, mrUpdated.getShort_name());
    assertEquals(testShortName, mrUpdated2.getShort_name());

    JsonObject json_contentStored = mrUpdated.getJson_content();
    JsonObject json_contentStored2 = mrUpdated2.getJson_content();

    assertEquals(testShortName, json_contentStored.get("short_name").getAsString());
    assertEquals(testShortName, json_contentStored2.get("short_name").getAsString());

    assertEquals(testJsonFieldValue, json_contentStored.get(testJsonFieldName).getAsInt());
    assertEquals(testJsonFieldValue, json_contentStored2.get(testJsonFieldName).getAsInt());

    assertEquals(testOwner, json_contentStored.get("ownerJsonTest").getAsString());
    assertEquals(testOwner, json_contentStored2.get("ownerJsonTest").getAsString());

    assertEquals(mrUpdated.getModifier(), json_contentStored.get(Finals.MODIFIER).getAsString());

    Date modifiedDateJson = DateHelper.FORMATTER.parse(json_contentStored.get(Finals.MODIFIED_DATE).getAsString());

    assertEquals(mrUpdated.getModified_date().getTime(), modifiedDateJson.getTime());

  }

  @Test
  public void testUpdateList_setNull() throws Exception {

    String testOwner = TestFinals.MAIN_RESOURCE_TEST_OWNER;
    String path = pathToUse;
    String data = "[{\"field_name\":null, \"owner\": \"" + testOwner + "\"}]";
    String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" + path + "\",\"token\":\"testToken\", \r\n"
        + "\"key\": \"owner\", \"data\": " + data + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 2;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // test that properties were actually updated
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));
    Main_resource mrUpdated2 = genericDAO.find(Main_resource.class, idUnderTestList.get(1));

    assertNull(mrUpdated.getField_name());
    assertNull(mrUpdated2.getField_name());

    JsonObject json_contentStored = mrUpdated.getJson_content();
    JsonObject json_contentStored2 = mrUpdated2.getJson_content();

    assertTrue(json_contentStored.get("field_name").isJsonNull());
    assertTrue(json_contentStored2.get("field_name").isJsonNull());

  }

  @Test
  public void testUpdateList_whenArchived_thenError() throws Exception {

    String dateJson = DateHelper.FORMATTER.format(new Date());
    JsonObject jsonObject = JsonHelper
        .getFromJson(TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING);
    jsonObject.addProperty("end_date", dateJson);

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonObject.toString()));

    // 2 elements created before already
    int id = idUnderTestList.get(2);
    String testOwner = TestFinals.MAIN_RESOURCE_TEST_OWNER;
    String path = pathToUse;
    String data = "[{\"field_name\":null, \"owner\": \"" + testOwner + "\"}]";
    String json = "{\r\n" + " \"op\":\"put\", \r\n" + " \"path\": \"" + path + "\",\"token\":\"testToken\", \r\n"
        + "\"key\": \"owner\", \"data\": " + data + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    System.out.println(error);
    assertNotNull(error);
    assertEquals(ErrorCodes.CANT_UPDATE_ARCHIVED, error.getErrcode());
    assertEquals(ErrorCodes.CANT_UPDATE_ARCHIVED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().contains(id + ""));

  }

}
