package ee.eesti.riha.rest.integration.main_resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.service.ApiClassicService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.AfterClass;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiClassicServiceImpl_GET<T> {

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
    if (idUnderTestList.size() == 0) {
      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceUnderTest, tableUnderTest, jsonToUseForCreate));
    }
  }

  @AfterClass
  public static void afterClass() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceUnderTest, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  @Test
  public void testGetAll_thenLimitationToNumOfElementsReturnedWorks() throws Exception {

    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    // int expected = Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED;
    int expected = 200;
    assertEquals(expected, jsonContentList.size());

  }

  @Test
  public void testGetAll_whenWrongTable_thenError() throws Exception {

    Response response = serviceUnderTest.getMany(TestFinals.NON_EXISTENT_TABLE, null, null, null, null, null);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testGetById() throws Exception {

    Response response = serviceUnderTest.getById(tableUnderTest, idUnderTestList.get(0), null);
    JsonObject jsonContent = null;
    try {
      jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    } catch (JsonSyntaxException e) {
      fail("JsonObject expected");
    }
    assertNotNull(jsonContent);

  }

  @Test
  public void testGetById_whenRequestingMainResource_thenClientExpectJsonContent() throws Exception {

    Response response = serviceUnderTest.getById(tableUnderTest, idUnderTestList.get(0), null);
    JsonObject json_content = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    System.out.println(json_content);
    assertNotNull(json_content);
    // test for string only found in our test json_content
    boolean test = json_content.get("personal_data").getAsBoolean();
    assertFalse(test);

  }

  @Test
  public void testGetById_whenWrongTable_thenError() throws Exception {

    Response response = serviceUnderTest.getById(TestFinals.NON_EXISTENT_TABLE, idUnderTestList.get(0), null);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testGetById_whenNonExistentId_thenError() throws Exception {

    Response response = serviceUnderTest.getById(tableUnderTest, TestFinals.NON_EXISTENT_ID_ANY_TABLE, null);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_ID_ANY_TABLE.toString()));

  }

  @Test
  public void testGetById_withFields() throws Exception {
    String fields = "[\"name\", \"short_name\", \"kind\"]";
    Response response = serviceUnderTest.getById(tableUnderTest, idUnderTestList.get(0), fields);
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

    String fields = "[\"name\", \"short_name\", \"kind_id\"]";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    int expectedNumOfFields = 3;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withFieldsURLSpace() throws Exception {

    String fields = "[\"name\",%20\"short_name\",%20\"kind_id\"]";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    int expectedNumOfFields = 3;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withWrongFields() throws Exception {

    String fields = "[\"name22\", \"asdfasd\", \"cvbn\"]";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    int expectedNumOfFields = 0;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withFields_commaSeparated() throws Exception {

    String fields = "name,kind_id";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    int expectedNumOfFields = 2;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withFields_commaSeparatedUrlSpace() throws Exception {

    String fields = "name%20,kind_id%20,creator";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    int expectedNumOfFields = 3;
    assertEquals(expectedNumOfFields, jsonContentList.get(0).entrySet().size());

  }

  @Test
  public void testGetAll_withFields_notJsonArray() throws Exception {

    String fields = "name:kind.creator";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    System.out.println(error);

    assertEquals(ErrorCodes.INPUT_JSON_NOT_VALID_JSON, error.getErrcode());
    assertTrue("Wrong error message", error.getErrmsg().contains(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG));

  }

  @Test
  public void testGetAll_withFieldsEmptyString() throws Exception {

    String fields = "";
    Response response = serviceUnderTest.getMany(tableUnderTest, 200, null, null, null, fields);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);

    // int expected = Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED;
    int expected = 200;
    assertEquals(expected, jsonContentList.size());

  }

}
