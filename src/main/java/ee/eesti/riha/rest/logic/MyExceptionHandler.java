package ee.eesti.riha.rest.logic;

import com.google.gson.JsonSyntaxException;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate5.HibernateJdbcException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * The Class MyExceptionHandler.
 */
public final class MyExceptionHandler {
  private static final Logger LOG = LoggerFactory.getLogger(MyExceptionHandler.class);
  
  private static final Integer VERSION_MAX_LENGTH = 10;

  private MyExceptionHandler() {
  }

  /**
   * To string.
   *
   * @param e the e
   * @return the string
   */
  public static String toString(Exception e) {
    StringWriter sw = new StringWriter();
    LOG.info("", new PrintWriter(sw));
    return sw.toString();
  }

  /**
   * Number format.
   *
   * @param e the e
   * @throws RihaRestException the riha rest exception
   */
  public static void numberFormat(Exception e) throws RihaRestException {
    throw new RihaRestException(numberFormatException(e));
  }

  /**
   * Number format.
   *
   * @param e the e
   * @param additionalMessage the additional message
   * @throws RihaRestException the riha rest exception
   */
  public static void numberFormat(Exception e, String additionalMessage) throws RihaRestException {
    RihaRestError error = numberFormatException(e);
    error.setErrmsg(error.getErrmsg() + " -- " + additionalMessage);
    throw new RihaRestException(error);
  }

  /**
   * Date format.
   *
   * @param e the e
   * @param additionalMessage the additional message
   * @throws RihaRestException the riha rest exception
   */
  public static void dateFormat(Exception e, String additionalMessage) throws RihaRestException {
    RihaRestError error = dateFormatException(e);
    error.setErrmsg(error.getErrmsg() + " -- " + additionalMessage);
    throw new RihaRestException(error);
  }

