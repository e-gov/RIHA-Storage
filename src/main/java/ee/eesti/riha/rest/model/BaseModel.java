package ee.eesti.riha.rest.model;

import java.util.Date;

import com.google.gson.JsonObject;

// TODO: Auto-generated Javadoc
/**
 * The Interface BaseModel.
 */
public interface BaseModel {

  /**
   * Call subclass method to get id (primary key).
   *
   * @return primary key value
   */
  int callGetId();

  /**
   * Call subclass method to set id (primary key).
   *
   * @param id the id
   */
  void callSetId(int id);

  /**
   * Gets the uri.
   *
   * @return the uri
   */
  String getUri();

  /**
   * Sets the uri.
   *
   * @param uri the new uri
   */
  void setUri(String uri);

  /**
   * Gets the json_content.
   *
   * @return the json_content
   */
  JsonObject getJson_content();

  /**
   * Sets the json_content.
   *
   * @param json_content the new json_content
   */
  void setJson_content(JsonObject json_content);

  /**
   * Gets the creator.
   *
   * @return the creator
   */
  String getCreator();

  /**
   * Sets the creator.
   *
   * @param creator the new creator
   */
  void setCreator(String creator);

  /**
   * Gets the modifier.
   *
   * @return the modifier
   */
  String getModifier();

  /**
   * Sets the modifier.
   *
   * @param modifier the new modifier
   */
  void setModifier(String modifier);

  /**
   * Gets the creation_date.
   *
   * @return the creation_date
   */
  Date getCreation_date();

  /**
   * Sets the creation_date.
   *
   * @param creation_date the new creation_date
   */
  void setCreation_date(Date creation_date);

  /**
   * Gets the modified_date.
   *
   * @return the modified_date
   */
  Date getModified_date();

  /**
   * Sets the modified_date.
   *
   * @param modified_date the new modified_date
   */
  void setModified_date(Date modified_date);

  /**
   * Gets the kind.
   *
   * @return the kind
   */
  String getKind();

  /**
   * Sets the kind.
   *
   * @param kind the new kind
   */
  void setKind(String kind);

}
