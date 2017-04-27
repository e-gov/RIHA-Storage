package ee.eesti.riha.rest.logic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class DateHelper.
 */
public final class DateHelper {

  private DateHelper() {

  }

  public static final String DATE_FORMAT_IN_JSON = "yyyy-MM-dd'T'HH:mm:ss";
  public static final String DATE_FORMAT_SIMPLE = "yyyy-MM-dd";
  public static final String DATE_FORMAT_NO_SECONDS = "yyyy-MM-dd'T'HH:mm";
  public static final SimpleDateFormat FORMATTER = new SimpleDateFormat(DATE_FORMAT_IN_JSON);

  /**
   * From date taken from json.
   *
   * @param dateStr the date str
   * @return the date
   * @throws ParseException the parse exception
   */
  public static Date fromDateTakenFromJson(String dateStr) throws ParseException {
    Date date = null;
    date = FORMATTER.parse(dateStr);
    return date;
  }

  /**
   * From string.
   *
   * @param dateStr the date str
   * @return the date
   * @throws ParseException the parse exception
   */
  public static Date fromString(String dateStr) throws ParseException {
    return DateUtils.parseDate(dateStr, DATE_FORMAT_IN_JSON, DATE_FORMAT_NO_SECONDS, DATE_FORMAT_SIMPLE);
  }

}
