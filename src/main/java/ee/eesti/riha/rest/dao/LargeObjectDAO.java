package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.logic.util.LengthCalculatingInputStream;
import ee.eesti.riha.rest.model.LargeObject;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.DatatypeConverter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class LargeObjectDAO {

    private static final Logger LOG = LoggerFactory.getLogger(LargeObjectDAO.class);

    private static final String HASH_ALGORITHM = "SHA-256";

    private boolean deleteWhenReuseFound = true;

    @Autowired
    @Qualifier("sessionFactory")
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
        LOG.info("=== LARGE OBJECT DAO DEBUG: Starting create method ===");
        
        // For debugging only - check sequence and max ID values without modifying them
        try {
            Session session = sessionFactory.getCurrentSession();
            
            // Check current sequence value
            Object currSeqValue = session.createNativeQuery("SELECT last_value FROM riha.large_object_seq", Object.class).getSingleResult();
            
            // Check current max ID in the table
            Object maxId = session.createNativeQuery("SELECT COALESCE(MAX(id), 0) FROM riha.large_object", Object.class).getSingleResult();
            
            LOG.info("LARGE OBJECT DAO DEBUG: Current sequence value: {}, MAX id in table: {}", currSeqValue, maxId);
            
            // If sequence is behind max ID, log a warning but don't modify
            if (Long.parseLong(currSeqValue.toString()) <= Long.parseLong(maxId.toString())) {
                LOG.warn("LARGE OBJECT DAO DEBUG: WARNING - Sequence value {} is not greater than max ID {}. " +
                         "This may cause primary key conflicts.", currSeqValue, maxId);
            }
        } catch (Exception e) {
            LOG.error("LARGE OBJECT DAO DEBUG: Error getting diagnostic information", e);
            // Continue anyway as this is just diagnostic
        }
        
        LengthCalculatingInputStream lengthCalculatingInputStream = new LengthCalculatingInputStream(inputStream);
        DigestInputStream digestInputStream;
        try {
            digestInputStream = new DigestInputStream(lengthCalculatingInputStream, MessageDigest.getInstance(HASH_ALGORITHM));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create DigestInputStream with algorithm " + HASH_ALGORITHM, e);
        }

        LOG.info("LARGE OBJECT DAO DEBUG: About to create entity from input stream");
        LargeObject entity = createEntityFromInputStream(digestInputStream);
        LOG.info("LARGE OBJECT DAO DEBUG: Created new entity with ID: {}", entity.getId());
        
        LOG.info("LARGE OBJECT DAO DEBUG: Setting hash for entity");
        setHash(entity, digestInputStream.getMessageDigest());
        LOG.info("LARGE OBJECT DAO DEBUG: Hash set to: {}", entity.getHash());
        
        LOG.info("LARGE OBJECT DAO DEBUG: Setting length for entity");
        setLength(entity, lengthCalculatingInputStream.getLength());
        LOG.info("LARGE OBJECT DAO DEBUG: Length set to: {}", entity.getLength());

        if (deleteWhenReuseFound) {
            LOG.info("LARGE OBJECT DAO DEBUG: Checking for reusable entities with same hash");
            Integer reusableEntityId = getFirstReusableEntityId(entity);
            if (reusableEntityId != null) {
                LOG.info("LARGE OBJECT DAO DEBUG: Found reusable entity with ID: {} for hash: {}", 
                         reusableEntityId, entity.getHash());
                if (LOG.isInfoEnabled()) {
                    LOG.info("Deleting persisted LargeObject with id {} since reusable LargeObject with id {} is found for hash {}",
                            entity.getId(), reusableEntityId, entity.getHash());
                }
                LOG.info("LARGE OBJECT DAO DEBUG: About to delete newly created entity");
                delete(entity);
                LOG.info("LARGE OBJECT DAO DEBUG: Entity deleted, returning reusable ID: {}", reusableEntityId);
                return reusableEntityId;
            } else {
                LOG.info("LARGE OBJECT DAO DEBUG: No reusable entity found with same hash");
            }
        }

        LOG.info("LARGE OBJECT DAO DEBUG: Returning newly created entity ID: {}", entity.getId());
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
        LOG.info("LARGE OBJECT DAO DEBUG: Creating LargeObject entity");
        
        Session session = sessionFactory.getCurrentSession();

        // Try to manually advance the sequence to avoid ID conflicts
        try {
            // First get the current max ID to ensure we're not behind
            Object maxId = session.createNativeQuery("SELECT COALESCE(MAX(id), 0) FROM riha.large_object", Object.class).getSingleResult();
            Long maxIdValue = Long.parseLong(maxId.toString());
            
            // Ensure sequence is ahead of max ID to avoid conflicts
            session.createNativeQuery("SELECT setval('riha.large_object_seq', :maxId, true)", Object.class)
                  .setParameter("maxId", maxIdValue)
                  .getSingleResult();
            
            LOG.info("LARGE OBJECT DAO DEBUG: Ensured sequence is at least: {}", maxIdValue);
        } catch (Exception e) {
            LOG.warn("LARGE OBJECT DAO DEBUG: Could not adjust sequence. Will continue with default sequence behavior", e);
        }

        LargeObject entity = new LargeObject();
        entity.setCreationDate(new Date());
        LOG.info("LARGE OBJECT DAO DEBUG: Setting creation date: {}", entity.getCreationDate());
        
        LOG.info("LARGE OBJECT DAO DEBUG: Creating BLOB from input stream");
        entity.setData(session.getLobHelper().createBlob(inputStream, -1));
        LOG.info("LARGE OBJECT DAO DEBUG: BLOB created");

        // Save and flush in order to persist blob and calculate hash
        try {
            LOG.info("LARGE OBJECT DAO DEBUG: Saving entity");
            session.persist(entity);
            LOG.info("LARGE OBJECT DAO DEBUG: Entity saved with generated ID: {}", entity.getId());
            
            LOG.info("LARGE OBJECT DAO DEBUG: Flushing session");
            session.flush();
            LOG.info("LARGE OBJECT DAO DEBUG: Session flushed");
            
            LOG.info("LARGE OBJECT DAO DEBUG: LargeObject with id {} is created", entity.getId());
        } catch (Exception e) {
            LOG.error("LARGE OBJECT DAO DEBUG: Error saving entity: {}", e.getMessage(), e);
            // If there was a duplicate key error, try to restart the sequence
            if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                LOG.error("LARGE OBJECT DAO DEBUG: Detected duplicate key error. This indicates sequence misalignment.");
                throw new RuntimeException("Duplicate key error when creating LargeObject. Sequence needs to be reset.", e);
            }
            throw e;
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

        sessionFactory.getCurrentSession().merge(entity);
    }

    private void setLength(LargeObject entity, long length) {
        entity.setLength(length);

        sessionFactory.getCurrentSession().merge(entity);
    }

    /**
     * Get {@link LargeObject} entity by id. Throws exception in case entity with provided id does not exist.
     *
     * @param id entity id for loading
     * @return loaded entity or null if not found
     */
    public LargeObject get(int id) {
        return sessionFactory.getCurrentSession().get(LargeObject.class, id);
    }

    /**
     * Deletes {@link LargeObject} entity.
     *
     * @param entity entity for deletion
     */
    public void delete(LargeObject entity) {
        sessionFactory.getCurrentSession().remove(entity);
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
