package ee.eesti.riha.rest.model.readonly;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.model.hibernate.JsonObjectUserType;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "registered_file_view")
public class RegisteredFileView {

    @EmbeddedId
    private RegisteredFilePK registeredFilePK;

    @Column(name = "file_resource_name")
    private String fileResourceName;

    @Column(name = "file_resource_large_object_id")
    private Integer fileResourceLargeObjectId;

    @Column(name = "infosystem_short_name")
    private String infoSystemShortName;

    @Column(name = "infosystem_name")
    private String infoSystemName;

    @Column(name = "infosystem_owner_name")
    private String infoSystemOwnerName;

    @Column(name = "infosystem_owner_code")
    private String infoSystemOwnerCode;

    public RegisteredFilePK getRegisteredFilePK() {
        return registeredFilePK;
    }

    public void setRegisteredFilePK(RegisteredFilePK registeredFilePK) {
        this.registeredFilePK = registeredFilePK;
    }

    public String getFileResourceName() {
        return fileResourceName;
    }

    public void setFileResourceName(String fileResourceName) {
        this.fileResourceName = fileResourceName;
    }

    public Integer getFileResourceLargeObjectId() {
        return fileResourceLargeObjectId;
    }

    public void setFileResourceLargeObjectId(Integer fileResourceLargeObjectId) {
        this.fileResourceLargeObjectId = fileResourceLargeObjectId;
    }

    public String getInfoSystemShortName() {
        return infoSystemShortName;
    }

    public void setInfoSystemShortName(String infoSystemShortName) {
        this.infoSystemShortName = infoSystemShortName;
    }

    public String getInfoSystemName() {
        return infoSystemName;
    }

    public void setInfoSystemName(String infoSystemName) {
        this.infoSystemName = infoSystemName;
    }

    public String getInfoSystemOwnerName() {
        return infoSystemOwnerName;
    }

    public void setInfoSystemOwnerName(String infoSystemOwnerName) {
        this.infoSystemOwnerName = infoSystemOwnerName;
    }

    public String getInfoSystemOwnerCode() {
        return infoSystemOwnerCode;
    }

    public void setInfoSystemOwnerCode(String infoSystemOwnerCode) {
        this.infoSystemOwnerCode = infoSystemOwnerCode;
    }

    @Embeddable
    public static class RegisteredFilePK implements Serializable {

        @Column(name = "file_resource_uuid")
        @Type(type = "uuid-char")
        private UUID fileResourceUuid;

        @Column(name = "infosystem_uuid")
        @Type(type = "uuid-char")
        private UUID infoSystemUuid;

        public RegisteredFilePK() {
        }

        public RegisteredFilePK(UUID fileResourceUuid, UUID infoSystemUuid) {
            this.fileResourceUuid = fileResourceUuid;
            this.infoSystemUuid = infoSystemUuid;
        }

        public UUID getFileResourceUuid() {
            return fileResourceUuid;
        }

        public void setFileResourceUuid(UUID fileResourceUuid) {
            this.fileResourceUuid = fileResourceUuid;
        }

        public UUID getInfoSystemUuid() {
            return infoSystemUuid;
        }

        public void setInfoSystemUuid(UUID infoSystemUuid) {
            this.infoSystemUuid = infoSystemUuid;
        }

    }

    @Entity
    @Subselect("SELECT" +
            " file_resource_large_object_id AS large_object_id," +
            " record.value AS value" +
            " FROM jsonb_array_elements(" +
            "  (SELECT search_content -> 'records'" +
            "   FROM large_object" +
            "   WHERE id = file_resource_large_object_id)) AS record")
    @TypeDefs({@TypeDef(name = "JsonObject", typeClass = JsonObjectUserType.class)})
    public static class LargeObjectRecord {

        @Id
        @Column(name = "large_object_id")
        Long largeObjectId;

        @Column(name = "value")
        @Type(type = "JsonObject")
        private JsonObject value;

        public Long getLargeObjectId() {
            return largeObjectId;
        }

        public void setLargeObjectId(Long largeObjectId) {
            this.largeObjectId = largeObjectId;
        }

        public JsonObject getValue() {
            return value;
        }

        public void setValue(JsonObject value) {
            this.value = value;
        }
    }
}
