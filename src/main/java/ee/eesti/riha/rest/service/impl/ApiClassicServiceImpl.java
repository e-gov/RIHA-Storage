package ee.eesti.riha.rest.service.impl;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.logic.ServiceLogic;
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
      public Response commandMethod() {
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
      public Response commandMethod() {
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
      public Response commandMethod() {
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
      public Response commandMethod() {
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
      public Response commandMethod() {
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
      public Response commandMethod() {
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
      return commandMethod();
    }

    /**
     * Abstract method which should call ServiceLogic method.
     *
     * @return the response
     */
    public abstract Response commandMethod();
  }

}
