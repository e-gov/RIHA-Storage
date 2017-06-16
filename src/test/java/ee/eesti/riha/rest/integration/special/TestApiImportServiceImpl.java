package ee.eesti.riha.rest.integration.special;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonElement;
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
import ee.eesti.riha.rest.logic.NewVersionLogic;
import ee.eesti.riha.rest.logic.URI;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.service.ApiClassicService;
import ee.eesti.riha.rest.service.ApiImportService;

@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiImportServiceImpl {
  
  @Autowired
  WebClient webClient;
  
  @Autowired
  ApiGenericDAO<Data_object, Integer> genericDAO;
  
  @Autowired
  ApiGenericDAO<Main_resource, Integer> mainResourceDAO;
  
  @Autowired
  ApiGenericDAO<Document, Integer> documentDAO;
  
  @Autowired
  NewVersionLogic<Main_resource, Integer> newVersionLogic;

  // service under test info here
  private static ApiImportService serviceUnderTest;
  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<>();
  private static List<Integer> mrIdUnderTestList = new ArrayList<>();
  
  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static String tableUnderTestMain_resource = TestFinals.MAIN_RESOURCE;
  private static Integer main_resourceId;
  
  private static String tableUnderTest = TestFinals.DATA_OBJECT;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_DATA_OBJECT_CORRECT_SAMPLE_AS_JSON_STRING_FOR_RESOURCE;
  private static String tableUnderTestDoc = TestFinals.DOCUMENT;
  private static String jsonToUseForCreateDoc = TestFinals.JSON_CONTENT_FOR_DOCUMENT_CORRECT_SAMPLE_AS_JSON_STRING_SHORTER_FOR_RESOURCE;
  private static Integer documentId;
  
  // field_names
  private static final String TEST_KINDS = "infosystemTests";
  private static final String TEST_KINDS_DOC = "documentTests";
  private static final String TEST_KINDS_SERVICE = "services";

  @Before
  public void beforeTest() {
    if (idUnderTestList.size() == 0) {

      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiImportService.class, true);
      // create test Main_resource
      if (main_resourceId == null) {
        main_resourceId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData,
            tableUnderTestMain_resource, jsonToUseForCreateMain_resource);
      }
      // add created Main_resource id to json
      // data_object
      JsonObject jsonObject = JsonHelper.toJsonObject(jsonToUseForCreate);
      jsonObject.addProperty("main_resource_id", main_resourceId);
      jsonToUseForCreate = jsonObject.toString();
      // create Data_object from modified json
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          jsonToUseForCreate));

      // document
      JsonObject jsonObjectDoc = JsonHelper.toJsonObject(jsonToUseForCreateDoc);
      jsonObjectDoc.addProperty("main_resource_id", main_resourceId);
      jsonToUseForCreateDoc = jsonObjectDoc.toString();
      if (documentId == null) {
        documentId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTestDoc,
            jsonToUseForCreateDoc);
      }
    }
  }

  @After
  public void afterTest() {
    // clean up always
    for (Integer idForTestEntry : idUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTest, idForTestEntry);
    }
    idUnderTestList.clear();
    for (Integer idForTestEntry : mrIdUnderTestList) {
      IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestMain_resource, idForTestEntry);
    }
    mrIdUnderTestList.clear();
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestDoc, documentId);
    documentId = null;
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestMain_resource,
        main_resourceId);
    main_resourceId = null;

  }

  @Test
  public void testUpdateInImport_whenDoesNotExist_thenDelete() throws Exception {
    
    // get created test resource
    Response response = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);
    
    assertNotNull(jsonContent.get(TEST_KINDS).getAsJsonArray());
    assertEquals(1, jsonContent.get(TEST_KINDS).getAsJsonArray().size());
    
