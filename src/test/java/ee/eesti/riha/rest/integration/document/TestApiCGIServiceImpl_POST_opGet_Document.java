package ee.eesti.riha.rest.integration.document;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import org.apache.commons.lang3.SystemUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opGet_Document<T> {

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
  private static String tableUnderTestMain_resource = TestFinals.MAIN_RESOURCE;
  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static String tableUnderTestData_object = TestFinals.DATA_OBJECT;
  private static String jsonToUseForCreateData_object = TestFinals.JSON_CONTENT_FOR_DATA_OBJECT_CORRECT_SAMPLE_AS_JSON_STRING;

  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_DOCUMENT;

  private static Integer main_resourceId = null;
  private static Integer data_objectId = null;
  
  @Before
  public void beforeTest() throws IOException {
    if (idUnderTestList.size() == 0) {
      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
      
      // create test Main_resource
      if (main_resourceId == null) {
        main_resourceId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData,
            tableUnderTestMain_resource, jsonToUseForCreateMain_resource);
      }
      if (data_objectId == null) {
        // add created Main_resource id to json
        JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreateData_object);
        jsonObject.addProperty("main_resource_id", main_resourceId);
        jsonToUseForCreateData_object = jsonObject.toString();
        // create Data_object from modified json
        data_objectId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData,
            tableUnderTestData_object, jsonToUseForCreateData_object);
      }
      
      JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
      jsonObject.addProperty("data_object_id", data_objectId);
      jsonToUseForCreate = jsonObject.toString();
      
      // create Document from modifed json
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          jsonToUseForCreate));
      
     }
  }

  @AfterClass
  public static void afterClass() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestMain_resource,
        main_resourceId);
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestData_object,
        data_objectId);
  }

  @Test
  public void testGetAll() throws Exception {
    Assume.assumeTrue("Test works only in server", SystemUtils.IS_OS_LINUX);
    String path = pathToUse;
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\", \"limit\":100}";
    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    assertFalse(jsonContentList.isEmpty());

  }

  @Test
  public void testGetCGI_whenWrongTable_thenError() throws Exception {

    String path = "/db/" + TestFinals.NON_EXISTENT_TABLE + "/";
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  // for special queries path is not required FIXME TODO

  @Test
  public void testGetCGI_whenJsonMissingRequiredParPath_thenError() throws Exception {

    String json = "{\"op\": \"get\", \"token\":\"testToken\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(error.getErrmsg().contains(Finals.PATH));
    assertTrue(error.getErrtrace().contains(json));

  }

  @Test
  public void testGetById() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    System.out.println(json);
    Response response = serviceUnderTest.postCGI(json);
    JsonObject jsonContent = null;
    try {
      jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    } catch (JsonSyntaxException e) {
      fail("JsonObject expected");
    }
    assertNotNull(jsonContent);

  }

  @Test
  public void testGetById_whenWrongTable_thenError() throws Exception {

    String path = "/db/" + TestFinals.NON_EXISTENT_TABLE + "/" + idUnderTestList.get(0);
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    System.out.println(json);
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testGetById_whenNonExistentId_thenError() throws Exception {

    String path = pathToUse + TestFinals.NON_EXISTENT_ID_ANY_TABLE;
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    System.out.println(json);
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID_MSG, error.getErrmsg());
    System.out.println("ERGT");
    System.out.println(error.getErrtrace());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_ID_ANY_TABLE.toString()));

  }

  @Test
  public void testGet_whenParameterPathValueIsNotValid_thenError() throws Exception {

    String path = pathToUse + "/test/test///";
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testGetById_whenParameterPathValueIsNotValid_whenIdIsNotNumber_thenError() throws Exception {

    String path = pathToUse + "abc/";
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

}
