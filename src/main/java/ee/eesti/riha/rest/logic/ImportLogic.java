package ee.eesti.riha.rest.logic;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;

import static ee.eesti.riha.rest.logic.util.DateHelper.DATE_FORMAT_IN_JSON;

/**
 * Handles full Main_resource (with connected items [Data_object, Document]) import.
 */
// causes error
// @Transactional
@Component
public class ImportLogic {

  @Autowired
  private ChangeLogic changeLogic;

  @Autowired
  private ServiceLogic serviceLogic;
  
  @Autowired
  private NewVersionLogic<Main_resource, Integer> newVersionLogic;

  private static final Logger LOG = LoggerFactory.getLogger(ImportLogic.class);

  /**
   * Creates new, updates or creates new version of Main_resource and of its connected items. Data_objects and Documents
   * that exist in database but do not exist in importJson will be deleted.
   * 
   * @param importJson
   * @throws RihaRestException
   * @throws ReflectiveOperationException
   * @throws IOException
   */
  public void logic(JsonObject importJson) throws RihaRestException, ReflectiveOperationException,
      IOException {

    String[] reqPars = {"uri", "version" };
    Validator.cantHaveMissingReqParsInJson(reqPars, importJson.toString());

    String importVersion = importJson.get("version").getAsString();
    Set<Entry<String, JsonElement>> jsonEntries = importJson.entrySet();

    // find all fields of type array, that contain objects with field called field_name
    List<String> arrayFieldsWithObjects = filterArrayFieldsWithFieldNames(jsonEntries);

    Map<String, JsonObject> uriToJsonObject = createUriToJsonMap(arrayFieldsWithObjects, importJson);

    String dateJson = new SimpleDateFormat(DATE_FORMAT_IN_JSON).format(new Date());
    String uri = importJson.get("uri").getAsString();
    FilterComponent fc = new FilterComponent("uri", "=", uri);
    // query active only
    FilterComponent fc2 = new FilterComponent("end_date", "null_or_>", dateJson);

    List<ObjectNode> many = changeLogic.doGetMany(Main_resource.class, 1, 0, Arrays.asList(fc, fc2), "-creation_date",
        null);

    if (many.isEmpty()) {
      // no corresponding Main_resource in database
      // save imported Main_resource to database
      LOG.info("Import create new");
      createFromImport(importJson, arrayFieldsWithObjects, uriToJsonObject);
    } else {

      ObjectNode existing = many.get(0);
      String existingVersion = existing.get("version").asText();
      int main_resource_id = existing.get("main_resource_id").asInt();
      if (existingVersion.equals(importVersion)) {
        // same version then update
        LOG.info("Import update");
        updateFromImport(main_resource_id, uriToJsonObject, arrayFieldsWithObjects, importJson);
      } else {
        // check older versions also
        // throw error if exists
        sameUriWithVersionExists(uri, importVersion, importJson);
        // different version then new version
        LOG.info("Import new version");
        newVersionFromImport(importJson, uri, main_resource_id,
            uriToJsonObject, arrayFieldsWithObjects);
      }
    }

  }

  /**
   * Creates new version of corresponding main_resource (and connected items) in database, then updates created new
   * version with values from importJson using {@link #updateFromImport(int, Map, List, JsonObject)}
   */
  protected void newVersionFromImport(JsonObject importJson, String uri, int main_resource_id,
                                      Map<String, JsonObject> uriToJsonObject, List<String> arrayFieldsWithObjects)
      throws RihaRestException, ReflectiveOperationException, IOException {

    // 1. create new version from Main_resource in database
    String newVersion = importJson.get("version").getAsString();

    newVersionLogic.doNewVersion(newVersion, uri);

    // 2. update new version of Main_resource with values from importJson
    // main_resource_id stays the same, copy will have new id
    updateFromImport(main_resource_id, uriToJsonObject, arrayFieldsWithObjects, importJson);

  }

