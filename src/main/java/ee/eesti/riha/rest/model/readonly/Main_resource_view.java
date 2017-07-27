package ee.eesti.riha.rest.model.readonly;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;
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
    private String uri;

    @JsonRawValue
    @Type(type = "JsonObject")
    private JsonObject json_content;

    private String creator;
    private String modifier;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified_date;

    private String kind;

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
}
