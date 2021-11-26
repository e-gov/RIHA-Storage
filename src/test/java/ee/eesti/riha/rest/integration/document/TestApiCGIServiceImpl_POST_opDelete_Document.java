package ee.eesti.riha.rest.integration.document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
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

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opDelete_Document<T> {

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

  // private static String testContentBeginning = "PD94b";
  private static String testContentBeginning = "<?xml version=\"1.0\"";
  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_DOCUMENT;

  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static Integer main_resourceId = null;
  
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
  public void testDelete1Item_whenValidCall_thenSuccess() throws Exception {

    Integer expectDeleted = 1;
    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\r\n" + "	\"op\":\"delete\", \r\n" + "\"token\":\"testToken\",	\"path\": \"" + path + "\" \r\n"
        + "}\r\n" + "";
    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    Map<String, Integer> result = TestHelper.getResultMap(response);
    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDelete_whenWrongTable_thenError() throws Exception {

    String path = "/db/" + TestFinals.NON_EXISTENT_TABLE + "/" + idUnderTestList.get(0);
    String json = "{\r\n" + "	\"op\":\"delete\", \r\n" + "\"token\":\"testToken\",	\"path\": \"" + path + "\" \r\n"
        + "}\r\n" + "";
    Response response = serviceUnderTest.postCGI(json);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testDelete_whenNonExistentId_thenReturn0() throws Exception {

    Integer expectDeleted = 0;
    String path = pathToUse + TestFinals.NON_EXISTENT_ID_ANY_TABLE;
    String json = "{\r\n" + "	\"op\":\"delete\", \r\n" + "\"token\":\"testToken\",	\"path\": \"" + path + "\" \r\n"
        + "}\r\n" + "";
    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    Map<String, Integer> result = TestHelper.getResultMap(response);
    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDelete_whenParameterPathIsNotValid_thenError() throws Exception {

    String path = pathToUse + "/test/test///";
    String json = "{\r\n" + "	\"op\":\"delete\", \r\n" + "\"token\":\"testToken\",	\"path\": \"" + path + "\" \r\n"
        + "}\r\n" + "";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testDeleteListWithPropertyWhoseNameMatchesWithPrimaryKeyField_shouldSucceed() throws Exception {

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    Integer expectDeleted = 2;
    // field must be existing field in matching table, or just id (will be
    // translated to right pk)
    String idField = "document_id";
    String idValues = "[" + idUnderTestList.get(0) + "," + idUnderTestList.get(1) + "]";
    String json = "{\"op\":\"delete\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"" + idField
        + "\": " + idValues + "}";
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    Map<String, Integer> result = TestHelper.getResultMap(response);
    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDeleteListByPropertyId_shouldSucceed() throws Exception {

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    Integer expectDeleted = 2;
    // field must be existing field in matching table, or just id (will be
    // translated to right pk)
    String idField = "id";
    String idValues = "[" + idUnderTestList.get(0) + "," + idUnderTestList.get(1) + "]";
    String json = "{\"op\":\"delete\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"" + idField
        + "\": " + idValues + "}";
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    Map<String, Integer> result = TestHelper.getResultMap(response);
    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDeleteListByUnknownColumn_thenError() throws Exception {

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    String idField = "TESTtestTEST";
    String idValues = "[" + idUnderTestList.get(0) + "," + idUnderTestList.get(1) + "]";
    String json = "{\"op\":\"delete\",\"path\":\"" + pathToUse + "\"," + "\"token\":\"testToken\",\"" + idField + "\":"
        + idValues + "}";
    Response response = serviceUnderTest.postCGI(json);
    System.out.println(json);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_CAN_NOT_FIND_COLUMN, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_CAN_NOT_FIND_COLUMN_MSG));
    System.out.println(json);
    System.out.println(error.getErrtrace());
    assertTrue(error.getErrtrace().equals(json));

  }

  @Test
  public void testDelete_whenIdNotProvidedInPathNorInsidePropertyId_thenError() throws Exception {
    // can not delete if don't have the id

    String json = "{\"op\":\"delete\"," + "\"token\":\"testToken\",\"path\":\"" + pathToUse + "\"" + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_GENERAL_SOMETHING_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_GENERAL_SOMETHING_MISSING_MSG));
    System.out.println(json);
    System.out.println(error.getErrtrace());
    assertTrue(error.getErrtrace().equals(json));

  }

  @Test
  public void testDeleteListByFieldOldId() throws Exception {

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    String oldId = "210881111";

    Integer expectDeleted = 2;
    String idField = "old_id";
    String idValues = "[" + oldId + "]";
    String json = "{\"op\":\"delete\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"" + idField
        + "\": " + idValues + "}";
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);

    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDeleteListByFieldOnlyInJsonContent() throws Exception {

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));

    Integer expectDeleted = 2;
    String idField = "fieldOnlyInJsonContentTest";
    String idValues = "[\"Gol\"]";
    String json = "{\"op\":\"delete\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"" + idField
        + "\": " + idValues + "}";
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);

    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDeleteListByFieldMime() throws Exception {

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    String mime = "application/digidocTEST";

    Integer expectDeleted = 2;
    String idField = "mime";
    String idValues = "[\"" + mime + "\"]";
    String json = "{\"op\":\"delete\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"" + idField
        + "\": " + idValues + "}";
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);

    assertNotNull(result);
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

}
