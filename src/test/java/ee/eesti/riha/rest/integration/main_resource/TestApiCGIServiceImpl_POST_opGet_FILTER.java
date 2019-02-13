package ee.eesti.riha.rest.integration.main_resource;

import static ee.eesti.riha.rest.logic.util.DateHelper.DATE_FORMAT_IN_JSON;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.model.readonly.Kind;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opGet_FILTER<T> {

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

  @AfterClass
  public static void afterClass() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // STRING comparison
  // use of URL ENCODING
  @Test
  public void testGetList_testingFilter_fieldExistAsDBField_stringComparisonShouldWork() throws Exception {

    String filterToTest = "[\"name\",\"=\",\"TESTaae7c00e-0d6a-49b0-a80d-9e7ff4578585\"]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
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
    json_content.addProperty("name", "TEST4e54a222-d687-475c-a702-13ede0481bb4");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // INTEGER comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_numComparisonShouldWork() throws Exception {

    String filterToTest = "[[\"name\",\"=\",\"TEST11e7c00e-0d6a-49b0-a80d-9e7ff4578585\"],[\"access_restriction\",\">=\",\"4\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 5);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 4);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json_content.addProperty("access_restriction", 1);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = 3;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // STRING and INTEGER comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_stringAndNumComparisonShouldWork() throws Exception {

    String filterToTest = "[[\"name\",\"=\",\"TEST37e99c18-b4b3-4b31-9078-6d0a1694f23b\"],[\"access_restriction\",\">=\",\"4\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST84a0618c-b5eb-4d24-9a8e-0b480b420b77");
    json_content.addProperty("access_restriction", 5);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST37e99c18-b4b3-4b31-9078-6d0a1694f23b");
    json_content.addProperty("access_restriction", 4);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST37e99c18-b4b3-4b31-9078-6d0a1694f23b");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TESTf67355e6-0eaf-4427-8a96-2e252808111a");
    json_content.addProperty("access_restriction", 1);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // DATE comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_dateComparisonShouldWork() throws Exception {

    String filterToTest = "[[\"name\",\"=\",\"TEST11e7c00e-0d6a-49b0-a80d111111\"],[\"start_date\",\"=\",\"2111-11-11T11:11:11\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d111111");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d111111");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d111111");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d111111");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d111111");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // DATE comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_dateComparisonShouldWork2() throws Exception {

    String filterToTest = "[[\"name\",\"=\",\"TEST11e7c00e-0d6a-49b0-a80d222222\"],[\"start_date\",\">\",\"2111-11-11T11:11:11\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d222222");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d222222");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d222222");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d222222");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d222222");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());

    String expectedDate = "2111-11-11T20:00:00";
    // System.out.println(jsonContentList.get(0).get("start_date").getAsString());
    assertEquals(expectedDate, jsonContentList.get(0).get("start_date").getAsString());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // DATE comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_dateComparison_whenOnlyDateGiven() throws Exception {

    // if no time is given then it will be set to 00:00:00
    // e.g 2111-11-11 becomes 2111-11-11 00:00:00
    // TODO should date comparison work differently?
    String filterToTest = "[[\"name\",\"=\",\"TEST11e7c00e-0d6a-49b0-a80d3333333\"],[\"start_date\",\"=\",\"2111-11-11\"]]";
    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d3333333");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d3333333");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d3333333");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d3333333");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d3333333");
    json_content.addProperty("start_date", "2111-11-11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    String json6 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json6, JsonObject.class);
    json_content.addProperty("name", "TEST11e7c00e-0d6a-49b0-a80d3333333");
    json_content.addProperty("start_date", "2111-11-11T00:00:00");
    json6 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json6));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    // int expected = 3;

    // only json5 and json6 have the start_date in same format
    int expected = 2;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // date and string comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_combinedComparisonShouldWork() throws Exception {

    String filterToTest = "[[\"name\",\"=\",\"TEST 6dc7e73d-2935-4336-95dd-8df65bb163f3\"],[\"start_date\",\">=\",\"2111-11-11T11:11:11\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST 6dc7e73d-2935-4336-95dd-8df65bb163f3");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 6dc7e73d-2935-4336-95dd-8df65bb163f3");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST 6dc7e73d-2935-4336-95dd-8df65bb163f3");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST 6dc7e73d-2935-4336-95dd-8df65bb163f3");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TESTc5e57bc3-b7fd-4b35-b3cf-29da9e1d50b1");
    json_content.addProperty("start_date", "2000-03-05T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 3;
    assertEquals(expected, jsonContentList.size());

  }

  // at leas one field in filter does not exist as db table field (means that
  // QUERY IS MADE OVER JSON_CONTENT FIELD)
  // STRING comparison
  // presumes that entries are returned by their create order
  @Test
  public void testGetList_testingFilter_atLeastOneFieldsExistOnlyInJsonContent_stringComparisonShouldWork()
      throws Exception {

    String strNotDBField = "STRINGfield11";
    String strNotDBField2 = "STRINGfield22";

    String filterToTest = "[[\"name\",\"=\",\"TEST02fcf723-4b6a-4901-8d96-9a7b83053d7b\"],[\"" + strNotDBField
        + "\",\"=\",\"abc\"],[\"" + strNotDBField2 + "\",\">\",\"a\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST02fcf723-4b6a-4901-8d96-9a7b83053d7b");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "c");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST02fcf723-4b6a-4901-8d96-9a7b83053d7b");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "b");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST02fcf723-4b6a-4901-8d96-9a7b83053d7b");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "a");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST02fcf723-4b6a-4901-8d96-9a7b83053d7b");
    json_content.addProperty(strNotDBField, "noMatch123");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST02fcf723-4b6a-4901-8d96-9a7b83053d7b");
    // has no first field
    json_content.addProperty(strNotDBField2, "b");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    // System.out.println(filterToTest);
    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());

    String expectedStr = "abc";
    String expectedStr2 = "c";
    assertEquals(expectedStr, jsonContentList.get(0).get(strNotDBField).getAsString());
    assertEquals(expectedStr2, jsonContentList.get(0).get(strNotDBField2).getAsString());
    expectedStr2 = "b";
    assertEquals(expectedStr, jsonContentList.get(1).get(strNotDBField).getAsString());
    assertEquals(expectedStr2, jsonContentList.get(1).get(strNotDBField2).getAsString());

  }

  // at leas one field in filter does not exist as db table field (means that
  // QUERY IS MADE OVER JSON_CONTENT FIELD)
  // INTEGER comparison
  // presumes that entries are returned by their create order
  @Test
  public void testGetList_testingFilter_atLeastOneFieldsExistOnlyInJsonContent_numComparisonShouldWork()
      throws Exception {

    String numNotDBField = "NUMfield11";
    String numNotDBField2 = "NUMfield22";

    String filterToTest = "[[\"name\",\"=\",\"TEST108d4a2b-1418-444a-9310-369af00d202b\"],[\"" + numNotDBField
        + "\",\">=\",\"2\"],[\"" + numNotDBField2 + "\",\"<\",\"5\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-1418-444a-9310-369af00d202b");
    json_content.addProperty(numNotDBField, 10);
    json_content.addProperty(numNotDBField2, 2000000);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-1418-444a-9310-369af00d202b");
    json_content.addProperty(numNotDBField, 2);
    json_content.addProperty(numNotDBField2, 1);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-1418-444a-9310-369af00d202b");
    json_content.addProperty(numNotDBField, 5);
    json_content.addProperty(numNotDBField2, 2);
    json3 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-1418-444a-9310-369af00d202b");
    json_content.addProperty(numNotDBField, 8);
    json_content.addProperty(numNotDBField2, 3);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-1418-444a-9310-369af00d202b");
    // has no first field
    json_content.addProperty(numNotDBField2, 4);
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 3;
    assertEquals(expected, jsonContentList.size());

    int expected1 = 2;
    int expected2 = 1;
    assertEquals(expected1, jsonContentList.get(0).get(numNotDBField).getAsInt());
    assertEquals(expected2, jsonContentList.get(0).get(numNotDBField2).getAsInt());
    expected1 = 5;
    expected2 = 2;
    assertEquals(expected1, jsonContentList.get(1).get(numNotDBField).getAsInt());
    assertEquals(expected2, jsonContentList.get(1).get(numNotDBField2).getAsInt());
    expected1 = 8;
    expected2 = 3;
    assertEquals(expected1, jsonContentList.get(2).get(numNotDBField).getAsInt());
    assertEquals(expected2, jsonContentList.get(2).get(numNotDBField2).getAsInt());

  }

  @Test
  public void testGetList_allFieldsAreDBFields_operatorLike_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"name\",\"like\",\"%777z-f5j6%\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-777z-f5j6h-9310-369af00d202b");
    json1 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST108d4a2b-777z-f5j6h-9310-369af00d202b");
    json2 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    assertEquals("TEST108d4a2b-777z-f5j6h-9310-369af00d202b", jsonContentList.get(0).get("name").getAsString());

  }

  @Test
  public void testGetList_DBField_asInt_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"old_id\",\"=\",123456789]]";
    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("old_id", "123456789");
    json1 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("old_id", "123456789");
    json2 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    assertEquals(123456789, jsonContentList.get(0).get("old_id").getAsInt());

  }

  @Test
  public void testGetList_fieldInJson_notAsString_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"json_num\",\"=\",123456789], " + "[\"excellent\",\"=\",false], [\"state\",\"=\",\"X\"]]";
    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("json_num", "123456789");
    json1 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("json_num", "123456789");
    json2 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    assertEquals(123456789, jsonContentList.get(0).get("json_num").getAsInt());

  }

  @Test
  public void testGetList_fieldInJson_operator_ilike_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"name\",\"ilike\",\"%Eesti%\"], [\"test_string\",\"ilike\",\"%string%\"]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("test_string", "RanDom StrIng WoWoWoo");
    json1 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("test_string", "RanDom StrIng WoWoWoo");
    json2 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    assertEquals("RanDom StrIng WoWoWoo", jsonContentList.get(0).get("test_string").getAsString());

  }

  @Test
  public void testGetList_fieldInJson_operator_jsonArrayContains_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"test_array\",\"?&\",[\"dfg\"]]]";

    String json = "{\r\n" + "	\"op\":\"get\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"filter\": " + filterToTest + "}";

    // prepare test items
    String testArray = "[\"asd\", \"dfg\", \"fgh\"]";
    JsonArray jsonArray = JsonHelper.GSON.fromJson(testArray, JsonArray.class);
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.add("test_array", jsonArray);
    json1 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());

  }

  @Test
  public void testGetList_filter_nullOrGreater_thenCorrectResult() throws Exception {
    String dateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).format(new Date());
    String filterToTest = "[[\"end_date\",\"null_or_>\",\"" + dateJson + "\"]]";

    String json = "{\"op\":\"get\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\",	\"filter\": "
        + filterToTest + ", \"limit\":1}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    assertNull(jsonContentList.get(0).get("end_date"));

  }

  @Test
  public void testGetList_filter_nullOrGreaterInJson_thenCorrectResult() throws Exception {
    String dateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).format(new Date());
    String filterToTest = "[[\"end_date\",\"null_or_>\",\"" + dateJson + "\"], [\"uses_ads\", \"=\",true]]";

    String json = "{\"op\":\"get\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\",	\"filter\": "
        + filterToTest + ", \"limit\":1}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    assertNull(jsonContentList.get(0).get("end_date"));

  }

  @Test
  public void testGetList_filter_nullOrGreater_withNewVersion_thenCorrectResult() throws Exception {

    Main_resource mr = (Main_resource) genericDAO.find(Main_resource.class, idUnderTestList.get(0));

    // create test data
    String jsonCreate = "{\"op\":\"newversion\", \"new_version\":\"v2\",\"path\": \"" + pathToUse + "\", \"uri\":\""
        + mr.getUri() + "\", \"token\":\"testToken\"}";

    Response createResponse = serviceUnderTest.postCGI(jsonCreate);
    assertNotNull(createResponse.getEntity());
    String s = TestHelper.readStream((InputStream) createResponse.getEntity());
    System.out.println(s);

    // get just archived version, offset == 1 (ignore current version)
    FilterComponent fc = new FilterComponent("uri", "=", mr.getUri());
    List<Main_resource> justArchivedList = genericDAO.find(Main_resource.class, 1, 1, Arrays.asList(fc),
        "-creation_date");
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    idUnderTestList.add(justArchived.getMain_resource_id());
    assertNotNull(justArchived.getEnd_date());

    // test
    String dateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).format(new Date());
    String filterToTest = "[[\"end_date\",\"null_or_>\",\"" + dateJson + "\",\"uri\",\"=\",\"" + mr.getUri() + "\"]]";

    String json = "{\"op\":\"get\",\"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\",	\"filter\": "
        + filterToTest + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    // current should be null
