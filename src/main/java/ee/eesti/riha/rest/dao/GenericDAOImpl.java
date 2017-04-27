package ee.eesti.riha.rest.dao;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import javax.transaction.Transactional;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * The Class GenericDAOImpl.
 *
 * @param <T> the generic type
 */
@Transactional
@Component
public class GenericDAOImpl<T> implements GenericDAO<T> {

  @Autowired
  SessionFactory sessionFactory;

  private static final Logger LOG = Logger.getLogger("GeneridDAO");

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.GenericDAO#findAll(java.lang.Class)
   */
  @Override
  public List<T> findAll(Class<T> clazz) {
    Session session = sessionFactory.getCurrentSession();

    List<T> objectList = session.createCriteria(clazz).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        .list();

    return objectList;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.GenericDAO#findById(java.lang.Class, java.lang.Integer)
   */
  @Override
  public T findById(Class<T> clazz, Integer id) {
    Session session = sessionFactory.getCurrentSession();

    // Class.cast() removes unchecked cast warning
    T object = clazz.cast(session.get(clazz, id));

    return object;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.GenericDAO#create(java.lang.Object)
   */
  @Override
  public int create(T object) {
    return createOrUpdate(object);
  }

  /**
   * Creates the or update.
   *
   * @param object the object
   * @return the int
   */
  private int createOrUpdate(T object) {
    Session session = sessionFactory.getCurrentSession();

    session.saveOrUpdate(object);
    Serializable id = session.getIdentifier(object);

    return (Integer) id;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.GenericDAO#update(java.lang.Object)
   */
  @Override
  public int update(T object) {
    return createOrUpdate(object);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.GenericDAO#delete(java.lang.Object)
   */
  @Override
  public void delete(T object) {
    Session session = sessionFactory.getCurrentSession();

    session.delete(object);

  }

}
