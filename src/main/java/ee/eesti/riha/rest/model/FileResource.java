package ee.eesti.riha.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ee.eesti.riha.rest.logic.Finals;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "file_resource")
@DynamicUpdate
public class FileResource {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    @Type(type = "pg-uuid")
    private UUID uuid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "infosystem_uuid")
    @Type(type = "pg-uuid")
    private UUID infoSystemUuid;

    @Column(name = "name")
    private String name;

    @Column(name = "content_type")
    private String contentType;

    @ManyToOne(targetEntity = LargeObject.class)
    @JoinColumn(name = "large_object_id", nullable = false)
    private LargeObject largeObject;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getInfoSystemUuid() {
        return infoSystemUuid;
    }

    public void setInfoSystemUuid(UUID infoSystemUuid) {
        this.infoSystemUuid = infoSystemUuid;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LargeObject getLargeObject() {
        return largeObject;
    }

    public void setLargeObject(LargeObject largeObject) {
        this.largeObject = largeObject;
    }
}
