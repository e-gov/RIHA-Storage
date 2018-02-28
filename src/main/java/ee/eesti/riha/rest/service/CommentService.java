package ee.eesti.riha.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
