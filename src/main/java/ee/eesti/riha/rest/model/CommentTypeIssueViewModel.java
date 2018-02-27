package ee.eesti.riha.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
import ee.eesti.riha.rest.logic.Finals;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentTypeIssueViewModel {

    private Integer comment_id;
    private Integer comment_parent_id;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    private Date creation_date;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    private Date modified_date;
    private UUID infosystem_uuid;
    private String comment;
    private String author_name;
    private String author_personal_code;
    private String organization_name;
    private String organization_code;
    private String status;
    private String type;
    private String title;
    private String sub_type;
    private String resolution_type;
    private String infosystem_short_name;
    private String infosystem_full_name;
    @JsonRawValue
    private String events;
    @JsonRawValue
    private String last_comment;

    public Integer getComment_id() {
        return comment_id;
    }

    public void setComment_id(Integer comment_id) {
        this.comment_id = comment_id;
    }

    public Integer getComment_parent_id() {
        return comment_parent_id;
    }

    public void setComment_parent_id(Integer comment_parent_id) {
        this.comment_parent_id = comment_parent_id;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public Date getModified_date() {
        return modified_date;
    }

    public void setModified_date(Date modified_date) {
        this.modified_date = modified_date;
    }

    public UUID getInfosystem_uuid() {
        return infosystem_uuid;
    }

    public void setInfosystem_uuid(UUID infosystem_uuid) {
        this.infosystem_uuid = infosystem_uuid;
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

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public String getAuthor_personal_code() {
        return author_personal_code;
    }

    public void setAuthor_personal_code(String author_personal_code) {
        this.author_personal_code = author_personal_code;
    }

    public String getOrganization_name() {
        return organization_name;
    }

    public void setOrganization_name(String organization_name) {
        this.organization_name = organization_name;
    }

    public String getOrganization_code() {
        return organization_code;
    }

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

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSub_type() {
        return sub_type;
    }

    public void setSub_type(String sub_type) {
        this.sub_type = sub_type;
    }

    public String getResolution_type() {
        return resolution_type;
    }

    public void setResolution_type(String resolution_type) {
        this.resolution_type = resolution_type;
    }

    public String getInfosystem_short_name() {
        return infosystem_short_name;
    }

    public void setInfosystem_short_name(String infosystem_short_name) {
        this.infosystem_short_name = infosystem_short_name;
    }

    public String getInfosystem_full_name() {
        return infosystem_full_name;
    }

    public void setInfosystem_full_name(String infosystem_full_name) {
        this.infosystem_full_name = infosystem_full_name;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }

    public String getLast_comment() {
        return last_comment;
    }

    public void setLast_comment(String last_comment) {
        this.last_comment = last_comment;
    }
}
