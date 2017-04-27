package ee.eesti.riha.rest.service.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.AuthInfo3rdParty;
import ee.eesti.riha.rest.auth.AuthServiceProvider;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.AuthInfoCreator;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.TokenValidator;
import ee.eesti.riha.rest.logic.util.StringHelper;
import ee.eesti.riha.rest.service.ApiAuthService;

/**
 * The Class ApiAuthServiceImpl.
 */
@Component
public class ApiAuthServiceImpl implements ApiAuthService {

  AuthServiceProvider authServiceProvider = AuthServiceProvider.getInstance();

  @Autowired
  TokenStore tokenStore;

  @Autowired
  AuthInfoCreator authInfoCreator;

  private static final Logger LOG = LoggerFactory.getLogger(ApiAuthServiceImpl.class);

  private static final AuthInfo3rdParty TEST_AUTHINFO = new AuthInfo3rdParty("38312280240", "70009646",
      "ROLL_RIHA_ADMINISTRAATOR", "testToken");

  @Override
  public Response checkToken(String sessionId) {

    LOG.info("URL: " + AuthServiceProvider.AUTH_SERVICE_URL);

    LOG.info("TokenStore " + tokenStore);

    try {

      if (Finals.IS_TEST && StringHelper.areEqual(Finals.TEST_TOKEN, sessionId)) {
        AuthInfo authInfo = authInfoCreator.convert(TEST_AUTHINFO);
        tokenStore.addToken(sessionId, authInfo);
        return Response.ok(authInfo).build();
      }

      // send cookie to 3rd party authentication service
      // AuthInfo user = (AuthInfo) TokenValidator.isTokenOk3rdParty(sessionId, authServiceProvider.get());
      AuthInfo3rdParty user3rdParty = (AuthInfo3rdParty) TokenValidator.isTokenOk3rdParty(sessionId,
          authServiceProvider.get());

      if (user3rdParty == null) {
        // should not happen, TokenValidator.isTokenOk3rdPary should throw error if null
        return Response.status(Status.UNAUTHORIZED).entity("Not authenticated").build();
      }

      user3rdParty.setToken(sessionId);

      // AuthInfo user = new AuthInfo(user3rdParty);
      AuthInfo user = authInfoCreator.convert(user3rdParty);

      tokenStore.addToken(sessionId, user);

      // return Response.ok(user3rdParty).build();
      return Response.ok(user).build();

    } catch (RihaRestException e) {
      e.printStackTrace();
      return Response.ok(e.getError()).build();
    } catch (Exception e) {
      return Response.status(Status.BAD_REQUEST).entity(MyExceptionHandler.unmapped(e)).build();
    }
  }

}
