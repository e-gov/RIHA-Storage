package ee.eesti.riha.rest.service;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This class contains all allowed rest resource paths that are following classical approach.
 * 
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface ApiClassicService {

  /**
   * Gets list of elements from given table. Always limitation ( {@link Finals#NUM_OF_ITEMS_IN_RESULT_ALLOWED}) to rows
   * returned applied.
   *
   * @param tableName only required input; then queries all rows from table (limitation to rows returned applied)
   * @param limit how many rows to return (always limitation ( {@link Finals#NUM_OF_ITEMS_IN_RESULT_ALLOWED}) to rows
   *          returned applied)
   * @param offset the offset
   * @param filter filter parameter (i.e., name,=,prepareSignature,main_resource_parent_id,>,29484)
   * @param sort results sorted by field (i.e., -access_restriction sorts DESC, access_restriction sorts ASC)
   * @param fields the fields
   * @return Response with status 200 and list of elements in body; or response 400 (Bad Request) with error explaining
   *         the problem
   */
  // @DefaultValue("" + Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED) removed
  @Path("/api/db/{tableName}")
  @GET
  Response getMany(@PathParam(value = "tableName") String tableName,
      @DefaultValue("0") @QueryParam(value = "limit") Integer limit,
      @DefaultValue("0") @QueryParam(value = "offset") Integer offset, @QueryParam(value = "filter") String filter,
      @QueryParam(value = "sort") String sort, @QueryParam(value = "fields") String fields);

  /**
   * Gets certain row by primary key of given table.
   *
   * @param tableName the table name
   * @param id primary key of table, model field annotated with @Id
   * @param fields the fields
   * @return Response with status 200 and list of elements in body; or response 400 (Bad Request) with error explaining
   *         the problem
   */
  @Path("/api/db/{tableName}/{id}")
  @GET
  Response getById(@PathParam(value = "tableName") String tableName, @PathParam(value = "id") Integer id,
      @QueryParam(value = "fields") String fields);

  /**
   * Gets certain full main resource object by primary key of given kind.
   *
   * @param id of Main_resource (main_resource_id)
   * @return Response with status 200 and list of elements in body; or response 400 (Bad Request) with error explaining
   *         the problem
   */
  @Path("/api/resource/{id}")
  @GET
  Response getResourceById(@PathParam(value = "id") Integer id);

  /**
   * Inserts new row to table given. Json represents new entry.
   *
   * @param json model object or array to be created
   * @param tableName the table name
   * @return list with created id e.g. [123, 124]
   */
  @Path("/api/db/{tableName}")
  @POST
  Response create(String json, @PathParam(value = "tableName") String tableName);

  /**
   * Update existing row in table. Null values will be ignored.
   *
   * @param json model object fields values which must be updated. Must not be array!
   * @param tableName the table name
   * @param id primary key
   * @return number of created objects e.g. {"ok":2}
   */
  @Path("/api/db/{tableName}/{id}")
  @PUT
  Response update(String json, @PathParam(value = "tableName") String tableName, @PathParam(value = "id") Integer id);

  /**
   * Delete certain row by primary key from table.
   *
   * @param tableName the table name
   * @param id primary key
   * @return number of deleted objects e.g. {"ok":0}
   */
  @Path("/api/db/{tableName}/{id}")
  @DELETE
  Response delete(@PathParam(value = "tableName") String tableName, @PathParam(value = "id") Integer id);

}
