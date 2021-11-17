package ee.eesti.riha.rest.integration.special;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.readonly.Kind;
import ee.eesti.riha.rest.service.ApiTableService;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiTableServiceImpl {

  @Autowired
  WebClient webClient;

  // service under test info here
  private static ApiTableService serviceUnderTest;

  @Before
  public void beforeTest() {
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiTableService.class);
  }

  @Test
  public void testWorksWithKind() throws Exception {
    Response response = serviceUnderTest.getFullTable("kind");

    List<Kind> kinds = TestHelper.getObjectsFromClient(response);
    assertNotNull(kinds);
    assertFalse(kinds.isEmpty());
  }

  @Test
  public void testError_withWrongTableName() throws Exception {
    Response response = serviceUnderTest.getFullTable("asdasd");

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    ;
    assertNotNull(error);
    assertEquals(ErrorCodes.WRONG_TABLE_FULL_SERVICE, error.getErrcode());
    assertEquals(ErrorCodes.WRONG_TABLE_FULL_SERVICE_MSG, error.getErrmsg());
    assertEquals("asdasd", error.getErrtrace());

  }

}
