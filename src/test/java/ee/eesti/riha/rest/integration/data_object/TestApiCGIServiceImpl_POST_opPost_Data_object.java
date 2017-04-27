package ee.eesti.riha.rest.integration.data_object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
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
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opPost_Data_object<T> {

  // general info here
  @Autowired
  WebClient webClient;
  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static String tableUnderTestMain_resource = TestFinals.MAIN_RESOURCE;
  private static Integer main_resourceId;

  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiCGIService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.DATA_OBJECT;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_DATA_OBJECT_CORRECT_SAMPLE_AS_JSON_STRING;

  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_DATA_OBJECT;

  @Before
  public void beforeTest() {

    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
    // create test Main_resource
    if (main_resourceId == null) {
      main_resourceId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData,
          tableUnderTestMain_resource, jsonToUseForCreateMain_resource);
    }
    // add created Main_resource id to json
    JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
    jsonObject.addProperty("main_resource_id", main_resourceId);
    jsonToUseForCreate = jsonObject.toString();
    // create Data_object from modified json
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
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestMain_resource,
        main_resourceId);
  }

  @Test
  public void testCreate() throws Exception {

    String json = "{\r\n" + "	\"op\":\"post\", \r\n" + "	\"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\", \r\n" + "	\"data\": " + jsonToUseForCreate + "}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    String resultKey = StringUtils.deleteAny(jsonReturned, "[]");
    String id1 = resultKey.replace(".0", "");
    assertTrue(org.apache.commons.lang3.StringUtils.isNumeric(id1));
    idUnderTestList.add(new Integer(id1));

  }

  @Test
  public void testCreate_whenJsonMissingRequiredParData_thenError() throws Exception {

    String json = "{\"op\":\"post\",\"token\":\"testToken\", \"path\": \"" + pathToUse + "\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(error.getErrmsg().contains(Finals.DATA));
    assertTrue(error.getErrtrace().contains(json));

  }

  @Test
  public void testCreate_whenJsonInDataIsMissingRequiredPars_thenError() throws Exception {

    String jsonInData = "{}";
    String json = "{\"op\":\"post\", \"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"data\":"
        + jsonInData + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(error.getErrmsg().contains("kind"));
    assertTrue(error.getErrtrace().contains(jsonInData));

  }

  @Test
  public void testCreate_whenParameterPathValueIsNotValid_thenError() throws Exception {

    String path = pathToUse + "/test/test///";
    String json = "{\"op\": \"post\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testCreate_whenParameterPathValueIsNotValid_whenIdIsNotNumber_thenError() throws Exception {

    String path = pathToUse + "abc/";
    String json = "{\"op\": \"post\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testCreate_whenMain_resource_idDoesNotExist_thenError() throws Exception {

    // set id that does not exist
    JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
    jsonObject.addProperty("main_resource_id", 1);
    jsonToUseForCreate = jsonObject.toString();

    String json = "{\r\n" + "	\"op\":\"post\", \r\n" + "	\"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\", \r\n" + "	\"data\": " + jsonToUseForCreate + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
//    assertEquals(ErrorCodes.FOREIGN_KEY_VIOLATION, error.getErrcode());
//    assertTrue(error.getErrmsg().startsWith(ErrorCodes.FOREIGN_KEY_VIOLATION_MSG));
//    assertTrue(error.getErrmsg().contains("violates foreign key"));
    assertEquals(ErrorCodes.NOT_AUTHORIZED_NO_REF_MAIN_RESOURECE, error.getErrcode());
    assertTrue(error.getErrmsg().startsWith(ErrorCodes.NOT_AUTHORIZED_NO_REF_MAIN_RESOURECE_MSG));

  }

  @Test
  public void testCreate_whenStringInCharacterJSON_thenError() throws Exception {

    // set Character state to String
    JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
    jsonObject.addProperty("state", "as");
    jsonToUseForCreate = jsonObject.toString();

    String json = "{\r\n" + "	\"op\":\"post\", \r\n" + "	\"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\", \r\n" + "	\"data\": " + jsonToUseForCreate + "}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.JSON_TYPE_ERROR, error.getErrcode());
    assertTrue(error.getErrmsg().startsWith(ErrorCodes.JSON_TYPE_ERROR_MSG));
    // no longer show exception message in Error.errmsg
//    assertTrue(error.getErrmsg().contains("Expecting character, got:"));

  }

  @Test
  public void testCreateList_whenInputOK1Item_thenReturnKeyOK() throws Exception {

    String json = "{\r\n" + "	\"op\":\"post\", \r\n" + "	\"path\": \"" + pathToUse + "\","
        + "\"token\":\"testToken\", \r\n" + "	\"data\": [" + jsonToUseForCreate + "	]" + "}";

    Response response = serviceUnderTest.postCGI(json);

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
    String jsonJson_contents = JsonHelper.GSON.toJson(jsonObjList);

    String json = "{\r\n" + "	\"op\":\"post\", \r\n" + "	\"path\": \"" + pathToUse + "\", \r\n"
        + "\"token\":\"testToken\",	\"data\":" + jsonJson_contents + "}";

    Response response = serviceUnderTest.postCGI(json);

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
  public void testCreateList_whenInput2NotOK_whenMissingRequiredPars_thenError() throws Exception {

    String jsonInData = "[{},{\"name\":\"nameExistsTEST\"}]";
    String json = "{\"op\":\"post\", \"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"data\":"
        + jsonInData + "}";
    Response response = serviceUnderTest.postCGI(json);

    PartialError partErrData = TestHelper.getObjectFromClient((InputStream) response.getEntity(), PartialError.class);
    assertNotNull(partErrData);
    List<Double> items = (List<Double>) partErrData.getSuccessData();
    assertEquals(0, items.size());
    List<RihaRestError> errors = partErrData.getErrors();
    assertEquals(2, errors.size());
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(0).getErrcode());
    assertTrue(errors.get(0).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(errors.get(0).getErrmsg().contains("kind"));
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(1).getErrcode());
    assertTrue(errors.get(1).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(errors.get(1).getErrmsg().contains("kind"));

  }

  @Test
  public void testCreateList_when2InputBothNotOK_bothMissingReqPar_thenPartError() throws Exception {

    String jsonInData = "[{},{\"newNonexistantProp\":20}]";
    String json = "{\"op\":\"post\", \"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"data\":"
        + jsonInData + "}";
    Response response = serviceUnderTest.postCGI(json);

    PartialError partErrData = TestHelper.getObjectFromClient((InputStream) response.getEntity(), PartialError.class);
    System.out.println(partErrData);
    assertNotNull(partErrData);
    List<Double> items = (List<Double>) partErrData.getSuccessData();
    System.out.println(items);
    assertEquals(0, items.size());
    List<RihaRestError> errors = partErrData.getErrors();
    assertEquals(2, errors.size());
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(0).getErrcode());
    assertTrue(errors.get(0).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(errors.get(0).getErrmsg().contains("kind"));
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(1).getErrcode());
    assertTrue(errors.get(1).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));

  }

  @Test
  public void testCreateList_when2ElementsOKBut2MissingPars_thenRespond2SuccessAnd2InError() throws Exception {

    // valid elements
    String item1 = jsonToUseForCreate;
    String item2 = jsonToUseForCreate;

    // error elements
    String item3WithMissingPars = jsonToUseForCreate;
    JsonObject json_content = JsonHelper.GSON.fromJson(item3WithMissingPars, JsonObject.class);
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
    String jsonInData = JsonHelper.GSON.toJson(jsonObjList);
    // System.out.println(json);

    String json = "{\"op\":\"post\", \"path\": \"" + pathToUse + "\"," + "\"token\":\"testToken\", \"data\":"
        + jsonInData + "}";
    Response response = serviceUnderTest.postCGI(json);

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
    // assertTrue(errors.get(0).getErrmsg().contains("kind"));
    assertTrue(errors.get(0).getErrmsg().contains("kind_id"));
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, errors.get(1).getErrcode());
    assertTrue(errors.get(1).getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    // assertTrue(errors.get(1).getErrmsg().contains("kind"));
    assertTrue(errors.get(1).getErrmsg().contains("kind_id"));

  }

}
