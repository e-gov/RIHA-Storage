package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.model.FileResource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Date;
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
        return (FileResource) sessionFactory.getCurrentSession().get(FileResource.class, uuid);
    }
}
