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
 * The Class Isik.
 */
@Transactional
@Entity
@Table(name = "isik", schema = "asutused")
@JsonInclude(Include.NON_NULL)
@Immutable
public class Isik {

  @FieldIsPK
  @Id
  private Integer i_id;
  private String kood;
  private String perenimi;
  private String eesnimi;

  // public String getNimi() {
  // return eesnimi + " " + perenimi;
  // }

  /**
   * Gets the i_id.
   *
   * @return the i_id
   */
  public Integer getI_id() {
    return i_id;
  }

  /**
   * Gets the kood.
   *
   * @return the kood
   */
  public String getKood() {
    return kood;
  }

  /**
   * Gets the perenimi.
   *
   * @return the perenimi
   */
  public String getPerenimi() {
    return perenimi;
  }

  /**
   * Gets the eesnimi.
   *
   * @return the eesnimi
   */
  public String getEesnimi() {
    return eesnimi;
  }

}
