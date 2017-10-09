package ee.eesti.riha.rest.service.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.ChangeLogic;
import ee.eesti.riha.rest.logic.FileResourceLogic;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.Validator;
import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.service.FileService;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class FileServiceImpl.
 */
public class FileServiceImpl implements FileService {

  private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);

  @Autowired
  ChangeLogic changeLogic;

  @Autowired
  private FileResourceLogic fileResourceLogic;

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

    @Override
    public Response uploadFile(Attachment attachment) {
        DataHandler dataHandler = attachment.getDataHandler();
        String name = dataHandler.getName();
        String contentType = dataHandler.getContentType();

        if (LOG.isInfoEnabled()) {
            LOG.info("Receiving upload of file '{}' with content type '{}'", name, contentType);
        }

        try {
            UUID fileResourceUuid = fileResourceLogic.create(dataHandler.getInputStream(), name, contentType);
            return Response.ok(fileResourceUuid.toString()).build();
        } catch (IOException e) {
            throw new RuntimeException("Could not retrieve request attachment data input stream", e);
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
