package ee.eesti.riha.rest.error;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PartialError.
 */
public class PartialError {

  private Object successData;

  private List<RihaRestError> errors;

  /**
   * Gets the success data.
   *
   * @return the success data
   */
  public Object getSuccessData() {
    return successData;
  }

  /**
   * Sets the success data.
   *
   * @param success the new success data
   */
  public void setSuccessData(Object success) {
    this.successData = success;
  }

  /**
   * Gets the errors.
   *
   * @return the errors
   */
  public List<RihaRestError> getErrors() {
    return errors;
  }

  /**
   * Sets the errors.
   *
   * @param myErrors the new errors
   */
  public void setErrors(List<RihaRestError> myErrors) {
    this.errors = myErrors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PartialError [successData=" + successData + ", errors=" + errors + "]";
  }

}
