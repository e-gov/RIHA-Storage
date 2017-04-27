package ee.eesti.riha.rest;

import org.springframework.beans.factory.annotation.Autowired;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.logic.Finals;

/**
 * Helper factory to provide {@link AuthInfo} objects for unit testing.
 * 
 * @author A
 *
 */
public class AuthInfoTestTokenFactory {

  @Autowired
  TokenStore tokenStore;
  
  public AuthInfo getTestToken() {
    return tokenStore.tokenExists(Finals.TEST_TOKEN);
  }
  
}
