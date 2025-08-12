package ee.eesti.riha.rest.service.impl;

import ee.eesti.riha.rest.logic.ChangeLogic;
import ee.eesti.riha.rest.logic.FileResourceLogic;
import ee.eesti.riha.rest.model.FileResource;
import ee.eesti.riha.rest.service.FileService;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.PagedRequestArgumentResolver;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.UUID;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

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

    @Autowired
    private PagedRequestArgumentResolver pagedRequestArgumentResolver;

    @Override
    public Response upload(Attachment attachment, String infoSystemUuidStr) {
        LOG.info("=== FILE UPLOAD DEBUG: Starting upload process ===");
        
        jakarta.activation.DataHandler dataHandler = attachment.getDataHandler();
        String name = dataHandler.getName();
        String contentType = dataHandler.getContentType();
        UUID infoSystemUuid = StringUtils.hasText(infoSystemUuidStr)
                ? UUID.fromString(infoSystemUuidStr)
                : null;

        LOG.info("FILE UPLOAD DEBUG: name='{}', contentType='{}', infoSystemUuid='{}'", name, contentType, infoSystemUuid);

        if (LOG.isInfoEnabled()) {
            LOG.info("Receiving upload of file '{}' with content type '{}'", name, contentType);
        }

        try {
            LOG.info("FILE UPLOAD DEBUG: About to call createFileResource");
            UUID fileResourceUuid = fileResourceLogic.createFileResource(dataHandler.getInputStream(), infoSystemUuid, name, contentType);
            LOG.info("FILE UPLOAD DEBUG: createFileResource returned UUID: {}", fileResourceUuid);
            
            LOG.info("FILE UPLOAD DEBUG: About to call indexFileResource");
            fileResourceLogic.indexFileResource(fileResourceUuid);
            LOG.info("FILE UPLOAD DEBUG: indexFileResource completed");
            
            LOG.info("FILE UPLOAD DEBUG: Returning successful response with UUID: {}", fileResourceUuid);
            return Response.ok(fileResourceUuid.toString()).build();
        } catch (IOException e) {
            LOG.error("FILE UPLOAD DEBUG: IOException occurred", e);
            throw new IllegalStateException("Could not retrieve request attachment input stream", e);
        } catch (Exception e) {
            LOG.error("FILE UPLOAD DEBUG: Unexpected exception occurred", e);
            throw e;
        }
    }

    @Override
    public Response download(String fileUuidStr, String infoSystemUuidStr) {
        final UUID fileResourceUuid = UUID.fromString(fileUuidStr);
        UUID infoSystemUuid = StringUtils.hasText(infoSystemUuidStr)
                ? UUID.fromString(infoSystemUuidStr)
                : null;
        LOG.debug("Handling file {} download", fileResourceUuid);

        FileResource fileResource = fileResourceLogic.get(fileResourceUuid, infoSystemUuid);
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

        Response.ResponseBuilder response = Response.ok();

        if (fileResource.getLargeObject().getLength() != null) {
            response.header(HttpHeaders.CONTENT_LENGTH, fileResource.getLargeObject().getLength());
        }

        if (StringUtils.hasText(fileResource.getContentType())) {
            response.header(HttpHeaders.CONTENT_TYPE, fileResource.getContentType());
        }

        if (StringUtils.hasText(fileResource.getName())) {
            response.header(HttpHeaders.CONTENT_DISPOSITION, "filename=\"" + fileResource.getName() + "\"");
        }

        return response.entity(streamingOutput).build();
    }

    @Override
    public Response list(UriInfo uriInfo) {
        PagedRequest request = pagedRequestArgumentResolver.resolve(uriInfo.getQueryParameters());
        return Response.ok(fileResourceLogic.list(request)).build();
    }

    @Override
    public Response createFileResourceFromExisting(String existingFileUuid, String existingInfoSystemUuidStr, String newInfoSystemUuidStr) {
        final UUID fileResourceUuid = UUID.fromString(existingFileUuid);
        final UUID existingInfoSystemUuid = UUID.fromString(existingInfoSystemUuidStr);
        final UUID newInfoSystemUuid = UUID.fromString(newInfoSystemUuidStr);

        UUID uuid = fileResourceLogic.createFileResourceFromExisting(
                fileResourceUuid, existingInfoSystemUuid, newInfoSystemUuid);

        return Response.ok(uuid.toString()).build();
    }
}
