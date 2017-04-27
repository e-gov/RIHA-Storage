package ee.eesti.riha.rest.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.AuthInfo3rdParty;
import ee.eesti.riha.rest.auth.AuthService;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.StringHelper;

// TODO: Auto-generated Javadoc
/**
 * The Class TokenValidator.
 */
public final class TokenValidator {

  private static final Logger LOG = LoggerFactory.getLogger(TokenValidator.class);

  private TokenValidator() {

  }

  /**
   * Check whether HTTP headers are ok (currently checks only X-Auth-Token).
   *
   * @param httpHeaders the http headers
   * @return true, if successful
   * @throws RihaRestException the riha rest exception
   */
  public static boolean areHeadersOk(HttpHeaders httpHeaders) throws RihaRestException {

    if (!tokenNotEmpty(httpHeaders.getHeaderString(Finals.X_AUTH_TOKEN))) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.NO_HTTP_AUTH_TOKEN_PROVIDED);
      error.setErrmsg(ErrorCodes.NO_HTTP_AUTH_TOKEN_PROVIDED_MSG);
      throw new RihaRestException(error);
    }

    if (!isTokenValid(httpHeaders.getHeaderString(Finals.X_AUTH_TOKEN))) {
      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.AUTH_TOKEN_INVALID);
      error.setErrmsg(ErrorCodes.AUTH_TOKEN_INVALID_MSG);
      throw new RihaRestException(error);
    }

    return true;
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
   * Return user if token is valid, else throw exception.
   *
   * @param token the token
   * @param service the service
   * @return the object
   * @throws RihaRestException the riha rest exception
   */
  public static Object isTokenOk3rdParty(String token, AuthService service) throws RihaRestException {

    // throws error if not ok
    isTokenOk(token);
    // AuthInfo user = null;
    AuthInfo3rdParty user = null;
    try {
      InputStream is = (InputStream) service.isValid(token);
      // user = getObjectFromClient(is, AuthInfo.class);
      user = getObjectFromClient(is, AuthInfo3rdParty.class);

      if (user == null) {
        // if (user == null || areFieldsNull(user)) {
        RihaRestError error = new RihaRestError();
        error.setErrcode(ErrorCodes.THIRD_PARTY_AUTH_TOKEN_INVALID);
        error.setErrmsg(ErrorCodes.THIRD_PARTY_AUTH_TOKEN_INVALID_MSG);
        error.setErrtrace(token + " -> " + JsonHelper.GSON.toJson(user));
        throw new RihaRestException(error);
      }
      LOG.info("TOKEN IS OK " + user);
    } catch (RihaRestException e) {
      throw e;
    } catch (Exception e) {
      // e.printStackTrace();
      handleException(e);
    }

    return user;
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
   * Handle exception.
   *
   * @param e the e
   * @throws RihaRestException the riha rest exception
   */
  private static void handleException(Exception e) throws RihaRestException {
    // e.printStackTrace();
    LOG.info("token exception: " + e);
    if (e.getClass() == ProcessingException.class
        && (e.getCause() != null && e.getCause().getClass() == ConnectException.class)
        || e.getClass() == NotFoundException.class) {

      RihaRestError error = new RihaRestError();
      error.setErrcode(ErrorCodes.CANT_CONNECT_TO_AUTH);
      error.setErrmsg(ErrorCodes.CANT_CONNECT_TO_AUTH_MSG);
      error.setErrtrace(e.toString());
      throw new RihaRestException(error);

    }

    throw new RihaRestException(MyExceptionHandler.unmapped(e, "Viga autentimsteenusega"));
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

  /**
   * Are fields null.
   *
   * @param authInfo the auth info
   * @return true, if successful
   */
  private static boolean areFieldsNull(AuthInfo authInfo) {
    return StringHelper
        .multipleEquals("null", authInfo.getOrg_code(), authInfo.getRole_code(), authInfo.getUser_code());
  }

  /**
   * Gets the object from client.
   *
   * @param <T> the generic type
   * @param inputStream the input stream
   * @param clazz the clazz
   * @return the object from client
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static <T> T getObjectFromClient(InputStream inputStream, Class<T> clazz) throws IOException {
    // riha authentication service returns ISO_8859_1
    String json = readStream(inputStream, StandardCharsets.ISO_8859_1);
    T result = JsonHelper.GSON.fromJson(json, clazz);
    return result;
  }

  /**
   * Read stream.
   *
   * @param inputStream the input stream
   * @param charset the charset
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String readStream(InputStream inputStream, Charset charset) throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
      StringBuilder sb = new StringBuilder();
      String line;

      while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
      }
      LOG.info(sb.toString());
      return sb.toString();
    }
  }
}