//    assertNull(jsonContentList.get(0).get("end_date"));
    assertTrue(jsonContentList.get(0).get("end_date").isJsonNull());

  }

  @Test
  public void testGetList_filterOperatorOverJson_isNotNull_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"contact_persons\",\"isnotnull\",null]]";

    String json = "{\"op\":\"get\", \"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\",	\"limit\":1, \"filter\": " + filterToTest + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    assertNotNull(jsonContentList.get(0).get("contact_persons"));

  }

  @Test
  public void testGetList_filterOperatorOverJson_isNull_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"contact_persons\",\"isnull\",null]]";

    String json = "{\"op\":\"get\", \"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\",	\"limit\":1, \"filter\": " + filterToTest + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    assertNull(jsonContentList.get(0).get("contact_persons"));

  }

  @Test
  public void testGetList_filterOperator_isNotNull_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"end_date\",\"isnotnull\",null]]";

    String json = "{\"op\":\"get\", \"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\",	\"limit\":1, \"filter\": " + filterToTest + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    assertNotNull(jsonContentList.get(0).get("end_date"));

  }

  @Test
  public void testGetList_filterOperator_isNull_thenCorrectResult() throws Exception {

    String filterToTest = "[[\"end_date\",\"isnull\",null]]";

    String json = "{\"op\":\"get\", \"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\",	\"limit\":1, \"filter\": " + filterToTest + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());
    assertNull(jsonContentList.get(0).get("end_date"));

  }

  @Test
  public void testGetList_testingFilter_byKindId() throws Exception {

    // get existing kind
    Kind kind = ((List<Kind>) genericDAO.find(Kind.class, 1, 0, null, null)).get(0);

    String filterToTest = "[\"kind_id\",\"=\"," + kind.getKind_id() + "]";

    String json = "{\"op\":\"get\"," + "\"path\": \"" + pathToUse + "\", "
        + "\"token\":\"testToken\",\"limit\":10, \"filter\": " + filterToTest + "}";

    // send query
    Response response = serviceUnderTest.postCGI(json);

    List<Main_resource> main_resources = TestHelper.getObjectsFromClient(response);

    assertNotNull(main_resources);
    int expected = 10;
    assertEquals(expected, main_resources.size());

  }

  // at leas one field in filter does not exist as db table field (means that
  // QUERY IS MADE OVER JSON_CONTENT FIELD)
  // DATE comparison
  // presumes that entries are returned by their create order
  @Ignore("TODO")
  @Test
  public void testGetList_testingFilter_atLeastOneFieldsExistOnlyInJsonContent_dateComparisonShouldWork()
      throws Exception {
  }

}
