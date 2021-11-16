package ee.eesti.riha.rest.integration.main_resource;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
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

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_GET_opGet_combineParameters<T> {

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

  // TESTING filter and sort
  // sort field exist as db table field
  // sort by Asc
  @Test
  public void testGetList_testingFilterANDSort_allFieldsAreDBFields_whenSortAsc_thenElementsAscOrder() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 4);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 2);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null,
        "name,=,TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585,access_restriction,>=,4", "access_restriction", null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    // System.out.println(jsonContentList);
    expected = 4;
    assertEquals(expected, jsonContentList.get(0).get("access_restriction").getAsInt());

  }

  // TESTING filter and sort
  // sort field exist as db table field
  // sort by Desc
  @Test
  public void testGetList_testingFilterANDSort_allFieldsAreDBFields_whenSortDesc_thenElementsDescOrder()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 4);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 2);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null,
        "name,=,TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585,access_restriction,>=,4", "-access_restriction", null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    // System.out.println(jsonContentList);
    expected = 6;
    assertEquals(expected, jsonContentList.get(0).get("access_restriction").getAsInt());

  }

  // TESTING filter and sort
  // sort field does not exist as db table field (so query over json_content
  // field)
  // sort by Asc
  @Test
  public void testGetList_testingFilterANDSort_sortFieldNotDBField_whenSortAsc_thenCorrectResult() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST 3d2db148-4998-46b9-9189-44ae2a07a856");
    json_content.addProperty("access_restrictionTEST123", 200);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 3d2db148-4998-46b9-9189-44ae2a07a856");
    json_content.addProperty("access_restrictionTEST123", 50);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST 3d2db148-4998-46b9-9189-44ae2a07a856");
    json_content.addProperty("access_restrictionTEST123", 300);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null,
        "name,=,TEST 3d2db148-4998-46b9-9189-44ae2a07a856,access_restrictionTEST123,>=,200",
        "access_restrictionTEST123", null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    // System.out.println(jsonContentList);
    expected = 200;
    assertEquals(expected, jsonContentList.get(0).get("access_restrictionTEST123").getAsInt());

  }

  // TESTING filter and sort
  // sort field does not exist as db table field (so query over json_content
  // field)
  // sort by Desc
  @Test
  public void testGetList_testingFilterANDSort_sortFieldNotDBField_whenSortDesc_thenCorrectResult() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restrictionTEST123", 100);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restrictionTEST123", 200);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restrictionTEST123", 300);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    // send query
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null,
        "name,=,TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585,access_restrictionTEST123,>=,200",
        "-access_restrictionTEST123", null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    // System.out.println(jsonContentList);
    expected = 300;
    assertEquals(expected, jsonContentList.get(0).get("access_restrictionTEST123").getAsInt());

  }

}
