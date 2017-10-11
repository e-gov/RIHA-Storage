package ee.eesti.riha.rest.service;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// TODO: Auto-generated Javadoc
/**
 * The Interface FileService.
 */
public interface FileService {

  /**
   * Gets the file.
   *
   * @param documentId the document id
   * @param token the token
   * @return the file
   */
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/api/document/{documentId}")
  @GET
  Response getDocument(@PathParam(value = "documentId") Integer documentId, @QueryParam(value = "token") String token);

  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
  @Path("/api/file")
  @POST
  Response uploadFile(@Multipart("file") Attachment attachment);

}
