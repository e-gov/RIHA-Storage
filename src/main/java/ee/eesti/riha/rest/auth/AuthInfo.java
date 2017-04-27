package ee.eesti.riha.rest.auth;

import java.util.List;

import ee.eesti.riha.rest.model.readonly.Role_right;

// TODO: Auto-generated Javadoc
/**
 * The Class AuthInfo.
 */
/**
 * @author Praktikant
 *
 */
public class AuthInfo {

  private String user_code;
  private String user_name;

  private String org_code;
  private String org_name;

  private String role_code;
  private String role_name;

  private String token;

  private List<SimpleRoleRight> role_right;

  public static final AuthInfo DEFAULT = new AuthInfo("UNAUTHORIZED", "UNAUTHORIZED", "DEFAULT");

  /**
   * Instantiates a new auth info.
   */
  public AuthInfo() {

  }

  /**
   * Instantiates a new auth info.
   *
   * @param isikuKood the isiku kood
   * @param asutus the asutus
   * @param roll the roll
   */
  public AuthInfo(String isikuKood, String asutus, String roll) {
    this.user_code = isikuKood;
    this.org_code = asutus;
    this.role_code = roll;
  }

  /**
   * Instantiates a new auth info.
   *
   * @param isikuKood the isiku kood
   * @param asutus the asutus
   * @param roll the roll
   * @param token the token
   */
  public AuthInfo(String isikuKood, String asutus, String roll, String token) {
    this.user_code = isikuKood;
    this.org_code = asutus;
    this.role_code = roll;
    this.token = token;
  }

  /**
   * Convert 3rd party data to be suitable for rest api.
   *
   * @param authInfo3rdParty the auth info3rd party
   */
  public AuthInfo(AuthInfo3rdParty authInfo3rdParty) {
    this.user_code = authInfo3rdParty.getIsikuKood();
    this.org_code = authInfo3rdParty.getAsutus();
    this.role_code = authInfo3rdParty.getRoll();
    this.token = authInfo3rdParty.getToken();
  }

  /**
   * Gets the isiku kood.
   *
   * @return the isiku kood
   */
  public String getUser_code() {
    return user_code;
  }

  /**
   * Sets the isiku kood.
   *
   * @param aUser_code the new user_code
   */
  public void setUser_code(String aUser_code) {
    user_code = aUser_code;
  }

  /**
   * Gets the org_code.
   *
   * @return the org_code
   */
  public String getOrg_code() {
    return org_code;
  }

  /**
   * Sets the org_code.
   *
   * @param aOrg_code the new org_code
   */
  public void setOrg_code(String aOrg_code) {
    org_code = aOrg_code;
  }

  /**
   * Gets the role_code.
   *
   * @return the role_code
   */
  public String getRole_code() {
    return role_code;
  }

  /**
   * Sets the role_code.
   *
   * @param aRole_code the new role_code
   */
  public void setRole_code(String aRole_code) {
    role_code = aRole_code;
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

  /**
   * Gets the user_name.
   *
   * @return the user_name
   */
  public String getUser_name() {
    return user_name;
  }

  /**
   * Sets the user_name.
   *
   * @param user_name the new user_name
   */
  public void setUser_name(String user_name) {
    this.user_name = user_name;
  }

  /**
   * Gets the org_name.
   *
   * @return the org_name
   */
  public String getOrg_name() {
    return org_name;
  }

  /**
   * Sets the org_name.
   *
   * @param org_name the new org_name
   */
  public void setOrg_name(String org_name) {
    this.org_name = org_name;
  }

  /**
   * Gets the role_name.
   *
   * @return the role_name
   */
  public String getRole_name() {
    return role_name;
  }

  /**
   * Sets the role_name.
   *
   * @param role_name the new role_name
   */
  public void setRole_name(String role_name) {
    this.role_name = role_name;
  }

  /**
   * Gets the role_right.
   *
   * @return the role_right
   */
  public List<SimpleRoleRight> getRole_right() {
    return role_right;
  }

  /**
   * Sets the role_right.
   *
   * @param role_right the new role_right
   */
  public void setRole_right(List<SimpleRoleRight> role_right) {
    this.role_right = role_right;
  }

  public static class SimpleRoleRight {
    private int create;
    private int read;
    private int update;
    private int delete;
    private int access_restriction;
    private String kind;

    public SimpleRoleRight(int create, int read, int update, int delete, int access_restriction, String kind) {
      this.create = create;
      this.read = read;
      this.update = update;
      this.delete = delete;
      this.access_restriction = access_restriction;
      this.kind = kind;
    }

    public SimpleRoleRight(Role_right role_right, String kindName) {
      this(role_right.getCreate(), role_right.getRead(),
          role_right.getUpdate(), role_right.getDelete(),
          role_right.getAccess_restriction(), kindName);
    }

    public int getCreate() {
      return create;
    }

    public int getRead() {
      return read;
    }

    public int getUpdate() {
      return update;
    }

    public int getDelete() {
      return delete;
    }

    public int getAccess_restriction() {
      return access_restriction;
    }

    public String getKind() {
      return kind;
    }

  }
}
