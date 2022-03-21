package ee.eesti.riha.rest.integration.special;

import com.google.gson.reflect.TypeToken;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.service.ApiCGIService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opGetNames {

  // general info here
  @Autowired
  WebClient webClient;

  // service under test info here
  private static ApiCGIService serviceUnderTest;

  @Before
  public void beforeTest() {
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
  }

  @Test
  public void testGetNames() throws Exception {
    String json = "{\"op\":\"getnames\", " + "\"organizations\":[\"70009646\", \"80296167\"], "
        + "\"persons\":[\"37211070309\", \"37404192743\"], " + "\"token\":\"testToken\"}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Map<String, String>> names = TestHelper.getObjectFromClient((InputStream) response.getEntity(),
        new TypeToken<HashMap<String, Map<String, String>>>() {
        }.getType());

    assertNotNull(names);
    assertFalse(names.isEmpty());
    assertEquals(2, names.get("organizations").size());
    assertEquals(2, names.get("persons").size());
  }

  @Test
  public void testGetNamesWithURIs() throws Exception {
    String json = "{\"op\":\"getnames\", " + "\"organizations\":[\"70009646\", \"80296167\"], "
        + "\"persons\":[\"37211070309\", \"37404192743\"], "
        + "\"uris\":[\"https://riha.eesti.ee/riha/onto/ravikindlustus/2008/r2\", " + "\"http://www.sehke.ee/Sehke\", "
        + "\"http://riha.eesti.ee/riha/onto/ravikindlustus/2008/r1\"], " + "\"token\":\"testToken\"}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Map<String, String>> names = TestHelper.getObjectFromClient((InputStream) response.getEntity(),
        new TypeToken<HashMap<String, Map<String, String>>>() {
        }.getType());

    assertNotNull(names);
    assertFalse(names.isEmpty());
    assertEquals(2, names.get("organizations").size());
    assertEquals(2, names.get("persons").size());
    assertEquals(3, names.get("uris").size());
  }

  @Test
  public void testGetNames_empty() throws Exception {
    String json = "{\"op\":\"getnames\", " + "\"organizations\":[], " + "\"persons\":[], " + "\"token\":\"testToken\"}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Map<String, String>> names = TestHelper.getObjectFromClient((InputStream) response.getEntity(),
        new TypeToken<HashMap<String, Map<String, String>>>() {
        }.getType());

    assertNotNull(names);
    assertFalse(names.isEmpty());
    assertEquals(0, names.get("organizations").size());
    assertEquals(0, names.get("persons").size());
  }

  @Test
  public void testGetNames_notFound() throws Exception {
    String json = "{\"op\":\"getnames\", " + "\"organizations\":[\"asdasd\"], " + "\"persons\":[\"ggg234234\"], "
        + "\"token\":\"testToken\"}";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    Map<String, Map<String, String>> names = TestHelper.getObjectFromClient((InputStream) response.getEntity(),
        new TypeToken<HashMap<String, Map<String, String>>>() {
        }.getType());

    assertNotNull(names);
    assertFalse(names.isEmpty());
    assertEquals(0, names.get("organizations").size());
    assertEquals(0, names.get("persons").size());
  }

}
