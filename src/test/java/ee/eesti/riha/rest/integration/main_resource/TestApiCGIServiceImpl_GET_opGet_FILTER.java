package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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
public class TestApiCGIServiceImpl_GET_opGet_FILTER<T> {

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

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // STRING comparison
  // use of URL ENCODING
  @Test
  public void testGetList_testingFilter_fieldExistAsDBField_whenFieldStringAndOpEqual_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 4e54a222-d687-475c-a702-13ede0481bb4");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST" + TestFinals.URLENCODING_SPACE
        + "4e54a222-d687-475c-a702-13ede0481bb4";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_when1IntegerFieldAND1OpLargerEqualTo_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST75560cfe-927e-41bd-8292-1f45aa9f1111");
    json_content.addProperty("access_restriction", 5);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST75560cfe-927e-41bd-8292-1f45aa9f1111");
    json_content.addProperty("access_restriction", 4);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST75560cfe-927e-41bd-8292-1f45aa9f1111");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST75560cfe-927e-41bd-8292-1f45aa9f1111");
    json_content.addProperty("access_restriction", 1);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST75560cfe-927e-41bd-8292-1f45aa9f1111,"
        + "access_restriction," + TestFinals.URLENCODING_LARGERTHANEQ + ",4";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_when1StringField1IntegerFieldAND1OpEqual1OpLargerEqualTo_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST 1e54da7f-745b-478f-a258-69b9cb7439d7");
    json_content.addProperty("access_restriction", 5);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST a54b528a-42b1-4273-abdb-35b66be74fec");
    json_content.addProperty("access_restriction", 4);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST a54b528a-42b1-4273-abdb-35b66be74fec");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST 3e760a9b-c84d-49bf-9369-8dad76ad7471");
    json_content.addProperty("access_restriction", 1);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST" + TestFinals.URLENCODING_SPACE
        + "a54b528a-42b1-4273-abdb-35b66be74fec,access_restriction," + TestFinals.URLENCODING_LARGERTHANEQ + ",4";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_when1DateFieldAND1OpEqual_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d,start_date," + TestFinals.URLENCODING_EQUAL
        + ",2111-11-11T11:11:11";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_when1DateFieldAND1OpLargernThan_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TESTa1c6dc07-6cae-4df0-bd3d-7ce44ae8726a");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64,start_date," + TestFinals.URLENCODING_LARGERTHAN
        + ",2111-11-11T11:11:11";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_when1DateFieldAND1OpEqual_whenOnlyDateGiven_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc5111");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc5111");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc5111");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc5111");
    json_content.addProperty("start_date", "2111-11-11T00:00:00");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST296c0472-8205-441c-86cd-cfdde683477f");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc5111,start_date," + TestFinals.URLENCODING_EQUAL + ",2111-11-11";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    // int expected = 3;

    // only json4 has the same timestamp with filter
    int expected = 1;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // date and string comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_when1StringField1DateFieldAND1OpEqual1OpLargerEqualTo_thenCorrectResult()
      throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST296c0472-8205-441c-86cd-cfdde683477f");
    json_content.addProperty("start_date", "2000-03-05T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d,start_date," + TestFinals.URLENCODING_LARGERTHANEQ
        + ",2111-11-11T11:11:11";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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

    String path = pathToUse;

    String strNotDBField = "STRINGfield11";
    String strNotDBField2 = "STRINGfield22";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "c");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "b");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "a");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "noMatch123");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    // has no first field
    json_content.addProperty(strNotDBField2, "b");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    // send query
    String filterToTest = "name,=,TEST463ff153-cc49-4af2-ab73-41c6b04990c0," + strNotDBField + ","
        + TestFinals.URLENCODING_EQUAL + ",abc," + strNotDBField2 + "," + TestFinals.URLENCODING_LARGERTHAN + ",a";
    // System.out.println(filterToTest);
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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

    String path = pathToUse;

    String numNotDBField = "NUMfield11";
    String numNotDBField2 = "NUMfield22";

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 10);
    json_content.addProperty(numNotDBField2, 2000000);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 2);
    json_content.addProperty(numNotDBField2, 1);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 5);
    json_content.addProperty(numNotDBField2, 2);
    json3 = json_content.toString();
    // 11111
    // 11112
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 8);
    json_content.addProperty(numNotDBField2, 3);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    // has no first field
    json_content.addProperty(numNotDBField2, 4);
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json5));

    String filterToTest = "name,=,TEST21e251bf-0098-4e3a-9844-f2765974a15c," + numNotDBField + ","
        + TestFinals.URLENCODING_LARGERTHANEQ + ",2," + numNotDBField2 + "," + TestFinals.URLENCODING_LESSTHAN + ",5";
    // System.out.println(filterToTest);
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

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
    // System.out.println(jsonContentList.get(2).get(numNotDBField).getAsString());
    // System.out.println(jsonContentList.get(2).get(numNotDBField2).getAsString());

  }

  @Test
  public void testGetList_testingFilterWithOffset() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST aae7c00e-0d6a-49b0-a80d-9e7ff4578585");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 4e54a222-d687-475c-a702-13ede0481bb4");
    json2 = json_content.toString();

    int expected = 5;
    for (int i = 0; i < expected; i++) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          json2));
    }

    int offset = 3;
    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST" + TestFinals.URLENCODING_SPACE
        + "4e54a222-d687-475c-a702-13ede0481bb4";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, offset, filterToTest, null, null);

    assertNotNull(response.getEntity());

    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);

    assertEquals(expected - offset, jsonContentList.size());
  }

  @Test
  public void testGetList_allFieldsAreDBFields_operatorLike_thenCorrectResult() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST2bc569d2-a7c1-409a-94f5-c3f4f1bc510d");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST296c0472-8205-441c-86cd-cfdde683477f");
    json_content.addProperty("start_date", "2000-03-05T11:11:11");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST296c0472-8205-441c-86cd-cfdde683477f");
    json_content.addProperty("start_date", "2000-03-05T11:11:11");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json3));

    // send query
    String filterToTest = "name," + "like," + TestFinals.URLENCODING_PERCENT + "8205-441c-86cd"
        + TestFinals.URLENCODING_PERCENT;
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 2;
    assertEquals(expected, jsonContentList.size());
    assertEquals("TEST296c0472-8205-441c-86cd-cfdde683477f", jsonContentList.get(0).get("name").getAsString());

  }

  @Test
  public void testGetList_wrongOperator_thenError() throws Exception {

    String path = pathToUse;

    // prepare test items
    String json2 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST296c0472-8205-441c-86cd-cfdde683477f");
    json_content.addProperty("start_date", "2000-03-05T11:11:11");
    json2 = json_content.toString();
    idUnderTestList.add(IntegrationTestHelper
        .addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest, json2));

    // send query
    String filterToTest = "name," + "like2," + "8205-441c-86cd";
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.SQL_NO_SUCH_OPERATOR_EXISTS, error.getErrcode());
    assertTrue(error.getErrmsg().startsWith(ErrorCodes.SQL_NO_SUCH_OPERATOR_EXISTS_MSG));

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
