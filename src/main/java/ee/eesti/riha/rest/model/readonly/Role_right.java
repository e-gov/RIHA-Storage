package ee.eesti.riha.rest.model.readonly;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ee.eesti.riha.rest.model.util.FieldIsPK;

// TODO: Auto-generated Javadoc
/**
 * The Class Role_right.
 */
@Transactional
@Entity
@Table(name = "role_right")
@JsonInclude(Include.NON_NULL)
@Immutable
public class Role_right {

  @FieldIsPK
  @Id
  private Integer role_right_id;
  private Integer kind_id;
  private String role_name;
  private Integer access_restriction;
  private Integer read;
  private Integer create;
  private Integer update;
  private Integer delete;

  public Role_right() {

  }

  /**
   * @param role_right_id
   * @param kind_id
   * @param role_name
   * @param access_restriction
   * @param read
   * @param create
   * @param update
   * @param delete
   */
  public Role_right(Integer role_right_id, Integer kind_id, String role_name, Integer access_restriction,
      Integer read, Integer create, Integer update, Integer delete) {
    this.role_right_id = role_right_id;
    this.kind_id = kind_id;
    this.role_name = role_name;
    this.access_restriction = access_restriction;
    this.read = read;
    this.create = create;
    this.update = update;
    this.delete = delete;
  }

  /**
   * Gets the role_right_id.
   *
   * @return the role_right_id
   */
  public Integer getRole_right_id() {
    return role_right_id;
  }

  /**
   * Gets the kind_id.
   *
   * @return the kind_id
   */
  public Integer getKind_id() {
    return kind_id;
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
   * Gets the access_restriction.
   *
   * @return the access_restriction
   */
  public Integer getAccess_restriction() {
    return access_restriction;
  }

  /**
   * Gets the read.
   *
   * @return the read
   */
  public Integer getRead() {
    return read;
  }

  /**
   * Gets the creates the.
   *
   * @return the creates the
   */
  public Integer getCreate() {
    return create;
  }

  /**
   * Gets the update.
   *
   * @return the update
   */
  public Integer getUpdate() {
    return update;
  }

  /**
   * Gets the delete.
   *
   * @return the delete
   */
  public Integer getDelete() {
    return delete;
  }

}
