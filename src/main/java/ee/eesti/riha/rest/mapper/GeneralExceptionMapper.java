package ee.eesti.riha.rest.mapper;

import java.net.SocketTimeoutException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.logic.MyExceptionHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralExceptionMapper.
 */
public class GeneralExceptionMapper implements ExceptionMapper<Exception> {

  /*
   * (non-Javadoc)
   * 
   * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Throwable)
   */

  private static final Logger LOG = LoggerFactory.getLogger(GeneralExceptionMapper.class);

  @Override
  public Response toResponse(Exception exception) {
    RihaRestError error = new RihaRestError();
    Status status = Status.INTERNAL_SERVER_ERROR;

    LOG.info("" + exception.getClass());

    if (exception.getClass() == SocketTimeoutException.class) {
      error.setErrcode(ErrorCodes.CLIENT_TIMEOUT);
      error.setErrmsg(ErrorCodes.CLIENT_TIMEOUT_MSG);
      status = Status.REQUEST_TIMEOUT;
    } else if (exception.getClass() == RuntimeException.class
        && msgContains(exception, "Invalid URL encoding: not a valid digit")) {
      error.setErrcode(ErrorCodes.URL_ENCODING_PERCENT);
      error.setErrmsg(ErrorCodes.URL_ENCODING_PERCENT_MSG);
      status = Status.BAD_REQUEST;
    } else {
      error.setErrcode(ErrorCodes.SERVER_ERROR);
      error.setErrmsg(ErrorCodes.SERVER_ERROR_MSG);
    }
    // don't show stacktrace
    // error.setErrtrace(exception.toString());
    LOG.info(MyExceptionHandler.toString(exception));
    return Response.status(status).entity(error).type(MediaType.APPLICATION_JSON + "; charset=UTF-8").build();
  }

  /**
   * Msg contains.
   *
   * @param e the e
   * @param s the s
   * @return true, if successful
   */
  private static boolean msgContains(Exception e, String s) {
    return e.getMessage() != null && e.getMessage().contains(s);
  }
}
