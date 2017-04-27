package ee.eesti.riha.rest.auth;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Service to connect to 3rd party authentication service
 *
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface AuthService {

  /**
   * Checks if token is valid.
   *
   * @param sessionId the session id
   * @return authenticated user if valid
   */
  @GET
  Object isValid(@QueryParam(value = "sessionId") String sessionId);
}
