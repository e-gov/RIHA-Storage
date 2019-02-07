package ee.eesti.riha.rest.model.readonly;


import ee.eesti.riha.rest.model.util.FieldIsPK;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = DataObjectSearchView.TABLE_NAME)
public class DataObjectSearchView {

    public static final String TABLE_NAME = "data_object_search_view";

    @Column(name = "infosystem")
    private String infosystem;

    @Column(name = "andmeobjekti_nimi")
    private String dataObjectName;

    @Column(name = "kommentaar")
    private String comment;

    @Column(name = "vanemobjekt")
    private String parentObject;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "file_uuid")
    @Id
    @FieldIsPK // this is not a real ID field, but otherwise the search logic does not work
    private UUID fileUuid;

    @Column(name = "DIA")
    private Boolean diaFlag;

    @Column(name = "AV")
    private Boolean avFlag;

    @Column(name = "IA")
    private Boolean iaFlag;

    @Column(name = "PA")
    private Boolean paFlag;




    public String getInfosystem() {
        return infosystem;
    }

    public void setInfosystem(String infosystem) {
        this.infosystem = infosystem;
    }

    public String getDataObjectName() {
        return dataObjectName;
    }

    public void setDataObjectName(String dataObjectName) {
        this.dataObjectName = dataObjectName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getParentObject() {
        return parentObject;
    }

    public void setParentObject(String parentObject) {
        this.parentObject = parentObject;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public UUID getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(UUID fileUuid) {
        this.fileUuid = fileUuid;
    }

    public Boolean getDiaFlag() {
        return diaFlag;
    }

    public void setDiaFlag(Boolean diaFlag) {
        this.diaFlag = diaFlag;
    }

    public Boolean getAvFlag() {
        return avFlag;
    }

    public void setAvFlag(Boolean avFlag) {
        this.avFlag = avFlag;
    }

    public Boolean getIaFlag() {
        return iaFlag;
    }

    public void setIaFlag(Boolean iaFlag) {
        this.iaFlag = iaFlag;
    }

    public Boolean getPaFlag() {
        return paFlag;
    }

    public void setPaFlag(Boolean paFlag) {
        this.paFlag = paFlag;
    }
}
