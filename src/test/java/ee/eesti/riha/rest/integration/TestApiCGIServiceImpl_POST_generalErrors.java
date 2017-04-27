package ee.eesti.riha.rest.integration;

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

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_generalErrors<T> {

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

  @Before
  public void beforeTest() {
    if (serviceHelpingCreateDeleteTestData == null) {
      // create only once (for all tests)
      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
    }
    if (idUnderTestList.size() == 0) {
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          jsonToUseForCreate));
    }
  }

  @AfterClass
  public static void afterClass() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  @Test
  public void testGetCGI_whenJsonMissingAllRequiredPars_thenError() throws Exception {

    String json = "{}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(error.getErrmsg().contains(Finals.OP));
    // for special queries path is not required FIXME TODO
    assertTrue(error.getErrmsg().contains(Finals.PATH));
    assertTrue(error.getErrtrace().contains(json));

  }

  @Test
  public void testGetCGI_whenJsonMissingAltogether_thenError() throws Exception {

    String json = "";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_MISSING_MSG));

  }

  @Test
  public void testGetCGI_whenSendGibberishInsteadOfJson_thenError() throws Exception {

    String json = "ÄÕÜ:te{";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_NOT_VALID_JSON, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG));
    assertTrue(error.getErrtrace().contains(json));

  }

  @Test
  public void testGetCGI_whenJsonMissingRequiredParOp_thenError() throws Exception {

    String json = "{\"path\": \"TEST\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG));
    assertTrue(error.getErrmsg().contains(Finals.OP));
    assertTrue(error.getErrtrace().contains(json));

  }

  @Test
  public void testPostCGI_whenParameterOpValueIsNotKnown_thenError() throws Exception {

    String path = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_MAIN_RESOURCE;
    String json = "{\"op\": " + TestFinals.UNKNOWN_OP_VALUE + ", \"path\": \"" + path + "\"," + "\"token\" : \"abca\"}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_JSON_OP_VALUE_UNKNOWN, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_OP_VALUE_UNKNOWN_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.UNKNOWN_OP_VALUE));

  }

  @Test
  public void testPostCGI_whenUpdateByFieldNoId_thenError() throws Exception {

    String path = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_MAIN_RESOURCE;
    String json = "{\"op\": \"put\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\", "
        + "\"data\":{\"asdfg\":555}}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.UPDATE_ID_MISSING, error.getErrcode());
    assertEquals(ErrorCodes.UPDATE_ID_MISSING_MSG, error.getErrmsg());

  }

  @Test
  public void testPostCGI_whenUpdateByFieldNoId2_thenError() throws Exception {

    String path = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_MAIN_RESOURCE;
    String json = "{\"op\": \"put\", \"path\": \"" + path + "\"," + "\"token\" : \"abca\", "
        + "\"key\":\"ggg\", \"data\":{\"asdfg\":555, \"ggg\":123}}";
    Response response = serviceUnderTest.postCGI(json);

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.UPDATE_ID_MISSING, error.getErrcode());
    assertEquals(ErrorCodes.UPDATE_ID_MISSING_MSG, error.getErrmsg());

  }

}
