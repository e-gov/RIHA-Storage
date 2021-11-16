package ee.eesti.riha.rest.logic.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// TODO: Auto-generated Javadoc
/**
 * The Class JsonHelper.
 */
public final class JsonHelper {

  private JsonHelper() {

  }

  public static final Gson GSON = new GsonBuilder().setDateFormat(DateHelper.DATE_FORMAT_IN_JSON).serializeNulls()
      .create();

  private static final Logger LOG = LoggerFactory.getLogger(JsonHelper.class);

  /**
   * Gets the from json.
   *
   * @param json the json
   * @return the from json
   */
  public static JsonObject getFromJson(String json) {
    return GSON.fromJson(json, JsonObject.class);
  }

  /**
   * Json to map.
   *
   * @param json the json
   * @return the map
   */
  public static Map<String, Object> jsonToMap(String json) {

    Map<String, Object> jsonAsMap = GSON.fromJson(json, new TypeToken<HashMap<String, Object>>() {
    }.getType());
    return jsonAsMap;

  }

  /**
   * To json object.
   *
   * @param json the json
   * @return the json object
   */
  public static JsonObject toJsonObject(String json) {

    return (JsonObject)JsonParser.parseString(json);

  }

  /**
   * Updates json properties of one json with properties from another json.
   *
   * @param jsonToUpd original json
   * @param updateData json properties to add/update the original json with
   * @param clazz the clazz
   * @return edited original json with updated info
   */
  public static JsonObject updateJsonObjWithValuesFromAnotherJsonObj(JsonObject jsonToUpd, JsonObject updateData,
      Class clazz) {

    LOG.info("" + updateData);

    for (Entry<String, JsonElement> e : updateData.entrySet()) {

      String propertyOrAndFieldToUpd = e.getKey();
      JsonElement value = e.getValue();

      // field must not be updated

      if (JsonFieldHelper.nonUpdatableFieldsMap.get(clazz).containsKey(e.getKey())) {
        LOG.info("SKIP UPDATE JSON FIELD" + e.getKey());
        continue;
      }

      jsonToUpd.add(propertyOrAndFieldToUpd, value);

    }

    return jsonToUpd;

  }

  /**
   * Checks if is json array.
   *
   * @param json the json
   * @return true, if is json array
   */
  public static boolean isJsonArray(String json) {

    String trimmedJson = StringUtils.trimLeadingWhitespace(json);
    if (trimmedJson.charAt(0) == '[') {
      return true;
    } else if (trimmedJson.charAt(0) == '{') {
      return false;
    } else {
      throw new IllegalArgumentException("Given JSON is not valid object or array: " + json);
    }

  }

  /**
   * Checks if is valid json.
   *
   * @param json the json
   * @return true, if is valid json
   */
  public static boolean isValidJson(String json) {

    try {
      GSON.fromJson(json, JsonObject.class);
      return true;
    } catch (JsonSyntaxException ex) {
      try {
        GSON.fromJson(json, List.class);
        return true;
      } catch (JsonSyntaxException e) {
        return false;
      }
    }

  }

  /**
   * Json to string list.
   *
   * @param jsonArray the json array
   * @return the list
   */
  public static List<String> jsonToStringList(JsonArray jsonArray) {
    List<String> result = new ArrayList<>();
    for (JsonElement e : jsonArray) {
      result.add(e.getAsString());
    }
    return result;
  }

  /**
   * Get json value by key or default if null.
   *
   * @param json the json
   * @param key the key
   * @param defaultValue the default value
   * @return the string
   */
  public static String get(ObjectNode json, String key, String defaultValue) {
    if (json.get(key) == null) {
      return defaultValue;
    }
    return json.get(key).asText(defaultValue);
  }

}
