package ee.eesti.riha.rest.logic.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.logic.Finals;

/**
 * Helper class to filter JsonObject fields, to get useful fields only
 *
 */
public final class JsonFieldsFilterer {

  private static final Logger LOG = LoggerFactory.getLogger(JsonFieldsFilterer.class);
  
  private JsonFieldsFilterer() {

  }

  /**
   * Filters data to return object(s) which contain(s) only fields from fieldsToKeep
   * 
   * @param data JsonArray or JsonObject to be filtered
   * @param fieldsToKeep JsonArray of fields to keep (strings)
   * @return filtered object or array
   */
  public static JsonElement filter(JsonElement data, JsonArray fieldsToKeep) {
    JsonElement result;

    // empty object
    if (isEmpty(data)) {
      result = data;
    } else if (data.isJsonArray()) {
      result = filterList(data.getAsJsonArray(), fieldsToKeep);
    } else {
      result = filterSingle(data.getAsJsonObject(), fieldsToKeep);
    }

    return result;
  }

  public static void removeIdFields(ObjectNode json, List<String> unWantedFields) {
    Iterator<Entry<String, JsonNode>> iterator = json.fields();
    while(iterator.hasNext()) {
      Entry<String,JsonNode> field = iterator.next();
      for (String unwantedField : unWantedFields) {
        // remove unwanted field
        if (StringHelper.areEqual(field.getKey(), unwantedField)) {
          iterator.remove();
          LOG.info("Removed unwanted field: " + field.getKey());
          break;
        }
      }
      
      if (field.getValue().isArray()) {
        ArrayNode array = (ArrayNode) field.getValue();
        // if array contains entities with kind then call recursively
        for (JsonNode node : array) {
          if (node.isObject()) {
            removeIdFields((ObjectNode) node, unWantedFields);
          }
        }
      } else if (field.getValue().isObject()) {
        removeIdFields((ObjectNode) field.getValue(), unWantedFields);
      }
      
    }
    LOG.info("Removing finished");
  }
  
  private static boolean isEmpty(JsonElement data) {
    return data.hashCode() == 0;
  }

  private static JsonObject filterSingle(JsonObject data, JsonArray fieldsToKeep) {
    JsonObject result = new JsonObject();
    if (isEmpty(data)) {
      return data;
    }
    for (JsonElement elem : fieldsToKeep) {
      String fieldName = elem.getAsString();
      JsonElement value = data.get(fieldName);
      if (value != null) {
        result.add(fieldName, value);
      }
    }

    return result;
  }

  private static JsonArray filterList(JsonArray data, JsonArray fieldsToKeep) {
    JsonArray result = new JsonArray();

    for (JsonElement elem : data) {
      result.add(filterSingle((JsonObject) elem, fieldsToKeep));
    }

    return result;
  }

}
