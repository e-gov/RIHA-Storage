package ee.eesti.riha.rest.mapper;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.logic.MyExceptionHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class MyExceptionMapper.
 */
public class MyExceptionMapper implements ExceptionMapper<ClientErrorException> {

  private static final String NOT_FOUND = "HTTP 404 Not Found";

  private static final Logger LOG = LoggerFactory.getLogger(MyExceptionMapper.class);

  /*
   * (non-Javadoc)
   * 
   * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
   */
  @Override
  public Response toResponse(ClientErrorException exception) {
    RihaRestError error = null;
    Status status = Status.BAD_REQUEST;
    LOG.info("MY EXCEPTION MAPPER");
    LOG.info(exception.getMessage());

    if (exception.getMessage().equals(NOT_FOUND)) {
      error = MyExceptionHandler.notFound(exception);
      status = Status.NOT_FOUND;
    }

    // exception.printStackTrace();
    LOG.info("" + exception.getClass());
    if (exception.getCause() != null) {
      LOG.info("" + exception.getCause());
      if (exception.getCause().getClass() == NumberFormatException.class) {
        error = MyExceptionHandler.numberFormatException((Exception) exception.getCause());
      }
    }

    if (error == null) {
      error = MyExceptionHandler.unmapped(exception);
    }

    return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON + "; charset=UTF-8").build();
  }

}
