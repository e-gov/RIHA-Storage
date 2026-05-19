package ee.eesti.riha.rest.service;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

/**
 * File resource controller interface. Provides endpoint for document and file resource uploads and downloads
 */
public interface FileService {


    /**
     * Uploads single file.
     *
     * @param attachment        multipart form attachment
     * @param infoSystemUuidStr optional info system UUID
     * @return uploaded file UUID
     */
    @Path("/api/file")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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

    /**
     * Provides file resource grid with filtering, pagination and sorting.
     * @param uriInfo request URI
     * @return page of filtered and sorted file resource information
     */
    @Path(value = "/api/file")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    Response list(@Context UriInfo uriInfo);

    /**
     * Creates new file resource by copying existing one with new info system UUID
     *
     * @param existingFileUuid file uuid to copy from
     * @param existingInfoSystemUuid info system uuid to copy from
     * @param newInfoSystemUuid new info system uuid
     * @return uuid of created file resource
     */

    @Path("/api/file/createFromExisting")
    @POST
    Response createFileResourceFromExisting(
            @QueryParam(value = "existingFileUuid") String existingFileUuid,
            @QueryParam(value = "existingInfoSystemUuid") String existingInfoSystemUuid,
            @QueryParam(value = "newInfoSystemUuid") String newInfoSystemUuid);
}
