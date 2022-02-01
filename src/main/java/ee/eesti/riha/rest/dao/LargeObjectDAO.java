package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.logic.util.LengthCalculatingInputStream;
import ee.eesti.riha.rest.model.LargeObject;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class LargeObjectDAO {

    private static final Logger LOG = LoggerFactory.getLogger(LargeObjectDAO.class);

    private static final String HASH_ALGORITHM = "SHA-256";

    private boolean deleteWhenReuseFound = true;

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * <p>Creates {@link LargeObject} entity from provided {@link InputStream} and calculates SHA-256 hash of the file
     * in the process.</p> <p> <p>In most cases, created entity will contain new data that was not previously persisted.
     * Making this assumption, this method tries to first persist entity and hash its data in the process and then try
     * to find already existing entities with the same hash. Either currently created or oldest existing entity ID will
     * be returned.</p>
     *
     * @param inputStream object input stream
     * @return id of created entity or id of existing entity with the same hash
     */
    public int create(InputStream inputStream) {
        LengthCalculatingInputStream lengthCalculatingInputStream = new LengthCalculatingInputStream(inputStream);
        DigestInputStream digestInputStream;
        try {
            digestInputStream = new DigestInputStream(lengthCalculatingInputStream, MessageDigest.getInstance(HASH_ALGORITHM));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create DigestInputStream with algorithm " + HASH_ALGORITHM, e);
        }

        LargeObject entity = createEntityFromInputStream(digestInputStream);
        setHash(entity, digestInputStream.getMessageDigest());
        setLength(entity, lengthCalculatingInputStream.getLength());

        if (deleteWhenReuseFound) {
            Integer reusableEntityId = getFirstReusableEntityId(entity);
            if (reusableEntityId != null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Deleting persisted LargeObject with id {} since reusable LargeObject with id {} is found for hash {}",
                            entity.getId(), reusableEntityId, entity.getHash());
                }
                delete(entity);
                return reusableEntityId;
            }
        }

        return entity.getId();
    }

    private Integer getFirstReusableEntityId(LargeObject entity) {
        List<Integer> existingObjects = findSameHashIds(entity);
        if (!existingObjects.isEmpty()) {
            int firstId = existingObjects.get(0);
            if (LOG.isInfoEnabled()) {
                LOG.info("There is {} other LargeObject entities with the same hash {}, using oldest one with id {}",
                        existingObjects.size(), entity.getHash(), firstId);
            }

            return firstId;
        }

        return null;
    }

    private List<Integer> findSameHashIds(LargeObject entity) {
        Session session = sessionFactory.getCurrentSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);

        Root<LargeObject> largeObject = cq.from(LargeObject.class);

        cq.select(largeObject.get("id"));
        Predicate idPredicate = cb.notEqual(largeObject.get("id"), entity.getId());
        Predicate hashPredicate = cb.equal(largeObject.get("hash"), entity.getHash());
        cq.where(idPredicate, hashPredicate);
        cq.orderBy(cb.asc(largeObject.get("creationDate")));

        return session.createQuery(cq).getResultList();
    }

    private LargeObject createEntityFromInputStream(InputStream inputStream) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating LargeObject entity");
        }

        Session session = sessionFactory.getCurrentSession();

        LargeObject entity = new LargeObject();
        entity.setCreationDate(new Date());
        entity.setData(session.getLobHelper().createBlob(inputStream, -1));

        // Save and flush in order to persist blob and calculate hash
        session.save(entity);

        if (LOG.isDebugEnabled()) {
            LOG.debug("LargeObject with id {} is created", entity.getId());
        }

        return entity;
    }

    private void setHash(LargeObject entity, MessageDigest digest) {
        String hash = DatatypeConverter.printHexBinary(digest.digest());
        String algorithm = digest.getAlgorithm();

        if (LOG.isDebugEnabled()) {
            LOG.debug("LargeObject entity calculated {} hash is {}", algorithm, hash);
        }

        entity.setHash(hash);

        sessionFactory.getCurrentSession().saveOrUpdate(entity);
    }

    private void setLength(LargeObject entity, long length) {
        entity.setLength(length);

        sessionFactory.getCurrentSession().saveOrUpdate(entity);
    }

    /**
     * Get {@link LargeObject} entity by id. Throws exception in case entity with provided id does not exist.
     *
     * @param id entity id for loading
     * @return loaded entity or null if not found
     */
    public LargeObject get(int id) {
        return (LargeObject) sessionFactory.getCurrentSession().get(LargeObject.class, id);
    }

    /**
     * Deletes {@link LargeObject} entity.
     *
     * @param entity entity for deletion
     */
    public void delete(LargeObject entity) {
        sessionFactory.getCurrentSession().delete(entity);
    }

    /**
     * Indicates that just created entity should be deleted in case another one with same hash already exists.
     *
     * @return true when entity is deleted when reuse found, false otherwise
     */
    public boolean isDeleteWhenReuseFound() {
        return deleteWhenReuseFound;
    }

    /**
     * Set to true (default) in order to delete just persisted entity in case another one with the same hash already
     * exists.
     *
     * @param deleteWhenReuseFound indicates if reuse entity should be used and just created entity dropped
     */
    public void setDeleteWhenReuseFound(boolean deleteWhenReuseFound) {
        this.deleteWhenReuseFound = deleteWhenReuseFound;
    }

}
