package ee.eesti.riha.rest.auth;

/**
 * Authentication data that comes from 3rd party service
 *
 */
public class AuthInfo3rdParty {

  private String isikuKood;

  private String asutus;

  private String roll;

  private String token;

  /**
   * Instantiates a new auth info.
   */
  public AuthInfo3rdParty() {

  }

  /**
   * Instantiates a new auth info.
   *
   * @param isikuKood the isiku kood
   * @param asutus the asutus
   * @param roll the roll
   */
  public AuthInfo3rdParty(String isikuKood, String asutus, String roll) {
    this.isikuKood = isikuKood;
    this.asutus = asutus;
    this.roll = roll;
  }

  /**
   * Instantiates a new auth info.
   *
   * @param isikuKood the isiku kood
   * @param asutus the asutus
   * @param roll the roll
   * @param token the token
   */
  public AuthInfo3rdParty(String isikuKood, String asutus, String roll, String token) {
    this.isikuKood = isikuKood;
    this.asutus = asutus;
    this.roll = roll;
    this.token = token;
  }

  /**
   * Gets the isiku kood.
   *
   * @return the isiku kood
   */
  public String getIsikuKood() {
    return isikuKood;
  }

  /**
   * Sets the isiku kood.
   *
   * @param aIsikuKood the new isiku kood
   */
  public void setIsikuKood(String aIsikuKood) {
    isikuKood = aIsikuKood;
  }

  /**
   * Gets the asutus.
   *
   * @return the asutus
   */
  public String getAsutus() {
    return asutus;
  }

  /**
   * Sets the asutus.
   *
   * @param aAsutus the new asutus
   */
  public void setAsutus(String aAsutus) {
    asutus = aAsutus;
  }

  /**
   * Gets the roll.
   *
   * @return the roll
   */
  public String getRoll() {
    return roll;
  }

  /**
   * Sets the roll.
   *
   * @param aRoll the new roll
   */
  public void setRoll(String aRoll) {
    roll = aRoll;
  }

  /**
   * Gets the token.
   *
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Sets the token.
   *
   * @param aToken the new token
   */
  public void setToken(String aToken) {
    token = aToken;
  }
}
