package ee.eesti.riha.rest.dao.util;

import java.text.ParseException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.util.DateHelper;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.logic.util.Tuple;
import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.readonly.Kind;

@Component
public class SqlFilter {

  @Autowired
  KindRepository kindRepository;

  private static final Logger LOG = LoggerFactory.getLogger(SqlFilter.class);

  public static final String ITEM_PREFIX = "item.";

  /**
   * Construct sql filter.
   *
   * @param filterComponents the filter components
   * @param clazz the clazz
   * @return the tuple
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   * @throws ParseException the parse exception
   */
  public <T> Tuple<String, Map<String, Object>> constructSqlFilter(List<FilterComponent> filterComponents,
      Class<T> clazz)
      throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ParseException {
    List<String> allFilters = new ArrayList<>();
    Map<String, Object> params = new HashMap<>();
    String joinedAsOneStr = "";
    String opRight = "fcOpr";

    for (int i = 0; i < filterComponents.size(); i++) {
      FilterComponent fc = filterComponents.get(i);
      // detect type of field (map parameter name to field of
      // given class)
      FieldTypeHolder fieldHolder = FieldTypeHolder.construct(clazz, fc.getOperandLeft());
      // fc.getOperandLeft() is already checked before this method call
      // it is certain that it is a field in sql table

      String filterExpr = null;
      String itemPrefix = ITEM_PREFIX;

      fc = replaceKindWithKindId(fc, clazz);
      fieldHolder = FieldTypeHolder.construct(clazz, fc.getOperandLeft());

      if (fc.getOperator().equals("isnull")) {
        filterExpr = itemPrefix + fc.getOperandLeft() + " IS NULL";

      } else if (fc.getOperator().equals("isnotnull")) {
        filterExpr = itemPrefix + fc.getOperandLeft() + " IS NOT NULL";

      } else if (fc.getOperator().equals("null_or_<=")) {

        filterExpr = "(" + itemPrefix + fc.getOperandLeft() + " <= :" + (opRight + i) + " OR " + itemPrefix
            + fc.getOperandLeft() + " IS NULL )";
        params.put(opRight + i, Double.valueOf(fc.getOperandRight()).intValue());

      } else if (fieldHolder.getType().getName().equals(Integer.class.getName())) {
        // get Double value because may contain decimal point then get intValue
        filterExpr = itemPrefix + fc.getOperandLeft() + fc.getOperator()
            + Double.valueOf(fc.getOperandRight()).intValue();
      } else if (fc.getOperator().equals("null_or_>")) {

        filterExpr = "(" + itemPrefix + fc.getOperandLeft() + " > :" + (opRight + i) + " OR " + itemPrefix
            + fc.getOperandLeft() + " IS NULL )";
        if (fieldHolder.getType().getName().equals(Date.class.getName())) {
          params.put(opRight + i, DateHelper.fromString(fc.getOperandRight()));
        } else {
          throw new IllegalArgumentException("This operator (null_or_>) is meant for end_date");
        }
      } else if (fieldHolder.getType().equals(UUID.class)) {
        filterExpr = itemPrefix + fc.getOperandLeft() + " = " + (":" + (opRight + i) + "\\:\\:uuid");
        params.put(opRight + i, fc.getOperandRight());
      } else {
        // by default treat as string (also applies to date)
        filterExpr = itemPrefix + fc.getOperandLeft() + " " + fc.getOperator() + " :" + (opRight + i);
        if (fieldHolder.getType().getName().equals(Date.class.getName())) {
          params.put(opRight + i, DateHelper.fromString(fc.getOperandRight()));
        } else {
          params.put(opRight + i, fc.getOperandRight());
        }
      }

      allFilters.add(filterExpr);
    }
    joinedAsOneStr = StringUtils.join(allFilters, " AND ");
    LOG.info("SQL FILTER: " + joinedAsOneStr + " params " + params);
    return new Tuple<>(joinedAsOneStr, params);
  }

