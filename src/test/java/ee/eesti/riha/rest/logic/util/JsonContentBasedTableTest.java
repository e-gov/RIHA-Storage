package ee.eesti.riha.rest.logic.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;

public class JsonContentBasedTableTest {

  private static List<Class> knownJsonContentBasedTables = new ArrayList<Class>();
  private static Class notJsonContentTableTrivialExample = String.class;

  static {
    knownJsonContentBasedTables.add(Main_resource.class);
    knownJsonContentBasedTables.add(Document.class);
    knownJsonContentBasedTables.add(Data_object.class);
    knownJsonContentBasedTables.add(Comment.class);
  }

  @Test
  public void testIsJsonContentBasedTable() throws Exception {

    assertTrue(JsonContentBasedTable.isJsonContentBasedTable(knownJsonContentBasedTables.get(0)));
    assertTrue(JsonContentBasedTable.isJsonContentBasedTable(knownJsonContentBasedTables.get(1)));
    assertTrue(JsonContentBasedTable.isJsonContentBasedTable(knownJsonContentBasedTables.get(2)));
    assertTrue(JsonContentBasedTable.isJsonContentBasedTable(knownJsonContentBasedTables.get(3)));
    assertFalse(JsonContentBasedTable.isJsonContentBasedTable(notJsonContentTableTrivialExample));

  }

}
