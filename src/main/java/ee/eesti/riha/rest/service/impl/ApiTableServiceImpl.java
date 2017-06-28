package ee.eesti.riha.rest.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.dao.GenericDAO;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.Validator;
import ee.eesti.riha.rest.model.readonly.Kind;
import ee.eesti.riha.rest.service.ApiTableService;

// TODO: Auto-generated Javadoc
/**
 * The Class ApiTableServiceImpl.
 *
 * @param <T> the generic type
 */
@Component
public class ApiTableServiceImpl<T> implements ApiTableService {

  static Map<String, Class> fullScanTables = new HashMap<>();

  private static final Logger LOG = LoggerFactory.getLogger(ApiTableServiceImpl.class);

  static {
    fullScanTables.put(Kind.class.getSimpleName().toLowerCase(), Kind.class);
  }

  @Autowired
  GenericDAO<T> noLogicDAO;

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.service.ApiTableService#getFullTable(java.lang.String)
   */
  @Override
  public Response getFullTable(String tableName) {

    LOG.info("getFullTable called");

    return getFullTableLogic(tableName);

  }

  private Response getFullTableLogic(String tableName) {
    try {
      String lowerTableName = tableName.toLowerCase();
      Validator.validateTableNameForFullTable(lowerTableName, fullScanTables);

      Class<T> clazz = fullScanTables.get(lowerTableName);

      List<T> items = noLogicDAO.findAll(clazz);

      return Response.ok(items).build();

    } catch (RihaRestException e) {

      return Response.status(Status.BAD_REQUEST).entity(e.getError()).build();

    } catch (Exception e) {

      return Response.status(Status.BAD_REQUEST).entity(MyExceptionHandler.unmapped(e)).build();
    }
  }

}
