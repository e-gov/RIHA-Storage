package ee.eesti.riha.rest.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Comment management controller interface.
 */
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public interface CommentService {

    /**
     * Retrieves paged list of comments
     *
     * @return paged list of comments
     */
    @Path(value = "/api/comment")
    @GET
    public Response list(@Context UriInfo uriInfo);

}