//    int dataObjectId = jsonContent.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject().get("data_object_id").getAsInt();
    assertFalse(jsonContent.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject().has("data_object_id"));
    
    int dataObjectId = idUnderTestList.get(0);
    
    // remove data_objects for testing
    jsonContent.remove(TEST_KINDS);
    
    // test update with data-objects field deleted
    Response responseImport = serviceUnderTest.doImport(jsonContent.toString(), Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data_objects no longer exist

    Data_object dataObject = genericDAO.find(Data_object.class, dataObjectId);
    assertNull(dataObject);
    
    // check that data_object no longer exists in resource
    Response responseCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseCheck.getEntity(), JsonObject.class);
    assertNull(jsonContentCheck.get(TEST_KINDS));

    // check that kind is replaced with kind_id in database
    Main_resource main_resource = mainResourceDAO.find(Main_resource.class, main_resourceId);
    assertNull(main_resource.getKind());
    assertNotNull(main_resource.getKind_id());
  }
  
  @Test
  public void testUpdateInImport_whenModified_thenUpdate() throws Exception {
    
    // get created test resource
    Response response = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);
    
    // modify data
    String shortName = "TEST_ZXC_MR_123";
    String nameModified = "TEST_123_XYZ_UPDATED";
    
    jsonContent.addProperty("short_name", shortName);
    
    JsonObject jsonDataObject = jsonContent.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject();
    jsonDataObject.addProperty("name", nameModified);
    // ids are removed in export
//    int data_objectId = jsonDataObject.get("data_object_id").getAsInt();
    assertFalse(jsonDataObject.has("data_object_id"));
    int data_objectId = idUnderTestList.get(0);

    // test update with modifications
    Response responseImport = serviceUnderTest.doImport(jsonContent.toString(), Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is modified
    Response responseCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseCheck.getEntity(), JsonObject.class);
    assertEquals(shortName, jsonContentCheck.get("short_name").getAsString());
    assertEquals(nameModified, jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(0)
        .getAsJsonObject().get("name").getAsString());

    // check that kind is replaced with kind_id in database
    Main_resource main_resource = mainResourceDAO.find(Main_resource.class, main_resourceId);
    assertNull(main_resource.getKind());
    assertNotNull(main_resource.getKind_id());
    
    Data_object data_object = genericDAO.find(Data_object.class, data_objectId);
    assertNull(data_object.getKind());
    assertNotNull(data_object.getKind_id());
  }
  

  @Test
  public void testUpdateInImport_whenNew_thenCreate() throws Exception {
    
    // get created test resource
    Response response = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);
    
    // add new data
    String nameModified = "TEST_123_XYZ_CREATED";
    
    JsonObject jsonDataObject = jsonContent.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject();

    int newDataId = 99999999;
    String newUri = URI.constructUri(jsonDataObject.get("kind").getAsString(), newDataId);
    JsonObject jsonNewData = JsonHelper.getFromJson(jsonDataObject.toString());
    jsonNewData.addProperty("data_object_id", newDataId);
    jsonNewData.addProperty("main_resource_id", 888888888);
    jsonNewData.addProperty("name", nameModified);
    jsonNewData.addProperty("uri", newUri);
    
    jsonContent.get(TEST_KINDS).getAsJsonArray().add(jsonNewData);
    
    System.out.println("TEST IMPORT RESPONSE\n\n\n");
    // test update with new data
    Response responseImport = serviceUnderTest.doImport(jsonContent.toString(), Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    Response responseCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseCheck.getEntity(), JsonObject.class);
    assertEquals(2, jsonContentCheck.get(TEST_KINDS).getAsJsonArray().size());
    
    // get added element index
    int index = 0;
    for (JsonElement je : jsonContentCheck.get(TEST_KINDS).getAsJsonArray()) {
      JsonObject jsonObj = (JsonObject) je;
      if (newUri.equals(jsonObj.get("uri").getAsString())) {
        break;
      }
      index++;
    }
    assertEquals(nameModified, jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(index)
        .getAsJsonObject().get("name").getAsString());
    assertEquals(newUri, jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(index)
        .getAsJsonObject().get("uri").getAsString());
    
    // check that kind is replaced with kind_id in database
    Main_resource main_resource = mainResourceDAO.find(Main_resource.class, main_resourceId);
    assertNull(main_resource.getKind());
    assertNotNull(main_resource.getKind_id());
    
    // ids are removed in export
