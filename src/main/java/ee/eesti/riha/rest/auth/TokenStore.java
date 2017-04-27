package ee.eesti.riha.rest.auth;

/**
 * The Interface TokenStore.
 */
public interface TokenStore {

  /**
   * Adds the token.
   *
   * @param token the token
   * @param authInfo the auth info
   */
  void addToken(String token, AuthInfo authInfo);

  /**
   * Return personCode corresponding to token or null if token does not exist.
   *
   * @param token the token
   * @return the auth info
   */
  AuthInfo tokenExists(String token);

  /**
   * Clear.
   */
  void clear();
}
