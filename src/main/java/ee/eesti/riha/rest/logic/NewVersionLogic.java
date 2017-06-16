package ee.eesti.riha.rest.logic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.dao.GenericDAO;
import ee.eesti.riha.rest.dao.SecureApiGenericDAO;
import ee.eesti.riha.rest.dao.util.FilterComponent;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.logic.util.FileHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.PathHolder;
import ee.eesti.riha.rest.logic.util.QueryHolder;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;

/**
 * Handles the creation of new versions of Main_resource and of items that are connected to it. Does not modify data
 * except for version, and metadata (creator, modifier, dates) and ids for copies.
 */
@Component
public class NewVersionLogic<T, K> {

  @Autowired
  ChangeLogic<T, K> changeLogic;

  @Autowired
  TableEntryCreateLogic tableEntryCreateLogic;

  @Autowired
  SecureApiGenericDAO<T, K> secureDAO;

  @Autowired
  GenericDAO<T> noLogicDAO;

  private static final Logger LOG = LoggerFactory.getLogger(NewVersionLogic.class);

  // {"op":"newversion","path": "db/main_resource/",
  // "token": "daa", "new_version": "1.2", "uri": "miski:uri:33"}
  /**
   * Special method to create new version of Main_resource. Old version will be saved as unmodifiable copy. Current
   * version main_resource_id stays the same (previous version (arhcive) will be made with new id). All connected
   * Documents, Data_objects and Documents connected through Data_objects will be made into unmodifiable copies as well. <br>
   * Unmodifiability is checked elsewhere (by end_date).
   *
   * @param queryHolder the query holder
   * @param user AuthInfo user
   * @return new version as ObjectNode
   * @throws RihaRestException the riha rest exception
   * @throws ReflectiveOperationException the reflective operation exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Object doNewVersion(QueryHolder queryHolder, Object user) throws RihaRestException,
      ReflectiveOperationException, IOException {
    LOG.info("doNewVersion");
    // 0. extract data from queryHolder
    PathHolder pathHolder = new PathHolder(queryHolder.getPath());
    Class<T> classRepresentingTable = Finals.getClassRepresentingTable(pathHolder.tableName);

    // table that has version colum
    if (classRepresentingTable != Main_resource.class) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.CANT_CREATE_NEW_VERSION);
      error.setErrmsg(ErrorCodes.CANT_CREATE_NEW_VERSION_MSG);
      error.setErrtrace(queryHolder.getAsJson().getAsJsonObject().toString());
      throw new RihaRestException(error);
    }

    JsonObject query = queryHolder.getAsJson().getAsJsonObject();

    String[] reqPars = {"new_version", "uri" };
    Validator.cantHaveMissingReqParsInJson(reqPars, query.toString());

    String newVersion = query.get("new_version").getAsString();
    String uri = query.get("uri").getAsString();
    String userId = ((AuthInfo) user).getUser_code();

    // 1. find old by uri, sort by start_date (to get current)
    // sort by creation_date instead !! (start_date may be null)
    FilterComponent fc = new FilterComponent("uri", "=", uri);

    // actually List<ObjectNode>
    // List<T> many = doGetMany(classRepresentingTable, 1, 0, Arrays.asList(fc), "-creation_date", null, null);
    List<T> many = changeLogic.doGetMany(classRepresentingTable, 1, 0, Arrays.asList(fc), "-creation_date", null,
        (AuthInfo) user);
    Validator.noSuchUriInGivenTable(many, uri);

    // check if given uri with given version already exists?
    List<FilterComponent> versionNameAlreadyUsed = Arrays.asList(
        new FilterComponent("uri", "=", uri), new FilterComponent("version", "=", newVersion));
    List<T> itemsWithSameVersion = changeLogic.doGetMany(classRepresentingTable, null, null,
        versionNameAlreadyUsed, null, null, (AuthInfo) user);
    Validator.versionMustBeDifferent(itemsWithSameVersion, uri, newVersion, query);

    T temp = many.get(0);

    // 2. copy old to temp / set temp id to null
    int id;

    ObjectNode tempJson = (ObjectNode) temp;

    String pkField = TableEntryCreateLogic.createPKFieldName(classRepresentingTable);
    id = tempJson.get(pkField).asInt();
    tempJson.remove(pkField);
    // 3. set end_date to temp
    String dateJson = DateHelper.FORMATTER.format(new Date());
    tempJson.put("end_date", dateJson);
    tempJson.put("modified_date", dateJson);
    tempJson.put("modifier", userId);

    // remove kind, because kind_id should be used instead of kind in server
    tempJson.remove("kind");

    // 4. save temp (with new id)
    // method throws RihaRestException
    T itemTemp = (T) tableEntryCreateLogic.fromJsonContentToObjKeepExisting(tempJson.toString(),
        classRepresentingTable, user);

    // genericDAO.create(itemTemp);
    List<Integer> archivedKeys = (List<Integer>) secureDAO.create(itemTemp);

    // doCreate(tempJson.toString(), classRepresentingTable, user);
    // temp = doGet(classRepresentingTable, id, null, null);
    temp = changeLogic.doGet(classRepresentingTable, id, null, (AuthInfo) user);
    ObjectNode oldJson = (ObjectNode) temp;

    // 5. update version of old
    oldJson.put("version", newVersion);

    oldJson.put("start_date", dateJson);
    // 6. set new created, modified to old
    oldJson.put("creator", userId);
    oldJson.put("modifier", userId);
    oldJson.put("creation_date", dateJson);
    oldJson.put("modified_date", dateJson);
    // when creating new version, then end_date will be null
    oldJson.putNull("end_date");

    // remove kind
    oldJson.remove("kind");

    JsonObject jsonModified = JsonHelper.GSON.fromJson(oldJson.toString(), JsonObject.class);
    T item = JsonHelper.GSON.fromJson(jsonModified, classRepresentingTable);
    ((BaseModel) item).setJson_content(jsonModified);
    ((BaseModel) item).callSetId(id);

    // 8. save old
    noLogicDAO.update(item);
    // 9. return old (modfiied with new version)
    // temp = doGet(classRepresentingTable, id, null, null);
    temp = changeLogic.doGet(classRepresentingTable, id, null, (AuthInfo) user);

    doNewVersionDocument((Class<T>) Document.class, id, archivedKeys.get(0), dateJson, (AuthInfo) user);
    doNewVersionData_object((Class<T>) Data_object.class, id, archivedKeys.get(0), dateJson, (AuthInfo) user);

    return temp;
  }

  /**
   * Helper method to call {@link #doNewVersion(QueryHolder, Object)}. This method only builds the queryHolder needed,
   * to ease access from other classes.
   * 
   * @see #doNewVersion(QueryHolder, Object)
   */
  public Object doNewVersion(String newVersion, String uri, AuthInfo user) throws RihaRestException,
      ReflectiveOperationException, IOException {
    LOG.info("doNewVersion construct queryHolder");

    JsonObject queryJson = new JsonObject();
    queryJson.addProperty("new_version", newVersion);
    queryJson.addProperty("path", "db/main_resource/");
    queryJson.addProperty("uri", uri);
    // op is not needed
    QueryHolder queryHolder = QueryHolder.create(JsonHelper.GSON, queryJson.toString());

    return doNewVersion(queryHolder, user);
  }

