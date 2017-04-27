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
 * The Class Comment.
 */
// @TypeDefs({ @TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType.class) })
@TypeDefs({@TypeDef(name = "JsonObject", typeClass = JsonObjectUserType.class) })
@Transactional
@Entity
@Table(name = "comment")
@JsonInclude(Include.NON_NULL)
public class Comment implements BaseModel {

  @FieldIsPK
  @Id
  @Column(updatable = false)
  private Integer comment_id;
  private String uri;
  private Integer comment_parent_id;
  private String organization;

  // http://stackoverflow.com/questions/15974474/mapping-postgresql-json-column-to-hibernate-value-type
  // @Type(type = "StringJsonObject")
  @JsonRawValue
  // otherwise mapping exception
  @Type(type = "JsonObject")
  private JsonObject json_content;

  private String main_resource_uri;
  private String data_object_uri;
  private String document_uri;
  private String comment_uri;
  private Integer access_restriction;
  private Character state;

  private String creator;
  private String modifier;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date creation_date;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date modified_date;

  private String kind;

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#callGetId()
   */
  @Override
  public int callGetId() {
    return getComment_id();
  }

  /*
   * (non-Javadoc)
   * 
   * @see ee.eesti.riha.rest.model.BaseModel#callSetId(int)
   */
  @Override
  public void callSetId(int id) {
    setComment_id(id);
  }

  /**
   * Gets the comment_id.
   *
   * @return the comment_id
   */
  public Integer getComment_id() {
    return comment_id;
  }

  /**
   * Sets the comment_id.
   *
   * @param aComment_id the new comment_id
   */
  @DisallowUseMethodForUpdate
  public void setComment_id(Integer aComment_id) {
    comment_id = aComment_id;
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
    uri = aUri;
  }

  /**
   * Gets the comment_parent_id.
   *
   * @return the comment_parent_id
   */
  public Integer getComment_parent_id() {
    return comment_parent_id;
  }

  /**
   * Sets the comment_parent_id.
   *
   * @param aComment_parent_id the new comment_parent_id
   */
  public void setComment_parent_id(Integer aComment_parent_id) {
    comment_parent_id = aComment_parent_id;
  }

  /**
   * Gets the organization.
   *
   * @return the organization
   */
  public String getOrganization() {
    return organization;
  }

  /**
   * Sets the organization.
   *
   * @param aOrganization the new organization
   */
  public void setOrganization(String aOrganization) {
    organization = aOrganization;
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
    json_content = aJson_content;
  }

  /**
   * Gets the main_resource_uri.
   *
   * @return the main_resource_uri
   */
  public String getMain_resource_uri() {
    return main_resource_uri;
  }

  /**
   * Sets the main_resource_uri.
   *
   * @param aMain_resource_uri the new main_resource_uri
   */
  public void setMain_resource_uri(String aMain_resource_uri) {
    main_resource_uri = aMain_resource_uri;
  }

  /**
   * Gets the data_object_uri.
   *
   * @return the data_object_uri
   */
  public String getData_object_uri() {
    return data_object_uri;
  }

  /**
   * Sets the data_object_uri.
   *
   * @param aData_object_uri the new data_object_uri
   */
  public void setData_object_uri(String aData_object_uri) {
    data_object_uri = aData_object_uri;
  }

  /**
   * Gets the document_uri.
   *
   * @return the document_uri
   */
  public String getDocument_uri() {
    return document_uri;
  }

  /**
   * Sets the document_uri.
   *
   * @param aDocument_uri the new document_uri
   */
  public void setDocument_uri(String aDocument_uri) {
    document_uri = aDocument_uri;
  }

  /**
   * Gets the comment_uri.
   *
   * @return the comment_uri
   */
  public String getComment_uri() {
    return comment_uri;
  }

  /**
   * Sets the comment_uri.
   *
   * @param aComment_uri the new comment_uri
   */
  public void setComment_uri(String aComment_uri) {
    comment_uri = aComment_uri;
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
    access_restriction = aAccess_restriction;
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
    state = aState;
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
    creator = aCreator;
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
    modifier = aModifier;
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
    creation_date = aCreation_date;
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
    modified_date = aModified_date;
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
    kind = aKind;
  }

}
