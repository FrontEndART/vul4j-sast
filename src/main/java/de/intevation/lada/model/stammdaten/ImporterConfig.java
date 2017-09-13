package de.intevation.lada.model.stammdaten;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the importer_config database table.
 * 
 */
@Entity
@Table(name="importer_config")
public class ImporterConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Integer id;

    private String action;

    private String attribute;

    @Column(name="from_value")
    private String fromValue;

    @Column(name="mst_id")
    private String mstId;

    private String tablename;

    @Column(name="to_value")
    private String toValue;

    public ImporterConfig() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getFromValue() {
        return this.fromValue;
    }

    public void setFromValue(String fromValue) {
        this.fromValue = fromValue;
    }

    public String getMstId() {
        return this.mstId;
    }

    public void setMstId(String mstId) {
        this.mstId = mstId;
    }

    public String getTablename() {
        return this.tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getToValue() {
        return this.toValue;
    }

    public void setToValue(String toValue) {
        this.toValue = toValue;
    }

}
