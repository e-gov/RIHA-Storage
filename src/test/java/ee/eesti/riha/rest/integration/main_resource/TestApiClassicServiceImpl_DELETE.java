package ee.eesti.riha.rest.integration.main_resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiClassicServiceImpl_DELETE<T> {

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

  @Test
  public void testDeleteNone() throws Exception {

    Response response = serviceUnderTest.delete(tableUnderTest, TestFinals.NON_EXISTENT_ID_ANY_TABLE);
    assertNotNull(response.getEntity());
    Map<String, Integer> result = TestHelper.getResultMap(response);
    System.out.println(result);
    assertNotNull(result);
    Integer expectDeleted = 0;
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDelete() throws Exception {

    Response response = serviceUnderTest.delete(tableUnderTest, idUnderTestList.get(0));

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);

    assertNotNull(result);
    Integer expectDeleted = 1;
    assertEquals(expectDeleted, result.get(Finals.OK));

  }

  @Test
  public void testDelete_whenWrongTable_thenError() throws Exception {

    Response response = serviceUnderTest.delete(TestFinals.NON_EXISTENT_TABLE, idUnderTestList.get(0));
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testDelete_whenWrongId_thenError() throws Exception {

    Response response = serviceUnderTest.delete(TestFinals.NON_EXISTENT_TABLE, idUnderTestList.get(0));
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

}
