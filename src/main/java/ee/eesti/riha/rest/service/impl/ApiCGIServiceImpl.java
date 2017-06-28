package ee.eesti.riha.rest.service.impl;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.logic.ServiceLogic;
import ee.eesti.riha.rest.service.ApiCGIService;

// TODO: Auto-generated Javadoc
/**
 * The Class ApiCGIServiceImpl.
 *
 * @param <T> the generic type
 * @param <K> the key type
 */
@Component
public class ApiCGIServiceImpl<T, K> implements ApiCGIService<T> {

  @Autowired
  ServiceLogic<T, K> serviceLogic;

  private static final Logger LOG = LoggerFactory.getLogger(ApiCGIServiceImpl.class);

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiCGIService#getCGI(java.lang.String, java.lang.String, java.lang.String,
   * java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Response getCGI(String operation, String path, String token, Integer limit, Integer offset, String filter,
      String sort, String fields) {

    LOG.info("getCGI API GENERIC called");

    return serviceLogic.getCGI(operation, path, token, limit, offset, filter, sort, fields);

  }

  // {"op":"get","path":"db/mytable/123","token":"abca"}
  // {"op":"post", "path": "/db/mytable", "data":{ "value": 58.3788, "name":
  // "lat"}}
  // {"op":"put", "path": "/db/mytable/123", "data":{ "value": 58.3788,
  // "name": "lat"}}
  // {"op":"delete", "path": "/db/mytable/123"}
  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiCGIService#postCGI(java.lang.String)
   */
  // {"op":"delete", "path": "/db/mytable", "id":[123,456,777]}
  @Override
  public Response postCGI(String json) {

    LOG.info("postCGI API GENERIC called");

    return serviceLogic.postCGI(json);

  }

}
