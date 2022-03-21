package ee.eesti.riha.rest.integration.document;

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
import org.apache.commons.lang3.SystemUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.AfterClass;
import org.junit.Assume;
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

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_GET_opGet_Document<T> {

  // general info here
  @Autowired
  WebClient webClient;
  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiCGIService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.DOCUMENT;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_DOCUMENT_CORRECT_SAMPLE_AS_JSON_STRING;

  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_DOCUMENT;

  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;

  private static Integer main_resourceId;
  
  @Before
  public void beforeTest() {
    if (idUnderTestList.size() == 0) {
      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
      
   // create test Main_resource
      if (main_resourceId == null) {
        main_resourceId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData,
            TestFinals.MAIN_RESOURCE, jsonToUseForCreateMain_resource);
      }
      // add created Main_resource id to json
      JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
      jsonObject.addProperty("main_resource_id", main_resourceId);
      jsonToUseForCreate = jsonObject.toString();
      
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          jsonToUseForCreate));
    }
  }

  @AfterClass
  public static void afterClass() {
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, TestFinals.MAIN_RESOURCE,
        main_resourceId);
  }

  @Test
  public void testGetAll() throws Exception {
    // access to document content file is needed
    Assume.assumeTrue("Test works only in server", SystemUtils.IS_OS_LINUX);
    Response response = serviceUnderTest.getCGI(Finals.GET, pathToUse, "testToken", 100, null, null, null, null);
    String json = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(json);
    assertNotNull(jsonContentList);
    assertFalse(jsonContentList.isEmpty());

  }

  @Test
  public void testGetAll_whenWrongTable_thenError() throws Exception {

    String path = "/db/" + TestFinals.NON_EXISTENT_TABLE + "/";

    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testGetCGI_whenURLMissingAllRequiredPars_thenError() throws Exception {

    Response response = serviceUnderTest.getCGI(null, null, null, null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING_MSG));

  }

  @Test
  public void testGetCGI_whenURLMissingRequiredParPath_thenError() throws Exception {

    Response response = serviceUnderTest.getCGI(Finals.GET, null, null, null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING_MSG));

  }

  @Test
  public void testGetCGI_whenURLMissingRequiredParOp_thenError() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    Response response = serviceUnderTest.getCGI(null, path, null, null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING_MSG));

  }

  @Test
  public void testGetCGI_whenParameterOpValueIsNotKnown_thenError() throws Exception {

    String path = pathToUse;
    Response response = serviceUnderTest.getCGI(TestFinals.UNKNOWN_OP_VALUE, path, null, null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_URL_OP_VALUE_UNKNOWN_OR_NOTSUITABLE, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_URL_OP_VALUE_UNKNOWN_OR_NOTSUITABLE_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.UNKNOWN_OP_VALUE));

  }

  @Test
  public void testGetCGI_whenParameterPathValueIsNotValid_thenError() throws Exception {

    String path = pathToUse + "/test/test///";

    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_URL_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_URL_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testGetById_whenParameterPathValueIsNotValid_whenIdIsNotNumber_thenError() throws Exception {

    String path = pathToUse + "abc/";

    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_URL_PATH_VALUE_NOTVALID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_URL_PATH_VALUE_NOTVALID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(path));

  }

  @Test
  public void testGetById_whenValidCall_thenSuccess() throws Exception {

    String path = pathToUse + idUnderTestList.get(0);
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);
    JsonObject json_content = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(json_content);

    // test for string only found in our test json_content
    boolean test = json_content.get("fieldOnlyInJsonContentTest").getAsString().equals("Gol");
    assertTrue(test);

  }

  @Test
  public void testGetById_whenWrongTable_thenError() throws Exception {

    String path = "/db/" + TestFinals.NON_EXISTENT_TABLE + "/" + idUnderTestList.get(0);
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testGetById_whenNonExistentId_thenError() throws Exception {

    String path = pathToUse + TestFinals.NON_EXISTENT_ID_ANY_TABLE;
    Response response = serviceUnderTest.getCGI(Finals.GET, path, "testToken", null, null, null, null, null);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_ID_ANY_TABLE.toString()));

  }

}
