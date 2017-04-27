package ee.eesti.riha.rest.logic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.logic.util.GsonToJacksonHelper;
import ee.eesti.riha.rest.logic.util.JsonContentBasedTable;
import ee.eesti.riha.rest.logic.util.JsonFieldsFilterer;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.readonly.Kind;

// TODO: Auto-generated Javadoc
/**
 * Gather any supporting or helper methods here that are used to read object/entry. Also some table specifics related to
 * reading entry from that table.
 *
 * @param <T> the generic type
 */
@Component
public class TableEntryReadLogic<T> {

  @Autowired
  KindRepository kindRepository;

  /**
   * To get entries from some table/object type, instead of returning actual entry, something else needs to be returned
   * (i.e., for tables containing json_content need not to return all fields but content of json_content field only). If
   * no changes needed, then returns object list unchanged.
   *
   * @param objectList the object list
   * @param fieldsToKeep jsonArray of fields that must be kept, others must be removed with filtering. if fieldsToKeep
   *          is null then no filter is used
   * @return the adjusted objs based on expected json
   */
  public List<T> getAdjustedObjsBasedOnExpectedJson(List<T> objectList, JsonArray fieldsToKeep) {

    if (objectList.size() > 0) {

      if (JsonContentBasedTable.isJsonContentBasedTable(objectList.get(0).getClass())) {
        List<T> adjustedObjs = new ArrayList<>();
        for (T t : objectList) {
          adjustedObjs.add(getAdjustedObjBasedOnExpectedJson(t, fieldsToKeep));
        }
        if (adjustedObjs.size() > 0) {
          return adjustedObjs;
        }
      } else {
        // for non-JsonContentBasedTables
        // convert objectList to json and then do the filtering
        List<T> adjustedObjs = new ArrayList<>();
        if (fieldsToKeep != null) {
          for (T t : objectList) {
            JsonObject filtered = (JsonObject) JsonFieldsFilterer.filter(JsonHelper.GSON.toJsonTree(t), fieldsToKeep);
            adjustedObjs.add((T) GsonToJacksonHelper.getJsonObjectOfGsonAsJsonNodeOfJackson(filtered));
          }
          if (adjustedObjs.size() > 0) {
            return adjustedObjs;
          }
        }

      }
      // otherwise return object list unchanged

    }

    return objectList;

  }

  /**
   * Shorthand method to get objects without filtering.
   *
   * @param objectList the object list
   * @return the adjusted objs based on expected json
   * @see TableEntryReadLogic#getAdjustedObjsBasedOnExpectedJson(T, JsonArray)
   */
  public List<T> getAdjustedObjsBasedOnExpectedJson(List<T> objectList) {

    return getAdjustedObjsBasedOnExpectedJson(objectList, null);

  }

  /**
   * To get entry from some table/object type, instead of returning actual entry, something else needs to be returned
   * (i.e., for tables containing json_content need not to return all fields but content of json_content field only). If
   * no changes needed, then returns object unchanged.
   *
   * @param obj the obj
   * @param fieldsToKeep jsonArray of fields that must be kept, others must be removed with filtering. if fieldsToKeep
   *          is null then no filter is used
   * @return the adjusted obj based on expected json
   */
  public T getAdjustedObjBasedOnExpectedJson(T obj, JsonArray fieldsToKeep) {

    T adjustedObj = obj;

    if (JsonContentBasedTable.isJsonContentBasedTable(obj.getClass())) {
      // for certain tables we expect response json to contain field
      // json_content only

      BaseModel baseModel = (BaseModel) obj;
      JsonObject jsonContent = baseModel.getJson_content();

      // add kind name to json if possible
      if (jsonContent.has("kind_id")) {
        int kindId = jsonContent.get("kind_id").getAsInt();
        Kind kind = kindRepository.getById(kindId);
        if (kind != null) {
          jsonContent.addProperty("kind", kind.getName());
        }

      }

      if (fieldsToKeep != null) {
        jsonContent = (JsonObject) JsonFieldsFilterer.filter(jsonContent, fieldsToKeep);
      }
      adjustedObj = GsonToJacksonHelper.getJsonObjectOfGsonAsJsonNodeOfJackson(jsonContent);
    }
    // otherwise return object unchanged

    return adjustedObj;

  }

  /**
   * Shorthand method to get object without filtering.
   *
   * @param obj the obj
   * @return the adjusted obj based on expected json
   * @see TableEntryReadLogic#getAdjustedObjBasedOnExpectedJson(T, JsonArray)
   */
  public T getAdjustedObjBasedOnExpectedJson(T obj) {

    return getAdjustedObjBasedOnExpectedJson(obj, null);

  }

}
