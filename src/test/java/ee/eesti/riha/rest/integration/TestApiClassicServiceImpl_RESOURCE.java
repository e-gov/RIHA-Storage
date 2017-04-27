package ee.eesti.riha.rest.integration;

import static org.junit.Assert.*;

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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ee.eesti.riha.rest.MyTestRunner;
import ee.eesti.riha.rest.TestHelper;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestApiClassicServiceImpl_RESOURCE {

  // general info here
  @Autowired
  WebClient webClient;
  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static String tableUnderTestMain_resource = TestFinals.MAIN_RESOURCE;
  private static Integer main_resourceId;

  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<Integer>();

  // service under test info here
  private static ApiClassicService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.DATA_OBJECT;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_DATA_OBJECT_CORRECT_SAMPLE_AS_JSON_STRING_FOR_RESOURCE;

  private static String tableUnderTestDoc = TestFinals.DOCUMENT;
  private static String jsonToUseForCreateDoc = TestFinals.JSON_CONTENT_FOR_DOCUMENT_CORRECT_SAMPLE_AS_JSON_STRING_SHORTER_FOR_RESOURCE;
  private static Integer documentId;
  private static Integer documentInDataId;

  private static final String TEST_KIND = "infosystem";

  private static final String TEST_KINDS = "infosystemTests";
  private static final String TEST_KINDS_DOC = "documentTests";

  @Before
  public void beforeTest() {
    if (idUnderTestList.size() == 0) {

      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
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
      
      JsonObject jsonObjectDataDoc = JsonHelper.toJsonObject(jsonToUseForCreateDoc);
      jsonObjectDataDoc.addProperty("data_object_id", idUnderTestList.get(0));
      jsonObjectDataDoc.remove("main_resource_id");
      jsonObjectDataDoc.remove("field_name");
      if (documentInDataId == null) {
        documentInDataId = IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTestDoc,
            jsonObjectDataDoc.toString());
      }
    }
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
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestDoc, documentId);
    IntegrationTestHelper.removeTestDataFromDB(serviceHelpingCreateDeleteTestData, tableUnderTestDoc, documentInDataId);
  }

  // for special queries path is not required FIXME TODO

  @Test
  public void testGetById() throws Exception {

    Response response = serviceUnderTest.getResourceById(main_resourceId);
    JsonObject jsonContent = null;

    assertNotNull(response.getEntity());
    jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);

    // data_object
    JsonArray dataObjects = (JsonArray) jsonContent.get(TEST_KINDS);
    assertNotNull(dataObjects);
    assertEquals(1, dataObjects.size());
    JsonObject dataObject = (JsonObject) dataObjects.get(0);

    assertEquals(TEST_KIND, dataObject.get("kind").getAsString());
    assertEquals(1, dataObject.get("trivialFieldHereA").getAsInt());

    // document
    JsonArray documents = (JsonArray) jsonContent.get(TEST_KINDS_DOC);
    assertNotNull(documents);
    assertEquals(1, documents.size());
    JsonObject document = (JsonObject) documents.get(0);

    assertEquals(TEST_KIND, document.get("kind").getAsString());
    assertEquals("Gol", document.get("fieldOnlyInJsonContentTest").getAsString());
    // ids are removed in export
//    assertEquals((int) documentId, document.get("document_id").getAsInt());
    assertFalse(document.has("document_id"));
  }
  
  @Test
  public void testGetByIdWithDocumentInData_object() throws Exception {

    Response response = serviceUnderTest.getResourceById(main_resourceId);
    JsonObject jsonContent = null;

    assertNotNull(response.getEntity());
    jsonContent = TestHelper.getObjectFromClient((InputStream) response.getEntity(), JsonObject.class);
    assertNotNull(jsonContent);

    // data_object
    JsonArray dataObjects = (JsonArray) jsonContent.get(TEST_KINDS);
    assertNotNull(dataObjects);
    assertEquals(1, dataObjects.size());
    JsonObject dataObject = (JsonObject) dataObjects.get(0);

    String defaultDocuments = "default_documents";
    
    assertNotNull(dataObject.get(defaultDocuments));
    assertEquals(1, dataObject.get(defaultDocuments).getAsJsonArray().size());

    // document in data_object
    JsonObject dataObjectDoc = dataObject.get(defaultDocuments).getAsJsonArray().get(0).getAsJsonObject();
    
    assertNull(dataObjectDoc.get("field_name"));
    assertNull(dataObjectDoc.get("main_resource_id"));

  }

}
