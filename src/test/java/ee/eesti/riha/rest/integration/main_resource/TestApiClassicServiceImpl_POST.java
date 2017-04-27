package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.google.gson.JsonObject;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.PartialError;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiClassicService;

@Transactional
// @RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiClassicServiceImpl_POST<T> {

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

  @Rollback(true)
  @Test
  public void testCreate_whenValidSample_thenSuccess() throws Exception {

    Response response = serviceUnderTest.create(jsonToUseForCreate, tableUnderTest);
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    String resultKey = StringUtils.deleteAny(jsonReturned, "[]");
    String id1 = resultKey.replace(".0", "");
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(id1));
    idUnderTestList.add(new Integer(id1));

  }

  @Test
  public void testCreate_whenWrongTable_thenError() throws Exception {

    Response response = serviceUnderTest.create(jsonToUseForCreate, TestFinals.NON_EXISTENT_TABLE);
    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testCreate_whenSendEmptyString_thenRespondWithClearError() throws Exception {

    String json = "";
    Response response = serviceUnderTest.create(json, tableUnderTest);
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    RihaRestError error = JsonHelper.GSON.fromJson(jsonReturned, RihaRestError.class);
    assertEquals(ErrorCodes.INPUT_JSON_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_MISSING_MSG));

  }

  @Test
  public void testCreate_whenSendGibberishInsteadOfJson_thenRespondWithClearError() throws Exception {

    String json = "ÄÕÜ:te{";
    Response response = serviceUnderTest.create(json, tableUnderTest);
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    RihaRestError error = JsonHelper.GSON.fromJson(jsonReturned, RihaRestError.class);
    assertEquals(ErrorCodes.INPUT_JSON_NOT_VALID_JSON, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG));
    assertTrue(error.getErrtrace().contains(json));

  }

  @Test
  public void testCreate_whenRequiredPropertiesMissing_thenRespondWithClearError() throws Exception {

    String json = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.getFromJson(jsonToUseForCreate);
    // json_content.remove("name");
    json_content.remove("kind");
    json_content.remove("kind_id");
    json = json_content.toString();
    Response response = serviceUnderTest.create(json, tableUnderTest);
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    RihaRestError error = JsonHelper.GSON.fromJson(jsonReturned, RihaRestError.class);
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    // assertTrue(error.getErrmsg().contains("name"));
    assertTrue(error.getErrmsg().contains("kind"));

  }

  @Test
  public void testCreateList_whenInputOK1Item_thenReturnKeyOK() throws Exception {

    JsonObject jsonObj1 = JsonHelper.GSON.fromJson(jsonToUseForCreate, JsonObject.class);
    List<JsonObject> jsonObjList = new ArrayList<>();
    jsonObjList.add(jsonObj1);
    String json = JsonHelper.GSON.toJson(jsonObjList);
    System.out.println(json);
    Response response = serviceUnderTest.create(json, tableUnderTest);
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    System.out.println(jsonReturned);
    String resultKeys = StringUtils.deleteAny(jsonReturned, "[]");
    String[] keys = resultKeys.split(",");
    assertEquals(1, keys.length);
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(keys[0]));
    idUnderTestList.add(new Integer(keys[0]));

  }

  @Test
  public void testCreateList_whenInputOK2Items_thenReturnKeysOK() throws Exception {

    String item1 = jsonToUseForCreate;
    String item2 = jsonToUseForCreate;
    JsonObject jsonObj1 = JsonHelper.GSON.fromJson(item1, JsonObject.class);
    JsonObject jsonObj2 = JsonHelper.GSON.fromJson(item2, JsonObject.class);
    List<JsonObject> jsonObjList = new ArrayList<>();
    jsonObjList.add(jsonObj1);
    jsonObjList.add(jsonObj2);
    String json = JsonHelper.GSON.toJson(jsonObjList);
    System.out.println(json);
    Response response = serviceUnderTest.create(json, tableUnderTest);
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    System.out.println(jsonReturned);
    String resultKeys = StringUtils.deleteAny(jsonReturned, "[]");
    String[] keys = resultKeys.split(",");
    assertEquals(2, keys.length);
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(keys[0]));
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(keys[1]));
    idUnderTestList.add(new Integer(keys[0]));
    idUnderTestList.add(new Integer(keys[1]));

  }

  @Test
  public void testCreateList_when2ElementOKBut2MissingPars_thenRespond2SuccessAnd2InError() throws Exception {

    // valid elements
    String item1 = jsonToUseForCreate;
    String item2 = jsonToUseForCreate;

    // error elements
    String item3WithMissingPars = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(item3WithMissingPars, JsonObject.class);
    // json_content.remove("name");
    json_content.remove("kind");
    json_content.remove("kind_id");
    item3WithMissingPars = json_content.toString();
    String item4WithMissingPars = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(item4WithMissingPars, JsonObject.class);
    json_content.remove("kind");
    json_content.remove("kind_id");
    item4WithMissingPars = json_content.toString();

    JsonObject jsonObj1 = JsonHelper.GSON.fromJson(item1, JsonObject.class);
    JsonObject jsonObj2 = JsonHelper.GSON.fromJson(item2, JsonObject.class);
    JsonObject jsonObj3 = JsonHelper.GSON.fromJson(item3WithMissingPars, JsonObject.class);
    JsonObject jsonObj4 = JsonHelper.GSON.fromJson(item4WithMissingPars, JsonObject.class);
    List<JsonObject> jsonObjList = new ArrayList<>();
    jsonObjList.add(jsonObj1);
    jsonObjList.add(jsonObj2);
    jsonObjList.add(jsonObj3);
    jsonObjList.add(jsonObj4);
    String json = JsonHelper.GSON.toJson(jsonObjList);
    // System.out.println(json);
    Response response = serviceUnderTest.create(json, tableUnderTest);
    assertNotNull(response.getEntity());

    PartialError partErrData = TestHelper.getObjectFromClient((InputStream) response.getEntity(), PartialError.class);
    List<Double> items = (List<Double>) partErrData.getSuccessData();
    assertEquals(2, items.size());
    String id1 = items.get(0).toString().replace(".0", "");
    String id2 = items.get(1).toString().replace(".0", "");
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(id1));
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(id2));
    idUnderTestList.add(new Integer(id1));
    idUnderTestList.add(new Integer(id2));
    List<RihaRestError> errors = partErrData.getErrors();
    assertEquals(2, errors.size());
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(0).getErrcode());
    assertTrue(errors.get(0).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    // assertTrue(errors.get(0).getErrmsg().contains("name"));
    // assertTrue(errors.get(0).getErrmsg().contains("kind"));
    assertTrue(errors.get(0).getErrmsg().contains("kind_id"));
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(1).getErrcode());
    assertTrue(errors.get(1).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    // assertFalse(errors.get(1).getErrmsg().contains("name"));
    // assertTrue(errors.get(1).getErrmsg().contains("kind"));
    assertTrue(errors.get(1).getErrmsg().contains("kind_id"));

  }

  @Test
  public void testCreateList_when2ElementOKBut2MissingPars_butWrongTable_thenWrongTableError() throws Exception {

    // valid elements
    String item1 = jsonToUseForCreate;
    String item2 = jsonToUseForCreate;

    // error elements
    String item3WithMissingPars = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(item3WithMissingPars, JsonObject.class);
    json_content.remove("name");
    item3WithMissingPars = json_content.toString();
    String item4WithMissingPars = jsonToUseForCreate;
    json_content = JsonHelper.GSON.fromJson(item4WithMissingPars, JsonObject.class);
    json_content.remove("kind");
    item4WithMissingPars = json_content.toString();

    JsonObject jsonObj1 = JsonHelper.GSON.fromJson(item1, JsonObject.class);
    JsonObject jsonObj2 = JsonHelper.GSON.fromJson(item2, JsonObject.class);
    JsonObject jsonObj3 = JsonHelper.GSON.fromJson(item3WithMissingPars, JsonObject.class);
    JsonObject jsonObj4 = JsonHelper.GSON.fromJson(item4WithMissingPars, JsonObject.class);
    List<JsonObject> jsonObjList = new ArrayList<>();
    jsonObjList.add(jsonObj1);
    jsonObjList.add(jsonObj2);
    jsonObjList.add(jsonObj3);
    jsonObjList.add(jsonObj4);
    String json = JsonHelper.GSON.toJson(jsonObjList);
    Response response = serviceUnderTest.create(json, TestFinals.NON_EXISTENT_TABLE);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

}
