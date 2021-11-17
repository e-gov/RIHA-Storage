package ee.eesti.riha.rest.integration.main_resource;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.service.ApiClassicService;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static ee.eesti.riha.rest.logic.util.DateHelper.DATE_FORMAT_IN_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiClassicServiceImpl_PUT<T> {

  // general info here
  @Autowired
  WebClient webClient;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiClassicService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.MAIN_RESOURCE;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING_WITH_FIELD_NAME;

  // other specifics
  @Resource(name = "apiGenericDAOImpl")
  ApiGenericDAO<Main_resource, Integer> genericDAO;

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
  public void testUpdate() throws Exception {

    String jsonContainingEditInfo = TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_SIMPLE;
    Response response = serviceUnderTest.update(jsonContainingEditInfo, tableUnderTest, idUnderTestList.get(0));

    assertNotNull(response.getEntity());

    // test that update did made change
    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;

    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // test that properties were actually updated
    // these fields should be updated in main_resource
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));

    // kind is final and must not be updatable
    // assertFalse(mrUpdated.getKind().equals("A"));
    assertEquals(2, mrUpdated.getOld_id().intValue());

    JsonObject json_contentStored = mrUpdated.getJson_content();
    // these fields should be updated in json_content
    // kind is final and must not be updatable
    // assertFalse(json_contentStored.get("kind").getAsString().equals("A"));
    assertEquals(2, json_contentStored.get("old_id").getAsInt());
    assertTrue(json_contentStored.get("description").getAsString().equals("TESTdesc"));

  }

  @Test
  public void testUpdate_whenWrongTable_thenError() throws Exception {

    String jsonContainingEditInfo = TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX;
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.NON_EXISTENT_TABLE,
        idUnderTestList.get(0));
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);

    assertNotNull(error);
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().equals(TestFinals.NON_EXISTENT_TABLE));

  }

  @Test
  public void testUpdate_whenNonExistentId_thenReturn0() throws Exception {

    String jsonContainingEditInfo = TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX;
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        TestFinals.NON_EXISTENT_ID_ANY_TABLE);
    Map<String, Integer> result = TestHelper.getResultMap(response);

    assertNotNull(result);
    Integer expectedChanges = 0;
    assertEquals(expectedChanges, result.get(Finals.OK));

  }

  @Test
  public void testUpdate_whenSendEmptyString_thenError() throws Exception {

    String jsonContainingEditInfo = "";
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        idUnderTestList.get(0));
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    RihaRestError error = JsonHelper.GSON.fromJson(jsonReturned, RihaRestError.class);
    assertEquals(ErrorCodes.INPUT_JSON_MISSING, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_MISSING_MSG));

  }

  @Test
  public void testUpdate_whenSendGibberishInsteadOfJson_thenError() throws Exception {

    String jsonContainingEditInfo = "ÄÕÜ:te{";
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        idUnderTestList.get(0));
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    RihaRestError error = JsonHelper.GSON.fromJson(jsonReturned, RihaRestError.class);
    assertEquals(ErrorCodes.INPUT_JSON_NOT_VALID_JSON, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG));
    assertTrue(error.getErrtrace().contains(jsonContainingEditInfo));

  }

  @Test
  public void testUpdate_whenSendJsonArrayInsteadOfSingleJsonObject_thenError() throws Exception {

    String jsonContainingEditInfo = "[{}]";
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        idUnderTestList.get(0));
    assertNotNull(response.getEntity());

    InputStream inpStream = (InputStream) response.getEntity();
    String jsonReturned = TestHelper.readStream(inpStream);
    RihaRestError error = JsonHelper.GSON.fromJson(jsonReturned, RihaRestError.class);
    assertEquals(ErrorCodes.INPUT_JSON_ARRAY_RECEIVED_BUT_CAN_ACCEPT_SINGLE_JSON_OBJ_ONLY, error.getErrcode());
    assertTrue(error.getErrmsg().contains(ErrorCodes.INPUT_JSON_ARRAY_RECEIVED_BUT_CAN_ACCEPT_SINGLE_JSON_OBJ_ONLY_MSG));
    assertTrue(error.getErrtrace().contains(jsonContainingEditInfo));

  }

  @Test
  public void testUpdate_jsonContentAndMainResourceWereBothUpdated() throws Exception {

    String jsonContainingEditInfo = TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX;
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        idUnderTestList.get(0));

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in main_resource
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));
    // kind is final and must not be updatable
    // assertFalse(mrUpdated.getKind().equals("A"));
    assertTrue(mrUpdated.getName().equals("TEST1"));
    // [OLD] should not have updated null value
    // now should set to null, if field is explicitly set to null
    assertNull(mrUpdated.getShort_name());
    assertEquals(2, mrUpdated.getOld_id().intValue());

    JsonObject json_contentStored = mrUpdated.getJson_content();

    // these fields should be updated in json_content
    // check strings ok
    // kind is final and must not be updatable
    // assertFalse(json_contentStored.get("kind").getAsString().equals("A"));
    assertTrue(json_contentStored.get("name").getAsString().equals("TEST1"));
    // should not have updated null value
    assertNotNull(json_contentStored.get("short_name"));
    // check jsonarray ok
    assertEquals(1, json_contentStored.get("organizations").getAsJsonArray().size());
    assertTrue(json_contentStored.get("organizations").getAsJsonArray().get(0).getAsJsonObject().get("test_field")
        .getAsBoolean());
    assertEquals(2, json_contentStored.get("groups").getAsJsonArray().size());
    assertTrue(json_contentStored.get("groups").getAsJsonArray().get(1).getAsString().equals("G_TEST2"));
    // check boolean ok
    assertFalse(json_contentStored.get("uses_ads").getAsBoolean());
    // check number ok
    assertEquals(2, json_contentStored.get("old_id").getAsInt());
    assertTrue(json_contentStored.get("description").getAsString().equals("TESTdesc"));
    // check jsonobject ok
    assertTrue(json_contentStored.get("another_object").getAsJsonObject().get("field1").getAsString().equals("val1"));

  }

  @Test
  public void testUpdate_modifierAndModified_dateUpdated() throws Exception {

    String jsonContainingEditInfo = TestFinals.JSON_TO_UPDATE_MAIN_RESOURCE_AS_JSON_STRING_COMPLEX;
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        idUnderTestList.get(0));

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in main_resource
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));

    JsonObject json_contentStored = mrUpdated.getJson_content();

    assertNotNull(mrUpdated.getModifier());
    assertEquals(mrUpdated.getModifier(), json_contentStored.get("modifier").getAsString());

    assertNotNull(mrUpdated.getModified_date());
    Date modifiedDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(json_contentStored.get(Finals.MODIFIED_DATE).getAsString());
    assertEquals(mrUpdated.getModified_date().getTime(), modifiedDateJson.getTime());

  }

  @Test
  public void testUpdate_setNullIfExplicitNullInJson() throws Exception {

    String jsonContainingEditInfo = "{\"field_name\":null}";
    Response response = serviceUnderTest.update(jsonContainingEditInfo, TestFinals.MAIN_RESOURCE,
        idUnderTestList.get(0));

    assertNotNull(response.getEntity());

    Map<String, Integer> result = TestHelper.getResultMap(response);
    Integer numOfChanged = 1;
    assertNotNull(result);
    assertEquals(numOfChanged, result.get(Finals.OK));

    // these fields should be updated in main_resource
    Main_resource mrUpdated = genericDAO.find(Main_resource.class, idUnderTestList.get(0));

    JsonObject json_contentStored = mrUpdated.getJson_content();

    assertNull(mrUpdated.getField_name());
    assertTrue(json_contentStored.get("field_name").isJsonNull());

  }

}
