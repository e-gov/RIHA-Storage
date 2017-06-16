package ee.eesti.riha.rest.service.impl;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.ServiceLogic;
import ee.eesti.riha.rest.logic.TokenValidator;
import ee.eesti.riha.rest.service.ApiClassicService;

// TODO: Auto-generated Javadoc
/**
 * The Class ApiClassicServiceImpl.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
@Component
public class ApiClassicServiceImpl<T, K> implements ApiClassicService {

  @Autowired
  ServiceLogic<T, K> serviceLogic;

  @Context
  HttpHeaders httpHeaders;

  @Autowired
  TokenStore tokenStore;

  private static final Logger LOG = LoggerFactory.getLogger(ApiClassicServiceImpl.class);

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiClassicService#getMany(java.lang.String, java.lang.Integer, java.lang.Integer,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Response getMany(final String tableName, final Integer limit, final Integer offset, final String filter,
      final String sort, final String fields) {

    // TODO what to do with token?

    LOG.info("getAll API GENERIC called");

    // no need to currently authenticate for GET requests
    // return serviceLogic.getMany(tableName, limit, offset, filter, sort, fields);

    return (new Command() {
      @Override
      public Response commandMethod(Object user) {
        return serviceLogic.getMany(tableName, limit, offset, filter,
            sort, fields);
      }
    }).doIfHeadersOk();

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiClassicService#getById(java.lang.String, java.lang.Integer, java.lang.String)
   */
  @Override
  public Response getById(final String tableName, final Integer id, final String fields) {

    LOG.info("getById API GENERIC called");

    // no need to currently authenticate for GET requests
    // return serviceLogic.getById(tableName, id, fields);

    return (new Command() {
      @Override
      public Response commandMethod(Object user) {
        return serviceLogic.getById(tableName, id, fields);
      }
    }).doIfHeadersOk();

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiClassicService#getResourceById(java.lang.Integer)
   */
  @Override
  public Response getResourceById(final Integer id) {

    LOG.info("getResourceById API GENERIC called");

    // no need to currently authenticate for GET requests
    // return serviceLogic.getResourceById(id);

    return (new Command() {
      @Override
      public Response commandMethod(Object user) {
        return serviceLogic.getResourceById(id);
      }
    }).doIfHeadersOk();

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiClassicService#create(java.lang.String, java.lang.String)
   */
  @Override
  public Response create(final String json, final String tableName) {

    LOG.info("create API GENERIC called");

    return (new Command() {
      @Override
      public Response commandMethod(Object user) {
        return serviceLogic.create(json, tableName);
      }
    }).doIfHeadersOk();

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiClassicService#update(java.lang.String, java.lang.String, java.lang.Integer)
   */
  @Override
  public Response update(final String json, final String tableName, final Integer id) {

    LOG.info("update API GENERIC called");

    return (new Command() {
      @Override
      public Response commandMethod(Object user) {
        return serviceLogic.update(json, tableName, id);
      }
    }).doIfHeadersOk();

  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiClassicService#delete(java.lang.String, java.lang.Integer)
   */
  @Override
  public Response delete(final String tableName, final Integer id) {

    LOG.info("delete API GENERIC called");

    return (new Command() {
      @Override
      public Response commandMethod(Object user) {
        return serviceLogic.delete(tableName, id);
      }
    }).doIfHeadersOk();

  }

  /**
   * Helper class to reuse code with command pattern.
   */
  private abstract class Command {

    /**
     * Check whether HTTP headers are OK and return result or error response accordingly.
     *
     * @return the response
     */
    public Response doIfHeadersOk() {
      try {
        String token = TokenValidator.getToken(httpHeaders);
        if (!StringUtils.isEmpty(token)) {
          // if (TokenValidator.areHeadersOk(httpHeaders)) {

          // return commandMethod(TokenValidator.isTokenOk(
          // TokenValidator.getToken(httpHeaders), getAuthService()));
          return commandMethod(TokenValidator.isTokenOk(TokenValidator.getToken(httpHeaders), tokenStore));
        } else {
          return commandMethod(AuthInfo.DEFAULT);
        }
        // areHeadersOK must return true or throw exception
        // throw new RuntimeException("This should never happen!");
      } catch (RihaRestException e) {
        RihaRestError error = (RihaRestError) e.getError();
        // don't show stacktrace
        // error.setErrtrace(Arrays.toString(e.getStackTrace()));
        return Response.status(Status.BAD_REQUEST).entity(error).build();
      }
    }

    /**
     * Abstract method which should call ServiceLogic method.
     *
     * @param user the user
     * @return the response
     */
    public abstract Response commandMethod(Object user);
  }

}
