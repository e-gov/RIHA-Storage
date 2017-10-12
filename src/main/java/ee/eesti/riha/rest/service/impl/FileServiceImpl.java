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
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.service.FileService;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataHandler;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
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
     * @see ee.eesti.riha.rest.service.FileService#getDocument(java.lang.Integer, java.lang.String)
     */
    @Override
    public Response getDocument(Integer documentId, String token) {

        try {
            String fields = "[\"document_id\", \"filename\"]";
            ObjectNode jsonObject = (ObjectNode) changeLogic.doGet(Document.class, documentId, fields);

            return getDocumentLogic(documentId, jsonObject);

        } catch (RihaRestException e) {
            return Response.status(Status.BAD_REQUEST).entity(MyExceptionHandler.unmapped(e, "FileService error"))
                    .type(MediaType.APPLICATION_JSON + "; charset=UTF-8").build();
        }

    }

    @Override
    public Response upload(Attachment attachment) {
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
            throw new IllegalStateException("Could not retrieve request attachment input stream", e);
        }
    }

    @Override
    public Response download(String uuid) {
        final UUID fileResourceUuid = UUID.fromString(uuid);
        LOG.debug("Handling file {} download", fileResourceUuid);

        FileResource fileResource = fileResourceLogic.get(fileResourceUuid);
        if (fileResource == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(final OutputStream output) throws IOException {
                try {
                    fileResourceLogic.copyLargeObjectData(fileResourceUuid, output);
                } catch (SQLException e) {
                    throw new IllegalStateException("Could not retrieve requested file data", e);
                }
            }
        };

        return Response.ok()
                .header(HttpHeaders.CONTENT_LENGTH, fileResource.getLargeObject().getLength())
                .header(HttpHeaders.CONTENT_TYPE, fileResource.getContentType())
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + fileResource.getName() + "\"")
                .entity(streamingOutput)
                .build();
    }

    private Response getDocumentLogic(Integer documentId, ObjectNode document) throws RihaRestException {
        String documentFilePath = FileHelper.createDocumentFilePathWithRoot(documentId);
        File file = new File(documentFilePath);

        Validator.documentFileMustExist(file, documentId);

        String fileName = JsonHelper.get(document, "filename", file.getName());

        return Response.ok(file).header("content-disposition", "attachment; filename =" + fileName).build();
    }

}
