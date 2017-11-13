package ee.eesti.riha.rest.model.readonly;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.BaseModel;
import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "main_resource_relation_view")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_NULL)
@Immutable
public class Main_resource_relation_view implements BaseModel {

    @Id
    @Column(name = "main_resource_relation_id", updatable = false)
    @FieldIsPK
    private int main_resource_relation_id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date creation_date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified_date;

    @Column(name = "infosystem_uuid")
    @Type(type = "pg-uuid")
    private UUID infosystem_uuid;

    @Column(name = "infosystem_name")
    private String infosystem_name;

    @Column(name = "infosystem_short_name")
    private String infosystem_short_name;

    @Column(name = "related_infosystem_uuid")
    @Type(type = "pg-uuid")
    private UUID related_infosystem_uuid;

    @Column(name = "related_infosystem_name")
    private String related_infosystem_name;

    @Column(name = "related_infosystem_short_name")
    private String related_infosystem_short_name;

    @Column(name = "type")
    private String type;

    public int getMain_resource_relation_id() {
        return main_resource_relation_id;
    }

    public void setMain_resource_relation_id(int main_resource_relation_id) {
        throw new UnsupportedOperationException();
    }

    public UUID getInfosystem_uuid() {
        return infosystem_uuid;
    }

    public void setInfosystem_uuid(UUID infosystem_uuid) {
        throw new UnsupportedOperationException();
    }

    public String getInfosystem_name() {
        return infosystem_name;
    }

    public void setInfosystem_name(String infosystem_name) {
        throw new UnsupportedOperationException();
    }

    public String getInfosystem_short_name() {
        return infosystem_short_name;
    }

    public void setInfosystem_short_name(String infosystem_short_name) {
        throw new UnsupportedOperationException();
    }

    public UUID getRelated_infosystem_uuid() {
        return related_infosystem_uuid;
    }

    public void setRelated_infosystem_uuid(UUID related_infosystem_uuid) {
        throw new UnsupportedOperationException();
    }

    public String getRelated_infosystem_name() {
        return related_infosystem_name;
    }

    public void setRelated_infosystem_name(String related_infosystem_name) {
        throw new UnsupportedOperationException();
    }

    public String getRelated_infosystem_short_name() {
        return related_infosystem_short_name;
    }

    public void setRelated_infosystem_short_name(String related_infosystem_short_name) {
        throw new UnsupportedOperationException();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int callGetId() {
        return main_resource_relation_id;
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
