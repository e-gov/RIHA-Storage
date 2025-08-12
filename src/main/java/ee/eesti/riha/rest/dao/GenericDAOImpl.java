package ee.eesti.riha.rest.dao;

import java.util.List;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.transaction.Transactional;
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

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.GenericDAO#findAll(java.lang.Class)
   */
  @Override
  public List<T> findAll(Class<T> clazz) {
    Session session = sessionFactory.getCurrentSession();
    CriteriaQuery<T> cq = session.getCriteriaBuilder().createQuery(clazz);
    
    // In Hibernate 6, distinct handling is done through the query itself
    return session.createQuery(cq.select(cq.from(clazz)).distinct(true)).getResultList();
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

    session.merge(object);
    Object id = session.getIdentifier(object);

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

    session.remove(object);

  }

}
