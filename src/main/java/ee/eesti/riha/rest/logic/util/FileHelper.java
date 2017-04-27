package ee.eesti.riha.rest.logic.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.util.PropsReader;

// TODO: Auto-generated Javadoc
/**
 * The Class FileHelper.
 */
public final class FileHelper {

  private FileHelper() {

  }

  private static final String TEN_ZEROS = "0000000000";
  private static final int TEN = 10;
  // must escape backslashes
  private static final String PATH_DELIMITER = "\\";
  private static final Charset UTF_8 = StandardCharsets.UTF_8;
  // public static final String PATH_ROOT = "C:\\Users\\Praktikant\\test_folder\\";
  public static final String PATH_ROOT;
  // private static final String PATH_ROOT_LINUX = "/home/girf/";
  private static final String PATH_ROOT_LINUX = PropsReader.get("PATH_ROOT");
  // project folder
  // private static final String PATH_ROOT_WINDOWS = "C:\\Users\\Praktikant\\test_folder\\";
  private static final String PATH_ROOT_WINDOWS = PropsReader.get("PATH_ROOT_WINDOWS");

  private static final Logger LOG = LoggerFactory.getLogger(FileHelper.class);

  // FIXME everything related to this is unnecessary - use maven profile + PATH_ROOT
  static {
    if (!SystemUtils.IS_OS_WINDOWS) {
      PATH_ROOT = PATH_ROOT_LINUX;
    } else {

      PATH_ROOT = PATH_ROOT_WINDOWS;
    }
  }

  /**
   * Convert to linux path if needed.
   *
   * @param path the path
   * @return the string
   */
  private static String convertToLinuxPathIfNeeded(String path) {
    if (!SystemUtils.IS_OS_WINDOWS) {
      return path.replace('\\', '/');
    }
    return path;
  }

  /**
   * create file path from document id e.g <br>
   * 12345 -> "00\\00\\01\\23\\45"
   *
   * @param documentId the document id
   * @return file path
   */
  public static String createDocumentFilePath(int documentId) {
    String idString = TEN_ZEROS + documentId;
    idString = idString.substring(idString.length() - TEN);
    StringBuilder sb = new StringBuilder();
    // split by two numbers
    for (int i = 0; i < TEN; i += 2) {
      sb.append(idString.substring(i, i + 2));

      // don't add if is end of string
      if (i + 2 < idString.length()) {
        sb.append(PATH_DELIMITER);
      }
    }
    return sb.toString();
  }

  /**
   * Creates the document file path with root.
   *
   * @param documentId the document id
   * @return the string
   */
  public static String createDocumentFilePathWithRoot(int documentId) {
    return convertToLinuxPathIfNeeded(PATH_ROOT + createDocumentFilePath(documentId));
  }

