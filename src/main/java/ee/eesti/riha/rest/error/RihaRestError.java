package ee.eesti.riha.rest.error;

// TODO: Auto-generated Javadoc
/**
 * The Class RihaRestError.
 */
public class RihaRestError {

  private int errcode;

  private String errmsg;

  private String errtrace = "";

  /**
   * Gets the errcode.
   *
   * @return the errcode
   */
  public int getErrcode() {
    return errcode;
  }

  /**
   * Sets the errcode.
   *
   * @param aErrcode the new errcode
   */
  public void setErrcode(int aErrcode) {
    errcode = aErrcode;
  }

  /**
   * Gets the errmsg.
   *
   * @return the errmsg
   */
  public String getErrmsg() {
    return errmsg;
  }

  /**
   * Sets the errmsg.
   *
   * @param aErrmsg the new errmsg
   */
  public void setErrmsg(String aErrmsg) {
    errmsg = aErrmsg;
  }

  /**
   * Gets the errtrace.
   *
   * @return the errtrace
   */
  public String getErrtrace() {
    return errtrace;
  }

  /**
   * Sets the errtrace.
   *
   * @param aErrtrace the new errtrace
   */
  public void setErrtrace(String aErrtrace) {
    errtrace = aErrtrace;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MyError [errcode=" + errcode + ", errmsg=" + errmsg + ", errtrace=" + errtrace + "]";
  }

}
