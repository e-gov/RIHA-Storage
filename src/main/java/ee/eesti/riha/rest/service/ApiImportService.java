package ee.eesti.riha.rest.service;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Service to import Main_resource. (Like the opposite of RESOURCE method)
 * {@link ApiClassicService#getResourceById(Integer) }
 *
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface ApiImportService {

  /**
   * Opposite of {@link ApiClassicService#getResourceById(Integer)}. Imports entire Main_resource with all its connected
   * items (Data_objects and Documents that have the same main_resource_id)
   *
   * @param json Main_resource with nested Data_objects and Documents to be imported
   * @param token authentication token
   * @return returns "ok" if import was successful, else returns RihaRestError as json.
   */
  @Path("/api/resource/")
  @POST
  Response doImport(String json, @QueryParam(value = "token") String token);

}
