package ee.eesti.riha.rest.dao.util;

import java.util.HashMap;
import java.util.Map;

import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;

// TODO: Auto-generated Javadoc
/**
 * One {@link FilterComponent} holds single filter unit (i.e., id < 4 -- where id being left operand, < operator, and 4
 * right operand) that would be placed after sql WHERE. Many {@link FilterComponent}s can be chained after same WHERE
 * when separated with AND, to produce more focused filter query.
 */
public class FilterComponent {

  // also field name
  private String operandLeft;

  private String operator;
  private String operandRight;

  private static final Map<String, Object> ALLOWED_OPERATORS = new HashMap<>();

  static {
    // update ErrorCodes also if changing this
    ALLOWED_OPERATORS.put("=", "ok");
    ALLOWED_OPERATORS.put(">", "ok");
    ALLOWED_OPERATORS.put("<", "ok");
    ALLOWED_OPERATORS.put(">=", "ok");
    ALLOWED_OPERATORS.put("<=", "ok");
    ALLOWED_OPERATORS.put("!=", "ok");
    ALLOWED_OPERATORS.put("<>", "ok");
    ALLOWED_OPERATORS.put("like", "ok");
    // psql special ignorecase like
    ALLOWED_OPERATORS.put("ilike", "ok");
    // postgres json operator
    // https://www.postgresql.org/docs/9.4/static/functions-json.html
    ALLOWED_OPERATORS.put("?&", "ok");

    // special operator to find current version
    ALLOWED_OPERATORS.put("null_or_>", "ok");
    // special operator to items with alloed access_restriction
    ALLOWED_OPERATORS.put("null_or_<=", "ok");

    ALLOWED_OPERATORS.put("isnull", "ok");
    ALLOWED_OPERATORS.put("isnotnull", "ok");
  }

  /**
   * Instantiates a new filter component.
   *
   * @param operandLeft the operand left
   * @param operator the operator
   * @param operandRight the operand right
   * @throws RihaRestException the riha rest exception
   */
  public FilterComponent(String operandLeft, String operator, String operandRight) throws RihaRestException {
    this.operandLeft = operandLeft;
    // this.operator = operator;
    setOperator(operator);
    this.operandRight = operandRight;
  }

  /**
   * Gets the operand left.
   *
   * @return the operand left
   */
  public String getOperandLeft() {
    return operandLeft;
  }

  /**
   * Sets the operand left.
   *
   * @param aOperandLeft the new operand left
   */
  public void setOperandLeft(String aOperandLeft) {
    this.operandLeft = aOperandLeft;
  }

  /**
   * Gets the operator.
   *
   * @return the operator
   */
  public String getOperator() {
    return operator;
  }

  /**
   * Sets the operator.
   *
   * @param aOperator the new operator
   * @throws RihaRestException the riha rest exception
   */
  public void setOperator(String aOperator) throws RihaRestException {
    if (ALLOWED_OPERATORS.containsKey(aOperator.toLowerCase())) {
      this.operator = aOperator;
    } else {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.SQL_NO_SUCH_OPERATOR_EXISTS);
      error.setErrmsg(ErrorCodes.SQL_NO_SUCH_OPERATOR_EXISTS_MSG + aOperator);
      throw new RihaRestException(error);
    }

  }

  /**
   * Gets the operand right.
   *
   * @return the operand right
   */
  public String getOperandRight() {
    return operandRight;
  }

  /**
   * Sets the operand right.
   *
   * @param aOperandRight the new operand right
   */
  public void setOperandRight(String aOperandRight) {
    this.operandRight = aOperandRight;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[" + operandLeft + "," + operator + "," + operandRight + "]";
  }

}
