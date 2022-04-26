package ee.eesti.riha.rest.model.readonly;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.Immutable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

// TODO: Auto-generated Javadoc
/**
 * The Class Asutus.
 */
@Transactional
@Entity
@Table(name = "asutus", schema = "asutused")
@JsonInclude(Include.NON_NULL)
@Immutable
public class Asutus {

  @FieldIsPK
  @Id
  private Integer asutus_id;
  private String registrikood;
  private String nimetus;

  /**
   * Gets the asutus_id.
   *
   * @return the asutus_id
   */
  public Integer getAsutus_id() {
    return asutus_id;
  }

  /**
   * Gets the registrikood.
   *
   * @return the registrikood
   */
  public String getRegistrikood() {
    return registrikood;
  }

  /**
   * Gets the nimetus.
   *
   * @return the nimetus
   */
  public String getNimetus() {
    return nimetus;
  }

}
