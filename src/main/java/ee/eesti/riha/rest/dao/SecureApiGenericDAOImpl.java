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
  private static final Logger LOG = LoggerFactory.getLogger(SecureApiGenericDAOImpl.class);

  @Autowired
  private ApiGenericDAO<T, K> genericDAO;

  @Override
  public List<T> find(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents,
                      String sort) throws RihaRestException {
    return genericDAO.find(clazz, limit, offset, filterComponents, sort);
  }

  @Override
  public T find(Class<T> clazz, Integer id) {
    T item = genericDAO.find(clazz, id);
    if (item == null) {
      return null;
    }

    return item;
  }

  @Override
  public List<T> findByMainResourceId(Class<T> clazz, Integer id) {
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
  public Integer findCount(Class<T> clazz) {

    return genericDAO.findCount(clazz);
  }

  @Override
  public List<K> create(T object) throws RihaRestException {
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
    T old = genericDAO.find((Class<T>) object.getClass(), id);
    try {
      Validator.noSuchIdInGivenTable(old, id);
    } catch (Exception e) {
      LOG.error("Error while updating", e);
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
  public int delete(Class<T> clazz, Integer id) {
    T old = genericDAO.find(clazz, id);
    try {
      Validator.noSuchIdInGivenTable(old, id);
    } catch (Exception e) {
      LOG.error("Error while deleting", e);
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
