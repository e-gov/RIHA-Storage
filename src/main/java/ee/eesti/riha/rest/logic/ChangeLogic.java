package ee.eesti.riha.rest.logic;

import static ee.eesti.riha.rest.logic.util.DateHelper.DATE_FORMAT_IN_JSON;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import ee.eesti.riha.rest.dao.GenericDAO;
import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.dao.NamesDAO;
import ee.eesti.riha.rest.dao.SecureApiGenericDAO;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.PartialError;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.TableEntryCreateLogic.JsonParseData;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.PathHolder;
import ee.eesti.riha.rest.logic.util.QueryHolder;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.readonly.Kind;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// TODO: Auto-generated Javadoc
/**
 * Responsible of actual processing, communicating with lower level DAO services for read/change data from/to database
 * and finally returning results in agreed format.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
@Component
public class ChangeLogic<T, K> {
  private static final Logger LOG = LoggerFactory.getLogger(ChangeLogic.class);

  @Autowired
  SecureApiGenericDAO<T, K> secureDAO;

  @Autowired
  GenericDAO<T> noLogicDAO;

  @Autowired
  NamesDAO namesDAO;

  @Autowired
  TableEntryCreateLogic tableEntryCreateLogic;

  @Autowired
  TableEntryReadLogic tableEntryReadLogic;

  @Autowired
  TableEntryUpdateLogic tableEntryUpdateLogic;

  @Autowired
  KindRepository kindRepository;

  /**
   * Do get many.
   *
   * @param classRepresentingTable the class representing table
   * @param limit the limit
   * @param offset the offset
   * @param filterComponents the filter components
   * @param sort the sort
   * @param fields the fields
   * @return the list
   * @throws RihaRestException the riha rest exception
   */
  public List<T> doGetMany(Class<T> classRepresentingTable, Integer limit, Integer offset,
                           List<FilterComponent> filterComponents, String sort, String fields) throws RihaRestException {
    List<T> all;
    try {
      addEscapedQuotesToJsonArray(filterComponents);
      all = secureDAO.find(classRepresentingTable, limit, offset, filterComponents, sort);
    } catch (RihaRestException e) {
      throw e;
    } catch (Exception e) {
      throw new RihaRestException(MyExceptionHandler.unmapped(e));
    }

    return all;
  }

  /**
   * Adds the escaped quotes to json array.
   *
   * @param fc the fc
   */
  // [[test_array,?&,[dfg]]] -> [[test_array,?&,[\"dfg\"]]]
  private void addEscapedQuotesToJsonArray(FilterComponent fc) {
    if (fc.getOperator().equals("?&")) {
      // operator expects array as argument
      String array = StringHelper.escapeStringsInArray(fc.getOperandRight());
      fc.setOperandRight(array);
    }
  }

  /**
   * Adds the escaped quotes to json array.
   *
   * @param fcList the fc list
   */
  private void addEscapedQuotesToJsonArray(List<FilterComponent> fcList) {
    if (fcList != null) {
      for (FilterComponent fc : fcList) {
        addEscapedQuotesToJsonArray(fc);
      }
    }
  }

  /**
   * Validate json array.
   *
   * @param jsonArray the json array
   * @return the string
   */
  private String validateJsonArray(String jsonArray) {
    if (jsonArray != null && jsonArray.length() > 0) {
      try {
        if (JsonHelper.isJsonArray(jsonArray)) {
          return jsonArray;
        }
      } catch (IllegalArgumentException iae) {
        jsonArray = "[" + jsonArray + "]";
        if (JsonHelper.isJsonArray(jsonArray)) {
          return jsonArray;
        }
      }
    }
    throw new IllegalArgumentException("Not valid JSON array " + jsonArray);
  }

  /**
   * Show only those element fields that are in fields JSON array.
   *
   * @param all elements whose fields will be shown
   * @param fields JSON array of fields that must be shown as String
   * @return List of elements, that show only fields, that are in fields JSON array
   * @throws RihaRestException the riha rest exception
   */
  private List<T> filterByFields(List<T> all, String fields) throws RihaRestException {
    if (fields == null || fields.isEmpty()) {
      all = tableEntryReadLogic.getAdjustedObjsBasedOnExpectedJson(all);
    } else {
      try {
        fields = validateJsonArray(fields);
        JsonArray fieldsJson = JsonHelper.GSON.fromJson(fields, JsonArray.class);
        all = tableEntryReadLogic.getAdjustedObjsBasedOnExpectedJson(all, fieldsJson);
      } catch (IllegalArgumentException | JsonSyntaxException jse) {
        throw new RihaRestException(createRestError(jse, fields));
      }
    }

    return all;
  }

  /**
   * Like {@link ChangeLogic#filterByFields(List, String) }, but single item instead of list.
   *
   * @param data the data
   * @param fields the fields
   * @return the t
   * @throws RihaRestException the riha rest exception
   */
  private T filterByFields(T data, String fields) throws RihaRestException {
    if (fields == null || fields.isEmpty()) {
      data = (T) tableEntryReadLogic.getAdjustedObjBasedOnExpectedJson(data);
    } else {
      try {
        fields = validateJsonArray(fields);
        JsonArray fieldsJson = JsonHelper.GSON.fromJson(fields, JsonArray.class);
        data = (T) tableEntryReadLogic.getAdjustedObjBasedOnExpectedJson(data, fieldsJson);
      } catch (IllegalArgumentException | JsonSyntaxException jse) {
        throw new RihaRestException(createRestError(jse, fields));
      }
    }
    return data;
  }

  private RihaRestError createRestError(Exception jse, String fields) {
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.INPUT_JSON_NOT_VALID_JSON);
    error.setErrmsg(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG + " fields: " + fields);
    error.setErrtrace(jse.getMessage());

    return error;
  }

  /**
   * Get row by main_resource_id.
   *
   * @param classRepresentingTable the class representing table
   * @param id the id
   * @return the list
   * @throws RihaRestException the riha rest exception
   */
  public List<T> doGetByMainResourceId(Class<T> classRepresentingTable, Integer id)
      throws RihaRestException {
    List<T> entityList = secureDAO.findByMainResourceId(classRepresentingTable, id);

    return entityList;
  }

  /**
   * Get row by id.
   *
   * @param classRepresentingTable the class representing table
   * @param id the id
   * @param fields the fields
   * @return the t
   * @throws RihaRestException the riha rest exception
   */
  public T doGet(Class<T> classRepresentingTable, Integer id, String fields)
      throws RihaRestException {
    T entity = secureDAO.find(classRepresentingTable, id);
    Validator.noSuchIdInGivenTable(entity, id);

    return entity;
  }

  /**
   * Get all table rows filtered by offset and limit or element by id.
   *
   * @param queryHolder holds query parameters
   * @return found elements or an element
   * @throws RihaRestException the riha rest exception
   * @throws NumberFormatException the number format exception
   */
  public Object doGet(QueryHolder queryHolder) throws RihaRestException {
    PathHolder pathHolder = new PathHolder(queryHolder.getPath());
    Class<T> classRepresentingTable = Finals.getClassRepresentingTable(pathHolder.tableName);
    if (pathHolder.id == null) {
      List<T> result = doGetMany(classRepresentingTable, queryHolder.getLimit(), queryHolder.getOffset(),
          (List<FilterComponent>) queryHolder.getFilter(), queryHolder.getSort(), queryHolder.getFields());

      return result;
    } else {
      // find by id
      T item = doGet(classRepresentingTable, Integer.valueOf(pathHolder.id), queryHolder.getFields());
      Validator.noSuchIdInGivenTable(item, Integer.valueOf(pathHolder.id));
      return item;
    }

  }

  /**
   * Get count of all table rows filtered by offset and limit as map {"ok":1234}.
   *
   * @param queryHolder holds query parameters
   * @return found elements or an element
   * @throws RihaRestException the riha rest exception
   * @throws NumberFormatException the number format exception
   */
  public Map<String, Integer> doCount(QueryHolder queryHolder) throws RihaRestException {
    PathHolder pathHolder = new PathHolder(queryHolder.getPath());
    Map<String, Integer> resultCount = new HashMap<>();
    Class<T> classRepresentingTable = Finals.getClassRepresentingTable(pathHolder.tableName);
    Integer count;
    if (pathHolder.id == null) {
      // no id means multiple
      LOG.info("COMPARE " + queryHolder.getLimit() + " == Finals -1");
      if (queryHolder.getLimit() == Finals.COUNT_ALL_LIMIT) {
        // count = genericDAO.findCount(classRepresentingTable);
        count = secureDAO.findCount(classRepresentingTable);
      } else {
        // needed for ?& operator
        addEscapedQuotesToJsonArray((List<FilterComponent>) queryHolder.getFilter());
        // count = genericDAO.findCount(classRepresentingTable, queryHolder.getLimit(), queryHolder.getOffset(),
        // (List<FilterComponent>) queryHolder.getFilter(), queryHolder.getSort());
        count = secureDAO.findCount(classRepresentingTable, queryHolder.getLimit(), queryHolder.getOffset(),
            (List<FilterComponent>) queryHolder.getFilter(), queryHolder.getSort());
      }
    } else {
      // find by id
      T item = doGet(classRepresentingTable, Integer.valueOf(pathHolder.id), queryHolder.getFields());
      if (item == null) {
        count = 0;
      } else {
        count = 1;
      }

    }

    resultCount.put(Finals.OK, count);
    return resultCount;
  }

  /**
   * Insert rows to table if json is array or row if json is object.
   *
   * @param json the json
   * @param classRepresentingTable the class representing table
   * @return list of created primary keys
   * @throws RihaRestException the riha rest exception
   */
  public List<K> doCreate(String json, Class<T> classRepresentingTable) throws RihaRestException {

    List<K> createdKeys = new ArrayList<>();

    Validator.needsToBeValidJson(json);
    Validator.jsonCantBeEmpty(json);

    if (JsonHelper.isJsonArray(json)) {

      LOG.info("CREATE MULTIPLE JSON ARRAY");
      // expect every json to be parsed as success or error
      List<JsonParseData> jsonMappedToParseResult = tableEntryCreateLogic.parseEveryJsonInArrayToObjOfDestinationClass(
          json, classRepresentingTable);
      List<JsonParseData> jsonToParsedOKObjList = new ArrayList<>();
      List<JsonParseData> jsonToErrorList = new ArrayList<>();
      // separate error and success data
      for (JsonParseData parseData : jsonMappedToParseResult) {
        if (parseData.getResult() instanceof RihaRestError) {
          // json and its error
          jsonToErrorList.add(parseData);
        } else {
          // json and its parsed object representation
          jsonToParsedOKObjList.add(parseData);
        }
      }

      // for successfully parse objects try to create entries in db
      for (JsonParseData parseDate : jsonToParsedOKObjList) {
        try {
          T item = (T) parseDate.getResult();
          List<K> createdKey = secureDAO.create(item);
          createdKeys.add(createdKey.get(0));
        } catch (Exception e) {
          // if unsuccessful, then store error
          RihaRestError error = MyExceptionHandler.unmapped(e, parseDate.getJson());
          // store json and exception causing error
          jsonToErrorList.add(tableEntryCreateLogic.new JsonParseData(parseDate.getJson(), error));
        }
        // continue with next item
      }

      // if any error, then return results as exception and partial error
      // object
      if (jsonToErrorList.size() > 0) {
        // collect error objects into an array
        List<T> errors = new ArrayList<>();
        for (JsonParseData parseData : jsonToErrorList) {
          errors.add((T) parseData.getResult());
        }

        PartialError partialError = new PartialError();
        partialError.setSuccessData(createdKeys);
        partialError.setErrors((List<RihaRestError>) errors);
        throw new RihaRestException(partialError);
      }

      return createdKeys;

    } else {
      T item = (T) tableEntryCreateLogic.jsonToObjOfType(json, classRepresentingTable);
      if (item instanceof RihaRestError) {
        throw new RihaRestException(item);
      }

      try {
        createdKeys = secureDAO.create(item);
      } catch (RihaRestException e) {
        throw e;
      } catch (Exception e) {

        RihaRestError error = MyExceptionHandler.unmapped(e, json);
        error.setErrmsg(error.getErrmsg() + " Salvestamisega tekkis probleem ");
        throw new RihaRestException(error);
      }

      return createdKeys;
    }
  }

  /**
   * Update rows in table with not null values from json.
   *
   * @param json object or array
   * @param classRepresentingTable the class representing table
   * @param id primary key used only if is not array
   * @param idFieldName name of field by which the updates will be made, only used is is array
   * @return number of changed rows
   * @throws ReflectiveOperationException the reflective operation exception
   * @throws RihaRestException the riha rest exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Integer> doUpdate(String json, Class<T> classRepresentingTable, Integer id, String idFieldName) throws ReflectiveOperationException, RihaRestException, IOException {

    int numOfChanged = 0;
    LOG.info("JSON " + json);

    String dtJsonFormat = new SimpleDateFormat(DATE_FORMAT_IN_JSON).format(new Date());

    if (JsonHelper.isJsonArray(json)) {

      // add modifier and date to json objects
      JsonArray jsonArray = (JsonArray) JsonParser.parseString(json);
      for (int i = 0; i < jsonArray.size(); i++) {
        JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
        addModificationDate(jsonObject, dtJsonFormat);

        if (classRepresentingTable != Comment.class) {
          // replace kind with kind_id
          replaceKindWithKindId(jsonObject);
        }
      }

      // create items from modified json
      Class arrayClass = Class.forName("[L" + classRepresentingTable.getCanonicalName() + ";");
      T[] items = (T[]) JsonHelper.GSON.fromJson(jsonArray, arrayClass);
      LOG.info(JsonHelper.GSON.toJson(items));
      List<T> itemList = new ArrayList<>(Arrays.asList(items));

      try {
        if (StringHelper.areEqual(idFieldName, Finals.KIND) && classRepresentingTable != Comment.class) {
          // kind doesn't exist in database, must be replaced
          idFieldName = Finals.KIND_ID;
        }
        // numOfChanged = genericDAO.update(itemList, idFieldName);
        numOfChanged = secureDAO.update(itemList, idFieldName);

        // throws exception if numOfChanged has error code as result
        Validator.fieldMustExistInDatabase(numOfChanged, idFieldName);
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
        LOG.error("Error while updating", e);
      }

    } else if (id != null) {

      JsonObject jsonObject = JsonHelper.getFromJson(json);
      addModificationDate(jsonObject, dtJsonFormat);

      if (classRepresentingTable != Comment.class) {
        replaceKindWithKindId(jsonObject);
      }

      // create from modified
      T item = JsonHelper.GSON.fromJson(jsonObject, classRepresentingTable);
      // id needed to create file path
      ((BaseModel) item).callSetId(id);

      numOfChanged = secureDAO.update(item, id);
    } else {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.UPDATE_ID_MISSING);
      error.setErrmsg(ErrorCodes.UPDATE_ID_MISSING_MSG);
      throw new RihaRestException(error);
    }

    Map<String, Integer> updatedResult = new HashMap<>();
    updatedResult.put(Finals.OK, numOfChanged);
    return updatedResult;
  }

  /**
   * Adds modification date.
   *
   * @param asJsonObject the as json object
   * @param dateJson the date json
   */
  private void addModificationDate(JsonObject asJsonObject, String dateJson) {
    asJsonObject.add(Finals.MODIFIED_DATE, new JsonPrimitive(dateJson));
    LOG.info("ADDED MODIFIED DATE TO JSON OBJECT");
  }

  /**
   * Replace kind with kind id.
   *
   * @param jsonObject the json object
   */
  protected void replaceKindWithKindId(JsonObject jsonObject) {
    if (jsonObject.has("kind")) {
      String kindName = jsonObject.get("kind").getAsString();
      Kind kind = kindRepository.getByName(kindName);
      if (kind == null) {
        throw new IllegalArgumentException("No kind exists with name: " + kindName);
      }
      jsonObject.addProperty("kind_id", kind.getKind_id());
      jsonObject.add("kind", JsonNull.INSTANCE);
    }
  }

  /**
   * Do update.
   *
   * @param queryHolder the query holder
   * @return the map
   * @throws ReflectiveOperationException the reflective operation exception
   * @throws RihaRestException the riha rest exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Integer> doUpdate(QueryHolder queryHolder) throws ReflectiveOperationException,
      RihaRestException, IOException {

    PathHolder pathHolder = new PathHolder(queryHolder.getPath());
    Class<T> classRepresentingTable = Finals.getClassRepresentingTable(pathHolder.tableName);

    Set<Entry<String, JsonElement>> entrySet = queryHolder.getAsJson().getAsJsonObject().entrySet();

    String idFieldName = getIdFieldName(entrySet);

    Integer id = pathHolder.id != null ? Integer.valueOf(pathHolder.id) : null;
    String dataJson = queryHolder.getData().toString();
    Map<String, Integer> updatedResult = doUpdate(dataJson, classRepresentingTable, id, idFieldName);
    return updatedResult;
  }

  /**
   * Delete rows.
   *
   * @param queryHolder holds query parameters
   * @return number of deleted rows
   * @throws RihaRestException the riha rest exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Integer> doDelete(QueryHolder queryHolder) throws RihaRestException,
      IOException {
    LOG.info("DO DELETE QUERYHOLDER");
    PathHolder pathHolder = new PathHolder(queryHolder.getPath());
    Map<String, Integer> deletedResult = null;

    if (queryHolder.getData() != null) {

      throw new IllegalArgumentException("JSON data field must be empty!");

    } else if (pathHolder.id != null && !pathHolder.id.isEmpty()) {

      deletedResult = doDelete(pathHolder.tableName, Integer.valueOf(pathHolder.id));

    } else {

      deletedResult = deleteByField(queryHolder, pathHolder);

    }

    return deletedResult;
  }

  /**
   * Helper method to delete by field if the field name is not reserved for other query parameters.
   *
   * @param queryHolder the query holder
   * @param pathHolder the path holder
   * @return the map
   * @throws RihaRestException the riha rest exception
   */
  private Map<String, Integer> deleteByField(QueryHolder queryHolder, PathHolder pathHolder)
      throws RihaRestException {
    Map<String, Integer> deletedResult = null;
    Set<Entry<String, JsonElement>> entrySet = queryHolder.getAsJson().getAsJsonObject().entrySet();
    boolean keyFound = false;
    for (Entry<String, JsonElement> entry : entrySet) {
      if (!StringHelper.contains(entry.getKey(), Finals.KNOWN_PARAMETERS)) {
        // entry.getKey() is the field name
        try {
          deletedResult = doDelete(pathHolder.tableName, entry.getKey(), entry.getValue());
        } catch (RihaRestException e) {
          Object err = e.getError();
          if (err instanceof RihaRestError) {
            RihaRestError rrErr = (RihaRestError) err;
            rrErr.setErrtrace(queryHolder.getAsJson().toString());
          }
          throw e;
        }
        keyFound = true;
        break;
      }
    }

    if (!keyFound) {
      String requiredInfo = "kustutatava kirje id";
      Validator.jsonGeneralSomethingMissing(queryHolder.getAsJson().toString(), requiredInfo);
    }

    LOG.info("END " + entrySet);
    return deletedResult;
  }

  /**
   * Do delete.
   *
   * @param tableName the table name
   * @param key the key
   * @param value the value
   * @return the map
   * @throws RihaRestException the riha rest exception
   */
  private Map<String, Integer> doDelete(String tableName, String key, JsonElement value)
      throws RihaRestException {
    LOG.info("DO Delete");
    LOG.info("" + value);

    if (value.isJsonArray()) {

      Object[] values = parseJsonArray(value);

      // replace kinds with kind_ids
      if (key.equals(Finals.KIND) && !StringHelper.areEqual(Comment.class.getSimpleName(), tableName)) {
        Integer[] integerValues = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
          String kindName = (String) values[i];
          Kind kind = kindRepository.getByName(kindName);
          // values[i] = kind.getKind_id();
          integerValues[i] = kind.getKind_id();
        }
        values = integerValues;
        key = Finals.KIND_ID;
      }

      int numOfDeleted = 0;
      try {
        // numOfDeleted = genericDAO.delete(tableName, key, values);
        numOfDeleted = secureDAO.delete(tableName, key, values);

        Validator.fieldMustExistInDatabase(numOfDeleted, key);
      } catch (SQLGrammarException e) {
        // e.printStackTrace();
        if (e.getCause() != null) {
          // should we show more specific error?
          String causeText = e.getCause().toString();
          if (causeText != null
              && causeText.toLowerCase().contains("column \"" + key.toLowerCase() + "\" does not exist")) {

            RihaRestError error = new RihaRestError();
            error.setErrcode(ErrorCodes.INPUT_CAN_NOT_FIND_COLUMN);
            error.setErrmsg(ErrorCodes.INPUT_CAN_NOT_FIND_COLUMN_MSG + key);
            throw new RihaRestException(error);
          }
        }
      }

      Map<String, Integer> deletedResult = new HashMap<>();

      deletedResult.put(Finals.OK, numOfDeleted);
      return deletedResult;
    } else {
      throw new IllegalArgumentException("parameter value must be JSONArray");
    }

  }

  /**
   * Do delete.
   *
   * @param tableName the table name
   * @param id the id
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Map<String, Integer> doDelete(String tableName, Integer id) throws IOException,
      RihaRestException {
    Class<T> clazz = Finals.getClassRepresentingTable(tableName);
    int numOfDeleted = secureDAO.delete(clazz, id);

    Map<String, Integer> deletedResult = new HashMap<>();
    deletedResult.put(Finals.OK, numOfDeleted);
    LOG.info("" + deletedResult);
    return deletedResult;
  }

  // ANY SUPPORTING AND HELPER METHODS BELOW

  /**
   * Gets the id field name.
   *
   * @param entrySet the entry set
   * @return the id field name
   */
  private String getIdFieldName(Set<Entry<String, JsonElement>> entrySet) {
    String idFieldName = Finals.NAME;
    for (Entry<String, JsonElement> entry : entrySet) {
      LOG.info("" + entry);
      if (entry.getKey().equals(Finals.KEY)) {
        idFieldName = entry.getValue().getAsString();
      }
    }
    return idFieldName;

  }

  /**
   * Parses the json array.
   *
   * @param value the value
   * @return generic type
   */
  private T[] parseJsonArray(JsonElement value) {

    Object[] values;
    if (value.toString().contains("\"")) {
      values = JsonHelper.GSON.fromJson(value, String[].class);
    } else {
      values = JsonHelper.GSON.fromJson(value, Integer[].class);
    }
    return (T[]) values;

  }

  /**
   * Find corresponding names for organizations and persons by given code and main_resource items by uri e.g <br>
   * Request {"organizations":["12345", "55511"], "persons":["38801011234", "60101011235"]} <br>
   * Response {"organizations":{"12345":"Asutus A", "55511":"Asutus X"}, "persons":{"38801011234":"Jaan Tamm",
   * "60101011235":"Mari Mets"}}
   *
   * @param queryHolder the query holder
   * @return the map
   */
  public Map<String, Map<String, String>> doGetNames(QueryHolder queryHolder) {
    Map<String, Map<String, String>> names = new HashMap<>();
    JsonObject queryObject = queryHolder.getAsJson().getAsJsonObject();
    JsonArray organizations = (JsonArray) queryObject.get("organizations");
    JsonArray persons = (JsonArray) queryObject.get("persons");
    JsonArray uris = (JsonArray) queryObject.get("uris");
    JsonArray ids = (JsonArray) queryObject.get("ids");

    names.put("organizations", getOrganizationNames(organizations));
    names.put("persons", getPersonNames(persons));
    names.put("uris", getUriNames(uris));
    names.put("ids", getIdNames(ids));

    return names;
  }

  /**
   * Gets the organization names.
   *
   * @param organizations the organizations
   * @return the organization names
   */
  private Map<String, String> getOrganizationNames(JsonArray organizations) {
    if (organizations == null || organizations.size() == 0) {
      return Collections.EMPTY_MAP;
    }
    return namesDAO.getOrganizationNames(JsonHelper.jsonToStringList(organizations));
  }

  /**
   * Gets the person names.
   *
   * @param persons the persons
   * @return the person names
   */
  private Map<String, String> getPersonNames(JsonArray persons) {
    if (persons == null || persons.size() == 0) {
      return Collections.EMPTY_MAP;
    }
    return namesDAO.getPersonNames(JsonHelper.jsonToStringList(persons));
  }

  /**
   * Gets the uri names.
   *
   * @param uris the uris
   * @return the uri names
   */
  private Map<String, String> getUriNames(JsonArray uris) {
    if (uris == null || uris.size() == 0) {
      return Collections.EMPTY_MAP;
    }
    return namesDAO.getUriNames(JsonHelper.jsonToStringList(uris));
  }

  /**
   * Gets the ID names.
   *
   * @param ids the IDs
   * @return the ID names
   */
  private Map<String, String> getIdNames(JsonArray ids) {
    if (ids == null || ids.size() == 0) {
      return Collections.EMPTY_MAP;
    }
    return namesDAO.getIdNames(JsonHelper.jsonToStringList(ids));
  }

}