  /**
   * Construct sql over json filter.
   *
   * @param filterComponents the filter components
   * @param clazz the clazz
   * @return the tuple
   * @throws RihaRestException the riha rest exception
   */
  public <T> Tuple<String, Map<String, Object>> constructSqlOverJsonFilter(List<FilterComponent> filterComponents,
      Class<T> clazz) throws RihaRestException {
    List<String> allFilters = new ArrayList<>();
    String joinedAsOneStr = "";
    String opRight = "fcOprJson";
    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < filterComponents.size(); i++) {
      FilterComponent fc = filterComponents.get(i);
      String jsonField = "";
      // fc.getOperandLeft() is already checked before this method call
      // it is certain that it is a field in sql table json column

      // change kind to kind_id
      fc = replaceKindWithKindId(fc, clazz);

      // if (org.apache.commons.lang3.StringUtils.isNumeric(fc.getOperandRight())) {
      if (fc.getOperator().equals("isnull")) {
        jsonField = Finals.JSON_CONTENT + "->>" + "'" + fc.getOperandLeft() + "'";
        allFilters.add("item." + jsonField + " IS NULL");

      } else if (fc.getOperator().equals("isnotnull")) {
        jsonField = Finals.JSON_CONTENT + "->>" + "'" + fc.getOperandLeft() + "'";
        allFilters.add("item." + jsonField + " IS NOT NULL");

      } else if (fc.getOperator().equals("null_or_<=")) {
        jsonField = Finals.JSON_CONTENT + "->>" + "'" + fc.getOperandLeft() + "'";
        allFilters.add("(cast(item." + jsonField + " AS int) <= :" + (opRight + i) + " OR item." + jsonField
            + " IS NULL )");

        params.put(opRight + i, Double.valueOf(fc.getOperandRight()).intValue());
      } else if (fc.getOperator().equals("jilike")) {
        String updatedOperandLeft = fc.getOperandLeft().replaceAll("\\.", ",");
        String jsonFieldName = "{" + updatedOperandLeft + "}";

        String jsonFieldNameParameter = "jField" + i;
        allFilters.add("(item." + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter + "\\:\\:text[]) ilike :" + (opRight + i));

        params.put(jsonFieldNameParameter, jsonFieldName);
        params.put(opRight + i, fc.getOperandRight());
      } else if (StringHelper.isNumber(fc.getOperandRight())) {
        jsonField = Finals.JSON_CONTENT + "->>" + "'" + fc.getOperandLeft() + "'";
        allFilters.add("cast(item." + jsonField + " AS int) " + fc.getOperator() + " :" + (opRight + i));
        params.put(opRight + i, Double.valueOf(fc.getOperandRight()));
      } else if (fc.getOperator().equals("?&")) {

        if (JsonHelper.isValidJson(fc.getOperandRight()) && JsonHelper.isJsonArray(fc.getOperandRight())) {
          jsonField = Finals.JSON_CONTENT + "->" + "'" + fc.getOperandLeft() + "'";
          // workaround can't find a way to escape '?' in query
          // use without ? instead
          // https://www.postgresql.org/docs/9.5/static/functions-json.html
          allFilters.add("item." + jsonField + " @> cast(:" + (opRight + i) + " AS jsonb)");
          params.put(opRight + i, fc.getOperandRight());
        } else {
          RihaRestError error = new RihaRestError();
          error.setErrcode(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY);
          error.setErrmsg(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY_MSG);
          error.setErrtrace("Filter: " + fc);
          throw new RihaRestException(error);
        }

      } else if (fc.getOperator().equals("null_or_>")) {
        jsonField = Finals.JSON_CONTENT + "->>" + "'" + fc.getOperandLeft() + "'";
        allFilters.add("(item." + jsonField + " > :" + (opRight + i) + " OR item." + jsonField + " IS NULL )");

        params.put(opRight + i, fc.getOperandRight());

      } else {
        jsonField = Finals.JSON_CONTENT + "->>" + "'" + fc.getOperandLeft() + "'";
        allFilters.add("item." + jsonField + " " + fc.getOperator() + " :" + (opRight + i));
        params.put(opRight + i, fc.getOperandRight());
      }
      // allFilters.add("item." + jsonField + " " + fc.getOperator() + " :" + (opRight + i));
      // params.put(opRight + i, fc.getOperandRight());
    }
    joinedAsOneStr = StringUtils.join(allFilters, " AND ");
    LOG.info("SQL JSON FILTER: " + joinedAsOneStr + " params " + params);
    return new Tuple<>(joinedAsOneStr, params);
  }

  private <T> FilterComponent replaceKindWithKindId(FilterComponent fc, Class<T> clazz) {
    // change kind to kind_id, because kind will be removed and used only in UI in the future
    if (fc.getOperandLeft().toLowerCase().equals(Finals.KIND) && clazz != Comment.class) {
      fc.setOperandLeft(Finals.KIND_ID);
      if (fc.getOperandRight() != null) {
        Kind kind = kindRepository.getByName(fc.getOperandRight());
        if (kind != null) {
          fc.setOperandRight("" + kind.getKind_id());
        } else {
          LOG.info("No kind_id found for given name, set kind_id = -1");
          fc.setOperandRight("-1");
        }
      }
    }
    return fc;
  }
}
