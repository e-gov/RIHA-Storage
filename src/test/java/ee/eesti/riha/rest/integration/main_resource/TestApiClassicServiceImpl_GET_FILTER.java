package ee.eesti.riha.rest.integration.main_resource;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
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
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
@Transactional
public class TestApiClassicServiceImpl_GET_FILTER<T> {

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

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // STRING comparison
  // use of URL ENCODING
  @Test
  public void testGetList_testingFilter_fieldExistAsDBField_stringComparisonShouldWork() throws Exception {

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST 834bc29a-21fb-4338-bb11-0cf7ce842d54");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST be8cfd69-f25b-47ea-bc9f-b6617dec1e1c");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST" + TestFinals.URLENCODING_SPACE
        + "834bc29a-21fb-4338-bb11-0cf7ce842d54";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_integerComparisonShouldWork() throws Exception {

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST 5d355f93-e82e-4686-b452-3aac640d7e22");
    json_content.addProperty("access_restriction", 5);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 5d355f93-e82e-4686-b452-3aac640d7e22");
    json_content.addProperty("access_restriction", 4);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST 5d355f93-e82e-4686-b452-3aac640d7e22");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST 5d355f93-e82e-4686-b452-3aac640d7e22");
    json_content.addProperty("access_restriction", 1);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST" + TestFinals.URLENCODING_SPACE
        + "5d355f93-e82e-4686-b452-3aac640d7e22,access_restriction," + TestFinals.URLENCODING_LARGERTHANEQ + ",4";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_stringANDintegerComprisonTogetherShouldWork()
      throws Exception {

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST a25f0f55-3988-45e7-a766-229e1e2a6045");
    json_content.addProperty("access_restriction", 5);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST 1040d68f-890e-4631-95e0-ebe815cee5da");
    json_content.addProperty("access_restriction", 4);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST 1040d68f-890e-4631-95e0-ebe815cee5da");
    json_content.addProperty("access_restriction", 6);
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST b74e4a24-2ddb-4e26-ab83-b0dd0f2e3f1e");
    json_content.addProperty("access_restriction", 1);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    // send query
    String filterToTest = "name,=,TEST" + TestFinals.URLENCODING_SPACE
        + "1040d68f-890e-4631-95e0-ebe815cee5da,access_restriction," + TestFinals.URLENCODING_LARGERTHANEQ + ",4";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST8c6d62e1-6a0b-4e65-b9b6-b2b954888c64,start_date," + TestFinals.URLENCODING_EQUAL
        + ",2111-11-11T11:11:11";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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
  public void testGetList_testingFilter_allFieldsAreDBFields_dateComparisonTogetherShouldWork() throws Exception {

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST5555d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST5555d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2010-02-22T01:01:01");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST5555d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T20:00:00");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST5555d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST5555d62e1-6a0b-4e65-b9b6-b2b954888c64");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST5555d62e1-6a0b-4e65-b9b6-b2b954888c64,start_date," + TestFinals.URLENCODING_LARGERTHAN
        + ",2111-11-11T11:11:11";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    int expected = 1;
    assertEquals(expected, jsonContentList.size());

  }

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // DATE and STRING comparison
  // use of url encoding
  @Test
  public void testGetList_testingFilter_allFieldsAreDBFields_dateANDstringComparisonTogetherShouldWork()
      throws Exception {

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST3e175b24-4c25-400d-a348-95e211bd4ad7");
    json_content.addProperty("access_restriction", 3);
    json_content.addProperty("start_date", "2000-05-18T22:15:05");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST3e175b24-4c25-400d-a348-95e211bd4ad7");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST3e175b24-4c25-400d-a348-95e211bd4ad7");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST3e175b24-4c25-400d-a348-95e211bd4ad7");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TESTac5e7e22-a3c8-44e9-8ac1-f56940b95164");
    json_content.addProperty("start_date", "2111-11-11T11:11:11");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json5));

    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL
        + ",TEST3e175b24-4c25-400d-a348-95e211bd4ad7,start_date," + TestFinals.URLENCODING_LARGERTHANEQ
        + ",2111-11-11T11:11:11";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "c");
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "b");
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "abc");
    json_content.addProperty(strNotDBField2, "a");
    json3 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    json_content.addProperty(strNotDBField, "noMatch123");
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST463ff153-cc49-4af2-ab73-41c6b04990c0");
    // has no first field
    json_content.addProperty(strNotDBField2, "b");
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json5));

    // send query
    String filterToTest = "name,=,TEST463ff153-cc49-4af2-ab73-41c6b04990c0," + strNotDBField + ","
        + TestFinals.URLENCODING_EQUAL + ",abc," + strNotDBField2 + "," + TestFinals.URLENCODING_LARGERTHAN + ",a";
    // System.out.println(filterToTest);
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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

    // prepare test items
    String json1 = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(json1, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 10);
    json_content.addProperty(numNotDBField2, 2000000);
    json1 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json1));

    String json2 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json2, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 2);
    json_content.addProperty(numNotDBField2, 1);
    json2 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json2));

    String json3 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json3, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 5);
    json_content.addProperty(numNotDBField2, 2);
    json3 = json_content.toString();
    // 11111
    // 11112
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json3));

    String json4 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json4, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    json_content.addProperty(numNotDBField, 8);
    json_content.addProperty(numNotDBField2, 3);
    json4 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json4));

    String json5 = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(json5, JsonObject.class);
    json_content.addProperty("name", "TEST21e251bf-0098-4e3a-9844-f2765974a15c");
    // has no first field
    json_content.addProperty(numNotDBField2, 4);
    json5 = json_content.toString();

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, json5));

    String filterToTest = "name,=,TEST21e251bf-0098-4e3a-9844-f2765974a15c," + numNotDBField + ","
        + TestFinals.URLENCODING_LARGERTHANEQ + ",2," + numNotDBField2 + "," + TestFinals.URLENCODING_LESSTHAN + ",5";
    // System.out.println(filterToTest);
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, filterToTest, null, null);

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
