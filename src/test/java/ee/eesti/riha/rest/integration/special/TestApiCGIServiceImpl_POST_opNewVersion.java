package ee.eesti.riha.rest.integration.special;

import static ee.eesti.riha.rest.logic.util.DateHelper.DATE_FORMAT_IN_JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.integration.IntegrationTestHelper;
import ee.eesti.riha.rest.integration.TestFinals;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(MyTestRunner.class)
@WebAppConfiguration
@ContextConfiguration("/integration-test-applicationContext.xml")
public class TestApiCGIServiceImpl_POST_opNewVersion {

  // general info here
  @Autowired
  WebClient webClient;

  @Autowired
  ApiGenericDAO genericDAO;

  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<>();

  // service under test info here
  private static ApiCGIService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.MAIN_RESOURCE;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;

  private static String jsonToUseForCreateDocument = TestFinals.JSON_CONTENT_FOR_DOCUMENT_CORRECT_SAMPLE_AS_JSON_STRING_SHORTER_FOR_RESOURCE;
  private static String jsonToUseForCreateData_object= TestFinals.JSON_CONTENT_FOR_DATA_OBJECT_CORRECT_SAMPLE_AS_JSON_STRING_FOR_RESOURCE;
  
  // other specifics
  private static String pathToUse = TestFinals.CGI_PATH_PROPERTY_VALUE_FOR_MAIN_RESOURCE;

  private String uri;
  private Main_resource old;
  private Integer connectedDocId;
  private Integer connectedData_objectId;
  // needed to delete test files
  private List<Integer> connectedDocIds = new ArrayList<>();

  @Before
  public void beforeTest() {
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
    idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
        jsonToUseForCreate));
    old = (Main_resource) genericDAO.find(Main_resource.class, idUnderTestList.get(0));
    uri = old.getUri();
    
    JsonObject docJson = JsonHelper.getFromJson(jsonToUseForCreateDocument);
    docJson.addProperty("main_resource_id", old.getMain_resource_id());
    connectedDocId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, 
        TestFinals.DOCUMENT, docJson.toString());
    System.out.println("CONNECTED DOC ID " + connectedDocId);
    connectedDocIds.add(connectedDocId);
    
    JsonObject dataJson = JsonHelper.getFromJson(jsonToUseForCreateData_object);
    dataJson.addProperty("main_resource_id", old.getMain_resource_id());
    connectedData_objectId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, TestFinals.DATA_OBJECT, dataJson.toString());
  }

  @After
  public void afterTest() throws IOException {
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
  }

  @Test
  public void testNewVersion() throws Exception {
    String newVersion = "v2";
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + uri + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);

    assertEquals(newVersion, jsonContent.get("version").getAsString());
    assertEquals(uri, jsonContent.get("uri").getAsString());
    assertEquals((int) old.getMain_resource_id(), jsonContent.get("main_resource_id").getAsInt());

    Date modifiedDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get(Finals.MODIFIED_DATE).getAsString());
    Date createdDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("creation_date").getAsString());
    Date startDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("start_date").getAsString());

    assertEquals(modifiedDateJson.getTime(), createdDateJson.getTime());
    assertEquals(modifiedDateJson.getTime(), startDateJson.getTime());

    // get just archived version, offset == 1 (ignore current version)
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    FilterComponent fc2 = new FilterComponent("end_date", "isnotnull", null);
    List<Main_resource> justArchivedList = genericDAO.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), null);
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    idUnderTestList.add(justArchived.getMain_resource_id());

    assertEquals(old.getVersion(), justArchived.getVersion());
    assertEquals(uri, justArchived.getUri());
    assertEquals(modifiedDateJson.getTime(), justArchived.getEnd_date().getTime());
    assertFalse(old.getMain_resource_id().equals(justArchived.getMain_resource_id()));

  }

  @Test
  public void testNewVersionWrongUri() throws Exception {
    String newVersion = "v2";
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + "bad.Uri:123" + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);

    assertEquals(ErrorCodes.NO_ITEM_WITH_URI_FOUND, error.getErrcode());
    assertEquals(ErrorCodes.NO_ITEM_WITH_URI_FOUND_MSG, error.getErrmsg());

  }

  @Test
  public void testNewVersionSameVersion() throws Exception {
    String newVersion = "3.14159265";
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + uri + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);

    assertEquals(ErrorCodes.VERSION_MUST_BE_UPDATED, error.getErrcode());
