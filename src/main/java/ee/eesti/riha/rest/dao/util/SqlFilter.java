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
   * @param filterComponent the filter component
   * @param clazz the clazz
   * @param params map that contains Hibernate parameters
   * @param index unique index to use in parametrized properties
   * @return created filter as string
   * @throws NoSuchFieldException the no such field exception
   * @throws SecurityException the security exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   * @throws ParseException the parse exception
   */
  public <T> String constructSqlFilter(FilterComponent filterComponent, Class<T> clazz,
             Map<String, Object> params, int index)
          throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ParseException {
    String opRight = "fcOpr";

    // detect type of field (map parameter name to field of
    // given class)
    FieldTypeHolder fieldHolder = FieldTypeHolder.construct(clazz, filterComponent.getOperandLeft());
    // fc.getOperandLeft() is already checked before this method call
    // it is certain that it is a field in sql table

    String filterExpr = null;
    String itemPrefix = ITEM_PREFIX;

    filterComponent = replaceKindWithKindId(filterComponent, clazz);
    fieldHolder = FieldTypeHolder.construct(clazz, filterComponent.getOperandLeft());

    if (filterComponent.getOperator().equals("isnull")) {
      filterExpr = itemPrefix + filterComponent.getOperandLeft() + " IS NULL";

    } else if (filterComponent.getOperator().equals("isnotnull")) {
      filterExpr = itemPrefix + filterComponent.getOperandLeft() + " IS NOT NULL";

    } else if (filterComponent.getOperator().equals("null_or_<=")) {

      filterExpr = "(" + itemPrefix + filterComponent.getOperandLeft() + " <= :" + (opRight + index) + " OR " + itemPrefix
          + filterComponent.getOperandLeft() + " IS NULL )";
      params.put(opRight + index, Double.valueOf(filterComponent.getOperandRight()).intValue());

    } else if (fieldHolder.getType().getName().equals(Integer.class.getName())) {
      // get Double value because may contain decimal point then get intValue
      filterExpr = itemPrefix + filterComponent.getOperandLeft() + filterComponent.getOperator()
          + Double.valueOf(filterComponent.getOperandRight()).intValue();
    } else if (filterComponent.getOperator().equals("null_or_>")) {

      filterExpr = "(" + itemPrefix + filterComponent.getOperandLeft() + " > :" + (opRight + index) + " OR " + itemPrefix
          + filterComponent.getOperandLeft() + " IS NULL )";
      if (fieldHolder.getType().getName().equals(Date.class.getName())) {
        params.put(opRight + index, DateHelper.fromString(filterComponent.getOperandRight()));
      } else {
        throw new IllegalArgumentException("This operator (null_or_>) is meant for end_date");
      }
    } else if (fieldHolder.getType().equals(UUID.class)) {
      filterExpr = itemPrefix + filterComponent.getOperandLeft() + " = " + (":" + (opRight + index) + "\\:\\:uuid");
      params.put(opRight + index, filterComponent.getOperandRight());
    } else {
      // by default treat as string (also applies to date)
      filterExpr = itemPrefix + filterComponent.getOperandLeft() + " " + filterComponent.getOperator() + " :" + (opRight + index);
      if (fieldHolder.getType().getName().equals(Date.class.getName())) {
        params.put(opRight + index, DateHelper.fromString(filterComponent.getOperandRight()));
      } else {
        params.put(opRight + index, filterComponent.getOperandRight());
      }
    }

    return filterExpr;
  }

  /**
   * Construct sql over json filter.
   *
   * @param filterComponent the filter component
   * @param clazz the clazz
   * @param params map that contains Hibernate parameters
   * @param index unique index to use in parametrized properties
   * @return created filter as string
   * @throws RihaRestException the riha rest exception
   */
  public <T> String constructSqlOverJsonFilter(FilterComponent filterComponent, Class<T> clazz,
             Map<String, Object> params, int index) throws RihaRestException {
    String filterExpr = null;
    String opRight = "fcOprJson";

    String jsonField = "";
    // fc.getOperandLeft() is already checked before this method call
    // it is certain that it is a field in sql table json column

    // change kind to kind_id
    filterComponent = replaceKindWithKindId(filterComponent, clazz);

    // if (org.apache.commons.lang3.StringUtils.isNumeric(fc.getOperandRight())) {
    if (filterComponent.getOperator().equals("isnull")) {
      String jsonFieldNameParameter = "jField" + index;
      filterExpr = "(" + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter + "\\:\\:text[]" + ") IS NULL";
      params.put(jsonFieldNameParameter, createJsonPath(filterComponent.getOperandLeft()));
    } else if (filterComponent.getOperator().equals("isnotnull")) {
      String jsonFieldNameParameter = "jField" + index;
      filterExpr = "(" + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter + "\\:\\:text[]" + ") IS NOT NULL";
      params.put(jsonFieldNameParameter, createJsonPath(filterComponent.getOperandLeft()));
    } else if (filterComponent.getOperator().equals("null_or_<=")) {
      jsonField = Finals.JSON_CONTENT + "->>" + "'" + filterComponent.getOperandLeft() + "'";
      filterExpr = "(cast(" + ITEM_PREFIX + jsonField + " AS int) <= :" + (opRight + index) + " OR " + ITEM_PREFIX + jsonField
          + " IS NULL )";

      params.put(opRight + index, Double.valueOf(filterComponent.getOperandRight()).intValue());
    } else if (filterComponent.getOperator().equals("jilike")) {
      String jsonFieldNameParameter = "jField" + index;
      filterExpr = "(" + ITEM_PREFIX + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter + "\\:\\:text[]) ilike :" + (opRight + index);
      params.put(jsonFieldNameParameter, createJsonPath(filterComponent.getOperandLeft()));
      params.put(opRight + index, filterComponent.getOperandRight());
    } else if (filterComponent.getOperator().equals("jarr")) {
      String jsonFieldAndArrayParametersValues = createJsonPath(filterComponent.getOperandLeft());

      String jsonFieldNameParameter = "jFieldParam";
      String jsonArrayTextValueParameter = "jArrayParam";

      filterExpr = "EXISTS(SELECT FROM jsonb_array_elements_text((" + ITEM_PREFIX + Finals.JSON_CONTENT + " #> :"
              + jsonFieldNameParameter + "\\:\\:text[])) WHERE " + ITEM_PREFIX + Finals.JSON_CONTENT + " #>> :" + jsonArrayTextValueParameter
              + "\\:\\:text[] is not null AND value ilike :" + (opRight + index) + ")";

      params.put(jsonFieldNameParameter, jsonFieldAndArrayParametersValues);
      params.put(jsonArrayTextValueParameter, jsonFieldAndArrayParametersValues);
      params.put(opRight + index, filterComponent.getOperandRight());
    } else if (StringHelper.isNumber(filterComponent.getOperandRight())) {
      jsonField = Finals.JSON_CONTENT + "->>" + "'" + filterComponent.getOperandLeft() + "'";
      filterExpr = "cast(" + ITEM_PREFIX  + jsonField + " AS int) " + filterComponent.getOperator() + " :" + (opRight + index);
      params.put(opRight + index, Double.valueOf(filterComponent.getOperandRight()));
    } else if (filterComponent.getOperator().equals("?&")) {

      if (JsonHelper.isValidJson(filterComponent.getOperandRight()) && JsonHelper.isJsonArray(filterComponent.getOperandRight())) {
        jsonField = Finals.JSON_CONTENT + "->" + "'" + filterComponent.getOperandLeft() + "'";
        // workaround can't find a way to escape '?' in query
        // use without ? instead
        // https://www.postgresql.org/docs/9.5/static/functions-json.html
        filterExpr = ITEM_PREFIX + jsonField + " @> cast(:" + (opRight + index) + " AS jsonb)";
        params.put(opRight + index, filterComponent.getOperandRight());
      } else {
        RihaRestError error = new RihaRestError();
        error.setErrcode(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY);
        error.setErrmsg(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY_MSG);
        error.setErrtrace("Filter: " + filterComponent);
        throw new RihaRestException(error);
      }

    } else if (filterComponent.getOperator().equals("null_or_>")) {
      jsonField = Finals.JSON_CONTENT + "->>" + "'" + filterComponent.getOperandLeft() + "'";
      filterExpr = "(" + ITEM_PREFIX + jsonField + " > :" + (opRight + index) + " OR " + ITEM_PREFIX + jsonField + " IS NULL )";

      params.put(opRight + index, filterComponent.getOperandRight());

    } else {
      jsonField = Finals.JSON_CONTENT + "->>" + "'" + filterComponent.getOperandLeft() + "'";
      filterExpr = ITEM_PREFIX + jsonField + " " + filterComponent.getOperator() + " :" + (opRight + index);
      params.put(opRight + index, filterComponent.getOperandRight());
    }
    // allFilters.add(ITEM_PREFIX + jsonField + " " + fc.getOperator() + " :" + (opRight + i));
    // params.put(opRight + i, fc.getOperandRight());

    return filterExpr;
  }

  private String createJsonPath(String dotSeparatedPath) {
    if (StringUtils.isBlank(dotSeparatedPath)) {
      return dotSeparatedPath;
    }

    return "{" + dotSeparatedPath.replaceAll("\\.", ",") + "}";
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
