package ee.eesti.riha.rest.logic.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.PathHolder;

public class PathHolderTest {

  PathHolder pathHolder;

  @Test
  public void testPathHolder_digestableFormatForGetById() {

    pathHolder = new PathHolder("db/mytable/123");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertTrue(pathHolder.id.equals("123"));
    pathHolder = new PathHolder("/db/mytable/123");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertTrue(pathHolder.id.equals("123"));
    pathHolder = new PathHolder("db/mytable/123/");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertTrue(pathHolder.id.equals("123"));
    pathHolder = new PathHolder("/db/mytable/123/");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertTrue(pathHolder.id.equals("123"));

  }

  @Test
  public void testPathHolder_digestableFormatForGetAll() {

    pathHolder = new PathHolder("db/mytable");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    // System.out.println(pathHolder.id);
    assertNull(pathHolder.id);
    pathHolder = new PathHolder("/db/mytable");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertNull(pathHolder.id);
    pathHolder = new PathHolder("db/mytable/");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertNull(pathHolder.id);
    pathHolder = new PathHolder("/db/mytable/");
    assertTrue(pathHolder.root.equals(Finals.PATH_ROOT));
    assertTrue(pathHolder.tableName.equals("mytable"));
    assertNull(pathHolder.id);

  }

  // testing some examples that should not accept
  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat() throws Exception {
    pathHolder = new PathHolder("NOTdb/mytable/");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat1() throws Exception {
    pathHolder = new PathHolder("/NOTdb/mytable/");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat2() throws Exception {
    pathHolder = new PathHolder(null);
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat3() throws Exception {
    pathHolder = new PathHolder("");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat4() throws Exception {
    pathHolder = new PathHolder("a");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat5() throws Exception {
    pathHolder = new PathHolder("/db/");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat6() throws Exception {
    pathHolder = new PathHolder("/db///");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat7() throws Exception {
    pathHolder = new PathHolder("///");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_indigestableFormat8() throws Exception {
    // too many parameters
    pathHolder = new PathHolder("db/tablenamehere/1/2");
  }

  @Test(expected = IllegalStateException.class)
  public void testPathHolder_whenWhereNumberIsExpectedAsIdAndNoNumProvided_thenException() throws Exception {
    // id should be number
    pathHolder = new PathHolder("db/tablenamehere/a");
  }

}
