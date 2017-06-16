package ee.eesti.riha.rest.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.AuthServiceProvider;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.logic.util.JsonFieldsFilterer;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.PathHolder;
import ee.eesti.riha.rest.logic.util.QueryHolder;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;

// TODO: Auto-generated Javadoc
/**
 * Responsible of first validation (input data), preparing some data, then sending call to lower level for actual
 * processing, and finally packaging results (or errors) into Response.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
@Component
public class ServiceLogic<T, K> {

  // object responsible for actual processing
  @Autowired
  ChangeLogic<T, K> changeLogic;

  @Autowired
  TokenStore tokenStore;

  @Autowired
  NewVersionLogic<T, K> newVersionLogic;

  AuthServiceProvider authServiceProvider = AuthServiceProvider.getInstance();

  private static final Logger LOG = LoggerFactory.getLogger(ServiceLogic.class);

  private ObjectMapper mapper = new ObjectMapper();

  // these fields should be removed from exported json
  private static final List<String> UNWANTED_EXPORT_FIELDS = Arrays.asList("old_id", "kind_id", 
      "older_data", "main_resource_id", "data_object_id", "document_id");
  
  /**
   * Gets the many.
   *
   * @param tableName the table name
   * @param limit the limit
   * @param offset the offset
   * @param filter the filter
   * @param sort the sort
   * @param fields the fields
   * @return the many
   */
  public Response getMany(String tableName, Integer limit, Integer offset, String filter, String sort, String fields,
      AuthInfo authInfo) {

    try {

      Validator.unknownTableRequested(tableName);

      Class<T> classRepresentingTable = Finals.getClassRepresentingTable(tableName);

      List<FilterComponent> filterComponents = null;
      if (filter != null) {
        String[] filterItems = prepareFilterItems(filter);
        if (filterItems != null) {
          Validator.countOfFilterItemsNeedsToBeCorrect(filterItems, filter);
          filterComponents = extractFilterComponents(filterItems);
        }
      }

      List<T> all = changeLogic.doGetMany(classRepresentingTable, limit, offset, filterComponents, sort, fields);
      return Response.ok(all).build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = tableName + ":" + limit + ":" + offset + ":" + filter + ":" + sort + ":" + fields;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  /**
   * Gets the by id.
   *
   * @param tableName the table name
   * @param id the id
   * @param fields the fields
   * @return the by id
   */
  public Response getById(String tableName, Integer id, String fields, AuthInfo authInfo) {

    try {

      Validator.unknownTableRequested(tableName);

      Class<T> classRepresentingTable = Finals.getClassRepresentingTable(tableName);
      T item = changeLogic.doGet(classRepresentingTable, id, fields);

      Validator.noSuchIdInGivenTable(item, id);

      return Response.ok(item).build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = tableName + ":" + id;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  /**
   * Gets the resource by id.
   *
   * @param id the id
   * @return the resource by id
   */
  public Response getResourceById(Integer id, AuthInfo authInfo) {

    try {
      T item = changeLogic.doGet((Class<T>) Main_resource.class, id, null);

      Validator.noSuchIdInGivenTable(item, id);

      List<T> documents = changeLogic.doGetByMainResourceId((Class<T>) Document.class, id);
      FileHelper.readDocumentFileToContent(documents, Document.class);
      Map<String, List<T>> fieldNameMap = extractItemsByFieldName(documents, Document.class);

      List<T> dataObjects = changeLogic.doGetByMainResourceId((Class<T>) Data_object.class, id);
      // get documents connnected to data_object
      addDocumentsToData_object(dataObjects, authInfo);

      fieldNameMap.putAll(extractItemsByFieldName(dataObjects, Data_object.class));

      // get connected services (Main_resource)
      List<T> services = getServicesByParentId(id, authInfo); 
      // add connected documents to services
      addDocumentsToService(services, authInfo);
      fieldNameMap.putAll(extractItemsByFieldName(services, Main_resource.class));
      
      ObjectNode objNode = (ObjectNode) item;

      addFieldMapToJson(objNode, fieldNameMap);

      // remove unwanted fields
      JsonFieldsFilterer.removeIdFields(objNode, UNWANTED_EXPORT_FIELDS);
      
      return Response.ok(item).build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = "main_resource:" + id;
      return handleUnspecifiedError(additionalHint, e);

    }

  }
  
  /**
   * Get child main_resources with kind "service". Actually returns List&ltObjectNode&gt
   */
  protected List<T> getServicesByParentId(Integer id, AuthInfo authInfo)
      throws RihaRestException {
    // get child main_resources
    FilterComponent fc = new FilterComponent("main_resource_parent_id", "=", "" + id);
    // get services only
    FilterComponent fcService = new FilterComponent("kind", "=", "service");
    List<T> servcies = changeLogic.doGetMany((Class<T>) Main_resource.class, 1, 0, Arrays.asList(fc, fcService),
        null, null);
    return servcies;
  }

  /**
   * Get documents connected to Main_resource with kind "service" and include them in service json/ObjectNode
   */
  private void addDocumentsToService(List<T> services, AuthInfo authInfo) throws RihaRestException {
    for (T service : services) {
      ObjectNode objNode = (ObjectNode) service;
      FilterComponent docHasMrId = new FilterComponent("main_resource_id", "=", objNode.get("main_resource_id").asText());
      List<T> connectedDocs = changeLogic.doGetMany((Class<T>) Document.class,
          null, null, Arrays.asList(docHasMrId), null, null);
      FileHelper.readDocumentFileToContent(connectedDocs, Document.class);
      Map<String, List<T>> fieldNameMapDataObject = extractItemsByFieldName(connectedDocs, Document.class);

      addFieldMapToJson(objNode, fieldNameMapDataObject);
    }
  }
  
  /**
   * Get documents connected to data_objects and include them in data_object json/ObjectNode
   */
  private void addDocumentsToData_object(List<T> dataObjects, AuthInfo authInfo) throws RihaRestException {
    for (T dataObject : dataObjects) {
      ObjectNode objNode = (ObjectNode) dataObject;
      FilterComponent docHasDataId = new FilterComponent("data_object_id", "=", objNode.get("data_object_id").asText());
      List<T> connectedDocs = changeLogic.doGetMany((Class<T>) Document.class,
          null, null, Arrays.asList(docHasDataId), null, null);
      FileHelper.readDocumentFileToContent(connectedDocs, Document.class);
      Map<String, List<T>> fieldNameMapDataObject = extractItemsByFieldName(connectedDocs, Document.class);

      addFieldMapToJson(objNode, fieldNameMapDataObject);
    }
  }

  /**
   * Add items in fieldNameMap to objectNode under field with name from fieldNameMap
   */
  private void addFieldMapToJson(ObjectNode objNode, Map<String, List<T>> fieldNameMap) {
    Iterator<Entry<String, List<T>>> it = fieldNameMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, List<T>> pair = it.next();

      JsonNode node = mapper.valueToTree(pair.getValue());
      objNode.set(pair.getKey().toString(), node);
      it.remove();
    }
  }

  /**
   * Extract items by field name. Use default value if no field_name given.
   *
   * @param items the items
   * @return the map
   */
  private Map<String, List<T>> extractItemsByFieldName(List<T> items, Class<?> clazz) {
    Map<String, List<T>> fieldNameMap = new HashMap<>();
    String defaultFieldName = "default_" + clazz.getSimpleName().toLowerCase() + "s";
    if (items != null && !items.isEmpty()) {
      for (T item : items) {
        ObjectNode obj = (ObjectNode) item;

        JsonNode fieldName = obj.get("field_name");
        if (fieldName != null) {
          String fieldNameStr = fieldName.asText();
          if (!fieldNameMap.containsKey(fieldNameStr)) {
            fieldNameMap.put(fieldNameStr, new ArrayList<T>());
          }

          fieldNameMap.get(fieldNameStr).add(item);

        } else {
          // use default value instead of field_name
          if (!fieldNameMap.containsKey(defaultFieldName)) {
            fieldNameMap.put(defaultFieldName, new ArrayList<T>());
          }
          fieldNameMap.get(defaultFieldName).add(item);

        }
      }
    }

    return fieldNameMap;
  }

  /**
   * Creates the.
   *
   * @param json the json
   * @param tableName the table name
   * @return the response
   */
  public Response create(String json, String tableName) {

    LOG.info("create API called");

    try {

      Validator.unknownTableRequested(tableName);
      Validator.tableCantBeModified(tableName);
      Validator.needsToBeValidJson(json);
      Validator.jsonCantBeEmpty(json);

      Class<T> classRepresentingTable = Finals.getClassRepresentingTable(tableName);
      List<K> createdKeys = changeLogic.doCreate(json, classRepresentingTable);

      return Response.ok(createdKeys).build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = tableName;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  /**
   * Update.
   *
   * @param json the json
   * @param tableName the table name
   * @param id the id
   * @return the response
   */
  public Response update(String json, String tableName, Integer id) {

    LOG.info("update API called");

    try {

      Validator.unknownTableRequested(tableName);
      Validator.tableCantBeModified(tableName);
      Validator.needsToBeValidJson(json);
      Validator.jsonCantBeEmpty(json);
      Validator.jsonCantBeArray(json);

      Map<String, Integer> updatedResult = null;
      Class<T> classRepresentingTable = Finals.getClassRepresentingTable(tableName);
      updatedResult = changeLogic.doUpdate(json, classRepresentingTable, id, Finals.NAME);

      LOG.info("" + updatedResult);
      return Response.ok(updatedResult).build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = json;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  /**
   * Delete.
   *
   * @param tableName the table name
   * @param id the id
   * @return the response
   */
  public Response delete(String tableName, Integer id) {

    LOG.info("delete API called");

    try {

      Validator.unknownTableRequested(tableName);
      Validator.tableCantBeModified(tableName);

      Map<String, Integer> deletedResult = changeLogic.doDelete(tableName, id);
      return Response.ok(deletedResult).build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = tableName + ":" + id;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  /**
   * Gets the cgi.
   *
   * @param operation the operation
   * @param path the path
   * @param token the token
   * @param limit the limit
   * @param offset the offset
   * @param filter the filter
   * @param sort the sort
   * @param fields the fields
   * @return the cgi
   */
  public Response getCGI(String operation, String path, String token, Integer limit, Integer offset, String filter,
      String sort, String fields) {

    LOG.info("getCGI API called");

    try {

      String[] reqPars = {"operation", "path" };
      Map<String, Object> parsToVal = new HashMap<>();
      parsToVal.put("operation", operation);
      parsToVal.put("path", path);
      parsToVal.put("token", token);
      parsToVal.put("offset", offset);

      Validator.cantHaveMissinReqParsInURL(reqPars, parsToVal);

      Validator.valueMustBeAllowed(Finals.GET_CGI_ALLOWED_VALUES, operation,
          ErrorCodes.INPUT_URL_OP_VALUE_UNKNOWN_OR_NOTSUITABLE,
          ErrorCodes.INPUT_URL_OP_VALUE_UNKNOWN_OR_NOTSUITABLE_MSG);

      PathHolder pathHolder = null;
      try {
        pathHolder = new PathHolder(path);
      } catch (IllegalStateException e) {
        RihaRestError error = new RihaRestError();
        error.setErrcode(ErrorCodes.INPUT_URL_PATH_VALUE_NOTVALID);
        error.setErrmsg(ErrorCodes.INPUT_URL_PATH_VALUE_NOTVALID_MSG);
        error.setErrtrace(path);
        throw new RihaRestException(error);
      }

      Validator.unknownTableRequested(pathHolder.tableName);

      List<FilterComponent> filterComponents = null;
      if (filter != null) {
        String[] filterItems = prepareFilterItems(filter);
        if (filterItems != null) {
          Validator.countOfFilterItemsNeedsToBeCorrect(filterItems, filter);
          filterComponents = extractFilterComponents(filterItems);
        }
      }

      QueryHolder queryHolder = new QueryHolder(operation, path, token, limit, offset, filterComponents, sort, fields);
      if (StringHelper.areEqual(operation, Finals.GET)) {
        Object result = changeLogic.doGet(queryHolder);
        return Response.ok(result).build();
      } else if (StringHelper.areEqual(operation, Finals.COUNT)) {
        Object result = changeLogic.doCount(queryHolder);
        return Response.ok(result).build();
      }
      return Response.ok().build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = operation + " : " + path + " : " + token + " : " + limit + " : " + offset;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  // {"op":"get","path":"db/mytable/123","token":"abca"}
  // {"op":"post", "path": "/db/mytable", "data":{ "value": 58.3788, "name":
  // "lat"}}
  // {"op":"put", "path": "/db/mytable/123", "data":{ "value": 58.3788,
  // "name": "lat"}}
  // {"op":"delete", "path": "/db/mytable/123"}
  /**
   * Post cgi.
   *
   * @param json the json
   * @return the response
   */
  // {"op":"delete", "path": "/db/mytable", "id":[123,456,777]}
  public Response postCGI(String json) {

    LOG.info("postCGI API called");

    try {

      Validator.needsToBeValidJson(json);
      Validator.jsonCantBeEmpty(json);

      // should have at least - op and path
      QueryHolder queryHolder = QueryHolder.create(JsonHelper.GSON, json);

      // getnames doesn't have path
      if (StringHelper.areEqual(queryHolder.getOp(), Finals.GET_NAMES)) {
        return specialCGI(queryHolder);
      }

      String[] reqPars = {"op", "path" };
      Validator.cantHaveMissingReqParsInJson(reqPars, json);

      Validator.valueMustBeAllowed(Finals.POST_CGI_ALLOWED_VALUES, queryHolder.getOp(),
          ErrorCodes.INPUT_JSON_OP_VALUE_UNKNOWN, ErrorCodes.INPUT_JSON_OP_VALUE_UNKNOWN_MSG);

      AuthInfo user = null;
      if (!Finals.READ_ALLOWED_VALUES.contains(queryHolder.getOp())) {
        // no need to currently authenticate for GET requests
        user = TokenValidator.isTokenOk(queryHolder.getToken(), tokenStore);

      } else {
        // allow to read if token not given
        if (!StringUtils.isEmpty(queryHolder.getToken())) {
          user = TokenValidator.isTokenOk(queryHolder.getToken(), tokenStore);
        } else {
          user = AuthInfo.DEFAULT;
        }
      }

      String[] filterItems = prepareFilterItems(queryHolder);
      if (filterItems != null) {
        Validator.countOfFilterItemsNeedsToBeCorrect(filterItems, json);
        List<FilterComponent> filter = extractFilterComponents(filterItems);
        queryHolder.setFilter(filter);
      }

      PathHolder pathHolder = null;
      try {
        pathHolder = new PathHolder(queryHolder.getPath());
      } catch (IllegalStateException e) {
        RihaRestError error = new RihaRestError();
        error.setErrcode(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID);
        error.setErrmsg(ErrorCodes.INPUT_JSON_PATH_VALUE_NOTVALID_MSG);
        error.setErrtrace(queryHolder.getPath());
        throw new RihaRestException(error);
      }

      Validator.unknownTableRequested(pathHolder.tableName);

      if (!Finals.READ_ALLOWED_VALUES.contains(queryHolder.getOp())) {
        Validator.tableCantBeModified(pathHolder.tableName);
      }

      Class<T> classRepresentingTable = Finals.getClassRepresentingTable(pathHolder.tableName);

      if (StringHelper.areEqual(queryHolder.getOp(), Finals.GET)) {

        Object result = changeLogic.doGet(queryHolder);
        return Response.ok(result).build();

      } else if (StringHelper.areEqual(queryHolder.getOp(), Finals.POST)) {

        String[] reqPars1 = {"data" };
        Validator.cantHaveMissingReqParsInJson(reqPars1, json);

        List<K> createdKeys = changeLogic.doCreate(queryHolder.getData().toString(), classRepresentingTable);
        return Response.ok(createdKeys).build();

      } else if (StringHelper.areEqual(queryHolder.getOp(), Finals.PUT)) {

        String[] reqPars1 = {"data" };
        Validator.cantHaveMissingReqParsInJson(reqPars1, json);

        Map<String, Integer> updateResult = changeLogic.doUpdate(queryHolder);
        return Response.ok(updateResult).build();

      } else if (StringHelper.areEqual(queryHolder.getOp(), Finals.DELETE)) {

        Map<String, Integer> deletedResult = changeLogic.doDelete(queryHolder);
        return Response.ok(deletedResult).build();

      } else if (StringHelper.areEqual(queryHolder.getOp(), Finals.COUNT)) {

        Map<String, Integer> resultCount = changeLogic.doCount(queryHolder);
        return Response.ok(resultCount).build();

      } else if (StringHelper.areEqual(queryHolder.getOp(), Finals.NEW_VERSION)) {

        Object result = newVersionLogic.doNewVersion(queryHolder);
        return Response.ok(result).build();

      }

      return Response.ok().build();

    } catch (RihaRestException e) {

      return handleKnownError(e);

    } catch (Exception e) {

      String additionalHint = json;
      return handleUnspecifiedError(additionalHint, e);

    }

  }

  /**
   * Special cgi.
   *
   * @param queryHolder the query holder
   * @return the response
   * @throws RihaRestException the riha rest exception
   */
  private Response specialCGI(QueryHolder queryHolder) throws RihaRestException {
    Validator.valueMustBeAllowed(Finals.POST_CGI_ALLOWED_VALUES, queryHolder.getOp(),
        ErrorCodes.INPUT_JSON_OP_VALUE_UNKNOWN, ErrorCodes.INPUT_JSON_OP_VALUE_UNKNOWN_MSG);

    Map<String, Map<String, String>> names = changeLogic.doGetNames(queryHolder);

    return Response.ok(names).build();
  }

  /**
   * Handle unspecified error.
   *
   * @param anyAdditionalHint the any additional hint
   * @param e the e
   * @return the response
   */
  private Response handleUnspecifiedError(String anyAdditionalHint, Exception e) {
    // one for log
    e.printStackTrace();

    // and one for client
    RihaRestError error = MyExceptionHandler.unmapped(e, anyAdditionalHint);

    return Response.status(Status.BAD_REQUEST).entity(error).build();
  }

  /**
   * Handle known error.
   *
   * @param e the e
   * @return the response
   */
  private Response handleKnownError(RihaRestException e) {
    // clearly specified errors thrown from below levels

    return Response.status(Status.BAD_REQUEST).entity(e.getError()).build();

  }

  /**
   * Explain by example. Having name,=,prepareSignature,main_resource_parent_id,>,29484. Produces array:
   * [name,=,prepareSignature,main_resource_parent_id,>,29484].
   *
   * @param allFilterInfoOnOneLine the all filter info on one line
   * @return the string[]
   */
  private String[] prepareFilterItems(String allFilterInfoOnOneLine) {
    // extract filter items into list of filter components
    return allFilterInfoOnOneLine.split(Finals.FILTER_ITEMS_SEPARATOR);
  }

  /**
   * Breaks filter data given in many array lists into one single array.
   * 
   * Example. Having list of lists [["state",">","C"],["state","<","F"]] or just list ["state",">","C"]. Produces array:
   * ["state",">","C","state","<","F"] or ["state",">","C"].
   *
   * @param queryHolderContainingFilter the query holder containing filter
   * @return the string[]
   */
  private String[] prepareFilterItems(QueryHolder queryHolderContainingFilter) {

    List<String> filterItems = new ArrayList<>();
    List<T> filterData = (List<T>) queryHolderContainingFilter.getFilter();
    if (filterData != null && filterData.size() != 0) {
      if (isFirstElementAList(filterData)) {
        // many arrays
        List<List<String>> filterStrings = (List<List<String>>) filterData;
        // need to get elements in different arraylists into one single
        // arraylist
        for (List<String> filterIs : filterStrings) {
          List<String> converted = StringHelper.convertToString(filterIs);
          filterItems.addAll(converted);
        }
      } else {
        // one array
        filterItems = StringHelper.convertToString(filterData);
      }

      // get output as string array
      return filterItems.toArray(new String[filterItems.size()]);
    } else {
      return null;
    }

  }

  /**
   * Checks if is first element a list.
   *
   * @param filterData the filter data
   * @return true, if is first element a list
   */
  private boolean isFirstElementAList(List<T> filterData) {
    return (filterData.get(0).getClass().equals(List.class) || filterData.get(0).getClass().equals(ArrayList.class));
  }

  /**
   * Explain by example. Having array [name,=,prepareSignature,main_resource_parent_id,>,29484]. Produces list of
   * {@link FilterComponent}s, first containing name,=,prepareSignature and second main_resource_parent_id,>,29484.
   *
   * @param filterItems the filter items
   * @return list of {@link FilterComponent}s
   * @throws RihaRestException the riha rest exception
   */
  private List<FilterComponent> extractFilterComponents(String[] filterItems) throws RihaRestException {

    List<FilterComponent> filterComponents = new ArrayList<>();
    for (int i = 0; i < filterItems.length; i += Finals.NUM_OF_FILTER_ITEMS) {
      String field = filterItems[i];
      String operator = filterItems[i + 1];
      String value = filterItems[i + 2];
      FilterComponent fC = new FilterComponent(field, operator, value);
      filterComponents.add(fC);
    }

    return filterComponents;

  }

}
