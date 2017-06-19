package ee.eesti.riha.rest.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ee.eesti.riha.rest.dao.ApiGenericDAOImpl;
import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.model.readonly.Kind;

// TODO: Auto-generated Javadoc
/**
 * The Class Validator.
 */
public class Validator extends Exception {

  private static final long serialVersionUID = -4780671967978509000L;

  private static final Logger LOG = LoggerFactory.getLogger(Validator.class);

  /**
   * Unknown table requested.
   *
   * @param <T> the generic type
   * @param tableName the table name
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void unknownTableRequested(String tableName) throws RihaRestException {

    Class<T> classRepresentingTable = Finals.getClassRepresentingTable(tableName);
    if (classRepresentingTable == null) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED);
      error.setErrmsg(ErrorCodes.INPUT_UNKNOWN_OBJECT_TYPE_REQUESTED_MSG);
      error.setErrtrace(tableName);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Table cant be modified.
   *
   * @param <T> the generic type
   * @param tableName the table name
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void tableCantBeModified(String tableName) throws RihaRestException {

    Class<T> classRepresentingTable = Finals.getClassRepresentingTableReadOnly(tableName);
    if (classRepresentingTable != null) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.TABLE_CANT_BE_MODIFIED);
      error.setErrmsg(ErrorCodes.TABLE_CANT_BE_MODIFIED_MSG);
      error.setErrtrace(tableName);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Validate table name for full table.
   *
   * @param tableName the table name
   * @param tablesMap the tables map
   * @throws RihaRestException the riha rest exception
   */
  public static void validateTableNameForFullTable(String tableName, Map<String, Class> tablesMap)
      throws RihaRestException {

    if (tableName == null || !tablesMap.containsKey(tableName)) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.WRONG_TABLE_FULL_SERVICE);
      error.setErrmsg(ErrorCodes.WRONG_TABLE_FULL_SERVICE_MSG);
      error.setErrtrace("" + tableName);
      throw new RihaRestException(error);
    }

  }

  /**
   * No such id in given table.
   *
   * @param <T> the generic type
   * @param entities the entities
   * @param idRequested the id requested
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void noSuchIdInGivenTable(T entities, Integer idRequested) throws RihaRestException {

    if (entities == null) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID);
      error.setErrmsg(ErrorCodes.INPUT_NO_OBJECT_FOUND_WITH_GIVEN_ID_MSG);
      error.setErrtrace(idRequested.toString());
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Needs to be valid json.
   *
   * @param json the json
   * @throws RihaRestException the riha rest exception
   */
  public static void needsToBeValidJson(String json) throws RihaRestException {

    if (!JsonHelper.isValidJson(json)) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_JSON_NOT_VALID_JSON);
      error.setErrmsg(ErrorCodes.INPUT_JSON_NOT_VALID_JSON_MSG);
      error.setErrtrace(json);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Json cant be empty.
   *
   * @param json the json
   * @throws RihaRestException the riha rest exception
   */
  public static void jsonCantBeEmpty(String json) throws RihaRestException {

    if (json.isEmpty()) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_JSON_MISSING);
      error.setErrmsg(ErrorCodes.INPUT_JSON_MISSING_MSG);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Json general something missing.
   *
   * @param json the json
   * @param whatIsMissing the what is missing
   * @throws RihaRestException the riha rest exception
   */
  public static void jsonGeneralSomethingMissing(String json, String whatIsMissing) throws RihaRestException {

    // TODO move testing here, currently only puts together error message
    // and throws exception
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.INPUT_JSON_GENERAL_SOMETHING_MISSING);
    error.setErrmsg(ErrorCodes.INPUT_JSON_GENERAL_SOMETHING_MISSING_MSG + whatIsMissing);
    error.setErrtrace(json);
    RihaRestException e = new RihaRestException(error);
    throw e;

  }

  /**
   * Json cant be array.
   *
   * @param json the json
   * @throws RihaRestException the riha rest exception
   */
  public static void jsonCantBeArray(String json) throws RihaRestException {

    if (JsonHelper.isJsonArray(json)) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_JSON_ARRAY_RECEIVED_BUT_CAN_ACCEPT_SINGLE_JSON_OBJ_ONLY);
      error.setErrmsg(ErrorCodes.INPUT_JSON_ARRAY_RECEIVED_BUT_CAN_ACCEPT_SINGLE_JSON_OBJ_ONLY_MSG);
      error.setErrtrace(json);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Value must be allowed.
   *
   * @param allowedValues the allowed values
   * @param value the value
   * @param errorCode the error code
   * @param errorMsg the error msg
   * @throws RihaRestException the riha rest exception
   */
  public static void valueMustBeAllowed(List<String> allowedValues, String value, int errorCode, String errorMsg)
      throws RihaRestException {

    if (!allowedValues.contains(value.toLowerCase())) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(errorCode);
      error.setErrmsg(errorMsg);
      error.setErrtrace(value);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Cant have missin req pars in url.
   *
   * @param reqPars the req pars
   * @param urlPropToVal the url prop to val
   * @throws RihaRestException the riha rest exception
   */
  public static void cantHaveMissinReqParsInURL(String[] reqPars, Map<String, Object> urlPropToVal)
      throws RihaRestException {

    List<String> missingRequiredPars = Validator.collectMissingProperties(reqPars, urlPropToVal);

    if (missingRequiredPars.size() > 0) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING);
      error.setErrmsg(ErrorCodes.INPUT_URL_REQUIRED_ATTRIBUTES_MISSING_MSG
          + StringUtils.join(missingRequiredPars, ", "));
      // error.setErrtrace();
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Cant have missing req pars in json.
   *
   * @param reqPars the req pars
   * @param json the json
   * @throws RihaRestException the riha rest exception
   */
  public static void cantHaveMissingReqParsInJson(String[] reqPars, String json) throws RihaRestException {

    List<String> missingRequiredPars = Validator.collectMissingProperties(reqPars, json);
    if (missingRequiredPars.size() > 0) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING);
      error.setErrmsg(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG
          + StringUtils.join(missingRequiredPars, ", "));
      error.setErrtrace(json);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Count of filter items needs to be correct.
   *
   * @param filterItems i.e. ["name","=","testN1","age",">","20"]
   * @param traceData the trace data
   * @throws RihaRestException the riha rest exception
   */
  public static void countOfFilterItemsNeedsToBeCorrect(String[] filterItems, String traceData)
      throws RihaRestException {

    // expect exactly group of 3
    if (filterItems.length % Finals.NUM_OF_FILTER_ITEMS != 0) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_FILTER_MUST_HAVE_3_ITEMS_PER_GROUP);
      error.setErrmsg(ErrorCodes.INPUT_FILTER_MUST_HAVE_3_ITEMS_PER_GROUP_MSG);
      error.setErrtrace(traceData);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Field must be part of model, or it must exist in json_content field in database.
   *
   * @param resultOrError Query result or error code
   * @param fieldName the field name
   * @throws RihaRestException the riha rest exception
   */
  public static void fieldMustExistInDatabase(int resultOrError, String fieldName) throws RihaRestException {
    if (resultOrError == ApiGenericDAOImpl.NOT_PART_OF_MODEL_OR_JSON) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_CAN_NOT_FIND_COLUMN);
      error.setErrmsg(ErrorCodes.INPUT_CAN_NOT_FIND_COLUMN_MSG + fieldName);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }
  }

  /**
   * Cant update version here.
   *
   * @param <T> the generic type
   * @param old the old
   * @param update the update
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void cantUpdateVersionHere(T old, T update) throws RihaRestException {
    if (isVersionUpdated(old, update)) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.CAN_UPDATE_VERSION_HERE);
      error.setErrmsg(ErrorCodes.CAN_UPDATE_VERSION_HERE_MSG);
      throw new RihaRestException(error);
    }
  }

  /**
   * No such uri in given table.
   *
   * @param <T> the generic type
   * @param items the items
   * @param uri the uri
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void noSuchUriInGivenTable(List<T> items, String uri) throws RihaRestException {

    if (items == null || items.isEmpty()) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.NO_ITEM_WITH_URI_FOUND);
      error.setErrmsg(ErrorCodes.NO_ITEM_WITH_URI_FOUND_MSG);
      error.setErrtrace(uri);
      RihaRestException e = new RihaRestException(error);
      throw e;
    }

  }

  /**
   * Version must be different. Must not be used before with given uri
   *
   * @param <T> the generic type
   * @param item the item
   * @param newVersion the new version
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void versionMustBeDifferent(List<T> itemsWithSameVersion, String uri, String newVersion,
      JsonObject query) throws RihaRestException {

    if (itemsWithSameVersion.size() > 0) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.VERSION_MUST_BE_UPDATED);
      error.setErrmsg(ErrorCodes.VERSION_MUST_BE_UPDATED_MSG + uri + " - " + newVersion);
      error.setErrtrace(query.toString());
      throw new RihaRestException(error);
    }

  }

  /**
   * Cant update archived element.
   *
   * @param <T> the generic type
   * @param old the old
   * @throws RihaRestException the riha rest exception
   */
  public static <T> void cantUpdateArchivedElement(T old) throws RihaRestException {
    LOG.info("CANT UPDATE ARCHIVED:: " + old);
    BaseModel bm = (BaseModel) old;
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.CANT_UPDATE_ARCHIVED);
    error.setErrmsg(ErrorCodes.CANT_UPDATE_ARCHIVED_MSG);
    error.setErrtrace(bm.callGetId() + " " + bm.getUri());
    long time = new Date().getTime();

    if (old.getClass() == Main_resource.class) {
      Main_resource mrOld = (Main_resource) old;
      if (mrOld.getEnd_date() != null && mrOld.getEnd_date().getTime() <= time) {
        throw new RihaRestException(error);
      }
    } else if (old.getClass() == Data_object.class) {
      Data_object mrOld = (Data_object) old;
      if (mrOld.getEnd_date() != null && mrOld.getEnd_date().getTime() <= time) {
        throw new RihaRestException(error);
      }
    } else if (old.getClass() == Document.class) {
      Document mrOld = (Document) old;
      if (mrOld.getEnd_date() != null && mrOld.getEnd_date().getTime() <= time) {
        throw new RihaRestException(error);
      }
    }

  }

  /**
   * Document must have main_resource_id or data_object_id not null
   * 
   * @param item
   * @throws RihaRestException
   */
  public static <T> void documentMustHaveReference(T item) throws RihaRestException {
    if (item.getClass() == Document.class) {
      Document doc = (Document) item;
      if (doc.getData_object_id() == null && doc.getMain_resource_id() == null) {
        // throw error
        RihaRestError error = new RihaRestError();
        error.setErrcode(ErrorCodes.DOCUMENT_CREATE_HAS_NO_REF);
        error.setErrmsg(ErrorCodes.DOCUMENT_CREATE_HAS_NO_REF_MSG);
        error.setErrtrace(doc.getJson_content().toString());
        throw new RihaRestException(error);
      }
    }
  }

  /**
   * Document must exist.
   *
   * @param file the file
   * @param documentId the document id
   * @throws RihaRestException the riha rest exception
   */
  public static void documentFileMustExist(File file, Integer documentId) throws RihaRestException {
    if (!file.exists()) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.DOCUMENT_FILE_NOT_FOUND);
      error.setErrmsg(ErrorCodes.DOCUMENT_FILE_NOT_FOUND_MSG);
      error.setErrtrace("Document id: "  + documentId);
      throw new RihaRestException(error);
    }
  }
  
  /**
   * Collect missing properties.
   *
   * @param reqPars the req pars
   * @param parsToVal the pars to val
   * @return the list
   */
  private static List<String> collectMissingProperties(String[] reqPars, Map<String, Object> parsToVal) {

    List<String> missingPars = new ArrayList<>();

    for (int i = 0; i < reqPars.length; i++) {
      if (!parsToVal.containsKey(reqPars[i]) || parsToVal.get(reqPars[i]) == null) {
        missingPars.add(reqPars[i]);
      }
    }
    return missingPars;

  }

  /**
   * Collect missing properties.
   *
   * @param reqPars the req pars
   * @param json the json
   * @return the list
   */
  private static List<String> collectMissingProperties(String[] reqPars, String json) {

    JsonObject jsonAsObj = JsonHelper.getFromJson(json);
    List<String> missingPars = new ArrayList<>();

    for (int i = 0; i < reqPars.length; i++) {
      if (!jsonAsObj.has(reqPars[i])) {
        missingPars.add(reqPars[i]);
      }
    }
    return missingPars;

  }

  /**
   * Checks if is version updated.
   *
   * @param <T> the generic type
   * @param old the old
   * @param update the update
   * @return true, if is version updated
   */
  private static <T> boolean isVersionUpdated(T old, T update) {
    if (old.getClass() == Main_resource.class && update.getClass() == Main_resource.class) {
      Main_resource mrOld = (Main_resource) old;
      Main_resource mrUpdate = (Main_resource) update;
      if (mrOld.getVersion() != null && mrUpdate.getVersion() != null) {
        LOG.info("VERSION :: old " + mrOld.getVersion() + " - new " + mrUpdate.getVersion());
        return !mrOld.getVersion().equals(mrUpdate.getVersion());
      }
    }
    return false;
  }

}
