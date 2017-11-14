package ee.eesti.riha.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table(name = "main_resource_relation")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Main_resource_relation implements BaseModel {

    @Id
    @GeneratedValue(strategy = AUTO, generator = "main_resource_relation_seq")
    @SequenceGenerator(name = "main_resource_relation_seq", sequenceName = "main_resource_relation_seq")
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

    @Column(name = "related_infosystem_uuid")
    @Type(type = "pg-uuid")
    private UUID related_infosystem_uuid;

    @Column(name = "type")
    private String type;

    public int getMain_resource_relation_id() {
        return main_resource_relation_id;
    }

    public void setMain_resource_relation_id(int main_resource_relation_id) {
        this.main_resource_relation_id = main_resource_relation_id;
    }

    public UUID getInfosystem_uuid() {
        return infosystem_uuid;
    }

    public void setInfosystem_uuid(UUID infosystem_uuid) {
        this.infosystem_uuid = infosystem_uuid;
    }

    public UUID getRelated_infosystem_uuid() {
        return related_infosystem_uuid;
    }

    public void setRelated_infosystem_uuid(UUID related_infosystem_uuid) {
        this.related_infosystem_uuid = related_infosystem_uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int callGetId() {
        return main_resource_relation_id;
    }

    @Override
    public void callSetId(int id) {
        this.main_resource_relation_id = id;
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
    }
}
