package ee.eesti.riha.rest.logic;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;

// TODO: Auto-generated Javadoc
/**
 * The Class TokenValidator.
 */
public final class TokenValidator {

  private static final Logger LOG = LoggerFactory.getLogger(TokenValidator.class);

  private TokenValidator() {

  }

  /**
   * Gets the token.
   *
   * @param httpHeaders the http headers
   * @return the token
   */
  public static String getToken(HttpHeaders httpHeaders) {
    return httpHeaders.getHeaderString(Finals.X_AUTH_TOKEN);
  }

  /**
   * Return true if token is valid, else throw exception.
   *
   * @param token the token
   * @return true, if is token ok
   * @throws RihaRestException the riha rest exception
   */
  public static boolean isTokenOk(String token) throws RihaRestException {

    if (!tokenNotEmpty(token)) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.NO_AUTH_TOKEN_PROVIDED);
      error.setErrmsg(ErrorCodes.NO_AUTH_TOKEN_PROVIDED_MSG);
      // LOG.info(error);
      throw new RihaRestException(error);
    }

    if (!isTokenValid(token)) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.AUTH_TOKEN_INVALID);
      error.setErrmsg(ErrorCodes.AUTH_TOKEN_INVALID_MSG);
      // LOG.info(error);
      throw new RihaRestException(error);
    }

    return true;
  }

  /**
   * Return personCode if token is valid, else throw exception.
   *
   * @param token the token
   * @param tokenStore the token store
   * @return the auth info
   * @throws RihaRestException the riha rest exception
   */
  public static AuthInfo isTokenOk(String token, TokenStore tokenStore) throws RihaRestException {

    // throws error if not ok
    isTokenOk(token);
    AuthInfo user = tokenStore.tokenExists(token);
    if (user == null) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.AUTH_TOKEN_INVALID);
      error.setErrmsg(ErrorCodes.AUTH_TOKEN_INVALID_MSG);
      throw new RihaRestException(error);
    }

    return user;
  }

  /**
   * Checks if is token valid.
   *
   * @param token the token
   * @return true, if is token valid
   */
  private static boolean isTokenValid(String token) {
    LOG.info(token);
    return true;
  }

  /**
   * Token not empty.
   *
   * @param token the token
   * @return true, if successful
   */
  private static boolean tokenNotEmpty(String token) {
    if (StringUtils.isNotEmpty(token)) {
      return true;
    }
    return false;
  }

}
