package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_GET_opCount<T> {

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

    // serviceHelpingCreateDeleteTestData.setAuthService(new AuthServiceImpl());
    // serviceUnderTest.setAuthService(new AuthServiceImpl());

    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    // serviceUnderTest.setAuthService(null);

  }

  @After
  public void afterTest() {
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  @Test
  public void testCountAll() throws IOException {
    String path = pathToUse;
    int limit = Finals.COUNT_ALL_LIMIT;
    Response response = serviceUnderTest.getCGI(Finals.COUNT, path, "testToken", limit, null, null, null, null);

    assertNotNull(response.getEntity());
    // String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    Map<String, Integer> resultCount = TestHelper.getResultMap(response);

    int countFromService = resultCount.get(Finals.OK);
    int countFromDB = genericDAO.findCount(Finals.getClassRepresentingTable(tableUnderTest));

    assertTrue(countFromService >= 1);
    assertEquals(countFromDB, countFromService);
  }

  @Ignore("Currently not using default limit ")
  @Test
  public void testCountLimitOverMax_thenDefaultLimit() throws IOException {
    String path = pathToUse;
    int limit = 100000;

    Response response = serviceUnderTest.getCGI(Finals.COUNT, path, "testToken", limit, null, null, null, null);
    assertNotNull(response.getEntity());

    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);
    assertEquals(Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED, countFromService);
  }

  @Test
  public void testCountLimitZero() throws IOException {
    String path = pathToUse;
    int limit = 0;

    Response response = serviceUnderTest.getCGI(Finals.COUNT, path, "testToken", limit, null, null, null, null);
    assertNotNull(response.getEntity());

    Map<String, Integer> resultCount = TestHelper.getResultMap(response);
    int countFromService = resultCount.get(Finals.OK);
    assertEquals(limit, countFromService);
  }

  // based on TestApiCGIServiceImpl_GET_opGet_FILTER
  // .testGetList_testingFilter_fieldExistAsDBField_whenFieldStringAndOpEqual_thenCorrectResult

  // all fields in filter exist as db table field (means that
  // QUERY IS MADE AS STANDARD SQL)
  // STRING comparison
  // use of URL ENCODING
  @Test
  public void testCount_testingFilter_fieldExistAsDBField_whenFieldStringAndOpEqual_thenCorrectResult()
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
    Response response = serviceUnderTest.getCGI(Finals.COUNT, path, "testToken", null, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    int resultCount = TestHelper.getResultMap(response).get(Finals.OK);

    int expected = 1;
    assertEquals(expected, resultCount);
  }

  @Test
  public void testCount_testingFilter_noMatch_thenZero() throws Exception {

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
        + "aaaaaaaaaaa";
    Response response = serviceUnderTest.getCGI(Finals.COUNT, path, "testToken", null, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    int resultCount = TestHelper.getResultMap(response).get(Finals.OK);

    int expected = 0;
    assertEquals(expected, resultCount);
  }

  @Test
  public void testCount_testingFilterWithZeroOffset() throws Exception {

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

    // idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
    // json2));

    int expected = 5;
    for (int i = 0; i < expected; i++) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          json2));
    }

    int offset = 0;
    // send query
    String filterToTest = "name," + TestFinals.URLENCODING_EQUAL + ",TEST" + TestFinals.URLENCODING_SPACE
        + "4e54a222-d687-475c-a702-13ede0481bb4";
    Response response = serviceUnderTest
        .getCGI(Finals.COUNT, path, "testToken", null, offset, filterToTest, null, null);

    assertNotNull(response.getEntity());
    int resultCount = TestHelper.getResultMap(response).get(Finals.OK);

    assertEquals(expected, resultCount);
  }

  @Test
  public void testCount_testingFilterWithOffset() throws Exception {

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
    Response response = serviceUnderTest
        .getCGI(Finals.COUNT, path, "testToken", null, offset, filterToTest, null, null);

    assertNotNull(response.getEntity());
    int resultCount = TestHelper.getResultMap(response).get(Finals.OK);

    assertEquals(expected - offset, resultCount);
  }

}
