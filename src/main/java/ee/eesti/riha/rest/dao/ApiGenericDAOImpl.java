package ee.eesti.riha.rest.dao;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.dao.util.DaoHelper;
import ee.eesti.riha.rest.dao.util.FieldTypeHolder;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.dao.util.OrderByData;
import ee.eesti.riha.rest.dao.util.SqlFilter;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.Validator;
import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.logic.util.JsonContentBasedTable;
import ee.eesti.riha.rest.logic.util.JsonFieldHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.util.DisallowUseMethodForUpdate;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.Table;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class ApiGenericDAOImpl.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
@Transactional
@Component
public class ApiGenericDAOImpl<T, K> implements ApiGenericDAO<T, K> {
  private static final Logger LOG = LoggerFactory.getLogger(ApiGenericDAOImpl.class);
  public static final int NOT_PART_OF_MODEL_OR_JSON = -1001;

  @Autowired
  SessionFactory sessionFactory;

  @Autowired
  KindRepository kindRepository;

  @Autowired
  SqlFilter sqlFilter;

  /**
   * Gets the table name.
   *
   * @param clazz the clazz
   * @return the table name
   */
  private String getTableName(Class<T> clazz) {
    if (JsonContentBasedTable.isJsonContentBasedTable(clazz)) {
      return clazz.getSimpleName();
    } else {
      Table table = clazz.getAnnotation(Table.class);
      return (StringUtils.isBlank(table.schema()) ? "" : table.schema() + ".") + table.name();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#find(java.lang.Class, java.lang.Integer, java.lang.Integer,
   * java.util.List, java.lang.String)
   */
  @Override
  public List<T> find(Class<T> clazz, Integer limit, Integer offset,
      List<FilterComponent> filterComponents, String sort) throws RihaRestException {

    Session session = sessionFactory.getCurrentSession();

    // prepare arguments for helper method

    // String tableName = clazz.getSimpleName();
    String tableName = getTableName(clazz);

    Field pkField = DaoHelper.getPrimaryKeyOfDataModel(clazz);

    // ATTEND limit
    limit = limitHelper(limit);

    offset = offsetHelper(offset);

    List<T> objectList = new ArrayList<>();

    // ATTEND sort
    OrderByData orderData = OrderByData.construct(sort, clazz, pkField.getName(), true);

    // ATTEND filterComponents
    if (filterComponents != null) {
      boolean isCount = false;
      Query query = findHelper(clazz, limit, offset, filterComponents, isCount, session, orderData, tableName);
      objectList = query.list();
    } else {
      objectList = noFilterOrderHelper(session, clazz, orderData, limit, offset);
    }
    return objectList;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#find(java.lang.Class, java.lang.Integer)
   */
  @Override
  public T find(Class<T> clazz, Integer id) {

    Session session = sessionFactory.getCurrentSession();

    LOG.info("FIND BY ID");
    // Class.cast() removes unchecked cast warning
    T object = clazz.cast(session.get(clazz, id));
    return object;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#findByMainResourceId(java.lang.Class, java.lang.Integer)
   */
  @Override
  public List<T> findByMainResourceId(Class<T> clazz, Integer id) {

    LOG.info("FIND BY MAIN_RESOURCE_ID");

    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<T> criteria = cb.createQuery(clazz);
    Root<T> root = criteria.from(clazz);

    criteria.where(cb.equal(root.get(Finals.MAIN_RESOURCE_ID), id));

    return session.createQuery(criteria).getResultList();
  }

  /**
   * Limit helper.
   *
   * @param limit the limit
   * @return the int
   */
  private int limitHelper(Integer limit) {
    // ignore max value
    if (limit == null) {
      return Integer.MAX_VALUE;
    }
    return limit;
    // // don't allow to return more lines than allowed
    // if (limit == null || limit > Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED
    // || limit < 0) {
    // // force limit to match allowed
    // limit = Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED;
    // }
    // return limit;
  }

  /**
   * Offset helper.
   *
   * @param offset the offset
   * @return the int
   */
  private int offsetHelper(Integer offset) {
    if (offset == null || offset < 0) {
      offset = 0;
    }
    return offset;
  }

  /**
   * Json filter fields exist.
   *
   * @param session the session
   * @param tableName the table name
   * @param filterComponents the filter components
   * @return true, if successful
   */
  private boolean jsonFilterFieldsExist(Session session, String tableName, List<FilterComponent> filterComponents) {
    boolean allExist = true;
    for (FilterComponent fc : filterComponents) {
      if (!jsonFieldExists(session, tableName, fc.getOperandLeft())) {
        allExist = false;
        break;
      }
    }
    return allExist;
  }

  /**
   * Find helper.
   *
   * @param clazz the clazz
   * @param limit the limit
   * @param offset the offset
   * @param filterComponents the filter components
   * @param isCount the is count
   * @param session the session
   * @param orderData the order data
   * @param tableName the table name
   * @return the query
   * @throws RihaRestException the riha rest exception
   */
  private Query findHelper(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents,
      boolean isCount, Session session, OrderByData orderData, String tableName) throws RihaRestException {

    Query query = null;

    // ATTEND filterComponents
    if (filterComponents != null) {

      // if any of the fields does not exist in model, then query over
      // json_content field

      StringBuffer queryString = new StringBuffer();

      // next construct where clause separated by AND
      String joinedAsOneStr = "";
      List<String> allFilters = new ArrayList<>();
      //construct HQL query if isCount == false
      if (isCount) {
        queryString.append("SELECT count(*) FROM (");
      }
      queryString.append("SELECT * FROM ")
              .append(tableName)
              .append(" item ");

      Map<String, Object> params = new HashMap<>();
      for (int i = 0 ; i < filterComponents.size(); i++) {
        FilterComponent filterComponent = filterComponents.get(i);
        if (DaoHelper.isFieldPartOfModel(filterComponent.getOperandLeft(), clazz)) {
          try {
            String filter = sqlFilter.constructSqlFilter(filterComponent, clazz, params, i);
            allFilters.add(filter);
          } catch (NumberFormatException e) {
            MyExceptionHandler.numberFormat(e, " filter " + filterComponents);
          } catch (ParseException e) {
            MyExceptionHandler.dateFormat(e, " filter " + filterComponents);
          } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // this should never happen, because allFieldsExistInModel
            // already tested whether field exists or not
            LOG.error("Error while constructing query", e);
            throw new RuntimeException(e);
          }
        } else {
          if (jsonFieldExists(session, tableName, filterComponent.getOperandLeft())) {
            String filter = sqlFilter.constructSqlOverJsonFilter(filterComponent, clazz, params, i);
            allFilters.add(filter);
          }
        }
      }

      joinedAsOneStr = StringUtils.join(allFilters, " AND ");
      if (!allFilters.isEmpty()) {
        queryString.append("WHERE ");
      }
      queryString.append(joinedAsOneStr);
      LOG.info("SQL FILTER: " + joinedAsOneStr + " params " + params);

      if (allFilters.isEmpty()) {
        LOG.info("SQL FILTER: No existing filter fields were found.");
      } else if (allFilters.size() != filterComponents.size()) {
        LOG.info("SQL FILTER: Some filter fields were not taken into account as they exist neither in model nor in JSON content.");
      }

      if (DaoHelper.isFieldPartOfModel(orderData.getOrderByField(), clazz)) {
        queryString.append(" ORDER BY item.")
                .append(orderData.getDatabaseColumnName())
                .append((orderData.isAsc() ? " ASC " : " DESC "));
      } else {
        if (jsonFieldExists(session, tableName, orderData.getOrderByField())) {
          String orderByParameterName = "jOrderParameter";
          queryString.append(" ").append(createJsonQueryClause(orderByParameterName, orderData));

          String jsonOrderByFieldName = "{" + orderData.getOrderByField().replaceAll("\\.", ",") + "}";
          params.put(orderByParameterName, jsonOrderByFieldName);
        } else {
          LOG.info("Sorting order field was not taken into account as it exists neither in model nor in JSON content.");
        }
      }

      if (isCount) {
        queryString.append(" LIMIT ")
                .append(limit)
                .append(" OFFSET ")
                .append(offset)
                .append(") AS foo;");
        query = session.createSQLQuery(queryString.toString());
      } else {
        // get object of type clazz in results
        query = session.createSQLQuery(queryString.toString()).addEntity(clazz);
        query.setMaxResults(limit);
        query.setFirstResult(offset);
      }
      query.setProperties(params);
    }

    return query;
  }

  private String createJsonQueryClause(String orderByParameterName, OrderByData orderData) {
    return "ORDER BY" +
            " item." + Finals.JSON_CONTENT +
            " #>>" +
            " :" + orderByParameterName + "\\:\\:text[]" +
            (orderData.isAsc() ? " ASC" : " DESC");
  }

  /**
   * No filter order helper.
   *
   * @param session the session
   * @param clazz the clazz
   * @param orderData the order data
   * @param limit the limit
   * @param offset the offset
   * @return the list
   */
  private List<T> noFilterOrderHelper(Session session, Class clazz, OrderByData orderData, int limit, int offset) {
    List<T> objectList;
    Criteria criteria = session.createCriteria(clazz);

    boolean orderByIsJsonContentField = (!DaoHelper.isFieldPartOfModel(orderData.getOrderByField(), clazz));
    if (orderByIsJsonContentField) {
      StringBuilder qry = new StringBuilder();
      qry.append("SELECT *")
              .append(" FROM ").append(getTableName(clazz)).append(" item ");

      String orderByParameterName = "jOrderParameter";
      qry.append(" ").append(createJsonQueryClause(orderByParameterName, orderData));

      Query query = session.createSQLQuery(qry.toString()).addEntity(clazz);

      String jsonOrderByFieldName = "{" + orderData.getOrderByField().replaceAll("\\.", ",") + "}";
      query.setParameter(orderByParameterName, jsonOrderByFieldName);

      query.setFirstResult(offset);
      objectList = query.setMaxResults(limit).list();
    } else {
      if (orderData.isAsc()) {
        criteria.addOrder(Order.asc(orderData.getOrderByField()));
      } else {
        criteria.addOrder(Order.desc(orderData.getOrderByField()));
      }
      criteria.setFirstResult(offset);
      objectList = criteria.setMaxResults(limit).list();
    }

    return objectList;
  }

  /**
   * Count no filter.
   *
   * @param session the session
   * @param tableName the table name
   * @param limit the limit
   * @param offset the offset
   * @return the query
   */
  private Query countNoFilter(Session session, String tableName, Integer limit, Integer offset) {
    // no filter, only limit and offset
    return session.createSQLQuery("SELECT count(*) FROM " + "(SELECT * from " + tableName + " LIMIT " + limit
        + " OFFSET " + offset + ") AS foo;");
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#findCount(java.lang.Class, java.lang.Integer, java.lang.Integer,
   * java.util.List, java.lang.String)
   */
  @Override
  public Integer findCount(Class<T> clazz, Integer limit, Integer offset, List<FilterComponent> filterComponents,
      String sort) throws RihaRestException {

    Integer rowCount;
    Session session = sessionFactory.getCurrentSession();

    // prepare arguments for helper method

    // String tableName = clazz.getSimpleName();
    String tableName = getTableName(clazz);
    Field pkField = DaoHelper.getPrimaryKeyOfDataModel(clazz);

    // ATTEND limit
    limit = limitHelper(limit);

    offset = offsetHelper(offset);

    // ATTEND sort
    OrderByData orderData = OrderByData.construct(sort, clazz, pkField.getName(), true);

    // ATTEND filterComponents
    Query query;
    if (filterComponents != null) {
      boolean isCount = true;
      query = findHelper(clazz, limit, offset, filterComponents, isCount, session, orderData, tableName);
    } else {
      // no filter, only limit and offset
      query = countNoFilter(session, tableName, limit, offset);
    }

    rowCount = ((BigInteger) query.uniqueResult()).intValue();
    return rowCount;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#findCount(java.lang.Class)
   */
  @Override
  public Integer findCount(Class<T> clazz) {

    Session session = sessionFactory.getCurrentSession();

    Integer rowCount = ((Long) session.createCriteria(clazz).setProjection(Projections.rowCount()).uniqueResult())
        .intValue();

    return rowCount;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#create(java.lang.Object)
   */
  @Override
  public List<K> create(T object) {

    Session session = sessionFactory.getCurrentSession();

    // session.saveOrUpdate(object);
    LOG.info(JsonHelper.GSON.toJson(object));
    session.save(object);

    Serializable id = session.getIdentifier(object);
    return Arrays.asList((K) id);

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#create(java.util.List)
   */
  @Override
  public List<K> create(List<T> objects) {

    Session session = sessionFactory.getCurrentSession();

    Set<K> createdIds = new HashSet<>();
    for (T t : objects) {
      Integer id = (Integer) session.save(t);
      createdIds.add((K) id);
    }

    return new ArrayList<>(createdIds);

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#update(java.lang.Object, java.lang.Integer)
   */
  @Override
  public int update(T newValue, Integer id) throws RihaRestException {

    if (newValue == null) {
      return 0;
    }

    Session session = sessionFactory.getCurrentSession();

    Class<T> clazz = (Class<T>) newValue.getClass();
    T existing = clazz.cast(session.get(clazz, id));
    if (existing == null) {
      return 0;
    }

      return updateEntity(existing, newValue);
  }

  private int updateJsonContentEntity(T existing, T newValue) throws RihaRestException {
    Class<T> clazz = (Class<T>) newValue.getClass();
    Session session = sessionFactory.getCurrentSession();
    try {
      BaseModel baseModel = (BaseModel) existing;
      BaseModel updateInfo = (BaseModel) newValue;

      Validator.cantUpdateArchivedElement(existing);
      Validator.cantUpdateVersionHere(existing, newValue);

      JsonHelper.updateJsonObjWithValuesFromAnotherJsonObj(baseModel.getJson_content(), updateInfo.getJson_content(),
          clazz);

      // get nulls in updateInfo.getJson_content()
      // set corresponding nulls in existing
      copyNullValuesFromJson(existing, updateInfo.getJson_content());

      // set json_content on updateInfo to null so copyNotNull won't touch it
      updateInfo.setJson_content(null);

      copyNotNullValues(existing, newValue);
      session.update(existing);

    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | IntrospectionException e) {
     LOG.error("Error while updating JSON content", e);

      // no update
      return 0;
    }
    // success then 1 updated
    return 1;
  }

  private int updateEntity(T existing, T updatedEntity) {
    Session session = sessionFactory.getCurrentSession();

    try {
      copyNotNullValues(existing, updatedEntity);
      session.update(existing);
    } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
      LOG.error("Failed to update entity {}", existing);
      LOG.debug("Failed to update entity", e);
      
      return 0;
    }
    return 1;
  }

  /**
   * Copy fields that are not null and not annotated with {@link DisallowUseMethodForUpdate} to copyTo from copyFrom.
   *
   * @param <T> the generic type
   * @param copyTo the copy to
   * @param copyFrom the copy from
   * @throws IntrospectionException the introspection exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  public static <T> void copyNotNullValues(T copyTo, T copyFrom) throws IntrospectionException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(copyFrom.getClass(), Object.class)
        .getPropertyDescriptors()) {

      // get new field value
      Object fromFieldValue = propertyDescriptor.getReadMethod().invoke(copyFrom);

      // update if not null and not ignored
      DisallowUseMethodForUpdate ignore = propertyDescriptor.getWriteMethod().getAnnotation(
          DisallowUseMethodForUpdate.class);
      if (fromFieldValue != null && ignore == null) {
        propertyDescriptor.getWriteMethod().invoke(copyTo, fromFieldValue);
      }
    }

  }

  /**
   * Copy null values from json if update is allowed. Json field is null only if it is explicitly set to null
   *
   * @param <T> the generic type
   * @param copyTo the copy to
   * @param copyFrom the copy from
   * @throws IntrospectionException the introspection exception
   * @throws IllegalAccessException the illegal access exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws InvocationTargetException the invocation target exception
   */
  public static <T> void copyNullValuesFromJson(T copyTo, JsonObject copyFrom) throws IntrospectionException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Map<String, Object> fieldsWithNulls = new HashMap<>();
    for (Map.Entry<String, JsonElement> e : copyFrom.entrySet()) {
      if (e.getValue().isJsonNull()) {
        LOG.info(e.getKey() + " :: " + e.getValue());
        fieldsWithNulls.put(e.getKey(), e.getKey());
      }
    }

    for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(copyTo.getClass(), Object.class)
        .getPropertyDescriptors()) {

      String fieldName = JsonFieldHelper.fieldNameFromSetter(propertyDescriptor.getWriteMethod().getName());

      if (fieldsWithNulls.containsKey(fieldName)) {
        // update if not ignored
        DisallowUseMethodForUpdate ignore = propertyDescriptor.getWriteMethod().getAnnotation(
            DisallowUseMethodForUpdate.class);
        if (ignore == null) {
          propertyDescriptor.getWriteMethod().invoke(copyTo, (Object) null);
        }
      }
    }

  }

  /**
   * Find items where idFieldName value (in table or json_content) equals updateInfo's corresponding field value.
   *
   * @param idFieldName the id field name
   * @param updateInfo the update info
   * @param tableName the table name
   * @param clazz the clazz
   * @param session the session
   * @return null if field not part of model or json
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   */
  private Query queryExistingItems(String idFieldName, T updateInfo, String tableName, Class<T> clazz, Session session)
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    Query queryExisting;
    if (DaoHelper.isFieldPartOfHibernateModel(idFieldName, clazz)) {
      FieldTypeHolder idField = FieldTypeHolder.construct(updateInfo, idFieldName);
      queryExisting = session.createQuery("FROM " + tableName + " item WHERE item." + idFieldName + "=:idFieldValue");
      queryExisting.setParameter("idFieldValue", idField.getValue());
    } else if (jsonFieldExists(session, tableName, idFieldName)) {
      // select * from main_resource
      // where json_content ->> 'test_abc' = '1234';
      String idFieldNameParameter = "idFieldNameParam";
      String sql = "SELECT * FROM " + tableName + " where json_content ->> :" + idFieldNameParameter + " =:idFieldValue";
      queryExisting = session.createSQLQuery(sql).addEntity(clazz);

      BaseModel bm = (BaseModel) updateInfo;
      String fieldValueString = bm.getJson_content().get(idFieldName).getAsString();
      queryExisting.setParameter("idFieldValue", fieldValueString);
      queryExisting.setParameter(idFieldNameParameter, idFieldName);
    } else {
      // return NOT_PART_OF_MODEL_OR_JSON;
      queryExisting = null;
    }
    return queryExisting;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#update(java.util.List, java.lang.String)
   */

  /**
   * Update of non json entities is not supported. Use {@link #update(Object, Integer)} method instead.
   * @param objects
   * @param idFieldName name of the field by which the update will be done, must not be null in object list elements
   * @return
   * @throws NoSuchFieldException
   * @throws SecurityException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws RihaRestException
   */
  @Override
  public int update(List<T> objects, String idFieldName) throws NoSuchFieldException, SecurityException,
      IllegalArgumentException, IllegalAccessException, RihaRestException {

    int numOfChanged = 0;
    // no need for lowerCase
    String tableName = objects.get(0).getClass().getSimpleName();
    Class<T> clazz = Finals.getClassRepresentingTable(tableName);
    Session session = sessionFactory.getCurrentSession();

    for (T updateData : objects) {
      // get objects in database that must be updated
      Query queryExisting = queryExistingItems(idFieldName, updateData, tableName, clazz, session);
      List<T> existingItems;
      if (queryExisting != null) {
        existingItems = queryExisting.list();
      } else {
        return NOT_PART_OF_MODEL_OR_JSON;
      }

      for (T item : existingItems) {
        // update changed fields
        try {
          // update changed json fields on existingitems from objects
          BaseModel baseModel = (BaseModel) item;
          BaseModel updateInfo = (BaseModel) updateData;

          Validator.cantUpdateArchivedElement(item);
          Validator.cantUpdateVersionHere(item, updateData);

          FileHelper.writeDocumentContentToFile(updateData, baseModel.callGetId());

          JsonHelper.updateJsonObjWithValuesFromAnotherJsonObj(baseModel.getJson_content(),
              updateInfo.getJson_content(), clazz);

          // get nulls in updateInfo.getJson_content()
          // set corresponding nulls in existing
          copyNullValuesFromJson(item, updateInfo.getJson_content());

          // set json_content on updateInfo to null so copyNotNull won't touch it
          JsonObject updateInfoJsonContent = updateInfo.getJson_content();
          updateInfo.setJson_content(null);

          copyNotNullValues(item, updateData);
          session.update(item);

          // set updateInfo json_content to its old value
          updateInfo.setJson_content(updateInfoJsonContent);
          numOfChanged++;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | IntrospectionException | IOException e) {
          LOG.error("Error updating, ROLLING BACK UPDATES", e);
          return 0;
        }
      }
    }

    return numOfChanged;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#delete(java.lang.Class, java.lang.Integer)
   */
  @Override
  public int delete(Class<T> type, Integer id) {

    int numOfDeleted = 0;
    if (type == null || id == null) {
      return numOfDeleted;
    }
    Session session = sessionFactory.getCurrentSession();

    T toBeDeleted = find(type, id);
    if (toBeDeleted != null) {
      session.delete(toBeDeleted);
      numOfDeleted = 1;
    }

    return numOfDeleted;

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#delete(java.lang.Object)
   */
  // used in tests only
  @Override
  public void delete(T object) {

    Session session = sessionFactory.getCurrentSession();

    session.delete(object);

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#delete(java.util.List)
   */
  // used in tests only
  @Override
  public void delete(List<T> objects) {

    Session session = sessionFactory.getCurrentSession();

    for (T t : objects) {
      session.delete(t);
    }

  }

  // special logic for documents
  // need to get documents to be deleted IDs, to delete corresponding
  // document files on disk
  /**
   * Document delete ids.
   *
   * @param session the session
   * @param tableName the table name
   * @param className the class name
   * @param key the key
   * @return the query
   */
  // if key is part of Document model
  private Query documentDeleteIds(Session session, String tableName, String className, String key) {
    Query documentQuery = null;
    if ((Class) Finals.getClassRepresentingTable(tableName) == Document.class
            && DaoHelper.isFieldPartOfModel(key, Finals.getClassRepresentingTable(tableName))) {
      String documentHQL = "select document_id from " + className + " where " + key + " IN (:fieldValues)";
      documentQuery = session.createQuery(documentHQL);
    }
    return documentQuery;
  }

  /**
   * Document delete ids json.
   *
   * @param session the session
   * @param tableName the table name
   * @param className the class name
   * @param key the key
   * @return the query
   */
  // if key is part of Document json
  private Query documentDeleteIdsJson(Session session, String tableName, String className, String key) {
    Query documentQuery = null;
    if ((Class) Finals.getClassRepresentingTable(tableName) == Document.class
            && jsonFieldExists(session, className, key)) {
      String keyParameter = "keyParameter";
      String documentSQL = "select document_id from " + className + " where json_content ->> :" + keyParameter
          + " IN (:fieldValues)";
      documentQuery = session.createSQLQuery(documentSQL);
      documentQuery.setParameter(keyParameter, key);
    }
    return documentQuery;
  }

  /**
   * Query document delete ids.
   *
   * @param documentQuery the document query
   * @param tableName the table name
   * @param values the values
   * @return the list
   */
  // get ids that must be deleted
  private List<Integer> queryDocumentDeleteIds(Query documentQuery, String tableName, Object[] values) {
    List<Integer> documentIds = null;
    if ((Class) Finals.getClassRepresentingTable(tableName) == Document.class && documentQuery != null) {
      documentQuery.setParameterList("fieldValues", values);
      documentIds = documentQuery.list();
      LOG.info("DOCUMENT DELETE IDS: " + documentIds);
    }
    return documentIds;
  }

  /**
   * Delete document files.
   *
   * @param tableName the table name
   * @param documentIds the document ids
   * @throws IOException Signals that an I/O exception has occurred.
   */
  // delete files on disk
  private void deleteDocumentFiles(String tableName, List<Integer> documentIds) throws IOException {
    if ((Class) Finals.getClassRepresentingTable(tableName) == Document.class && documentIds != null) {
      for (int id : documentIds) {
        FileHelper.deleteFile(FileHelper.PATH_ROOT + FileHelper.createDocumentFilePath(id));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.dao.ApiGenericDAO#delete(java.lang.String, java.lang.String, java.lang.Object[])
   */
  @Override
  public int delete(String tableName, String key, Object[] values) {

    int numOfDeleted = 0;
    Session session = sessionFactory.getCurrentSession();

    String className = StringUtils.capitalize(tableName);
    Query query;
    Query documentQuery = null;
    if (DaoHelper.isFieldPartOfHibernateModel(key, Finals.getClassRepresentingTable(tableName))) {
      documentQuery = documentDeleteIds(session, tableName, className, key);
      String hql = "delete from " + className + " where " + key + " IN (:fieldValues)";
      query = session.createQuery(hql);
    } else if (jsonFieldExists(session, tableName, key)) {
      documentQuery = documentDeleteIdsJson(session, tableName, className, key);
      // delete from main_resource
      // where json_content ->> 'test_abc' IN ('test_123', 'test_1234');
      String keyParameter = "keyParam";
      String sql = "delete from " + className + " where json_content ->> :" + keyParameter + " IN (:fieldValues)";
      query = session.createSQLQuery(sql);
      query.setParameter(keyParameter, key);
      // psql cannot get number from json, only json or text
      // therefore possible numbers must be converted to strings
      // to enable comparison in DELETE WHERE clause
      values = StringHelper.convertToString(values);
    } else {
      return NOT_PART_OF_MODEL_OR_JSON;
    }

    List<Integer> documentIds = queryDocumentDeleteIds(documentQuery, tableName, values);

    query.setParameterList("fieldValues", values);
    numOfDeleted = query.executeUpdate();

    try {
      deleteDocumentFiles(tableName, documentIds);
    } catch (IOException e) {
      LOG.error("Error while deleting", e);
      throw new RuntimeException(e);
    }

    return numOfDeleted;

  }

  /**
   * Searches if any row has json_content which has the field key. Key may be represent deeper level json key by using
   * dots in the key name. In order to check existence of the key, separate key to tokens and construct correct query
   * with nested key tokens. Additionally, for security reasons, avoid adding plain text keys to the native query.
   * Resulting query with key owner.name will look like:
   * <pre>
   * ... where (json_content-> :keyToken0\:\:text -> :keyToken1\:\:text) is not null
   * </pre>, where parameters keyToken0 and keyToken1 will be 'owner' and 'name' respectively.
   *
   * @param session   the session
   * @param tableName the table name
   * @param key       the key
   * @return true if exists
   */
  private boolean jsonFieldExists(Session session, String tableName, String key) {
    if (key.equals("kind")) {
      // TODO should find a better way for this
      return true;
    }
    if (tableName.contains(".")) {
      // TODO could be done better, currently expect that non-json_content
      // tables in different schema -> .
      return false;
    }
    // select count(*) from main_resource
    // where (json_content->'test_abc') is not null;

    Class classRepresentingTable = Finals.getClassRepresentingTable(tableName);
    if (!JsonContentBasedTable.isJsonContentBasedTable(classRepresentingTable)) {
      return false;
    }

    // Construct SQL query respecting nested keys
    String[] keyTokens = StringUtils.split(key, ".");
    if (keyTokens.length == 0) {
      return false;
    }

    // Create indexed key token list and parameters for the query
    List<String> conditionTokens = new ArrayList<>();
    Map<String, String> parameters = new HashMap<>();
    for (int i = 0; i < keyTokens.length; i++) {
      String keyTokenParameterName = "keyToken" + i;
      conditionTokens.add(":" + keyTokenParameterName + "\\:\\:text");
      parameters.put(keyTokenParameterName, keyTokens[i]);
    }

    // Create native SQL query with key tokens
    Query q = session.createSQLQuery("select count(*) from " + tableName +
                                             " where (" + Finals.JSON_CONTENT + "->" +
                                             StringUtils.join(conditionTokens, "->") +
                                             ") is not null;");
    q.setProperties(parameters);
    int rowCount = ((BigInteger) q.uniqueResult()).intValue();
    return rowCount > 0;
  }

}
