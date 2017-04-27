package ee.eesti.riha.rest.logic.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonFieldsFiltererTest {

  String data = "{\"name\":\"asd asd\", \"address\":\"asd 123\", \"age\":55, "
      + "\"emails\": [\"asd\", \"dsfs\"], \"car\":{\"name\":\"ford\", \"age\":33}}";

  String dataArray = "[{\"name\":\"asd asd\", \"address\":\"asd 123\", \"age\":55, "
      + "\"emails\": [\"asd\", \"dsfs\"], \"car\":{\"name\":\"ford\", \"age\":33}},"
      + "{\"name\":\"asd asd asd\", \"address\":\"asd  asdfasdf  123\", \"age\":55, "
      + "\"emails\": [\"assdfgsdfgd\", \"dsfs\"], \"car\":{\"name\":\"ford asd\", \"age\":33}}]";

  String allFields = "[\"name\", \"address\", \"age\", \"emails\", \"car\"]";
  String someFields = "[\"name\", \"car\"]";
  String noFields = "[]";
  String wrongFields = "[\"zxc\", \"mnobg\"]";

  JsonObject jsonData = JsonHelper.getFromJson(data);
  JsonArray jsonArray = JsonHelper.GSON.fromJson(dataArray, JsonArray.class);
  JsonObject emptyData = JsonHelper.getFromJson("{}");
  JsonArray emptyDataArray = JsonHelper.GSON.fromJson("[]", JsonArray.class);
  JsonArray arrayOfEmptyData = JsonHelper.GSON.fromJson("[{}]", JsonArray.class);

  JsonArray allFieldsArray = JsonHelper.GSON.fromJson(allFields, JsonArray.class);
  JsonArray someFieldsArray = JsonHelper.GSON.fromJson(someFields, JsonArray.class);
  JsonArray noFieldsArray = JsonHelper.GSON.fromJson(noFields, JsonArray.class);
  JsonArray wrongFieldsArray = JsonHelper.GSON.fromJson(wrongFields, JsonArray.class);
  
  String testJson = "{\"aa\":\"asdasd\",\"bb\":\"gfhfhg\",\"cc\":[\"werwe\",\"asdas\"],"
      + "\"m_id\":33,\"foo\":{\"m_id\":33, \"y_id\":55, \"zaz\":\"ere\"},"
      + "\"bar\":[{\"y_id\":23, \"maz\":\"noo\", \"items\":[{\"ff\":\"fsef\", \"m_id\":345}]}]}";
  
  String testJsonImport = "{\"uri\": \"urn:test_info_sys_123:abc555YYYTESTEST\",\"url\": null,"
      + "\"kind\": \"infosystem\",\"kind_id\":234,\"name\": \"test_infosys_123_abc5555TEST\","
      + "\"owner\": \"19999992\",\"vesrion\":\"v3\",\"groups\": [\"asd\"],\"creator\": \"TEST_ISIKUKOOD\","
      + "\"classifiers\": [\"asdasd\"],\"main_resource_id\": 799051,\"old_id\":\"234234\","
      + "\"infosystem_status\": \"asutamine_sisestamisel\",\"default_main_resources\": ["
      + "{\"uri\": \"urn:fdc:riha.eesti.ee:2017:service:796748:xxxYYYTEST\",\"url\": null,"
      + "\"kind\": \"service\",\"name\": \"test_teenus_123_abcXXXTEST\",\"areas\": [],"
      + "\"owner\": \"70009646\",\"creator\": \"TEST_ISIKUKOOD\",\"kind_id\": 399,"
      + "\"version\": \"1\",\"old_id\":555,\"description\": \"test_test_test\","
      + "\"creation_date\": \"2017-01-10T13:14:15\",\"main_resource_id\": 799052,"
      + "\"main_resource_parent_id\": 799051,\"documents\": [{\"uri\": \"urn:test:test:doc:123123TEST\","
      + "\"kind\": \"document\",\"name\": \"testDoc123TEST\",\"owner\": \"TEST_ASUTUS\","
      + "\"content\": \"pZnlpbmdQcm9wZXJ0aWVzPjwvT2JqZWN0Pgo8L1NpZ25hdHVyZT4KCjwvU2lnbmVkRG9jPg=\","
      + "\"creator\": \"TEST_ISIKUKOOD\",\"kind_id\": 408,\"old_id\":123,"
      + "\"field_name\": \"documents\",\"document_id\": 240489,\"creation_date\": \"2017-01-10T13:14:15\","
      + "\"main_resource_id\": 799052}]}]}";
  
  List<String> testUnwantedFields = Arrays.asList("m_id", "y_id");
  List<String> testImportUnwantedFields = Arrays.asList("old_id", "kind_id", 
      "older_data", "main_resource_id", "data_object_id", "document_id");
  
  @Test(expected = NullPointerException.class)
  public void testNullFilter() {
    JsonFieldsFilterer.filter(jsonData, null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullData() {
    JsonFieldsFilterer.filter(null, jsonArray);
  }

  @Test()
  public void testEmptyData() {
    JsonObject result = (JsonObject) JsonFieldsFilterer.filter(emptyData, jsonArray);
    assertEquals(emptyData, result);
  }

  @Test()
  public void testEmptyDataArray() {
    JsonArray result = (JsonArray) JsonFieldsFilterer.filter(emptyDataArray, jsonArray);
    assertEquals(emptyDataArray, result);
  }

  @Test()
  public void testArrayOfEmptyData() {
    JsonArray result = (JsonArray) JsonFieldsFilterer.filter(arrayOfEmptyData, jsonArray);
    assertEquals(arrayOfEmptyData, result);
  }

  @Test
  public void testFilterKeepAll() {
    JsonObject result = (JsonObject) JsonFieldsFilterer.filter(jsonData, allFieldsArray);
    assertEquals(jsonData, result);
  }

  @Test
  public void testFilterKeepSome() {
    JsonObject result = (JsonObject) JsonFieldsFilterer.filter(jsonData, someFieldsArray);
    assertEquals(jsonData.get("name"), result.get("name"));
    assertEquals(jsonData.get("car"), result.get("car"));
    assertEquals(someFieldsArray.size(), result.entrySet().size());
  }

  @Test
  public void testFilterKeepNone() {
    JsonObject result = (JsonObject) JsonFieldsFilterer.filter(jsonData, noFieldsArray);
    assertEquals(noFieldsArray.size(), result.entrySet().size());
  }

  @Test
  public void testFilterWrongFields() {
    JsonObject result = (JsonObject) JsonFieldsFilterer.filter(jsonData, wrongFieldsArray);
    assertEquals(0, result.entrySet().size());
  }

  @Test
  public void testFilterKeepAllArray() {
    JsonArray result = (JsonArray) JsonFieldsFilterer.filter(jsonArray, allFieldsArray);
    assertEquals(jsonArray, result);
  }

  @Test
  public void testFilterKeepSomeArray() {
    JsonArray result = (JsonArray) JsonFieldsFilterer.filter(jsonArray, someFieldsArray);
    JsonObject arrayFirst = (JsonObject) jsonArray.get(0);
    JsonObject resultFirst = (JsonObject) result.get(0);

    assertEquals(arrayFirst.get("name"), resultFirst.get("name"));
    assertEquals(arrayFirst.get("car"), resultFirst.get("car"));
    assertEquals(someFieldsArray.size(), resultFirst.entrySet().size());
  }

  @Test
  public void testFilterKeepNoneArray() {
    JsonArray result = (JsonArray) JsonFieldsFilterer.filter(jsonArray, noFieldsArray);
    assertEquals(noFieldsArray.size(), ((JsonObject) result.get(0)).entrySet().size());
    assertEquals(noFieldsArray.size(), ((JsonObject) result.get(1)).entrySet().size());
  }

  @Test
  public void testFilterWrongFieldsArray() {
    JsonArray result = (JsonArray) JsonFieldsFilterer.filter(jsonArray, wrongFieldsArray);
    assertEquals(0, ((JsonObject) result.get(0)).entrySet().size());
    assertEquals(0, ((JsonObject) result.get(1)).entrySet().size());
  }

  
  @Test
  public void testRemoveIdFields() throws Exception {
    JsonObject jsonObject = JsonHelper.GSON.fromJson(testJson, JsonObject.class);
    ObjectNode objectNode = GsonToJacksonHelper.getJsonObjectOfGsonAsJsonNodeOfJackson(jsonObject);
    System.out.println(objectNode.toString());
    System.out.println("MODIFYING");
    JsonFieldsFilterer.removeIdFields(objectNode, testUnwantedFields);

    String json = objectNode.toString();
    System.out.println(json);
    for (String unwanted : testUnwantedFields) {
      assertFalse(json.contains(unwanted));
    }
  }
  
  @Test
  public void testRemoveIdFieldsWithMoreRealisticData() throws Exception {
    JsonObject jsonObject = JsonHelper.GSON.fromJson(testJsonImport, JsonObject.class);
    ObjectNode objectNode = GsonToJacksonHelper.getJsonObjectOfGsonAsJsonNodeOfJackson(jsonObject);
    System.out.println(objectNode.toString());
    System.out.println("MODIFYING");
    JsonFieldsFilterer.removeIdFields(objectNode, testImportUnwantedFields);

    String json = objectNode.toString();
    System.out.println(json);
    for (String unwanted : testImportUnwantedFields) {
      assertFalse(json.contains(unwanted));
    }
  }
}
