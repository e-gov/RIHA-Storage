package ee.eesti.riha.rest.dao;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.TableEntryCreateLogic;
import ee.eesti.riha.rest.logic.Validator;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.model.BaseModel;

@Component
public class SecureApiGenericDAOImpl<T, K> implements SecureApiGenericDAO<T, K> {

  @Autowired
  private ApiGenericDAO<T, K> genericDAO;

  private static final Logger LOG = LoggerFactory.getLogger(SecureApiGenericDAOImpl.class);

  // LOGIC
  // get Main_resource if Main_resource or by main_resource_id if other table
  // get kindId from main_resource
  // get access restriction from main_resource
  // get access restrictions from parent main_resources
  // get max access restriction
  // get Role_rights: DEFAULT, AUTHENTICATED, authInfo.getRoll()
  // filter Role_rights by kindId && accessRestriction
  // calculate Role_right from role_Rights
  // call canCreate(<Calculated Role_right>, <AuthInfo>, <Main_resource owner>}

  @Override
  public List<T> find(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents,
                      String sort) throws RihaRestException {
    System.out.println("FIND MANY");

    return genericDAO.find(clazz, limit, offset, filterComponents, sort);
  }

  @Override
  public T find(Class<T> clazz, Integer id) throws RihaRestException {

    T item = genericDAO.find(clazz, id);
    if (item == null) {
      return null;
    }

    return item;
  }

  @Override
  public List<T> findByMainResourceId(Class<T> clazz, Integer id) throws RihaRestException {

    // main_resource is already checked on higher level that it may be read (ServicLogic.getResourceById)

    return genericDAO.findByMainResourceId(clazz, id);
  }

  @Override
  // TODO Milleks limit, offset ja sort siin?
  public Integer findCount(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents,
                           String sort) throws RihaRestException {

    if (limit != null && limit == 0) {
      // special case for count
      return 0;
    }

    return genericDAO.findCount(clazz, limit, offset, filterComponents, sort);
  }

  @Override
  public Integer findCount(Class<T> clazz) throws RihaRestException {

    return genericDAO.findCount(clazz);
  }

  @Override
  public List<K> create(T object) throws RihaRestException {

    System.out.println("CREATE " + JsonHelper.GSON.toJson(object));
    Validator.documentMustHaveReference(object);

    return genericDAO.create(object);
  }

  @Override
  public List<K> create(List<T> objects) throws RihaRestException {

    for (T object : objects) {
      Validator.documentMustHaveReference(object);
    }

    return genericDAO.create(objects);
  }

  @Override
  public int update(T object, Integer id) throws RihaRestException {

    System.out.println("CLASS " + object.getClass());
    T old = genericDAO.find((Class<T>) object.getClass(), id);
    try {
      Validator.noSuchIdInGivenTable(old, id);
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }

    return genericDAO.update(object, id);
  }

  @Override
  public int update(List<T> objects, String idFieldName) throws NoSuchFieldException,
      SecurityException, IllegalArgumentException, IllegalAccessException, RihaRestException {

    for (T object : objects) {
      BaseModel bm = (BaseModel) object;
      JsonObject jsonContent = bm.getJson_content();
      JsonElement je = jsonContent.get(idFieldName);
      if (je != null && !je.isJsonNull() && je.isJsonPrimitive()) {
        FilterComponent fc = new FilterComponent(idFieldName, "=", je.getAsString());
        List<T> existing = genericDAO.find((Class<T>) object.getClass(), null, null, Arrays.asList(fc), null);
      }
    }

    return genericDAO.update(objects, idFieldName);
  }

  @Override
  public int delete(Class<T> clazz, Integer id) throws RihaRestException {

    T old = genericDAO.find(clazz, id);
    try {
      Validator.noSuchIdInGivenTable(old, id);
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }

    return genericDAO.delete(clazz, id);
  }

  @Override
  public int delete(String tableName, String key, Object[] values) throws RihaRestException {

    Class<T> clazz = Finals.getClassRepresentingTable(tableName);

    if (StringHelper.areEqual(Finals.ID, key)) {
      key = TableEntryCreateLogic.createPKFieldName(clazz);
    }

    for (Object value : values) {
      FilterComponent fc = new FilterComponent(key, "=", value + "");
      List<T> toBeDeleted = genericDAO.find(clazz, null, null, Arrays.asList(fc), null);
    }

    return genericDAO.delete(tableName, key, values);
  }

}
