package ee.eesti.riha.rest.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.cxf.common.util.Base64Utility;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.model.Document;

public class TestFileHelper {

	@Test
	public void testCreateDirectory() throws IOException {
		
		String fullPath = FileHelper.PATH_ROOT + "/00/" + "test123";
		Path file = Paths.get(fullPath);
		try {
			
			Files.createDirectories(file); // creates parents
			
		} catch (FileAlreadyExistsException e) {
			
			Files.delete(file);
			Files.createDirectories(file);
			
		} finally {
			
			Files.delete(file);
			
		} // -finally
		
	} // -testCreateDirectory

  @Test
  public void testFileCreateReadDelete() throws Exception {
    int id = 12345;
    String path = FileHelper.createDocumentFilePath(id);
    String testData = "TEST DATA";
    String fullPath = FileHelper.PATH_ROOT + path;
    FileHelper.writeFile(fullPath, testData);

    List<String> lines = FileHelper.readFile(fullPath);
    assertNotNull(lines);
    assertEquals(testData, lines.get(0));

    FileHelper.deleteFile(fullPath);
  }

  @Test
  public void testReadFileToDocument() throws Exception {
    int id = 12345;
    String path = FileHelper.createDocumentFilePath(id);
    String testData = "TEST DATA";
    String fullPath = FileHelper.PATH_ROOT + path;
    FileHelper.writeFile(fullPath, testData);

    List<String> lines = FileHelper.readFile(fullPath);
    assertNotNull(lines);
    assertEquals(testData, lines.get(0));

    ObjectMapper mapper = new ObjectMapper();
    JsonNode actualObj = mapper.readTree("{\"document_id\": 12345,"
        + "\"content\": \"00\\/\\/00\\/\\/01\\/\\/23\\/\\/45\"}");

    FileHelper.readDocumentFileToContent((ObjectNode) actualObj, Document.class);
    System.out.println(actualObj);

    byte[] bytes = Base64Utility.decode(actualObj.get("content").asText());

    Path newPath = Paths.get(FileHelper.PATH_ROOT + "/00/" + "test1234");
    Files.write(newPath, bytes);

    lines = FileHelper.readFile(newPath.toString());
    System.out.println(lines);
    assertEquals(testData, lines.get(0));

    FileHelper.deleteFile(fullPath);
    FileHelper.deleteFile(newPath.toString());
  }

}
