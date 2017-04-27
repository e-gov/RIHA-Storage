package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import ee.eesti.riha.rest.auth.AuthServiceImpl;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_GET_opGet_LIMIT<T> {

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
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
  }

  @After
  public void afterTest() {
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  @Test
  public void testGetList_limitWorks() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST 1b09a1d0-46f1-4737-825d-10716700e75c");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 1b09a1d0-46f1-4737-825d-10716700e75c");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST 1b09a1d0-46f1-4737-825d-10716700e75c");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST 1b09a1d0-46f1-4737-825d-10716700e75c");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", 2, null, null, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    // System.out.println(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());

  }

  @Test
  public void testGetList_limitWorks2() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json_content.addProperty("randomFieldABC", "test 123");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", 3, null, null, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    // System.out.println(jsonContentList);
    int expected = 3;
    assertEquals(expected, jsonContentList.size());

  }

  @Ignore("Token not required with GET")
  @Test
  public void testGetList_noAuthToken_thenError() throws Exception {

    String path = pathToUse;

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "", 3, null, null, null, null);

    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.NO_AUTH_TOKEN_PROVIDED, error.getErrcode());
    assertEquals(ErrorCodes.NO_AUTH_TOKEN_PROVIDED_MSG, error.getErrmsg());

  }

  @Ignore("Using fake")
  @Test
  public void testGetList_with3rdPartyAuthValidation_expectCantConnect() throws IOException {
    // turn off fake validation
    serviceUnderTest.setAuthService(null);
    String path = pathToUse;
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);
    // TODO currently expect that 3rd party token validation service not found
    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertEquals(ErrorCodes.CANT_CONNECT_TO_AUTH, error.getErrcode());
    assertEquals(ErrorCodes.CANT_CONNECT_TO_AUTH_MSG, error.getErrmsg());

    // turn on fake validation
    serviceUnderTest.setAuthService(new AuthServiceImpl());
  }
}