  /**
   * Date format exception.
   *
   * @param e the e
   * @return the riha rest error
   */
  private static RihaRestError dateFormatException(Exception e) {
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.DATE_FORMAT_ERROR);
    error.setErrmsg(ErrorCodes.DATE_FORMAT_ERROR_MSG);
    error.setErrtrace(toString(e));
    return error;
  }

  /**
   * Database error.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError databaseError(Exception e) {
    RihaRestError error = new RihaRestError();

    error.setErrcode(ErrorCodes.DB_CONNECTION_ERROR);
    error.setErrmsg(ErrorCodes.DB_CONNECTION_ERROR_MSG);
//    error.setErrmsg(ErrorCodes.DB_CONNECTION_ERROR_MSG + " :: " + e.getMessage());
    addCausePSQL(error, e);
    error.setErrtrace(toString(e));
    return error;
  }

  /**
   * Sql error.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError sqlError(Exception e) {
    RihaRestError error = new RihaRestError();

    error.setErrcode(ErrorCodes.SQL_ERROR);
    error.setErrmsg(ErrorCodes.SQL_ERROR_MSG);
//    error.setErrmsg(ErrorCodes.SQL_ERROR_MSG + " :: " + e.getMessage());
    addCausePSQL(error, e);
    error.setErrtrace(toString(e));
    return error;
  }

  /**
   * Constraint error.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError constraintError(Exception e) {
    RihaRestError error = new RihaRestError();
    String exceptionAsString = MyExceptionHandler.toString(e);
    // filter some common errors
    Pattern pattern = Pattern.compile("column \"(.*?)\" violates not-null constraint");
    Matcher matcher = pattern.matcher(exceptionAsString);

    LOG.info("" + e.getClass());
    LOG.info("" + e.getCause());

    System.out.println("CAUSE: " + e.getCause().toString());
    
    if (matcher.find()) {
      error.setErrcode(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING);
      error.setErrmsg(ErrorCodes.INPUT_JSON_REQUIRED_PROPERTIES_MISSING_MSG + matcher.group(1));
    } else if (e.getCause() != null && e.getCause().toString().contains("violates foreign key constraint")) {
      error.setErrcode(ErrorCodes.FOREIGN_KEY_VIOLATION);
      error.setErrmsg(ErrorCodes.FOREIGN_KEY_VIOLATION_MSG);
//      error.setErrmsg(ErrorCodes.FOREIGN_KEY_VIOLATION_MSG + " :: " + ExceptionUtils.getRootCauseMessage(e));
//      error.setErrmsg(ErrorCodes.FOREIGN_KEY_VIOLATION_MSG + " :: " + e.getMessage());
      addCausePSQL(error, e);
    } else if (e.getCause() != null && ExceptionUtils.getRootCauseMessage(e).contains("value too long for type character varying(")) {
      String errMsg = ExceptionUtils.getRootCauseMessage(e);
      String varCharLength = errMsg.split("character varying\\(")[1].split("\\)")[0];
      error.setErrcode(ErrorCodes.VARCHAR_TOO_LONG_ERROR);
      error.setErrmsg(ErrorCodes.VARCHAR_TOO_LONG_ERROR_MSG + varCharLength);
      if (Integer.valueOf(varCharLength) == VERSION_MAX_LENGTH) {
        error.setErrmsg(error.getErrmsg() + " (version)");
      }
    } else {
      error.setErrcode(ErrorCodes.CONSTRAINT_VIOLATION);
      error.setErrmsg(ErrorCodes.CONSTRAINT_VIOLATION_MSG);
//      error.setErrmsg(ErrorCodes.CONSTRAINT_VIOLATION_MSG + " :: " + ExceptionUtils.getRootCauseMessage(e));
//      error.setErrmsg(ErrorCodes.CONSTRAINT_VIOLATION_MSG + " :: " + e.getMessage());
      addCausePSQL(error, e);
    }
    
    error.setErrtrace(toString(e));
    return error;
  }

  /**
   * SQL exception probably from database trigger exception.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError sqlTriggerError(Exception e) {
    RihaRestError error = new RihaRestError();
    HibernateJdbcException hje = (HibernateJdbcException) e;

    SQLException sqlEx = hje.getSQLException();
    String errMsg = "SQL state [" + sqlEx.getSQLState() + "]; error code [" + sqlEx.getErrorCode() + "]; "
        + sqlEx.getMessage();

    error.setErrcode(ErrorCodes.SQL_TRIGGER_ERROR);
    error.setErrmsg(ErrorCodes.SQL_TRIGGER_ERROR_MSG + errMsg);
    error.setErrtrace(hje.getMostSpecificCause() + " - " + toString(e));

    return error;
  }

  /**
   * Json error.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError jsonError(Exception e) {
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.JSON_TYPE_ERROR);
    error.setErrmsg(ErrorCodes.JSON_TYPE_ERROR_MSG);
//    error.setErrmsg(ErrorCodes.JSON_TYPE_ERROR_MSG + " :: " + e.getMessage());
    error.setErrtrace(toString(e));
    return error;
  }

  /**
   * Unmapped.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError unmapped(Exception e) {

    return unmapped(e, null);
  }

  /**
   * Unmapped.
   *
   * @param e the e
   * @param additionalMessage the additional message
   * @return the riha rest error
   */
  public static RihaRestError unmapped(Exception e, String additionalMessage) {
    RihaRestError error = new RihaRestError();
    if (e.getClass() == RihaRestException.class) {
      error = (RihaRestError) ((RihaRestException) e).getError();
    } else if (e.getClass() == GenericJDBCException.class || e.getClass() == JDBCConnectionException.class) {
      error = databaseError(e);
    } else if (e.getClass() == SQLGrammarException.class) {
      error = sqlError(e);
    } else if (e.getClass() == ConstraintViolationException.class
        || e.getClass() == DataIntegrityViolationException.class) {
      error = constraintError(e);
    } else if (e.getClass() == HibernateJdbcException.class) {
      error = sqlTriggerError(e);
    } else if (e.getClass() == JsonSyntaxException.class) {
      error = jsonError(e);
    } else if (e.getClass() == NumberFormatException.class) {
      error = numberFormatException(e);
    } else if (e.getClass() == IllegalArgumentException.class) {
      error = illegalArgument(e);
    } else {
      // default
      error.setErrcode(ErrorCodes.UNMAPPED_PROBLEM);
      error.setErrmsg(ErrorCodes.UNMAPPED_PROBLEM_MSG);
      error.setErrtrace(toString(e));
    }
    
    // don't show stacktrace
    if (error.getErrtrace().contains("Exception")) {
      error.setErrtrace("");
    }
    LOG.error("Error", e);
    setAdditionalTrace(error, additionalMessage);
    return error;
  }

  /**
   * Number format exception.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError numberFormatException(Exception e) {
    RihaRestError error = new RihaRestError();
    String err = e.getMessage().split("For input string: ")[1];
    error.setErrcode(ErrorCodes.INPUT_EXPECTED_INTEGER);
    error.setErrmsg(ErrorCodes.INPUT_EXPECTED_INTEGER_MSG + err);
    error.setErrtrace(toString(e));
    return error;
  }

  /**
   * Not found.
   *
   * @param e the e
   * @return the riha rest error
   */
  public static RihaRestError notFound(Exception e) {
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.NOT_FOUND);
    error.setErrmsg(ErrorCodes.NOT_FOUND_MSG);
    error.setErrtrace(toString(e));
    return error;
  }

  public static RihaRestError illegalArgument(Exception e) {
    RihaRestError error = new RihaRestError();
    if (e.getMessage().contains("No kind exists")) {
      error.setErrcode(ErrorCodes.KIND_NOT_FOUND);
      error.setErrmsg(ErrorCodes.KIND_NOT_FOUND_MSG);
    } else {
      error.setErrcode(ErrorCodes.UNMAPPED_PROBLEM);
      error.setErrmsg(ErrorCodes.UNMAPPED_PROBLEM_MSG);
    }
    return error;
  }
  
  /**
   * Sets the additional trace.
   *
   * @param error the error
   * @param text the text
   */
  private static void setAdditionalTrace(RihaRestError error, String text) {
    if (!StringUtils.isEmpty(text)) {
      error.setErrtrace(text + " : " + error.getErrtrace());
    }
  }

  /**
   * Adds the cause psql.
   *
   * @param error the error
   * @param e the e
   */
  private static void addCausePSQL(RihaRestError error, Exception e) {
//    if (e.getCause() != null && e.getCause().getClass() == PSQLException.class) {
//      error.setErrmsg(error.getErrmsg() + ", " + e.getCause().getMessage());
//    }
    // should not show exception message in Error.errmsg
  }

}
