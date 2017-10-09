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

    public UUID create(FileResource entity) {
        entity.setCreationDate(new Date());
        return (UUID) sessionFactory.getCurrentSession().save(entity);
    }
}
