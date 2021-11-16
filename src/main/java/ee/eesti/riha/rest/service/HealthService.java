package ee.eesti.riha.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface HealthService {

    @Path(value = "/health")
    @GET
    public Response health() throws SQLException;
}
