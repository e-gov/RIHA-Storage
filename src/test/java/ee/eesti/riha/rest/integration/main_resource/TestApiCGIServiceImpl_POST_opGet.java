package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opGet<T> {

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

  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_MAIN_RESOURCE;

  @Before
  public void beforeTest() {
    if (idUnderTestList.size() == 0) {
      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
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
  }

  @Ignore("Currently not using default limit ")
  @Test
  public void testGetAll_thenLimitationToNumOfElementsReturnedWorks() throws Exception {

    String path = pathToUse;
    String json = "{\"op\": \"get\", \"token\":\"testToken\", \"path\": \"" + path + "\"}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED;
    assertEquals(expected, jsonContentList.size());

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

  @Test
  public void testGetById_withFields() throws Exception {
    String fields = "[name, short_name, kind]";
    String path = pathToUse + idUnderTestList.get(0);
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\", \"fields\":" + fields + "}";
    System.out.println(json);
    Response response = serviceUnderTest.postCGI(json);
    JsonObject jsonContent = null;
    try {
      jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    } catch (JsonSyntaxException e) {
      fail("JsonObject expected");
    }
    assertNotNull(jsonContent);
    int expectedNumOfFields = 3;
    assertEquals(expectedNumOfFields, jsonContent.entrySet().size());
  }

  @Test
  public void testGetAll_withFields() throws Exception {
    String fields = "[name, short_name, kind_id]";
    String path = pathToUse;
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\", " + "\"token\":\"testToken\", \"fields\":" + fields
        + ", \"limit\":200}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    // int expected = Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED;
    int expected = 200;
    assertEquals(expected, jsonContentList.size());

    int expectedNumOfFields = 3;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withWrongFields() throws Exception {
    String path = pathToUse;
    String fields = "[name22, asdfasd, cvbn]";
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\", " + "\"token\":\"testToken\", \"fields\":" + fields
        + ", \"limit\":200}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    int expectedNumOfFields = 0;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withCommaSeparated_thenJsonNotValid() throws Exception {
    String path = pathToUse;
    String fields = "name, kind";
    String json = "{\"op\": \"get\", \"path\": \"" + path + "\", " + "\"token\":\"testToken\", \"fields\":" + fields
        + ", \"limit\":200}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_NOT_VALID_JSON, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG));
    assertTrue(error.getErrtrace().contains(json));

  }

}
