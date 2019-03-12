package ee.eesti.riha.rest.logic.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The Class GsonToJacksonHelper.
 */
public final class GsonToJacksonHelper {
  private static final Logger LOG = LoggerFactory.getLogger(GsonToJacksonHelper.class);

  // given object extract from it JsonContent field,
  // we need to have json_content JsonObject which is Gson,
  // to be recognized for Jackson that is serializing,
  /**
   * Gets the json object of gson as json node of jackson.
   *
   * @param <T> the generic type
   * @param jObj the j obj
   * @return the json object of gson as json node of jackson
   */
  // so convert from JsonObject (Gson) to JsonNode (Jackson);
  public static <T> T getJsonObjectOfGsonAsJsonNodeOfJackson(JsonObject jObj) {

    ObjectMapper mapper = new ObjectMapper();
    // must use JsonNode here, because Jackson does not know how to
    // serialize Gson JsonObject (serializing will become
    // complicated)
    // TODO review, could be easier option here
    JsonNode jsonContent = null;
    try {
      jsonContent = mapper.readTree(JsonHelper.GSON.toJson(jObj));
    } catch (IOException e) {
      LOG.error("Error reading JSON", e);
    }
    if (jsonContent != null) {
      return (T) jsonContent;
    } else {
      return (T) "";
    }
  }
}