  /**
   * Creates new Main_resource and extract its nested objects to separate tables.
   */
  protected void createFromImport(JsonObject importJson, List<String> arrayFieldsWithObjects,
                                  Map<String, JsonObject> uriToJsonObject) throws RihaRestException {
    // save imported Main_resource to database
    ObjectNode mainResourceJson = createMain_resourceFromImport(importJson, arrayFieldsWithObjects);
    int main_resource_id = mainResourceJson.get("main_resource_id").asInt();
    createConnectedObjects(uriToJsonObject, main_resource_id);
  }

  /**
   * Updates corresponding Main_resource and nested items. If nested items don't exist then creates new, if exist then
   * updates. If imported Main_resource does not have connected item that exists in database then deletes.
   */
  protected void updateFromImport(int main_resource_id, Map<String, JsonObject> uriToJsonObject,
                                  List<String> arrayFieldsWithObjects, JsonObject importJson) throws RihaRestException,
      ReflectiveOperationException, IOException {

    List<ObjectNode> dataObjects = changeLogic.doGetByMainResourceId(Data_object.class, main_resource_id);
    List<ObjectNode> documents = changeLogic.doGetByMainResourceId(Document.class, main_resource_id);
    List<ObjectNode> services = serviceLogic.getServicesByParentId(main_resource_id);

    // updateOrDeleteConnectedObjects(dataObjects, uriToJsonObject, Data_object.class, user);
    // special method for nested Documents
    updateOrDeleteConnectedDataObjects(dataObjects, uriToJsonObject);

    // special case for nested service in Main_resource
    updateOrDeleteConnectedServices(services, uriToJsonObject);
    
    updateOrDeleteConnectedObjects(documents, uriToJsonObject, Document.class);

    // only those remain in uriToJsonObject that must be created
    createConnectedObjects(uriToJsonObject, main_resource_id);

    // remove Documents and Data_objects from imported Main_resource
    for (String arrayField : arrayFieldsWithObjects) {
      importJson.remove(arrayField);
    }
    // update Main_resource
    changeLogic.doUpdate(importJson.toString(), Main_resource.class, main_resource_id, null);
  }

  /**
   * Creates the main_resource from import. Makes copy of importJson and removes nested items from copy, that must be
   * extracted to other tables.
   *
   */
  private ObjectNode createMain_resourceFromImport(JsonObject importJson, List<String> arrayFieldsWithObjects) throws RihaRestException {
    JsonObject jsonObjectCopy = JsonHelper.getFromJson(importJson.toString());
    // remove Documents and Data_objects from imported Main_resource
    for (String arrayField : arrayFieldsWithObjects) {
      jsonObjectCopy.remove(arrayField);
    }
    jsonObjectCopy.remove("main_resource_id");

    changeLogic.replaceKindWithKindId(jsonObjectCopy);

    // create imported Main_resource
    List<Integer> createdKeys = changeLogic.doCreate(jsonObjectCopy.toString(), Main_resource.class);
    int main_resourceId = createdKeys.get(0);
    return (ObjectNode) changeLogic.doGet(Main_resource.class, main_resourceId, null);

  }

