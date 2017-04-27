package ee.eesti.riha.rest.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.model.readonly.Kind;

/**
 * Simple kind repository implementation with spring cache
 * 
 *
 */
// TODO use getCurrentSession, add @Transactional, should use better cache implementation
// to work with transactions, EhCahce maybe?
@Component
public class KindRepositoryImpl implements KindRepository {

  @Autowired
  SessionFactory sessionFactory;

  private static final Logger LOG = LoggerFactory.getLogger(KindRepositoryImpl.class);

  @Override
  public Kind getByName(String name) {
    LOG.info("KindRepository.getByName called");
    return getByNameHelper(name);
  }

  private Kind getByNameHelper(String name) {
    Session session = sessionFactory.openSession();
    try {
      Kind kind = (Kind) session.createCriteria(Kind.class).add(Restrictions.eq("name", name)).uniqueResult();
      return kind;

    } finally {
      session.flush();
      session.close();
    }

  }

  @Override
  public Kind getById(Integer kind_id) {
    LOG.info("KindRepository.getById called");
    return getByIdHelper(kind_id);
  }

  private Kind getByIdHelper(Integer id) {
    Session session = sessionFactory.openSession();
    try {
      Kind kind = (Kind) session.get(Kind.class, id);
      return kind;
    } finally {
      session.flush();
      session.close();
    }
  }

}
