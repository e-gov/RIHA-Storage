package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.model.FileResource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class FileResourceDAO {

    private final SessionFactory sessionFactory;

    public FileResourceDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

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
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<FileResource> cq = cb.createQuery(FileResource.class);

        Root<FileResource> fileResource = cq.from(FileResource.class);
        Predicate predicate = cb.equal(fileResource.get("uuid"), uuid);

        if (infoSystemUuid != null) {
            predicate = cb.and(predicate, cb.equal(fileResource.get("infoSystemUuid"), infoSystemUuid));
        }

        cq.where(predicate);

        TypedQuery<FileResource> query = session.createQuery(cq);

        return query.getResultStream().findFirst().orElse(null);
    }

    /**
     * Retrieves list of {@link FileResource} which were not indexed yet.
     *
     * @return list of file resources that need to be indexed
     */
    public List<FileResource> getUnindexedFiles() {
        Session session = sessionFactory.getCurrentSession();

        TypedQuery<FileResource> query = session.createQuery(
            "SELECT f FROM FileResource f JOIN f.largeObject lo WHERE lo.indexed = false", FileResource.class);

        return query.getResultList();
    }
}
