package ee.eesti.riha.rest.integration.main_resource;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiClassicService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiClassicServiceImpl_GET_LIMIT<T> {

  // general info here
  @Autowired
  WebClient webClient;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiClassicService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.MAIN_RESOURCE;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;

  @Before
  public void beforeTest() {
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, jsonToUseForCreate));
  }

  @After
  public void afterTest() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceUnderTest, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  @Test
  public void testGetList_limitWorks() throws Exception {

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST4fc3f25d-851d-4597-9e4c-cf05a8c8ea24");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST4fc3f25d-851d-4597-9e4c-cf05a8c8ea24");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST4fc3f25d-851d-4597-9e4c-cf05a8c8ea24");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST4fc3f25d-851d-4597-9e4c-cf05a8c8ea24");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    // send query
    Response response = serviceUnderTest.getMany(tableUnderTest, 2, null, null, null, null);

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

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json_content.addProperty("randomFieldABC", "test 123");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST5089bf02-be05-4aa8-858a-c3b75157e0d0");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    // send query
    Response response = serviceUnderTest.getMany(tableUnderTest, 3, null, null, null, null);

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
  public void testGetList_whenNoAuthToken_thenError() throws Exception {
    // create client with empty header field
    webClient.replaceHeader(Finals.X_AUTH_TOKEN, "");
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);

    // send query
    Response response = serviceUnderTest.getMany(tableUnderTest, 3, null, null, null, null);

    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.NO_HTTP_AUTH_TOKEN_PROVIDED, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.NO_HTTP_AUTH_TOKEN_PROVIDED_MSG));

    // restore token to enable cleanup
    webClient.replaceHeader(Finals.X_AUTH_TOKEN, "asdasd");
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
  }

}
