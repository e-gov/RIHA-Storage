package ee.eesti.riha.rest.model.readonly;


import ee.eesti.riha.rest.model.util.FieldIsPK;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = DataObjectSearchView.TABLE_NAME)
public class DataObjectSearchView {

    public static final String TABLE_NAME = "data_object_search_view";

    @Id  // this is not a real ID field, but otherwise the search logic does not work
    @FieldIsPK // this is not a real ID field, but otherwise the search logic does not work
    private String id;

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
    @Type(type = "pg-uuid")
    private UUID fileUuid;

    @Column(name = "DIA")
    private Boolean diaFlag;

    @Column(name = "AV")
    private Boolean avFlag;

    @Column(name = "IA")
    private Boolean iaFlag;

    @Column(name = "PA")
    private Boolean paFlag;

    @Column(name = "personal_data")
    private String personalData;

    @Column(name = "search_name")
    private String searchName;

    @Column(name = "search_text")
    private String searchText;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getPersonalData() {
        return personalData;
    }

    public void setPersonalData(String personalData) {
        this.personalData = personalData;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }
}
