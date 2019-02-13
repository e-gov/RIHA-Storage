package ee.eesti.riha.rest.logic.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * The Class DateHelper.
 */
public final class DateHelper {
  public static final String DATE_FORMAT_IN_JSON = "yyyy-MM-dd'T'HH:mm:ss";
  private static final String DATE_FORMAT_SIMPLE = "yyyy-MM-dd";
  private static final String DATE_FORMAT_NO_SECONDS = "yyyy-MM-dd'T'HH:mm";

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
