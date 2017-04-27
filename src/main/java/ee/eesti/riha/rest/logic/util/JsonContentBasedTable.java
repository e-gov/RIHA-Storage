package ee.eesti.riha.rest.logic.util;

import java.util.ArrayList;
import java.util.List;

import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;

/**
 * Json_content based table is table that contains field json_content. All the other fields that exist in table must
 * also exist in json_content as properties (fields always duplicated from json_content). If to update json_content
 * property then duplicated field should be updated. New entry is created from json_content, it is stored in field
 * json_content and json_content is returned when entry needs to be read.
 */
public final class JsonContentBasedTable {

  private static List<Class> jsonContentBasedTables = new ArrayList<>();

  private JsonContentBasedTable() {

  }

  static {
    jsonContentBasedTables.add(Main_resource.class);
    jsonContentBasedTables.add(Document.class);
    jsonContentBasedTables.add(Data_object.class);
    jsonContentBasedTables.add(Comment.class);
  }

  /**
   * Checks if is json content based table.
   *
   * @param classRepresentingTable the class representing table
   * @return true, if is json content based table
   */
  public static boolean isJsonContentBasedTable(Class classRepresentingTable) {
    return jsonContentBasedTables.contains(classRepresentingTable);
  }

}
