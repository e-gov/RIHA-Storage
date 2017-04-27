package ee.eesti.riha.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Interface ApiAuthService.
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface ApiAuthService {

  /**
   * Method to call 3rd party authentication service with sessionId to find whether user is authenticated or not. If
   * user exists then cache for later use.
   * 
   * Respond with given user AuthInfo.
   *
   * @param sessionId B9756007F...3D
   * @return operation result
   */
  @Path("/token/{sessionId}")
  @GET
  Response checkToken(@PathParam("sessionId") String sessionId);

}
