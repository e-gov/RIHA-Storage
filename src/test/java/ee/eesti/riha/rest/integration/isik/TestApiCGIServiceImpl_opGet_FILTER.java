package ee.eesti.riha.rest.integration.isik;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.service.ApiCGIService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_opGet_FILTER<T> {

  // general info here
  @Autowired
  WebClient webClient;

  // service under test info here
  private static ApiCGIService serviceUnderTest;

  @Before
  public void beforeTest() {
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
  }

  @After
  public void afterTest() {

  }

  @Test
  public void testGetList_withFilter() throws Exception {

    // send query
    String filterToTest = "eesnimi,ilike,%25a%25";
    Response response = serviceUnderTest.getCGI(Finals.GET, "db/isik", "testToken", 10, null, filterToTest, null, null);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = 10;
    assertEquals(expected, jsonContentList.size());
    assertTrue(jsonContentList.get(0).get("eesnimi").toString().contains("a"));

  }

  @Test
  public void testGetList_withFilterAndFields_post() throws Exception {

    String json = "{\"op\":\"get\", \"path\":\"db/isik\", \"token\":\"testToken\", "
        + "\"limit\":10, \"filter\":[[\"perenimi\", \"ilike\", \"%a%\"]]," + "\"fields\":[\"perenimi\"]}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = 10;
    assertEquals(expected, jsonContentList.size());
    assertEquals(1, jsonContentList.get(0).entrySet().size());
    assertTrue(jsonContentList.get(0).get("perenimi").toString().contains("a"));

  }

  @Test
  public void testGetList_withFilterAndFields_post_Asutus() throws Exception {

    String json = "{\"op\":\"get\", \"path\":\"db/asutus\", \"token\":\"testToken\", "
        + "\"limit\":10, \"filter\":[[\"nimetus\", \"ilike\", \"%ee%\"]]," + "\"fields\":[\"nimetus\"]}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());
    String jsonReceived = TestHelper.readStream((InputStream) response.getEntity());
    List<JsonObject> jsonContentList = TestHelper.getJsonContentList(jsonReceived);
    assertNotNull(jsonContentList);
    System.out.println(jsonContentList);
    int expected = 10;
    assertEquals(expected, jsonContentList.size());
    assertEquals(1, jsonContentList.get(0).entrySet().size());
    assertTrue(jsonContentList.get(0).get("nimetus").toString().contains("ee"));

  }

  @Test
  public void testIllegalModification_thenError() throws Exception {

    String json = "{\"op\":\"put\", \"path\":\"db/isik/123\", \"token\":\"testToken\", " + "\"data\":{}}";

    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.TABLE_CANT_BE_MODIFIED, error.getErrcode());
    assertEquals(ErrorCodes.TABLE_CANT_BE_MODIFIED_MSG, error.getErrmsg());
    assertEquals("isik", error.getErrtrace());

  }

  @Test
  public void testIllegalModification_CREATE_thenError() throws Exception {

    String json = "{\"op\":\"post\", \"path\":\"db/isik/\", \"token\":\"testToken\", " + "\"data\":{}}";

    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.TABLE_CANT_BE_MODIFIED, error.getErrcode());
    assertEquals(ErrorCodes.TABLE_CANT_BE_MODIFIED_MSG, error.getErrmsg());
    assertEquals("isik", error.getErrtrace());

  }

  @Test
  public void testIllegalModification_DELETE_thenError() throws Exception {

    String json = "{\"op\":\"delete\", \"path\":\"db/isik/123\", \"token\":\"testToken\"}";

    Response response = serviceUnderTest.postCGI(json);
    assertNotNull(response.getEntity());
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.TABLE_CANT_BE_MODIFIED, error.getErrcode());
    assertEquals(ErrorCodes.TABLE_CANT_BE_MODIFIED_MSG, error.getErrmsg());
    assertEquals("isik", error.getErrtrace());

  }

}
