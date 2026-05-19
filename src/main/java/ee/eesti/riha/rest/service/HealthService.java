package ee.eesti.riha.rest.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;

@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface HealthService {

    @Path(value = "/health")
    @GET
    public Response health() throws SQLException;
}
