package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.model.readonly.Kind;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Component
@Repository
@Transactional
public class KindRepositoryImpl implements KindRepository {

  private final SessionFactory sessionFactory;

  private static final Logger LOG = LoggerFactory.getLogger(KindRepositoryImpl.class);

  public KindRepositoryImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Kind getByName(String name) {
    LOG.info("KindRepository.getByName called");
    return getByNameHelper(name);
  }

  private Kind getByNameHelper(String name) {
    Session session = sessionFactory.getCurrentSession();

    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<Kind> cq = cb.createQuery(Kind.class);

    Root<Kind> kind = cq.from(Kind.class);
    Predicate namePredicate = cb.equal(kind.get("name"), name);
    cq.where(namePredicate);

    TypedQuery<Kind> query = session.createQuery(cq);

    return query.getResultStream().findFirst().orElse(null);
  }

  @Override
  public Kind getById(Integer kind_id) {
    LOG.info("KindRepository.getById called");
    return getByIdHelper(kind_id);
  }

  private Kind getByIdHelper(Integer id) {
    Session session = sessionFactory.getCurrentSession();
    return session.get(Kind.class, id);
  }
}
