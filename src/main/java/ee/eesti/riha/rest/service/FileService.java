package ee.eesti.riha.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
  @Path("/api/file/{documentId}")
  @GET
  Response getFile(@PathParam(value = "documentId") Integer documentId, @QueryParam(value = "token") String token);

}
