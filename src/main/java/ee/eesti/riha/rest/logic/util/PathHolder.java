package ee.eesti.riha.rest.logic.util;

import ee.eesti.riha.rest.logic.Finals;

// TODO: Auto-generated Javadoc
/**
 * The Class PathHolder.
 */
public class PathHolder {

  public final String root;

  public final String tableName;

  public final String id;

  private static final int PATH_MAX_NUM_OF_PARTS = 3;

  // db/mytable/123
  // /db/mytable/123
  // db/mytable
  // /db/mytable
  // illegal: /db/mytable/123/111
  // illegal: /NOTdb/mytable/123
  /**
   * Instantiates a new path holder.
   *
   * @param path the path
   */
  public PathHolder(String path) {
    // test 1
    if (path == null || path.isEmpty()) {
      // LOG.info("expected string");
      throw new IllegalStateException("expected string");
    }

    // clean - remove trailing and leading /
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    String[] pathParts = path.split("/");

    // test 2 - now expect db/mytable/123 or db/mytable
    if (pathParts.length < 2 || pathParts.length > PATH_MAX_NUM_OF_PARTS) {
      // LOG.info("unexpected num of elements");
      throw new IllegalStateException("unexpected num of elements");
    }

    root = pathParts[0];
    // test 3 - expect constant root value
    if (!root.equals(Finals.PATH_ROOT)) {
      // LOG.info("unexpected root value");
      throw new IllegalStateException("unexpected root value");
    }

    tableName = pathParts[1];
    if (pathParts.length > 2) {
      id = pathParts[2];
      // test 4 - expect id to be number
      try {
        new Integer(id);
      } catch (NumberFormatException e) {
        // LOG.info("unexpected id value");
        throw new IllegalStateException("unexpected id value");
      }
    } else {
      id = null;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PathHolder [root=" + root + ", tableName=" + tableName + ", id=" + id + "]";
  }

}
