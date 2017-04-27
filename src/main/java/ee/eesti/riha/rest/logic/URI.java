package ee.eesti.riha.rest.logic;

import java.util.Calendar;

// TODO: Auto-generated Javadoc
/**
 * The Class URI.
 */
public final class URI {

  private URI() {

  }

  static final String URI_PREFIX = "urn:fdc:riha.eesti.ee";

  /**
   * Construct uri.
   *
   * @param kind the kind
   * @param primaryKeyId the primary key id
   * @return the string
   */
  public static String constructUri(String kind, Integer primaryKeyId) {

    StringBuffer uri = new StringBuffer();
    uri.append(URI_PREFIX);
    uri.append(":");
    int yearNow = Calendar.getInstance().get(Calendar.YEAR);
    uri.append(yearNow);
    uri.append(":");
    uri.append(kind);
    uri.append(":");
    uri.append(primaryKeyId);
    return uri.toString();

  }

}