  /**
   * Write file.
   *
   * @param fileName the file name
   * @param data the data
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void writeFile(String fileName, String data) throws IOException {
    Path file = Paths.get(convertToLinuxPathIfNeeded(fileName));
    List<String> lines = Arrays.asList(data);

    Files.createDirectories(file.getParent());

    Files.write(file, lines, UTF_8);

    LOG.info("" + readFile(fileName));
  }

  /**
   * Write file.
   *
   * @param fileName the file name
   * @param bytes the bytes
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void writeFile(String fileName, byte[] bytes) throws IOException {
    Path file = Paths.get(convertToLinuxPathIfNeeded(fileName));

    Files.createDirectories(file.getParent());

    Files.write(file, bytes);

    LOG.info("" + readFileBytes(fileName));
  }

  /**
   * Read file.
   *
   * @param fileName the file name
   * @return the list
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List<String> readFile(String fileName) throws IOException {
    Path file = Paths.get(convertToLinuxPathIfNeeded(fileName));
    List<String> lines = null;

    lines = Files.readAllLines(file, UTF_8);

    return lines;
  }

  /**
   * Read file bytes.
   *
   * @param fileName the file name
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static byte[] readFileBytes(String fileName) throws IOException {
    Path file = Paths.get(convertToLinuxPathIfNeeded(fileName));

    return Files.readAllBytes(file);
  }

  /**
   * Delete file.
   *
   * @param fileName the file name
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void deleteFile(String fileName) throws IOException {
    Path file = Paths.get(convertToLinuxPathIfNeeded(fileName));

    Files.deleteIfExists(file);

  }

  /**
   * If item is Document then write content to separate file and replace content with path to created file (generated
   * from id).
   *
   * @param <T> the generic type
   * @param item the item
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static <T> void writeDocumentContentToFile(T item) throws IOException {
    writeDocumentContentToFile(item, ((BaseModel) item).callGetId());
  }

  /**
   * If item is Document then write content to separate file and replace content with path to created file (generated
   * from id).
   *
   * @param <T> the generic type
   * @param item the item
   * @param documentId the document id
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static <T> void writeDocumentContentToFile(T item, Integer documentId) throws IOException {
    if (item.getClass() == Document.class) {
      LOG.info("IS DOCUMENT");
      Document document = (Document) item;
      String pathToFile = FileHelper.createDocumentFilePath(documentId);
      String data = null;
      if (document.getJson_content() != null && document.getJson_content().get("content") != null) {
        data = document.getJson_content().get("content").getAsString();
      } else {
        LOG.info("DOCUMENT DATA IS NULL");
      }

      if (data != null) {
        byte[] fileData = null;
        try {
          fileData = Base64Utility.decode(data);
        } catch (Base64Exception e) {
          e.printStackTrace();
          throw new IOException("Base64 error", e);
        }
        // FileHelper.writeFile(FileHelper.PATH_ROOT + pathToFile, data);
        // FileHelper.writeFile(FileHelper.PATH_ROOT + pathToFile, new String(fileData));
        // write as bytes
        FileHelper.writeFile(FileHelper.PATH_ROOT + pathToFile, fileData);
        document.getJson_content().addProperty("content", pathToFile);
        LOG.info("DOCUMENT WRITTEN TO FILE");
        LOG.info("" + document);
      }
    }
  }

  /**
   * Read document file to content.
   *
   * @param <T> the generic type
   * @param item the item
   * @param classRepresentingTable the class representing table
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void readDocumentFileToContent(T item, Class classRepresentingTable) throws RihaRestException {
    if (classRepresentingTable == Document.class && item.getClass() == ObjectNode.class) {
      LOG.info("IS DOCUMENT CLASS");
      ObjectNode objNode = (ObjectNode) item;
      JsonNode content = objNode.get("content");
      if (content != null) {
        try {
          Path path;
          if (content.asText().startsWith("http")) {
            // content is URL, not relative path
            // special check for getResourceById -> content already replaced with url
            path = Paths.get(createDocumentFilePathWithRoot(objNode.get("document_id").asInt()));
          } else {
            // content is file relative path
            path = Paths.get(PATH_ROOT + convertToLinuxPathIfNeeded(content.asText()));
          }

          String base64 = Base64Utility.encode(Files.readAllBytes(path));

          objNode.put("content", base64);
          LOG.info("DOCUMENT FILE WRITTEN TO JSON");
        } catch (IOException e) {
          RihaRestError error = new RihaRestError();
          error.setErrcode(ErrorCodes.DOCUMENT_FILE_READ_ERROR);
          error.setErrmsg(ErrorCodes.DOCUMENT_FILE_READ_ERROR_MSG);
          error.setErrtrace(e.toString());
          throw new RihaRestException(error);
        }
      }
    }

  }

  /**
   * Read document file to content.
   *
   * @param <T> the generic type
   * @param items the items
   * @param classRepresentingTable the class representing table
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void readDocumentFileToContent(List<T> items, Class classRepresentingTable)
      throws RihaRestException {
    if (items != null && !items.isEmpty() && items.get(0) != null && classRepresentingTable == Document.class) {
      for (T item : items) {
        readDocumentFileToContent(item, classRepresentingTable);
      }
    }
  }

  /**
   * Read file bytes.
   *
   * @param documentId the document id
   * @return the byte[]
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static byte[] readFileBytes(int documentId) throws IOException {
    String path = createDocumentFilePath(documentId);
    return readFileBytes(PATH_ROOT + path);
  }

  public static void copyFile(String copyFrom, String copyTo) throws IOException {
    Path copyFromPath = Paths.get(convertToLinuxPathIfNeeded(copyFrom));
    Path copyToPath = Paths.get(convertToLinuxPathIfNeeded(copyTo));
    
    LOG.info("CopyFromFile Exists? (should) " + copyFromPath.toFile().exists());
    LOG.info("CopyToFile Exists? (should not)" + copyToPath.toFile().exists());
    
    Files.createDirectories(copyToPath.getParent());
    Files.copy(copyFromPath, copyToPath);
  }

  // public static void main(String[] args) {
  //
  // String data = "asdöl dfsölasdfkjölas dfjlöasdkj földkjasf öldas";
  // int id = 112431;
  //
  // String filePath = createDocumentFilePath(id);
  //
  // String fileName = PATH_ROOT + filePath;
  //
  // writeFile(fileName, data);
  // LOG.info(readFile(fileName));
  // // deleteFile(fileName);
  //
  // }
}
