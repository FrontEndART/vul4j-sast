/* Copyright (C) 2013 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out
 * the documentation coming with IMIS-Labordaten-Application for details.
 */
package de.intevation.lada.model.land;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.intevation.lada.model.Probe;


/**
 * The persistent class for the probe database table.
 */
@Entity
@Table(name="probe")
public class LProbe extends Probe {
    private static final long serialVersionUID = 1L;

    @OneToOne(mappedBy="probe")
    private ProbeTranslation probeTranslation;

    @Column(name="hauptproben_nr")
    private String hauptprobenNr;

    @Column(name="erzeuger_id")
    private Integer erzeugerId;

    @Column(name="mp_kat")
    private String mpKat;

    @Column(name="mpl_id")
    private String mplId;

    @Column(name="mpr_id")
    private Integer mprId;

    @Column(name="probe_nehmer_id")
    private Integer probeNehmerId;

    @Column(name="solldatum_beginn")
    private Timestamp solldatumBeginn;

    @Column(name="solldatum_ende")
    private Timestamp solldatumEnde;

    @Transient
    private boolean readonly;

    @Transient
    private boolean owner;

    public LProbe() {
    }

    public Integer getErzeugerId() {
        return this.erzeugerId;
    }

    public void setErzeugerId(Integer erzeugerId) {
        this.erzeugerId = erzeugerId;
    }

    public String getMpKat() {
        return this.mpKat;
    }

    public void setMpKat(String mpKat) {
        this.mpKat = mpKat;
    }

    public String getMplId() {
        return this.mplId;
    }

    public void setMplId(String mplId) {
        this.mplId = mplId;
    }

    public Integer getMprId() {
        return this.mprId;
    }

    public void setMprId(Integer mprId) {
        this.mprId = mprId;
    }

    public Integer getProbeNehmerId() {
        return this.probeNehmerId;
    }

    public void setProbeNehmerId(Integer probeNehmerId) {
        this.probeNehmerId = probeNehmerId;
    }

    public Timestamp getSolldatumBeginn() {
        return this.solldatumBeginn;
    }

    public void setSolldatumBeginn(Timestamp solldatumBeginn) {
        this.solldatumBeginn = solldatumBeginn;
    }

    public Timestamp getSolldatumEnde() {
        return this.solldatumEnde;
    }

    public void setSolldatumEnde(Timestamp solldatumEnde) {
        this.solldatumEnde = solldatumEnde;
    }

    @JsonIgnore
    public ProbeTranslation getProbeTranslation() {
        return probeTranslation;
    }

    public void setProbeTranslation(ProbeTranslation probeTranslation) {
        this.probeTranslation = probeTranslation;
    }

    public String getHauptprobenNr() {
        return this.hauptprobenNr;
    }

    public void setHauptprobenNr(String hauptprobenNr) {
        this.hauptprobenNr = hauptprobenNr;
    }

    public String getProbeIdAlt() {
        if (this.probeTranslation != null) {
            return this.probeTranslation.getProbeIdAlt();
        }
        else {
            return "";
        }
    }

    public void setProbeIdAlt(String probeIdAlt) {}

    public boolean getIsOwner() {
        return this.owner; //TODO set a valid value in authorization process.
    }

    public void setIsOwner(boolean value) {
        this.owner = value;
    }

    public boolean getReadonly() {
        return this.readonly; //TODO set a valid value in authorization process.
    }

    public void setReadonly(boolean value) {
        this.readonly = value;
    }
}
