package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.model.FileResource;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class FileResourceDAO {

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Creates single {@link FileResource} entity.
     *
     * @param entity persisted entity
     * @return UUID of persisted {@link FileResource}
     */
    public UUID create(FileResource entity) {
        entity.setCreationDate(new Date());
        return (UUID) sessionFactory.getCurrentSession().save(entity);
    }

    /**
     * Retrieves single {@link FileResource} entity by its UUID.
     *
     * @param uuid entity UUID
     * @return loaded entity or null if not found
     */
    public FileResource get(UUID uuid) {
        return get(uuid, null);
    }

    /**
     * Retrieves single {@link FileResource} entity by its UUID. If info system UUID is provided it is used in the
     * query.
     *
     * @param uuid           entity UUID
     * @param infoSystemUuid associated info system UUID
     * @return loaded entity or null if not found
     */
    public FileResource get(UUID uuid, UUID infoSystemUuid) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(FileResource.class, "f");
        criteria.add(Restrictions.idEq(uuid));

        if (infoSystemUuid != null) {
            criteria.add(Restrictions.eq("f.infoSystemUuid", infoSystemUuid));
        }

        return ((FileResource) criteria.uniqueResult());
    }

    /**
     * Retrieves list of {@link FileResource} which were not indexed yet.
     *
     * @return list of file resources that need to be indexed
     */
    public List<FileResource> getUnindexedFiles() {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(FileResource.class, "f")
                .createAlias("f.largeObject", "lo")
                .add(Restrictions.eq("lo.indexed", false));

        return ((List<FileResource>) criteria.list());
    }
}
