package ee.eesti.riha.rest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.google.gson.JsonObject;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.hibernate.JsonObjectUserType;
import ee.eesti.riha.rest.model.util.DisallowUseMethodForUpdate;
import ee.eesti.riha.rest.model.util.FieldIsPK;

// TODO: Auto-generated Javadoc
/**
 * The Class Main_resource.
 */
// @TypeDefs({ @TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType.class) })
@TypeDefs({@TypeDef(name = "JsonObject", typeClass = JsonObjectUserType.class) })
@Transactional
@Entity
@Table(name = "main_resource")
@JsonInclude(Include.NON_NULL)
public class Main_resource implements BaseModel {

  @FieldIsPK
  @Id
  @Column(updatable = false)
  private Integer main_resource_id;
  private String uri;
  private String name;
  private String short_name;
  private String version;

  // http://stackoverflow.com/questions/15974474/mapping-postgresql-json-column-to-hibernate-value-type
  // @Type(type = "StringJsonObject")
  @JsonRawValue
  // otherwise mapping exception
  @Type(type = "JsonObject")
  private JsonObject json_content;

  private String parent_uri;
  private Integer main_resource_parent_id;
  private String kind;
  private Integer access_restriction;
  private Character state;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date start_date;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date end_date;
  private String creator;
  private String modifier;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date creation_date;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date modified_date;

  private Integer old_id;
  private String field_name;

  private Integer kind_id;

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#callGetId()
   */
  @Override
  public int callGetId() {
    return getMain_resource_id();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#callSetId(int)
   */
  @Override
  public void callSetId(int id) {
    setMain_resource_id(id);
  }

  /**
   * Gets the main_resource_id.
   *
   * @return the main_resource_id
   */
  public Integer getMain_resource_id() {
    return main_resource_id;
  }

  /**
   * Sets the main_resource_id.
   *
   * @param aMain_resource_id the new main_resource_id
   */
  @DisallowUseMethodForUpdate
  public void setMain_resource_id(Integer aMain_resource_id) {
    this.main_resource_id = aMain_resource_id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getUri()
   */
  @Override
  public String getUri() {
    return uri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setUri(java.lang.String)
   */
  @Override
  @DisallowUseMethodForUpdate
  public void setUri(String aUri) {
    this.uri = aUri;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param aName the new name
   */
  public void setName(String aName) {
    this.name = aName;
  }

  /**
   * Gets the short_name.
   *
   * @return the short_name
   */
  public String getShort_name() {
    return short_name;
  }

  /**
   * Sets the short_name.
   *
   * @param aShort_name the new short_name
   */
  public void setShort_name(String aShort_name) {
    this.short_name = aShort_name;
  }

  /**
   * Gets the version.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets the version.
   *
   * @param aVersion the new version
   */
  public void setVersion(String aVersion) {
    this.version = aVersion;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getJson_content()
   */
  @Override
  public JsonObject getJson_content() {
    return json_content;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setJson_content(com.google.gson.JsonObject)
   */
  @Override
  public void setJson_content(JsonObject aJson_content) {
    this.json_content = aJson_content;
  }

  /**
   * Gets the parent_uri.
   *
   * @return the parent_uri
   */
  public String getParent_uri() {
    return parent_uri;
  }

  /**
   * Sets the parent_uri.
   *
   * @param aParent_uri the new parent_uri
   */
  public void setParent_uri(String aParent_uri) {
    this.parent_uri = aParent_uri;
  }

  /**
   * Gets the main_resource_parent_id.
   *
   * @return the main_resource_parent_id
   */
  public Integer getMain_resource_parent_id() {
    return main_resource_parent_id;
  }

  /**
   * Sets the main_resource_parent_id.
   *
   * @param aMain_resource_parent_id the new main_resource_parent_id
   */
  public void setMain_resource_parent_id(Integer aMain_resource_parent_id) {
    this.main_resource_parent_id = aMain_resource_parent_id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getKind()
   */
  @Override
  public String getKind() {
    return kind;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setKind(java.lang.String)
   */
  @Override
  @DisallowUseMethodForUpdate
  public void setKind(String aKind) {
    this.kind = aKind;
  }

  /**
   * Gets the access_restriction.
   *
   * @return the access_restriction
   */
  @Override
  public Integer getAccess_restriction() {
    return access_restriction;
  }

  /**
   * Sets the access_restriction.
   *
   * @param aAccess_restriction the new access_restriction
   */
  @Override
  public void setAccess_restriction(Integer aAccess_restriction) {
    this.access_restriction = aAccess_restriction;
  }

  /**
   * Gets the state.
   *
   * @return the state
   */
  public Character getState() {
    return state;
  }

  /**
   * Sets the state.
   *
   * @param aState the new state
   */
  public void setState(Character aState) {
    this.state = aState;
  }

  /**
   * Gets the start_date.
   *
   * @return the start_date
   */
  public Date getStart_date() {
    return start_date;
  }

  /**
   * Sets the start_date.
   *
   * @param aStart_date the new start_date
   */
  public void setStart_date(Date aStart_date) {
    this.start_date = aStart_date;
  }

  /**
   * Gets the end_date.
   *
   * @return the end_date
   */
  public Date getEnd_date() {
    return end_date;
  }

  /**
   * Sets the end_date.
   *
   * @param aEnd_date the new end_date
   */
  public void setEnd_date(Date aEnd_date) {
    this.end_date = aEnd_date;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getCreator()
   */
  @Override
  public String getCreator() {
    return creator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setCreator(java.lang.String)
   */
  @Override
  @DisallowUseMethodForUpdate
  public void setCreator(String aCreator) {
    this.creator = aCreator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getModifier()
   */
  @Override
  public String getModifier() {
    return modifier;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setModifier(java.lang.String)
   */
  @Override
  public void setModifier(String aModifier) {
    this.modifier = aModifier;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getCreation_date()
   */
  @Override
  public Date getCreation_date() {
    return creation_date;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setCreation_date(java.util.Date)
   */
  @Override
  @DisallowUseMethodForUpdate
  public void setCreation_date(Date aCreation_date) {
    this.creation_date = aCreation_date;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#getModified_date()
   */
  @Override
  public Date getModified_date() {
    return modified_date;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#setModified_date(java.util.Date)
   */
  @Override
  public void setModified_date(Date aModified_date) {
    this.modified_date = aModified_date;
  }

  /**
   * Gets the old_id.
   *
   * @return the old_id
   */
  public Integer getOld_id() {
    return old_id;
  }

  /**
   * Sets the old_id.
   *
   * @param aOld_id the new old_id
   */
  public void setOld_id(Integer aOld_id) {
    this.old_id = aOld_id;
  }

  /**
   * Gets the field_name.
   *
   * @return the field_name
   */
  public String getField_name() {
    return field_name;
  }

  /**
   * Sets the field_name.
   *
   * @param aField_name the new field_name
   */
  public void setField_name(String aField_name) {
    this.field_name = aField_name;
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
   * Sets the kind_id.
   *
   * @param aKind_id the new kind_id
   */
  @DisallowUseMethodForUpdate
  public void setKind_id(Integer aKind_id) {
    this.kind_id = aKind_id;
  }

}
