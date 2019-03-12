package ee.eesti.riha.rest.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.model.util.FieldIsPK;

import static javax.persistence.GenerationType.AUTO;

@Entity
@Table(name = "classifier")
public class Classifier {

	public enum ClassifierDiscriminator {
		TEXT, JSON
	}

	@Id
	@GeneratedValue(strategy = AUTO, generator = "classifier_seq")
	@SequenceGenerator(name = "classifier_seq", sequenceName = "classifier_seq")
	@Column(name = "id", updatable = false)
	@FieldIsPK
	private int id;

	@Column(name = "type")
	private String type;

	@Column(name = "code")
	private String code;

	@Column(name = "value")
	private String value;

	@Column(name = "json_value")
	private String jsonValue;

	@Column(name = "discriminator")
	@Enumerated(EnumType.STRING)
	private ClassifierDiscriminator discriminator;

	@Column(name = "description")
	private String description;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	private Date creationDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Finals.DATE_FORMAT)
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getJsonValue() {
		return jsonValue;
	}

	public void setJsonValue(String jsonValue) {
		this.jsonValue = jsonValue;
	}

	public ClassifierDiscriminator getDiscriminator() {
		return discriminator;
	}

	public void setDiscriminator(ClassifierDiscriminator discriminator) {
		this.discriminator = discriminator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
