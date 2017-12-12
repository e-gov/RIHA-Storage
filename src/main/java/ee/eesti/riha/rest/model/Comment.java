package ee.eesti.riha.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.hibernate.JsonObjectUserType;
import ee.eesti.riha.rest.model.util.DisallowUseMethodForUpdate;
import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

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
  private Integer comment_parent_id;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date creation_date;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
  @Temporal(TemporalType.TIMESTAMP)
  private Date modified_date;

  @Type(type="pg-uuid")
  private UUID infosystem_uuid;

  @Column(name = "title")
  private String title;

  @Column(name = "comment")
  private String comment;

  @Column(name = "author_name")
  private String author_name;

  @Column(name = "author_personal_code")
  private String author_personal_code;

  @Column(name = "organization_name")
  private String organization_name;

  @Column(name = "organization_code")
  private String organization_code;

  @Column(name = "status")
  private String status;

  @Column(name = "type")
  private String type;

  @Column(name = "sub_type")
  private String sub_type;

  @Column(name = "resolution_type")
  private String resolution_type;

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
   * @return null
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public String getUri() {
    return null;
  }

  /**
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public void setUri(String uri) {
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
  @DisallowUseMethodForUpdate
  public void setComment_parent_id(Integer aComment_parent_id) {
    comment_parent_id = aComment_parent_id;
  }

  /**
   * @return null
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public JsonObject getJson_content() {
    return null;
  }

  /**
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public void setJson_content(JsonObject aJson_content) {
  }

  /**
   * @return null
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public String getCreator() {
    return null;
  }

  /**
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public void setCreator(String creator) {
  }

  /**
   * @return null
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public String getModifier() {
    return null;
  }

  /**
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public void setModifier(String modifier) {
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

  /**
   * @return null
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  public String getKind() {
    return null;
  }

  /**
   * @deprecated not used anymore and will be removed in the future
   */
  @Override
  @Deprecated
  @Transient
  @DisallowUseMethodForUpdate
  public void setKind(String aKind) {
  }

  public UUID getInfosystem_uuid() {
    return infosystem_uuid;
  }

  @DisallowUseMethodForUpdate
  public void setInfosystem_uuid(UUID infosystem_uuid) {
    this.infosystem_uuid = infosystem_uuid;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getAuthor_name() {
    return author_name;
  }

  @DisallowUseMethodForUpdate
  public void setAuthor_name(String author_name) {
    this.author_name = author_name;
  }

  public String getAuthor_personal_code() {
    return author_personal_code;
  }

  @DisallowUseMethodForUpdate
  public void setAuthor_personal_code(String author_personal_code) {
    this.author_personal_code = author_personal_code;
  }

  public String getOrganization_name() {
    return organization_name;
  }

  @DisallowUseMethodForUpdate
  public void setOrganization_name(String organization_name) {
    this.organization_name = organization_name;
  }

  public String getOrganization_code() {
    return organization_code;
  }

  @DisallowUseMethodForUpdate
  public void setOrganization_code(String organization_code) {
    this.organization_code = organization_code;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getType() {
    return type;
  }

  @DisallowUseMethodForUpdate
  public void setType(String type) {
    this.type = type;
  }

  public String getSub_type() {
    return sub_type;
  }

  @DisallowUseMethodForUpdate
  public void setSub_type(String sub_type) {
    this.sub_type = sub_type;
  }

  public String getResolution_type() {
    return resolution_type;
  }

  @DisallowUseMethodForUpdate
  public void setResolution_type(String resolution_type) {
    this.resolution_type = resolution_type;
  }
}