  private void doNewVersionDocument(Class<T> clazz, int currentMain_resourceId, int archivedMain_resourceId,
      String dateJson, AuthInfo user) throws RihaRestException {

    LOG.info("doNewVersionDocument currentMainResourceId " + currentMain_resourceId 
        + " archivedMainResourceId " + archivedMain_resourceId);
    
    FilterComponent filterByMR = new FilterComponent("main_resource_id", "=", "" + currentMain_resourceId);

    doNewVersionDocumentHelper(clazz, filterByMR, archivedMain_resourceId, "main_resource_id", dateJson, user);

  }

  private void doNewVersionDocumentThroughData_object(Class<T> clazz, int currentData_objectId,
      int archivedData_objectId, String dateJson, AuthInfo user) throws RihaRestException {

    FilterComponent filterByData_object = new FilterComponent("data_object_id", "=", "" + currentData_objectId);

    doNewVersionDocumentHelper(clazz, filterByData_object, archivedData_objectId, "data_object_id", dateJson, user);

  }

  private void doNewVersionDocumentHelper(Class<T> clazz, FilterComponent filterById, int archivedId, String idField,
      String dateJson, AuthInfo user) throws RihaRestException {
    // find all connected Documents

    // List<ObjectNode> actually
    List<T> connectedDocs = changeLogic.doGetMany((Class<T>) Document.class, null, null, Arrays.asList(filterById),
        null, null, user);

    // for each Document create copy (with new id), set End_date on copy, set archived main_resource_id

    LOG.info("connectedDocs to current: " + connectedDocs.size());
    
    // map current docId to archived docId
    // key is current, value is archived
    Map<Integer, Integer> docIdMap = new HashMap<>();
    for (T connectedDoc : connectedDocs) {
      ObjectNode docJson = (ObjectNode) connectedDoc;
      
      LOG.info("Connected Doc to current " + docJson);
      
      // remove id
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);
      int id = docJson.get(pkField).asInt();
      docJson.remove(pkField);

      docJson.put(idField, archivedId);
      // can't add end_date here, because can't update object which has end_date set with SecureApiGenericDAO
      // docJson.put("end_date", dateJson);
      docJson.put("modified_date", dateJson);
      docJson.put("modifier", user.getUser_code());

      // remove kind
      docJson.remove("kind");

      LOG.info("Archiving doc " + docJson);
      
      T itemTemp = (T) tableEntryCreateLogic.fromJsonContentToObjKeepExisting(docJson.toString(),
          clazz, user);
      // save archived item to database
      List<Integer> createdKeys = (List<Integer>) secureDAO.create(itemTemp);
      docIdMap.put(id, createdKeys.get(0));
    }
    
