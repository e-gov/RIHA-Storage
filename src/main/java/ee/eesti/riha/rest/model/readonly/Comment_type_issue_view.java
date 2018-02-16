package ee.eesti.riha.rest.model.readonly;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "comment_type_issue_view")
@Immutable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment_type_issue_view implements BaseModel {

    @Id
    @Column(name = "comment_id", updatable = false)
    @FieldIsPK
    private Integer comment_id;

    @Column(name = "comment_parent_id")
    private Integer comment_parent_id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified_date;

    @Column(name = "infosystem_uuid")
    @Type(type = "pg-uuid")
    private UUID infosystem_uuid;

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

    @Column(name = "title")
    private String title;

    @Column(name = "sub_type")
    private String sub_type;

    @Column(name = "resolution_type")
    private String resolution_type;

    @Column(name = "infosystem_short_name")
    private String infosystem_short_name;

    @Column(name = "infosystem_full_name")
    private String infosystem_full_name;

    @JsonRawValue
    private String events;

    public Integer getComment_id() {
        return comment_id;
    }

    public void setComment_id(int comment_id) {
        throw new UnsupportedOperationException();
    }

    public Integer getComment_parent_id() {
        return comment_parent_id;
    }

    public void setComment_parent_id(int comment_parent_id) {
        throw new UnsupportedOperationException();
    }

    public UUID getInfosystem_uuid() {
        return infosystem_uuid;
    }

    public void setInfosystem_uuid(UUID infosystem_uuid) {
        throw new UnsupportedOperationException();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        throw new UnsupportedOperationException();
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        throw new UnsupportedOperationException();
    }

    public String getAuthor_personal_code() {
        return author_personal_code;
    }

    public void setAuthor_personal_code(String author_personal_code) {
        throw new UnsupportedOperationException();
    }

    public String getOrganization_name() {
        return organization_name;
    }

    public void setOrganization_name(String organization_name) {
        throw new UnsupportedOperationException();
    }

    public String getOrganization_code() {
        return organization_code;
    }

    public void setOrganization_code(String organization_code) {
        throw new UnsupportedOperationException();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        throw new UnsupportedOperationException();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        throw new UnsupportedOperationException();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        throw new UnsupportedOperationException();
    }

    public String getSub_type() {
        return sub_type;
    }

    public void setSub_type(String sub_type) {
        throw new UnsupportedOperationException();
    }

    public String getResolution_type() {
        return resolution_type;
    }

    public void setResolution_type(String resolution_type) {
        throw new UnsupportedOperationException();
    }

    public String getInfosystem_short_name() {
        return infosystem_short_name;
    }

    public void setInfosystem_short_name(String infosystem_short_name) {
        throw new UnsupportedOperationException();
    }

    public String getInfosystem_full_name() {
        return infosystem_full_name;
    }

    public void setInfosystem_full_name(String infosystem_full_name) {
        throw new UnsupportedOperationException();
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int callGetId() {
        return comment_id;
    }

    @Override
    public void callSetId(int id) {
        throw new UnsupportedOperationException();
    }

    /**
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
        throw new UnsupportedOperationException();
    }

    /**
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
    public void setJson_content(JsonObject json_content) {
        throw new UnsupportedOperationException();
    }

    /**
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
        throw new UnsupportedOperationException();
    }

    /**
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getCreation_date() {
        return creation_date;
    }

    @Override
    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    @Override
    public Date getModified_date() {
        return modified_date;
    }

    @Override
    public void setModified_date(Date modified_date) {
        this.modified_date = modified_date;
    }

    /**
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
    public void setKind(String kind) {
        throw new UnsupportedOperationException();
    }
}
