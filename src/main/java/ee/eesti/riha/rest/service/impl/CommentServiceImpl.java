package ee.eesti.riha.rest.service.impl;

import ee.eesti.riha.rest.dao.CommentDAO;
import ee.eesti.riha.rest.service.CommentService;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.PagedRequestArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Implementation of {@link CommentService} interface.
 */
@Component
public class CommentServiceImpl implements CommentService {

    @Autowired
    private PagedRequestArgumentResolver pagedRequestArgumentResolver;

    @Autowired
    private CommentDAO commentDAO;

    @Override
    public Response list(UriInfo uriInfo) {
        PagedRequest request = pagedRequestArgumentResolver.resolve(uriInfo.getQueryParameters());
        return Response.ok(commentDAO.list(request)).build();
    }
}
