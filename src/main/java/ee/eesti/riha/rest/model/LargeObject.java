package ee.eesti.riha.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.hibernate.JsonObjectUserType;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.sql.Blob;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Entity for holding different large objects like document attachments or arbitrary files
 */
@Entity
@Table(name = "large_object")
@DynamicUpdate
@TypeDefs({@TypeDef(name = "JsonObject", typeClass = JsonObjectUserType.class)})
public class LargeObject {

    @Id
    @GeneratedValue(strategy = AUTO, generator = "large_object_seq")
    @SequenceGenerator(name = "large_object_seq", sequenceName = "large_object_seq")
    @Column(name = "id", updatable = false)
    private int id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "hash")
    private String hash;

    @Column(name = "length")
    private Long length;

    @Lob
    @Column(name = "data")
    private Blob data;

    @JsonIgnore
    @Column(name = "csv_search_content")
    @Type(type = "JsonObject")
    private JsonObject csvSearchContent;

    @JsonIgnore
    @Column(name = "indexed")
    private boolean indexed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }

    public JsonObject getCsvSearchContent() {
        return csvSearchContent;
    }

    public void setCsvSearchContent(JsonObject csvSearchContent) {
        this.csvSearchContent = csvSearchContent;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }
}
