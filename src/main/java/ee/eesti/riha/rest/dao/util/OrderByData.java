package ee.eesti.riha.rest.dao.util;

// TODO: Auto-generated Javadoc
/**
 * The Class OrderByData.
 */
public final class OrderByData {

  private String orderByField;
  private boolean isAsc;

  /**
   * Constructs data for: order by {data}. If field parsing fails, then returned data remains with default values.
   *
   * @param sortStringToParse represents field; - at the beginning of string indicates to sort in descending order;
   *          examples1: -modifiedDate; example2: modifiedDate
   * @param classRepresentingTable table to parse string for; filed must exist in that class
   * @param defaultOrderByField the default order by field
   * @param defaultIsAscVal the default is asc val
   * @return the order by data
   */
  public static OrderByData construct(String sortStringToParse, Class classRepresentingTable,
      String defaultOrderByField, boolean defaultIsAscVal) {

    OrderByData orderByData = new OrderByData(defaultOrderByField, defaultIsAscVal);
    if (sortStringToParse != null) {
      if (sortStringToParse.trim().startsWith("-")) {
        orderByData.setAsc(false);
        sortStringToParse = sortStringToParse.replaceFirst("-", "");
      }
      orderByData.setOrderByField(sortStringToParse);
    }

    return orderByData;

  }

  /**
   * Instantiates a new order by data.
   */
  private OrderByData() {
  }

  /**
   * Instantiates a new order by data.
   *
   * @param orderByField the order by field
   * @param isAsc the is asc
   */
  private OrderByData(String orderByField, boolean isAsc) {
    this.orderByField = orderByField;
    this.isAsc = isAsc;
  }

  /**
   * Gets the order by field.
   *
   * @return the order by field
   */
  public String getOrderByField() {
    return orderByField;
  }

  /**
   * Sets the order by field.
   *
   * @param aOrderByField the new order by field
   */
  public void setOrderByField(String aOrderByField) {
    this.orderByField = aOrderByField;
  }

  /**
   * Checks if is asc.
   *
   * @return true, if is asc
   */
  public boolean isAsc() {
    return isAsc;
  }

  /**
   * Sets the asc.
   *
   * @param aIsAsc the new asc
   */
  public void setAsc(boolean aIsAsc) {
    this.isAsc = aIsAsc;
  }

}
