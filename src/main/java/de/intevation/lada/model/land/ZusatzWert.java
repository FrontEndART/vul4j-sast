package de.intevation.lada.model.land;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * The persistent class for the zusatz_wert database table.
 * 
 */
@Entity
@Table(name="zusatz_wert")
public class ZusatzWert implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(name="letzte_aenderung")
    private Timestamp letzteAenderung;

    private float messfehler;

    @Column(name="messwert_pzs")
    private double messwertPzs;

    @Column(name="nwg_zu_messwert")
    private double nwgZuMesswert;

    @Column(name="probe_id")
    private Integer probeId;

    @Column(name="pzs_id")
    private String pzsId;

    @Column(name="tree_modified")
    private Timestamp treeModified;

    @OneToOne
    @JoinColumn(name="probe_id", insertable=false, updatable=false)
    private Probe probe;

    @Transient
    private boolean owner;

    @Transient
    private boolean readonly;

    @Transient
    private Timestamp parentModified;

    public ZusatzWert() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getLetzteAenderung() {
        return this.letzteAenderung;
    }

    public void setLetzteAenderung(Timestamp letzteAenderung) {
        this.letzteAenderung = letzteAenderung;
    }

    public float getMessfehler() {
        return this.messfehler;
    }

    public void setMessfehler(float messfehler) {
        this.messfehler = messfehler;
    }

    public double getMesswertPzs() {
        return this.messwertPzs;
    }

    public void setMesswertPzs(double messwertPzs) {
        this.messwertPzs = messwertPzs;
    }

    public double getNwgZuMesswert() {
        return this.nwgZuMesswert;
    }

    public void setNwgZuMesswert(double nwgZuMesswert) {
        this.nwgZuMesswert = nwgZuMesswert;
    }

    public Integer getProbeId() {
        return this.probeId;
    }

    public void setProbeId(Integer probeId) {
        this.probeId = probeId;
    }

    public String getPzsId() {
        return this.pzsId;
    }

    public void setPzsId(String pzsId) {
        this.pzsId = pzsId;
    }

    public Timestamp getTreeModified() {
        return this.treeModified;
    }

    public void setTreeModified(Timestamp treeModified) {
        this.treeModified = treeModified;
    }

    /**
     * @return the owner
     */
    public boolean isOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    /**
     * @return the readonly
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * @param readonly the readonly to set
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Timestamp getParentModified() {
        if (this.parentModified == null && this.probe != null) {
            return this.probe.getTreeModified();
        }
        return this.parentModified;
    }

    public void setParentModified(Timestamp parentModified) {
        this.parentModified = parentModified;
    }
}
