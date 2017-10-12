package ee.eesti.riha.rest.model;

import org.hibernate.annotations.DynamicUpdate;

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
public class LargeObject {

    @Id
    @GeneratedValue(strategy = AUTO, generator = "large_object_seq")
    @SequenceGenerator(name = "large_object_seq", sequenceName = "large_object_seq")
    @Column(name = "id", updatable = false)
    private int id;

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
}
