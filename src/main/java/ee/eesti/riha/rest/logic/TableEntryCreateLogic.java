package ee.eesti.riha.rest.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.eesti.riha.rest.model.Main_resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ee.eesti.riha.rest.dao.GenericDAO;
import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.dao.UtilitiesDAO;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.logic.util.JsonContentBasedTable;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.readonly.Kind;

// TODO: Auto-generated Javadoc
/**
 * Gather any supporting or helper methods here that are used to create new object/entry. Also some table specifics
 * related to creating new entry in that table.
 *
 * @param <T> the generic type
 */
@Component
public class TableEntryCreateLogic<T extends BaseModel> {

  @Autowired
  UtilitiesDAO<T> utilitiesDAO;

  @Autowired
  GenericDAO<Kind> noLogicDAO;

  @Autowired
  KindRepository kindRepository;

  private static final Logger LOG = LoggerFactory.getLogger(TableEntryCreateLogic.class);

  /**
   * Parses every element into destination object type/table. Resulting array contains list of original json element
   * with their parse result (when successful in parse then object or when unsuccessfull then error).
   *
   * @param json array containting elements to be parsed
   * @param classRepresentingTable given object type/table to parse every json element
   * @return list containing objects that hold json element parsed with parse result
   * @throws RihaRestException the riha rest exception
   */
  public List<JsonParseData> parseEveryJsonInArrayToObjOfDestinationClass(String json, Class<T> classRepresentingTable) throws RihaRestException {

    List<JsonParseData> resultingParseResults = new ArrayList<>();

    if (JsonContentBasedTable.isJsonContentBasedTable(classRepresentingTable)) {

      List<JsonObject> fromJson = JsonHelper.GSON.fromJson(json, new TypeToken<List<JsonObject>>() {
      }.getType());
      for (JsonObject t : fromJson) {
        T item = jsonToObjOfType(t.toString(), classRepresentingTable);

        resultingParseResults.add(new JsonParseData(t.toString(), item));

      }
      return resultingParseResults;

    } else {

      System.out.println(" TODO FIXME constructObjsFromJson should cover other tables "
          + "beside json_content based tables as well");
      // currently there should not be other tables
      return null;
    }

  }

  /**
   * Json to obj of type.
   *
   * @param json the json
   * @param classRepresentingTable the class representing table
   * @return the t
   * @throws RihaRestException the riha rest exception
   */
  public T jsonToObjOfType(String json, Class<T> classRepresentingTable) throws RihaRestException {

    T fromJson = null;

    try {

      if (JsonContentBasedTable.isJsonContentBasedTable(classRepresentingTable)) {
        // here gather logic for one group of tables/objects; they all
        // expect json_content and from json_content table entry will be
        // created
        if (classRepresentingTable == Comment.class) {
          fromJson = fromJsonContentToObjForComment(json, classRepresentingTable);
        } else {
          fromJson = fromJsonContentToObj(json, classRepresentingTable);
        }
      } else {
        fromJson = fromJsonContentToObj(json, classRepresentingTable);
      }

    } catch (Exception e) {
      // all unexpected exceptions handle here
      RihaRestError error = MyExceptionHandler.unmapped(e, json);
      throw new RihaRestException(error);
    }

    return fromJson;

  }

  /**
   * Having json_content received. We must create from it table entry, containing field json_content and fill other
   * fields in that entry that we can find from json_content. PS! This method applies to all similar tables with
   * json_content which have kind provided. Use only for tables that are alike with those that this method is already
   * used with. PS! Does not create entry to database. Only construct new object ready to be stored in database.
   * 
   * Recognizes tables: Main_resource, Document, Data_object, Comment (FIXME: need to generalize this)
   *
   * @param json the json
   * @param classRepresentingTable class representing json content based table
   * @return the t
   */
  public T fromJsonContentToObj(String json, Class<T> classRepresentingTable) {

    // expect json_content
    JsonObject jsonContent = JsonHelper.getFromJson(json);

    Integer pkId = utilitiesDAO.getNextSeqValForPKForTable(classRepresentingTable);

    return fromJsonToObjHelper(jsonContent, pkId, classRepresentingTable);
  }

  /**
   * Like {@link #fromJsonContentToObj(String, Class)} but is special for Comment to use kind instead of
   * kind_id, because Comment does not have kind_id field at this point.
   *
   * @param json the json
   * @param classRepresentingTable the class representing table
   * @return the t
   */
  public T fromJsonContentToObjForComment(String json, Class<T> classRepresentingTable) {

    // expect json_content
    JsonObject jsonContent = JsonHelper.getFromJson(json);

    String[] reqPars = {"kind" };
    List<String> missingRequiredPars = collectMissingProperties(reqPars, jsonContent);
    if (missingRequiredPars.size() > 0) {
      return (T) requiredParsMissing(missingRequiredPars, json);
    }

    Integer pkId = utilitiesDAO.getNextSeqValForPKForTable(classRepresentingTable);

    if (!jsonContent.has("uri") || jsonContent.get("uri").isJsonNull()) {
      String generatedUri = URI.constructUri(jsonContent.get("kind").getAsString(), pkId);
      // jsonContent must also contain uri
      jsonContent.addProperty("uri", generatedUri);
    }

    return fromJsonToObjHelper(jsonContent, pkId, classRepresentingTable);

  }

