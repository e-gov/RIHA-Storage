package ee.eesti.riha.rest.integration.main_resource;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.service.ApiCGIService;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opCount {

  // general info here
  @Autowired
  WebClient webClient;

  @Autowired
  ApiGenericDAO genericDAO;

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

  @Ignore("Currently not using default limit ")
  @Test
  public void testCountNoLimit_thenLimitsToMaxAllowed() throws Exception {

    String path = pathToUse;
    String json = "{\"op\": \"count\", \"token\":\"testToken\", \"path\": \"" + path + "\"}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> resultCount = TestHelper.getResultMap(response);

    int countFromService = resultCount.get(Finals.OK);

    assertEquals(Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED, countFromService);
  }

  @Ignore("Currently not using default limit ")
  @Test
  public void testCountLimitOverMax_thenLimitsToMaxAllowed() throws Exception {

    String path = pathToUse;
    int limit = 1000000;
    String json = "{\"op\": \"count\", \"token\":\"testToken\", \"path\": \"" + path + "\"" + ", \"limit\": " + limit
        + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Integer> resultCount = TestHelper.getResultMap(response);

    int countFromService = resultCount.get(Finals.OK);

    assertEquals(Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED, countFromService);
  }

  @Test
  public void testCountAll() throws IOException {
    String path = pathToUse;
    int limit = Finals.COUNT_ALL_LIMIT;
    String json = "{\"op\": \"count\", \"path\": \"" + path + "\"" + ",\"token\":\"testToken\", \"limit\": " + limit
        + "}";

    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    Map<String, Integer> resultCount = TestHelper.getResultMap(response);

    int countFromService = resultCount.get(Finals.OK);
    int countFromDB = genericDAO.findCount(Finals.getClassRepresentingTable(tableUnderTest));

    assertTrue(countFromService >= 1);
    assertEquals(countFromDB, countFromService);
  }

  @Test
  public void testCountLimitZero() throws IOException {
    String path = pathToUse;
    int limit = 0;
    String json = "{\"op\": \"count\", \"path\": \"" + path + "\"" + ",\"token\":\"testToken\", \"limit\": " + limit
        + "}";
    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());

    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);
    assertEquals(limit, countFromService);
  }

  // based on TestApiCGIServiceImpl_POST_opGet_FILTER.
  // testGetList_testingFilter_fieldExistAsDBField_stringComparisonShouldWork()

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // STRING comparison
  // use of URL ENCODING
  @Test
  public void testCount_withFilter_noOffset() throws Exception {

    String testName = "TEST_XXXXXX_123_ZXCVBN";
    String filterToTest = "[\"name\",\"=\",\"" + testName + "\"]";

    String json = "{\r\n" + "	\"op\":\"count\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // System.out.println(json);
    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TESTaae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", testName);
    json2 = json_content.toString();

    int expected = 5;
    for (int i = 0; i < expected; i++) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          json2));
    }

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);
    assertEquals(expected, countFromService);

  }

  @Test
  public void testCount_withFilter_zeroOffset() throws Exception {

    String testName = "TEST_XXXXXX_123_ZXCVBN";
    String filterToTest = "[\"name\",\"=\",\"" + testName + "\"]";
    int offset = 0;
    String json = "{\r\n" + "	\"op\":\"count\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + ", \"offset\": " + offset + "}";

    // System.out.println(json);
    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TESTaae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", testName);
    json2 = json_content.toString();

    int expected = 5;
    for (int i = 0; i < expected; i++) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          json2));
    }

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);
    assertEquals(expected, countFromService);

  }

  @Test
  public void testCount_withFilter_offset() throws Exception {

    String testName = "TEST_XXXXXX_123_ZXCVBN";
    String filterToTest = "[\"name\",\"=\",\"" + testName + "\"]";
    int offset = 3;
    String json = "{\r\n" + "	\"op\":\"count\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + ", \"offset\": " + offset + "}";

    // System.out.println(json);
    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TESTaae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", testName);
    json2 = json_content.toString();

    int expected = 5;
    for (int i = 0; i < expected; i++) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          json2));
    }

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);
    assertEquals(expected - offset, countFromService);

  }

  @Test
  public void testCount_withFilter_offsetGreaterThanResult() throws Exception {

    String testName = "TEST_XXXXXX_123_ZXCVBN";
    String filterToTest = "[\"name\",\"=\",\"" + testName + "\"]";
    int offset = 7;
    String json = "{\r\n" + "	\"op\":\"count\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + ", \"offset\": " + offset + "}";

    // System.out.println(json);
    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TESTaae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", testName);
    json2 = json_content.toString();

    int expected = 5;
    for (int i = 0; i < expected; i++) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          json2));
    }

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);

    expected = 0;
    assertEquals(expected, countFromService);

  }

  @Test
  public void testCreatedTestDataHasNoEndDateAdded() {
    Main_resource mr = (Main_resource) genericDAO.find(Main_resource.class, idUnderTestList.get(0));

    assertNull(mr.getEnd_date());
  }

}
