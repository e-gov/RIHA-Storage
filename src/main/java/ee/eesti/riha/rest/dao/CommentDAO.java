package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.dao.grid.CommentGrid;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.PagedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Performs various persistence related operations on Comment entity.
 */
@Transactional
@Repository
public class CommentDAO {

    @Autowired
    private CommentGrid commentGrid;

    public PagedResponse list(PagedRequest request) {
        return commentGrid.query(request);
    }

}
