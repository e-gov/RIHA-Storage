package ee.eesti.riha.rest.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class NamesDAOImpl.
 */
// needed for getCurrentSession
@Transactional
@Component
public class NamesDAOImpl implements NamesDAO {

  @Autowired
  SessionFactory sessionFactory;

  private static final int MAX_NUM_OF_COLUMNS = 3;

  @Override
  public Map<String, String> getOrganizationNames(List<String> organizations) {

    String sql = "SELECT registrikood, nimetus FROM asutused.asutus " + "WHERE registrikood IN (:values)";

    return getGeneric(sql, organizations);

  }

  @Override
  public Map<String, String> getPersonNames(List<String> persons) {

    String sql = "SELECT kood, eesnimi, perenimi FROM asutused.isik " + "WHERE kood IN (:values)";

    return getGeneric(sql, persons);

  }

  @Override
  public Map<String, String> getUriNames(List<String> uris) {

    String sql = "SELECT uri, name FROM main_resource " + "WHERE uri IN (:values)";
    String sql_data_object = "SELECT uri, name FROM data_object " + "WHERE uri IN (:values)";

    Map<String, String> result = getGeneric(sql, uris);
    Map<String, String> result2 = getGeneric(sql_data_object, uris);
    result.putAll(result2);
    return result;
  }

  @Override
  public Map<String, String> getIdNames(List<String> ids) {

    String sql = "SELECT main_resource_id, name FROM main_resource " + "WHERE main_resource_id IN (:values)";
    List<Integer> integerIds = new ArrayList<Integer>();
    for (String id : ids) {
      integerIds.add(Integer.valueOf(id));
    }
    return getGeneric(sql, integerIds);
  }

  /**
   * Select 2 or 3 columns query helper<br>
   * SELECT a, b, (c) FROM z WHERE a IN (:values)<br>
   * result<br>
   * MAP["a"="b"] or MAP["a"= "b c"]
   */
  private Map<String, String> getGeneric(String sql, List values) {
    Session session = sessionFactory.getCurrentSession();

    Query query = session.createSQLQuery(sql);
    query.setParameterList("values", values);
    List<Object[]> result = query.list();

    Map<String, String> resultMap = new HashMap<>();
    for (Object[] arr : result) {
      if (arr.length == 2) {
        resultMap.put((String) arr[0].toString(), (String) arr[1]);
      } else if (arr.length == MAX_NUM_OF_COLUMNS) {
        resultMap.put((String) arr[0].toString(), (String) arr[1] + " " + (String) arr[2]);
      } else {
        throw new IllegalArgumentException("SQL should only select 2 or 3 values in this method!");
      }
    }
    return resultMap;

  }

}
