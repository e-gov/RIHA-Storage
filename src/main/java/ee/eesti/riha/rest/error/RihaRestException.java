package ee.eesti.riha.rest.error;

import java.io.Serial;

// TODO: Auto-generated Javadoc
/**
 * The Class RihaRestException.
 */
public class RihaRestException extends Exception {

  @Serial
  private static final long serialVersionUID = -6561570608859278604L;

  private Object error;

  /**
   * Instantiates a new riha rest exception.
   *
   * @param error the error
   */
  public RihaRestException(Object error) {
    this.error = error;
  }

  /**
   * Gets the error.
   *
   * @return the error
   */
  public Object getError() {
    return error;
  }

}
