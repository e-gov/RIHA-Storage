package ee.eesti.riha.rest.service.impl;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.node.ObjectNode;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.AuthServiceProvider;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.ChangeLogic;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.TokenValidator;
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

  AuthServiceProvider authServiceProvider = AuthServiceProvider.getInstance();

  private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

  @Autowired
  ChangeLogic changeLogic;

  @Autowired
  TokenStore tokenStore;

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.FileService#getFile(java.lang.Integer, java.lang.String)
   */
  @Override
  public Response getFile(Integer documentId, String token) {

    try {
      AuthInfo user = TokenValidator.isTokenOk(token, tokenStore);
      String fields = "[\"document_id\", \"filename\"]";
      ObjectNode jsonObject = (ObjectNode) changeLogic.doGet(Document.class, documentId, fields, user);

      return getFileLogic(documentId, jsonObject, token);

    } catch (RihaRestException e) {
      return Response.status(Status.BAD_REQUEST).entity(MyExceptionHandler.unmapped(e, "FileService error"))
          .type(MediaType.APPLICATION_JSON + "; charset=UTF-8").build();
    }

  }

  private Response getFileLogic(Integer documentId, ObjectNode document, String token) throws RihaRestException {
    // if doesn't throw then OK
    // TokenValidator.isTokenOk(token, authServiceProvider.get());
    // TokenValidator.isTokenOk(token, tokenStore);
    // no need to currently authenticate for GET requests

    String filePath = FileHelper.createDocumentFilePathWithRoot(documentId);
    File file = new File(filePath);

    Validator.documentFileMustExist(file, documentId);

    String fileName = JsonHelper.get(document, "filename", file.getName());

    return Response.ok(file).header("content-disposition", "attachment; filename =" + fileName).build();
  }

}
