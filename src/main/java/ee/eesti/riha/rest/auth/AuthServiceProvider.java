package ee.eesti.riha.rest.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import ee.eesti.riha.rest.util.PropsReader;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthServiceProvider.
 */
public final class AuthServiceProvider {

  // public static final String AUTH_SERVICE_URL = "http://localhost:1234";
  public static final String AUTH_SERVICE_URL = PropsReader.get("AUTH_SERVICE_URL");

  private static AuthServiceProvider instance = new AuthServiceProvider();

  /**
   * Instantiates a new auth service provider.
   */
  private AuthServiceProvider() {
  }

  /**
   * Gets the single instance of AuthServiceProvider.
   *
   * @return single instance of AuthServiceProvider
   */
  public static AuthServiceProvider getInstance() {
    return instance;
  }

  private AuthService authService;

  /**
   * Gets the.
   *
   * @param url the url
   * @return the auth service
   */
  private AuthService get(String url) {
    if (authService == null) {
      authService = create(url);
    }
    return authService;
  }

  /**
   * Gets the.
   *
   * @return the auth service
   */
  public AuthService get() {
    return get(AUTH_SERVICE_URL);
    // use fake
    // return get(null);
  }

  /**
   * Sets the.
   *
   * @param aAuthService the auth service
   */
  public void set(AuthService aAuthService) {
    this.authService = aAuthService;
  }

  /**
   * Creates the.
   *
   * @param url the url
   * @return the auth service
   */
  private static AuthService create(String url) {
    if (StringUtils.isEmpty(url)) {
      return new AuthServiceImpl();
    } else {
      return JAXRSClientFactory.create(url, AuthService.class);
    }
  }

}
