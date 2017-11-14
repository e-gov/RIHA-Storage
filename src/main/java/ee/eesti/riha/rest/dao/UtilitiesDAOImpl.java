package ee.eesti.riha.rest.dao;

import ee.eesti.riha.rest.dao.util.DAOConstants;
import ee.eesti.riha.rest.model.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.math.BigInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class UtilitiesDAOImpl.
 *
 * @param <T> the generic type
 */
// needed for getCurrentSession
@Transactional
@Component
public class UtilitiesDAOImpl<T> implements UtilitiesDAO<T> {

  @Autowired
  private SessionFactory sessionFactory;

  private static final Logger LOG = LoggerFactory.getLogger(UtilitiesDAOImpl.class);

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.UtilitiesDAO#getNextSeqValForPKForTable(java.lang.Class)
   */
  @Override
  public Integer getNextSeqValForPKForTable(Class<T> classRepresentingTable) {

    Session session = sessionFactory.getCurrentSession();

    String seqName = "";
    if (classRepresentingTable.equals(Main_resource.class)) {
      seqName = DAOConstants.MAIN_RESOURCE_PK_SEQ_NAME;
    } else if (classRepresentingTable.equals(Main_resource_relation.class)) {
      seqName = DAOConstants.MAIN_RESOURCE_RELATION_PK_SEQ_NAME;
    } else if (classRepresentingTable.equals(Document.class)) {
      seqName = DAOConstants.DOCUMENT_PK_SEQ_NAME;
    } else if (classRepresentingTable.equals(Data_object.class)) {
      seqName = DAOConstants.DATA_OBJECT_PK_SEQ_NAME;
    } else if (classRepresentingTable.equals(Comment.class)) {
      seqName = DAOConstants.COMMENT_PK_SEQ_NAME;
    } else {
      throw new IllegalStateException("Wrong class provided or code needs class->seq mapping specified.");
    }
    Query query = session.createSQLQuery("SELECT nextval('" + seqName + "')");
    int nextPK = ((BigInteger) query.uniqueResult()).intValue();
    LOG.info("NEXT PK: " + nextPK);
    return nextPK;

  }

}
