package ee.eesti.riha.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import ee.eesti.riha.rest.logic.util.JsonHelper;

public class TestHelper {

  public static <T> T getObjectFromClient(InputStream inputStream, Class<T> clazz) throws IOException {
    String json = readStream(inputStream);
    // System.out.println("JSON is" + json);
    // System.out.println("CLASS is " + clazz);
    T result = JsonHelper.GSON.fromJson(json, clazz);
    // System.out.println("RESULT is " + result);
    return result;
  }

  public static <T> T getObjectFromClient(InputStream inputStream, Type type) throws IOException {
    String json = readStream(inputStream);
    T result = JsonHelper.GSON.fromJson(json, type);
    return result;
  }

  public static <T> List<JsonObject> getJsonContentList(String json) throws IOException {
    // Type type = new TypeToken<List<clazz>>() {
    // }.getType();
    List<JsonObject> objectsList = (List<JsonObject>) JsonHelper.GSON.fromJson(json, new TypeToken<List<JsonObject>>() {
    }.getType());
    return objectsList;
  }

  public static String readStream(InputStream inputStream) throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
      }
      System.out.println(sb.toString());
      return sb.toString();
    }
  }

  public static Map<String, Integer> getResultMap(Response response) throws IOException {
    return (Map<String, Integer>) TestHelper.getObjectFromClient((InputStream) response.getEntity(),
        new TypeToken<HashMap<String, Integer>>() {
        }.getType());
  }

  public static <T> List<T> getObjectsFromClient(Response response) throws IOException {
    return TestHelper.getObjectFromClient((InputStream) response.getEntity(), new TypeToken<List<T>>() {
    }.getType());
  }
}
