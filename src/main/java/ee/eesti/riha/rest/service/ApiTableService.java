package ee.eesti.riha.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// TODO: Auto-generated Javadoc
/**
 * The Interface ApiTableService.
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface ApiTableService {

  /**
   * Special service to get all rows of some tables, that could be cached in client.
   *
   * @param tableName the table name
   * @return the full table
   */
  @Path("/api/table/{tableName}")
  @GET
  Response getFullTable(@PathParam(value = "tableName") String tableName);

}