//    int data_objectId = jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(index)
//        .getAsJsonObject().get("data_object_id").getAsInt();
    assertFalse(jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(index)
        .getAsJsonObject().has("data_object_id"));
    
    FilterComponent filterUriData = new FilterComponent("main_resource_id", "=", "" + main_resourceId);
    Data_object data_object = genericDAO.find(Data_object.class, 1, 0, Arrays.asList(filterUriData), null).get(0);
    assertNull(data_object.getKind());
    assertNotNull(data_object.getKind_id());
  }
  
  @Test
  public void testCreateInImport_whenMain_resourceDoesNotExist_thenCreate() throws Exception {

    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    String filter = "uri,=," + TestFinals.IMPORT_SERVICE_JSON_URI;
    Response responseCheck = serviceHelpingCreateDeleteTestData.getMany("main_resource", null, null, filter, null, null);
    List<JsonObject> jsonObjects = TestHelper.getJsonContentList(TestHelper.readStream((InputStream) responseCheck.getEntity()));
    assertEquals(1, jsonObjects.size());
    
    int main_resourceId = jsonObjects.get(0).get("main_resource_id").getAsInt();
    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    
    // check that import is successful
    Response responseResourceCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseResourceCheck.getEntity(), JsonObject.class);
    assertEquals(1, jsonContentCheck.get(TEST_KINDS).getAsJsonArray().size());
    assertEquals(TestFinals.IMPORT_SERVICE_JSON_URI_DATA, jsonContentCheck.get(TEST_KINDS)
        .getAsJsonArray().get(0).getAsJsonObject().get("uri").getAsString());
    assertEquals(TestFinals.IMPORT_SERVICE_JSON_URI_DOC, jsonContentCheck.get(TEST_KINDS_DOC)
        .getAsJsonArray().get(0).getAsJsonObject().get("uri").getAsString());

    
    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_JSON_URI);
    FilterComponent filterUriData = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_JSON_URI_DATA);

    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), null);
    List<Data_object> dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), null);
    
    assertEquals(1, mainResources.size());
    assertEquals(1, dataObjects.size());
    
    // check that kind is replaced with kind_id in database
    Main_resource main_resource = mainResources.get(0);
    assertNull(main_resource.getKind());
    assertNotNull(main_resource.getKind_id());
    
    // ids are removed in export
