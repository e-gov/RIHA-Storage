package ee.eesti.riha.rest.integration.special;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
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
import ee.eesti.riha.rest.service.FileService;

@RunWith(MyTestRunner.class)
@ContextConfiguration("classpath*: **/integration-test-applicationContext.xml")
public class TestFileServiceImpl {

  // general info here
  @Autowired
  WebClient webClient;
  private static ApiClassicService serviceHelpingCreateDeleteTestData;
  private static List<Integer> idUnderTestList = new ArrayList<>();

  // service under test info here
  private static FileService serviceUnderTest;
  // table under test info here
  private static String tableUnderTest = TestFinals.DOCUMENT;
  private static String jsonToUseForCreate = TestFinals.JSON_CONTENT_FOR_DOCUMENT_CORRECT_SAMPLE_AS_JSON_STRING;

  private static String jsonToUseForCreateMain_resource = TestFinals.JSON_CONTENT_FOR_MAIN_RESOURCE_CORRECT_SAMPLE_AS_JSON_STRING;
  private static Integer main_resourceId;
  
  @Before
  public void beforeTest() {
    if (idUnderTestList.size() == 0) {
      webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
      serviceHelpingCreateDeleteTestData = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
      serviceUnderTest = JAXRSClientFactory.fromClient(webClient, FileService.class);
      
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
      
      // create faulty document, that has no content to test FileService error
      jsonObject.remove("content");
      jsonObject.remove("filename");
      idUnderTestList.add(IntegrationTestHelper.addTestDataToDB(serviceHelpingCreateDeleteTestData, tableUnderTest,
          jsonObject.toString()));
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
  public void testGetFile() throws Exception {

    Response response = serviceUnderTest.getFile(idUnderTestList.get(0), "testToken");

    assertNotNull(response.getEntity());

    String content = TestHelper.readStream((InputStream) response.getEntity());
    assertFalse(StringUtils.isEmpty(content));
    assertTrue(content.startsWith("<?xml version"));
  }
  
  @Test
  public void testGetFileError() throws Exception {
    int documentId = idUnderTestList.get(1);
    Response response = serviceUnderTest.getFile(documentId, "testToken");

    assertNotNull(response.getEntity());

    RihaRestError error = TestHelper.getObjectFromClient((InputStream) response.getEntity(), RihaRestError.class);
    assertNotNull(error);
    assertEquals(ErrorCodes.DOCUMENT_FILE_NOT_FOUND, error.getErrcode());
    assertEquals(ErrorCodes.DOCUMENT_FILE_NOT_FOUND_MSG, error.getErrmsg());
    assertTrue(error.getErrtrace().contains("" + documentId));
  }

}
