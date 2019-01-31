package ee.eesti.riha.rest.model.readonly;

import com.fasterxml.jackson.annotation.*;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.hibernate.JsonObjectUserType;
import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.Date;

@TypeDefs({@TypeDef(name = "JsonObject", typeClass = JsonObjectUserType.class)})
@Transactional
@Entity
@Table(name = "main_resource_view")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Immutable
public class Main_resource_view implements BaseModel {

    @FieldIsPK
    @Id
    @Column(updatable = false)
    private Integer main_resource_id;

    @JsonIgnore
    private String uri;

    @JsonRawValue
    @Type(type = "JsonObject")
    private JsonObject json_content;

    @JsonIgnore
    private String creator;

    @JsonIgnore
    private String modifier;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation_date;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified_date;

    @JsonIgnore
    private String kind;

    // Needed for indication that this field should not be searched in json content
    @JsonIgnore
    private String search_content;

    @JsonIgnore
    @Column(name = "j_creation_timestamp")
    private Date j_creation_timestamp;

    @JsonIgnore
    @Column(name = "j_update_timestamp")
    private Date j_update_timestamp;

    @Column(name = "last_positive_approval_request_type")
    private String last_positive_approval_request_type;

    @Column(name = "last_positive_approval_request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_positive_approval_request_date;

    @Column(name = "last_positive_establishment_request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_positive_establishment_request_date;

    @Column(name = "last_positive_take_into_use_request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_positive_take_into_use_request_date;

    @Column(name = "last_positive_finalization_request_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_positive_finalization_request_date;

    @Column(name = "has_used_system_type_relations")
    private boolean hasUsedSystemTypeRelations;

    public Integer getMain_resource_id() {
        return main_resource_id;
    }

    @Override
    public int callGetId() {
        return getMain_resource_id();
    }

    @Override
    public void callSetId(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonObject getJson_content() {
        return json_content;
    }

    @Override
    public void setJson_content(JsonObject json_content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public void setCreator(String creator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getModifier() {
        return modifier;
    }

    @Override
    public void setModifier(String modifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getCreation_date() {
        return creation_date;
    }

    @Override
    public void setCreation_date(Date creation_date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getModified_date() {
        return modified_date;
    }

    @Override
    public void setModified_date(Date modified_date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getKind() {
        return kind;
    }

    @Override
    public void setKind(String kind) {
        throw new UnsupportedOperationException();
    }

    public boolean isHasUsedSystemTypeRelations() {
        return hasUsedSystemTypeRelations;
    }

    public void setHasUsedSystemTypeRelations(boolean hasUsedSystemTypeRelations) {
        this.hasUsedSystemTypeRelations = hasUsedSystemTypeRelations;
    }

    public String getLast_positive_approval_request_type() {
        return last_positive_approval_request_type;
    }

    public void setLast_positive_approval_request_type(String last_positive_approval_request_type) {
        this.last_positive_approval_request_type = last_positive_approval_request_type;
    }

    public Date getLast_positive_approval_request_date() {
        return last_positive_approval_request_date;
    }

    public void setLast_positive_approval_request_date(Date last_positive_approval_request_date) {
        this.last_positive_approval_request_date = last_positive_approval_request_date;
    }

    public Date getLast_positive_establishment_request_date() {
        return last_positive_establishment_request_date;
    }

    public void setLast_positive_establishment_request_date(Date last_positive_establishment_request_date) {
        this.last_positive_establishment_request_date = last_positive_establishment_request_date;
    }

    public Date getLast_positive_take_into_use_request_date() {
        return last_positive_take_into_use_request_date;
    }

    public void setLast_positive_take_into_use_request_date(Date last_positive_take_into_use_request_date) {
        this.last_positive_take_into_use_request_date = last_positive_take_into_use_request_date;
    }

    public Date getLast_positive_finalization_request_date() {
        return last_positive_finalization_request_date;
    }

    public void setLast_positive_finalization_request_date(Date last_positive_finalization_request_date) {
        this.last_positive_finalization_request_date = last_positive_finalization_request_date;
    }
}
