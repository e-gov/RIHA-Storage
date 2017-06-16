package ee.eesti.riha.rest.service.impl;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.ChangeLogic;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.Validator;
import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.service.FileService;

// TODO: Auto-generated Javadoc
/**
 * The Class FileServiceImpl.
 */
public class FileServiceImpl implements FileService {

  private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

  @Autowired
  ChangeLogic changeLogic;

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.FileService#getFile(java.lang.Integer, java.lang.String)
   */
  @Override
  public Response getFile(Integer documentId, String token) {

    try {
      String fields = "[\"document_id\", \"filename\"]";
      ObjectNode jsonObject = (ObjectNode) changeLogic.doGet(Document.class, documentId, fields);

      return getFileLogic(documentId, jsonObject);

    } catch (RihaRestException e) {
      return Response.status(Status.BAD_REQUEST).entity(MyExceptionHandler.unmapped(e, "FileService error"))
          .type(MediaType.APPLICATION_JSON + "; charset=UTF-8").build();
    }

  }

  private Response getFileLogic(Integer documentId, ObjectNode document) throws RihaRestException {
    String filePath = FileHelper.createDocumentFilePathWithRoot(documentId);
    File file = new File(filePath);

    Validator.documentFileMustExist(file, documentId);

    String fileName = JsonHelper.get(document, "filename", file.getName());

    return Response.ok(file).header("content-disposition", "attachment; filename =" + fileName).build();
  }

}