  /**
   * Create nested new objects that did not exist before. Also creates nested Documents in Data_object
   * 
   */
  private void createConnectedObjects(Map<String, JsonObject> uriToJsonObject, int main_resource_id)
      throws RihaRestException {

    // only those remain in uriToJsonObject that must be created
    LOG.info("Create - uriToJsonObject: " + uriToJsonObject);
    for (Entry<String, JsonObject> entry : uriToJsonObject.entrySet()) {
      JsonObject jsonToCreate = entry.getValue();
      // remove primary key
      Class clazz = getType(jsonToCreate);
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);
      jsonToCreate.remove(pkField);
      jsonToCreate.addProperty("main_resource_id", main_resource_id);

      changeLogic.replaceKindWithKindId(jsonToCreate);

      if (clazz == Data_object.class) {
        // special case, Data_object may have child Documents
        List<String> dataObjectDocumentArray = filterArrayFieldsWithFieldNames(jsonToCreate.entrySet());
        Map<String, JsonObject> dataUriToJsonObject = createUriToJsonMap(dataObjectDocumentArray, jsonToCreate);

        for (String documentField : dataObjectDocumentArray) {
          jsonToCreate.remove(documentField);
        }
        // create connected data
        List<Integer> createdKeys = changeLogic.doCreate(jsonToCreate.toString(), clazz);
        int dataObjectId = createdKeys.get(0);

        // create Docuemnts connected to data
        for (Entry<String, JsonObject> entryDoc : dataUriToJsonObject.entrySet()) {
          // expect that consists of Documents
          JsonObject jsonDoc = entryDoc.getValue();
          jsonDoc.addProperty("data_object_id", dataObjectId);
          changeLogic.replaceKindWithKindId(jsonDoc);
          changeLogic.doCreate(jsonDoc.toString(), Document.class);
        }
      } else if (clazz == Main_resource.class) {
        LOG.info("Creating special Main_resource services");
        
        // join element with main_resource
        jsonToCreate.addProperty("main_resource_parent_id", main_resource_id);
        jsonToCreate.add("main_resource_id", JsonNull.INSTANCE);
        
        // special case, nest Main_resource ("service") may have child Documents
        List<String> serviceDocumentArray = filterArrayFieldsWithFieldNames(jsonToCreate.entrySet());
        Map<String, JsonObject> serviceUriToJsonObject = createUriToJsonMap(serviceDocumentArray, jsonToCreate);

        for (String documentField : serviceDocumentArray) {
          jsonToCreate.remove(documentField);
        }
        // create connected data
        List<Integer> createdKeys = changeLogic.doCreate(jsonToCreate.toString(), clazz);
        int serviceId = createdKeys.get(0);

        // create Docuemnts connected to data
        for (Entry<String, JsonObject> entryDoc : serviceUriToJsonObject.entrySet()) {
          // expect that consists of Documents
          JsonObject jsonDoc = entryDoc.getValue();
          jsonDoc.addProperty("main_resource_id", serviceId);
          changeLogic.replaceKindWithKindId(jsonDoc);
          changeLogic.doCreate(jsonDoc.toString(), Document.class);
        }
      } else {
        // create connected data
        List<Integer> createdKeys = changeLogic.doCreate(jsonToCreate.toString(), clazz);
      }

    }

  }

  /**
   * Special method when Document is nested in Data_object to create.
   *
   * @param dataUriToJsonObject Data_object nested documents mapped by uri
   * @param data_object_id the data_object_id
   * @throws RihaRestException the riha rest exception
   */
  private void createConnectedDataObjectDocuments(Map<String, JsonObject> dataUriToJsonObject, int data_object_id) throws RihaRestException {

    // only those remain in uriToJsonObject that must be created
    LOG.info("Create DataObject - uriToJsonObject: " + dataUriToJsonObject);
    for (Entry<String, JsonObject> entry : dataUriToJsonObject.entrySet()) {
      JsonObject jsonToCreate = entry.getValue();
      // remove primary key
      Class clazz = getType(jsonToCreate);
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);
      jsonToCreate.remove(pkField);
      jsonToCreate.addProperty("data_object_id", data_object_id);

      changeLogic.replaceKindWithKindId(jsonToCreate);

      // create connected data
      List<Integer> createdKeys = changeLogic.doCreate(jsonToCreate.toString(), clazz);

    }

  }

  /**
   * Updates connectedObjs if has same uri in uriToJsonObject, else deletes. <b>Uri is removed from uriToJsonObject
   * after check</b>, so afterwards only those uris remain, that must be created.
   *  @param connectedObjs Data_objects or Documents json_content
   * @param uriToJsonObject will be modified!
   */
  private void updateOrDeleteConnectedObjects(List<ObjectNode> connectedObjs, Map<String, JsonObject> uriToJsonObject,
                                              Class clazz) throws ReflectiveOperationException, RihaRestException, IOException {
    for (ObjectNode connectedObj : connectedObjs) {
      String objUri = connectedObj.get("uri").asText();
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);

      // remove ids because can cause foreign key errors
      uriToJsonObject.get(objUri).remove("main_resource_id");
      uriToJsonObject.get(objUri).remove("data_object_id");
      uriToJsonObject.get(objUri).remove("document_id");

      int objId = connectedObj.get(pkField).asInt();
      if (uriToJsonObject.containsKey(objUri)) {
        LOG.info("Update exists");
        changeLogic.doUpdate(uriToJsonObject.get(objUri).toString(), clazz, objId, null);
        // update item
      } else {
        LOG.info("Delete does not exist " + objUri);
        changeLogic.doDelete(clazz.getSimpleName(), objId);
        // delete item
      }
      // remove uri so only those that must be created remain
      uriToJsonObject.remove(objUri);

    }
  }

  /**
   * Like {@link #updateOrDeleteConnectedObjects(List, Map, Class)} but special case for Data_object to handle
   * nested Documents in Data_object.
   *
   * @param connectedObjs Data_objects with nested Documents
   * @param uriToJsonObject Data_object parent Main_resource's nested child Data_objects, Documents mapped by uri
   * @throws ReflectiveOperationException the reflective operation exception
   * @throws RihaRestException the riha rest exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void updateOrDeleteConnectedDataObjects(List<ObjectNode> connectedObjs,
                                                  Map<String, JsonObject> uriToJsonObject) throws ReflectiveOperationException, RihaRestException, IOException {
    LOG.info("UpdateOrDeleteConnectedDataObjects");
    Class clazz = Data_object.class;
    for (ObjectNode connectedObj : connectedObjs) {
      String dataUri = connectedObj.get("uri").asText();
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);
      int dataId = connectedObj.get(pkField).asInt();
      LOG.info("OBJ_URI: " + dataUri);
      LOG.info("DATA OBJECT -> uriToJsonObject " + uriToJsonObject + "\n");

      if (uriToJsonObject.containsKey(dataUri)) {
        LOG.info("Update Data_object exists");

        JsonObject dataObjectJson = uriToJsonObject.get(dataUri);

        LOG.info("ENTRY SET " + dataObjectJson.entrySet());
        List<String> dataArrayFields = filterArrayFieldsWithFieldNames(dataObjectJson.entrySet());
        Map<String, JsonObject> dataUriToJsonObject = createUriToJsonMap(dataArrayFields, dataObjectJson);

        LOG.info("DATA_ARRAY_FIELDS " + dataArrayFields);
        LOG.info("DATA_URI_JSON_OBJECT " + dataUriToJsonObject + "\n");

        // check if connectedObj has nested Documents
        FilterComponent fc = new FilterComponent("data_object_id", "=", "" + dataId);
        List<ObjectNode> documents = changeLogic.doGetMany(Document.class, null, null, Arrays.asList(fc), null, null);
        if (documents.size() > 0) {
          // update or delete
          LOG.info("SHOULD UPDATE OR DELETE NESTED DOCUEMNT ");
          updateOrDeleteConnectedObjects(documents, dataUriToJsonObject, Document.class);
        }
        LOG.info("SHOULD CREATE NESTED DOCUEMNT");
        // create new if does not exist before
        createConnectedDataObjectDocuments(dataUriToJsonObject, dataId);

        for (String arrayField : dataArrayFields) {
          // remove nested Document, they are extracted
          uriToJsonObject.get(dataUri).remove(arrayField);
        }

        changeLogic.doUpdate(uriToJsonObject.get(dataUri).toString(), clazz, dataId, null);
        // update item
      } else {
        LOG.info("Delete Data_object does not exist  " + dataUri);
        changeLogic.doDelete(clazz.getSimpleName(), dataId);
        // delete item
      }
      // remove uri so only those that must be created remain
      uriToJsonObject.remove(dataUri);

    }
  }
  
  /**
   * Like {@link #updateOrDeleteConnectedObjects(List, Map, Class)} but special case for Main_resource servcie to handle
   * nested Documents in Main_resource.
   *
   * @param connectedObjs Data_objects with nested Documents
   * @param uriToJsonObject Data_object parent Main_resource's nested child Data_objects, Documents mapped by uri
   * @throws ReflectiveOperationException the reflective operation exception
   * @throws RihaRestException the riha rest exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void updateOrDeleteConnectedServices(List<ObjectNode> connectedObjs,
                                               Map<String, JsonObject> uriToJsonObject)
          throws ReflectiveOperationException, RihaRestException, IOException {
    LOG.info("UpdateOrDeleteConnectedServices");
    Class clazz = Main_resource.class;
    for (ObjectNode connectedObj : connectedObjs) {
      String serviceUri = connectedObj.get("uri").asText();
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);
      int serviceId = connectedObj.get(pkField).asInt();
      LOG.info("OBJ_URI: " + serviceUri);
      LOG.info("MR SERVICE -> uriToJsonObject " + uriToJsonObject + "\n");

      if (uriToJsonObject.containsKey(serviceUri)) {
        LOG.info("Update service exists");

        JsonObject dataObjectJson = uriToJsonObject.get(serviceUri);

        LOG.info("ENTRY SET " + dataObjectJson.entrySet());
        List<String> dataArrayFields = filterArrayFieldsWithFieldNames(dataObjectJson.entrySet());
        Map<String, JsonObject> serviceUriToJsonObject = createUriToJsonMap(dataArrayFields, dataObjectJson);

        LOG.info("DATA_ARRAY_FIELDS " + dataArrayFields);
        LOG.info("SERVICE_URI_JSON_OBJECT " + serviceUriToJsonObject + "\n");

        // check if connectedObj has nested Documents
        FilterComponent fc = new FilterComponent("main_resource_id", "=", "" + serviceId);
        List<ObjectNode> documents = changeLogic.doGetMany(Document.class, null, null, Arrays.asList(fc), null, null);
        if (documents.size() > 0) {
          // update or delete
          LOG.info("SHOULD UPDATE OR DELETE NESTED DOCUEMNT ");
          updateOrDeleteConnectedObjects(documents, serviceUriToJsonObject, Document.class);
        }
        LOG.info("SHOULD CREATE NESTED DOCUEMNT");
        // create new if does not exist before
        createConnectedDataObjectDocuments(serviceUriToJsonObject, serviceId);

        for (String arrayField : dataArrayFields) {
          // remove nested Document, they are extracted
          uriToJsonObject.get(serviceUri).remove(arrayField);
        }

        changeLogic.doUpdate(uriToJsonObject.get(serviceUri).toString(), clazz, serviceId, null);
        // update item
      } else {
        LOG.info("Delete service does not exist  " + serviceUri);
        changeLogic.doDelete(clazz.getSimpleName(), serviceId);
        // delete item
      }
      // remove uri so only those that must be created remain
      uriToJsonObject.remove(serviceUri);

    }
  }

  /**
   * Find fields that are of type array, that contain objects, which have a field called field_name and the value of
   * that name equals the name of the array field
   * 
   * @param jsonEntries
   * @return
   */
  private List<String> filterArrayFieldsWithFieldNames(Set<Entry<String, JsonElement>> jsonEntries) {
    List<String> arrayFieldsWithObjects = new ArrayList<>();
    for (Entry<String, JsonElement> jsonEntry : jsonEntries) {

      if (jsonEntry.getValue().isJsonArray()) {
        JsonArray jsonArray = jsonEntry.getValue().getAsJsonArray();
        if (jsonArray.size() > 0 && jsonArray.get(0).isJsonObject()) {
          // special case if field_name is NULL then add to array by class
          // ServiceLogic.getResourceById, extractItemsByFieldName
          if (jsonEntry.getKey().equals(Finals.DEFAULT_DOC) || jsonEntry.getKey().equals(Finals.DEFAULT_DATA)
              || jsonEntry.getKey().equals(Finals.DEFAULT_MAIN_RESOURCE)) {
            arrayFieldsWithObjects.add(jsonEntry.getKey());
          }
          JsonObject jsonObj = jsonArray.get(0).getAsJsonObject();
          if (jsonObj.has("field_name")) {
            if (jsonEntry.getKey().equals(jsonObj.get("field_name").getAsString())) {
              arrayFieldsWithObjects.add(jsonEntry.getKey());
            } else {
              throw new IllegalArgumentException("field_name value should be same as the array field name "
                  + jsonEntry.getKey() + " - " + jsonObj.get("field_name").getAsString());
            }
          }

        }
      }
    }
    return arrayFieldsWithObjects;
  }

  /**
   * Creates map of Document and Data_object uris to Document and Data_object json_contents
   */
  private Map<String, JsonObject> createUriToJsonMap(List<String> arrayFieldsWithObjects, JsonObject jsonObject) {
    Map<String, JsonObject> uriToJsonObject = new HashMap<>();

    for (String arrayField : arrayFieldsWithObjects) {
      JsonArray jsonArray = jsonObject.get(arrayField).getAsJsonArray();
      for (JsonElement je : jsonArray) {
        JsonObject baseModelJson = je.getAsJsonObject();
        Class clazz = getType(baseModelJson);

        // id might not be the same
        baseModelJson.remove("main_resource_id");

        String uri = baseModelJson.get("uri").getAsString();
        LOG.info(uri);
        // baseModelJson.addProperty("it_works", "SUPER__YEEE");
        uriToJsonObject.put(uri, baseModelJson);

      }

    }
    return uriToJsonObject;
  }

  /**
   * Get class from JsonObject by existence of ids
   * 
   */
  private Class getType(JsonObject jsonObject) {
    if (jsonObject.has("comment_id")) {
      return Comment.class;
    } else if (jsonObject.has("document_id")) {
      return Document.class;
    } else if (jsonObject.has("data_object_id")) {
      return Data_object.class;
    } else if (jsonObject.has("main_resource_id")) {
      return Main_resource.class;
    } else {
      return getTypeWithoutIds(jsonObject);
    }
  }
  
  /**
   * Get class from JsonObject from import based on fields
   * TODO this is temporary until added table name to kind
   */
  private Class getTypeWithoutIds(JsonObject jsonObject) {
    String kind = jsonObject.get(Finals.KIND).getAsString();
    if (jsonObject.has("owner") && jsonObject.has("version") && kind.equals("service")) {
      return Main_resource.class;
    } else if (jsonObject.has("content") || jsonObject.has("url")
        || jsonObject.has("mime") || jsonObject.has("filename")) {
      return Document.class;
    } else if (kind.equals("entity") || kind.equals("function")) {
      return Data_object.class;
    } else {
      return null;
    }
  }

  /**
   * Throws error if Main_resource with same uri and version exists.
   */
  private void sameUriWithVersionExists(String uri, String version, JsonObject jsonObject)
      throws RihaRestException {
    // check if given uri with given version already exists?
    List<FilterComponent> versionNameAlreadyUsed = Arrays.asList(
        new FilterComponent("uri", "=", uri), new FilterComponent("version", "=", version));
    List<ObjectNode> itemsWithSameVersion = changeLogic.doGetMany(Main_resource.class, null, null,
        versionNameAlreadyUsed, null, null);
    Validator.versionMustBeDifferent(itemsWithSameVersion, uri, version, jsonObject);
  }
}
