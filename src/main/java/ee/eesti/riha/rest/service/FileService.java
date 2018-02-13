package ee.eesti.riha.rest.service;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * File resource controller interface. Provides endpoint for document and file resource uploads and downloads
 */
public interface FileService {

    /**
     * Gets the file.
     *
     * @param documentId the document id
     * @param token      the token
     * @return the file
     */
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/api/document/{documentId}")
    @GET
    Response getDocument(@PathParam(value = "documentId") Integer documentId,
                         @QueryParam(value = "token") String token);

    /**
     * Uploads single file.
     *
     * @param attachment        multipart form attachment
     * @param infoSystemUuidStr optional info system UUID
     * @return uploaded file UUID
     */
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/api/file")
    @POST
    Response upload(@Multipart("file") Attachment attachment,
                    @DefaultValue("") @QueryParam(value = "infoSystemUuid") String infoSystemUuidStr);

    /**
     * Downloads single file resource.
     *
     * @param fileUuidStr       file resource UUID
     * @param infoSystemUuidStr optional info system UUID
     * @return file resource stream
     */
    @Path("/api/file/{fileUuid}")
    @GET
    Response download(@PathParam("fileUuid") String fileUuidStr,
                      @DefaultValue("") @QueryParam(value = "infoSystemUuid") String infoSystemUuidStr);

}
