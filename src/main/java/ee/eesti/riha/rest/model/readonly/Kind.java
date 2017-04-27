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
 * The Class Kind.
 */
@Transactional
@Entity
@Table(name = "kind")
@JsonInclude(Include.NON_NULL)
@Immutable
// TODO currently using Kind as immutable, should be changed in future
public class Kind {

  @FieldIsPK
  @Id
  private Integer kind_id;

  private String name;

  /**
   * Gets the kind_id.
   *
   * @return the kind_id
   */
  public Integer getKind_id() {
    return kind_id;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

}
