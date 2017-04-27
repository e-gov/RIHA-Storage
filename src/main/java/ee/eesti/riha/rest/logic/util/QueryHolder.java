package ee.eesti.riha.rest.logic.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Finals;

// TODO: Auto-generated Javadoc
/**
 * The Class QueryHolder.
 */
// {“op”:”get”,”path”:”db/mytable/123”,”token”:”abca”}
public class QueryHolder {

  private String op;

  private String path;

  private String token;

  private Integer limit;

  private Integer offset;

  private Object filter;

  private String sort;

  private String fields;

  private JsonElement data;

  private JsonElement asJson;

  public static final String FIELDS = "fields";

  /**
   * Creates the.
   *
   * @param gson the gson
   * @param json the json
   * @return the query holder
   * @throws RihaRestException the riha rest exception
   */
  public static QueryHolder create(Gson gson, String json) throws RihaRestException {
    JsonObject queryJson = new JsonParser().parse(json).getAsJsonObject();
    // remove fields because Gson doesn't allow to save json array as a string
    JsonElement fields = queryJson.get(FIELDS);
    queryJson.remove(FIELDS);
    mustBeInt(queryJson, "limit");
    mustBeInt(queryJson, "offset");
    QueryHolder queryHolder = gson.fromJson(queryJson, QueryHolder.class);

    if (fields != null) {
      queryJson.add(FIELDS, fields);
      // set fields array as string
      queryHolder.setFields(fields.toString());
    }
    queryHolder.setAsJson(queryJson);
    return queryHolder;
  }

  /**
   * Must be int.
   *
   * @param queryJson the query json
   * @param fieldName the field name
   * @throws RihaRestException the riha rest exception
   */
  private static void mustBeInt(JsonObject queryJson, String fieldName) throws RihaRestException {
    try {
      JsonElement intJson = queryJson.get(fieldName);
      if (intJson != null) {
        intJson.getAsInt();
      }
    } catch (ClassCastException | NumberFormatException e) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.INPUT_EXPECTED_INTEGER);
      error.setErrmsg(ErrorCodes.INPUT_EXPECTED_INTEGER_MSG + fieldName + " - " + queryJson.get(fieldName));
      throw new RihaRestException(error);
    }
  }

  /**
   * Instantiates a new query holder.
   */
  public QueryHolder() {
  }

  /**
   * Instantiates a new query holder.
   *
   * @param op the op
   * @param path the path
   * @param token the token
   * @param limit the limit
   * @param offset the offset
   * @param filter the filter
   * @param sort the sort
   * @param fields the fields
   */
  public QueryHolder(String op, String path, String token, Integer limit, Integer offset, Object filter, String sort,
      String fields) {
    this.op = op;
    this.path = path;
    this.token = token;
    this.limit = limit;
    this.offset = offset;
    this.filter = filter;
    this.sort = sort;
    this.fields = fields;
  }

  /**
   * Gets the op.
   *
   * @return the op
   */
  public String getOp() {
    return op;
  }

  /**
   * Sets the op.
   *
   * @param aOp the new op
   */
  public void setOp(String aOp) {
    op = aOp;
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path.
   *
   * @param aPath the new path
   */
  public void setPath(String aPath) {
    path = aPath;
  }

  /**
   * Gets the token.
   *
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Sets the token.
   *
   * @param aToken the new token
   */
  public void setToken(String aToken) {
    token = aToken;
  }

  /**
   * Gets the limit.
   *
   * @return the limit
   */
  public Integer getLimit() {
    if (limit == null) {
      return Finals.NUM_OF_ITEMS_IN_RESULT_ALLOWED;
    }
    return limit;
  }

  /**
   * Sets the limit.
   *
   * @param aLimit the new limit
   */
  public void setLimit(Integer aLimit) {
    limit = aLimit;
  }

  /**
   * Gets the offset.
   *
   * @return the offset
   */
  public Integer getOffset() {
    if (offset == null) {
      return 0;
    }
    return offset;
  }

  /**
   * Sets the offset.
   *
   * @param aOffset the new offset
   */
  public void setOffset(Integer aOffset) {
    offset = aOffset;
  }

  /**
   * Gets the filter.
   *
   * @return the filter
   */
  public Object getFilter() {
    return filter;
  }

  /**
   * Sets the filter.
   *
   * @param aFilter the new filter
   */
  public void setFilter(Object aFilter) {
    filter = aFilter;
  }

  /**
   * Gets the sort.
   *
   * @return the sort
   */
  public String getSort() {
    return sort;
  }

  /**
   * Sets the sort.
   *
   * @param aSort the new sort
   */
  public void setSort(String aSort) {
    sort = aSort;
  }

  /**
   * Gets the fields.
   *
   * @return the fields
   */
  public String getFields() {
    return fields;
  }

  /**
   * Sets the fields.
   *
   * @param aFields the new fields
   */
  public void setFields(String aFields) {
    fields = aFields;
  }

  /**
   * Gets the data.
   *
   * @return the data
   */
  public JsonElement getData() {
    return data;
  }

  /**
   * Sets the data.
   *
   * @param aData the new data
   */
  public void setData(JsonElement aData) {
    data = aData;
  }

  /**
   * Gets the as json.
   *
   * @return the as json
   */
  public JsonElement getAsJson() {
    return asJson;
  }

  /**
   * Sets the as json.
   *
   * @param aAsJson the new as json
   */
  public void setAsJson(JsonElement aAsJson) {
    asJson = aAsJson;
  }

}