    LOG.info("Map of current doc ids to archived doc ids " + docIdMap);

    // get unmodified connected Documents
    connectedDocs = changeLogic.doGetMany((Class<T>) Document.class, null, null, Arrays.asList(filterById), null, null,
        user);

    // for each Document (original) set Start_date, set
    for (T connectedDoc : connectedDocs) {
      ObjectNode docJson = (ObjectNode) connectedDoc;
      LOG.info("Original doc, that becomes current: " + docJson);
      docJson.put("start_date", dateJson);
      docJson.put("modified_date", dateJson);
      docJson.put("modifier", user.getUser_code());
      // remove kind
      docJson.remove("kind");
      // when creating new version, then end_date will be null
      docJson.putNull("end_date");
      docJson.put("content", FileHelper.createDocumentFilePath(docJson.get("document_id").asInt()));
      LOG.info("Original doc, modified to current: " + docJson);
      T itemTemp = JsonHelper.GSON.fromJson(docJson.toString(), clazz);
      BaseModel bm = (BaseModel) itemTemp;
      bm.setJson_content(JsonHelper.getFromJson(docJson.toString()));
      // update new version
      noLogicDAO.update(itemTemp);
    }

    LOG.info("Copying files...");
    
    // copy files
    for (Entry<Integer, Integer> entry : docIdMap.entrySet()) {
      int currentDocId = entry.getKey();
      int archivedDocId = entry.getValue();
      String currentDocPath = FileHelper.createDocumentFilePathWithRoot(currentDocId);
      String archivedDocPath = FileHelper.createDocumentFilePathWithRoot(archivedDocId);
      String archivedDocPathToSave = FileHelper.createDocumentFilePath(archivedDocId);
      // can't update file path (content) before, because new id of archived document is not known
      JsonObject updateJson = new JsonObject();
      updateJson.addProperty("document_id", archivedDocId);
      updateJson.addProperty("content", archivedDocPathToSave);
      // add end_date here, because can't update object which has end_date set with SecureApiGenericDAO
      updateJson.addProperty("end_date", dateJson);

      LOG.info("updateJson in copy: " + updateJson);
      
      Document doc = JsonHelper.GSON.fromJson(updateJson, Document.class);
      doc.setJson_content(updateJson);
      secureDAO.update((T) doc, archivedDocId);
      LOG.info("update successful? time to copy files");
      try {
        LOG.info("Copying file in new version current to archived");
        LOG.info("Copying.. " + currentDocPath + " -> " + archivedDocPath);
        LOG.info("IDs current -> archived " + currentDocId + " -> " + archivedDocId);
        FileHelper.copyFile(currentDocPath, archivedDocPath);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }

    }
  }

  private void doNewVersionData_object(Class<T> clazz, int currentMain_resourceId, int archivedMain_resourceId,
      String dateJson, AuthInfo user) throws RihaRestException {
    // find all connected Data_objects
    FilterComponent filterByMR = new FilterComponent("main_resource_id", "=", "" + currentMain_resourceId);
    // List<ObjectNode> actually
    List<T> connectedData_objects = changeLogic.doGetMany((Class<T>) Data_object.class, null, null,
        Arrays.asList(filterByMR),
        null, null, user);

    // arr[0] is currentId, arr[1] is archivedId
    Map<String, int[]> uriToDataIdMap = new HashMap<>();
    // for each Data_object create copy (with new id), set End_date on copy, set archived main_resource_id

    for (T connectedData : connectedData_objects) {
      ObjectNode dataJson = (ObjectNode) connectedData;

      // remove id
      String pkField = TableEntryCreateLogic.createPKFieldName(clazz);
      int id = dataJson.get(pkField).asInt();
      dataJson.remove(pkField);

      dataJson.put("main_resource_id", archivedMain_resourceId);
      dataJson.put("end_date", dateJson);
      dataJson.put("modified_date", dateJson);
      dataJson.put("modifier", user.getUser_code());

      // remove kind
      dataJson.remove("kind");

      T itemTemp = (T) tableEntryCreateLogic.fromJsonContentToObjKeepExisting(dataJson.toString(),
          clazz, user);
      // save archived item to database
      List<Integer> createdKeys = (List<Integer>) secureDAO.create(itemTemp);
    }

    // get unmodified connected Data_objects
    connectedData_objects = changeLogic.doGetMany((Class<T>) Data_object.class, null, null,
        Arrays.asList(filterByMR), null, null, user);

    // for each Data_object (original) set Start_date, set modifier
    for (T connectedData : connectedData_objects) {
      ObjectNode dataJson = (ObjectNode) connectedData;
      dataJson.put("start_date", dateJson);
      dataJson.put("modified_date", dateJson);
      dataJson.put("modifier", user.getUser_code());
      // remove kind
      dataJson.remove("kind");
      // when creating new version, then end_date will be null
      dataJson.putNull("end_date");

      T itemTemp = JsonHelper.GSON.fromJson(dataJson.toString(), clazz);
      BaseModel bm = (BaseModel) itemTemp;
      bm.setJson_content(JsonHelper.getFromJson(dataJson.toString()));
      // update new version
      noLogicDAO.update(itemTemp);
      uriToDataIdMap.put(dataJson.get("uri").asText(), new int[] {dataJson.get("data_object_id").asInt(), -1 });
    }

    // get archived data_object ids
    FilterComponent filterByMRArchived = new FilterComponent("main_resource_id", "=", "" + archivedMain_resourceId);
    List<T> justArchivedData = changeLogic.doGetMany((Class<T>) Data_object.class, null, null,
        Arrays.asList(filterByMRArchived), null, null, user);
    // add archived data_object ids to uriToDataIdMap
    for (T connectedData : justArchivedData) {
      ObjectNode dataJson = (ObjectNode) connectedData;
      uriToDataIdMap.get((dataJson).get("uri").asText())[1] = dataJson.get("data_object_id").asInt();
    }

    for (int[] ids : uriToDataIdMap.values()) {
      doNewVersionDocumentThroughData_object((Class<T>) Document.class, ids[0], ids[1], dateJson, user);
    }

  }

}