//    int data_objectId = jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(0)
//        .getAsJsonObject().get("data_object_id").getAsInt();
    assertFalse(jsonContentCheck.get(TEST_KINDS).getAsJsonArray().get(0)
        .getAsJsonObject().has("data_object_id"));
    
    Data_object data_object = dataObjects.get(0);
    assertNull(data_object.getKind());
    assertNotNull(data_object.getKind_id());
  }

  @Test
  public void testNewVersionInImport_whenSameUriAndVersionExists_thenError() throws Exception {

    // get created test resource
    Response response = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);
    
    String oldVersion = jsonContent.get("version").getAsString();
    String uri = jsonContent.get("uri").getAsString();
    
    // create new version
    String newVersion = "v6";
    ObjectNode newVersionObject = (ObjectNode) newVersionLogic.doNewVersion(newVersion, uri);

    // get just archived version
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    FilterComponent fc2 = new FilterComponent("end_date", "isnotnull", null);
    List<Main_resource> justArchivedList = mainResourceDAO.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), null);
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    mrIdUnderTestList.add(justArchived.getMain_resource_id());
    
    // try to do import with old version 
    int mrIdNew = newVersionObject.get("main_resource_id").asInt();
    
    System.out.println("\n" + mrIdUnderTestList);
    System.out.println("\n" + mrIdNew);
    
    // get current
    Response newResponse = serviceHelpingCreateDeleteTestData.getResourceById(mrIdNew);
    JsonObject newJsonContent = TestHelper.getObjectFromClient((InputStream) newResponse.getEntity(), JsonObject.class);
    assertNotNull(newJsonContent);
    
    // set version in current to old
    newJsonContent.addProperty("version", oldVersion);
    
    Response errorResponse = serviceUnderTest.doImport(newJsonContent.toString(), Finals.TEST_TOKEN);
    RihaRestError error = TestHelper.getObjectFromClient((InputStream) errorResponse.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.VERSION_MUST_BE_UPDATED, error.getErrcode());
    assertTrue(error.getErrmsg().startsWith(ErrorCodes.VERSION_MUST_BE_UPDATED_MSG));

  }
  
  @Test
  public void testNewVersionInImport_whenNewVersion_thenSuccess() throws Exception {

    // get created test resource
    Response response = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);
    
    String oldVersion = jsonContent.get("version").getAsString();
    String uri = jsonContent.get("uri").getAsString();
    
    // make modifications for new version
    JsonObject modifiedJson = JsonHelper.getFromJson(jsonContent.toString());

    String newName = "Test_IMPORT_NEW_NAME_ZXC_123";
    String newVersion = "v7-alpha";
    String newNameData = "Test_IMPORT_NEW_DATA_OBJECT_ZXC_123";
    
    modifiedJson.addProperty("name", newName);
    modifiedJson.addProperty("version", newVersion);
    
    JsonObject dataObjectJson = modifiedJson.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject();
    dataObjectJson.addProperty("name", newNameData);
    
    Response importResponse = serviceUnderTest.doImport(modifiedJson.toString(), Finals.TEST_TOKEN);
    String importText = TestHelper.getObjectFromClient((InputStream) importResponse.getEntity(), String.class);
    assertEquals(Finals.OK, importText);


    // get just archived version
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    FilterComponent fc2 = new FilterComponent("end_date", "isnotnull", null);
    List<Main_resource> justArchivedList = mainResourceDAO.find(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), null);
    Main_resource justArchived = justArchivedList.get(0);
    // add to be deleted after
    mrIdUnderTestList.add(justArchived.getMain_resource_id());
    
    int archivedMrId = justArchived.getMain_resource_id();
    
    // get archived resource
    Response archivedResource = serviceHelpingCreateDeleteTestData.getResourceById(archivedMrId);
    JsonObject archivedJson = TestHelper.getObjectFromClient((InputStream) archivedResource.getEntity(), JsonObject.class);
    assertNotNull(archivedJson);
    
    assertEquals(jsonContent.get("name").getAsString(), archivedJson.get("name").getAsString());
    assertEquals(jsonContent.get("version").getAsString(), archivedJson.get("version").getAsString());
    assertEquals(jsonContent.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString(), 
        archivedJson.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());
    
    
    // get updated new version resource
    Response updatedResource = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject updatedJson = TestHelper.getObjectFromClient((InputStream) updatedResource.getEntity(), JsonObject.class);
    assertNotNull(updatedJson);
    
    assertEquals(newName, updatedJson.get("name").getAsString());
    assertEquals(newVersion, updatedJson.get("version").getAsString());
    assertEquals(newNameData, 
        updatedJson.get(TEST_KINDS).getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString());


  }

  @Test
  public void testCreateInImport_nestedDocumentInData_object_thenCreate() throws Exception {
    // using default field_names from resource request, because field_name fields are null
    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    String filter = "uri,=," + TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI;
    Response responseCheck = serviceHelpingCreateDeleteTestData.getMany("main_resource", null, null, filter, null, null);
    List<JsonObject> jsonObjects = TestHelper.getJsonContentList(TestHelper.readStream((InputStream) responseCheck.getEntity()));
    assertEquals(1, jsonObjects.size());
    
    int main_resourceId = jsonObjects.get(0).get("main_resource_id").getAsInt();
    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    
    // check that import is successful
    Response responseResourceCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseResourceCheck.getEntity(), JsonObject.class);
    assertNull(jsonContentCheck.get(TEST_KINDS));
    assertEquals(1, jsonContentCheck.get(Finals.DEFAULT_DATA).getAsJsonArray().size());
    assertEquals(TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DATA, jsonContentCheck.get(Finals.DEFAULT_DATA)
        .getAsJsonArray().get(0).getAsJsonObject().get("uri").getAsString());
    
    // check that nested Document in Data_object has correct uri
    assertEquals(TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DOC, jsonContentCheck.get(Finals.DEFAULT_DATA)
        .getAsJsonArray().get(0).getAsJsonObject().get(Finals.DEFAULT_DOC)
        .getAsJsonArray().get(0).getAsJsonObject().get("uri").getAsString());
    

    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI);
    FilterComponent filterUriDoc = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DOC);
    FilterComponent filterUriData = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DATA);

    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), null);
    List<Document> documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    List<Data_object> dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), null);
    
    assertEquals(1, mainResources.size());
    assertEquals(1, documents.size());
    assertEquals(1, dataObjects.size());
    
    // check that kind is replaced with kind_id in database
    Main_resource main_resource = mainResources.get(0);
    assertNull(main_resource.getKind());
    assertNotNull(main_resource.getKind_id());
    
    // ids are removed in export
