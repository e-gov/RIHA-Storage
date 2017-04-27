package ee.eesti.riha.rest.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.model.readonly.Role_right;

//TODO use getCurrentSession, add @Transactional, should use better cache implementation
//to work with transactions, EhCahce maybe?
@Component
public class Role_rightRepositoryImpl implements Role_rightRepository {

  @Autowired
  SessionFactory sessionFactory;

  private static final Logger LOG = LoggerFactory.getLogger(Role_rightRepositoryImpl.class);

  @Override
  public List<Role_right> getByName(String name) {
    LOG.info("Role_rightRepository.getByName called");
    return getByNameHelper(name);
  }

  @Override
  public Role_right getById(Integer role_right_id) {
    LOG.info("Role_rightRepository.getById called");
    return getByIdHelper(role_right_id);
  }

  private List<Role_right> getByNameHelper(String name) {
    Session session = sessionFactory.openSession();
    try {
      List<Role_right> roles = session.createCriteria(Role_right.class).add(Restrictions.eq("role_name", name)).list();

      if (roles == null) {
        LOG.info("No such role found, using AUTHENTICATED instead.");
        roles = session.createCriteria(Role_right.class).add(Restrictions.eq("role_name", "AUTHENTICATED")).list();
      }

      return roles;

    } finally {
      session.flush();
      session.close();
    }

  }

  private Role_right getByIdHelper(Integer id) {
    Session session = sessionFactory.openSession();
    try {
      Role_right role = (Role_right) session.get(Role_right.class, id);
      return role;
    } finally {
      session.flush();
      session.close();
    }
  }
}
