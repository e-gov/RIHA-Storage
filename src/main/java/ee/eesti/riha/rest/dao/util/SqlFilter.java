package ee.eesti.riha.rest.dao.util;

import java.text.ParseException;
import java.util.*;

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
      filterExpr = itemPrefix + fieldHolder.getDatabaseColumnName() + " IS NULL";

    } else if (filterComponent.getOperator().equals("isnotnull")) {
      filterExpr = itemPrefix + fieldHolder.getDatabaseColumnName() + " IS NOT NULL";

    } else if (filterComponent.getOperator().equals("null_or_<=")) {

      filterExpr = "(" + itemPrefix + fieldHolder.getDatabaseColumnName() + " <= :" + (opRight + index) + " OR " + itemPrefix
          + fieldHolder.getDatabaseColumnName() + " IS NULL )";
      params.put(opRight + index, Double.valueOf(filterComponent.getOperandRight()).intValue());

    } else if (fieldHolder.getType().getName().equals(Integer.class.getName())) {
      // get Double value because may contain decimal point then get intValue
      filterExpr = itemPrefix + fieldHolder.getDatabaseColumnName() + filterComponent.getOperator()
          + Double.valueOf(filterComponent.getOperandRight()).intValue();
    } else if (filterComponent.getOperator().equals("null_or_>")) {

      filterExpr = "(" + itemPrefix + fieldHolder.getDatabaseColumnName() + " > :" + (opRight + index) + " OR " + itemPrefix
          + fieldHolder.getDatabaseColumnName() + " IS NULL )";
      if (fieldHolder.getType().getName().equals(Date.class.getName())) {
        params.put(opRight + index, DateHelper.fromString(filterComponent.getOperandRight()));
      } else {
        throw new IllegalArgumentException("This operator (null_or_>) is meant for end_date");
      }
    } else if (fieldHolder.getType().equals(UUID.class)) {
      filterExpr = itemPrefix + fieldHolder.getDatabaseColumnName() + " = " + (":" + (opRight + index) + "\\:\\:uuid");
      params.put(opRight + index, filterComponent.getOperandRight());
    } else {
      // by default treat as string (also applies to date)
      filterExpr = itemPrefix + fieldHolder.getDatabaseColumnName() + " " + filterComponent.getOperator() + " :" + (opRight + index);
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

    FieldTypeHolder fieldHolder = null;
    try {
      fieldHolder = FieldTypeHolder.construct(clazz, filterComponent.getOperandLeft());
    } catch (NoSuchFieldException | IllegalAccessException e) {
      LOG.error("Exception in SQL construction, cannot determine field for " +  filterComponent.getOperandLeft(), e);
    }

    // if (org.apache.commons.lang3.StringUtils.isNumeric(fc.getOperandRight())) {
    String updatedOperandLeft = fieldHolder.getDatabaseColumnName().replaceAll("\\.", ",");
    String jsonTextFieldName = "{" + updatedOperandLeft + "}";
    String jsonFieldNameParameter = "jField" + index;

    if (filterComponent.getOperator().equals("isnull")) {
      filterExpr = "(" + ITEM_PREFIX + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter + "\\:\\:text[]" + ") IS NULL";
      params.put(jsonFieldNameParameter, jsonTextFieldName);
    } else if (filterComponent.getOperator().equals("isnotnull")) {
      filterExpr = "(" + ITEM_PREFIX + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter + "\\:\\:text[]" + ") IS NOT NULL";
      params.put(jsonFieldNameParameter, jsonTextFieldName);
    } else if (filterComponent.getOperator().equals("null_or_<=")) {
      String jsonNextFieldNameParameter = "jFieldNext" + index;
      filterExpr = "(cast(" + ITEM_PREFIX + Finals.JSON_CONTENT + " ->> :" + jsonFieldNameParameter + " AS int) <= :"
              + (opRight + index) + " OR " + ITEM_PREFIX + Finals.JSON_CONTENT + " ->> :"
              + jsonNextFieldNameParameter + " IS NULL )";

      params.put(opRight + index, Double.valueOf(filterComponent.getOperandRight()).intValue());
      params.put(jsonFieldNameParameter, updatedOperandLeft);
      params.put(jsonNextFieldNameParameter, updatedOperandLeft);
    } else if (filterComponent.getOperator().equals("jilike")) {
      filterExpr = "(" + ITEM_PREFIX + Finals.JSON_CONTENT + " #>> " + ":" + jsonFieldNameParameter
              + "\\:\\:text[]) ilike :" + (opRight + index);

      params.put(jsonFieldNameParameter, jsonTextFieldName);
      params.put(opRight + index, filterComponent.getOperandRight());
    } else if (filterComponent.getOperator().equals("jarr")) {
      String jsonNextFieldNameParameter = "jFieldNext" + index;

      filterExpr = "EXISTS(SELECT FROM jsonb_array_elements_text((" + ITEM_PREFIX + Finals.JSON_CONTENT + " #> :"
              + jsonFieldNameParameter + "\\:\\:text[])) WHERE " + ITEM_PREFIX + Finals.JSON_CONTENT + " #>> :" + jsonNextFieldNameParameter
              + "\\:\\:text[] is not null AND value ilike :" + (opRight + index) + ")";

      params.put(jsonFieldNameParameter, jsonTextFieldName);
      params.put(jsonNextFieldNameParameter, jsonTextFieldName);
      params.put(opRight + index, filterComponent.getOperandRight());
    } else if (StringHelper.isNumber(filterComponent.getOperandRight())) {
      filterExpr = "cast(" + ITEM_PREFIX + Finals.JSON_CONTENT + " ->> :" + jsonFieldNameParameter + " AS int) "
              + filterComponent.getOperator() + " :" + (opRight + index);
      params.put(opRight + index, Double.valueOf(filterComponent.getOperandRight()));
      params.put(jsonFieldNameParameter, updatedOperandLeft);
    } else if (filterComponent.getOperator().equals("?&")) {

      if (JsonHelper.isValidJson(filterComponent.getOperandRight()) && JsonHelper.isJsonArray(filterComponent.getOperandRight())) {
        // workaround can't find a way to escape '?' in query
        // use without ? instead
        // https://www.postgresql.org/docs/9.5/static/functions-json.html
        filterExpr = ITEM_PREFIX + Finals.JSON_CONTENT + " -> :" + jsonFieldNameParameter
                + " @> cast(:" + (opRight + index) + " AS jsonb)";
        params.put(opRight + index, filterComponent.getOperandRight());
        params.put(jsonFieldNameParameter, updatedOperandLeft);
      } else {
        RihaRestError error = new RihaRestError();
        error.setErrcode(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY);
        error.setErrmsg(ErrorCodes.FILTER_OP_VALUE_MUST_BE_ARRAY_MSG);
        error.setErrtrace("Filter: " + filterComponent);
        throw new RihaRestException(error);
      }

    } else if (filterComponent.getOperator().equals("null_or_>")) {
      String jsonNextFieldNameParameter = "jFieldNext" + index;
      filterExpr = "(" + ITEM_PREFIX + Finals.JSON_CONTENT + " ->> :" + jsonFieldNameParameter + " > :" + (opRight + index)
              + " OR " + ITEM_PREFIX + Finals.JSON_CONTENT + " ->> :" + jsonNextFieldNameParameter + " IS NULL )";

      params.put(opRight + index, filterComponent.getOperandRight());
      params.put(jsonFieldNameParameter, updatedOperandLeft);
      params.put(jsonNextFieldNameParameter, updatedOperandLeft);
    } else {
      filterExpr = ITEM_PREFIX + Finals.JSON_CONTENT + " ->> :" + jsonFieldNameParameter + " "
              + filterComponent.getOperator() + " :" + (opRight + index);
      params.put(opRight + index, filterComponent.getOperandRight());
      params.put(jsonFieldNameParameter, updatedOperandLeft);
    }
    // allFilters.add(ITEM_PREFIX + jsonField + " " + fc.getOperator() + " :" + (opRight + i));
    // params.put(opRight + i, fc.getOperandRight());

    return filterExpr;
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
