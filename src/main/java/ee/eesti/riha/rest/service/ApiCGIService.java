package ee.eesti.riha.rest.service;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ee.eesti.riha.rest.auth.AuthService;
import ee.eesti.riha.rest.auth.AuthServiceImpl;
import ee.eesti.riha.rest.logic.Finals;

// TODO: Auto-generated Javadoc
/**
 * This class contains all allowed rest resource paths that are following classical and CGI approach.
 *
 * @param <T> the generic type
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface ApiCGIService<T> {

  /**
   * Generic method to perform different read operations. Must not modify database.
   *
   * @param operation the operation
   * @param path the path
   * @param token the token
   * @param limit same meaning as limit in SQL
   * @param offset same meaning as offset in SQL
   * @param filter string representing WHERE condition, in agreed format (i.e., "age,>,20")
   * @param sort string representing expression after ORDER BY
   * @param fields result should contain only these fields, if present
   * @return the cgi
   */
  @Path("/api")
  @GET
  Response getCGI(@QueryParam(value = "op") String operation, @QueryParam(value = "path") String path,
      @QueryParam(value = "token") String token,
      @DefaultValue("" + Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED) @QueryParam(value = "limit") Integer limit,
      @DefaultValue("0") @QueryParam(value = "offset") Integer offset, @QueryParam(value = "filter") String filter,
      @QueryParam(value = "sort") String sort, @QueryParam(value = "fields") String fields);

  /**
   * Generic method to perform different operations.
   * 
   * @param json {@link ee.eesti.riha.rest.logic.util.QueryHolder} in json format e.g. { "op":"put", "path":
   *          "db/main_resource/", "key": "version", "data": [{"short_name":"short","version":"1.1"}] }
   * @return operation result
   */
  @Path("/api")
  @POST
  Response postCGI(String json);

  // getters and setters won't work without being a service themselves
  // this is needed to use fake AuthService in integration tests
  // TODO find if better solution exists, maybe ignore those paths?

  /**
   * Getter needed for integration tests.
   *
   * @return the auth service
   */
  @Path("/not/to/be/called/by/url/x")
  @GET
  AuthService getAuthService();

  /**
   * Setter needed for integration tests.
   *
   * @param authService the new auth service
   */
  @Path("/not/to/be/called/by/url2/x")
  @GET
  void setAuthService(@QueryParam(value = "authService") AuthServiceImpl authService);

}