  /**
   * Helper method for code reuse.
   *
   * @param jsonContent the json content
   * @param pkId the pk_id
   * @param classRepresentingTable the class representing table
   * @return the t
   */
  private T fromJsonToObjHelper(JsonObject jsonContent, Integer pkId, Class<T> classRepresentingTable) {
    Gson gson = JsonHelper.GSON;

    Date dt = new Date();
    String dtJsonFormat = DateHelper.FORMATTER.format(dt);
    LOG.info(dtJsonFormat);

    if (classRepresentingTable != Main_resource.class) {
      jsonContent.addProperty("creation_date", dtJsonFormat);
    }

    // save primary key to json_content as well
    String pkFieldName = createPKFieldName(classRepresentingTable);
    jsonContent.addProperty(pkFieldName, pkId);

    String adjustedJsonObj = gson.toJson(jsonContent);

    // Map json to fields of class that represents table
    T entity = gson.fromJson(adjustedJsonObj, classRepresentingTable);

    // Set required fields
    entity.callSetId(pkId);
    entity.setCreation_date(dt);

    return entity;
  }

  /**
   * Like {@link #fromJsonContentToObj(String, Class)}, but keeps existing information (e.g creation_date).
   * Needed for changeLogic.doNewVersion()
   *
   * @param json the json
   * @param classRepresentingTable the class representing table
   * @return the t
   */
  public T fromJsonContentToObjKeepExisting(String json, Class<T> classRepresentingTable)
      throws RihaRestException {

    // expect json_content

    T constructedJsonObj;
    JsonObject jsonContent = JsonHelper.getFromJson(json);

    // String[] reqPars = { "kind" };
    String[] reqPars = {"kind_id" };
    List<String> missingRequiredPars = collectMissingProperties(reqPars, jsonContent);
    if (missingRequiredPars.size() > 0) {
      throw new RihaRestException(requiredParsMissing(missingRequiredPars, json));
    }

    Integer pkId = utilitiesDAO.getNextSeqValForPKForTable(classRepresentingTable);

    Gson gson = JsonHelper.GSON;

    // save primary key to json_content as well
    String pkFieldName = createPKFieldName(classRepresentingTable);
    jsonContent.addProperty(pkFieldName, pkId);

    String adjustedJsonObj = gson.toJson(jsonContent);

    // let gson construct blindly the table entry fields by fields
    // it can find from provided json

    if (JsonContentBasedTable.isJsonContentBasedTable(classRepresentingTable)) {
      BaseModel objToCreate = (BaseModel) gson.fromJson(adjustedJsonObj, classRepresentingTable);
      // store json_content into objToCreate
      objToCreate.callSetId(pkId);
      objToCreate.setJson_content(jsonContent);
      constructedJsonObj = (T) objToCreate;
    } else {
      throw new IllegalArgumentException("can not convert " + classRepresentingTable + " with chosen method");
    }

    if (((BaseModel) constructedJsonObj).getJson_content() == null) {
      throw new IllegalArgumentException("JsonContent can't be null! JsonContent " + jsonContent + " Created object "
          + constructedJsonObj);
    }

    return constructedJsonObj;

  }

  /**
   * Collect missing properties.
   *
   * @param reqPars the req pars
   * @param jsonContent the json content
   * @return the list
   */
  private List<String> collectMissingProperties(String[] reqPars, JsonObject jsonContent) {

    List<String> missingPars = new ArrayList<>();

    for (int i = 0; i < reqPars.length; i++) {
      if (!jsonContent.has(reqPars[i])) {
        missingPars.add(reqPars[i]);
      }
    }

    return missingPars;

  }

  /**
   * Required pars missing.
   *
   * @param missingRequiredPars the missing required pars
   * @param json the json
   * @return the riha rest error
   */
  private RihaRestError requiredParsMissing(List<String> missingRequiredPars, String json) {
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING);
    error
        .setErrmsg(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG + StringUtils.join(missingRequiredPars, ", "));
    error.setErrtrace(json);
    return error;
  }

  /**
   * Creates the pk field name.
   *
   * @param <T> the generic type
   * @param classRepresentingTable the class representing table
   * @return the string
   */
  public static <T> String createPKFieldName(Class<T> classRepresentingTable) {
    return classRepresentingTable.getSimpleName().toLowerCase() + "_id";
  }

  /**
   * Purpose of this class is just to be holder for json and its parse (json to object) results.
   */
  public class JsonParseData<U> {
    private String json;
    private U result;

    /**
     * Instantiates a new json parse data.
     *
     * @param json the json
     * @param result the result
     */
    public JsonParseData(String json, U result) {
      this.json = json;
      this.result = result;
    }

    /**
     * Gets the json.
     *
     * @return the json
     */
    public String getJson() {
      return json;
    }

    /**
     * Gets the result.
     *
     * @return the result
     */
    public U getResult() {
      return result;
    }
  }

}