//    assertEquals(ErrorCodes.VERSION_MUST_BE_UPDATED_MSG, error.getErrmsg());
    assertTrue(error.getErrmsg().startsWith(ErrorCodes.VERSION_MUST_BE_UPDATED_MSG));
    assertTrue(error.getErrmsg().contains(uri));
    assertTrue(error.getErrmsg().contains(newVersion));

  }
  
  @Test
  public void testNewVersionWrongTable() throws Exception {
    String newVersion = "v2";
    String json = "{\"op\":\"newversion\",\"path\": \"db/data_object/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + uri + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);

    assertEquals(ErrorCodes.CANT_CREATE_NEW_VERSION, error.getErrcode());
    assertEquals(ErrorCodes.CANT_CREATE_NEW_VERSION_MSG, error.getErrmsg());

  }

  @Test
  public void testNewVersionNoNewVersion() throws Exception {
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"uri\": \"" + uri
        + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);

    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING, error.getErrcode());
    assertEquals(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG + "new_version", error.getErrmsg());

  }

  @Test
  public void testNewVersionWithConnectedItems() throws Exception {
    
    String newVersion = "v2";
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + uri + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);

    assertEquals(newVersion, jsonContent.get("version").getAsString());
    assertEquals(uri, jsonContent.get("uri").getAsString());
    assertEquals((int) old.getMain_resource_id(), jsonContent.get("main_resource_id").getAsInt());

    Date modifiedDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get(Finals.MODIFIED_DATE).getAsString());
    Date createdDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("creation_date").getAsString());
    Date startDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("start_date").getAsString());

    assertEquals(modifiedDateJson.getTime(), createdDateJson.getTime());
    assertEquals(modifiedDateJson.getTime(), startDateJson.getTime());

    // get just archived version, offset == 1 (ignore current version)
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    FilterComponent fc2 = new FilterComponent("end_date", "isnotnull", null);
    List<Main_resource> justArchivedList = genericDAO.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), null);
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    idUnderTestList.add(justArchived.getMain_resource_id());

    assertEquals(old.getVersion(), justArchived.getVersion());
    assertEquals(uri, justArchived.getUri());
    assertEquals(modifiedDateJson.getTime(), justArchived.getEnd_date().getTime());
    assertFalse(old.getMain_resource_id().equals(justArchived.getMain_resource_id()));

    // assert connected items copied, made new versions
    
    // assert connected items of new version have new start_date
    FilterComponent filterByMrId = new FilterComponent("main_resource_id", "=", "" + old.getMain_resource_id());
    List<Document> connectedDocs = genericDAO.find(Document.class, null, null,Arrays.asList(filterByMrId), null);
    
    System.out.println("CONNECTED DOCS LIST: " + connectedDocs + " ID " + connectedDocs.get(0).getDocument_id());
    
    assertEquals(1, connectedDocs.size());
    assertEquals(modifiedDateJson.getTime(), connectedDocs.get(0).getModified_date().getTime());
    assertEquals(startDateJson.getTime(), connectedDocs.get(0).getStart_date().getTime());
    assertNull(connectedDocs.get(0).getEnd_date());
    
    // assert connected items of archived have end_date
    FilterComponent filterByArchivedId = new FilterComponent("main_resource_id", "=", "" + justArchived.getMain_resource_id());
    List<Document> connectedArchivedDocs = genericDAO.find(Document.class, null, null,Arrays.asList(filterByArchivedId), null);
    
    System.out.println("CONNECTED ARCHIVED DOCS LIST: " + connectedArchivedDocs + " ID " + connectedArchivedDocs.get(0).getDocument_id());
    
    assertEquals(1, connectedArchivedDocs.size());
    // add to enable file delete afterwards
    connectedDocIds.add(connectedArchivedDocs.get(0).getDocument_id());
    
    assertEquals(modifiedDateJson.getTime(), connectedArchivedDocs.get(0).getModified_date().getTime());
    assertEquals(modifiedDateJson.getTime(), connectedArchivedDocs.get(0).getEnd_date().getTime());
    // because test data has start_date null and this method must not alter start_date value of archived element
    assertNull(connectedArchivedDocs.get(0).getStart_date());
    // file paths (content) must be different
    assertNotNull(connectedDocs.get(0).getJson_content());
    assertNotNull(connectedDocs.get(0).getJson_content().get("content"));
    assertNotEquals(connectedDocs.get(0).getJson_content().get("content").getAsString(),
        connectedArchivedDocs.get(0).getJson_content().get("content").getAsString());
  }
  
  @Test
  public void testNewVersionWithConnectedData_object() throws Exception {
    
    String newVersion = "v2";
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + uri + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);

    assertEquals(newVersion, jsonContent.get("version").getAsString());
    assertEquals(uri, jsonContent.get("uri").getAsString());
    assertEquals((int) old.getMain_resource_id(), jsonContent.get("main_resource_id").getAsInt());

    Date modifiedDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get(Finals.MODIFIED_DATE).getAsString());
    Date createdDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("creation_date").getAsString());
    Date startDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("start_date").getAsString());

    assertEquals(modifiedDateJson.getTime(), createdDateJson.getTime());
    assertEquals(modifiedDateJson.getTime(), startDateJson.getTime());

    // get just archived version, offset == 1 (ignore current version)
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    FilterComponent fc2 = new FilterComponent("end_date", "isnotnull", null);
    List<Main_resource> justArchivedList = genericDAO.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), null);
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    idUnderTestList.add(justArchived.getMain_resource_id());

    assertEquals(old.getVersion(), justArchived.getVersion());
    assertEquals(uri, justArchived.getUri());
    assertEquals(modifiedDateJson.getTime(), justArchived.getEnd_date().getTime());
    assertFalse(old.getMain_resource_id().equals(justArchived.getMain_resource_id()));

    // assert connected items copied, made new versions
    
    // assert connected items of new version have new start_date
    FilterComponent filterByMrId = new FilterComponent("main_resource_id", "=", "" + old.getMain_resource_id());
    List<Data_object> connectedData_objects = genericDAO.find(Data_object.class, null, null,Arrays.asList(filterByMrId), null);
    
    Data_object connectedData = connectedData_objects.get(0);
    System.out.println("CONNECTED DATA_OBJECT LIST: " + connectedData_objects + " ID " + connectedData.getData_object_id());
    
    assertEquals(1, connectedData_objects.size());
    assertEquals((int)connectedData.getMain_resource_id(), connectedData.getJson_content().get("main_resource_id").getAsInt());
    assertEquals((int)connectedData.getData_object_id(), connectedData.getJson_content().get("data_object_id").getAsInt());
    assertEquals(modifiedDateJson.getTime(), connectedData.getModified_date().getTime());
    assertEquals(startDateJson.getTime(), connectedData.getStart_date().getTime());
    assertNull(connectedData.getEnd_date());
    
    
    // assert connected items of archived have end_date
    FilterComponent filterByArchivedId = new FilterComponent("main_resource_id", "=", "" + justArchived.getMain_resource_id());
    List<Data_object> connectedArchivedData_objects = genericDAO.find(Data_object.class, null, null,Arrays.asList(filterByArchivedId), null);
    
    Data_object archivedData = connectedArchivedData_objects.get(0);
    System.out.println("CONNECTED ARCHIVED DATA_OBJECT LIST: " + connectedArchivedData_objects + " ID " + archivedData.getData_object_id());
    
    assertEquals(1, connectedArchivedData_objects.size());
    assertEquals((int)archivedData.getMain_resource_id(), archivedData.getJson_content().get("main_resource_id").getAsInt());
    assertEquals((int)archivedData.getData_object_id(), archivedData.getJson_content().get("data_object_id").getAsInt());
    assertEquals(modifiedDateJson.getTime(), archivedData.getModified_date().getTime());
    assertEquals(modifiedDateJson.getTime(), archivedData.getEnd_date().getTime());
    // because test data has start_date null and this method must not alter start_date value of archived element
    assertNull(connectedArchivedData_objects.get(0).getStart_date());

  }
  
  @Test
  public void testNewVersionWithConnectedDocumentThroughData_object() throws Exception {
    
    // prepare test, add Document to Data-object
    
    JsonObject docJson = JsonHelper.getFromJson(jsonToUseForCreateDocument);
    docJson.addProperty("data_object_id", connectedData_objectId);
    connectedDocId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, 
        TestFinals.DOCUMENT, docJson.toString());
    System.out.println("CONNECTED DOC ID " + connectedDocId);
    // add to enable file delete afterwards
    connectedDocIds.add(connectedDocId);
    
    String newVersion = "v2";
    String json = "{\"op\":\"newversion\",\"path\": \"db/main_resource/\"," + "\"new_version\": \"" + newVersion
        + "\", " + "\"uri\": \"" + uri + "\", \"token\":\"testToken\"} ";

    Response response = serviceUnderTest.postCGI(json);

    assertNotNull(response.getEntity());

    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);

    assertEquals(newVersion, jsonContent.get("version").getAsString());
    assertEquals(uri, jsonContent.get("uri").getAsString());
    assertEquals((int) old.getMain_resource_id(), jsonContent.get("main_resource_id").getAsInt());

    Date modifiedDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get(Finals.MODIFIED_DATE).getAsString());
    Date createdDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("creation_date").getAsString());
    Date startDateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).parse(jsonContent.get("start_date").getAsString());

    assertEquals(modifiedDateJson.getTime(), createdDateJson.getTime());
    assertEquals(modifiedDateJson.getTime(), startDateJson.getTime());

    // get just archived version, offset == 1 (ignore current version)
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    FilterComponent fc2 = new FilterComponent("end_date", "isnotnull", null);
    List<Main_resource> justArchivedList = genericDAO.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), null);
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    idUnderTestList.add(justArchived.getMain_resource_id());

    assertEquals(old.getVersion(), justArchived.getVersion());
    assertEquals(uri, justArchived.getUri());
    assertEquals(modifiedDateJson.getTime(), justArchived.getEnd_date().getTime());
    assertFalse(old.getMain_resource_id().equals(justArchived.getMain_resource_id()));

    // assert connected items copied, made new versions
    
    // assert connected items of new version have new start_date
    FilterComponent filterByMrId = new FilterComponent("main_resource_id", "=", "" + old.getMain_resource_id());
    List<Data_object> connectedData_objects = genericDAO.find(Data_object.class, null, null,Arrays.asList(filterByMrId), null);
    
    Data_object connectedData = connectedData_objects.get(0);
    System.out.println("CONNECTED DATA_OBJECT LIST: " + connectedData_objects + " ID " + connectedData.getData_object_id());
    
    assertEquals(1, connectedData_objects.size());
    assertEquals((int)connectedData.getMain_resource_id(), connectedData.getJson_content().get("main_resource_id").getAsInt());
    assertEquals((int)connectedData.getData_object_id(), connectedData.getJson_content().get("data_object_id").getAsInt());
    assertEquals(modifiedDateJson.getTime(), connectedData.getModified_date().getTime());
    assertEquals(startDateJson.getTime(), connectedData.getStart_date().getTime());
    assertNull(connectedData.getEnd_date());
    
    
    // assert connected items of archived have end_date
    FilterComponent filterByArchivedId = new FilterComponent("main_resource_id", "=", "" + justArchived.getMain_resource_id());
    List<Data_object> connectedArchivedData_objects = genericDAO.find(Data_object.class, null, null,Arrays.asList(filterByArchivedId), null);
    
    Data_object archivedData = connectedArchivedData_objects.get(0);
    System.out.println("CONNECTED ARCHIVED DATA_OBJECT LIST: " + connectedArchivedData_objects + " ID " + archivedData.getData_object_id());
    
    assertEquals(1, connectedArchivedData_objects.size());
    assertEquals((int)archivedData.getMain_resource_id(), archivedData.getJson_content().get("main_resource_id").getAsInt());
    assertEquals((int)archivedData.getData_object_id(), archivedData.getJson_content().get("data_object_id").getAsInt());
    assertEquals(modifiedDateJson.getTime(), archivedData.getModified_date().getTime());
    assertEquals(modifiedDateJson.getTime(), archivedData.getEnd_date().getTime());
    // because test data has start_date null and this method must not alter start_date value of archived element
    assertNull(archivedData.getStart_date());
    
    // assert documents new versions are same
    FilterComponent filterByDataId = new FilterComponent("data_object_id", "=", "" + connectedData.getData_object_id());
    List<Document> connectedDocuments = genericDAO.find(Document.class, null, null,Arrays.asList(filterByDataId), null);
    System.out.println(JsonHelper.GSON.toJson(connectedDocuments));
    

    
    assertEquals(1, connectedDocuments.size());
    Document connectedDocument = connectedDocuments.get(0);
    assertEquals((int)connectedDocument.getData_object_id(), 
        connectedDocument.getJson_content().get("data_object_id").getAsInt());
    assertNull(connectedDocument.getMain_resource_id());
    assertNull(connectedDocument.getEnd_date());
    assertEquals(startDateJson.getTime(), connectedDocument.getStart_date().getTime());
    
    // assert archived documents are different
    FilterComponent filterByArchivedDataId = new FilterComponent("data_object_id", "=", "" + archivedData.getData_object_id());
    List<Document> archivedDocuments = genericDAO.find(Document.class, null, null,Arrays.asList(filterByArchivedDataId), null);
    assertEquals(1, archivedDocuments.size());
    Document archivedDocument = archivedDocuments.get(0);
    assertEquals((int)archivedDocument.getData_object_id(), 
        archivedDocument.getJson_content().get("data_object_id").getAsInt());
    assertNull(archivedDocument.getMain_resource_id());
    assertEquals(modifiedDateJson.getTime(), archivedDocument.getEnd_date().getTime());

    assertNotEquals(connectedDocument.getData_object_id(), archivedDocument.getData_object_id());
  }
}