//    int data_objectId = jsonContentCheck.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0)
//        .getAsJsonObject().get("data_object_id").getAsInt();
    assertFalse(jsonContentCheck.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0)
        .getAsJsonObject().has("data_object_id"));
    
    Data_object data_object = dataObjects.get(0);
    assertNull(data_object.getKind());
    assertNotNull(data_object.getKind_id());
    assertEquals(main_resource.getMain_resource_id(), data_object.getMain_resource_id());
    
//    int dataObjectDocumentId = jsonContentCheck.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0)
//        .getAsJsonObject().get(Finals.DEFAULT_DOC).getAsJsonArray().get(0)
//        .getAsJsonObject().get("document_id").getAsInt();
    assertFalse(jsonContentCheck.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0)
        .getAsJsonObject().get(Finals.DEFAULT_DOC).getAsJsonArray().get(0)
        .getAsJsonObject().has("document_id"));
    
    Document document = documents.get(0);
    assertNull(document.getKind());
    assertNotNull(document.getKind_id());
    assertEquals((Integer)data_object.callGetId(), document.getData_object_id());
  }
  
  @Test
  public void testNewVersionInImport_nestedDocument_noChanges_thenSuccess() throws Exception {
    // using default field_names from resource request, because field_name fields are null
    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    String filter = "uri,=," + TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI;
    Response responseCheck = serviceHelpingCreateDeleteTestData.getMany("main_resource", null, null, filter, null, null);
    List<JsonObject> jsonObjects = TestHelper.getJsonContentList(TestHelper.readStream((InputStream) responseCheck.getEntity()));
    assertEquals(1, jsonObjects.size());
    
    int main_resourceId = jsonObjects.get(0).get("main_resource_id").getAsInt();
    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    
    Response responseToUpdate = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    
    assertNotNull(responseToUpdate.getEntity());
    JsonObject resourceJson = TestHelper.getObjectFromClient((InputStream) responseToUpdate.getEntity(), JsonObject.class);
    assertNotNull(resourceJson);

    String oldVersion = resourceJson.get("version").getAsString();
    
    // check that only 1 version uri exists
    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI);
    FilterComponent filterUriDoc = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DOC);
    FilterComponent filterUriData = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DATA);
    
    List<Document> documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    List<Data_object> dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), null);
    
    assertEquals(1, documents.size());
    assertEquals(1, dataObjects.size());
    
    // change version
    String newVersion = "v8_test";
    resourceJson.addProperty("version", newVersion);
    
    // do new version
    Response newVersionImport = serviceUnderTest.doImport(resourceJson.toString(), Finals.TEST_TOKEN);
    assertNotNull(newVersionImport.getEntity());
    String importNewVersionText = TestHelper.getObjectFromClient((InputStream) newVersionImport.getEntity(), String.class);
    assertEquals(Finals.OK, importNewVersionText);

    // check that 2 version exist
    // when DESC then NULL is before, and current has end_date NULL in this example
    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), "-end_date");
    
    // add created copy to be deleted afterwards
    mrIdUnderTestList.add(mainResources.get(0).getMain_resource_id());
    mrIdUnderTestList.add(mainResources.get(1).getMain_resource_id());
    
    assertEquals(main_resourceId, (int)mainResources.get(0).getMain_resource_id());
    assertNotEquals(main_resourceId, (int)mainResources.get(1).getMain_resource_id());
    
    documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), null);
    
    assertEquals(2, mainResources.size());
    assertEquals(2, dataObjects.size());
    assertEquals(2, documents.size());


    assertEquals(newVersion, mainResources.get(0).getVersion());
    assertEquals(oldVersion, mainResources.get(1).getVersion());
  }
  
  @Test
  public void testNewVersionInImport_nestedDocument_withChanges_thenSuccess() throws Exception {
    // using default field_names from resource request, because field_name fields are null
    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    String filter = "uri,=," + TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI;
    Response responseCheck = serviceHelpingCreateDeleteTestData.getMany("main_resource", null, null, filter, null, null);
    List<JsonObject> jsonObjects = TestHelper.getJsonContentList(TestHelper.readStream((InputStream) responseCheck.getEntity()));
    assertEquals(1, jsonObjects.size());
    
    int main_resourceId = jsonObjects.get(0).get("main_resource_id").getAsInt();
    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    
    Response responseToUpdate = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    
    assertNotNull(responseToUpdate.getEntity());
    JsonObject resourceJson = TestHelper.getObjectFromClient((InputStream) responseToUpdate.getEntity(), JsonObject.class);
    assertNotNull(resourceJson);

    String oldVersion = resourceJson.get("version").getAsString();
    
    // check that only 1 version uri exists
    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI);
    FilterComponent filterUriDoc = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DOC);
    FilterComponent filterUriData = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DATA);
    
    List<Document> documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    List<Data_object> dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), null);
    
    assertEquals(1, documents.size());
    assertEquals(1, dataObjects.size());
    
    // change version
    String newName = "TEST_NEW_VERSION_IMPORT_MAIN_RESOURCE_123_zxc";
    String newNameData = "TEST_NEW_VERSION_IMPORT_Data_object_123_xyz";
    String newFileNameDoc = "TEST_NEW_VERSION_IMPORT_Document_123_xyz.json";
    String newVersion = "v8_test";
    
    // update json
    resourceJson.addProperty("version", newVersion);
    resourceJson.addProperty("name", newName);
    resourceJson.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0).getAsJsonObject().addProperty("name", newNameData);
    resourceJson.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0).getAsJsonObject()
      .get(Finals.DEFAULT_DOC).getAsJsonArray().get(0).getAsJsonObject().addProperty("filename", newFileNameDoc);
    
    // do new version
    Response newVersionImport = serviceUnderTest.doImport(resourceJson.toString(), Finals.TEST_TOKEN);
    assertNotNull(newVersionImport.getEntity());
    String importNewVersionText = TestHelper.getObjectFromClient((InputStream) newVersionImport.getEntity(), String.class);
    assertEquals(Finals.OK, importNewVersionText);

    // check that 2 version exist
    // when DESC then NULL is before, and current has end_date NULL in this example
    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), "-end_date");
    
    // add created copy to be deleted afterwards
    mrIdUnderTestList.add(mainResources.get(0).getMain_resource_id());
    mrIdUnderTestList.add(mainResources.get(1).getMain_resource_id());
    
    assertEquals(main_resourceId, (int)mainResources.get(0).getMain_resource_id());
    assertNotEquals(main_resourceId, (int)mainResources.get(1).getMain_resource_id());
    
 // when DESC then NULL is before, and current has end_date NULL in this example
    documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), "-end_date");
    dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), "-end_date");
    
    assertEquals(2, mainResources.size());
    assertEquals(2, dataObjects.size());
    assertEquals(2, documents.size());

    assertEquals(newVersion, mainResources.get(0).getVersion());
    assertEquals(oldVersion, mainResources.get(1).getVersion());
    
    assertEquals(newName, mainResources.get(0).getName());
    assertEquals(newNameData, dataObjects.get(0).getName());
    assertEquals(newFileNameDoc, documents.get(0).getFilename());
    
    assertNotEquals(newName, mainResources.get(1).getName());
    assertNotEquals(newNameData, dataObjects.get(1).getName());
    assertNotEquals(newFileNameDoc, documents.get(1).getFilename());
    
  }
  
  @Test
  public void testUpdateInImport_nestedDocument_thenSuccess() throws Exception {
    // using default field_names from resource request, because field_name fields are null
    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    String filter = "uri,=," + TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI;
    Response responseCheck = serviceHelpingCreateDeleteTestData.getMany("main_resource", null, null, filter, null, null);
    List<JsonObject> jsonObjects = TestHelper.getJsonContentList(TestHelper.readStream((InputStream) responseCheck.getEntity()));
    assertEquals(1, jsonObjects.size());
    
    int main_resourceId = jsonObjects.get(0).get("main_resource_id").getAsInt();
    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    
    Response responseToUpdate = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    
    assertNotNull(responseToUpdate.getEntity());
    JsonObject resourceJson = TestHelper.getObjectFromClient((InputStream) responseToUpdate.getEntity(), JsonObject.class);
    assertNotNull(resourceJson);
    
    String newName = "Test_Import_update_Main_resource_123_xyz";
    String newNameData = "Test_Import_update_Data_object_123_xyz";
    String newFileNameDoc = "Test_Import_update_Document_123_xyz.json";
    
    // update json
    resourceJson.addProperty("name", newName);
    resourceJson.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0).getAsJsonObject().addProperty("name", newNameData);
    resourceJson.get(Finals.DEFAULT_DATA).getAsJsonArray().get(0).getAsJsonObject()
      .get(Finals.DEFAULT_DOC).getAsJsonArray().get(0).getAsJsonObject().addProperty("filename", newFileNameDoc);
    
    // do new version
    Response updateImport = serviceUnderTest.doImport(resourceJson.toString(), Finals.TEST_TOKEN);
    assertNotNull(updateImport.getEntity());
    String importNewVersionText = TestHelper.getObjectFromClient((InputStream) updateImport.getEntity(), String.class);
    assertEquals(Finals.OK, importNewVersionText);


    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI);
    FilterComponent filterUriDoc = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DOC);
    FilterComponent filterUriData = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_DOC_JSON_URI_DATA);

    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), null);
    List<Document> documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    List<Data_object> dataObjects = genericDAO.find(Data_object.class, null, null, Arrays.asList(filterUriData), null);
    
    assertEquals(1, mainResources.size());
    assertEquals(1, documents.size());
    assertEquals(1, dataObjects.size());
    
    assertEquals(newName, mainResources.get(0).getName());
    assertEquals(newFileNameDoc, documents.get(0).getFilename());
    assertEquals(newNameData, dataObjects.get(0).getName());
    
  }
  
  @Test
  public void testCreateInImportWithNestedServiceInMain_resource() throws Exception {

    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    // get ids
    
    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI);
    FilterComponent filterUriDoc = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI_DOC);
    FilterComponent filterUriService = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI_SERVICE);

    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), null);
    List<Document> documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    List<Main_resource> services = mainResourceDAO.find(Main_resource.class, null, null, Arrays.asList(filterUriService), null);
    
    assertEquals(1, mainResources.size());
    assertEquals(1, documents.size());
    assertEquals(1, services.size());
    
    int main_resourceId = mainResources.get(0).callGetId();
    int serviceId = services.get(0).callGetId();
    int documentId = documents.get(0).callGetId();

    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    mrIdUnderTestList.add(serviceId);
    
    // -get ids
    
    // check that import is successful
    Response responseResourceCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseResourceCheck.getEntity(), JsonObject.class);
    assertEquals(1, jsonContentCheck.get(TEST_KINDS_SERVICE).getAsJsonArray().size());
    assertEquals(TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI_SERVICE, jsonContentCheck.get(TEST_KINDS_SERVICE)
        .getAsJsonArray().get(0).getAsJsonObject().get("uri").getAsString());
    // check nested doc in nested service
    assertEquals(TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI_DOC, jsonContentCheck.get(TEST_KINDS_SERVICE)
        .getAsJsonArray().get(0).getAsJsonObject().get(TEST_KINDS_DOC)
        .getAsJsonArray().get(0).getAsJsonObject().get("uri").getAsString());
    
    // check that kind is replaced with kind_id in database
    Main_resource main_resource = mainResourceDAO.find(Main_resource.class, main_resourceId);
    assertNull(main_resource.getKind());
    assertNotNull(main_resource.getKind_id());
    // assert nested service is extracted
    assertFalse(main_resource.getJson_content().has(TEST_KINDS_SERVICE));
    
    Main_resource nestedService = mainResourceDAO.find(Main_resource.class, serviceId);
    assertNull(nestedService.getKind());
    assertNotNull(nestedService.getKind_id());
    assertFalse(nestedService.getJson_content().has(TEST_KINDS_DOC));
    assertEquals((Integer)main_resourceId, nestedService.getMain_resource_parent_id());
    
    Document document = documentDAO.find(Document.class, documentId);
    assertNull(document.getKind());
    assertNotNull(document.getKind_id());
    assertEquals((Integer)serviceId, document.getMain_resource_id());
  }
  
  @Test
  public void testUpdateInImportWithNestedServiceInMain_resource() throws Exception {

    Response responseImport = serviceUnderTest.doImport(TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON, Finals.TEST_TOKEN);
    
    assertNotNull(responseImport.getEntity());
    String importText = TestHelper.getObjectFromClient((InputStream) responseImport.getEntity(), String.class);
    assertEquals(Finals.OK, importText);
    
    // check that data is created
    // get ids
    
    FilterComponent filterUriMain = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI);
    FilterComponent filterUriDoc = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI_DOC);
    FilterComponent filterUriService = new FilterComponent("uri", "=", TestFinals.IMPORT_SERVICE_NESTED_SERVICE_WITH_DOC_JSON_URI_SERVICE);

    List<Main_resource> mainResources = mainResourceDAO.find(Main_resource.class, null, null, 
        Arrays.asList(filterUriMain), null);
    List<Document> documents = documentDAO.find(Document.class, null, null, Arrays.asList(filterUriDoc), null);
    List<Main_resource> services = mainResourceDAO.find(Main_resource.class, null, null, Arrays.asList(filterUriService), null);
    
    assertEquals(1, mainResources.size());
    assertEquals(1, documents.size());
    assertEquals(1, services.size());
    
    int main_resourceId = mainResources.get(0).callGetId();
    int serviceId = services.get(0).callGetId();
    int documentId = documents.get(0).callGetId();

    // add to be deleted afterwards
    mrIdUnderTestList.add(main_resourceId);
    mrIdUnderTestList.add(serviceId);
    
    // -get ids
    
    // export data
    Response responseResourceCheck = serviceHelpingCreateDeleteTestData.getResourceById(main_resourceId);
    JsonObject jsonContentCheck = TestHelper.getObjectFromClient((InputStream) responseResourceCheck.getEntity(), JsonObject.class);
    
    // update nested service
    
    String nameUpdated = "nestedServiceUpdated123";
    String nameUpdatedDoc = "nestedDocUpdated123";
    JsonObject serviceObject = jsonContentCheck.get(TEST_KINDS_SERVICE).getAsJsonArray().get(0)
        .getAsJsonObject();
    serviceObject.addProperty("name", nameUpdated);
    JsonObject documentObject  = serviceObject.get(TEST_KINDS_DOC).getAsJsonArray().get(0).getAsJsonObject();
    documentObject.addProperty("name", nameUpdatedDoc);
    
    // call update method with modified json
    Response responseImportUpdate = serviceUnderTest.doImport(jsonContentCheck.toString(), Finals.TEST_TOKEN);
    String importUpdateText = TestHelper.getObjectFromClient((InputStream) responseImportUpdate.getEntity(), String.class);
    assertEquals(Finals.OK, importUpdateText);
    
    // check that update is successful
    
    Main_resource nestedService = mainResourceDAO.find(Main_resource.class, serviceId);
    assertEquals(nameUpdated, nestedService.getName());
    assertFalse(nestedService.getJson_content().has(TEST_KINDS_DOC));
    assertEquals((Integer)main_resourceId, nestedService.getMain_resource_parent_id());
    
    Document document = documentDAO.find(Document.class, documentId);
    assertEquals(nameUpdatedDoc, document.getName());
    assertEquals((Integer)serviceId, document.getMain_resource_id());

  }
}
